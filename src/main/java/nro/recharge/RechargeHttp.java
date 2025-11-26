package nro.recharge;


import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.*;

import nro.jdbc.DBService;
import nro.models.player.Player;
import nro.server.Client;
import nro.services.Service;

public class RechargeHttp {

    private static final int PORT = 8080; 
    private static final String WEBHOOK_KEY = "admindonal"; // Key webhook

    // ✅ Hệ số sự kiện (2.0 = X2, 3.0 = X3, 4.0 = X4, 1.0 = bình thường)
    private static final double HE_SO_SU_KIEN = 2.0;  

    public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/sepay", new SepayHandler());
        server.start();
        System.out.println("🚀 Sepay Webhook chạy tại http://0.0.0.0:" + PORT + "/sepay");
    }

    static class SepayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) {
            try {
                String method = ex.getRequestMethod();

                if (method.equalsIgnoreCase("GET")) {
                    send(ex, 200, "Webhook Sepay đang hoạt động – vui lòng dùng POST");
                    return;
                }

                if (!method.equalsIgnoreCase("POST")) {
                    send(ex, 405, "Method Not Allowed");
                    return;
                }

                // ✅ Kiểm tra API Key
                String auth = ex.getRequestHeaders().getFirst("Authorization");
                if (auth == null || !auth.equals("Apikey " + WEBHOOK_KEY)) {
                    System.out.println("❌ Sai API Key: " + auth);
                    send(ex, 401, "Unauthorized");
                    return;
                }

                // ✅ Đọc body JSON
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println(" Raw body nhận: " + body);

                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                System.out.println("JSON parse: " + json.toString());

                // ✅ Đọc amount
                int amount = 0;
                if (json.has("transferAmount") && !json.get("transferAmount").isJsonNull()) {
                    amount = json.get("transferAmount").getAsInt();
                } else if (json.has("amount_in") && !json.get("amount_in").isJsonNull()) {
                    amount = json.get("amount_in").getAsInt();
                } else if (json.has("amount") && !json.get("amount").isJsonNull()) {
                    amount = json.get("amount").getAsInt();
                }

                // ✅ Đọc description
                String description = json.has("transaction_content")
                        ? json.get("transaction_content").getAsString()
                        : (json.has("content") ? json.get("content").getAsString()
                        : (json.has("description") ? json.get("description").getAsString() : ""));

                // ✅ Transaction id
                String transId = json.has("transaction_id")
                        ? json.get("transaction_id").getAsString()
                        : (json.has("referenceCode") ? json.get("referenceCode").getAsString()
                        : (json.has("id") ? json.get("id").getAsString() : "unknown"));

                // ✅ Regex tìm accountId
                Matcher m = Pattern.compile("NAP[\\s_\\-]?(\\d+)", Pattern.CASE_INSENSITIVE).matcher(description);

                int accountId = -1;
                if (m.find()) {
                    accountId = Integer.parseInt(m.group(1));
                    System.out.println("Tìm thấy accountId = " + accountId + " trong nội dung: " + description);
                } else {
                    System.out.println("️ Không tìm thấy accountId trong nội dung: " + description);
                    saveLog(transId, amount, description, false);
                    send(ex, 200, "Logged but no NAP id");
                    return;
                }

                // ✅ Log để debug
                System.out.println("➡️ accountId=" + accountId + ", amount=" + amount + ", desc=" + description);

                // ✅ Nạp tiền
                processTopup(accountId, amount, "sepay", transId, description);
                send(ex, 200, "OK");

            } catch (Exception e) {
                e.printStackTrace();
                try { send(ex, 500, "Server error"); } catch (Exception ignored) {}
            }
        }
    }

    // ✅ Cộng tiền vào DB + gửi thông báo
    private static void processTopup(int accountId, int amount, String provider, String transId, String desc) {
        int soTienCong = (int)(amount * HE_SO_SU_KIEN); // số tiền nhận sau khi nhân hệ số

        try (Connection con = DBService.gI().getConnectionForGame()) {
            PreparedStatement ps = con.prepareStatement(
                "UPDATE account SET vnd = vnd + ?, tongnap = tongnap + ? WHERE id = ?"
            );
            ps.setInt(1, soTienCong); // cộng vnd theo hệ số
            ps.setInt(2, amount);     // tongnap chỉ cộng số gốc
            ps.setInt(3, accountId);
            int updated = ps.executeUpdate();
            ps.close();

            if (updated > 0) {
                System.out.println(" Nạp thành công: accId=" + accountId + ", +" + soTienCong + " (gốc " + amount + ")");
                saveLog(transId, amount, desc, true);
            } else {
                System.out.println("Không tìm thấy account id=" + accountId);
                saveLog(transId, amount, desc, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Gửi thông báo cho player online
        Player pl = Client.gI().getPlayerByUser(accountId);
        if (pl != null) {
            try {
                pl.getSession().vnd += soTienCong;
                pl.tongnap += amount;
            } catch (Exception ignored) {}
            Service.getInstance().sendThongBao(pl,
                "Bạn đã nạp " + amount + " VNĐ (nhận " + soTienCong + " VNĐ, X" + HE_SO_SU_KIEN + ")");
            Service.getInstance().sendMoney(pl);
        }
    }

    // ✅ Lưu log giao dịch
    private static void saveLog(String transId, int amount, String desc, boolean success) {
        try (Connection con = DBService.gI().getConnectionForGame()) {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO recharge_log(trans_id, amount, description, success, time) VALUES (?,?,?,?,NOW())"
            );
            ps.setString(1, transId);
            ps.setInt(2, amount);
            ps.setString(3, desc);
            ps.setInt(4, success ? 1 : 0);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Trả response UTF-8
    private static void send(HttpExchange ex, int code, String msg) throws Exception {
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }
}
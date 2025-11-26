package nro.services;

import nro.models.player.Player;
import nro.utils.Util;
import nro.jdbc.DBService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import nro.models.item.Item;

public class CccdService {

    private static final int COST_GOLD = 100_000_000; // 100tr vàng
    private static final int CCCD_LENGTH = 12;

    private static CccdService i;

    public static CccdService gI() {
        if (i == null) i = new CccdService();
        return i;
    }
public void register(Player p) {
    if (p == null) return;

    if (p.cccd != null && !p.cccd.isEmpty()) {
        Service.getInstance().sendThongBao(p, "Bạn đã có CCCD: " + p.cccd);
        return;
    }

    final int ITEM_ID = 1559; // id tiền tệ
    final int COST = 200;     // số lượng cần

    // kiểm tra số lượng có đủ không
    int soLuong = InventoryService.gI().getQuantity(p, ITEM_ID);
    if (soLuong < COST) {
        Service.getInstance().sendThongBao(p,
            "Cần " + COST + " vật phẩm (id=" + ITEM_ID + ") để đăng ký CCCD!");
        return;
    }

    // tìm item trong túi theo id
    Item moneyItem = null;
    for (Item it : p.inventory.itemsBag) {
        if (it != null && it.template != null && it.template.id == ITEM_ID) {
            moneyItem = it;
            break;
        }
    }

    if (moneyItem == null) {
        Service.getInstance().sendThongBao(p, "Không tìm thấy vật phẩm trong hành trang!");
        return;
    }

    // trừ item
    InventoryService.gI().subQuantityItemsBag(p, moneyItem, COST);
    InventoryService.gI().sendItemBags(p);

    // tạo số CCCD
    String cccd = randomDigits(CCCD_LENGTH);
    p.cccd = cccd;
    p.cccdDate = System.currentTimeMillis();
    p.cccdPlace = getPlaceByPlanet(p.gender);

    // lưu DB
    boolean ok = updatePlayerCccd(p);
    if (!ok) {
        Service.getInstance().sendThongBao(p, "Lỗi lưu CCCD, thử lại sau.");
        return;
    }

    Service.getInstance().sendThongBao(p,
        "Đăng ký thành công CCCD!\nSố: " + cccd +
        "\nNơi cấp: " + p.cccdPlace +
        "\nNgày cấp: " + Util.dateToString(p.cccdDate));
}



    /** Hiển thị CCCD */
    public void show(Player p) {
        if (p == null) return;
        if (p.cccd == null || p.cccd.isEmpty()) {
            Service.getInstance().sendThongBaoOK(p, "Bạn chưa có CCCD, hãy đến chú công an để đăng ký.");
            return;
        }

        long days = (p.cccdDate > 0)
        ? Math.max(0L, (System.currentTimeMillis() - p.cccdDate) / 86_400_000L)
        : -1L;


        String msg = "Căn Cước Công Dân\n" +
                "Số: " + p.cccd + "\n" +
                "Chủ: player " + p.name + "\n" +
                "Số tuổi bắt (đầu từ ngày đăng kí cccd): " + (days >= 0 ? days + " ngày" : "Không xác định") + "\n" +
                "Nơi cấp: " + (p.cccdPlace == null ? "Không rõ" : p.cccdPlace) + "\n" +
                "Ngày cấp: " + Util.dateToString(p.cccdDate);

        Service.getInstance().sendThongBaoOK(p, msg);
    }

    // ================= HELPER =================

    private boolean updatePlayerCccd(Player p) {
        try (Connection cn = DBService.gI().getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE player SET cccd=?, cccd_date=?, cccd_place=? WHERE id=?")) {
            ps.setString(1, p.cccd);
            ps.setLong(2, p.cccdDate);         // BIGINT -> setLong
            ps.setString(3, p.cccdPlace);
            // Nếu p.id là long trong codebase của bạn, dùng setLong để tránh hẹp kiểu:
            // (Dùng setLong an toàn ngay cả khi cột id là INT)
            ps.setLong(4, p.id);               // 👈 đổi từ setInt(...) -> setLong(...)
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String randomDigits(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(nro.utils.Util.nextInt(0, 9));
        return sb.toString();
    }

    private String getPlaceByPlanet(int gender) {
        switch (gender) {
            case 0: return "Trái Đất";
            case 1: return "Namek";
            case 2: return "Xayda";
            default: return "Liên hành tinh";
        }
    }
}

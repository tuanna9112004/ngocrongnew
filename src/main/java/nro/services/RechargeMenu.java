package nro.services;

import java.io.IOException;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.Service;
import nro.utils.Util;

/**
 * Menu nạp tiền với QR code - Giao diện đẹp như GameDuDoan
 * @author Hoàng Việt - 0857853150
 */
public class RechargeMenu {

    private static RechargeMenu instance;
    
    // Thông tin ngân hàng
    public static final String BANK_CODE = "970422"; // MB Bank
    public static final String BANK_NAME = "MBBank";
    public static final String ACCOUNT_NUMBER = "76763979838638";
    public static final String ACCOUNT_NAME = "LE HONG TUYEN";
    
    // Hệ số sự kiện
    public static final double HE_SO_SU_KIEN = 2.0;

    public static RechargeMenu gI() {
        if (instance == null) {
            instance = new RechargeMenu();
        }
        return instance;
    }

    // ✅ Hiển thị menu nạp tiền với giao diện đẹp (giống GameDuDoan)
    public void openRechargeMenu(Player pl) {
        Message msg = null;
        try {
            if (pl == null || pl.getSession() == null) {
                return;
            }

            msg = new Message(111);
            msg.writer().writeByte(5); // Menu type cho nạp tiền
            
            // Thông tin tài khoản
            msg.writer().writeInt(pl.getSession().userId); // Account ID
            msg.writer().writeUTF(pl.name); // Tên nhân vật
            msg.writer().writeInt(pl.getSession().vnd); // Số dư hiện tại
            msg.writer().writeInt(pl.tongnap); // Tổng nạp
            
            // Thông tin ngân hàng
            msg.writer().writeUTF(BANK_NAME); // Tên ngân hàng
            msg.writer().writeUTF(ACCOUNT_NUMBER); // Số tài khoản
            msg.writer().writeUTF(ACCOUNT_NAME); // Chủ tài khoản
            
            // Nội dung chuyển khoản
            String noiDung = "NAP " + pl.getSession().userId;
            msg.writer().writeUTF(noiDung);
            
            // Link QR code
            String qrUrl = generateQRCode(pl.getSession().userId);
            msg.writer().writeUTF(qrUrl);
            
            // Hệ số sự kiện
            msg.writer().writeDouble(HE_SO_SU_KIEN);
            
            // Danh sách gói khuyến mãi
            msg.writer().writeInt(4); // Số gói
            
            // Gói 1: 50k
            msg.writer().writeInt(50000);
            msg.writer().writeInt(55000);
            msg.writer().writeUTF("Nạp 50K nhận 55K (+10%)");
            
            // Gói 2: 100k
            msg.writer().writeInt(100000);
            msg.writer().writeInt(120000);
            msg.writer().writeUTF("Nạp 100K nhận 120K (+20%)");
            
            // Gói 3: 500k
            msg.writer().writeInt(500000);
            msg.writer().writeInt(650000);
            msg.writer().writeUTF("Nạp 500K nhận 650K (+30%)");
            
            // Gói 4: 1 triệu
            msg.writer().writeInt(1000000);
            msg.writer().writeInt(1500000);
            msg.writer().writeUTF("Nạp 1 TRIỆU nhận 1,5 TRIỆU (+50%)");
            
            // Hướng dẫn
            msg.writer().writeUTF("⚠️ Chuyển khoản ĐÚNG nội dung: " + noiDung);
            msg.writer().writeUTF("💰 Tiền tự động cộng sau 1-5 phút");
            msg.writer().writeUTF("🎁 Đang có sự kiện nạp X" + (int)HE_SO_SU_KIEN);
            
            // Thông báo cuộn (giống GameDuDoan)
            msg.writer().writeInt(3); // Số dòng thông báo
            msg.writer().writeUTF("🎉 Chúc mừng [Player1] vừa nạp 500K VNĐ!");
            msg.writer().writeUTF("💎 [Player2] đã nạp tổng 5 TRIỆU VNĐ!");
            msg.writer().writeUTF("⭐ Sự kiện X" + (int)HE_SO_SU_KIEN + " đang diễn ra!");
            
            pl.sendMessage(msg);
            msg.cleanup();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ Tạo link QR code từ account ID
    private String generateQRCode(int accountId) {
        String noiDung = "NAP " + accountId;
        
        // Encode nội dung và tên chủ TK
        String encodedContent = noiDung.replace(" ", "%20");
        String encodedName = ACCOUNT_NAME.replace(" ", "%20");
        
        // VietQR API format
        String qrUrl = String.format(
            "https://img.vietqr.io/image/%s-%s-compact2.png?amount=&addInfo=%s&accountName=%s",
            BANK_CODE,
            ACCOUNT_NUMBER,
            encodedContent,
            encodedName
        );
        
        return qrUrl;
    }

    // ✅ Gửi thông báo khi mở menu (dạng popup)
    public void sendRechargeNotification(Player pl) {
        Message msg = null;
        try {
            msg = new Message(111);
            msg.writer().writeByte(6); // Notification type
            msg.writer().writeUTF("📱 Hướng dẫn nạp tiền đã được gửi đến bạn!");
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Hiển thị lịch sử nạp tiền (menu phụ)
    public void showRechargeHistory(Player pl) {
        Message msg = null;
        try {
            if (pl == null || pl.getSession() == null) {
                return;
            }

            msg = new Message(111);
            msg.writer().writeByte(7); // History menu type
            
            msg.writer().writeInt(pl.getSession().userId);
            msg.writer().writeInt(pl.tongnap); // Tổng nạp
            
            // TODO: Lấy từ database recharge_log
            // Mock data:
            msg.writer().writeInt(5); // Số giao dịch
            
            for (int i = 0; i < 5; i++) {
                msg.writer().writeUTF("2025-01-15 10:30:00"); // Thời gian
                msg.writer().writeInt(100000 * (i + 1)); // Số tiền
                msg.writer().writeUTF("Thành công"); // Trạng thái
                msg.writer().writeUTF("Trans_" + (12345 + i)); // Mã GD
            }
            
            pl.sendMessage(msg);
            msg.cleanup();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ Cập nhật thông tin realtime (gọi định kỳ hoặc khi có thay đổi)
    public void updateRechargeInfo(Player pl) {
        Message msg = null;
        try {
            msg = new Message(111);
            msg.writer().writeByte(8); // Update type
            msg.writer().writeInt(pl.getSession().vnd); // Số dư mới
            msg.writer().writeInt(pl.tongnap); // Tổng nạp mới
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Tính tiền bonus theo số tiền nạp
    public int calculateBonus(int amount) {
        if (amount >= 1000000) {
            return (int)(amount * 0.50); // +50%
        } else if (amount >= 500000) {
            return (int)(amount * 0.30); // +30%
        } else if (amount >= 100000) {
            return (int)(amount * 0.20); // +20%
        } else if (amount >= 50000) {
            return (int)(amount * 0.10); // +10%
        }
        return 0;
    }

    // ✅ Thông báo nạp tiền thành công (hiển thị đẹp)
    public void notifyRechargeSuccess(Player pl, int amount, int received) {
        try {
            Message msg = new Message(111);
            msg.writer().writeByte(9); // Success notification type
            msg.writer().writeUTF(pl.name);
            msg.writer().writeInt(amount);
            msg.writer().writeInt(received);
            msg.writer().writeInt(pl.tongnap);
            msg.writer().writeInt(pl.getSession().vnd);
            pl.sendMessage(msg);
            msg.cleanup();
            
            // Thông báo cho tất cả player (optional)
            String announcement = "🎉 Chúc mừng " + pl.name + " vừa nạp " 
                                + Util.numberToMoney(amount) + " VNĐ!";
            Service.getInstance().sendThongBaoAllPlayer(announcement);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Đóng menu nạp tiền
    public void closeRechargeMenu(Player pl) {
        try {
            Message msg = new Message(111);
            msg.writer().writeByte(10); // Close menu
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
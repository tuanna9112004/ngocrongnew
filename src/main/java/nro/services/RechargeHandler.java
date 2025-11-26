package nro.services;


import java.io.IOException;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.func.Input;

/**
 * Xử lý tương tác với menu nạp tiền
 * Tương tự như GameDuDoan xử lý đặt cược
 */
public class RechargeHandler {

    private static RechargeHandler instance;

    public static RechargeHandler gI() {
        if (instance == null) {
            instance = new RechargeHandler();
        }
        return instance;
    }

    // ✅ Xử lý khi player click vào menu nạp tiền
    public void handleRechargeMenu(Player pl, byte action) {
        if (pl == null || pl.getSession() == null) {
            return;
        }

        switch (action) {
            case 0: // Mở menu nạp tiền
                RechargeMenu.gI().openRechargeMenu(pl);
                break;
                
            case 1: // Làm mới QR code
                RechargeMenu.gI().openRechargeMenu(pl);
                Service.getInstance().sendThongBao(pl, "Đã làm mới mã QR!");
                break;
                
            case 2: // Xem lịch sử nạp tiền
                RechargeMenu.gI().showRechargeHistory(pl);
                break;
                
            case 3: // Copy nội dung chuyển khoản
                String content = "NAP " + pl.getSession().userId;
                Service.getInstance().sendThongBao(pl, "Nội dung CK: " + content);
                break;
                
            case 4: // Copy STK
                Service.getInstance().sendThongBao(pl, 
                    "STK: " + RechargeMenu.ACCOUNT_NUMBER);
                break;
                
            case 5: // Đóng menu
                RechargeMenu.gI().closeRechargeMenu(pl);
                break;
                
            case 6: // Hỏi hỗ trợ
                Service.getInstance().sendThongBao(pl, 
                    "Liên hệ Admin qua Zalo hoặc Telegram để được hỗ trợ!");
                break;
                
            case 7: // Chọn gói khuyến mãi
                showPackageDetails(pl);
                break;
                
            default:
                break;
        }
    }

    // ✅ Hiển thị chi tiết các gói khuyến mãi
    private void showPackageDetails(Player pl) {
        String text = "╔════════════════════════╗\n"
                + "║   GÓI NẠP KHUYẾN MÃI   ║\n"
                + "╚════════════════════════╝\n\n"
                + "🎁 GÓI 1: Nạp 50K\n"
                + "   → Nhận: 55K VNĐ (+10%)\n\n"
                + "🎁 GÓI 2: Nạp 100K\n"
                + "   → Nhận: 120K VNĐ (+20%)\n\n"
                + "🎁 GÓI 3: Nạp 500K\n"
                + "   → Nhận: 650K VNĐ (+30%)\n\n"
                + "💎 GÓI VIP: Nạp 1 TRIỆU\n"
                + "   → Nhận: 1,5 TRIỆU VNĐ (+50%)\n\n"
                + "⚡ Sự kiện X" + (int)RechargeMenu.HE_SO_SU_KIEN 
                + " đang diễn ra!\n"
                + "Nội dung CK: NAP " + pl.getSession().userId;
        
        Service.getInstance().sendThongBaoOK(pl, text);
    }

    // ✅ Xử lý tin nhắn từ client (tương tự GameDuDoan xử lý chat)
    public void handleRechargeMessage(Player pl, Message msg) {
        try {
            byte type = msg.reader().readByte();
            
            switch (type) {
                case 0: // Mở menu
                    RechargeMenu.gI().openRechargeMenu(pl);
                    break;
                    
                case 1: // Refresh
                    RechargeMenu.gI().updateRechargeInfo(pl);
                    break;
                    
                case 2: // Xem lịch sử
                    RechargeMenu.gI().showRechargeHistory(pl);
                    break;
                    
//                case 3: // Chat/Hỏi đáp
//                    Input.gI().createFormRechargeSupport(pl);
//                    break;
                    
                default:
                    break;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ Kiểm tra điều kiện mở menu
    public boolean canOpenRechargeMenu(Player pl) {
        if (pl == null || pl.getSession() == null) {
            return false;
        }
        
        // Kiểm tra thành viên (nếu cần)
        if (!pl.getSession().actived) {
            Service.getInstance().sendThongBao(pl, 
                "Yêu cầu mở thành viên để nạp tiền!");
            return false;
        }
        
        return true;
    }

    // ✅ Gửi thông báo có người vừa nạp (broadcast)
    public void broadcastRecharge(String playerName, int amount) {
        try {
            Message msg = new Message(111);
            msg.writer().writeByte(11); // Broadcast type
            msg.writer().writeUTF("🎉 " + playerName + " vừa nạp " 
                                + nro.utils.Util.numberToMoney(amount) + " VNĐ!");
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Cập nhật thông tin realtime cho tất cả player đang xem menu
    public void updateAllRechargeMenus() {
        // TODO: Lặp qua tất cả player đang mở menu nạp tiền
        // và gọi RechargeMenu.gI().updateRechargeInfo(player);
    }
}
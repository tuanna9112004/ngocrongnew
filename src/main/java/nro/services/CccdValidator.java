package nro.services;


import nro.models.player.Player;

public class CccdValidator {

    private static final int ADULT_DAYS = 18; // đủ 18 ngày kể từ đăng ký CCCD

    private static CccdValidator i;

    public static CccdValidator gI() {
        if (i == null) i = new CccdValidator();
        return i;
    }

    /** Kiểm tra có CCCD hay chưa */
    public boolean hasCccd(Player p) {
        if (p == null) return false;
        return (p.cccd != null && !p.cccd.isEmpty() && p.cccdDate > 0);
    }

    /** Trả về số ngày đã đăng ký CCCD */
    public long getCccdDays(Player p) {
        if (!hasCccd(p)) return 0;
        return (System.currentTimeMillis() - p.cccdDate) / 86_400_000L;
    }

//    /** Kiểm tra đã đủ tuổi trưởng thành chưa */
//    public boolean isAdult(Player p) {
//        if (p == null) return false;
//        if (!hasCccd(p)) {
//            Service.getInstance().sendThongBao(p, "❌ Bạn cần có CCCD để sử dụng chức năng này!");
//            return false;
//        }
//        long days = getCccdDays(p);
//        if (days < ADULT_DAYS) {
//            Service.getInstance().sendThongBao(p,
//                "❌ Bạn chưa đủ tuổi trưởng thành (" + ADULT_DAYS + " ngày kể từ khi đăng ký CCCD)!");
//            return false;
//        }
//        return true;
//    }
}

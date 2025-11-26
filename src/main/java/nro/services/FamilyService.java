package nro.services;

import nro.jdbc.daos.FamilyDAO;
import nro.models.item.Item;
import nro.models.player.Family;
import nro.models.player.Player;
import nro.server.Client;

public class FamilyService {

    private static final int MAX_CHILD_LEVEL = 18;
    private static final long PREGNANCY_TIME = (9 * 60 + 10) * 60 * 1000L; // 9h10p = 33.600.000 ms

    private static FamilyService i;
    public static FamilyService gI() {
        if (i == null) i = new FamilyService();
        return i;
    }

    /** Load thông tin family khi login */
    public void onLogin(Player p) {
        FamilyDAO.load(p);
        if (p.nPoint != null) {
            recalc(p);
        }
    }

    // =========================================================================
    // KẾT HÔN
    // =========================================================================
public void proposeMarriage(Player a, Player b) {
    if (!validateMarriagePair(a, b)) return;

    long now = System.currentTimeMillis();
    
    a.family.spouseId = b.id;
    a.family.marriedAt = now;
    a.family.status = Family.STATUS_MARRIED;

    b.family.spouseId = a.id;
    b.family.marriedAt = now;
    b.family.status = Family.STATUS_MARRIED;

    // ✅ THAY 2 DÒNG NÀY
    // FamilyDAO.save(a);
    // FamilyDAO.save(b);
    
    // BẰNG DÒNG NÀY:
    boolean saved = FamilyDAO.saveBoth(a, b);
    
    if (saved) {
        Service.getInstance().sendThongBao(a, "✓ Kết hôn thành công với " + b.name);
        Service.getInstance().sendThongBao(b, "✓ Kết hôn thành công với " + a.name);
        recalc(a);
        recalc(b);
    } else {
        // Rollback nếu lỗi
        a.family.spouseId = 0;
        a.family.status = Family.STATUS_SINGLE;
        b.family.spouseId = 0;
        b.family.status = Family.STATUS_SINGLE;
        Service.getInstance().sendThongBao(a, "✗ Lỗi lưu dữ liệu!");
        Service.getInstance().sendThongBao(b, "✗ Lỗi lưu dữ liệu!");
    }
}

    private boolean validateMarriagePair(Player a, Player b) {
        if (a == null || b == null) {
            if (a != null) Service.getInstance().sendThongBao(a, " Người chơi không hợp lệ");
            return false;
        }
        if (a.id == b.id) {
            Service.getInstance().sendThongBao(a, " Không thể kết hôn với chính mình");
            return false;
        }
        if (a.family == null || b.family == null) {
            Service.getInstance().sendThongBao(a, " Không tải được thông tin gia đình");
            return false;
        }
        if (a.family.status == Family.STATUS_MARRIED) {
            Service.getInstance().sendThongBao(a, " Bạn đã kết hôn rồi!");
            return false;
        }
        if (b.family.status == Family.STATUS_MARRIED) {
            Service.getInstance().sendThongBao(a, " Đối phương đã kết hôn!");
            return false;
        }
        return true;
    }

    // =========================================================================
    // NHẬN CON / THAI KỲ / SINH CON
    // =========================================================================
    public void adoptChild(Player p) {
//        if (!CccdValidator.gI().isAdult(p)) return; // check CCCD + tuổi

        if (p == null || p.family == null || (!p.family.isMarried() && p.family.status != Family.STATUS_WIDOW)) {
            if (p != null) Service.getInstance().sendThongBao(p, "Bạn chưa kết hôn");
            return;
        }

        if (p.family.childLevel == 0 && !p.family.isPregnant) {
            p.family.isPregnant = true;
            p.family.pregnancyStart = System.currentTimeMillis();
            FamilyDAO.save(p);
            Service.getInstance().sendThongBao(p, " Bạn đã bắt đầu mang thai, hãy chờ 9 giờ 10 phút để sinh con!");
            return;
        }

        if (p.family.isPregnant) {
            giveBirth(p);
            return;
        }

        Service.getInstance().sendThongBao(p, "Bạn đã có con rồi");
    }

    public void giveBirth(Player p) {
      //  if (!CccdValidator.gI().isAdult(p)) return; // check CCCD + tuổi

        if (p == null || p.family == null || !p.family.isPregnant) {
            if (p != null) Service.getInstance().sendThongBao(p, "Bạn không trong thai kỳ.");
            return;
        }
        long elapsed = System.currentTimeMillis() - p.family.pregnancyStart;
        if (elapsed < PREGNANCY_TIME) {
            long remainMin = (PREGNANCY_TIME - elapsed) / 60000;
            Service.getInstance().sendThongBao(p, "Bạn cần chờ thêm " + remainMin + " phút nữa để sinh con.");
            return;
        }

        Item cost1 = InventoryService.gI().findItemBagByTemp(p, 1559); // tiền tệ id=1559
        if (cost1 == null || cost1.quantity < 200) {
            Service.getInstance().sendThongBao(p, " Bạn cần x200 tiền tệ để sinh con.");
            return;
        }

        InventoryService.gI().subQuantityItemsBag(p, cost1, 200);
        InventoryService.gI().sendItemBags(p);

        p.family.isPregnant = false;
        p.family.pregnancyStart = 0;
        setChildLevelBoth(p, (byte) 1, 0);

        Service.getInstance().sendThongBao(p, " Xin chúc mừng! Bạn đã sinh con Lv1.");
    }

    // =========================================================================
    // NUÔI CON (THÊM EXP)
    // =========================================================================
   public void addChildExp(Player p, int exp) {
    if (p == null || exp <= 0 || p.family == null || p.family.childLevel <= 0) return;
    if (p.family.childLevel >= MAX_CHILD_LEVEL) return;

    p.family.childExp += exp;
    boolean leveled = false;

    // Check lên cấp
    while (p.family.childLevel < MAX_CHILD_LEVEL && p.family.childExp >= needExp(p.family.childLevel)) {
        p.family.childExp = 0;
        p.family.childLevel++;
        leveled = true;
    }

    // Đồng bộ với vợ/chồng
    Player spouse = getSpouseOnline(p);
    if (spouse != null && spouse.family != null && spouse.family.status == Family.STATUS_MARRIED) {
        spouse.family.childLevel = p.family.childLevel;
        spouse.family.childExp = p.family.childExp;
        
        // ✅ Lưu cả 2 trong transaction
        FamilyDAO.saveBoth(p, spouse);
    } else {
        // Chỉ lưu 1 người nếu spouse offline
        FamilyDAO.save(p);
    }

    // Update chỉ số
    recalc(p);
    if (spouse != null) {
        recalc(spouse);
    }

    // Thông báo
    if (leveled) {
        int buffPercent = p.family.childLevel * 10;
        Service.getInstance().sendThongBao(p, "✓ Con bạn đã lên Lv" + p.family.childLevel +
                " (+Buff HP/MP/Dame " + buffPercent + "%)");
        if (spouse != null) {
            Service.getInstance().sendThongBao(spouse, "✓ Con chung đã lên Lv" + spouse.family.childLevel +
                    " (+Buff HP/MP/Dame " + buffPercent + "%)");
        }
    }
}

    // =========================================================================
    // LY HÔN
    // =========================================================================
    public void divorce(Player p) {
        if (p == null || p.family == null || !p.family.isMarried()) {
            if (p != null) Service.getInstance().sendThongBao(p, "Bạn chưa kết hôn");
            return;
        }
        Player spouse = getSpouseOnline(p);

        p.family.status = Family.STATUS_DIVORCED;
        p.family.spouseId = 0;
        p.family.marriedAt = 0;
        p.family.resetChild();
        p.family.isPregnant = false;
        p.family.pregnancyStart = 0;
        FamilyDAO.save(p);
        recalc(p);
        Service.getInstance().sendThongBao(p, " Bạn đã chủ động ly hôn và mất con.");

        if (spouse != null && spouse.family != null) {
            spouse.family.status = Family.STATUS_WIDOW;
            spouse.family.spouseId = 0;
            spouse.family.marriedAt = 0;
            spouse.family.isPregnant = false;
            spouse.family.pregnancyStart = 0;
            if (spouse.family.childLevel > 0) {
                spouse.family.childLevel = 1;
                spouse.family.childExp = 0;
            }
            FamilyDAO.save(spouse);
            recalc(spouse);
            Service.getInstance().sendThongBao(spouse,
                "Bạn đã trở thành goá phụ, con reset về Lv1 nhưng vẫn thuộc quyền nuôi của bạn.");
        }
    }

    // =========================================================================
    // BỎ CON
    // =========================================================================
    public void abandonChild(Player p) {
        if (p == null || p.family == null ||
            (p.family.status != Family.STATUS_MARRIED && p.family.status != Family.STATUS_WIDOW)) {
            if (p != null) Service.getInstance().sendThongBao(p, "Bạn chưa kết hôn/goá phụ hoặc không hợp lệ");
            return;
        }
        if (p.family.childLevel == 0) {
            Service.getInstance().sendThongBao(p, "Bạn chưa có con");
            return;
        }
        p.family.resetChild();
        p.family.isPregnant = false;
        p.family.pregnancyStart = 0;
        FamilyDAO.save(p);
        recalc(p);

        Player spouse = getSpouseOnline(p);
        if (spouse != null && spouse.family != null && spouse.family.status == Family.STATUS_MARRIED) {
            spouse.family.resetChild();
            spouse.family.isPregnant = false;
            spouse.family.pregnancyStart = 0;
            FamilyDAO.save(spouse);
            recalc(spouse);
            Service.getInstance().sendThongBao(spouse, " Con chung đã được bỏ.");
        }
        Service.getInstance().sendThongBao(p, " Bạn đã bỏ con.");
    }

    // =========================================================================
    // TIỆN ÍCH
    // =========================================================================
    private int needExp(int currentLv) {
        return 1000 * currentLv;
    }

    public Player getSpouseOnline(Player p) {
        if (p == null || p.family == null || p.family.spouseId <= 0) return null;
        for (Player online : Client.gI().getPlayers()) {
            if (online.id == p.family.spouseId) {
                return online;
            }
        }
        return null;
    }

    private void setChildLevelBoth(Player p, byte level, int exp) {
        p.family.childLevel = level;
        p.family.childExp = exp;
        FamilyDAO.save(p);
        Player spouse = getSpouseOnline(p);
        if (spouse != null && spouse.family != null && spouse.family.status == Family.STATUS_MARRIED) {
            spouse.family.childLevel = level;
            spouse.family.childExp = exp;
            FamilyDAO.save(spouse);
            recalc(spouse);
        }
        recalc(p);
    }

    private void recalc(Player p) {
        if (p != null && p.nPoint != null) {
            int lv = (p.family != null ? p.family.childLevel : 0);
            int bonus = lv * 2; // mỗi Lv +10%

            // reset buff trước khi set mới
            p.nPoint.familyPercentHp   = 0;
            p.nPoint.familyPercentMp   = 0;
            p.nPoint.familyPercentDame = 0;

            if (lv > 0) {
                p.nPoint.familyPercentHp   = bonus;
                p.nPoint.familyPercentMp   = bonus;
                p.nPoint.familyPercentDame = bonus;
            }

            // gọi lại calPoint để áp dụng buff
            p.nPoint.calPoint();

            // full máu/ki hiện tại
            p.nPoint.hp = p.nPoint.hpMax;
            p.nPoint.mp = p.nPoint.mpMax;

            PlayerService.gI().sendInfoHpMpMoney(p);
        }
    }

    public Player findNearestPlayer(Player p) {
        if (p == null || p.zone == null) return null;
        Player nearest = null;
        int minDist = Integer.MAX_VALUE;

        for (Player other : p.zone.getPlayers()) {
            if (other == null || other.id == p.id) continue;
            int dx = Math.abs(p.location.x - other.location.x);
            int dy = Math.abs(p.location.y - other.location.y);
            int dist = dx + dy;
            if (dist < minDist) {
                minDist = dist;
                nearest = other;
            }
        }
        return (nearest != null && minDist <= 200) ? nearest : null;
    }
}

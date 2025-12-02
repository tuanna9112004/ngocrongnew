package nro.models.boss.bosstuonglai;

import nro.consts.ConstItem;
import nro.consts.ConstRatio;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.Manager;
import nro.services.RewardService;
import nro.services.Service;
import nro.utils.Util;

import nro.models.boss.BossManager;
import nro.services.SkillService;
import nro.utils.SkillUtil;

/**
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 */
public class Itachi extends Boss {

    public Itachi() {
        super(BossFactory.ITACHI, BossData.ITACHI);
    }

    @Override
    protected boolean useSpecialSkill() {
        this.playerSkill.skillSelect = this.getSkillSpecial();
        if (SkillService.gI().canUseSkillWithCooldown(this)) {
            SkillService.gI().useSkill(this, null, null, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void attack() {
        try {
            Player pl = getPlayerAttack();
            if (pl != null) {
                if (!useSpecialSkill()) {
                    this.playerSkill.skillSelect = this.getSkillAttack();
                    if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                        if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                            goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50), false);
                        }
                        SkillService.gI().useSkill(this, pl, null, null);
                        checkPlayerDie(pl);
                    } else {
                        goToPlayer(pl, false);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

  @Override
public void rewards(Player pl) {

    int tempId = 1537; // Mảnh Vĩ Thú
    int soluong = 0;

    // Tỉ lệ theo số lượng (càng nhiều càng hiếm)
    if (Util.isTrue(70, 100)) {
        soluong = Util.nextInt(1, 30);
    } else if (Util.isTrue(40, 100)) {
        soluong = Util.nextInt(31, 80);
    } else if (Util.isTrue(15, 100)) {
        soluong = Util.nextInt(81, 150);
    } else if (Util.isTrue(5, 100)) {
        soluong = Util.nextInt(151, 300);
    } else {
        soluong = Util.nextInt(1, 10);
    }

    // =========================
    // Chỉ rơi tối đa 3 gói
    // =========================
    int packages = 3;
    int each = soluong / packages;
    int a = 0;

    for (int i = 0; i < packages; i++) {

        int sl = each;

        // để đảm bảo tổng đúng, gói cuối ăn phần dư
        if (i == packages - 1) {
            sl = soluong - (each * (packages - 1));
        }

        // nếu sl = 0 thì không tạo item
        if (sl <= 0) continue;

        ItemMap itemMap = new ItemMap(
                this.zone,
                tempId,
                sl,
                this.location.x + a,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                pl.id
        );

        Service.getInstance().dropItemMap(this.zone, itemMap);
        a += 15;
    }
}


    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[]{"Oải rồi hả?", "Ê cố lên nhóc",
            "Chán", "Ta có nhầm không nhỉ"};

    }

    @Override
    public void leaveMap() {
        BossFactory.createBoss(BossFactory.ITACHI).setJustRest();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }

}

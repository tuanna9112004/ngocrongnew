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
public class RongDen extends Boss {

    public RongDen() {
        super(BossFactory.RONG_DEN, BossData.RONG_DEN);
    }

    @Override
    protected boolean useSpecialSkill() {
        this.playerSkill.skillSelect = this.getSkillSpecial();
        if (SkillService.gI().canUseSkillWithCooldown(this)) {
            SkillService.gI().useSkill(this, null, null,null);
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
                        SkillService.gI().useSkill(this, pl, null,null);
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
        int a = 0;
        int b = 5;
        for (int i = 0; i < 8; i++) {
            ItemMap itemMap = new ItemMap(this.zone, 16, 1,
                    this.location.x + a, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), -1);
            Service.getInstance().dropItemMap(this.zone, itemMap);
            a += 15;
        }
        for (int i = 0; i < 2; i++) {
            ItemMap itemMap1 = new ItemMap(this.zone, 15, 1,
                    this.location.x + b, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), -1);
            Service.getInstance().dropItemMap(this.zone, itemMap1);
            b += 15;
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
        BossFactory.createBoss(BossFactory.RONG_DEN).setJustRest();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }

}

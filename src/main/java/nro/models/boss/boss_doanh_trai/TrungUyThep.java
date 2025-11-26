package nro.models.boss.boss_doanh_trai;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.services.func.ChangeMapService;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Player;
import nro.services.SkillService;
import nro.utils.SkillUtil;
import nro.utils.Util;

import java.util.List;
import nro.models.map.ItemMap;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.TaskService;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class TrungUyThep extends BossDoanhTrai {

    private boolean activeAttack;

    public TrungUyThep(DoanhTrai doanhTrai) {
        super(BossFactory.TRUNG_UD_THEP, BossData.TRUNG_UY_THEP, doanhTrai);
    }

    @Override
    public void attack() {
        try {
            if (activeAttack) {
                Player pl = getPlayerAttack();
                this.playerSkill.skillSelect = this.getSkillAttack();
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    SkillService.gI().useSkill(this, pl, null,null);
                    checkPlayerDie(pl);
                } else {
                    goToPlayer(pl, false);
                }
            } else {
                List<Player> notBosses = this.zone.getNotBosses();
                for (Player pl : notBosses) {
                    if (pl.location.x >= 650 && !pl.effectSkin.isVoHinh) {
                        this.activeAttack = true;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
    }

    @Override
    public void joinMap() {
        try {
            this.zone = this.doanhTrai.getMapById(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
            ChangeMapService.gI().changeMap(this, this.zone, 900, this.zone.map.yPhysicInTop(900, 100));
        } catch (Exception e) {

        }
    }
     @Override
     public void rewards(Player pl) {
     int[] tempIds1 = new int[]{1564, 1565};
   

        int tempId = -1;
        if (Util.isTrue(30, 100)) {
            tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
        }
        if (tempId != -1) {
            ItemMap itemMap = new ItemMap(this.zone, tempId, 2,
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            if (tempId == 1564) {
            }
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
        generalRewards(pl);
    }

    @Override
    public long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt != null && !SkillUtil.isUseSkillDam(plAtt)) {
            return super.injured(plAtt, damage, piercing, isMobAttack);
        }
        damage = damage / 70;
        if (damage <= 0) {
            damage = 1;
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

}

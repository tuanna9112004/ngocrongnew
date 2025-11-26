package nro.models.boss.boss_doanh_trai;

import nro.consts.ConstPlayer;
import nro.consts.ConstRatio;
import nro.models.boss.BossData;
import nro.models.map.ItemMap;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Player;
import nro.services.RewardService;
import nro.services.Service;
import nro.utils.Util;
import nro.services.SkillService;
import nro.services.TaskService;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class NinjaAoTimFake extends NinjaAoTim {

    public NinjaAoTimFake(byte id, DoanhTrai doanhTrai) {
        super(id, BossData.NINJA_AO_TIM_FAKE, doanhTrai);
        this.typePk = ConstPlayer.PK_ALL;
    }

    @Override
    public void attack() {
        try {
            if (!useSpecialSkill()) {
                if (Util.isTrue(30, ConstRatio.PER100)) {
                    this.talk();
                }
                Player pl = getPlayerAttack();
                this.playerSkill.skillSelect = this.getSkillAttack();
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(50, ConstRatio.PER100)) {
                        goToXY(pl.location.x + Util.nextInt(-20, 20), Util.nextInt(pl.location.y - 80,
                                this.zone.map.yPhysicInTop(pl.location.x, 0)), false);
                    }
                    SkillService.gI().useSkill(this, pl, null,null);
                    checkPlayerDie(pl);
                } else {
                    goToPlayer(pl, false);
                }
            }
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
    }

    @Override
     public void rewards(Player pl) {
     int[] tempIds1 = new int[]{1269};
   

        int tempId = -1;
        if (Util.isTrue(30, 100)) {
            tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
        }
        if (tempId != -1) {
            ItemMap itemMap = new ItemMap(this.zone, tempId, 5,
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            if (tempId == 1564) {
            }
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
        generalRewards(pl);
     }
}
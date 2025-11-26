package nro.models.boss.boss_doanh_trai;

import nro.consts.ConstRatio;
import nro.models.boss.BossData;
import nro.models.map.ItemMap;
import nro.services.func.ChangeMapService;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Player;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.SkillService;
import nro.services.TaskService;
import nro.utils.Util;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class RobotVeSi extends BossDoanhTrai {

    public RobotVeSi(byte id, DoanhTrai doanhTrai) {
        super(id, BossData.ROBOT_VE_SI, doanhTrai);
    }

    @Override
    public void attack() {
        try {
            if (!useSpecialSkill()) {
                Player pl = getPlayerAttack();
                this.playerSkill.skillSelect = this.getSkillAttack();
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(20, ConstRatio.PER100)) {
                        goToXY(pl.location.x + Util.nextInt(-20, 20), Util.nextInt(pl.location.y - 80,
                                this.zone.map.yPhysicInTop(pl.location.x, 0)), false);
                    }
                    SkillService.gI().useSkill(this, pl, null,null);
                    checkPlayerDie(pl);
                } else {
                    goToPlayer(pl, false);
                }
            }
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
            ItemMap itemMap = new ItemMap(this.zone, tempId, 20,
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
    public void joinMap() {
        try {
            this.zone = this.doanhTrai.getMapById(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
            int x = Util.nextInt(50, this.zone.map.mapWidth - 50);
            ChangeMapService.gI().changeMap(this, this.zone, x, this.zone.map.yPhysicInTop(x, 100));
        } catch (Exception e) {

        }
    }

}

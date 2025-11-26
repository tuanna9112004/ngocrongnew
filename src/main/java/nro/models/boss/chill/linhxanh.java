package nro.models.boss.chill;

import nro.models.boss.bosstuonglai.*;
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
import nro.server.ServerNotify;
import nro.services.SkillService;
import nro.services.TaskService;
import nro.services.func.ChangeMapService;
import nro.utils.SkillUtil;

/**
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 */
public class linhxanh extends Boss {

    public linhxanh() {
        super(BossFactory.LINHXANH, BossData.LINHXANH);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }
      
     @Override
    public long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt != null && !SkillUtil.isUseSkillDam(plAtt)) {
            return super.injured(plAtt, damage, piercing, isMobAttack);
        }
        damage = damage / 20;
        if (damage <= 0) {
            damage = 1;
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
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
     int[] tempIds1 = new int[]{1573};
   

        int tempId = -1;
        if (Util.isTrue(15, 100)) {
            tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
        }
        if (tempId != -1) {
            ItemMap itemMap = new ItemMap(this.zone, tempId, 1,
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            if (tempId == 1573) {
                itemMap.options.add(new ItemOption(77, Util.nextInt(37, 45)));
                itemMap.options.add(new ItemOption(103, Util.nextInt(37, 45)));
                itemMap.options.add(new ItemOption(50, Util.nextInt(37, 45)));
                itemMap.options.add(new ItemOption(117, Util.nextInt(15, 25)));
                 if (Util.isTrue(90, 100)) {
                  itemMap.options.add(new ItemOption(93, Util.nextInt(1, 7)));
                 }
            
            }
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
        generalRewards(pl);
    }
    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[]{"HỒ CHÍ MINH MUÔN NĂM?", "ĐẢNG CỘNG SẢN MUÔN NĂM",
            "VIỆT NAM 1", ""};

    }
  @Override
    public void leaveMap() {
        ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.TENNIS_SPACE_SHIP);
        super.leaveMap();
    }

}

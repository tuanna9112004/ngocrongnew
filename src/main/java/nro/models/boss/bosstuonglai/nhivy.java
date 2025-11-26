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
import nro.services.TaskService;
import nro.utils.SkillUtil;

/**
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 */
public class nhivy extends Boss {

    public nhivy() {
        super(BossFactory.NHIVY, BossData.NHIVY);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }
       

//    @Override
//    public void attack() {
//        try {
//            Player pl = getPlayerAttack();
//            if (pl != null) {
//                if (!useSpecialSkill()) {
//                    this.playerSkill.skillSelect = this.getSkillAttack();
//                    if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
//                        if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
//                            goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
//                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50), false);
//                        }
//                        SkillService.gI().useSkill(this, pl, null, null);
//                        checkPlayerDie(pl);
//                    } else {
//                        goToPlayer(pl, false);
//                    }
//                }
//            }
//        } catch (Exception ex) {
//        }
//    }

    @Override
   public void rewards(Player pl) {
     int[] tempIds1 = new int[]{1540};
   

        int tempId = -1;
        if (Util.isTrue(50, 100)) {
            tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
        }
        if (tempId != -1) {
            ItemMap itemMap = new ItemMap(this.zone, tempId, 1,
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            if (tempId == 1540) {
                itemMap.options.add(new ItemOption(77, Util.nextInt(1, 3)));
                itemMap.options.add(new ItemOption(103, Util.nextInt(1, 3)));
                itemMap.options.add(new ItemOption(50, Util.nextInt(1, 3)));
               // itemMap.options.add(new ItemOption(117, Util.nextInt(7, 15)));
            Util.isTrue(80, 100); 
                  itemMap.options.add(new ItemOption(93, Util.nextInt(1, 7)));
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
    protected boolean charge() {

        return true;

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
       Boss nhatvy = BossFactory.createBoss(BossFactory.TAMVY);
                       nhatvy.zone = this.zone;
                                            nhatvy.location.x = this.location.x;
                                            nhatvy.location.y = this.location.y;
                                      
        super.leaveMap();
        BossManager.gI().removeBoss(this);
        this.setJustRestToFuture();
    }

}


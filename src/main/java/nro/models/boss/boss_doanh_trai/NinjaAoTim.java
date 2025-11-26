package nro.models.boss.boss_doanh_trai;

import nro.consts.ConstRatio;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.services.func.ChangeMapService;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Player;
import nro.services.PlayerService;
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
public class NinjaAoTim extends BossDoanhTrai {

    public NinjaAoTim(DoanhTrai doanhTrai) {
        super(BossFactory.NINJA_AO_TIM, BossData.NINJA_AO_TIM, doanhTrai);
    }

    public NinjaAoTim(byte id, BossData bossData, DoanhTrai doanhTrai) {
        super(id, bossData, doanhTrai);
    }

    @Override
    public void attack() {
        try {
            if (!useSpecialSkill()) {
//                if (Util.isTrue(10)) {
//                    this.maxCountIdle = Util.nextInt(5, 10);
//                    this.changeIdle();
//                    return;
//                }
                if (Util.isTrue(10, ConstRatio.PER100)) {
                    this.talk();
                }
                Player pl = getPlayerAttack();
                this.playerSkill.skillSelect = this.getSkillAttack();
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(50, ConstRatio.PER100)) {
                        goToXY(pl.location.x + Util.nextInt(-20, 20), Util.nextInt(pl.location.y
                                - 80, this.zone.map.yPhysicInTop(pl.location.x, 0)), false);
                    }
                    if (Util.isTrue(30, ConstRatio.PER100)) {
                        this.phanThan();
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

    private int maxCountIdle;
    private int countIdle;

    @Override
    public void idle() {
        if (countIdle >= maxCountIdle) {
            this.countIdle = 0;
            this.changeAttack();
        } else {
            if (Util.isTrue(30, ConstRatio.PER100)) {
                this.talk();
            }
            this.countIdle++;
            int xMove = this.location.x += Util.nextInt(-200, 200);
            if (xMove < 50) {
                xMove = 50;
            } else if (xMove > this.zone.map.mapWidth - 50) {
                xMove = this.zone.map.mapWidth - 50;
            }
            PlayerService.gI().playerMove(this, xMove, this.zone.map.yPhysicInTop(xMove, 100));
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
            ItemMap itemMap = new ItemMap(this.zone, tempId, 10,
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

    private boolean phanThan;

    private void phanThan() {
        if (!phanThan) {
            doanhTrai.bosses.add(new NinjaAoTimFake(BossFactory.NINJA_AO_TIM_FAKE_1, this.doanhTrai));
            doanhTrai.bosses.add(new NinjaAoTimFake(BossFactory.NINJA_AO_TIM_FAKE_2, this.doanhTrai));
            doanhTrai.bosses.add(new NinjaAoTimFake(BossFactory.NINJA_AO_TIM_FAKE_3, this.doanhTrai));
            doanhTrai.bosses.add(new NinjaAoTimFake(BossFactory.NINJA_AO_TIM_FAKE_4, this.doanhTrai));
            doanhTrai.bosses.add(new NinjaAoTimFake(BossFactory.NINJA_AO_TIM_FAKE_5, this.doanhTrai));
            doanhTrai.bosses.add(new NinjaAoTimFake(BossFactory.NINJA_AO_TIM_FAKE_6, this.doanhTrai));
            phanThan = true;
        }
    }

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[]{"Hahaha..", "Ngươi nghĩ có thể bắt kịp cựu ninja như ta sao",
            "Còn chậm lắm nhóc ơi", "Chịu thua đi..", "Con già vợ yếu đang chờ ngươi ở nhà kìa!"};
    }

}

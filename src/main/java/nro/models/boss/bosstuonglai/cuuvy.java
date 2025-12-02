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
public class cuuvy extends Boss {

    public cuuvy() {
        super(BossFactory.CUUVY, BossData.CUUVY);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
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

    int[] tempIds1 = new int[]{1524}; // item rơi

    int tempId = -1;
    if (Util.isTrue(70, 100)) { // 70% rơi item
        tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
    }

    if (tempId != -1) {
        // tạo item rơi
        ItemMap itemMap = new ItemMap(this.zone, tempId, 1,
                pl.location.x, 
                this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), 
                pl.id);

        // ❗ XÓA PHẦN TẠO CHỈ SỐ — CHỈ RƠI VẬT PHẨM THUẦN
        // => KHÔNG add options, KHÔNG random gì cả

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
       //Boss nhatvy = BossFactory.createBoss(BossFactory.NHIVY);
                     //  nhatvy.zone = this.zone;
                                        //    nhatvy.location.x = this.location.x;
                                        //    nhatvy.location.y = this.location.y;
                                      
        super.leaveMap();
        BossManager.gI().removeBoss(this);
        this.setJustRestToFuture();
    }

}

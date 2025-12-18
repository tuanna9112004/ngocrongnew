package nro.models.boss.tieudoisatthu;

import nro.models.boss.*;
import nro.models.player.Player;
import nro.services.TaskService;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class So2 extends FutureBoss {

    public So2() {
        super(BossFactory.SO2, BossData.SO2);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }
    @Override
    protected boolean charge() {

        return true;

    }
    @Override
    public void rewards(Player pl) {
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
        this.textTalkMidle = new String[]{"Oải rồi hả?", "Ê cố lên nhóc",
            "Chán", "Đại ca Fide có nhầm không nhỉ"};

    }

    @Override
    public void leaveMap() {
        BossManager.gI().getBossById(BossFactory.SO1).changeToAttack();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }

}

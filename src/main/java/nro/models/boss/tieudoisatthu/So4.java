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
public class So4 extends FutureBoss {

    public So4() {
        super(BossFactory.SO4, BossData.SO4);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
        generalRewards(pl);
    }
    @Override
    protected boolean charge() {

        return true;

    }
    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{""};
        this.textTalkMidle = new String[]{"Oải rồi hả?", "Ê cố lên nhóc",
            "Chán", "Đại ca Fide có nhầm không nhỉ"};
    }

    @Override
    public void leaveMap() {
        BossManager.gI().getBossById(BossFactory.SO3).changeToAttack();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }
}

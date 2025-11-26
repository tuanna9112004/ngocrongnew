package nro.models.boss.robotsatthu;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import nro.consts.ConstItem;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.services.PlayerService;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.TaskService;
import nro.utils.Util;

/**
 *
 * @author ❤Girlkun75❤
 * @copyright ❤Trần Lại❤
 */
public class Android19 extends Boss {

    public Android19() {
        super(BossFactory.ANDROID_19, BossData.ANDROID_19);
    }

    @Override
    public void joinMap() {
        super.joinMap();
    }

    @Override
    public void leaveMap() {
        BossManager.gI().getBossById(BossFactory.ANDROID_20).changeToAttack();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
   public void rewards(Player pl) {
        generalRewards(pl);
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
public void initTalk() {
    this.textTalkBefore = new String[]{
        "|-1|Android 19 – sẵn sàng hút sạch năng lượng của ngươi."
    };
    this.textTalkMidle = new String[]{
        "|-1|Đừng cố chạy... tao sẽ hút cạn kiệt sức lực!",
        "|-1|Tụi mày chỉ là nguồn pin cho tao mà thôi.",
        "|-1|Sức mạnh con người? Đáng thương!",
        "|-1|Lập trình xong rồi – đến giờ hành hình!"
    };
    this.textTalkAfter = new String[]{
        "|-1|Không... không thể nào... hết pin rồi sao..."
    };
}


    @Override
    public long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt != null) {
            switch (plAtt.playerSkill.skillSelect.template.id) {
                case Skill.KAMEJOKO:
                case Skill.MASENKO:
                case Skill.ANTOMIC:
                    int hpHoi = (int) (damage - ((long) damage * 80 / 100));
                    PlayerService.gI().hoiPhuc(this, hpHoi, 0);
                    return 0;
            }
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

}

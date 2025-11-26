package nro.models.boss.robotsatthu;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import nro.consts.ConstItem;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
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
public class Android20 extends Boss {

    public Android20() {
        super(BossFactory.ANDROID_20, BossData.ANDROID_20);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        BossFactory.createBoss(BossFactory.ANDROID_19).zone = this.zone;
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
        "|-1|Ta là Tiến sĩ Gero – cha đẻ của những cơn ác mộng!"
    };
    this.textTalkMidle = new String[]{
        "|-1|Bọn ngươi sẽ phải trả giá cho những gì đã gây ra!",
        "|-1|Trí tuệ của ta vượt xa giới hạn phàm nhân.",
        "|-1|Android của ta... sẽ nghiền nát tất cả!",
        "|-1|Sức mạnh khoa học là tuyệt đối!"
    };
    this.textTalkAfter = new String[]{
        "|-1|Không... ta không thể bị đánh bại bởi... lũ nhãi nhép..."
    };
}


    @Override
    public long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt != null) {
            switch (plAtt.playerSkill.skillSelect.template.id) {
                case Skill.KAMEJOKO:
                case Skill.MASENKO:
                case Skill.ANTOMIC:
                    PlayerService.gI().hoiPhuc(this, damage, 0);
                    return 0;
            }
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

}

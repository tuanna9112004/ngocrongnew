package nro.models.boss.boss_ban_do_kho_bau;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.map.ItemMap;
import nro.models.map.phoban.BanDoKhoBau;
import nro.models.player.Player;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.TaskService;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class Sanji extends BossBanDoKhoBau {

    public Sanji(BanDoKhoBau banDoKhoBau) {
        super(BossFactory.SANJI, BossData.SANJI, banDoKhoBau);
    }

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[]{
            "Đừng có khơi mào một trận chiến nếu ngươi không kết thúc được nó!",
            "Là đàn ông, chúng ta phải sẵn sàng tha thứ cho lời nói dối của phụ nữ!",
            "Dù có chết, tôi cũng không đánh phụ nữ!",
            "Con dao là linh hồn của người đầu bếp, không phải là thứ để các ngươi tự do múa máy như thế!",
            "Miễn là còn điều gì đó cần được bảo vệ, tôi sẽ vẫn tiếp tục chiến đấu!"
        };
    }

    @Override
    public void joinMap() {
        try {
            this.zone = this.banDoKhoBau.getMapById(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
            ChangeMapService.gI().changeMap(this, this.zone, 115, 456);
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
            ItemMap itemMap = new ItemMap(this.zone, tempId, 2,
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
    public void leaveMap() {
        for (BossBanDoKhoBau boss : this.banDoKhoBau.bosses) {
            if (boss.id == BossFactory.ZORO) {
                boss.changeToAttack();
                break;
            }
        }
        super.leaveMap();
    }

}

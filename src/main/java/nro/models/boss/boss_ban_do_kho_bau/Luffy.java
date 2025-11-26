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
public class Luffy extends BossBanDoKhoBau {

    public Luffy(BanDoKhoBau banDoKhoBau) {
        super(BossFactory.LUFFY, BossData.LUFFY, banDoKhoBau);
    }

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[]{
            "Gomu gomu no... pistal",
            "Gomu gomu no... ",
            "Gomu Gomu no Gatling",
            "Gomu Gomu no Bazooka",
            "Ta sẽ trở thành vua hải tặc",
            "Chỉ cần tay chân ta còn cử động được thì ta vô địch!",
            "Bạn bè của ta… dù ta chết… cũng đừng hòng cướp đi bất cứ người nào!!!",
            "Thế giới này chỉ cần có một vua hải tặc thôi!"
        };
    }

    @Override
    public void idle() {
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
    public void joinMap() {
        try {
            this.zone = this.banDoKhoBau.getMapById(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
            ChangeMapService.gI().changeMap(this, this.zone, 165, 456);
        } catch (Exception e) {

        }
    }

}

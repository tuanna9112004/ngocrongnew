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
public class zoro extends BossBanDoKhoBau {

    public zoro(BanDoKhoBau banDoKhoBau) {
        super(BossFactory.ZORO, BossData.ZORO, banDoKhoBau);
    }

   

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[]{
            "Tôi là một thợ săn hải tặc",
            "Nếu ngươi chết, ta sẽ giết ngươi!",
            "Tốt thôi! Tôi thà làm hải tặc còn hơn chết ở đây!",
            "Chỉ những người đã chịu đựng lâu, mới có thể nhìn thấy ánh sáng trong bóng tối",
            "Ngươi muốn giết ta? Ngươi còn không có thể giết ta chán nản!",
            "Nếu tôi chết ở đây, thì tôi là một người đàn ông chỉ có thể đi xa đến mức này",
            "Tôi làm mọi thứ theo cách riêng của tôi! Vì vậy, đừng có nói với tôi về nó!"
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
            ChangeMapService.gI().changeMap(this, this.zone, 240, 456);
        } catch (Exception e) {

        }
    }

    @Override
    public void leaveMap() {
        for (BossBanDoKhoBau boss : this.banDoKhoBau.bosses) {
            if (boss.id == BossFactory.LUFFY) {
                boss.changeToAttack();
                break;
            }
        }
        super.leaveMap();
    }

}

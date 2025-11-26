package nro.services.func.lr;

import nro.models.item.Item;
import nro.models.player.Player;
import nro.services.RewardService;
import nro.services.Service;

import java.util.List;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class LuckyRoundGem extends AbsLuckyRound {

    private static LuckyRoundGem i;

    public static LuckyRoundGem gI() {
        if (i == null) {
            i = new LuckyRoundGem();
        }
        return i;
    }

    private LuckyRoundGem() {
        this.price = 4;
        this.ticket = 821;
        this.icons.add(24100);
        this.icons.add(24100);
        this.icons.add(24100);
        this.icons.add(24100);
        this.icons.add(24100);
        this.icons.add(24100);
        this.icons.add(24100);
    }

    @Override
    public List<Item> reward(Player player, byte quantity) {
        List<Item> list = RewardService.gI().getListItemLuckyRound(player, quantity);
        addItemToBox(player, list);
        return list;
    }

    @Override
    public boolean checkMoney(Player player, int price) {
        if (player.inventory.getRuby()< price) {
            Service.getInstance().sendThongBao(player, "Bạn không đủ hồng ngọc");
            return false;
        }
        return true;
    }

    @Override
    public void payWithMoney(Player player, int price) {
        player.inventory.subGem(price);
    }

}

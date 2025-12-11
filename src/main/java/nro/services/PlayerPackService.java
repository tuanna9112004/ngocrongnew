package nro.services;

import java.util.*;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;
import nro.services.func.CombineServiceNew;
import nro.utils.Util;

public class PlayerPackService {

    // ==========================================
    // CẤU TRÚC CẦU THỦ
    // ==========================================
    public static class PlayerCard {
        public short id;
        public int over;
        public int type;
        public PlayerCard(short id, int over, int type) {
            this.id = id;
            this.over = over;
            this.type = type;
        }
    }

    // ==========================================
    // POOL CẦU THỦ THEO MÙA
    // ==========================================
    private static final List<PlayerCard> ITM = Arrays.asList(
            new PlayerCard((short)1700,25,0),
            new PlayerCard((short)1701,22,0),
            new PlayerCard((short)1702,21,0),
            new PlayerCard((short)1706,25,1),
            new PlayerCard((short)1707,23,1),
            new PlayerCard((short)1708,17,1),
            new PlayerCard((short)1709,14,1),
            new PlayerCard((short)1710,25,2),
            new PlayerCard((short)1711,23,2),
            new PlayerCard((short)1712,21,2),
            new PlayerCard((short)1713,19,2),
            new PlayerCard((short)1714,17,2)
    );

    private static final List<PlayerCard> CC = Arrays.asList(
            new PlayerCard((short)1731,20,0),
            new PlayerCard((short)1732,17,0),
            new PlayerCard((short)1733,18,0),
            new PlayerCard((short)1734,15,0),
            new PlayerCard((short)1735,13,0),
            new PlayerCard((short)1736,21,1),
            new PlayerCard((short)1737,18,1),
            new PlayerCard((short)1738,16,1),
            new PlayerCard((short)1739,15,1),
            new PlayerCard((short)1740,14,1),
            new PlayerCard((short)1741,21,2),
            new PlayerCard((short)1742,17,2),
            new PlayerCard((short)1743,14,2),
            new PlayerCard((short)1744,13,2),
            new PlayerCard((short)1745,12,2)
    );

    private static final List<PlayerCard> BTB = Arrays.asList(
            new PlayerCard((short)1760,14,2),
            new PlayerCard((short)1761,11,2),
            new PlayerCard((short)1762,9,2),
            new PlayerCard((short)1763,8,2),
            new PlayerCard((short)1764,7,2),
            new PlayerCard((short)1765,13,1),
            new PlayerCard((short)1766,10,1),
            new PlayerCard((short)1767,8,1),
            new PlayerCard((short)1768,7,1),
            new PlayerCard((short)1769,6,1),
            new PlayerCard((short)1770,13,0),
            new PlayerCard((short)1771,10,0),
            new PlayerCard((short)1772,8,0),
            new PlayerCard((short)1773,7,0),
            new PlayerCard((short)1774,5,0)
    );

    // ==========================================
    // PACK ALL – RANDOM TẦNG 1
    // ==========================================
    private static List<PlayerCard> randomPoolForAll() {
        int r = Util.nextInt(100);
        if (r < 65) return BTB;  // 65%
        if (r < 95) return CC;   // 30%
        return ITM;              // 5%
    }

    // ==========================================
    // LẤY POOL THEO PACK
    // ==========================================
    public static List<PlayerCard> getPoolByPack(short id) {
        switch (id) {
            case 1680: case 1684: case 1685: case 1686: case 1695:
                return ITM;

            case 1681: case 1687: case 1688: case 1689: case 1696:
                return CC;

            case 1682: case 1690: case 1691: case 1692: case 1697:
                return BTB;

            case 1683: case 1693: case 1694:
                return randomPoolForAll();
        }
        return null;
    }

    // ==========================================
    // BẢNG TỈ LỆ THEO PACK
    // ==========================================
    private static int getWeight(short packId, PlayerCard c) {

        // PACK ITM
        if (packId == 1680 || packId == 1684 || packId == 1685 || packId == 1686 || packId == 1695) {
            if (c.over >= 25) return 1;
            if (c.over >= 23) return 4;
            if (c.over >= 21) return 10;
            if (c.over >= 19) return 20;
            if (c.over >= 17) return 25;
            return 40;
        }

        // PACK CC
        if (packId == 1681 || packId == 1687 || packId == 1688 || packId == 1689 || packId == 1696) {
            if (c.over >= 21) return 2;
            if (c.over >= 20) return 5;
            if (c.over >= 18) return 15;
            if (c.over >= 17) return 20;
            if (c.over >= 15) return 25;
            return 33;
        }

        // PACK EBS
        if (packId == 1682 || packId == 1690 || packId == 1691 || packId == 1692 || packId == 1697) {
            if (c.over >= 14) return 5;
            if (c.over >= 13) return 8;
            if (c.over >= 10) return 15;
            if (c.over >= 8) return 30;
            return 42;
        }

        // PACK ALL
        return Math.max(1, 100 - c.over * 3);
    }

    // ==========================================
    // RANDOM THEO TRỌNG SỐ
    // ==========================================
    private static PlayerCard weightedRandom(short packId, List<PlayerCard> list) {
        int total = 0;
        int[] w = new int[list.size()];

        for (int i = 0; i < list.size(); i++) {
            w[i] = getWeight(packId, list.get(i));
            total += w[i];
        }

        int r = Util.nextInt(total);
        int sum = 0;

        for (int i = 0; i < list.size(); i++) {
            sum += w[i];
            if (r < sum) return list.get(i);
        }

        return list.get(0);
    }

    // ==========================================
    // TẠO ITEM CẦU THỦ
    // ==========================================
    public static Item createPlayerItem(PlayerCard card) {
        Item item = ItemService.gI().createNewItem(card.id);

        // Over 223
        item.itemOptions.add(new ItemOption(223, card.over));

        // Vị trí 220/221/222
        item.itemOptions.add(new ItemOption(getOTP2ByType(card.type), 0));

        return item;
    }

    private static int getOTP2ByType(int type) {
        if (type == 0) return 220;
        if (type == 1) return 221;
        return 222;
    }

    // ==========================================
    // MỞ PACK
    // ==========================================
    public static Item openPack(Player p, Item pack) {

        short id = pack.template.id;
        List<PlayerCard> pool = getPoolByPack(id);

        if (pool == null || pool.isEmpty()) return null;
        if (InventoryService.gI().getCountEmptyBag(p) == 0) return null;

        PlayerCard card = weightedRandom(id, pool);

        Item reward = createPlayerItem(card);

        int level = getLevelByPack(id);

        if (level > 0) {

            int[] sum = {0,1,2,3,5,7};
            int newOver = card.over + sum[level];

            removeOption(reward, 223);
            reward.itemOptions.add(0, new ItemOption(223, newOver));

            reward.itemOptions.add(new ItemOption(72, level));
        }

        InventoryService.gI().addItemBag(p, reward, 1);
        InventoryService.gI().subQuantityItemsBag(p, pack, 1);
        InventoryService.gI().sendItemBags(p);

        CombineServiceNew.gI().sendEffectOpenItem(p, pack.template.iconID, reward.template.iconID);

        return reward;
    }

    private static int getLevelByPack(int id) {
        switch (id) {
            case 1684: case 1687: case 1690: return Util.nextInt(1,3);
            case 1685: case 1688: case 1691: return Util.nextInt(1,5);
            case 1686: case 1689: case 1692: case 1693: return Util.nextInt(3,5);
            case 1694: case 1695: case 1696: case 1697: return 5;
        }
        return 0;
    }

    private static void removeOption(Item item, int otp) {
        item.itemOptions.removeIf(op -> op.optionTemplate.id == otp);
    }
}

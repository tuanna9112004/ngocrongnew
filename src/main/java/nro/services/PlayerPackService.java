package nro.services;

import java.util.*;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;

public class PlayerPackService {

    // ==========================================
    // CẤU TRÚC CẦU THỦ
    // ==========================================
    public static class PlayerCard {

        public short id;
        public int over;
        public int type; // 0 = ST, 1 = CAM, 2 = CB

        public PlayerCard(short id, int over, int type) {
            this.id = id;
            this.over = over;
            this.type = type;
        }
    }

    // ==========================================
    // DANH SÁCH CẦU THỦ THEO MÙA
    // ==========================================
    // ----------- ITM (17–25) -----------
    private static final List<PlayerCard> ITM = Arrays.asList(
            new PlayerCard((short) 1700, 25, 0),
            new PlayerCard((short) 1701, 22, 0),
            new PlayerCard((short) 1702, 21, 0),
            new PlayerCard((short) 1706, 25, 1),
            new PlayerCard((short) 1707, 23, 1),
            new PlayerCard((short) 1708, 17, 1),
            new PlayerCard((short) 1709, 14, 1),
            new PlayerCard((short) 1710, 25, 2),
            new PlayerCard((short) 1711, 23, 2),
            new PlayerCard((short) 1712, 21, 2),
            new PlayerCard((short) 1713, 19, 2),
            new PlayerCard((short) 1714, 17, 2)
    );

    // ----------- CC (13–21) -----------
    private static final List<PlayerCard> CC = Arrays.asList(
            new PlayerCard((short) 1731, 20, 0),
            new PlayerCard((short) 1732, 17, 0),
            new PlayerCard((short) 1733, 18, 0),
            new PlayerCard((short) 1734, 15, 0),
            new PlayerCard((short) 1735, 13, 0),
            new PlayerCard((short) 1736, 21, 1),
            new PlayerCard((short) 1737, 18, 1),
            new PlayerCard((short) 1738, 16, 1),
            new PlayerCard((short) 1739, 15, 1),
            new PlayerCard((short) 1740, 14, 1),
            new PlayerCard((short) 1741, 21, 2),
            new PlayerCard((short) 1742, 17, 2),
            new PlayerCard((short) 1743, 14, 2),
            new PlayerCard((short) 1744, 13, 2),
            new PlayerCard((short) 1745, 12, 2)
    );

    // ----------- BTB (7–14) -----------
    private static final List<PlayerCard> BTB = Arrays.asList(
            new PlayerCard((short) 1760, 14, 2),
            new PlayerCard((short) 1761, 11, 2),
            new PlayerCard((short) 1762, 9, 2),
            new PlayerCard((short) 1763, 8, 2),
            new PlayerCard((short) 1764, 7, 2),
            new PlayerCard((short) 1765, 13, 1),
            new PlayerCard((short) 1766, 10, 1),
            new PlayerCard((short) 1767, 8, 1),
            new PlayerCard((short) 1768, 7, 1),
            new PlayerCard((short) 1769, 6, 1),
            new PlayerCard((short) 1770, 13, 0),
            new PlayerCard((short) 1771, 10, 0),
            new PlayerCard((short) 1772, 8, 0),
            new PlayerCard((short) 1773, 7, 0),
            new PlayerCard((short) 1774, 5, 0)
    );

    // ==========================================
    // LẤY LIST TƯƠNG ỨNG THEO GÓI
    // ==========================================
    public static List<PlayerCard> getPoolByPack(short idPack) {
        switch (idPack) {
            case 1680:
                return ITM;               // Gói ITM
            case 1681:
                return CC;                // Gói CC
            case 1682:
                return BTB;               // Gói BTB/EBS
            case 1683:                           // Gói ALL
                List<PlayerCard> all = new ArrayList<>();
                all.addAll(ITM);
                all.addAll(CC);
                all.addAll(BTB);
                return all;
        }
        return null;
    }

    // ==========================================
    // TẠO OTP THỨ 2
    // ==========================================
    private static int getOTP2ByType(int type) {
        if (type == 0) {
            return 220; // ST
        }
        if (type == 1) {
            return 221; // CAM
        }
        return 222;                // CB
    }

    // ==========================================
    // TẠO ITEM SAU KHI RANDOM
    // ==========================================
    public static Item createPlayerItem(PlayerCard card) {
        Item item = ItemService.gI().createNewItem(card.id);

        // OTP Over
        item.itemOptions.add(new ItemOption(223, card.over));

        // OTP theo vị trí
        int otp2 = getOTP2ByType(card.type);
        item.itemOptions.add(new ItemOption(otp2, 0));

        return item;
    }

    // ==========================================
    // XỬ LÝ MỞ GÓI
    // ==========================================
    public static Item openPack(Player p, Item pack) {

    List<PlayerCard> pool = getPoolByPack(pack.template.id);

    if (pool == null || pool.isEmpty()) {
        Service.getInstance().sendThongBao(p, "Gói lỗi hoặc chưa được cấu hình!");
        return null;
    }

    // Kiểm tra túi
    if (InventoryService.gI().getCountEmptyBag(p) == 0) {
        Service.getInstance().sendThongBao(p, "Hành trang không đủ chỗ trống!");
        return null;
    }

    // Random cầu thủ
    PlayerCard card = pool.get(new Random().nextInt(pool.size()));
    Item reward = createPlayerItem(card);

    // Add vào túi
    InventoryService.gI().addItemBag(p, reward,1);

    // Trừ gói
    InventoryService.gI().subQuantityItemsBag(p, pack, 1);

    // Cập nhật túi
    InventoryService.gI().sendItemBags(p);

    // Thông báo cho người chơi
    Service.getInstance().sendThongBao(p,
            "Bạn đã nhận được <b>" + reward.template.name + "</b> (Over " + card.over + ")");

    // ================================
    //           LOG RA CONSOLE
    // ================================
    String pos = (card.type == 0 ? "ST" : card.type == 1 ? "CAM" : "CB");

    System.out.println(
            "\n========== PACK OPEN LOG ==========" +
                    "\nPlayer: " + p.name + " (ID: " + p.id + ")" +
                    "\nĐã mở gói: " + pack.template.id + " - " + pack.template.name +
                    "\nNhận được thẻ: " + reward.template.id + " - " + reward.template.name +
                    "\nOver: " + card.over +
                    "\nVị trí: " + pos +
                    "\nThời gian: " + new java.util.Date() +
                    "\n===================================\n"
    );

    return reward;
}


}

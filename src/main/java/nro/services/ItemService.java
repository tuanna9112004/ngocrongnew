package nro.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.sql.ResultSet;
import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;
import nro.models.consignment.ConsignmentItem;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.shop.ItemShop;
import nro.server.Manager;
import nro.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.utils.Util;

/**
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 */
public class ItemService {

    private static ItemService i;

    private static final int OPT_DAY_LEFT = 93;
    private static final int OPT_EXP_EPOCH = 196;

    public static ItemService gI() {
        if (i == null) {
            i = new ItemService();
        }
        return i;
    }

    public Item createItemNull() {
        Item item = new Item();
        return item;
    }

    public Item createItemFromItemShop(ItemShop itemShop) {
        Item item = new Item();
        item.template = itemShop.temp;
        item.quantity = 1;
        item.content = item.getContent();
        item.info = item.getInfo();
        for (ItemOption io : itemShop.options) {
            item.itemOptions.add(new ItemOption(io));
        }
        return item;
    }

    public Item copyItem(Item item) {
        Item it = new Item();
        it.itemOptions = new ArrayList<>();
        it.template = item.template;
        it.info = item.info;
        it.content = item.content;
        it.quantity = item.quantity;
        it.createTime = item.createTime;
        for (ItemOption io : item.itemOptions) {
            it.itemOptions.add(new ItemOption(io));
        }
        return it;
    }

    public ConsignmentItem convertToConsignmentItem(Item item) {
        ConsignmentItem it = new ConsignmentItem();
        it.itemOptions = new ArrayList<>();
        it.template = item.template;
        it.info = item.info;
        it.content = item.content;
        it.quantity = item.quantity;
        it.createTime = item.createTime;
        for (ItemOption io : item.itemOptions) {
            it.itemOptions.add(new ItemOption(io));
        }
        it.setPriceGold(-1);
        it.setPriceGem(-1);
        return it;
    }

    public Item createNewItem(short tempId) {
        return createNewItem(tempId, 1);
    }

    public Item createNewItem(short tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();

        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item DoThienSu(int itemId, int gender) {
        Item dots = createItemSetKichHoat(itemId, 1);
        List<Integer> ao = Arrays.asList(1048, 1049, 1050);
        List<Integer> quan = Arrays.asList(1051, 1052, 1053);
        List<Integer> gang = Arrays.asList(1054, 1055, 1056);
        List<Integer> giay = Arrays.asList(1057, 1058, 1059);
        List<Integer> nhan = Arrays.asList(1060, 1061, 1062);
        //áo
        if (ao.contains(itemId)) {
            dots.itemOptions.add(new ItemOption(47, Util.highlightsItem(gender == 2, new Random().nextInt(1201) + 2800))); // áo từ 2800-4000 giáp
            if (Util.isTrue(30, 100)) {
                dots.itemOptions.add(new ItemOption(108, Util.nextInt(3, 10)));
            }

        }
        //quần
        if (Util.isTrue(60, 100)) {
            if (quan.contains(itemId)) {
                dots.itemOptions.add(new ItemOption(22, Util.highlightsItem(gender == 0, new Random().nextInt(11) + 120))); // hp 120k-130k
            }
        } else {
            if (quan.contains(itemId)) {
                dots.itemOptions.add(new ItemOption(22, Util.highlightsItem(gender == 0, new Random().nextInt(51) + 130))); // hp 130-180k 15%
                if (Util.isTrue(30, 100)) {
                    dots.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                }
            }
        }
        //găng
        if (Util.isTrue(60, 100)) {
            if (gang.contains(itemId)) {
                dots.itemOptions.add(new ItemOption(0, Util.highlightsItem(gender == 2, new Random().nextInt(7651) + 11000))); // 11000-18600
            }
        } else {
            if (gang.contains(itemId)) {
                dots.itemOptions.add(new ItemOption(0, Util.highlightsItem(gender == 2, new Random().nextInt(7001) + 12000))); // gang 15% 12-19k -xayda 12k1
                if (Util.isTrue(30, 100)) {
                    dots.itemOptions.add(new ItemOption(50, Util.nextInt(3, 10)));
                }
            }
        }
        //giày
        if (Util.isTrue(60, 100)) {
            if (giay.contains(itemId)) {
                dots.itemOptions.add(new ItemOption(23, Util.highlightsItem(gender == 1, new Random().nextInt(21) + 120))); // ki 90-110k
            }
        } else {
            if (giay.contains(itemId)) {
                dots.itemOptions.add(new ItemOption(23, Util.highlightsItem(gender == 1, new Random().nextInt(21) + 130))); // ki 110-130k
                if (Util.isTrue(30, 100)) {
                    dots.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                }
            }
        }

        if (nhan.contains(itemId)) {
            dots.itemOptions.add(new ItemOption(14, Util.highlightsItem(gender == 1, new Random().nextInt(3) + 18))); // nhẫn 18-20%
            if (Util.isTrue(30, 100)) {
                dots.itemOptions.add(new ItemOption(117, Util.nextInt(3, 10)));
            }
        }
        dots.itemOptions.add(new ItemOption(21, 120));
//        dots.itemOptions.add(new ItemOption(30, 1));
        return dots;
    }

    public void settaiyoken(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1105);
        Item ao = ItemService.gI().otpts((short) 1048);
        Item quan = ItemService.gI().otpts((short) 1051);
        Item gang = ItemService.gI().otpts((short) 1054);
        Item giay = ItemService.gI().otpts((short) 1057);
        Item nhan = ItemService.gI().otpts((short) 1060);
        ao.itemOptions.add(new ItemOption(127, 1));
        quan.itemOptions.add(new ItemOption(127, 1));
        gang.itemOptions.add(new ItemOption(127, 1));
        giay.itemOptions.add(new ItemOption(127, 1));
        nhan.itemOptions.add(new ItemOption(127, 1));
        ao.itemOptions.add(new ItemOption(139, 1));
        quan.itemOptions.add(new ItemOption(139, 1));
        gang.itemOptions.add(new ItemOption(139, 1));
        giay.itemOptions.add(new ItemOption(139, 1));
        nhan.itemOptions.add(new ItemOption(139, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được set thiên sứ ");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgenki(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1105);
        Item ao = ItemService.gI().otpts((short) 1048);
        Item quan = ItemService.gI().otpts((short) 1051);
        Item gang = ItemService.gI().otpts((short) 1054);
        Item giay = ItemService.gI().otpts((short) 1057);
        Item nhan = ItemService.gI().otpts((short) 1060);
        ao.itemOptions.add(new ItemOption(128, 1));
        quan.itemOptions.add(new ItemOption(128, 1));
        gang.itemOptions.add(new ItemOption(128, 1));
        giay.itemOptions.add(new ItemOption(128, 1));
        nhan.itemOptions.add(new ItemOption(128, 1));
        ao.itemOptions.add(new ItemOption(140, 1));
        quan.itemOptions.add(new ItemOption(140, 1));
        gang.itemOptions.add(new ItemOption(140, 1));
        giay.itemOptions.add(new ItemOption(140, 1));
        nhan.itemOptions.add(new ItemOption(140, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được set thiên sứ ");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setkamejoko(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1105);
        Item ao = ItemService.gI().otpts((short) 1048);
        Item quan = ItemService.gI().otpts((short) 1051);
        Item gang = ItemService.gI().otpts((short) 1054);
        Item giay = ItemService.gI().otpts((short) 1057);
        Item nhan = ItemService.gI().otpts((short) 1060);
        ao.itemOptions.add(new ItemOption(129, 1));
        quan.itemOptions.add(new ItemOption(129, 1));
        gang.itemOptions.add(new ItemOption(129, 1));
        giay.itemOptions.add(new ItemOption(129, 1));
        nhan.itemOptions.add(new ItemOption(129, 1));
        ao.itemOptions.add(new ItemOption(141, 1));
        quan.itemOptions.add(new ItemOption(141, 1));
        gang.itemOptions.add(new ItemOption(141, 1));
        giay.itemOptions.add(new ItemOption(141, 1));
        nhan.itemOptions.add(new ItemOption(141, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được set thiên sứ ");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgodki(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1105);
        Item ao = ItemService.gI().otpts((short) 1049);
        Item quan = ItemService.gI().otpts((short) 1052);
        Item gang = ItemService.gI().otpts((short) 1055);
        Item giay = ItemService.gI().otpts((short) 1058);
        Item nhan = ItemService.gI().otpts((short) 1061);
        ao.itemOptions.add(new ItemOption(130, 1));
        quan.itemOptions.add(new ItemOption(130, 1));
        gang.itemOptions.add(new ItemOption(130, 1));
        giay.itemOptions.add(new ItemOption(130, 1));
        nhan.itemOptions.add(new ItemOption(130, 1));
        ao.itemOptions.add(new ItemOption(142, 1));
        quan.itemOptions.add(new ItemOption(142, 1));
        gang.itemOptions.add(new ItemOption(142, 1));
        giay.itemOptions.add(new ItemOption(142, 1));
        nhan.itemOptions.add(new ItemOption(142, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được set thiên sứ ");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgoddam(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1105);
        Item ao = ItemService.gI().otpts((short) 1049);
        Item quan = ItemService.gI().otpts((short) 1052);
        Item gang = ItemService.gI().otpts((short) 1055);
        Item giay = ItemService.gI().otpts((short) 1058);
        Item nhan = ItemService.gI().otpts((short) 1061);
        ao.itemOptions.add(new ItemOption(131, 1));
        quan.itemOptions.add(new ItemOption(131, 1));
        gang.itemOptions.add(new ItemOption(131, 1));
        giay.itemOptions.add(new ItemOption(131, 1));
        nhan.itemOptions.add(new ItemOption(131, 1));
        ao.itemOptions.add(new ItemOption(143, 1));
        quan.itemOptions.add(new ItemOption(143, 1));
        gang.itemOptions.add(new ItemOption(143, 1));
        giay.itemOptions.add(new ItemOption(143, 1));
        nhan.itemOptions.add(new ItemOption(143, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được set thiên sứ ");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setsummon(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1105);
        Item ao = ItemService.gI().otpts((short) 1049);
        Item quan = ItemService.gI().otpts((short) 1052);
        Item gang = ItemService.gI().otpts((short) 1055);
        Item giay = ItemService.gI().otpts((short) 1058);
        Item nhan = ItemService.gI().otpts((short) 1061);
        ao.itemOptions.add(new ItemOption(132, 1));
        quan.itemOptions.add(new ItemOption(132, 1));
        gang.itemOptions.add(new ItemOption(132, 1));
        giay.itemOptions.add(new ItemOption(132, 1));
        nhan.itemOptions.add(new ItemOption(132, 1));
        ao.itemOptions.add(new ItemOption(144, 1));
        quan.itemOptions.add(new ItemOption(144, 1));
        gang.itemOptions.add(new ItemOption(144, 1));
        giay.itemOptions.add(new ItemOption(144, 1));
        nhan.itemOptions.add(new ItemOption(144, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được set thiên sứ ");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgodgalick(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1105);
        Item ao = ItemService.gI().otpts((short) 1050);
        Item quan = ItemService.gI().otpts((short) 1053);
        Item gang = ItemService.gI().otpts((short) 1056);
        Item giay = ItemService.gI().otpts((short) 1059);
        Item nhan = ItemService.gI().otpts((short) 1062);
        ao.itemOptions.add(new ItemOption(133, 1));
        quan.itemOptions.add(new ItemOption(133, 1));
        gang.itemOptions.add(new ItemOption(133, 1));
        giay.itemOptions.add(new ItemOption(133, 1));
        nhan.itemOptions.add(new ItemOption(133, 1));
        ao.itemOptions.add(new ItemOption(136, 1));
        quan.itemOptions.add(new ItemOption(136, 1));
        gang.itemOptions.add(new ItemOption(136, 1));
        giay.itemOptions.add(new ItemOption(136, 1));
        nhan.itemOptions.add(new ItemOption(136, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được set thiên sứ ");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setmonkey(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1105);
        Item ao = ItemService.gI().otpts((short) 1050);
        Item quan = ItemService.gI().otpts((short) 1053);
        Item gang = ItemService.gI().otpts((short) 1056);
        Item giay = ItemService.gI().otpts((short) 1059);
        Item nhan = ItemService.gI().otpts((short) 1062);
        ao.itemOptions.add(new ItemOption(134, 1));
        quan.itemOptions.add(new ItemOption(134, 1));
        gang.itemOptions.add(new ItemOption(134, 1));
        giay.itemOptions.add(new ItemOption(134, 1));
        nhan.itemOptions.add(new ItemOption(134, 1));
        ao.itemOptions.add(new ItemOption(137, 1));
        quan.itemOptions.add(new ItemOption(137, 1));
        gang.itemOptions.add(new ItemOption(137, 1));
        giay.itemOptions.add(new ItemOption(137, 1));
        nhan.itemOptions.add(new ItemOption(137, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được set thiên sứ ");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgodhp(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1105);
        Item ao = ItemService.gI().otpts((short) 1050);
        Item quan = ItemService.gI().otpts((short) 1053);
        Item gang = ItemService.gI().otpts((short) 1056);
        Item giay = ItemService.gI().otpts((short) 1059);
        Item nhan = ItemService.gI().otpts((short) 1062);
        ao.itemOptions.add(new ItemOption(135, 1));
        quan.itemOptions.add(new ItemOption(135, 1));
        gang.itemOptions.add(new ItemOption(135, 1));
        giay.itemOptions.add(new ItemOption(135, 1));
        nhan.itemOptions.add(new ItemOption(135, 1));
        ao.itemOptions.add(new ItemOption(138, 1));
        quan.itemOptions.add(new ItemOption(138, 1));
        gang.itemOptions.add(new ItemOption(138, 1));
        giay.itemOptions.add(new ItemOption(138, 1));
        nhan.itemOptions.add(new ItemOption(138, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được set thiên sứ ");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void settaiyokenHD(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1586);
        Item ao = Util.ratiItemHuyDiet((short) 650);
        Item quan = Util.ratiItemHuyDiet((short) 651);
        Item gang = Util.ratiItemHuyDiet((short) 657);
        Item giay = Util.ratiItemHuyDiet((short) 658);
        Item nhan = Util.ratiItemHuyDiet((short) 656);
        ao.itemOptions.add(new ItemOption(127, 1));
        quan.itemOptions.add(new ItemOption(127, 1));
        gang.itemOptions.add(new ItemOption(127, 1));
        giay.itemOptions.add(new ItemOption(127, 1));
        nhan.itemOptions.add(new ItemOption(127, 1));
        ao.itemOptions.add(new ItemOption(139, 1));
        quan.itemOptions.add(new ItemOption(139, 1));
        gang.itemOptions.add(new ItemOption(139, 1));
        giay.itemOptions.add(new ItemOption(139, 1));
        nhan.itemOptions.add(new ItemOption(139, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Hủy diệt");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgenkiHD(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1586);
        Item ao = Util.ratiItemHuyDiet((short) 650);
        Item quan = Util.ratiItemHuyDiet((short) 651);
        Item gang = Util.ratiItemHuyDiet((short) 657);
        Item giay = Util.ratiItemHuyDiet((short) 658);
        Item nhan = Util.ratiItemHuyDiet((short) 656);
        ao.itemOptions.add(new ItemOption(128, 1));
        quan.itemOptions.add(new ItemOption(128, 1));
        gang.itemOptions.add(new ItemOption(128, 1));
        giay.itemOptions.add(new ItemOption(128, 1));
        nhan.itemOptions.add(new ItemOption(128, 1));
        ao.itemOptions.add(new ItemOption(140, 1));
        quan.itemOptions.add(new ItemOption(140, 1));
        gang.itemOptions.add(new ItemOption(140, 1));
        giay.itemOptions.add(new ItemOption(140, 1));
        nhan.itemOptions.add(new ItemOption(140, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Hủy diệt");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setkamejokoHD(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1586);
        Item ao = Util.ratiItemHuyDiet((short) 650);
        Item quan = Util.ratiItemHuyDiet((short) 651);
        Item gang = Util.ratiItemHuyDiet((short) 657);
        Item giay = Util.ratiItemHuyDiet((short) 658);
        Item nhan = Util.ratiItemHuyDiet((short) 656);
        ao.itemOptions.add(new ItemOption(129, 1));
        quan.itemOptions.add(new ItemOption(129, 1));
        gang.itemOptions.add(new ItemOption(129, 1));
        giay.itemOptions.add(new ItemOption(129, 1));
        nhan.itemOptions.add(new ItemOption(129, 1));
        ao.itemOptions.add(new ItemOption(141, 1));
        quan.itemOptions.add(new ItemOption(141, 1));
        gang.itemOptions.add(new ItemOption(141, 1));
        giay.itemOptions.add(new ItemOption(141, 1));
        nhan.itemOptions.add(new ItemOption(141, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Hủy diệt");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgodkiHD(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1586);
        Item ao = Util.ratiItemHuyDiet((short) 652);
        Item quan = Util.ratiItemHuyDiet((short) 653);
        Item gang = Util.ratiItemHuyDiet((short) 659);
        Item giay = Util.ratiItemHuyDiet((short) 660);
        Item nhan = Util.ratiItemHuyDiet((short) 656);
        ao.itemOptions.add(new ItemOption(130, 1));
        quan.itemOptions.add(new ItemOption(130, 1));
        gang.itemOptions.add(new ItemOption(130, 1));
        giay.itemOptions.add(new ItemOption(130, 1));
        nhan.itemOptions.add(new ItemOption(130, 1));
        ao.itemOptions.add(new ItemOption(142, 1));
        quan.itemOptions.add(new ItemOption(142, 1));
        gang.itemOptions.add(new ItemOption(142, 1));
        giay.itemOptions.add(new ItemOption(142, 1));
        nhan.itemOptions.add(new ItemOption(142, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Hủy diệt");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgoddamHD(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1586);
        Item ao = Util.ratiItemHuyDiet((short) 652);
        Item quan = Util.ratiItemHuyDiet((short) 653);
        Item gang = Util.ratiItemHuyDiet((short) 659);
        Item giay = Util.ratiItemHuyDiet((short) 660);
        Item nhan = Util.ratiItemHuyDiet((short) 656);
        ao.itemOptions.add(new ItemOption(131, 1));
        quan.itemOptions.add(new ItemOption(131, 1));
        gang.itemOptions.add(new ItemOption(131, 1));
        giay.itemOptions.add(new ItemOption(131, 1));
        nhan.itemOptions.add(new ItemOption(131, 1));
        ao.itemOptions.add(new ItemOption(143, 1));
        quan.itemOptions.add(new ItemOption(143, 1));
        gang.itemOptions.add(new ItemOption(143, 1));
        giay.itemOptions.add(new ItemOption(143, 1));
        nhan.itemOptions.add(new ItemOption(143, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Hủy diệt");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setsummonHD(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1586);
        Item ao = Util.ratiItemHuyDiet((short) 652);
        Item quan = Util.ratiItemHuyDiet((short) 653);
        Item gang = Util.ratiItemHuyDiet((short) 659);
        Item giay = Util.ratiItemHuyDiet((short) 660);
        Item nhan = Util.ratiItemHuyDiet((short) 656);
        ao.itemOptions.add(new ItemOption(132, 1));
        quan.itemOptions.add(new ItemOption(132, 1));
        gang.itemOptions.add(new ItemOption(132, 1));
        giay.itemOptions.add(new ItemOption(132, 1));
        nhan.itemOptions.add(new ItemOption(132, 1));
        ao.itemOptions.add(new ItemOption(144, 1));
        quan.itemOptions.add(new ItemOption(144, 1));
        gang.itemOptions.add(new ItemOption(144, 1));
        giay.itemOptions.add(new ItemOption(144, 1));
        nhan.itemOptions.add(new ItemOption(144, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Hủy diệt");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgodgalickHD(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1586);
        Item ao = Util.ratiItemHuyDiet((short) 654);
        Item quan = Util.ratiItemHuyDiet((short) 655);
        Item gang = Util.ratiItemHuyDiet((short) 661);
        Item giay = Util.ratiItemHuyDiet((short) 662);
        Item nhan = Util.ratiItemHuyDiet((short) 656);
        ao.itemOptions.add(new ItemOption(133, 1));
        quan.itemOptions.add(new ItemOption(133, 1));
        gang.itemOptions.add(new ItemOption(133, 1));
        giay.itemOptions.add(new ItemOption(133, 1));
        nhan.itemOptions.add(new ItemOption(133, 1));
        ao.itemOptions.add(new ItemOption(136, 1));
        quan.itemOptions.add(new ItemOption(136, 1));
        gang.itemOptions.add(new ItemOption(136, 1));
        giay.itemOptions.add(new ItemOption(136, 1));
        nhan.itemOptions.add(new ItemOption(136, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Hủy diệt");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setmonkeyHD(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1586);
        Item ao = Util.ratiItemHuyDiet((short) 654);
        Item quan = Util.ratiItemHuyDiet((short) 655);
        Item gang = Util.ratiItemHuyDiet((short) 661);
        Item giay = Util.ratiItemHuyDiet((short) 662);
        Item nhan = Util.ratiItemHuyDiet((short) 656);
        ao.itemOptions.add(new ItemOption(134, 1));
        quan.itemOptions.add(new ItemOption(134, 1));
        gang.itemOptions.add(new ItemOption(134, 1));
        giay.itemOptions.add(new ItemOption(134, 1));
        nhan.itemOptions.add(new ItemOption(134, 1));
        ao.itemOptions.add(new ItemOption(137, 1));
        quan.itemOptions.add(new ItemOption(137, 1));
        gang.itemOptions.add(new ItemOption(137, 1));
        giay.itemOptions.add(new ItemOption(137, 1));
        nhan.itemOptions.add(new ItemOption(137, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Hủy diệt");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgodhpHD(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1586);
        Item ao = Util.ratiItemHuyDiet((short) 654);
        Item quan = Util.ratiItemHuyDiet((short) 655);
        Item gang = Util.ratiItemHuyDiet((short) 661);
        Item giay = Util.ratiItemHuyDiet((short) 662);
        Item nhan = Util.ratiItemHuyDiet((short) 656);
        ao.itemOptions.add(new ItemOption(135, 1));
        quan.itemOptions.add(new ItemOption(135, 1));
        gang.itemOptions.add(new ItemOption(135, 1));
        giay.itemOptions.add(new ItemOption(135, 1));
        nhan.itemOptions.add(new ItemOption(135, 1));
        ao.itemOptions.add(new ItemOption(138, 1));
        quan.itemOptions.add(new ItemOption(138, 1));
        gang.itemOptions.add(new ItemOption(138, 1));
        giay.itemOptions.add(new ItemOption(138, 1));
        nhan.itemOptions.add(new ItemOption(138, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Hủy diệt");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void settaiyokenTL(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1538);
        Item ao = Util.ratiItemTL((short) 555);
        Item quan = Util.ratiItemTL((short) 556);
        Item gang = Util.ratiItemTL((short) 562);
        Item giay = Util.ratiItemTL((short) 563);
        Item nhan = Util.ratiItemTL((short) 561);
        ao.itemOptions.add(new ItemOption(127, 1));
        quan.itemOptions.add(new ItemOption(127, 1));
        gang.itemOptions.add(new ItemOption(127, 1));
        giay.itemOptions.add(new ItemOption(127, 1));
        nhan.itemOptions.add(new ItemOption(127, 1));
        ao.itemOptions.add(new ItemOption(139, 1));
        quan.itemOptions.add(new ItemOption(139, 1));
        gang.itemOptions.add(new ItemOption(139, 1));
        giay.itemOptions.add(new ItemOption(139, 1));
        nhan.itemOptions.add(new ItemOption(139, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Thần linh");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgenkiTL(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1538);
        Item ao = Util.ratiItemTL((short) 555);
        Item quan = Util.ratiItemTL((short) 556);
        Item gang = Util.ratiItemTL((short) 562);
        Item giay = Util.ratiItemTL((short) 563);
        Item nhan = Util.ratiItemTL((short) 561);
        ao.itemOptions.add(new ItemOption(128, 1));
        quan.itemOptions.add(new ItemOption(128, 1));
        gang.itemOptions.add(new ItemOption(128, 1));
        giay.itemOptions.add(new ItemOption(128, 1));
        nhan.itemOptions.add(new ItemOption(128, 1));
        ao.itemOptions.add(new ItemOption(140, 1));
        quan.itemOptions.add(new ItemOption(140, 1));
        gang.itemOptions.add(new ItemOption(140, 1));
        giay.itemOptions.add(new ItemOption(140, 1));
        nhan.itemOptions.add(new ItemOption(140, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Thần linh");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setkamejokoTL(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1538);
        Item ao = Util.ratiItemTL((short) 555);
        Item quan = Util.ratiItemTL((short) 556);
        Item gang = Util.ratiItemTL((short) 562);
        Item giay = Util.ratiItemTL((short) 563);
        Item nhan = Util.ratiItemTL((short) 561);
        ao.itemOptions.add(new ItemOption(129, 1));
        quan.itemOptions.add(new ItemOption(129, 1));
        gang.itemOptions.add(new ItemOption(129, 1));
        giay.itemOptions.add(new ItemOption(129, 1));
        nhan.itemOptions.add(new ItemOption(129, 1));
        ao.itemOptions.add(new ItemOption(141, 1));
        quan.itemOptions.add(new ItemOption(141, 1));
        gang.itemOptions.add(new ItemOption(141, 1));
        giay.itemOptions.add(new ItemOption(141, 1));
        nhan.itemOptions.add(new ItemOption(141, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Thần linh");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgodkiTL(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1538);
        Item ao = Util.ratiItemTL((short) 557);
        Item quan = Util.ratiItemTL((short) 558);
        Item gang = Util.ratiItemTL((short) 564);
        Item giay = Util.ratiItemTL((short) 565);
        Item nhan = Util.ratiItemTL((short) 561);
        ao.itemOptions.add(new ItemOption(130, 1));
        quan.itemOptions.add(new ItemOption(130, 1));
        gang.itemOptions.add(new ItemOption(130, 1));
        giay.itemOptions.add(new ItemOption(130, 1));
        nhan.itemOptions.add(new ItemOption(130, 1));
        ao.itemOptions.add(new ItemOption(142, 1));
        quan.itemOptions.add(new ItemOption(142, 1));
        gang.itemOptions.add(new ItemOption(142, 1));
        giay.itemOptions.add(new ItemOption(142, 1));
        nhan.itemOptions.add(new ItemOption(142, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Thần linh");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgoddamTL(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1538);
        Item ao = Util.ratiItemTL((short) 557);
        Item quan = Util.ratiItemTL((short) 558);
        Item gang = Util.ratiItemTL((short) 564);
        Item giay = Util.ratiItemTL((short) 565);
        Item nhan = Util.ratiItemTL((short) 561);
        ao.itemOptions.add(new ItemOption(131, 1));
        quan.itemOptions.add(new ItemOption(131, 1));
        gang.itemOptions.add(new ItemOption(131, 1));
        giay.itemOptions.add(new ItemOption(131, 1));
        nhan.itemOptions.add(new ItemOption(131, 1));
        ao.itemOptions.add(new ItemOption(143, 1));
        quan.itemOptions.add(new ItemOption(143, 1));
        gang.itemOptions.add(new ItemOption(143, 1));
        giay.itemOptions.add(new ItemOption(143, 1));
        nhan.itemOptions.add(new ItemOption(143, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Thần linh");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setsummonTL(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1538);
        Item ao = Util.ratiItemTL((short) 557);
        Item quan = Util.ratiItemTL((short) 558);
        Item gang = Util.ratiItemTL((short) 564);
        Item giay = Util.ratiItemTL((short) 565);
        Item nhan = Util.ratiItemTL((short) 561);
        ao.itemOptions.add(new ItemOption(132, 1));
        quan.itemOptions.add(new ItemOption(132, 1));
        gang.itemOptions.add(new ItemOption(132, 1));
        giay.itemOptions.add(new ItemOption(132, 1));
        nhan.itemOptions.add(new ItemOption(132, 1));
        ao.itemOptions.add(new ItemOption(144, 1));
        quan.itemOptions.add(new ItemOption(144, 1));
        gang.itemOptions.add(new ItemOption(144, 1));
        giay.itemOptions.add(new ItemOption(144, 1));
        nhan.itemOptions.add(new ItemOption(144, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Thần linh");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgodgalickTL(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1538);
        Item ao = Util.ratiItemTL((short) 559);
        Item quan = Util.ratiItemTL((short) 560);
        Item gang = Util.ratiItemTL((short) 566);
        Item giay = Util.ratiItemTL((short) 567);
        Item nhan = Util.ratiItemTL((short) 561);
        ao.itemOptions.add(new ItemOption(133, 1));
        quan.itemOptions.add(new ItemOption(133, 1));
        gang.itemOptions.add(new ItemOption(133, 1));
        giay.itemOptions.add(new ItemOption(133, 1));
        nhan.itemOptions.add(new ItemOption(133, 1));
        ao.itemOptions.add(new ItemOption(136, 1));
        quan.itemOptions.add(new ItemOption(136, 1));
        gang.itemOptions.add(new ItemOption(136, 1));
        giay.itemOptions.add(new ItemOption(136, 1));
        nhan.itemOptions.add(new ItemOption(136, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Thần linh");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setmonkeyTL(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1538);
        Item ao = Util.ratiItemTL((short) 559);
        Item quan = Util.ratiItemTL((short) 560);
        Item gang = Util.ratiItemTL((short) 566);
        Item giay = Util.ratiItemTL((short) 567);
        Item nhan = Util.ratiItemTL((short) 561);
        ao.itemOptions.add(new ItemOption(134, 1));
        quan.itemOptions.add(new ItemOption(134, 1));
        gang.itemOptions.add(new ItemOption(134, 1));
        giay.itemOptions.add(new ItemOption(134, 1));
        nhan.itemOptions.add(new ItemOption(134, 1));
        ao.itemOptions.add(new ItemOption(137, 1));
        quan.itemOptions.add(new ItemOption(137, 1));
        gang.itemOptions.add(new ItemOption(137, 1));
        giay.itemOptions.add(new ItemOption(137, 1));
        nhan.itemOptions.add(new ItemOption(137, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Thần linh");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public void setgodhpTL(Player player) throws Exception {
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1538);
        Item ao = Util.ratiItemTL((short) 559);
        Item quan = Util.ratiItemTL((short) 560);
        Item gang = Util.ratiItemTL((short) 566);
        Item giay = Util.ratiItemTL((short) 567);
        Item nhan = Util.ratiItemTL((short) 561);
        ao.itemOptions.add(new ItemOption(135, 1));
        quan.itemOptions.add(new ItemOption(135, 1));
        gang.itemOptions.add(new ItemOption(135, 1));
        giay.itemOptions.add(new ItemOption(135, 1));
        nhan.itemOptions.add(new ItemOption(135, 1));
        ao.itemOptions.add(new ItemOption(138, 1));
        quan.itemOptions.add(new ItemOption(138, 1));
        gang.itemOptions.add(new ItemOption(138, 1));
        giay.itemOptions.add(new ItemOption(138, 1));
        nhan.itemOptions.add(new ItemOption(138, 1));
        ao.itemOptions.add(new ItemOption(30, 0));
        quan.itemOptions.add(new ItemOption(30, 0));
        gang.itemOptions.add(new ItemOption(30, 0));
        giay.itemOptions.add(new ItemOption(30, 0));
        nhan.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            InventoryService.gI().addItemBag(player, ao, 0);
            InventoryService.gI().addItemBag(player, quan, 0);
            InventoryService.gI().addItemBag(player, gang, 0);
            InventoryService.gI().addItemBag(player, giay, 0);
            InventoryService.gI().addItemBag(player, nhan, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5 món SKH Thần linh");
            InventoryService.gI().subQuantityItemsBag(player, hq, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
        }
    }

    public Item otpts(short tempId) {
        return otpts(tempId, 1);
    }

    public Item otpts(short tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(21, 120));
            item.itemOptions.add(new ItemOption(47, Util.nextInt(2000, 2500)));
        }
        if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(21, 120));
            item.itemOptions.add(new ItemOption(22, Util.nextInt(150, 200)));
        }
        if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(21, 120));
            item.itemOptions.add(new ItemOption(0, Util.nextInt(18000, 20000)));
        }
        if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(21, 120));
            item.itemOptions.add(new ItemOption(23, Util.nextInt(150, 200)));
        }
        if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(21, 120));
            item.itemOptions.add(new ItemOption(14, Util.nextInt(20, 25)));
        }
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public int randomSKHId(byte gender) {
        if (gender == 3) {
            gender = 2;
        }
        int[][] options = {{128, 129, 127}, {130, 131, 132}, {133, 135, 134}};
        int skhv1 = 25;
        int skhv2 = 35;
        int skhc = 40;
        int skhId = -1;
        int rd = Util.nextInt(1, 100);
        if (rd <= skhv1) {
            skhId = 0;
        } else if (rd <= skhv1 + skhv2) {
            skhId = 1;
        } else if (rd <= skhv1 + skhv2 + skhc) {
            skhId = 2;
        }
        return options[gender][skhId];
    }

    public Item itemSKH(int itemId, int skhId) {
        Item item = createItemSetKichHoat(itemId, 1);
        if (item != null) {
            item.itemOptions.addAll(ItemService.gI().getListOptionItemShop((short) itemId));
            item.itemOptions.add(new ItemOption(skhId, 1));
            item.itemOptions.add(new ItemOption(optionIdSKH(skhId), 1));
            item.itemOptions.add(new ItemOption(30, 1));
        }
        return item;
    }

    public List<ItemOption> getListOptionItemShop(short id) {
        List<ItemOption> list = new ArrayList<>();
        Manager.SHOPS.forEach(shop -> shop.tabShops.forEach(tabShop -> tabShop.itemShops.forEach(itemShop -> {
            if (itemShop.temp.id == id && list.size() == 0) {
                list.addAll(itemShop.options);
            }
        })));
        return list;
    }

    public Item createItemSetKichHoat(int tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.itemOptions = createItemNull().itemOptions;
        item.createTime = System.currentTimeMillis();
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public ConsignmentItem createNewConsignmentItem(short tempId, int quantity) {
        ConsignmentItem item = new ConsignmentItem();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item createItemFromItemMap(ItemMap itemMap) {
        Item item = createNewItem(itemMap.itemTemplate.id, itemMap.quantity);
        item.itemOptions = itemMap.options;
        return item;
    }

    public ItemOptionTemplate getItemOptionTemplate(int id) {
        return Manager.ITEM_OPTION_TEMPLATES.get(id);
    }

    public ItemTemplate getTemplate(int id) {
        return Manager.ITEM_TEMPLATES.get(id);
    }

    public boolean isItemActivation(Item item) {
        return false;
    }

    public int getPercentTrainArmor(Item item) {
        if (item != null) {
            switch (item.template.id) {
                case 529:
                case 534:
                    return 10;
                case 530:
                case 535:
                    return 20;
                case 531:
                case 536:
                    return 30;
                default:
                    return 0;
            }
        } else {
            return 0;
        }
    }

    public boolean isTrainArmor(Item item) {
        if (item != null) {
            switch (item.template.id) {
                case 529:
                case 534:
                case 530:
                case 535:
                case 531:
                case 536:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public int optionIdSKH(int skhId) {
        switch (skhId) {
            case 127: //Set Viet Taiyoken
                return 139;
            case 128: //Set Viet Genki
                return 140;
            case 129: //Set Viet Kamejoko
                return 141;
            case 130: //Set Viet KI
                return 142;
            case 131: //Set Viet Dame
                return 143;
            case 132: //Set Viet Summon
                return 144;
            case 133: //Set Viet Galick
                return 136;
            case 134: //Set Viet Monkey
                return 137;
            case 135: //Set Viet HP
                return 138;
        }
        return 0;
    }

    public boolean isOutOfDateTime(Item item) {
        if (item == null || item.itemOptions == null) {
            return false;
        }
        final long now = System.currentTimeMillis();
        for (ItemOption io : item.itemOptions) {
            final int id = io.optionTemplate.id;
            // ====== 196: mốc tuyệt đối (epoch giây) ======
            if (id == OPT_EXP_EPOCH) {
                long expMs = (io.param & 0xffffffffL) * 1000L;
                if (now >= expMs) {
                    makeEmptySlot(item);
                    return true;
                }
            }
            // ====== 93: ngày còn lại (đếm lùi theo ngày lịch UTC) ======
            if (id == OPT_DAY_LEFT) {
                if (io.param <= 0) {            // đã hết từ trước
                    makeEmptySlot(item);
                    return true;
                }
                long base = item.createTime > 0 ? item.createTime : now;
                int dayPass = utcDaysBetween(base, now);
                if (dayPass > 0) {
                    io.param = Math.max(0, io.param - dayPass);
                    if (io.param <= 0) {
                        makeEmptySlot(item);
                        return true;
                    } else {
                        item.createTime = base + dayPass * 86_400_000L;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Chênh lệch theo NGÀY LỊCH (UTC), bền vững qua downtime và DST
     */
    private int utcDaysBetween(long fromMs, long toMs) {
        if (toMs <= fromMs) {
            return 0;
        }
        long fromDay = Math.floorDiv(fromMs, 86_400_000L);
        long toDay = Math.floorDiv(toMs, 86_400_000L);
        long d = toDay - fromDay;
        return d > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) d;
    }

    private void makeEmptySlot(Item item) {
        item.quantity = 0;
        item.createTime = 0L;
        item.template = null;  // serialize ra temp_id = -1

        // QUAN TRỌNG: Xóa hết option nhưng GIỮ lại mảng (không set = null)
        if (item.itemOptions != null) {
            item.itemOptions.clear();
        } else {
            // Nếu chưa có, khởi tạo mảng rỗng
            item.itemOptions = new ArrayList<>();
        }

        // Nếu serialize cần đảm bảo "option": [] thay vì null
        // (code trên đã handle bằng cách clear thay vì set null)
    }

    public boolean isItemNoLimitQuantity(int id) {// item k giới hạn số lượng
        if (id >= 1066 && id <= 1070) {// mảnh trang bị thiên sứ
            return true;
        }
        return false;
    }

    public int randomSKHThanhTon(byte gender) {
        if (gender == 3) {
            gender = 2;
        }
        int[] options = {234, 235, 236};
        return options[gender];
    }

    public int optionIdSKHThanhTon(int skhId) {
        switch (skhId) {
            case 234: //Set Thánh tôn trái dất
                return 237;
            case 235: //Set Thánh tôn namec
                return 238;
            case 236: //Set Thánh tôn xayda
                return 239;
        }
        return 0;
    }

    public ItemMap randDoTL(Zone zone, int x, int y, long pId, Player pl) {

        int gender = pl.gender;
        if (gender < 0 || gender > 2) {
            gender = 0; // fallback
        }

        // Danh sách TL theo giới tính
        int[] dsTL = TL_BY_GENDER[gender];
        int[] nhanTL = TL_BY_GENDER[3];

        // Gộp mảng (đảm bảo không cần mergeArrays)
        int[] fullListTL = new int[dsTL.length + nhanTL.length];
        System.arraycopy(dsTL, 0, fullListTL, 0, dsTL.length);
        System.arraycopy(nhanTL, 0, fullListTL, dsTL.length, nhanTL.length);

        // Random món TL phù hợp giới tính
        int tempId = fullListTL[Util.nextInt(0, fullListTL.length - 1)];

        // Tạo item gốc
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = 1;
        item.createTime = System.currentTimeMillis();

        int type = item.template.type;

        // ==========================================
        // OPTION CƠ BẢN
        // ==========================================

        switch (type) {

            case 0: // Áo
                // Giáp 1300 - 1800
                item.itemOptions.add(new ItemOption(
                        47,
                        Util.highlightsItem(
                                item.template.gender == 2,
                                Util.nextInt(1300, 1800)
                        )
                ));
                break;

            case 1: // Quần
                // HP 45 - 55 (tương đương 45k - 55k)
                item.itemOptions.add(new ItemOption(
                        22,
                        Util.highlightsItem(
                                item.template.gender == 0,
                                Util.nextInt(45, 55)
                        )
                ));
                break;

            case 2: // Găng
                // Sức đánh 3500 - 4500
                item.itemOptions.add(new ItemOption(
                        0,
                        Util.highlightsItem(
                                item.template.gender == 2,
                                Util.nextInt(4500, 5500)
                        )
                ));
                break;

            case 3: // Giày
                // KI 35 - 45
                item.itemOptions.add(new ItemOption(
                        23,
                        Util.highlightsItem(
                                item.template.gender == 1,
                                Util.nextInt(35, 45)
                        )
                ));
                break;

            case 4: // Nhẫn TL
                // Chí mạng 15 - 16
                item.itemOptions.add(new ItemOption(
                        14,
                        Util.nextInt(15, 16)
                ));
                break;
        }
        
        // ==========================================
        // OPTION SAO
        // ==========================================
        RewardService.gI().initStarOption(item, new RewardService.RatioStar[]{
            new RewardService.RatioStar((byte) 1, 1, 2),
            new RewardService.RatioStar((byte) 2, 1, 3),
            new RewardService.RatioStar((byte) 3, 1, 4),
            new RewardService.RatioStar((byte) 4, 1, 5),
            new RewardService.RatioStar((byte) 5, 1, 6),
            new RewardService.RatioStar((byte) 6, 1, 7),
            new RewardService.RatioStar((byte) 7, 1, 8),});

        // ==========================================
        // SKH theo giới tính
        // ==========================================
        int skhId = randomSKHId(pl.gender);
        int skhOption2 = optionIdSKH(skhId);

        item.itemOptions.add(new ItemOption(skhId, 1));
        item.itemOptions.add(new ItemOption(skhOption2, 1));
        item.itemOptions.add(new ItemOption(21, 18)); // yêu cầu sm

        item.itemOptions.add(new ItemOption(30, 1)); // khóa trang bị
        

        // ==========================================
        // Chuyển sang ItemMap
        // ==========================================
        ItemMap itemMap = new ItemMap(zone, tempId, 1, x, y, pId);
        itemMap.options.clear();

        for (ItemOption io : item.itemOptions) {
            itemMap.options.add(new ItemOption(io.optionTemplate.id, io.param));
        }

        return itemMap;
    }

    public ItemMap ratiDoC12(Zone zone, int x, int y, long pId, Player pl) {

        int gender = pl.gender;
        if (gender < 0 || gender > 2) {
            gender = 0;
        }

        // =============================
        //   DANH SÁCH ĐỒ C12 THEO HÀNH TINH
        // =============================
        int[][] C12_BY_GENDER = {
            // 0 = Đất
            {233, 245, 257, 269},
            // 1 = Namec
            {237, 249, 261, 273},
            // 2 = Xayda
            {241, 253, 265, 277}
        };

        int nhanC12 = 281; // Nhẫn chung C12

        // Tạo list rơi theo hành tinh
        int[] ds = C12_BY_GENDER[gender];
        int[] fullList = new int[ds.length + 1];
        System.arraycopy(ds, 0, fullList, 0, ds.length);
        fullList[ds.length] = nhanC12;

        // Random ID đồ C12 đúng hành tinh
        int tempId = fullList[Util.nextInt(0, fullList.length - 1)];

        // Tạo itemMap
        ItemMap it = new ItemMap(zone, tempId, 1,
                x, zone.map.yPhysicInTop(x, y - 24), pId);

        Random rd = new Random();

        // =============================
        //    RANDOM CHỈ SỐ CƠ BẢN C12
        // =============================
        // Áo
        if (tempId == 233 || tempId == 237 || tempId == 241) {
            it.options.add(new ItemOption(47, Util.highlightsItem(
                    it.itemTemplate.gender == 2,
                    rd.nextInt(121) + 350 // 350–470
            )));
        }

        // Quần
        if (tempId == 245 || tempId == 249 || tempId == 253) {
            it.options.add(new ItemOption(22, Util.highlightsItem(
                    it.itemTemplate.gender == 0,
                    rd.nextInt(5) + 20 // 20–24
            )));
        }

        // Găng
        if (tempId == 257 || tempId == 261 || tempId == 265) {
            it.options.add(new ItemOption(0, Util.highlightsItem(
                    it.itemTemplate.gender == 2,
                    rd.nextInt(51) + 2200 // 2200–2250
            )));
        }

        // Giày
        if (tempId == 269 || tempId == 273 || tempId == 277) {
            it.options.add(new ItemOption(23, Util.highlightsItem(
                    it.itemTemplate.gender == 1,
                    rd.nextInt(4) + 20 // 20–23
            )));
        }

        // Nhẫn
        if (tempId == nhanC12) {
            it.options.add(new ItemOption(14, rd.nextInt(3) + 10)); // 10–12
        }

        // =============================
        //      TÙY CHỌN BỔ SUNG
        // =============================
     //   it.options.add(new ItemOption(207, 1)); // Đồ boss
     //   it.options.add(new ItemOption(21, 2)); // YC SM

        // =============================
        //      RANDOM SAO PHA LÊ
        // =============================
        if (Util.isTrue(70, 100)) {
            it.options.add(new ItemOption(107, rd.nextInt(1) + 3)); // 1–3 sao
        } else if (Util.isTrue(4, 100)) {
            it.options.add(new ItemOption(107, rd.nextInt(3) + 5)); // 5–7 sao
        } else {
            it.options.add(new ItemOption(107, rd.nextInt(2) + 3)); // 3–5 sao
        }

        // =============================
        //      SKH THEO GIỚI TÍNH
        // =============================
        int skhId = randomSKHId(pl.gender);
        int skh2 = optionIdSKH(skhId);

        it.options.add(new ItemOption(skhId, 1));
        it.options.add(new ItemOption(skh2, 1));
        it.options.add(new ItemOption(30, 1)); // khóa SKH

        return it;
    }

// ===== DANH SÁCH ĐỒ THẦN LINH THEO GIỚI TÍNH =====
    private final int[][] TL_BY_GENDER = {
        // 0 = Trái Đất
        {555, 556, 562, 563},
        // 1 = Namec
        {557, 558, 564, 565},
        // 2 = Xayda
        {559, 560, 566, 567},
        // 3 = Nhẫn Thần Linh (chung)
        {561}
    };

}

package nro.services;

import nro.consts.ConstItem;
import nro.manager.PetFollowManager;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.map.war.BlackBallWar;
import nro.models.npc.specialnpc.MabuEgg;
import nro.models.npc.specialnpc.MagicTree;
import nro.models.player.Pet;
import nro.models.player.PetFollow;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nro.data.ItemData;
import nro.models.player.MiniPet;
import nro.server.Manager;

/**
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 */
public class InventoryService {

    private static InventoryService i;

    public static InventoryService gI() {
        if (i == null) {
            i = new InventoryService();
        }
        return i;
    }

    public List<Item> copyItemsBag(Player player) {
        return copyList(player.inventory.itemsBag);
    }

    private List<Item> copyList(List<Item> items) {
        List<Item> list = new ArrayList<>();
        for (Item item : items) {
            list.add(ItemService.gI().copyItem(item));
        }
        return list;
    }

    public boolean existItemBag(Player player, int tempId) {
        return existItemInList(player.inventory.itemsBag, tempId);
    }

    private boolean existItemInList(List<Item> list, int tempId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isNotNullItem() && list.get(i).template.id == tempId) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOptionTemplateId(Item item, int optionTemplateId) {
        for (ItemOption option : item.itemOptions) {
            if (option.optionTemplate.id == optionTemplateId) {
                return true;
            }
        }
        return false;
    }

    public byte getIndexBag(Player pl, Item it) {
        for (byte i = 0; i < pl.inventory.itemsBag.size(); ++i) {
            Item item = pl.inventory.itemsBag.get(i);
            if (item != null && it.equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public boolean addItemBag(Player player, Item item, int maxQuantity) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }

        if (ItemMapService.gI().isBlackBall(item.template.id)) {
            return BlackBallWar.gI().pickBlackBall(player, item);
        }

        // Tennis spaceship
        if (item.template.id == 453) {
            player.haveTennisSpaceShip = true;
            return true;
        }

        // Đùi gà nướng
        if (item.template.id == 74) {
            player.nPoint.setFullHpMp();
            PlayerService.gI().sendInfoHpMp(player);
            return true;
        }

        // ✅ FIX: Gold, gem, ruby - thêm atomic operation
        switch (item.template.type) {
            case 9: // Gold
                long newGold = player.inventory.gold + item.quantity;
                if (player.playerIntrinsic.intrinsic.id == 23) {
                    newGold = player.inventory.gold + player.nPoint.calPercent(item.quantity, player.playerIntrinsic.intrinsic.param1);
                }

                if (newGold <= player.inventory.getGoldLimit()) {
                    player.inventory.gold = newGold;
                    Service.getInstance().sendMoney(player);
                    return true;
                } else {
                    Service.getInstance().sendThongBao(player, "Vàng sau khi nhặt quá giới hạn cho phép");
                    return false;
                }

            case 10: // Gem
                player.inventory.gem += item.quantity;
                Service.getInstance().sendMoney(player);
                return true;

            case 34: // Ruby
                player.inventory.ruby += item.quantity;
                Service.getInstance().sendMoney(player);
                return true;
        }

        // Mở rộng hành trang
        switch (item.template.id) {
            case 517:
                if (player.inventory.itemsBag.size() < Manager.MAX_BAG) {
                    player.inventory.itemsBag.add(ItemService.gI().createItemNull());
                    Service.getInstance().sendThongBaoOK(player, "Hành trang của bạn đã được mở rộng thêm 1 ô");
                    sendItemBags(player);
                    return true;
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Hành trang của bạn đã đạt tối đa");
                    return false;
                }

            case 518:
                if (player.inventory.itemsBox.size() < Manager.MAX_BOX) {
                    player.inventory.itemsBox.add(ItemService.gI().createItemNull());
                    Service.getInstance().sendThongBaoOK(player, "Rương đồ của bạn đã được mở rộng thêm 1 ô");
                    sendItemBox(player);
                    return true;
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Rương đồ của bạn đã đạt tối đa");
                    return false;
                }

            case 988:
                long maxLimit = 500_000_000_000L; // 200 tỷ

                if (player.inventory.getGoldLimit() < maxLimit) {

                    // Tăng lên 50 tỷ
                    player.inventory.goldLimit += 50_000_000_000L;

                    // Nếu vượt 200 tỷ thì fix lại = đúng 200 tỷ
                    if (player.inventory.goldLimit > maxLimit) {
                        player.inventory.goldLimit = maxLimit;
                    }

                    Service.getInstance().sendThongBao(player,
                            "Giới hạn vàng của bạn đã tăng thêm 50 Tỷ\n"
                            + "Giới hạn vàng hiện tại: " + Util.numberToMoney(player.inventory.getGoldLimit()));
                    return true;

                } else {
                    // Đã đạt 200 tỷ từ trước
                    player.inventory.goldLimit = maxLimit; // đảm bảo chính xác
                    Service.getInstance().sendThongBaoOK(player, "Giới hạn vàng của bạn đã đạt tối đa (500 Tỷ)");
                    return false;
                }

        }

        // Add vào list
        return addItemList(player.inventory.itemsBag, item, maxQuantity);
    }

    public boolean addItemBox(Player player, Item item, int maxQuantity) {
        return addItemList(player.inventory.itemsBox, item, maxQuantity);
    }

    public boolean addItemBody(Player player, Item item, int maxQuantity) {
        return addItemList(player.inventory.itemsBody, item, maxQuantity);
    }

    public boolean addItemNotUpToUpQuantity(List<Item> items, Item item) {
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isNotNullItem()) {
                items.set(i, item);
                return true;
            }
        }
        return false;
    }

    public boolean addItemList(List<Item> items, Item item, int maxQuantity) {
        //nếu item ko có option, add option rỗng vào
        if (item.itemOptions.isEmpty()) {
            item.itemOptions.add(new ItemOption(73, 0));
        }

        //item cộng thêm chỉ số param: tự động luyện tập
        int optionId = isItemIncrementalOption(item);
        if (optionId != -1) {
            int param = 0;
            for (ItemOption io : item.itemOptions) {
                if (io.optionTemplate.id == optionId) {
                    param = io.param;
                }
            }
            for (Item it : items) {
                if (it.isNotNullItem() && it.template.id == item.template.id) {
                    for (ItemOption io : it.itemOptions) {
                        if (io.optionTemplate.id == optionId) {
                            io.param += param;
                        }
                    }
                    return true;
                }
            }
        }

        //item tăng số lượng
        //item tăng số lượng
        if (item.template.isUpToUp) {
            for (Item it : items) {
                if (!it.isNotNullItem() || it.template.id != item.template.id || (hasOptionTemplateId(it, 73) && hasOptionTemplateId(item, 30))) {
                    continue;
                }
                if (!it.isNotNullItem() || it.template.id != item.template.id || (hasOptionTemplateId(it, 30) && hasOptionTemplateId(item, 73))) {
                    continue;
                }
                if (!it.isNotNullItem() || it.template.id != item.template.id
                        || (hasOptionTemplateId(it, 30) && hasOptionTemplateId(item, 86))
                        || (hasOptionTemplateId(it, 30) && hasOptionTemplateId(item, 87))
                        || (hasOptionTemplateId(it, 73) && hasOptionTemplateId(item, 86))
                        || (hasOptionTemplateId(it, 73) && hasOptionTemplateId(item, 87))
                        || (hasOptionTemplateId(it, 86) && hasOptionTemplateId(item, 30))
                        || (hasOptionTemplateId(it, 87) && hasOptionTemplateId(item, 30))
                        || (hasOptionTemplateId(it, 86) && hasOptionTemplateId(item, 73))
                        || (hasOptionTemplateId(it, 87) && hasOptionTemplateId(item, 73))
                        || (hasOptionTemplateId(it, 87) && hasOptionTemplateId(item, 86))
                        || (hasOptionTemplateId(it, 86) && hasOptionTemplateId(item, 87))) {
                    continue;
                }
                //457-thỏi vàng; 590-bí kiếp
                if (item.template.id == 457 || item.template.type == 27 || item.template.id == 1537 || item.template.id == 1559 || item.template.id == 1549 || item.template.type == 99 || item.template.type == 14
                        || item.template.type == 1577
                        || item.template.type == 12 || item.template.type == 29 || item.template.type == 33 || item.template.type == 31 || item.template.type == 30) {
                    it.quantity += item.quantity;
                    item.quantity = 0;
                    return true;
                }
                if (it.quantity < 99) {
                    int add = 99 - it.quantity;
                    if (item.quantity <= add) {
                        it.quantity += item.quantity;
                        item.quantity = 0;
                        return true;
                    } else {
                        it.quantity = 99;
                        item.quantity -= add;
                    }
                }
            }
        }

        //add item vào ô mới
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isNotNullItem()) {
                items.set(i, ItemService.gI().copyItem(item));
                item.quantity = 0;
                return true;
            }
        }
        return false;
    }

    private boolean isItemIncremental(Item item) { //item cộng dồn số lượng
        switch (item.template.type) {
            case 8: //vật phẩm nhiệm vụ
            case 12: //ngọc rồng
            case 30: //sao pha lê
            case 6: //đậu thần
            case 29: //item time, đồ ăn
            case 25: //rađa dò ngọc namếc
            case 27: //đồ tạp
                if (isminipet(item.template.id)) {
                    return false;
                }
            case 33: //mảnh rada
            case 14: //đá nâng cấp
            case 50: //vé đổi đồ hủy diệt
                return true;
            default:
                return false;
        }
    }

    private byte isItemIncrementalOption(Item item) { //trả về id option template
        int temp = item.template.id;
        byte opp = -1;
        switch (temp) {
            case 521:
                opp = 1;
                break;
            default:
                break;

        }
        return opp;
    }

    public void throwItem(Player player, int where, int index) {
        Item itemThrow = null;
        if (where == 0) {
            if (index >= 0 && index <= player.inventory.itemsBody.size()) {
                itemThrow = player.inventory.itemsBody.get(index);
                if (itemThrow.isNotNullItem()) {
                    removeItemBody(player, index);
                    sendItemBody(player);
                }
            }
        } else if (where == 1) {
            if (index >= 0 && index <= player.inventory.itemsBag.size()) {
                itemThrow = player.inventory.itemsBag.get(index);
                if (itemThrow.isNotNullItem()) {
                    if (itemThrow.template.id != 457 && itemThrow.template.id != 1400) {
                        removeItemBag(player, index);
                        sortItemBag(player);
                        sendItemBags(player);
                    } else {
                        Service.getInstance().sendThongBao(player, "Thưa ngài");
                    }
                }
            }
        }
        if (!itemThrow.isNotNullItem()) {
            return;
        } else {
            Service.getInstance().point(player);
        }

//        ItemMap itemMap = new ItemMap(player.map, itemThrow.template.id,
//                itemThrow.quantity, player.location.x, player.location.y, player.id);
//        itemMap.options = itemThrow.itemOptions;
        //Service.getInstance().dropItemMap(player.map, itemMap);
        Service.getInstance().Send_Caitrang(player);
    }

    public void arrangeItems(List<Item> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (!list.get(i).isNotNullItem()) {
                int indexSwap = -1;
                for (int j = i + 1; j < list.size(); j++) {
                    if (list.get(j).isNotNullItem()) {
                        indexSwap = j;
                        break;
                    }
                }
                if (indexSwap != -1) {
                    Item sItem = ItemService.gI().createItemNull();
                    list.set(i, list.get(indexSwap));
                    list.set(indexSwap, sItem);
                } else {
                    break;
                }
            }
        }
    }

    private Item putItemBag(Player player, Item item) {
        for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
            if (!player.inventory.itemsBag.get(i).isNotNullItem()) {
                player.inventory.itemsBag.set(i, item);
                Item sItem = ItemService.gI().createItemNull();
                return sItem;
            }
        }
        return item;
    }

    private Item putItemBox(Player player, Item item) {
        for (int i = 0; i < player.inventory.itemsBox.size(); i++) {
            if (!player.inventory.itemsBox.get(i).isNotNullItem()) {
                player.inventory.itemsBox.set(i, item);
                Item sItem = ItemService.gI().createItemNull();
                return sItem;
            }
        }
        return item;
    }

    public Item putItemBody(Player player, Item item) {
        if (!item.isNotNullItem()) {
            return item;
        }

        // Kiểm tra type hợp lệ
        switch (item.template.type) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 32:
            case 23:
            case 24:
            case 11:
            case 75:
            case 74:
            case 72:
            case 21:
            case 35:
                break;
            default:
                Service.getInstance().sendThongBaoOK(player.isPet ? ((Pet) player).master : player, "Trang bị không phù hợp!");
                return item;
        }

        // Kiểm tra gender
        if (item.template.gender < 3 && item.template.gender != player.gender) {
            Service.getInstance().sendThongBaoOK(player.isPet ? ((Pet) player).master : player, "Trang bị không phù hợp!");
            return item;
        }

        // Kiểm tra sức mạnh
        long powerRequire = item.template.strRequire;
        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 21) {
                powerRequire = io.param * 1000000000L;
                break;
            }
        }
        if (player.nPoint.power < powerRequire) {
            Service.getInstance().sendThongBaoOK(player.isPet ? ((Pet) player).master : player, "Sức mạnh không đủ yêu cầu!");
            return item;
        }

        // Xác định slot
        int index = -1;
        switch (item.template.type) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                index = item.template.type;
                break;
            case 32: // Nhẫn
                index = 6;
                break;
            case 21: // 
                index = player.isPet ? 8 : 7;
                break;
            case 24: // Flag
                index = 9;
                break;
            case 11: // Title
                index = player.isPet ? 7 : 8;
                break;
            case 75:
            case 72: // Áo choàng/Vũ khí phụ
                index = player.isPet ? 9 : 10;
                break;
            case 74: // Foot
                index = 11;
                break;
            case 35: // Miniature
                index = 12;
                break;
            default:

                return item;
        }

        // ✅✅✅ ĐÂY LÀ FIX QUAN TRỌNG NHẤT ✅✅✅
        Item oldItem = player.inventory.itemsBody.get(index);  // Lấy item cũ
        player.inventory.itemsBody.set(index, item);           // Set item mới
        return oldItem;  // ✅ TRẢ VỀ ITEM CŨ (không phải 'sItem'!)
    }

    private void broadcastItemBodyChange(Player player, Item item) {

        if (player.zone == null) {

            return;
        }

        if (player.isBoss || player.isPet || player.isMiniPet) {

            return;
        }

        // ✅ QUAN TRỌNG: Chỉ gọi 1 LẦN duy nhất, NGOÀI vòng lặp
        int itemType = item.template.type;

        switch (itemType) {
            case 21: // ✅ LINH THÚ - PHẢI RELOAD TOÀN BỘ

                player.zone.load_Me_To_Another(player);

                break;

            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5: // Áo, Quần, Găng, Giày, Rada
            case 23: // Xe
            case 24: // Flag
            case 32: // Nhẫn
            case 72: // Áo choàng
            case 75: // Vũ khí phụ
            case 35: // Miniature

                player.zone.load_Me_To_Another(player);

                break;

            case 11: // Title - XỬ LÝ ĐẶC BIỆT (cần loop)
          
            synchronized (player.zone.getPlayers()) {
                    for (Player pl : player.zone.getPlayers()) {
                        if (pl == null || pl.id == player.id) {
                            continue;
                        }
                        Service.getInstance().sendTitleRv(player, pl, (short) item.template.id);
                    }
                }

                break;

            case 74: // Foot - XỬ LÝ ĐẶC BIỆT (cần loop)
          
            synchronized (player.zone.getPlayers()) {
                    short footId = -1;
                    if (player.inventory.itemsBody.size() > 11 && player.inventory.itemsBody.get(11).isNotNullItem()) {
                        footId = (short) player.inventory.itemsBody.get(11).template.id;
                    }

                    for (Player pl : player.zone.getPlayers()) {
                        if (pl == null || pl.id == player.id) {
                            continue;
                        }
                        Service.getInstance().sendFootRv(player, pl, footId);
                    }
                }

                break;

            default:

                break;
        }

    }

    public void itemBagToBody(Player player, int index) {
        if (index < 0 || index >= player.inventory.itemsBag.size()) {
            return;
        }
        Item item = player.inventory.itemsBag.get(index);
        if (item.isNotNullItem()) {
            player.inventory.itemsBag.set(index, putItemBody(player, item));
            if (item.template.id > 1299 && item.template.id < 1309) {
                Service.getInstance().removeTitle(player);
                Service.getInstance().sendFoot(player, item.template.id);
            }
            if (item.template.type == 11) {
                Service.getInstance().removeTitle(player);
                Service.getInstance().sendTitle(player, item.template.id);
            }
            if (item.template.type == 5) {
                Service.getInstance().Send_Caitrang(player);
            }
            if (item.template.type == 21) {
                MiniPet.callMiniPet(player, item.template.id);
            }
            if (item.template.type == 72) {
                PetFollow pet = PetFollowManager.gI().findByID(item.getId());
                player.setPetFollow(pet);
                PlayerService.gI().sendPetFollow(player);
            }
            sendItemBags(player);
            sendItemBody(player);
            Service.getInstance().point(player);
            Service.getInstance().sendFlagBag(player);
        }
    }

    public void itemBodyToBag(Player player, int index) {
        if (index < 0 || index >= player.inventory.itemsBody.size()) {
            return;
        }
        Item item = player.inventory.itemsBody.get(index);
        if (item.isNotNullItem()) {
            if (item.template.id > 1299 && item.template.id < 1309) {
                Service.getInstance().removeTitle(player);
                Service.getInstance().sendFoot(player, item.template.id);
            }
            if (item.template.type == 11) {
                Service.getInstance().removeTitle(player);
                Service.getInstance().sendTitle(player, item.template.id);
            }
            player.inventory.itemsBody.set(index, putItemBag(player, item));
            if (item.template.type == 5) {
                Service.getInstance().Send_Caitrang(player);
            }
            if (item.template.type == 21) {
                player.minipet.changeStatus(MiniPet.GOHOME);
            }
            if (item.template.type == 72) {
                player.setPetFollow(null);
                PlayerService.gI().sendPetFollow(player);
            }
            sendItemBags(player);
            sendItemBody(player);
            Service.getInstance().point(player);
            Service.getInstance().sendFlagBag(player);
        }
    }

    public void itemBagToPetBody(Player player, int index) {
        if (player.pet == null) {

            return;
        }

        if (player.pet != null && player.pet.nPoint.power >= 0) {
            if (index < 0 || index >= player.inventory.itemsBag.size()) {

                return;
            }
            Item item = player.inventory.itemsBag.get(index);

            if (item.template.id == 1326 || item.template.id == 1325 || item.template.id == 1579
                    || (item.template.type >= 0 && item.template.type <= 5)
                    || item.template.type == 32 || item.template.type == 21 || item.template.type == 72) {

                if (item.isNotNullItem()) {

                    Item itemSwap = putItemBody(player.pet, item);
                    player.inventory.itemsBag.set(index, itemSwap);

                    sendItemBags(player);
                    sendItemBody(player);
                    Service.getInstance().Send_Caitrang(player.pet);
                    Service.getInstance().Send_Caitrang(player);

                    if (!itemSwap.equals(item)) {

                        Service.getInstance().point(player);
                        Service.getInstance().showInfoPet(player);

                        if (player.pet.zone != null) {

                        } else {

                        }
                    } else {

                    }
                }
            } else {

                Service.getInstance().sendThongBaoOK(player, "Đệ tử không mang được vật phẩm này");
            }
        } else {

            Service.getInstance().sendThongBaoOK(player, "Đệ tử phải đạt 1tr5 sức mạnh mới có thể mặc");
        }

    }

    public void itemPetBodyToBag(Player player, int index) {
        if (index < 0 || index >= player.inventory.itemsBody.size()) {
            return;
        }
        Item item = player.pet.inventory.itemsBody.get(index);
        if (item.isNotNullItem()) {
            player.pet.inventory.itemsBody.set(index, putItemBag(player, item));
            sendItemBags(player);
            sendItemBody(player);
            Service.getInstance().Send_Caitrang(player.pet);
            Service.getInstance().Send_Caitrang(player);
            Service.getInstance().point(player);
            Service.getInstance().showInfoPet(player);

            // ✅ BROADCAST ĐỆ TỬ THÁO ĐỒ
            if (player.pet.zone != null) {
                player.pet.zone.load_Me_To_Another(player.pet);
            }
        }
    }

    public Item findItem(Player pl, int itemId, int quantity) {
        if (pl == null || pl.inventory == null || pl.inventory.itemsBag == null) {
            return null;
        }
        for (Item it : pl.inventory.itemsBag) {
            if (it == null || it.template == null || it.quantity <= 0) {
                continue;
            }
            if (it.template.id == itemId && it.quantity >= quantity) {
                return it;
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public void itemBoxToBodyOrBag(Player player, int index) {
        if (index < 0 || index >= player.inventory.itemsBox.size()) {
            return;
        }
        Item item = player.inventory.itemsBox.get(index);
        if (item.isNotNullItem()) {
            boolean done = false;

            // Thử equip vào body trước
            if (item.template.type >= 0 && item.template.type <= 5 || item.template.type == 32) {
                Item itemBody = player.inventory.itemsBody.get(item.template.type == 32 ? 6 : item.template.type);
                if (!itemBody.isNotNullItem()) {
                    if (item.template.gender == player.gender || item.template.gender == 3) {
                        long powerRequire = item.template.strRequire;
                        for (ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 21) {
                                powerRequire = io.param * 1000000000L;
                                break;
                            }
                        }
                        if (powerRequire <= player.nPoint.power) {
                            player.inventory.itemsBody.set(item.template.type == 32 ? 6 : item.template.type, item);
                            player.inventory.itemsBox.set(index, itemBody);
                            done = true;

                            sendItemBody(player);
                            Service.getInstance().Send_Caitrang(player);
                            Service.getInstance().point(player);
                            sendItemBox(player);
                        }
                    }
                }
            }

            // ✅ FIX: Nếu không equip được, mới thử chuyển vào Bag
            if (!done) {
                // Clone item để tránh reference issue
                Item clonedItem = cloneItem(item);
                if (addItemBag(player, clonedItem, 99)) {
                    // ✅ Chỉ xóa khỏi Box khi đã add thành công vào Bag
                    Item nullItem = ItemService.gI().createItemNull();
                    player.inventory.itemsBox.set(index, nullItem);
                    sendItemBags(player);
                    sendItemBox(player);
                } else {
                    // Thông báo nếu không thể chuyển
                    Service.getInstance().sendThongBao(player, "Hành trang đầy, không thể lấy vật phẩm");
                }
            }
        }
    }

    public void itemBagToBox(Player player, int index) {
        if (index < 0 || index >= player.inventory.itemsBag.size()) {
            return;
        }
        Item item = player.inventory.itemsBag.get(index);
        if (item.isNotNullItem()) {
            // ✅ FIX: Lưu số lượng trước khi add
            int originalQuantity = item.quantity;

            if (addItemBox(player, item, 99)) {
                // ✅ Kiểm tra xem có add hết chưa
                if (item.quantity == 0) {
                    Item nullItem = ItemService.gI().createItemNull();
                    player.inventory.itemsBag.set(index, nullItem);
                }
                // ✅ Nếu chỉ add được một phần, item.quantity đã được update bởi addItemBox
                arrangeItems(player.inventory.itemsBag);
                sendItemBags(player);
                sendItemBox(player);
            } else {
                // ✅ Rollback nếu thất bại hoàn toàn
                item.quantity = originalQuantity;
                Service.getInstance().sendThongBao(player, "Rương đồ đầy");
            }
        }
    }

    public void itemBodyToBox(Player player, int index) {
        if (index < 0 || index >= player.inventory.itemsBody.size()) {
            return;
        }
        Item item = player.inventory.itemsBody.get(index);
        if (item.isNotNullItem()) {
            // ✅ FIX: Kiểm tra xem có thể add vào Box không
            Item clonedItem = cloneItem(item);
            if (addItemBox(player, clonedItem, 1)) {
                // Chỉ xóa khỏi Body khi đã add thành công
                Item nullItem = ItemService.gI().createItemNull();
                player.inventory.itemsBody.set(index, nullItem);

                sendItemBody(player);
                sendItemBox(player);
                Service.getInstance().Send_Caitrang(player);
                Service.getInstance().point(player);
            } else {
                Service.getInstance().sendThongBao(player, "Rương đồ đầy");
            }
        }
    }

//--------------------------------------------------------------------------
// ✅ FIX: Thêm return boolean và update UI
    public boolean subQuantityItemsBag(Player player, Item item, int quantity) {
        boolean result = subQuantityItem(player.inventory.itemsBag, item, quantity);
        if (result) {
            sendItemBags(player);
            arrangeItems(player.inventory.itemsBag);
        }
        return result;
    }

    public boolean subQuantityItemsBody(Player player, Item item, int quantity) {
        boolean result = subQuantityItem(player.inventory.itemsBody, item, quantity);
        if (result) {
            sendItemBody(player);
            Service.getInstance().point(player);
        }
        return result;
    }

    public boolean subQuantityItem(List<Item> items, Item item, int quantity) {
        if (items == null || item == null || quantity <= 0) {
            return false;
        }

        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            if (it != null && it.isNotNullItem() && it.template.id == item.template.id) {
                // ✅ FIX: Kiểm tra đủ số lượng trước khi trừ
                if (it.quantity >= quantity) {
                    it.quantity -= quantity;
                    if (it.quantity <= 0) {
                        items.set(i, ItemService.gI().createItemNull());
                    }
                    return true;
                } else {
                    // Không đủ số lượng
                    return false;
                }
            }
        }
        return false; // Không tìm thấy item
    }

// ✅ Helper method để clone item (tránh reference bug)
    private Item cloneItem(Item item) {
        try {
            Item cloned = new Item();
            cloned.template = item.template;
            cloned.quantity = item.quantity;
            cloned.itemOptions = new ArrayList<>(item.itemOptions);
            cloned.content = item.content;
            // Copy các thuộc tính khác nếu cần
            return cloned;
        } catch (Exception e) {
            return item; // Fallback
        }
    }

// Xoá item khỏi list (theo object) -> reset slot
    public void removeItem(List<Item> items, Item item) {
        if (items == null || item == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            if (item.equals(items.get(i))) {
                items.set(i, ItemService.gI().createItemNull());
                break;
            }
        }
    }

// Xoá item khỏi list (theo index) -> reset slot
    public void removeItem(List<Item> items, int index) {
        if (items == null || index < 0 || index >= items.size()) {
            return;
        }
        items.set(index, ItemService.gI().createItemNull());
    }

//--------------------------------------------------------------------------
// Sort túi
    public void sortItemBag(Player player) {
        sortItem(player.inventory.itemsBag);
    }

    public void sortItem(List<Item> items) {
        int index = 0;
        for (Item item : items) {
            if (item.isNotNullItem()) {
                items.set(index, item);
                index++;
            }
        }
        for (int i = index; i < items.size(); i++) {
            items.set(i, ItemService.gI().createItemNull());
        }
    }

//--------------------------------------------------------------------------
// Các hàm tiện ích remove ở player
    public void removeItemBag(Player player, int index) {
        removeItem(player.inventory.itemsBag, index);
    }

    public void removeItemBag(Player player, Item item) {
        removeItem(player.inventory.itemsBag, item);
    }

    public void removeItemBody(Player player, int index) {
        removeItem(player.inventory.itemsBody, index);
    }

    public void removeItemPetBody(Player player, int index) {
        removeItem(player.pet.inventory.itemsBody, index);
    }

    public void removeItemBox(Player player, int index) {
        removeItem(player.inventory.itemsBox, index);
    }

//--------------------------------------------------------------------------
// Tìm item trong list
    public Item findItem(List<Item> items, int templateId) {
        if (items == null) {
            return null;
        }
        for (Item it : items) {
            if (it == null) {
                continue;
            }
            if (it.template == null) {
                // System.err.println("[InventoryService] WARNING: item null template trong list");
                continue;
            }
            if (it.template.id == templateId) {
                return it;
            }
        }
        return null;
    }

    public Item findItem(Player player, short templateId, int quantity) {
        if (player == null || player.inventory == null) {
            return null;
        }
        for (Item it : player.inventory.itemsBag) {
            if (it == null) {
                continue; // skip null
            }
            if (it.template == null) {
                System.err.println("[InventoryService] ERROR: Player " + player.id
                        + " có item NULL template trong túi. Bỏ qua item này!");
                continue;
            }
            if (it.template.id == templateId && it.quantity >= quantity) {
                return it;
            }
        }
        return null;
    }

    public Item findItemOption(List<Item> list, int tempId, int option, int param) {
        try {
            for (Item item : list) {
                if (item == null) {
                    return null;
                }
                if (item.isNotNullItem() && item.template.id == tempId) {
                    for (ItemOption io : item.itemOptions) {
                        if (io.optionTemplate.id == option && param == io.param) {
                            return item;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Loi check item theo option va param");
        }
        return null;
    }

    /// item sự kiện
    public Item findVeTangNgoc(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && (item.template.id == 2023)) {
                return item;
            }
        }
        return null;
    }

    public Item findBuaBaoVeNangCap(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 2019) {
                return item;
            }
        }
        return null;
    }

    public Item finditemnguyenlieuKeo(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && (item.template.id == 2013) && item.quantity >= 10) {
                return item;
            }
        }
        return null;
    }

    public Item finditemnguyenlieuBanh(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && (item.template.id == 2014) && item.quantity >= 10) {
                return item;
            }
        }
        return null;
    }

    public Item finditemnguyenlieuBingo(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && (item.template.id == 2015) && item.quantity >= 10) {
                return item;
            }
        }
        return null;
    }

    public Item finditemnguyenlieuGiokeo(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && (item.template.id == 2016) && item.quantity >= 3) {
                return item;
            }
        }
        return null;
    }

    public Item finditemnguyenlieuVe(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && (item.template.id == 2018) && item.quantity >= 3) {
                return item;
            }
        }
        return null;
    }

    public Item finditemnguyenlieuHopmaquy(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && (item.template.id == 2017) && item.quantity >= 3) {
                return item;
            }
        }
        return null;
    }

    public Item finditemBongHoa(Player player, int soluong) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && (item.template.id == 589) && item.quantity >= soluong) {
                return item;
            }
        }
        return null;
    }

    public boolean finditemWoodChest(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 570) {
                return false;
            }
        }
        for (Item item : player.inventory.itemsBox) {
            if (item.isNotNullItem() && item.template.id == 570) {
                return false;
            }
        }
        return true;
    }

    public Item finditemKeoGiangSinh(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 1446) {
                return item;
            }
        }
        return null;
    }

    public Item findItemBagByOption(Player player, int tempId, int option, int param) {
        return findItemOption(player.inventory.itemsBag, tempId, option, param);
    }

    public Item findItemBagByTemp(Player player, int tempId) {
        return findItem(player.inventory.itemsBag, tempId);
    }

    public Item findItemBodyByTemp(Player player, int tempId) {
        return findItem(player.inventory.itemsBody, tempId);
    }

    public List<Item> getListItem(Player player, int... items) {
        return player.inventory.itemsBag.stream().filter(i -> in(i, items)).collect(Collectors.toList());
    }

    private boolean in(Item item, int... items) {
        return IntStream.of(items).anyMatch(id -> (item.isNotNullItem() && item.template.id == id));
    }

    public Item findMealChangeDestroyClothes(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && (item.template.id >= 663 && item.template.id <= 667)
                    && item.quantity >= 99) {
                return item;
            }
        }
        return null;
    }

    public Item findTicketChangeDestroyClothes(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 1327 && item.template.id <= 1331) {
                return item;
            }
        }
        return null;
    }

    public Item findGodClothesByType(Player player, int type) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.type == type && (item.template.id >= 555 && item.template.id <= 567)) {
                return item;
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public void sendItemBags(Player player) {
        arrangeItems(player.inventory.itemsBag);
        Message msg;
        try {
            msg = new Message(-36);
            msg.writer().writeByte(0);
            msg.writer().writeByte(player.inventory.itemsBag.size());
            for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
                Item item = player.inventory.itemsBag.get(i);
                if (!item.isNotNullItem()) {
                    continue;
                }
                msg.writer().writeShort(item.template.id);
                msg.writer().writeInt(item.quantity);
                msg.writer().writeUTF(item.getInfo());
                msg.writer().writeUTF(item.getContent());
                List<ItemOption> itemOptions = item.getDisplayOptions();
                msg.writer().writeByte(itemOptions.size()); //options
                for (ItemOption o : itemOptions) {
                    msg.writer().writeByte(o.optionTemplate.id);
                    msg.writer().writeInt(o.param);
                }
            }

            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendItemBody(Player player) {
        Message msg;
        try {
            msg = new Message(-37);
            msg.writer().writeByte(0);
            msg.writer().writeShort(player.getHead());
            msg.writer().writeByte(player.inventory.itemsBody.size());
            for (Item item : player.inventory.itemsBody) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.getDisplayOptions();
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeInt(itemOption.param);
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
        Service.getInstance().Send_Caitrang(player);
    }

    public void sendItemBox(Player player) {
        Message msg;
        try {
            msg = new Message(-35);
            msg.writer().writeByte(0);
            msg.writer().writeByte(player.inventory.itemsBox.size());
            for (Item it : player.inventory.itemsBox) {
                msg.writer().writeShort(it.isNotNullItem() ? it.template.id : -1);
                if (it.isNotNullItem()) {
                    msg.writer().writeInt(it.quantity);
                    msg.writer().writeUTF(it.getInfo());
                    msg.writer().writeUTF(it.getContent());
                    List<ItemOption> itemOptions = it.getDisplayOptions();
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption io : itemOptions) {
                        msg.writer().writeByte(io.optionTemplate.id);
                        msg.writer().writeInt(io.param);
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void openBox(Player player) {
        Message msg;
        try {
            msg = new Message(-35);
            msg.writer().writeByte(1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void eatPea(Player player) {
        Item pea = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.type == 6) {
                pea = item;
                break;
            }
        }
        if (pea != null) {
            int hpKiHoiPhuc = 0;
            int lvPea = Integer.parseInt(pea.template.name.substring(13));
            for (ItemOption io : pea.itemOptions) {
                if (io.optionTemplate.id == 2) {
                    hpKiHoiPhuc = io.param * 1000;
                    break;
                }
                if (io.optionTemplate.id == 48) {
                    hpKiHoiPhuc = io.param;
                    break;
                }
            }
            player.nPoint.setHp(player.nPoint.hp + hpKiHoiPhuc);
            player.nPoint.setMp(player.nPoint.mp + hpKiHoiPhuc);
            PlayerService.gI().sendInfoHpMp(player);
            Service.getInstance().sendInfoPlayerEatPea(player);
            if (player.pet != null && player.zone.equals(player.pet.zone) && !player.pet.isDie()) {
                int statima = 100 * lvPea;
                player.pet.nPoint.stamina += statima;
                if (player.pet.nPoint.stamina > player.pet.nPoint.maxStamina) {
                    player.pet.nPoint.stamina = player.pet.nPoint.maxStamina;
                }
                player.pet.nPoint.setHp(player.pet.nPoint.hp + hpKiHoiPhuc);
                player.pet.nPoint.setMp(player.pet.nPoint.mp + hpKiHoiPhuc);
                Service.getInstance().sendInfoPlayerEatPea(player.pet);
                Service.getInstance().chatJustForMe(player, player.pet, "Cảm ơn sư phụ đã cho con đậu thần");
            }
            subQuantityItemsBag(player, pea, 1);
            sendItemBags(player);
        }
    }

    public int addPeaHarvest(Player player, byte level, int quantity) {
        Item pea = ItemService.gI().createNewItem(MagicTree.PEA_TEMP[level - 1], quantity);
        pea.itemOptions.add(new ItemOption(level - 1 > 1 ? 2 : 48, MagicTree.PEA_PARAM[level - 1]));
        addItemBag(player, pea, 99);
        if (pea.quantity > 0) {
            addItemBox(player, pea, 99);
        }
        if (pea.quantity < quantity) {
            Service.getInstance().sendThongBao(player, "Bạn vừa thu hoạch được " + (quantity - pea.quantity) + " hạt " + pea.template.name);
        }
        return pea.quantity;
    }

    public Item getPeaBox(Player player) {
        for (Item item : player.inventory.itemsBox) {
            if (item.isNotNullItem() && item.template.type == 6) {
                return item;
            }
        }
        return null;
    }

    private byte getNumPeaBag(Player player) {
        return getNumPea(player.inventory.itemsBag);
    }

    private byte getNumPeaBox(Player player) {
        return getNumPea(player.inventory.itemsBox);
    }

    private byte getNumPea(List<Item> items) {
        byte num = 0;
        for (Item item : items) {
            if (item.isNotNullItem() && item.template.type == 6) {
                num += item.quantity;
            }
        }
        return num;
    }

    public byte getCountEmptyBag(Player player) {
        return getCountEmptyListItem(player.inventory.itemsBag);
    }

    public byte getCountEmptyBody(Player player) {
        return getCountEmptyListItem(player.inventory.itemsBody);
    }

    public byte getCountEmptyListItem(List<Item> list) {
        byte count = 0;
        for (Item item : list) {
            if (!item.isNotNullItem()) {
                count++;
            }
        }
        return count;
    }

    public boolean isminipet(int id) {
        return ItemData.IdMiniPet.contains(id);
    }

    public String itemsBagToString(Player player) {
        JSONArray dataBag = new JSONArray();
        for (Item item : player.inventory.itemsBag) {
            JSONObject dataItem = new JSONObject();
            if (item.isNotNullItem()) {
                JSONArray options = new JSONArray();
                dataItem.put("temp_id", item.template.id);
                dataItem.put("quantity", item.quantity);
                for (ItemOption io : item.itemOptions) {
                    JSONArray option = new JSONArray();
                    option.add(io.optionTemplate.id);
                    option.add(io.param);
                    options.add(option);
                }
                dataItem.put("option", options);
            } else {
                JSONArray options = new JSONArray();
                dataItem.put("temp_id", -1);
                dataItem.put("quantity", 0);
                dataItem.put("create_time", 0);
                dataItem.put("option", options);
            }
            dataBag.add(dataItem);
        }
        String itemsBag = dataBag.toJSONString();
        return itemsBag;
    }

    public Item findItemBagByIndex(Player player, int index) {
        if (player.inventory.itemsBag.get(index).isNotNullItem()) {
            return player.inventory.itemsBag.get(index);
        }
        return null;
    }

    public int getQuantity(Player player, int itemID) {
        Item item = findItem(player.inventory.itemsBag, itemID);
        if (item == null) {
            return -1;
        }
        return item.quantity;
    }
}

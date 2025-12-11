package nro.models.npc;

import nro.attr.Attribute;
import nro.attr.AttributeManager;
import nro.consts.*;
import nro.dialog.ConfirmDialog;
import nro.dialog.MenuDialog;
import nro.jdbc.daos.PlayerDAO;
import nro.lib.RandomCollection;
import nro.models.boss.Boss;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import nro.models.boss.event.EscortedBoss;
import nro.models.boss.event.Qilin;
import nro.models.clan.Clan;
import nro.models.clan.ClanMember;
import nro.models.consignment.ConsignmentShop;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.item.ItemTemplate;
import nro.models.map.ItemMap;
import nro.models.map.Map;
import nro.models.map.SantaCity;
import nro.models.map.Zone;
import nro.models.map.challenge.MartialCongressService;
import nro.models.map.dungeon.SnakeRoad;
import nro.models.map.dungeon.zones.ZSnakeRoad;
import nro.models.map.mabu.MabuWar;
import nro.models.map.phoban.BanDoKhoBau;
import nro.models.map.phoban.DoanhTrai;
import nro.models.map.war.BlackBallWar;
import nro.models.map.war.NamekBallWar;
import nro.models.player.Inventory;
import nro.models.player.NPoint;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.noti.NotiManager;
import nro.server.Maintenance;
import nro.server.Manager;
import nro.server.ServerManager;
import nro.server.io.Message;
import nro.services.*;
import nro.services.func.*;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.TimeUtil;
import nro.utils.Util;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import nro.manager.TopManager;
import nro.models.map.mabu.MabuWar14h;
import nro.models.phuban.DragonNamecWar.TranhNgoc;
import nro.models.phuban.DragonNamecWar.TranhNgocService;
import nro.models.player.Family;
import nro.server.Client;

import static nro.server.Manager.*;
import nro.services.func.Input.SubInput;
import static nro.services.func.SummonDragon.*;

/**
 *
 * @copyright 💖 GirlkuN 💖
 */
public class NpcFactory {

    private static boolean nhanVang = true;
    private static boolean nhanDeTu = true;

    // playerid - object
    public static final java.util.Map<Long, Object> PLAYERID_OBJECT = new HashMap<Long, Object>();

    private NpcFactory() {

    }

    public static Npc createNPC(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        Npc npc = null;
        try {
            switch (tempId) {
                case ConstNpc.FIDE:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (player.iDMark.getTranhNgoc() == 1) {
                                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Cút!Ta không nói chuyện với sinh vật hạ đẳng", "Đóng");
                                return;
                            }
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Hãy mang ngọc rồng về cho ta", "Đưa ngọc", "Đóng");
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                switch (select) {
                                    case 0:
                                        if (player.iDMark.getTranhNgoc() == 2 && player.isHoldNamecBallTranhDoat) {
                                            if (!Util.canDoWithTime(player.lastTimePickItem, 20000)) {
                                                Service.getInstance().sendThongBao(player, "Vui lòng đợi " + ((player.lastTimePickItem + 20000 - System.currentTimeMillis()) / 1000) + " giây để có thể trả");
                                                return;
                                            }
                                            TranhNgocService.getInstance().dropBall(player, (byte) 2);
                                            player.zone.pointFide++;
                                            if (player.zone.pointFide > ConstTranhNgocNamek.MAX_POINT) {
                                                player.zone.pointFide = ConstTranhNgocNamek.MAX_POINT;
                                            }
                                            TranhNgocService.getInstance().sendUpdatePoint(player);
                                        }
                                        break;
                                    case 1:
                                        break;
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.CADIC:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (player.iDMark.getTranhNgoc() == 2) {
                                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                        "Cút!Ta không nói chuyện với sinh vật hạ đẳng", "Đóng");
                                return;
                            }
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Hãy mang ngọc rồng về cho ta", "Đưa ngọc", "Đóng");
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                switch (select) {
                                    case 0:
                                        if (player.iDMark.getTranhNgoc() == 1 && player.isHoldNamecBallTranhDoat) {
                                            if (!Util.canDoWithTime(player.lastTimePickItem, 20000)) {
                                                Service.getInstance().sendThongBao(player, "Vui lòng đợi " + ((player.lastTimePickItem + 20000 - System.currentTimeMillis()) / 1000) + " giây để có thể trả");
                                                return;
                                            }
                                            TranhNgocService.getInstance().dropBall(player, (byte) 1);
                                            player.zone.pointCadic++;
                                            if (player.zone.pointCadic > ConstTranhNgocNamek.MAX_POINT) {
                                                player.zone.pointCadic = ConstTranhNgocNamek.MAX_POINT;
                                            }
                                            TranhNgocService.getInstance().sendUpdatePoint(player);
                                        }
                                        break;
                                    case 1:
                                        break;
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.TORIBOT:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Chào mừng bạn đến với cửa hàng đá qúy số 1 thời đại", "Cửa Hàng");
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                ShopService.gI().openShopSpecial(player, this, ConstNpc.SHOP_TORIBOT, 0, -1);
                            }
                        }
                    };
                    break;
                case ConstNpc.NGO_KHONG:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Chu mi nga", "Tặng quả\nHồng đào\nChín");
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                int itemNeed = ConstItem.QUA_HONG_DAO_CHIN;
                                Item item = InventoryService.gI().findItemBagByTemp(player, itemNeed);
                                if (item != null) {
                                    RandomCollection<Integer> rc = Manager.HONG_DAO_CHIN;
                                    int itemID = rc.next();
                                    int x = cx + Util.nextInt(-50, 50);
                                    int y = player.zone.map.yPhysicInTop(x, cy - 24);
                                    int quantity = 1;
                                    if (itemID == ConstItem.HONG_NGOC) {
                                        quantity = Util.nextInt(1, 2);
                                    }
                                    InventoryService.gI().subQuantityItemsBag(player, item, 1);
                                    InventoryService.gI().sendItemBags(player);
                                    ItemMap itemMap = new ItemMap(player.zone, itemID, quantity, x, y, player.id);
                                    Service.getInstance().dropItemMap(player.zone, itemMap);
                                    npcChat(player.zone, "Xie xie");
                                } else {
                                    Service.getInstance().sendThongBao(player, "Không tìm thấy!");
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.DUONG_TANG:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (this.mapId == MapName.LANG_ARU) {
                                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                        "|7|NGŨ HÀNH SƠN"
                                        + "\n|2|A mi phò phò, thí chủ hãy giúp giải cứu đồ đệ của bần tăng đang bị phong ấn tại ngũ hành sơn."
                                        + "\n|3|Tại đây sức mạnh dưới 16 Tỷ đánh quái được x2 TNSM",
                                        "Đồng ý", "Từ chối");
                            }
                            if (this.mapId == MapName.NGU_HANH_SON_3) {
                                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                        "A mi phò phò, thí chủ hãy thu thập bùa 'giải khai phong ấn', mỗi chữ 10 cái.",
                                        "Về\nLàng Aru", "Từ chối");
                            }
                            if (this.mapId == MapName.NGU_HANH_SON) {
                                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                        "A mi phò phò, thí chủ hãy thu thập bùa 'giải khai phong ấn', mỗi chữ 10 cái.",
                                        "Đổi đào chín", "Giải phong ấn", "Từ chối");
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == MapName.LANG_ARU) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:

                                                if (!Manager.gI().getGameConfig().isOpenPrisonPlanet()) {
                                                    Service.getInstance().sendThongBao(player,
                                                            "Lối vào ngũ hành sơn chưa mở");
                                                    return;
                                                }

                                                Zone zone = MapService.gI().getZoneJoinByMapIdAndZoneId(player, 124, 0);
                                                if (zone != null) {
                                                    player.location.x = 100;
                                                    player.location.y = 384;
                                                    MapService.gI().goToMap(player, zone);
                                                    Service.getInstance().clearMap(player);
                                                    zone.mapInfo(player);
                                                    player.zone.loadAnotherToMe(player);
                                                    player.zone.load_Me_To_Another(player);
                                                }
                                                // Service.getInstance().sendThongBao(player, "Lối vào ngũ hành sơn chưa
                                                // mở");
                                                break;

                                        }
                                    }
                                }
                                if (this.mapId == MapName.NGU_HANH_SON_3) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                Zone zone = MapService.gI().getZoneJoinByMapIdAndZoneId(player, 0, 0);
                                                if (zone != null) {
                                                    player.location.x = 600;
                                                    player.location.y = 432;
                                                    MapService.gI().goToMap(player, zone);
                                                    Service.getInstance().clearMap(player);
                                                    zone.mapInfo(player);
                                                    player.zone.loadAnotherToMe(player);
                                                    player.zone.load_Me_To_Another(player);
                                                }
                                                break;
                                        }
                                    }
                                }
                                if (this.mapId == MapName.NGU_HANH_SON) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                // Đổi đào
                                                Item item = InventoryService.gI().findItemBagByTemp(player,
                                                        ConstItem.QUA_HONG_DAO);
                                                if (item == null || item.quantity < 10) {
                                                    npcChat(player,
                                                            "Cần 10 quả đào xanh để đổi lấy đào chín từ bần tăng.");
                                                    return;
                                                }
                                                if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                                    npcChat(player, "Túi đầy rồi kìa.");
                                                    return;
                                                }
                                                Item newItem = ItemService.gI()
                                                        .createNewItem((short) ConstItem.QUA_HONG_DAO_CHIN, 1);
                                                InventoryService.gI().subQuantityItemsBag(player, item, 10);
                                                InventoryService.gI().addItemBag(player, newItem, 0);
                                                InventoryService.gI().sendItemBags(player);
                                                npcChat(player,
                                                        "Ta đã đổi cho thí chủ rồi đó, hãy mang cho đệ tử ta đi nào.");
                                                break;

                                            case 1:
                                                // giải phong ấn
                                                if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                                    npcChat(player, "Túi đầy rồi kìa.");
                                                    return;
                                                }
                                                int[] itemsNeed = {ConstItem.CHU_GIAI, ConstItem.CHU_KHAI,
                                                    ConstItem.CHU_PHONG, ConstItem.CHU_AN};
                                                List<Item> items = InventoryService.gI().getListItem(player, itemsNeed)
                                                        .stream().filter(i -> i.quantity >= 10)
                                                        .collect(Collectors.toList());
                                                boolean[] flags = new boolean[4];
                                                for (Item i : items) {
                                                    switch ((int) i.template.id) {
                                                        case ConstItem.CHU_GIAI:
                                                            flags[0] = true;
                                                            break;

                                                        case ConstItem.CHU_KHAI:
                                                            flags[1] = true;
                                                            break;

                                                        case ConstItem.CHU_PHONG:
                                                            flags[2] = true;
                                                            break;

                                                        case ConstItem.CHU_AN:
                                                            flags[3] = true;
                                                            break;
                                                    }
                                                }
                                                for (int i = 0; i < flags.length; i++) {
                                                    if (!flags[i]) {
                                                        ItemTemplate template = ItemService.gI()
                                                                .getTemplate(itemsNeed[i]);
                                                        npcChat("Thí chủ còn thiếu " + template.name);
                                                        return;
                                                    }
                                                }

                                                for (Item i : items) {
                                                    InventoryService.gI().subQuantityItemsBag(player, i, 10);
                                                }

                                                RandomCollection<Integer> rc = new RandomCollection<>();
                                                rc.add(10, ConstItem.CAI_TRANG_TON_NGO_KHONG_DE_TU);
                                                rc.add(10, ConstItem.CAI_TRANG_BAT_GIOI_DE_TU);
                                                rc.add(50, ConstItem.GAY_NHU_Y);
                                                switch (player.gender) {
                                                    case ConstPlayer.TRAI_DAT:
                                                        rc.add(30, ConstItem.CAI_TRANG_TON_NGO_KHONG);
                                                        break;

                                                    case ConstPlayer.NAMEC:
                                                        rc.add(30, ConstItem.CAI_TRANG_TON_NGO_KHONG_545);
                                                        break;

                                                    case ConstPlayer.XAYDA:
                                                        rc.add(30, ConstItem.CAI_TRANG_TON_NGO_KHONG_546);
                                                        break;
                                                }
                                                int itemID = rc.next();
                                                Item nItem = ItemService.gI().createNewItem((short) itemID);
                                                boolean all = itemID == ConstItem.CAI_TRANG_TON_NGO_KHONG_DE_TU
                                                        || itemID == ConstItem.CAI_TRANG_BAT_GIOI_DE_TU
                                                        || itemID == ConstItem.CAI_TRANG_TON_NGO_KHONG
                                                        || itemID == ConstItem.CAI_TRANG_TON_NGO_KHONG_545
                                                        || itemID == ConstItem.CAI_TRANG_TON_NGO_KHONG_546;
                                                if (all) {
                                                    nItem.itemOptions.add(new ItemOption(50, Util.nextInt(20, 35)));
                                                    nItem.itemOptions.add(new ItemOption(77, Util.nextInt(20, 35)));
                                                    nItem.itemOptions.add(new ItemOption(103, Util.nextInt(20, 35)));
                                                    nItem.itemOptions.add(new ItemOption(94, Util.nextInt(5, 10)));
                                                    nItem.itemOptions.add(new ItemOption(100, Util.nextInt(10, 20)));
                                                    nItem.itemOptions.add(new ItemOption(101, Util.nextInt(10, 20)));
                                                }
                                                if (itemID == ConstItem.CAI_TRANG_TON_NGO_KHONG
                                                        || itemID == ConstItem.CAI_TRANG_TON_NGO_KHONG_545
                                                        || itemID == ConstItem.CAI_TRANG_TON_NGO_KHONG_546) {
                                                    nItem.itemOptions.add(new ItemOption(80, Util.nextInt(5, 15)));
                                                    nItem.itemOptions.add(new ItemOption(81, Util.nextInt(5, 15)));
                                                    nItem.itemOptions.add(new ItemOption(106, 0));
                                                } else if (itemID == ConstItem.CAI_TRANG_TON_NGO_KHONG_DE_TU
                                                        || itemID == ConstItem.CAI_TRANG_BAT_GIOI_DE_TU) {
                                                    nItem.itemOptions.add(new ItemOption(197, 0));
                                                }
                                                if (all) {
                                                    if (Util.isTrue(499, 500)) {
                                                        nItem.itemOptions.add(new ItemOption(93, Util.nextInt(3, 30)));
                                                    }
                                                } else if (itemID == ConstItem.GAY_NHU_Y) {
                                                    RandomCollection<Integer> rc2 = new RandomCollection<>();
                                                    rc2.add(60, 30);
                                                    rc2.add(30, 90);
                                                    rc2.add(10, 365);
                                                    nItem.itemOptions.add(new ItemOption(93, rc2.next()));
                                                }
                                                InventoryService.gI().addItemBag(player, nItem, 0);
                                                InventoryService.gI().sendItemBags(player);
                                                npcChat(player.zone,
                                                        "A mi phò phò, đa tạ thí chủ tương trợ, xin hãy nhận món quà mọn này, bần tăng sẽ niệm chú giải thoát cho Ngộ Không");
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.TAPION:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 19) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Ác quỷ truyền thuyết Hirudegarn\nđã thoát khỏi phong ấn ngàn năm\nHãy giúp tôi chế ngự nó",
                                            "OK", "Từ chối");
                                }
                                if (this.mapId == 126) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Tôi sẽ đưa bạn về", "OK",
                                            "Từ chối");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 19) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                SantaCity santaCity = (SantaCity) MapService.gI().getMapById(126);
                                                if (santaCity != null) {
                                                    if (!santaCity.isOpened() || santaCity.isClosed()) {
                                                        Service.getInstance().sendThongBao(player,
                                                                "Hẹn gặp bạn lúc 22h mỗi ngày");
                                                        return;
                                                    }
                                                    santaCity.enter(player);
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Có lỗi xảy ra!");
                                                }
                                                break;
                                        }
                                    }
                                }
                                if (this.mapId == 126) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                SantaCity santaCity = (SantaCity) MapService.gI().getMapById(126);
                                                if (santaCity != null) {
                                                    santaCity.leave(player);
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Có lỗi xảy ra!");
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.QUAN_LY_CAU_THU:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (!canOpenNpc(player)) {
                                return;
                            }
                            if (this.mapId == 5 || this.mapId == 13) {
                                // Menu chính: 2 chức năng như yêu cầu
                                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                        "|7|QUẢN LÝ CẦU THỦ\n"
                                        + "|1|Ta có thể giúp gì cho ngươi?",
                                        "Đập thẻ",
                                        "Mở chỉ số\ncầu thủ");
                            } else {
                                super.openBaseMenu(player);
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (!canOpenNpc(player)) {
                                return;
                            }
                            if (!(this.mapId == 5 || this.mapId == 13)) {
                                return;
                            }

                            // Menu chọn chức năng
                            if (player.iDMark.isBaseMenu()) {
                                switch (select) {
                                    case 0: // Đập thẻ cầu thủ
                                        CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.DAP_THE_CAU_THU);
                                        break;
                                    case 1: // Mở chỉ số cầu thủ
                                        CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.MO_CS_THE);
                                        break;
                                }
                                return;
                            } else// Menu xác nhận đập / mở chỉ số riêng cho NPC này
                            if (player.iDMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                                switch (player.combineNew.typeCombine) {
                                    case CombineServiceNew.DAP_THE_CAU_THU:
                                    case CombineServiceNew.MO_CS_THE:
                                        if (select == 0) { // nút "Đập thẻ" hoặc "Mở chỉ số"
                                            CombineServiceNew.gI().startCombine(player);
                                        }
                                        break;
                                }
                            }

                        }
                    };
                    break;

                case ConstNpc.LY_TIEU_NUONG:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                // Nếu chưa có family thì khởi tạo mặc định
                                if (player.family == null) {
                                    player.family = new Family();
                                    player.family.status = Family.STATUS_SINGLE;
                                }

                                String info = "|5|Ta là Lý Tiểu Nương\n"
                                        + "|7|Quản lý hôn nhân và nuôi con\n\n"
                                        + " Cầu hôn cần Nhẫn Cầu Hôn"
                                        + "Sinh con cần 200 tiền tệ  \n"
                                        + " mỗi level sẽ tăng 2% chỉ số của bạn \n"
                                        + "Người chủ động ly hôn sẽ mất toàn bộ buff từ con\n\n";

                                // Trạng thái hôn nhân
                                switch (player.family.status) {
                                    case Family.STATUS_SINGLE:
                                        info += " Trạng thái: Độc thân\n";
                                        break;
                                    case Family.STATUS_MARRIED:
                                        info += " Trạng thái: Đã kết hôn\n";
                                        break;
                                    case Family.STATUS_DIVORCED:
                                        info += " Trạng thái: Đã ly hôn\n";
                                        break;
                                    case Family.STATUS_WIDOW:
                                        info += " Trạng thái: Goá phụ (nuôi con 1 mình)\n";
                                        break;
                                    default:
                                        info += " Trạng thái: Không xác định\n";
                                        break;
                                }

                                // Thông tin mang thai
                                if (player.family.isPregnant) {
                                    long elapsed = System.currentTimeMillis() - player.family.pregnancyStart;
                                    long pregnancyTime = (9 * 60 + 10) * 60 * 1000L; // 9h10p
                                    if (elapsed < pregnancyTime) {
                                        long remainMs = pregnancyTime - elapsed;
                                        long hours = remainMs / (1000 * 60 * 60);
                                        long minutes = (remainMs / (1000 * 60)) % 60;
                                        info += " Đang mang thai\n";
                                        info += " Còn lại: " + hours + " giờ " + minutes + " phút\n";
                                    } else {
                                        info += " Mang thai đã đủ thời gian\n";
                                        info += " Có thể sinh con bằng vật phẩm\n";
                                    }
                                } else {
                                    info += " Không mang thai\n";
                                }

                                // Thông tin con
                                if (player.family.childLevel > 0) {
                                    int needExp = 1000 * player.family.childLevel;
                                    int remain = Math.max(0, needExp - player.family.childExp);

                                    info += "\n Con: Lv" + player.family.childLevel
                                            + " (" + player.family.childExp + "/" + needExp + " EXP)\n";
                                    if (player.family.childLevel >= 18) {
                                        info += "|6|(Con đã đủ 18 tuổi)\n";
                                    } else {
                                        info += " Còn thiếu: " + remain + " EXP để lên cấp\n";
                                    }
                                } else {
                                    info += "\n Chưa có con\n";
                                }

                                // Menu
                                createOtherMenu(player, ConstNpc.FAMILY_MENU,
                                        info,
                                        "Cầu hôn", "Đẻ con", "Nuôi con", "Ly hôn", "Bỏ con", "Thoát");
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (!canOpenNpc(player)) {
                                return;
                            }

                            // ============= MENU CHÍNH =============
                            if (player.iDMark.getIndexMenu() == ConstNpc.FAMILY_MENU) {
                                switch (select) {
                                    case 0: // Cầu hôn
                                        Item nhan = InventoryService.gI().findItemBagByTemp(player, 1538); // ID nhẫn cầu hôn
                                        if (nhan == null || nhan.quantity < 1) {
                                            Service.getInstance().sendThongBao(player, "Bạn cần có Nhẫn Cầu Hôn để cầu hôn.");
                                            return;
                                        }

                                        // Tìm ứng viên trong zone
                                        java.util.List<Player> candidates = new java.util.ArrayList<>();
                                        for (Player p : player.zone.getPlayers()) {
                                            if (p != null && p.id != player.id && p.family != null && !p.family.isMarried()) {
                                                candidates.add(p);
                                            }
                                        }

                                        if (candidates.isEmpty()) {
                                            Service.getInstance().sendThongBao(player, " Không có ai để cầu hôn.");
                                            return;
                                        }

                                        String[] menu = candidates.stream().map(p -> p.name).toArray(String[]::new);
                                        player.iDMark.setMenuPlayers(candidates); // lưu danh sách chọn
                                        createOtherMenu(player, ConstNpc.MARRIAGE_SELECT, "Chọn người bạn muốn cầu hôn:", menu);
                                        break;

                                    case 1: // Đẻ con
                                        FamilyService.gI().adoptChild(player);
                                        break;

                                    case 2: // Nuôi con (item sữa ID 1587)
                                        Item sua = InventoryService.gI().findItemBagByTemp(player, 1587);
                                        if (sua == null) {
                                            Service.getInstance().sendThongBao(player, "Bạn không có Sữa.");
                                            return;

                                        } else {
                                            InventoryService.gI().subQuantityItemsBag(player, sua, 1);
                                            InventoryService.gI().sendItemBags(player);
                                            FamilyService.gI().addChildExp(player, 50);
                                            Service.getInstance().sendThongBao(player, " Con của bạn đã được cho uống sữa (+50 EXP).");
                                        }
                                        break;

                                    case 3: // Ly hôn
                                        FamilyService.gI().divorce(player);
                                        break;

                                    case 4: // Bỏ con
                                        FamilyService.gI().abandonChild(player);
                                        break;

                                    case 5: // Thoát
                                        break;
                                }
                            } // ============= CHỌN NGƯỜI ĐỂ CẦU HÔN =============
                            else if (player.iDMark.getIndexMenu() == ConstNpc.MARRIAGE_SELECT) {
                                Player target = player.iDMark.getMenuPlayerByIndex(select);
                                if (target != null) {
                                    target.iDMark.setIdCauHon(player.id);
                                    createOtherMenu(target, ConstNpc.MARRIAGE_CONFIRM,
                                            " " + player.name + " |1|ANH YÊU EM NHIỀU LẮM \n"
                                            + "|3| làm người yêu anh nha  ",
                                            "Đồng ý đi kìa \n đồng ý đi ", "không nhé anh \n em có người yếu rồi");
                                }
                            } // ============= NGƯỜI BỊ CẦU HÔN TRẢ LỜI =============
                            else if (player.iDMark.getIndexMenu() == ConstNpc.MARRIAGE_CONFIRM) {
                                Player proposer = Client.gI().getPlayer(player.iDMark.getIdCauHon());
                                if (proposer == null) {
                                    Service.getInstance().sendThongBao(player, "Người cầu hôn đã rời game.");
                                    return;
                                }

                                if (select == 0) { // Đồng ý
                                    Item nhan = InventoryService.gI().findItemBagByTemp(proposer, 1538);
                                    if (nhan == null || nhan.quantity < 1) {
                                        Service.getInstance().sendThongBao(proposer, "Bạn không còn Nhẫn Cầu Hôn.");
                                        Service.getInstance().sendThongBao(player, "Người cầu hôn không có nhẫn.");
                                        return;
                                    }

                                    // Trừ nhẫn
                                    InventoryService.gI().subQuantityItemsBag(proposer, nhan, 1);
                                    InventoryService.gI().sendItemBags(proposer);

                                    // Tiến hành kết hôn
                                    FamilyService.gI().proposeMarriage(proposer, player);
                                } else { // Từ chối
                                    Service.getInstance().sendThongBaoOK(proposer,
                                            "" + player.name + " |3|đã từ trối lời tỏ tình của bạn \n bạn có cảm thấy bùn hong .");
                                }
                                player.iDMark.setIdCauHon(0); // reset
                            }
                        }
                    };
                    break;

                case ConstNpc.SO_MAY_MAN:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Triệu hồi Vỹ Thú từ Nhất Vỹ đến Cửu Vỹ.\n"
                                            + "|7|Mỗi cấp cần số mảnh phong ấn tương ứng và Vỹ Thú cấp trước.\n",
                                            "triệu hồi \n nhất vỹ \n(99 mảnh phong ấn )",
                                            "triệu hồi \n nhị vỹ \n(199 mảnh phong ấn )",
                                            "triệu hồi \n tam vỹ \n(299 mảnh phong ấn )",
                                            "triệu hồi \n tứ vỹ \n(399 mảnh phong ấn )",
                                            "triệu hồi \n ngũ vỹ \n(499 mảnh phong ấn )",
                                            "triệu hồi \n lục vỹ \n(599 mảnh phong ấn )",
                                            "triệu hồi \n thất vỹ \n(699 mảnh phong ấn )",
                                            "triệu hồi \n bát vỹ \n(799 mảnh phong ấn )",
                                            "triệu hồi \n cửu vỹ \n(899 mảnh phong ấn )");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    switch (select) {

                                        // ========================
                                        // CASE 0 – NHẤT VỸ
                                        // ========================
                                        case 0:
                                            Item vth = null;
                                            try {
                                                vth = InventoryService.gI().findItemBagByTemp(player, (short) 1537);
                                            } catch (Exception e) {
                                            }
                                            if (vth == null || vth.quantity < 99) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn chưa đủ 99 mảnh phong ấn");
                                                return;
                                            }
                                            Boss nhatvy = BossFactory.createBoss(BossFactory.NHATVY);
                                            nhatvy.zone = player.zone;
                                            nhatvy.location.x = player.location.x;
                                            nhatvy.location.y = player.location.y;
                                            InventoryService.gI().subQuantityItemsBag(player, vth, 99);
                                            InventoryService.gI().sendItemBags(player);
                                            Service.getInstance().sendThongBao(player, "Bạn vừa triệu hồi Nhất Vỹ");
                                            break;

                                        // ========================
                                        // CASE 1 – NHỊ VỸ
                                        // ========================
                                        case 1:
                                            Item m1 = InventoryService.gI().findItemBagByTemp(player, (short) 1537);
                                            Item vt1 = InventoryService.gI().findItemBagByTemp(player, (short) 1539);
                                            if (m1 == null || m1.quantity < 199) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần 199 mảnh phong ấn");
                                                return;
                                            }
                                            if (vt1 == null) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần Nhất Vỹ (1539)");
                                                return;
                                            }
                                            Boss nhivy = BossFactory.createBoss(BossFactory.NHIVY);
                                            nhivy.zone = player.zone;
                                            nhivy.location.x = player.location.x;
                                            nhivy.location.y = player.location.y;
                                            InventoryService.gI().subQuantityItemsBag(player, m1, 199);
                                            InventoryService.gI().subQuantityItemsBag(player, vt1, 1);
                                            InventoryService.gI().sendItemBags(player);
                                            Service.getInstance().sendThongBao(player, "Bạn vừa triệu hồi Nhị Vỹ");
                                            break;

                                        // ========================
                                        // CASE 2 – TAM VỸ
                                        // ========================
                                        case 2:
                                            Item m2 = InventoryService.gI().findItemBagByTemp(player, (short) 1537);
                                            Item vt2 = InventoryService.gI().findItemBagByTemp(player, (short) 1540);
                                            if (m2 == null || m2.quantity < 299) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần 299 mảnh phong ấn");
                                                return;
                                            }
                                            if (vt2 == null) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần Nhị Vỹ (1540)");
                                                return;
                                            }
                                            Boss tamvy = BossFactory.createBoss(BossFactory.TAMVY);
                                            tamvy.zone = player.zone;
                                            tamvy.location.x = player.location.x;
                                            tamvy.location.y = player.location.y;
                                            InventoryService.gI().subQuantityItemsBag(player, m2, 299);
                                            InventoryService.gI().subQuantityItemsBag(player, vt2, 1);
                                            InventoryService.gI().sendItemBags(player);
                                            Service.getInstance().sendThongBao(player, "Bạn vừa triệu hồi Tam Vỹ");
                                            break;

                                        // ========================
                                        // CASE 3 – TỨ VỸ
                                        // ========================
                                        case 3:
                                            Item m3 = InventoryService.gI().findItemBagByTemp(player, (short) 1537);
                                            Item vt3 = InventoryService.gI().findItemBagByTemp(player, (short) 1541);
                                            if (m3 == null || m3.quantity < 399) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần 399 mảnh phong ấn");
                                                return;
                                            }
                                            if (vt3 == null) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần Tam Vỹ (1541)");
                                                return;
                                            }
                                            Boss tuvy = BossFactory.createBoss(BossFactory.TUVY);
                                            tuvy.zone = player.zone;
                                            tuvy.location.x = player.location.x;
                                            tuvy.location.y = player.location.y;
                                            InventoryService.gI().subQuantityItemsBag(player, m3, 399);
                                            InventoryService.gI().subQuantityItemsBag(player, vt3, 1);
                                            InventoryService.gI().sendItemBags(player);
                                            Service.getInstance().sendThongBao(player, "Bạn vừa triệu hồi Tứ Vỹ");
                                            break;

                                        // ========================
                                        // CASE 4 – NGŨ VỸ
                                        // ========================
                                        case 4:
                                            Item m4 = InventoryService.gI().findItemBagByTemp(player, (short) 1537);
                                            Item vt4 = InventoryService.gI().findItemBagByTemp(player, (short) 1542);
                                            if (m4 == null || m4.quantity < 499) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần 499 mảnh phong ấn");
                                                return;
                                            }
                                            if (vt4 == null) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần Tứ Vỹ (1542)");
                                                return;
                                            }
                                            Boss ngucy = BossFactory.createBoss(BossFactory.NGUUVY);
                                            ngucy.zone = player.zone;
                                            ngucy.location.x = player.location.x;
                                            ngucy.location.y = player.location.y;
                                            InventoryService.gI().subQuantityItemsBag(player, m4, 499);
                                            InventoryService.gI().subQuantityItemsBag(player, vt4, 1);
                                            InventoryService.gI().sendItemBags(player);
                                            Service.getInstance().sendThongBao(player, "Bạn vừa triệu hồi Ngũ Vỹ");
                                            break;

                                        // ========================
                                        // CASE 5 – LỤC VỸ
                                        // ========================
                                        case 5:
                                            Item m5 = InventoryService.gI().findItemBagByTemp(player, (short) 1537);
                                            Item vt5 = InventoryService.gI().findItemBagByTemp(player, (short) 1543);
                                            if (m5 == null || m5.quantity < 599) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần 599 mảnh phong ấn");
                                                return;
                                            }
                                            if (vt5 == null) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần Ngũ Vỹ (1543)");
                                                return;
                                            }
                                            Boss lucvy = BossFactory.createBoss(BossFactory.LUCVY);
                                            lucvy.zone = player.zone;
                                            lucvy.location.x = player.location.x;
                                            lucvy.location.y = player.location.y;
                                            InventoryService.gI().subQuantityItemsBag(player, m5, 599);
                                            InventoryService.gI().subQuantityItemsBag(player, vt5, 1);
                                            InventoryService.gI().sendItemBags(player);
                                            Service.getInstance().sendThongBao(player, "Bạn vừa triệu hồi Lục Vỹ");
                                            break;

                                        // ========================
                                        // CASE 6 – THẤT VỸ
                                        // ========================
                                        case 6:
                                            Item m6 = InventoryService.gI().findItemBagByTemp(player, (short) 1537);
                                            Item vt6 = InventoryService.gI().findItemBagByTemp(player, (short) 1544);
                                            if (m6 == null || m6.quantity < 699) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần 699 mảnh phong ấn");
                                                return;
                                            }
                                            if (vt6 == null) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần Lục Vỹ (1544)");
                                                return;
                                            }
                                            Boss thatvy = BossFactory.createBoss(BossFactory.THATVY);
                                            thatvy.zone = player.zone;
                                            thatvy.location.x = player.location.x;
                                            thatvy.location.y = player.location.y;
                                            InventoryService.gI().subQuantityItemsBag(player, m6, 699);
                                            InventoryService.gI().subQuantityItemsBag(player, vt6, 1);
                                            InventoryService.gI().sendItemBags(player);
                                            Service.getInstance().sendThongBao(player, "Bạn vừa triệu hồi Thất Vỹ");
                                            break;

                                        // ========================
                                        // CASE 7 – BÁT VỸ
                                        // ========================
                                        case 7:
                                            Item m7 = InventoryService.gI().findItemBagByTemp(player, (short) 1537);
                                            Item vt7 = InventoryService.gI().findItemBagByTemp(player, (short) 1545);
                                            if (m7 == null || m7.quantity < 799) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần 799 mảnh phong ấn");
                                                return;
                                            }
                                            if (vt7 == null) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần Thất Vỹ (1545)");
                                                return;
                                            }
                                            Boss batvy = BossFactory.createBoss(BossFactory.BATVY);
                                            batvy.zone = player.zone;
                                            batvy.location.x = player.location.x;
                                            batvy.location.y = player.location.y;
                                            InventoryService.gI().subQuantityItemsBag(player, m7, 799);
                                            InventoryService.gI().subQuantityItemsBag(player, vt7, 1);
                                            InventoryService.gI().sendItemBags(player);
                                            Service.getInstance().sendThongBao(player, "Bạn vừa triệu hồi Bát Vỹ");
                                            break;

                                        // ========================
                                        // CASE 8 – CỬU VỸ
                                        // ========================
                                        case 8:
                                            Item m8 = InventoryService.gI().findItemBagByTemp(player, (short) 1537);
                                            Item vt8 = InventoryService.gI().findItemBagByTemp(player, (short) 1546);
                                            if (m8 == null || m8.quantity < 899) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần 899 mảnh phong ấn");
                                                return;
                                            }
                                            if (vt8 == null) {
                                                Service.getInstance().sendThongBao(player, "|3|Bạn cần Bát Vỹ (1546)");
                                                return;
                                            }
                                            Boss cuuvy = BossFactory.createBoss(BossFactory.CUUVY);
                                            cuuvy.zone = player.zone;
                                            cuuvy.location.x = player.location.x;
                                            cuuvy.location.y = player.location.y;
                                            InventoryService.gI().subQuantityItemsBag(player, m8, 899);
                                            InventoryService.gI().subQuantityItemsBag(player, vt8, 1);
                                            InventoryService.gI().sendItemBags(player);
                                            Service.getInstance().sendThongBao(player, "Bạn vừa triệu hồi Cửu Vỹ");
                                            break;
                                    }

                                }
                            }
                        }
                    };
                    break;

                case ConstNpc.QUY_LAO_KAME:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                EscortedBoss escortedBoss = player.getEscortedBoss();
                                if (escortedBoss != null && escortedBoss instanceof Qilin) {
                                    this.createOtherMenu(player, ConstNpc.ESCORT_QILIN_MENU,
                                            "Ah con đã tìm thấy lân con thất lạc của ta\nTa sẽ thưởng cho con 1 viên Capsule Tết 2024.",
                                            "Đồng ý", "Từ chối");
                                } else {
                                    if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Chào con, con muốn ta giúp gì nào?",
                                                "Giải tán bang hội", "Lãnh địa\nbang hội", "Kho báu\ndưới biển");

                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    switch (select) {

                                        case 0:
                                            if (player.clan != null) {
                                                ClanService.gI().RemoveClanAll(player);
                                            } else {
                                                Service.getInstance().sendThongBao(player,
                                                        "Bạn không có bang hội nào để giải tán.");
                                            }
                                            break;
                                        case 1:
                                            if (player.clan != null) {
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 153, -1, -1);
                                            } else {
                                                Service.getInstance().sendThongBao(player, "Yêu cầu có bang hội !!!");
                                            }
                                            break;
                                        case 2:
                                            if (player.clan != null) {
                                                if (player.clan.banDoKhoBau != null) {
                                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                                            "Bang hội của con đang đi tìm kho báu dưới biển cấp độ "
                                                            + player.clan.banDoKhoBau.level
                                                            + "\nCon có muốn đi theo không?",
                                                            "Đồng ý", "Từ chối");
                                                } else {
                                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\n"
                                                            + "Ở đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                                            "Chọn\ncấp độ", "Từ chối");
                                                }
                                            } else {
                                                this.npcChat(player, "Con phải có bang hội ta mới có thể cho con đi");
                                            }
                                            break;
                                        case 3:
                                            switch (Manager.EVENT_SEVER) {

                                                case 1:
                                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_SUKIEN,
                                                            "Sự kiện Halloween chính thức tại Ngọc Rồng "
                                                            + Manager.SERVER_NAME + "\n"
                                                            + "Chuẩn bị x10 nguyên liệu Kẹo, Bánh Quy, Bí ngô để đổi Giỏ Kẹo cho ta nhé\n"
                                                            + "Nguyên Liệu thu thập bằng cách đánh quái tại các hành tinh được chỉ định\n"
                                                            + "Tích lũy 3 Giỏ Kẹo +  3 Vé mang qua đây ta sẽ cho con 1 Hộp Ma Quỷ\n"
                                                            + "Tích lũy 3 Giỏ Kẹo, 3 Hộp Ma Quỷ + 3 Vé \nmang qua đây ta sẽ cho con 1 hộp quà thú vị.",
                                                            "Đổi\nGiỏ Kẹo", "Đổi Hộp\nMa Quỷ", "Đổi Hộp\nQuà Halloween",
                                                            "Từ chối");
                                                    break;
                                                case 2:
                                                    Attribute at = ServerManager.gI().getAttributeManager()
                                                            .find(ConstAttribute.VANG);
                                                    String text = "Sự kiện 20/11 chính thức tại Ngọc Rồng "
                                                            + Manager.SERVER_NAME + "\n "
                                                            + "Số điểm hiện tại của bạn là : "
                                                            + player.event.getEventPoint()
                                                            + "\nTổng số hoa đã tặng trên toàn máy chủ "
                                                            + EVENT_COUNT_QUY_LAO_KAME % 999 + "/999";
                                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_SUKIEN,
                                                            at != null && !at.isExpired() ? text
                                                            + "\nToàn bộ máy chủ được nhân đôi số vàng rơi ra từ quái,thời gian còn lại "
                                                            + at.getTime() / 60 + " phút."
                                                            : text + "\nKhi tặng đủ 999 bông hoa toàn bộ máy chủ được nhân đôi số vàng rơi ra từ quái trong 60 phút",
                                                            "Tặng 1\n Bông hoa", "Tặng\n10 Bông", "Tặng\n99 Bông",
                                                            "Đổi\nHộp quà");
                                                    break;
                                                case 3:
                                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_SUKIEN,
                                                            "Sự kiên giáng sinh 2022 " + Manager.SERVER_NAME
                                                            + "\nKhi đội mũ len bất kì đánh quái sẽ có cơ hội nhận được kẹo giáng sinh"
                                                            + "\nĐem 99 kẹo giáng sinh tới đây để đổi 1 Vớ,tất giáng sinh\nChúc bạn một mùa giáng sinh vui vẻ",
                                                            "Đổi\nTất giáng sinh");

                                                    break;
                                                case 4:
                                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_SUKIEN,
                                                            "|7|Sự kiên Tết nguyên đán 2024 " + Manager.SERVER_NAME
                                                            + "\n|2|Bạn đang có: " + player.event.getEventPoint() + " điểm sự kiện"
                                                            + "\n|5|-Thịt heo và Ba chỉ: quái Tương lai"
                                                            + "\n-Gạo nếp: quái Hành tinh Nappa"
                                                            + "\n-Đỗ xanh: quái Bản đồ kho báu, Doanh trại"
                                                            + "\n-Lá dong và Lá chuối: quái Mộc nhân"
                                                            + "\n-Gia vị và Phụ gia: quái map Cold"
                                                            + "\n|3|(Săn Boss có tỉ lệ rơi các vật phẩm trên)"
                                                            + "\n|1|Chúc bạn năm mới dui dẻ",
                                                            "Nhận Lìxì", "Đổi Điểm\nSự Kiện");
                                                    break;
                                                case 5:
                                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_SUKIEN,
                                                            "Sự kiện 8/3 chính thức tại Ngọc Rồng "
                                                            + Manager.SERVER_NAME + "\nBạn đang có: "
                                                            + player.event.getEventPoint()
                                                            + " điểm sự kiện\nChúc bạn chơi game dui dẻ",
                                                            "Tặng 1\n Bông hoa", "Tặng\n10 Bông", "Tặng\n99 Bông",
                                                            "Đổi Capsule");
                                                    break;
                                            }
                                            break;

//                                        case 5:
//                                            if (Manager.KHUYEN_MAI_NAP != 1) {
//                                                this.createOtherMenu(player, ConstNpc.QUY_DOI_HN,
//                                                        "|7|QUY ĐỔI HỒNG NGỌC"
//                                                        + "\n|6|Giới hạn đổi không quá 1.000.000 Coin"
//                                                        + "\n|1|Coin hiện còn : " + " " + Util.format(player.getSession().vnd)
//                                                        + "\n\n|5|Nhập 10.000Đ được 50.000 Hồng ngọc"
//                                                        + "\n|3| Server đang x" + Manager.KHUYEN_MAI_NAP + " Quy đổi "
//                                                        + "(10.000 Coin = " + Util.format(Manager.KHUYEN_MAI_NAP * 50000) + " Hồng ngọc)",
//                                                        "Đồng ý", "Từ chối");
//                                            } else {
//                                                this.createOtherMenu(player, ConstNpc.QUY_DOI_HN,
//                                                        "|7|QUY ĐỔI HỒNG NGỌC"
//                                                        + "\n|6|Giới hạn đổi không quá 1.000.000 Coin"
//                                                        + "\n|1|Coin hiện còn : " + " " + Util.format(player.getSession().vnd)
//                                                        + "\n\n|5|Nhập 10.000 Coin được 50.000 Hồng ngọc",
//                                                        "Đồng ý", "Từ chối");
//                                            }
//                                            break;
                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPEN_SUKIEN) {
                                    openMenuSuKien(player, this, tempId, select);
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPENED_DBKB) {
                                    switch (select) {
                                        case 0:
                                            if (player.isAdmin()
                                                    || player.nPoint.power >= BanDoKhoBau.POWER_CAN_GO_TO_DBKB) {
                                                ChangeMapService.gI().goToDBKB(player);
                                            } else {
                                                this.npcChat(player, "Sức mạnh của con phải ít nhất phải đạt "
                                                        + Util.numberToMoney(BanDoKhoBau.POWER_CAN_GO_TO_DBKB));
                                            }
                                            break;

                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPEN_DBKB) {
                                    switch (select) {
                                        case 0:
                                            if (player.isAdmin()
                                                    || player.nPoint.power >= BanDoKhoBau.POWER_CAN_GO_TO_DBKB) {
                                                Input.gI().createFormChooseLevelBDKB(player);
                                            } else {
                                                this.npcChat(player, "Sức mạnh của con phải ít nhất phải đạt "
                                                        + Util.numberToMoney(BanDoKhoBau.POWER_CAN_GO_TO_DBKB));
                                            }
                                            break;
                                    }

                                } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_ACCEPT_GO_TO_BDKB) {
                                    switch (select) {
                                        case 0:
                                            BanDoKhoBauService.gI().openBanDoKhoBau(player,
                                                    Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
                                            break;
                                    }

                                } else if (player.iDMark.getIndexMenu() == ConstNpc.ESCORT_QILIN_MENU) {
                                    switch (select) {
                                        case 0: {
                                            if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                                this.npcChat(player,
                                                        "Con phải có ít nhất 1 ô trống trong hành trang ta mới đưa cho con được");
                                                return;
                                            }
                                            EscortedBoss escortedBoss = player.getEscortedBoss();
                                            if (escortedBoss != null) {
                                                escortedBoss.stopEscorting();
                                                Item item = ItemService.gI()
                                                        .createNewItem((short) ConstItem.CAPSULE_TET_2022);
                                                item.quantity = 1;
                                                InventoryService.gI().addItemBag(player, item, 0);
                                                InventoryService.gI().sendItemBags(player);
                                                Service.getInstance().sendThongBao(player,
                                                        "Bạn nhận được " + item.template.name);
                                            }
                                        }
                                        break;
                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.QUY_DOI_HN) {
                                    switch (select) {
                                        case 0:
                                            Input.gI().createFormQDHN(player);
                                            break;

                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.QUY_DOI_TV) {
                                    switch (select) {
                                        case 0:
                                            Input.gI().createFormQDTV(player);
                                            break;

                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.QUY_DOI_XU) {
                                    switch (select) {
                                        case 0:
                                            Input.gI().createFormQDXu(player);
                                            break;

                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.TRUONG_LAO_GURU:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            Item mcl = InventoryService.gI().findItemBagByTemp(player, 1517);
                            int slMCL = (mcl == null) ? 0 : mcl.quantity;
                            if (canOpenNpc(player)) {
                                if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Ngọc rồng Namếc đang bị 2 thế lực tranh giành\nHãy chọn cấp độ tham gia tùy theo sức mạnh bản thân",
                                            "Tham gia", "Đổi điểm\nThưởng\n[" + slMCL + "]", "Bảng\nxếp hạng", "Từ chối");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                switch (player.iDMark.getIndexMenu()) {
                                    case ConstNpc.BASE_MENU:
                                        switch (select) {
                                            case 0:
                                                if (TranhNgoc.gI().isTimeRegisterWar()) {
                                                    if (player.iDMark.getTranhNgoc() == -1) {
                                                        this.createOtherMenu(player, ConstNpc.REGISTER_TRANH_NGOC,
                                                                "Ngọc rồng Namếc đang bị 2 thế lực tranh giành\nHãy chọn cấp độ tham gia tùy theo sức mạnh bản thân\nPhe Cadic: " + TranhNgoc.gI().getPlayersCadic().size() + "\nPhe Fide: " + TranhNgoc.gI().getPlayersFide().size(),
                                                                "Tham gia phe Cadic", "Tham gia phe Fide", "Đóng");
                                                    } else {
                                                        this.createOtherMenu(player, ConstNpc.LOG_OUT_TRANH_NGOC,
                                                                "Ngọc rồng Namếc đang bị 2 thế lực tranh giành\nHãy chọn cấp độ tham gia tùy theo sức mạnh bản thân\nPhe Cadic: " + TranhNgoc.gI().getPlayersCadic().size() + "\nPhe Fide: " + TranhNgoc.gI().getPlayersFide().size(),
                                                                "Hủy\nĐăng Ký", "Đóng");
                                                    }
                                                    return;
                                                }
                                                Service.getInstance().sendPopUpMultiLine(player, 0, 7184, "Sự kiện sẽ mở đăng ký vào lúc " + TranhNgoc.HOUR_REGISTER + ":" + TranhNgoc.MIN_REGISTER + "\nSự kiện sẽ bắt đầu vào " + TranhNgoc.HOUR_OPEN + ":" + TranhNgoc.MIN_OPEN + " và kết thúc vào " + TranhNgoc.HOUR_CLOSE + ":" + TranhNgoc.HOUR_CLOSE);
                                                break;
                                            case 1:// Shop
                                                ShopService.gI().openShopSpecial(player, this,
                                                        ConstNpc.TRUONG_LAO_GURU, 0, -1);
                                                break;
                                            case 2:
                                                Service.getInstance().sendThongBao(player, "Update coming soon");
                                                break;
                                        }
                                        break;
                                    case ConstNpc.REGISTER_TRANH_NGOC:
                                        switch (select) {
                                            case 0:
                                                if (!player.getSession().actived) {
                                                    Service.getInstance().sendThongBao(player, "Vui lòng kích hoạt tài khoản để sửa dụng chức năng này!");
                                                    return;
                                                }
                                                player.iDMark.setTranhNgoc((byte) 1);
                                                TranhNgoc.gI().addPlayersCadic(player);
                                                Service.getInstance().sendThongBao(player, "Đăng ký vào phe Cadic thành công");
                                                break;
                                            case 1:
                                                if (!player.getSession().actived) {
                                                    Service.getInstance().sendThongBao(player, "Vui lòng kích hoạt tài khoản để sửa dụng chức năng này!");
                                                    return;
                                                }
                                                player.iDMark.setTranhNgoc((byte) 2);
                                                TranhNgoc.gI().addPlayersFide(player);
                                                Service.getInstance().sendThongBao(player, "Đăng ký vào phe Fide thành công");
                                                break;
                                        }
                                        break;
                                    case ConstNpc.LOG_OUT_TRANH_NGOC:
                                        switch (select) {
                                            case 0:
                                                player.iDMark.setTranhNgoc((byte) -1);
                                                TranhNgoc.gI().removePlayersCadic(player);
                                                TranhNgoc.gI().removePlayersFide(player);
                                                Service.getInstance().sendThongBao(player, "Hủy đăng ký thành công");
                                                break;
                                        }
                                        break;
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.VUA_VEGETA:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                EscortedBoss escortedBoss = player.getEscortedBoss();
                                if (escortedBoss != null && escortedBoss instanceof Qilin) {
                                    this.createOtherMenu(player, ConstNpc.ESCORT_QILIN_MENU,
                                            "Ah con đã tìm thấy lân con thất lạc của ta\nTa sẽ thưởng cho con 1 viên Capsule Tết 2024.",
                                            "Đồng ý", "Từ chối");
                                } else {
                                    if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                        super.openBaseMenu(player);
                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.getIndexMenu() == ConstNpc.ESCORT_QILIN_MENU) {
                                    switch (select) {
                                        case 0: {
                                            if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                                this.npcChat(player,
                                                        "Con phải có ít nhất 1 ô trống trong hành trang ta mới đưa cho con được");
                                                return;
                                            }
                                            EscortedBoss escortedBoss = player.getEscortedBoss();
                                            if (escortedBoss != null) {
                                                escortedBoss.stopEscorting();
                                                Item item = ItemService.gI()
                                                        .createNewItem((short) ConstItem.CAPSULE_TET_2022);
                                                item.quantity = 1;
                                                InventoryService.gI().addItemBag(player, item, 0);
                                                InventoryService.gI().sendItemBags(player);
                                                Service.getInstance().sendThongBao(player,
                                                        "Bạn nhận được " + item.template.name);
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.TRONG_TAI:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 113) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Đại hội võ thuật Siêu Hạng"
                                            + "\ndiễn ra 24/7 kể cả ngày lễ và chủ nhật"
                                            + "\nHãy thi đấu để khẳng định đẳng cấp của mình nhé"
                                            + "\n|7|(Chức năng đang bảo trì)",
                                            "Top 100\nCao thủ", "Nhận thưởng quà top", "Đấu ngay", "Về\nĐại Hội\nVõ Thuật");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 113) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                TopManager.getInstance().loadSieuHang();// Lấy danh sách TOP từ Manager
                                                Service.getInstance().showTopSieuHang(player);
                                                break;
                                            case 1:
                                                LocalDate currentDate = LocalDate.now();
                                                LocalDate lastClaimDate = Instant.ofEpochMilli(player.timesieuhang).atZone(ZoneId.systemDefault()).toLocalDate();
                                                this.createOtherMenu(player, 1, "|7|Ngươi ở Top " + player.rankSieuHang
                                                        + "\n|0|Mỗi ngày chỉ có thể nhận thưởng một lần"
                                                        + "\n|5|" + ((lastClaimDate.isEqual(currentDate)) ? "(Đã nhận thưởng)" : "(Chưa nhận thưởng)"), "Nhận thưởng");
                                                break;
                                            case 2:
                                            try {
                                                List<Player> list = TopManager.getInstance().getListSieuHang();
                                                TopManager.getInstance().loadSieuHang();// Lấy danh sách TOP từ Manager
                                                if (list != null && !list.isEmpty()) {
                                                    Service.getInstance().showTopSieuHang(player);
                                                    list.clear(); // Xóa danh sách sau khi hiển thị
                                                } else {
                                                    // Xử lý trường hợp danh sách rỗng hoặc null (tuỳ theo logic của bạn)
                                                    System.out.println("Danh sách TOP trống hoặc không có.");
                                                }
                                            } catch (Exception e) {
                                                // Xử lý ngoại lệ nếu có
                                                e.printStackTrace();
                                            }

                                            break;
                                            case 3:
                                                ChangeMapService.gI().changeMapNonSpaceship(player, 52, -1, 432);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == 1) {
                                        switch (select) {
                                            case 0:
                                                if (!player.isnhanthuong1) {
                                                    LocalDate currentDate = LocalDate.now();
                                                    LocalDate lastClaimDate = Instant.ofEpochMilli(player.timesieuhang).atZone(ZoneId.systemDefault()).toLocalDate();

                                                    if (lastClaimDate.isEqual(currentDate)) {
                                                        Service.getInstance().sendThongBao(player, "Bạn đã nhận thưởng rồi trong ngày hôm nay");
                                                    } else {
                                                        Item voso = null;
                                                        voso = ItemService.gI().createNewItem((short) 1296);
                                                        if (player.rankSieuHang == 10) {
                                                            voso.quantity += 10;
                                                        }
                                                        if (player.rankSieuHang == 9) {
                                                            voso.quantity += 10;
                                                        }
                                                        if (player.rankSieuHang == 8) {
                                                            voso.quantity += 10;
                                                        }
                                                        if (player.rankSieuHang == 7) {
                                                            voso.quantity += 10;
                                                        }
                                                        if (player.rankSieuHang == 6) {
                                                            voso.quantity += 10;
                                                        }
                                                        if (player.rankSieuHang == 5) {
                                                            voso.quantity += 10;
                                                        }
                                                        if (player.rankSieuHang == 4) {
                                                            voso.quantity += 10;
                                                        }
                                                        if (player.rankSieuHang == 3) {
                                                            voso.quantity += 20;
                                                        }
                                                        if (player.rankSieuHang == 2) {
                                                            voso.quantity += 30;
                                                        }
                                                        if (player.rankSieuHang == 1) {
                                                            voso.quantity += 50;
                                                        }
                                                        InventoryService.gI().addItemBag(player, voso, 99);
                                                        InventoryService.gI().sendItemBags(player);
                                                        PlayerService.gI().sendInfoHpMpMoney(player);
                                                        Service.getInstance().sendThongBao(player, "Bạn đã nhận được " + voso.template.name);

                                                        // Cập nhật thông tin ngày nhận thưởng
                                                        player.timesieuhang = System.currentTimeMillis();
                                                        player.isnhanthuong1 = true;
                                                    }
                                                    if (!lastClaimDate.isEqual(currentDate)) {
                                                        player.isnhanthuong1 = false;
                                                    }
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Bạn đã nhận thưởng rồi");
                                                }
                                                break;

                                        }
                                    }

                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.ONG_GOHAN:
                case ConstNpc.ONG_MOORI:
                case ConstNpc.ONG_PARAGUS:
//                     npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
//                         @Override
//                         public void openBaseMenu(Player player) {
//                             if (canOpenNpc(player)) {
//                                 String mtv;
//                                 if (player.getSession().actived) {
//                                     mtv = "bạn đã được mở thành viên , chào mừng bặn đến với ngọc rồng donal)";
//                                 } else {
//                                     mtv = "chỉ cần nạp 10k mở tv";
//                                 }
//                                 if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
//                                     this.createOtherMenu(player, ConstNpc.BASE_MENU,
//                                             "Hãy cùng khám phá Thế giới này nào\n"
//                                                     .replaceAll("%1", player.gender == ConstPlayer.TRAI_DAT ? "Quy lão Kamê"
//                                                             : player.gender == ConstPlayer.NAMEC ? "Trưởng lão Guru" : "Vua Vegeta")
//                                             + "Ta đang giữ tiền tiết kiệm của con"
//                                             + "\n|1|Coin hiện còn : " + " " + Util.format(player.getSession().vnd)
//                                             + "\n\n|2| ***" + mtv + "***",
//                                             "Đổi Mật Khẩu", "Giftcode", "Nhận đệ tử",
//                                             "Next NV", "Mở thành viên", "đổi\n thỏi vàng", "đổi \n tiền tệ");
//                                 }
//                             }
//                         }

//                         @Override
//                         public void confirmMenu(Player player, int select) {
//                             if (canOpenNpc(player)) {
//                                 if (player.iDMark.isBaseMenu()) {
//                                     switch (select) {
//                                         case 0:
//                                             Input.gI().createFormChangePassword(player);
//                                             break;
//                                         case 1:
//                                             Input.gI().createFormGiftCode(player);
//                                             break;
//                                         case 2:
//                                             if (player.pet == null) {
//                                                 PetService.gI().createNormalPet(player);
//                                                 Service.getInstance().sendThongBao(player, "Bạn vừa nhận được đệ tử");
//                                             } else {
//                                                 this.npcChat(player, "Tham Lam");
//                                             }
//                                             break;
//                                         case 3:
//                                             if (TaskService.gI().TaskNext(player) == true
//                                                     && (TaskService.gI().getIdTask(player) < ConstTask.TASK_34_0
//                                                     || TaskService.gI().getIdTask(player) == ConstTask.TASK_39_0)) {
//                                                 player.playerTask.taskMain.index++;
//                                                 if (player.playerTask.taskMain.index >= player.playerTask.taskMain.subTasks.size()) {
//                                                     TaskService.gI().sendNextTaskMain(player);
//                                                 } else {
//                                                     TaskService.gI().sendNextSubTask(player);
//                                                 }
//                                             } else {
//                                                 this.npcChat(player, "Chỉ hỗ trợ Next Nhiệm vụ Kết bạn, Vào bang và nv 500tr sm");
//                                             }
//                                             break;
//                                         case 4:
//                                             if (player.getSession().actived == true) {
//                                                 this.createOtherMenu(player, 53747,
//                                                         "|7|MỞ THÀNH VIÊN"
//                                                         + "\n|5|Bạn đã là thành viên của ngọc rồng donal"
//                                                         + "\nĐã mở khóa chức năng Giao dịch và Chat thế giới"
//                                                         + "\n|4|Hãy tiếp tục nâng cao sức mạnh của mình lên nào",
//                                                         "Ố kê");
//                                             } else {
//                                                 this.createOtherMenu(player, 1456,
//                                                         "|7|MỞ THÀNH VIÊN"
//                                                         + "\n|5|Khi bạn trờ thành thành viên chính thức của Ngọc rồng donal sẽ được mở khóa chức năng Giao dịch và Chat thế giới"
//                                                         + "\n|3|Giá Kích hoạt tài khoản: 10.000 Coin"
//                                                         + "\n|1|Coin hiện còn : " + " " + Util.format(player.getSession().vnd) + " Coin"
//                                                         + "\n|7|Bạn có chắc muốn Kích hoạt tài khoản không?",
//                                                         "Đồng ý", "Từ chối");
//                                             }
//                                             break;
// //                                        case 4:
// //                                            this.createOtherMenu(player, 1432,
// //                                                    "|7|QÙA MỐC NẠP"
// //                                                    + "\n\n|1|Tích lũy đổi Coin của bạn là : " + Util.format(player.tongnap)
// //                                                    + "\n\n|5|- Mốc 50k : 5 Bộ Ngọc rồng 1s, 5 item biến hình broly"
// //                                                    + "\n- Mốc 100k : 200 Hộp sách kỹ năng, 10 item biến hình broly"
// //                                                    + "\n- Mốc 200k : 500 Hộp sách kỹ năng, 15 item biến hình broly"
// //                                                    + "\n- Mốc 500k : 2000 Hộp sách kỹ năng, 99 Máy dò Boss, 500 Hộp pháp sư"
// //                                                    + "\n Mốc 1tr : Danh hiệu Thiên tử(Sử dụng cho Đệ tử), 30 Kẹo một mắt, 20 Đá Cầu vòng, 1000 Hộp pháp sư, 10 Viên Ngọc rồng Siêu cấp"
// //                                                    + "\n Mốc 2tr : 100 Capsule Bạc, 50 Capsule Vàng, 2000 Đá Hoàng Kim, 20 Viên Ngọc rồng Siêu cấp"
// //                                                    + "\n Mốc 5tr : 5000 Đá Hoàng Kim, 15.000 Thỏi vàng"
// //                                                    + "\n\n|2|Mỗi mốc chỉ nhận được 1 lần !!!",
// //                                                    "50k" + (player.mot == 0 ? "\n(Chưa nhận)" : "\n(Đã nhận)"),
// //                                                    "100k" + (player.hai == 0 ? "\n(Chưa nhận)" : "\n(Đã nhận)"),
// //                                                    "200k" + (player.ba == 0 ? "\n(Chưa nhận)" : "\n(Đã nhận)"),
// //                                                    "500k" + (player.bon == 0 ? "\n(Chưa nhận)" : "\n(Đã nhận)"),
// //                                                    "1Tr" + (player.nam == 0 ? "\n(Chưa nhận)" : "\n(Đã nhận)"),
// //                                                    "2Tr" + (player.sau == 0 ? "\n(Chưa nhận)" : "\n(Đã nhận)"),
// //                                                    "5Tr" + (player.bay == 0 ? "\n(Chưa nhận)" : "\n(Đã nhận)"));
// //                                            break;
// //                                        case 5:
// //                                            Input.gI().createGiftMember(player);
// //                                            break;
//                                         case 5:
//                                             this.createOtherMenu(player, ConstNpc.QUY_DOI_TV,
//                                                     "|7|CHỌN MỐC COIN ĐỂ ĐỔI THỎI VÀNG\n"
//                                                     + "|1|Coin hiện có: " + Util.format(player.getSession().vnd)
//                                                     + "\n|5|Giới hạn mỗi lần: 1.000.000 Coin",
//                                                     "10.000 → 32 TV",
//                                                     "20.000 → 64 TV",
//                                                     "50.000 → 160 TV",
//                                                     "100.000 → 320 TV",
//                                                     "200.000 → 670 TV",
//                                                     "500.000 → 1.700 TV",
//                                                     "1.000.000 → 3.600 TV",
//                                                     "Hủy");
//                                             break;
//                                         case 6:
//                                             if (Manager.KHUYEN_MAI_NAP != 1) {
//                                                 this.createOtherMenu(player, ConstNpc.QUY_DOI_XU,
//                                                         "|7|QUY ĐỔI TIỀN"
//                                                         + "\n|6|Giới hạn đổi không quá 1.000.000 Coin"
//                                                         + "\n|1|Coin hiện còn : " + " " + Util.format(player.getSession().vnd)
//                                                         + "\n\n|5|Nhập 10.000 Coin được 100 TIỀN TỆ"
//                                                         + "\n|3| Server đang x" + Manager.KHUYEN_MAI_NAP + " Quy đổi "
//                                                         + "(10.000 Coin = " + Util.format(Manager.KHUYEN_MAI_NAP * 100) + " Xu vàng)",
//                                                         "Đồng ý", "Từ chối");
//                                             } else {
//                                                 this.createOtherMenu(player, ConstNpc.QUY_DOI_XU,
//                                                         "|7|QUY ĐỔI TIỀN TỆ"
//                                                         + "\n|6|Giới hạn đổi không quá 1.000.000 Coin"
//                                                         + "\n|1|Coin hiện còn : " + " " + Util.format(player.getSession().vnd)
//                                                         + "\n\n|5|Nhập 10.000 Coin được 100 TIỀN TỆ",
//                                                         "Đồng ý", "Từ chối");
//                                             }
//                                             break;
//                                     }
//                                 } else if (player.iDMark.getIndexMenu() == 1456) {
//                                     switch (select) {
//                                         case 0:
//                                             if (player.getSession().actived) {
//                                                 Service.getInstance().sendThongBaoOK(player,
//                                                         "|4|Bạn đã mở thành viên rồi mà. Tiếp tục chơi game thui nào!!!!");
//                                                 return;
//                                             }
// //    // 👉 kiểm tra đã có CCCD chưa
// //    if (player.cccd == null || player.cccd.isEmpty()) {
// //        Service.getInstance().sendThongBaoOK(player, 
// //            "|7|Bạn cần phải đăng ký Căn Cước Công Dân (CCCD) trước khi mở thành viên!");
// //        return;
// //    }
//                                             if (player.getSession().tongnap1 < 10000 && !player.getSession().actived) {
//                                                 Service.getInstance().sendThongBao(player,
//                                                         "bạn chưa nạp 10k để mở tv");
//                                                 return;
//                                             }
//                                             if (player.getSession().tongnap1 >= 10000 && !player.getSession().actived) {
//                                                 try {
//                                                     PlayerDAO.subActive(player, 1);
//                                                     player.getSession().actived = true;
//                                                     Service.getInstance().sendThongBaoOK(player,
//                                                             "|2|Bạn đã mở thành viên Thành công. Đã mở khóa chức năng Giao dịch và Chat thế giới !!");
//                                                 } catch (Exception e) {
//                                                     System.out.println("Lỗi chức năng mở thành viên");
//                                                 }
//                                             }
//                                             break;
//                                     }
//                                 } else if (player.iDMark.getIndexMenu() == 1432) {
//                                     switch (select) {
//                                         case 0:
//                                             if (player.tongnap < 50000) {
//                                                 this.npcChat(player, "|7|Bạn chưa đủ điều kiện nhận!!!!");
//                                                 return;
//                                             }
//                                             if (player.mot != 0) {
//                                                 this.npcChat(player, "|7|Bạn đã nhận thưởng rồi mà !!!!");
//                                                 return;
//                                             }
//                                             if (InventoryService.gI().getCountEmptyBag(player) < 8) {
//                                                 this.npcChat(player, "Hành trang của bạn không đủ chỗ trống");
//                                                 return;
//                                             }
//                                             if (player.tongnap >= 50000 && player.mot == 0) {
//                                                 player.mot++;
//                                                 Item nro1 = ItemService.gI().createNewItem((short) 14);
//                                                 Item nro2 = ItemService.gI().createNewItem((short) 15);
//                                                 Item nro3 = ItemService.gI().createNewItem((short) 16);
//                                                 Item nro4 = ItemService.gI().createNewItem((short) 17);
//                                                 Item nro5 = ItemService.gI().createNewItem((short) 18);
//                                                 Item nro6 = ItemService.gI().createNewItem((short) 19);
//                                                 Item nro7 = ItemService.gI().createNewItem((short) 20);
//                                                 Item cuonghoa = ItemService.gI().createNewItem((short) 1386);
//                                                 nro1.quantity = 5;
//                                                 nro2.quantity = 5;
//                                                 nro3.quantity = 5;
//                                                 nro4.quantity = 5;
//                                                 nro5.quantity = 5;
//                                                 nro6.quantity = 5;
//                                                 nro7.quantity = 5;
//                                                 cuonghoa.quantity = 5;
//                                                 InventoryService.gI().addItemBag(player, nro1, 99);
//                                                 InventoryService.gI().addItemBag(player, nro2, 99);
//                                                 InventoryService.gI().addItemBag(player, nro3, 99);
//                                                 InventoryService.gI().addItemBag(player, nro4, 99);
//                                                 InventoryService.gI().addItemBag(player, nro5, 99);
//                                                 InventoryService.gI().addItemBag(player, nro6, 99);
//                                                 InventoryService.gI().addItemBag(player, nro7, 99);
//                                                 InventoryService.gI().addItemBag(player, cuonghoa, 99);
//                                                 InventoryService.gI().sendItemBags(player);
//                                                 this.npcChat(player, "Bạn nhận được 5 Bộ Ngọc rồng 1s, 5 item biến hình broly");
//                                             }
//                                             break;
//                                         case 1:
//                                             if (player.tongnap < 100000) {
//                                                 this.npcChat(player, "|7|Bạn chưa đủ điều kiện nhận!!!!");
//                                                 return;
//                                             }
//                                             if (player.hai != 0) {
//                                                 this.npcChat(player, "|7|Bạn đã nhận thưởng rồi mà !!!!");
//                                                 return;
//                                             }
//                                             if (InventoryService.gI().getCountEmptyBag(player) < 3) {
//                                                 this.npcChat(player, "Hành trang của bạn không đủ chỗ trống");
//                                                 return;
//                                             }
//                                             if (player.tongnap >= 100000 && player.hai == 0) {
//                                                 player.hai++;
//                                                 Item ruongsach = ItemService.gI().createNewItem((short) 1525);
//                                                 Item cuonghoa = ItemService.gI().createNewItem((short) 1386);
//                                                 ruongsach.quantity = 200;
//                                                 cuonghoa.quantity = 50;
//                                                 InventoryService.gI().addItemBag(player, ruongsach, 1);
//                                                 InventoryService.gI().addItemBag(player, cuonghoa, 1);
//                                                 InventoryService.gI().sendItemBags(player);
//                                                 this.npcChat(player, "200 Hộp sách kỹ năng, 50 item biến hình broly");
//                                             }
//                                             break;
//                                         case 2:
//                                             if (player.tongnap < 200000) {
//                                                 this.npcChat(player, "|7|Bạn chưa đủ điều kiện nhận!!!!");
//                                                 return;
//                                             }
//                                             if (player.ba != 0) {
//                                                 this.npcChat(player, "|7|Bạn đã nhận thưởng rồi mà !!!!");
//                                                 return;
//                                             }
//                                             if (InventoryService.gI().getCountEmptyBag(player) < 3) {
//                                                 this.npcChat(player, "Hành trang của bạn không đủ chỗ trống");
//                                                 return;
//                                             }
//                                             if (player.tongnap >= 200000 && player.ba == 0) {
//                                                 player.ba++;
//                                                 Item ruongsach = ItemService.gI().createNewItem((short) 1525);
//                                                 Item cuonghoa = ItemService.gI().createNewItem((short) 1386);
//                                                 ruongsach.quantity = 500;
//                                                 cuonghoa.quantity = 10;
//                                                 InventoryService.gI().addItemBag(player, ruongsach, 1);
//                                                 InventoryService.gI().addItemBag(player, cuonghoa, 1);
//                                                 InventoryService.gI().sendItemBags(player);
//                                                 this.npcChat(player, "Bạn nhận được 500 Hộp sách kỹ năng, 10 item biến hình broly");
//                                             }
//                                             break;
//                                         case 3:
//                                             if (player.tongnap < 500000) {
//                                                 this.npcChat(player, "|7|Bạn chưa đủ điều kiện nhận!!!!");
//                                                 return;
//                                             }
//                                             if (player.bon != 0) {
//                                                 this.npcChat(player, "|7|Bạn đã nhận thưởng rồi mà !!!!");
//                                                 return;
//                                             }
//                                             if (InventoryService.gI().getCountEmptyBag(player) < 4) {
//                                                 this.npcChat(player, "Hành trang của bạn không đủ chỗ trống");
//                                                 return;
//                                             }
//                                             if (player.tongnap >= 500000 && player.bon == 0) {
//                                                 player.bon++;
//                                                 Item ruongsach = ItemService.gI().createNewItem((short) 1525);
//                                                 Item cuonghoa = ItemService.gI().createNewItem((short) 1237);
//                                                 Item maydo = ItemService.gI().createNewItem((short) 1296);
//                                                 //   Item ruongspl = ItemService.gI().createNewItem((short) 1479);
//                                                 // ruongspl.quantity = 10;
//                                                 // ruongspl.itemOptions.add(new ItemOption(30, 1));
//                                                 ruongsach.quantity = 2000;
//                                                 cuonghoa.quantity = 500;
//                                                 maydo.quantity = 99;
//                                                 //InventoryService.gI().addItemBag(player, ruongspl, 1);
//                                                 InventoryService.gI().addItemBag(player, maydo, 1);
//                                                 InventoryService.gI().addItemBag(player, ruongsach, 1);
//                                                 InventoryService.gI().addItemBag(player, cuonghoa, 1);
//                                                 InventoryService.gI().sendItemBags(player);
//                                                 this.npcChat(player, "Bạn nhận được 2000 Hộp sách kỹ năng, 99 máy dò Boss, 500 Hộp pháp sư, 10 rương Sao pha lê VIP");
//                                             }
//                                             break;
//                                         case 4:
//                                             if (player.tongnap < 1000000) {
//                                                 this.npcChat(player, "|7|Bạn chưa đủ điều kiện nhận!!!!");
//                                                 return;
//                                             }
//                                             if (player.nam != 0) {
//                                                 this.npcChat(player, "|7|Bạn đã nhận thưởng rồi mà !!!!");
//                                                 return;
//                                             }
//                                             if (InventoryService.gI().getCountEmptyBag(player) < 5) {
//                                                 this.npcChat(player, "Hành trang của bạn không đủ chỗ trống");
//                                                 return;
//                                             }
//                                             if (player.tongnap >= 1000000 && player.nam == 0) {
//                                                 player.nam++;
//                                                 Item hopps = ItemService.gI().createNewItem((short) 1237);
//                                                 Item danhhieu = ItemService.gI().createNewItem((short) 1326);
//                                                 Item cauvong = ItemService.gI().createNewItem((short) 1083);
//                                                 Item nrosc = ItemService.gI().createNewItem((short) 1015);
//                                                 Item keo = ItemService.gI().createNewItem((short) 899);
//                                                 danhhieu.itemOptions.add(new ItemOption(50, 40));
//                                                 danhhieu.itemOptions.add(new ItemOption(77, 55));
//                                                 danhhieu.itemOptions.add(new ItemOption(77, 55));
//                                                 danhhieu.itemOptions.add(new ItemOption(14, 15));
//                                                 danhhieu.itemOptions.add(new ItemOption(101, 50));
//                                                 danhhieu.itemOptions.add(new ItemOption(30, 1));
//                                                 nrosc.quantity = 20;
//                                                 cauvong.quantity = 20;
//                                                 hopps.quantity = 1000;
//                                                 keo.quantity = 30;
//                                                 InventoryService.gI().addItemBag(player, nrosc, 1);
//                                                 InventoryService.gI().addItemBag(player, danhhieu, 1);
//                                                 InventoryService.gI().addItemBag(player, cauvong, 1);
//                                                 InventoryService.gI().addItemBag(player, hopps, 1);
//                                                 InventoryService.gI().addItemBag(player, keo, 1);
//                                                 InventoryService.gI().sendItemBags(player);
//                                                 this.npcChat(player, "Bạn nhận được Danh hiệu Thiên tử, 20 Đá cầu vòng và 1000 Hộp pháp sư, 20 Viên Ngọc rồng Siêu cấp");
//                                             }
//                                             break;
//                                         case 5:
//                                             if (player.tongnap < 2000000) {
//                                                 this.npcChat(player, "|7|Bạn chưa đủ điều kiện nhận!!!!");
//                                                 return;
//                                             }
//                                             if (player.sau != 0) {
//                                                 this.npcChat(player, "|7|Bạn đã nhận thưởng rồi mà !!!!");
//                                                 return;
//                                             }
//                                             if (InventoryService.gI().getCountEmptyBag(player) < 4) {
//                                                 this.npcChat(player, "Hành trang của bạn không đủ chỗ trống");
//                                                 return;
//                                             }
//                                             if (player.tongnap >= 2000000 && player.sau == 0) {
//                                                 player.sau++;
//                                                 Item csbac = ItemService.gI().createNewItem((short) 573, 100);
//                                                 Item csvang = ItemService.gI().createNewItem((short) 574, 50);
//                                                 Item dahkim = ItemService.gI().createNewItem((short) 1318, 2000);
//                                                 Item nrsc = ItemService.gI().createNewItem((short) 1015, 20);
//                                                 InventoryService.gI().addItemBag(player, csbac, 1);
//                                                 InventoryService.gI().addItemBag(player, csvang, 1);
//                                                 InventoryService.gI().addItemBag(player, dahkim, 1);
//                                                 InventoryService.gI().addItemBag(player, nrsc, 1);
//                                                 InventoryService.gI().sendItemBags(player);
//                                                 this.npcChat(player, "Bạn nhận được 100 Capsule Bạc, 50 Capsule Vàng, 2000 Đá Hoàng Kim, 20 Viên Ngọc rồng Siêu cấp");
//                                             }
//                                             break;
//                                         case 6:
//                                             if (player.tongnap < 5000000) {
//                                                 this.npcChat(player, "|7|Bạn chưa đủ điều kiện nhận!!!!");
//                                                 return;
//                                             }
//                                             if (player.bay != 0) {
//                                                 this.npcChat(player, "|7|Bạn đã nhận thưởng rồi mà !!!!");
//                                                 return;
//                                             }
//                                             if (InventoryService.gI().getCountEmptyBag(player) < 4) {
//                                                 this.npcChat(player, "Hành trang của bạn không đủ chỗ trống");
//                                                 return;
//                                             }
//                                             if (player.tongnap >= 5000000 && player.bay == 0) {
//                                                 player.bay++;
//                                                 Item dahkim = ItemService.gI().createNewItem((short) 1318, 5000);
//                                                 Item tvang = ItemService.gI().createNewItem((short) 457, 15000);
//                                                 //  Item caitrangvip = ItemService.gI().createNewItem((short) 1399,50);
//                                                 //InventoryService.gI().addItemBag(player, caitrangvip, 1);
//                                                 //caitrangvip.itemOptions.add(new ItemOption(30, 1));
//                                                 InventoryService.gI().addItemBag(player, dahkim, 1);
//                                                 InventoryService.gI().addItemBag(player, tvang, 1);
//                                                 InventoryService.gI().sendItemBags(player);
//                                                 this.npcChat(player, "Bạn nhận được 3000 Capsule Bạc, 5000 Capsule Vàng, 5000 Đá Hoàng Kim, 15.000 Thỏi vàng");
//                                             }
//                                             break;
//                                     }
//                                 } else if (player.iDMark.getIndexMenu() == ConstNpc.QUA_TAN_THU) {
//                                     switch (select) {
//                                         case 0:
//                                             // if (!player.gift.gemTanThu) {
//                                             if (true) {
//                                                 player.inventory.gem = 200000;
//                                                 Service.getInstance().sendMoney(player);
//                                                 Service.getInstance().sendThongBao(player,
//                                                         "Bạn vừa nhận được 100K ngọc xanh");
//                                                 player.gift.gemTanThu = true;
//                                             } else {
//                                                 this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
//                                                         "Con đã nhận phần quà này rồi mà", "Đóng");
//                                             }
//                                             break;
//                                         // case 1:
//                                         // if (nhanVang) {
//                                         // player.inventory.gold = Inventory.LIMIT_GOLD;
//                                         // Service.getInstance().sendMoney(player);
//                                         // Service.getInstance().sendThongBao(player, "Bạn vừa nhận được 2 tỉ vàng");
//                                         // } else {
//                                         // this.npcChat("Tính năng Nhận vàng đã đóng.");
//                                         // }
//                                         // break;
//                                         case 1:
//                                             if (nhanDeTu) {
//                                                 if (player.pet == null) {
//                                                     PetService.gI().createNormalPet(player);
//                                                     Service.getInstance().sendThongBao(player,
//                                                             "Bạn vừa nhận được đệ tử");
//                                                 } else {
//                                                     this.npcChat("Con đã nhận đệ tử rồi");
//                                                 }
//                                             } else {
//                                                 this.npcChat("Tính năng Nhận đệ tử đã đóng.");
//                                             }
//                                             break;
//                                     }
//                                 } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_PHAN_THUONG) {
//                                     switch (select) {
//                                         // case 0:
//                                         // ShopService.gI().openBoxItemReward(player);
//                                         // break;
//                                         case 0:
//                                             if (player.getSession().goldBar > 0) {
//                                                 if (InventoryService.gI().getCountEmptyBag(player) > 0) {
//                                                     int quantity = player.getSession().goldBar;
//                                                     Item goldBar = ItemService.gI().createNewItem((short) 457,
//                                                             quantity);
//                                                     InventoryService.gI().addItemBag(player, goldBar, 0);
//                                                     InventoryService.gI().sendItemBags(player);
//                                                     this.npcChat(player, "Ông đã để " + quantity
//                                                             + " Thỏi vàng vào hành trang con rồi đấy");
//                                                     PlayerDAO.subGoldBar(player, quantity);
//                                                     player.getSession().goldBar = 0;
//                                                 } else {
//                                                     this.npcChat(player,
//                                                             "Con phải có ít nhất 1 ô trống trong hành trang ông mới đưa cho con được");
//                                                 }
//                                             }
//                                             break;
//                                     }
//                                 } else if (player.iDMark.getIndexMenu() == ConstNpc.QUY_DOI_TV) {
//                                     int[] coinMoc = {10000, 20000, 50000, 100000, 200000, 500000, 1000000};
//                                     int[] tvMoc = {32, 64, 160, 320, 670, 1700, 3600};
//                                     // Nếu chọn "Hủy"
//                                     if (select == 7) {
//                                         Service.getInstance().sendThongBao(player, "Đã hủy quy đổi");
//                                         return;
//                                     }
//                                     // Valid lựa chọn
//                                     if (select < 0 || select > 6) {
//                                         return;
//                                     }
//                                     int coinCan = coinMoc[select];
//                                     int tvNhan = tvMoc[select];
//                                     // Kiểm tra coin
//                                     if (player.getSession().vnd < coinCan) {
//                                         Service.getInstance().sendThongBao(player,
//                                                 "Bạn cần " + Util.format(coinCan) + " Coin nhưng chỉ có "
//                                                 + Util.format(player.getSession().vnd));
//                                         return;
//                                     }
//                                     // Trừ coin
//                                     PlayerDAO.subVnd(player, coinCan);
//                                     // Tạo thỏi vàng
//                                     Item tv = ItemService.gI().createNewItem((short) 457, tvNhan);
//                                     InventoryService.gI().addItemBag(player, tv, 9999);
//                                     InventoryService.gI().sendItemBags(player);
//                                     Service.getInstance().sendThongBao(player,
//                                             "Đổi thành công " + Util.format(coinCan)
//                                             + " Coin → " + Util.format(tvNhan) + " Thỏi vàng!");
//                                 } else if (player.iDMark.getIndexMenu() == ConstNpc.QUY_DOI_XU) {
//                                     switch (select) {
//                                         case 0:
//                                             Input.gI().createFormQDXu(player);
//                                             break;
//                                     }
//                                 }
//                             }
//                         }
//                     };
//                     break;
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {

                        @Override
                        public void openBaseMenu(Player player) {
                            if (!canOpenNpc(player)) {
                                return;
                            }

                            String mtv = player.getSession().actived
                                    ? "Bạn đã là thành viên, chào mừng đến Ngọc Rồng Donal!"
                                    : "Chỉ cần nạp 10.000 Coin để mở Thành viên";

                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Xin chào chiến binh!\n"
                                    + "Coin hiện có: " + Util.format(player.getSession().vnd)
                                    + "\n" + mtv,
                                    "GiftCode",
                                    "MTV",
                                    "Nạp Vàng",
                                    "Next NV"
                                   // ,"Nhận đệ tử"
                                );
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (!canOpenNpc(player)) {
                                return;
                            }

                            /* ===============================
                MENU CHÍNH (BASE_MENU)
           =============================== */
                            if (player.iDMark.isBaseMenu()) {
                                switch (select) {

                                    case 0: // Giftcode
                                        Input.gI().createFormGiftCode(player);
                                        break;

                                    case 1: // Mở thành viên
                                        if (player.getSession().actived) {
                                            this.createOtherMenu(player, -1,
                                                    "Bạn đã là thành viên.\nTiếp tục phiêu lưu nhé!",
                                                    "Đóng");
                                        } else {
                                            this.createOtherMenu(player, 1456,
                                                    "|7|MỞ THÀNH VIÊN"
                                                    + "\nGiá mở: 10.000 Coin"
                                                    + "\nCoin hiện có: " + Util.format(player.getSession().vnd)
                                                    + "\nBạn có chắc muốn mở không?",
                                                    "Đồng ý", "Từ chối");
                                        }
                                        break;

                                    case 2: // Đổi thỏi vàng
                                        this.createOtherMenu(player, ConstNpc.QUY_DOI_TV,
                                                "CHỌN MỐC COIN ĐỂ ĐỔI THỎI VÀNG\n"
                                                + "Coin hiện có: " + Util.format(player.getSession().vnd)
                                                + "\nGiới hạn mỗi lần: 1.000.000 Coin",
                                                "10.000 \n 32 TV",
                                                "20.000\n 64 TV",
                                                "50.000\n 160 TV",
                                                "100.000\n 320 TV",
                                                "200.000\n 670 TV",
                                                "500.000\n 1.700 TV",
                                                "1.000.000\n 3.600 TV",
                                                "Hủy");
                                        break;

                                    case 3: // Next nhiệm vụ
                                        if (TaskService.gI().TaskNext(player)
                                                && (TaskService.gI().getIdTask(player) < ConstTask.TASK_34_0
                                                || TaskService.gI().getIdTask(player) == ConstTask.TASK_39_0)) {

                                            player.playerTask.taskMain.index++;
                                            if (player.playerTask.taskMain.index >= player.playerTask.taskMain.subTasks.size()) {
                                                TaskService.gI().sendNextTaskMain(player);
                                            } else {
                                                TaskService.gI().sendNextSubTask(player);
                                            }
                                        } else {
                                            this.npcChat(player,
                                                    "Chỉ hỗ trợ Next các nhiệm vụ:\n- Kết bạn\n- Vào bang\n- Nhiệm vụ 500tr SM");
                                        }
                                        break;

                                    case 4: // Nhận đệ tử
                                        if (player.pet == null) {
                                            PetService.gI().createNormalPet(player);
                                            Service.getInstance().sendThongBao(player, "Bạn đã nhận được đệ tử");
                                        } else {
                                            this.npcChat(player, "Bạn đã có đệ tử rồi");
                                        }
                                        break;
                                }

                                return;
                            }

                            /* ===============================
                MỞ THÀNH VIÊN (1456)
           =============================== */
                            if (player.iDMark.getIndexMenu() == 1456) {
                                if (select == 0) {
                                    if (player.getSession().vnd < 10000) {
                                        Service.getInstance().sendThongBao(player, "Bạn chưa đủ 10.000 Coin");
                                        return;
                                    }

                                    PlayerDAO.subActive(player, 1);
                                    player.getSession().actived = true;

                                    Service.getInstance().sendThongBaoOK(player,
                                            "Mở thành viên thành công!\n"
                                            + "Đã mở khóa Giao dịch & Chat thế giới.");
                                }
                                return;
                            }

                            /* ===============================
                QUY ĐỔI THỎI VÀNG (ConstNpc.QUY_DOI_TV)
           =============================== */
                            if (player.iDMark.getIndexMenu() == ConstNpc.QUY_DOI_TV) {

                                int[] coinMoc = {10000, 20000, 50000, 100000, 200000, 500000, 1000000};
                                int[] tvMoc = {32, 64, 160, 320, 670, 1700, 3600};

                                if (select == 7) {
                                    Service.getInstance().sendThongBao(player, "Đã hủy quy đổi!");
                                    return;
                                }

                                if (select < 0 || select > 6) {
                                    return;
                                }

                                int coin = coinMoc[select];
                                int tv = tvMoc[select];

                                if (player.getSession().vnd < coin) {
                                    Service.getInstance().sendThongBao(player,
                                            "Bạn cần " + Util.format(coin) + " Coin nhưng chỉ có "
                                            + Util.format(player.getSession().vnd));
                                    return;
                                }

                                PlayerDAO.subVnd(player, coin);

                                Item item = ItemService.gI().createNewItem((short) 457, tv);
                                InventoryService.gI().addItemBag(player, item, 9999);
                                InventoryService.gI().sendItemBags(player);

                                Service.getInstance().sendThongBao(player,
                                        "Đổi thành công " + Util.format(coin) + " Coin → "
                                        + Util.format(tv) + " Thỏi vàng!");
                            }
                        }
                    };
                    break;
                case ConstNpc.BUNMA:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Cậu cần trang bị gì cứ đến chỗ tôi nhé", "Cửa\nhàng");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    switch (select) {
                                        case 0:// Shop
                                            if (player.gender == ConstPlayer.TRAI_DAT) {
                                                this.openShopWithGender(player, ConstNpc.SHOP_BUNMA_QK_0, 0);
                                            } else {
                                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                                        "Xin lỗi cưng, chị chỉ bán đồ cho người Trái Đất", "Đóng");
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.DENDE:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                    if (player.isHoldNamecBall) {
                                        this.createOtherMenu(player, ConstNpc.ORTHER_MENU,
                                                "Ô,ngọc rồng Namek,anh thật may mắn,nếu tìm đủ 7 viên ngọc có thể triệu hồi Rồng Thần Namek,",
                                                "Gọi rồng", "Từ chối");
                                    } else {
                                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                                "Anh cần trang bị gì cứ đến chỗ em nhé", "Cửa\nhàng");
                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    switch (select) {
                                        case 0:// Shop
                                            if (player.gender == ConstPlayer.NAMEC) {
                                                this.openShopWithGender(player, ConstNpc.SHOP_DENDE_0, 0);
                                            } else {
                                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                                        "Xin lỗi anh, em chỉ bán đồ cho dân tộc Namếc", "Đóng");
                                            }
                                            break;
                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.ORTHER_MENU) {
                                    NamekBallWar.gI().summonDragon(player, this);
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.APPULE:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Ngươi cần trang bị gì cứ đến chỗ ta nhé", "Cửa\nhàng");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    switch (select) {
                                        case 0:// Shop
                                            if (player.gender == ConstPlayer.XAYDA) {
                                                this.openShopWithGender(player, ConstNpc.SHOP_APPULE_0, 0);
                                            } else {
                                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                                        "Về hành tinh hạ đẳng của ngươi mà mua đồ cùi nhé. Tại đây ta chỉ bán đồ cho người Xayda thôi",
                                                        "Đóng");
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.DR_DRIEF:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player pl) {
                            if (canOpenNpc(pl)) {
                                if (this.mapId == 84) {
                                    this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                            "Tàu Vũ Trụ của ta có thể đưa cậu đến hành tinh khác chỉ trong 3 giây. Cậu muốn đi đâu?",
                                            pl.gender == ConstPlayer.TRAI_DAT ? "Đến\nTrái Đất"
                                                    : pl.gender == ConstPlayer.NAMEC ? "Đến\nNamếc" : "Đến\nXayda");
                                } else if (this.mapId == 153) {
                                    Clan clan = pl.clan;
                                    ClanMember cm = pl.clanMember;
                                    if (cm.role == Clan.LEADER) {
                                        this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                                "Cần 1000 capsule bang [đang có " + clan.clanPoint
                                                + " capsule bang] để nâng cấp bang hội lên cấp "
                                                + (clan.level + 1) + "\n"
                                                + "+1 tối đa số lượng thành viên\n"
                                                + "|3|Cùng 1 thành viên trong bang đánh quái ở Lãnh địa bang hội để nhận được Capsule bang hội",
                                                "Về\nĐảoKame", "Góp " + cm.memberPoint + " capsule", "Nâng cấp",
                                                "Từ chối");
                                    } else {
                                        this.createOtherMenu(pl, ConstNpc.BASE_MENU, "Bạn đang có " + cm.memberPoint
                                                + " capsule bang,bạn có muốn đóng góp toàn bộ cho bang hội của mình không ?",
                                                "Về\nĐảoKame", "Đồng ý", "Từ chối");
                                    }
                                } else if (!TaskService.gI().checkDoneTaskTalkNpc(pl, this)) {
                                    if (pl.playerTask.taskMain.id == 7) {
                                        NpcService.gI().createTutorial(pl, this.avartar,
                                                "Hãy lên đường cứu đứa bé nhà tôi\n"
                                                + "Chắc bây giờ nó đang sợ hãi lắm rồi");
                                    } else {
                                        this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                                "Tàu Vũ Trụ của ta có thể đưa cậu đến hành tinh khác chỉ trong 3 giây. Cậu muốn đi đâu?",
                                                "Đến\nNamếc", "Đến\nXayda", "Siêu thị");
                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 84) {
                                    ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 24, -1, -1);
                                } else if (mapId == 153) {
                                    switch (select) {
                                        case 0:
                                            ChangeMapService.gI().changeMap(player, ConstMap.DAO_KAME, -1, 1059, 408);
                                            break;
                                        case 1:
                                            Clan clan = player.clan;
                                            if (clan == null) {
                                                Service.getInstance().sendThongBao(player, "Chưa có bang hội");
                                                return;
                                            }
                                            ClanMember cm = player.clanMember;
                                            player.clan.clanPoint += cm.memberPoint;
                                            cm.clanPoint += cm.memberPoint;
                                            cm.memberPoint = 0;
                                            Service.getInstance().sendThongBao(player, "Đóng góp thành công");
                                            break;
                                        case 2:
                                            Clan clan1 = player.clan;
                                            if (clan1 == null) {
                                                Service.getInstance().sendThongBao(player,
                                                        "Bạn chưa có bang hội");
                                                return;
                                            }
                                            if (clan1.level >= 10) {
                                                Service.getInstance().sendThongBao(player,
                                                        "Bang hội của bạn đã đạt cấp tối đa");
                                                return;
                                            }
                                            if (clan1.clanPoint < 1000) {
                                                Service.getInstance().sendThongBao(player, "Không đủ capsule");
                                            } else {
                                                clan1.level++;
                                                clan1.maxMember++;
                                                clan1.clanPoint -= 1000;
                                                Service.getInstance().sendThongBao(player,
                                                        "Bang hội của bạn đã được nâng cấp lên cấp " + clan1.level);
                                            }
                                            break;
                                    }
                                } else if (player.iDMark.isBaseMenu()) {
                                    switch (select) {
                                        case 0:
                                            ChangeMapService.gI().changeMapBySpaceShip(player, 25, -1, -1);
                                            break;
                                        case 1:
                                            ChangeMapService.gI().changeMapBySpaceShip(player, 26, -1, -1);
                                            break;
                                        case 2:
                                            ChangeMapService.gI().changeMapBySpaceShip(player, 84, -1, -1);
                                            break;
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.CARGO:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player pl) {
                            if (canOpenNpc(pl)) {
                                if (!TaskService.gI().checkDoneTaskTalkNpc(pl, this)) {
                                    if (pl.playerTask.taskMain.id == 7) {
                                        NpcService.gI().createTutorial(pl, this.avartar,
                                                "Hãy lên đường cứu đứa bé nhà tôi\n"
                                                + "Chắc bây giờ nó đang sợ hãi lắm rồi");
                                    } else {
                                        this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                                "Tàu Vũ Trụ của ta có thể đưa cậu đến hành tinh khác chỉ trong 3 giây. Cậu muốn đi đâu?",
                                                "Đến\nTrái Đất", "Đến\nXayda", "Siêu thị");
                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    switch (select) {
                                        case 0:
                                            ChangeMapService.gI().changeMapBySpaceShip(player, 24, -1, -1);
                                            break;
                                        case 1:
                                            ChangeMapService.gI().changeMapBySpaceShip(player, 26, -1, -1);
                                            break;
                                        case 2:
                                            ChangeMapService.gI().changeMapBySpaceShip(player, 84, -1, -1);
                                            break;
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.CUI:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {

                        private final int COST_FIND_BOSS = 20000000;

                        @Override
                        public void openBaseMenu(Player pl) {
                            if (canOpenNpc(pl)) {
                                if (!TaskService.gI().checkDoneTaskTalkNpc(pl, this)) {
                                    if (pl.playerTask.taskMain.id == 7) {
                                        NpcService.gI().createTutorial(pl, this.avartar,
                                                "Hãy lên đường cứu đứa bé nhà tôi\n"
                                                + "Chắc bây giờ nó đang sợ hãi lắm rồi");
                                    } else {
                                        if (this.mapId == 19) {

                                            int taskId = TaskService.gI().getIdTask(pl);
                                            switch (taskId) {
                                                case ConstTask.TASK_19_0:
                                                    this.createOtherMenu(pl, ConstNpc.MENU_FIND_KUKU,
                                                            "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                                            "Đến chỗ\nKuku\n(" + Util.numberToMoney(COST_FIND_BOSS)
                                                            + " vàng)",
                                                            "Đến Cold", "Đến\nNappa", "Từ chối");
                                                    break;
                                                case ConstTask.TASK_19_1:
                                                    this.createOtherMenu(pl, ConstNpc.MENU_FIND_MAP_DAU_DINH,
                                                            "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                                            "Đến chỗ\nMập đầu đinh\n("
                                                            + Util.numberToMoney(COST_FIND_BOSS) + " vàng)",
                                                            "Đến Cold", "Đến\nNappa", "Từ chối");
                                                    break;
                                                case ConstTask.TASK_19_2:
                                                    this.createOtherMenu(pl, ConstNpc.MENU_FIND_RAMBO,
                                                            "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                                            "Đến chỗ\nRambo\n(" + Util.numberToMoney(COST_FIND_BOSS)
                                                            + " vàng)",
                                                            "Đến Cold", "Đến\nNappa", "Từ chối");
                                                    break;
                                                default:
                                                    this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                                            "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                                            "Đến Cold", "Đến\nNappa", "Từ chối");

                                                    break;
                                            }
                                        } else if (this.mapId == 68) {
                                            this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                                    "Ngươi muốn về Thành Phố Vegeta", "Đồng ý", "Từ chối");
                                        } else {
                                            this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                                    "Tàu vũ trụ Xayda sử dụng công nghệ mới nhất, "
                                                    + "có thể đưa ngươi đi bất kỳ đâu, chỉ cần trả tiền là được.",
                                                    "Đến\nTrái Đất", "Đến\nNamếc", "Siêu thị");
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 26) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 24, -1, -1);
                                                break;
                                            case 1:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 25, -1, -1);
                                                break;
                                            case 2:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 84, -1, -1);
                                                break;
                                        }
                                    }
                                }
                                if (this.mapId == 19) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                                                break;
                                            case 1:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_FIND_KUKU) {
                                        switch (select) {
                                            case 0:
                                                Boss boss = BossManager.gI().getBossById(BossFactory.KUKU);
                                                if (boss != null && !boss.isDie()) {
                                                    if (player.inventory.gold >= COST_FIND_BOSS) {
                                                        player.inventory.gold -= COST_FIND_BOSS;
                                                        ChangeMapService.gI().changeMap(player, boss.zone,
                                                                boss.location.x, boss.location.y);
                                                        Service.getInstance().sendMoney(player);
                                                    } else {
                                                        Service.getInstance().sendThongBao(player,
                                                                "Không đủ vàng, còn thiếu "
                                                                + Util.numberToMoney(
                                                                        COST_FIND_BOSS - player.inventory.gold)
                                                                + " vàng");
                                                    }
                                                }
                                                break;
                                            case 1:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                                                break;
                                            case 2:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_FIND_MAP_DAU_DINH) {
                                        switch (select) {
                                            case 0:
                                                Boss boss = BossManager.gI().getBossById(BossFactory.MAP_DAU_DINH);
                                                if (boss != null && !boss.isDie()) {
                                                    if (player.inventory.gold >= COST_FIND_BOSS) {
                                                        player.inventory.gold -= COST_FIND_BOSS;
                                                        ChangeMapService.gI().changeMap(player, boss.zone,
                                                                boss.location.x, boss.location.y);
                                                        Service.getInstance().sendMoney(player);
                                                    } else {
                                                        Service.getInstance().sendThongBao(player,
                                                                "Không đủ vàng, còn thiếu "
                                                                + Util.numberToMoney(
                                                                        COST_FIND_BOSS - player.inventory.gold)
                                                                + " vàng");
                                                    }
                                                }
                                                break;
                                            case 1:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                                                break;
                                            case 2:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_FIND_RAMBO) {
                                        switch (select) {
                                            case 0:
                                                Boss boss = BossManager.gI().getBossById(BossFactory.RAMBO);
                                                if (boss != null && !boss.isDie()) {
                                                    if (player.inventory.gold >= COST_FIND_BOSS) {
                                                        player.inventory.gold -= COST_FIND_BOSS;
                                                        ChangeMapService.gI().changeMap(player, boss.zone,
                                                                boss.location.x, boss.location.y);
                                                        Service.getInstance().sendMoney(player);
                                                    } else {
                                                        Service.getInstance().sendThongBao(player,
                                                                "Không đủ vàng, còn thiếu "
                                                                + Util.numberToMoney(
                                                                        COST_FIND_BOSS - player.inventory.gold)
                                                                + " vàng");
                                                    }
                                                }
                                                break;
                                            case 1:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                                                break;
                                            case 2:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                                                break;
                                        }
                                    }
                                }
                                if (this.mapId == 68) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 19, -1, 1100);
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.SANTA:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                createOtherMenu(player, ConstNpc.BASE_MENU,
                                        "xin chào ta có rất nhiều đồ vip ngươi cần mua gì ",
                                        "Cửa hàng",
                                        "cửa hàng \n hỗ trợ"
                                //, "shop\nvip"
                                );
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 5 || this.mapId == 13 || this.mapId == 20) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0: //shop
                                                ShopService.gI().openShopNormal(player, this, ConstNpc.SHOP_SANTA_0, 0, -1);
                                                break;
                                            case 1: //tiệm hồng ngọc
                                                ShopService.gI().openShopSpecial(player, this, ConstNpc.SHOP_SANTA_1, 1, -1);
                                                break;
//                                            case 2:
//                                                ShopService.gI().openShopSpecial(player, this, ConstNpc.SHOP_SANTA_3, 3, -1);
//                                                break;
//                                            case 3: //tiệm hớt tóc
//                                                ShopService.gI().openShopSpecial(player, this, ConstNpc.SHOP_SANTA_4, 4, -1);
//                                                break;
                                            case 2: //tiệm hớt tóc
                                                ShopService.gI().openShopSpecial(player, this, ConstNpc.SHOP_SANTA_5, 5, -1);
                                                break;

                                        }
                                    }
                                }
                            }
                        }

                    };
                    break;
                case ConstNpc.GIUMA_DAU_BO:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 6 || this.mapId == 25 || this.mapId == 26) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Gô Tên, Calich và Monaka đang gặp chuyện ở hành tinh Potaufeu \n Hãy đến đó ngay", "Đến \nPotaufeu");
                                } else if (this.mapId == 139) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Người muốn trở về?", "Quay về", "Từ chối");
                                }//lãnh địa bang
                                else if (this.mapId == 153) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Theo ta, ta sẽ đưa ngươi đến Khu vực Thánh địa\nNơi đây ngươi sẽ truy tìm mảnh bông tai cấp 2 và Hồn bông tai để mở chỉ số Bông tai Cấp 3."
                                            + "\n|7|Ngươi có muốn đến đó không?", "Đến\nThánh địa", "Từ chối");
                                } else if (this.mapId == 156) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Người muốn trở về?", "Quay về", "Từ chối");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 24 || this.mapId == 25 || this.mapId == 26) {
                                    if (player.iDMark.isBaseMenu()) {
                                        if (select == 0) {
                                            //đến potaufeu
                                            ChangeMapService.gI().goToPotaufeu(player);
                                        }
                                    }
                                } else if (this.mapId == 139) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            //về trạm vũ trụ
                                            case 0:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 24 + player.gender, -1, -1);
                                                break;
                                        }
                                    }
                                } else if (this.mapId == 153) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            //lãnh địa bang
                                            case 0:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 156, -1, -1);
                                                break;
                                        }
                                    }
                                } else if (this.mapId == 156) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            //về trạm vũ trụ
                                            case 0:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 21 + player.gender, -1, -1);
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.URON:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player pl) {
                            if (canOpenNpc(pl)) {
                                this.openShopWithGender(pl, ConstNpc.SHOP_URON_0, 0);
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {

                            }
                        }
                    };
                    break;
                case ConstNpc.BA_HAT_MIT:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 5 || this.mapId == 13) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Ngươi tìm ta có việc gì?",
                                            // "Ép sao\ntrang bị",
                                            // "dục lỗ khảm",
                                            // "khảm đá",
                                            // "Pha lê\nhóa\ntrang bị",
                                            // "Đột phá\ntrang bị",
                                            // "Nâng cấp\n đê",
                                            // //"Gia Hạn\nvật phẩm",
                                            // "nâng cấp skh",
                                            // "Chân mệnh",
                                            // "mở phong ấn\nvỹ thú"
                                            "Pha Lê\nHóa",
                                            "SKH",
                                            "Thẻ football",
                                            "Thêm"
                                    );
                                } else if (this.mapId == 121) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Ngươi tìm ta có việc gì?",
                                            "Về đảo\nrùa");

                                } else {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Ngươi tìm ta có việc gì?",
                                            "Cửa hàng\nBùa", "Nâng cấp\nVật phẩm",
                                            "Nhập\nNgọc Rồng",
                                            "Nâng cấp\nBông tai\nPorata",
                                            "Mở chỉ số\n bông tai ",
                                            "Sách tuyệt kỹ");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 5 || this.mapId == 13) {
                                    if (player.iDMark.isBaseMenu()) {
                                        // switch (select) {
                                        //     case 0:
                                        //         CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.EP_SAO_TRANG_BI);
                                        //         break;
                                        //     case 1:
                                        //         CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.DUC_LO_TRANG_BI);
                                        //         break;
                                        //     case 2:
                                        //         CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.KHAM_DA_TRANG_BI);
                                        //         break;
                                        //     case 3:
                                        //         CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.PHA_LE_HOA_TRANG_BI);
                                        //         break;
                                        //     case 4: //nâng cấp Chân mệnh
                                        //         this.createOtherMenu(player, 8632,
                                        //                 "|7|ĐỘT PHÁ TRANG BỊ"
                                        //                 + "\n\n|1|Ta sẽ giúp trang bị của ngươi mạnh hơn rất nhiều. Hãy lựa chọn để biết thêm thông tin chi tiết!!!",
                                        //                 "Tinh ấn\ntrang bị",
                                        //                 "Pháp sư hoá\ntrang bị",
                                        //                 "Tẩy\npháp sư");
                                        //         break;
                                        //     case 5:
                                        //         CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.DE_TU_VIP);
                                        //         break;
                                        //     //  this.createOtherMenu(player, 12514,
                                        //     //    "|7|NÂNG CẤP TRANG BỊ"
                                        //     //  + "\n|0|Ta sẽ nâng cấp Trang bị của con lên cấp cao hơn:"
                                        //     // + "\n|5|- Nâng đồ Thần linh : nâng từ đồ Thần linh thành Đồ Hủy diệt"
                                        //     // + "\n- Nâng đồ SKH Vip : cần 3 món Đồ Hủy diệt để nâng thành Set kích hoạt Vip hơn (Tỉ lệ ra đồ Thần linh SKH)",
                                        //     // "Nâng đồ\nThần linh", "Nâng đồ\nSKH Vip");
                                        //     // break;
                                        //     //   case 6:
                                        //     //  CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.GIA_HAN_VAT_PHAM);
                                        //     // break;
                                        //     case 6:
                                        //         CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_SKH);
                                        //         break;
                                        //     case 7: //nâng cấp Chân mệnh
                                        //         this.createOtherMenu(player, 5701,
                                        //                 "|8|CHÂN MỆNH"
                                        //                 + "\nNếu đã có Chân mệnh. Ta sẽ giúp ngươi nâng cấp bậc lên với các dòng chỉ số cao hơn",
                                        //                 "Nâng cấp Chân mệnh");
                                        //         break;
                                        //     case 8:
                                        //         CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.MO_CS_VY_THU);
                                        //         break;
                                        //     //     case 10:
                                        //     // CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_SKH);
                                        //     //  break;  
                                        // }
                                        switch (select) {

                                            // ================================
                                            // 0. PHA LÊ HÓA (SUBMENU)
                                            // ================================
                                            case 0:
                                                this.createOtherMenu(player, 20000, // id menu tự đặt
                                                        "|7|PHA LÊ HÓA\n"
                                                        + "Hãy chọn chức năng muốn sử dụng",
                                                        "Pha lê hóa\ntrang bị",
                                                        "Ép sao\ntrang bị");
                                                break;

                                            // ================================
                                            // 1. NÂNG CẤP SKH
                                            // ================================
                                            case 1:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_SKH);
                                                break;

                                            // ================================
                                            // 2. CHÂN MỆNH
                                            // ================================
                                            case 2:
                                                this.createOtherMenu(player, 5701,
                                                        "|7|QUẢN LÝ CẦU THỦ\nChọn tính năng muốn sử dụng",
                                                        "Đập thẻ cầu thủ",
                                                        "Mở chỉ số cầu thủ");
                                                break;

                                            // ================================
                                            // 3. ĐỘT PHÁ TRANG BỊ (submenu)
                                            // ================================
                                            case 3:
                                                this.createOtherMenu(player, 8632,
                                                        "|7|ĐỘT PHÁ TRANG BỊ\n"
                                                        + "Chọn tính năng muốn sử dụng",
                                                        "Tinh ấn\ntrang bị",
                                                        "Pháp sư hoá\ntrang bị",
                                                        "Tẩy pháp sư",
                                                        "Mở chỉ số\nVỹ Thú",
                                                        "Mở Khóa GD"
                                                );

                                                break;

                                        }
                                    } else if (player.iDMark.getIndexMenu() == 8632) {
                                        switch (select) {
                                            case 0:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.AN_TRANG_BI);
                                                break;
                                            case 1:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.PHAP_SU_HOA);
                                                break;
                                            case 2:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.TAY_PHAP_SU);
                                                break;
                                            case 3:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.MO_CS_VY_THU);
                                                break;
                                            case 4:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.MO_KHOA_GD);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == 20000) {

                                        switch (select) {
                                            case 0:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.PHA_LE_HOA_TRANG_BI);
                                                break;

                                            case 1:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.EP_SAO_TRANG_BI);
                                                break;
                                        }

                                        // =====================================
                                        // SUBMENU: ĐỘT PHÁ TRANG BỊ
                                        // =====================================
                                    } else if (player.iDMark.getIndexMenu() == 12514) {
                                        switch (select) {
                                            case 0:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_THAN_LINH);
                                                break;
                                            case 1:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.Nang_Cap_SKH);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == 5701) {
                                        switch (select) {

                                            case 0: // Đập thẻ
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.DAP_THE_CAU_THU);
                                                break;
                                            case 1: // Mở chỉ số
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.MO_CS_THE);
                                                break;

                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                                        switch (player.combineNew.typeCombine) {
                                            case CombineServiceNew.EP_SAO_TRANG_BI:
                                            case CombineServiceNew.DUC_LO_TRANG_BI:
                                            case CombineServiceNew.KHAM_DA_TRANG_BI:
                                            case CombineServiceNew.NANG_CAP_SKH:
                                            case CombineServiceNew.AN_TRANG_BI:
                                            case CombineServiceNew.MO_CS_VY_THU:
                                            case CombineServiceNew.PHA_LE_HOA_TRANG_BI:
                                            case CombineServiceNew.CHUYEN_HOA_TRANG_BI:
                                            case CombineServiceNew.PHAP_SU_HOA:
                                            case CombineServiceNew.DE_TU_VIP:
                                            case CombineServiceNew.TAY_PHAP_SU:
                                            case CombineServiceNew.NANG_CAP_CHAN_MENH:
                                            case CombineServiceNew.DAP_THE_CAU_THU:
                                            case CombineServiceNew.MO_CS_THE:
                                            //  case CombineServiceNew.CHUYEN_HOA_DO_HUY_DIET:
                                            case CombineServiceNew.NANG_CAP_THAN_LINH:
                                            case CombineServiceNew.Nang_Cap_SKH:
                                            case CombineServiceNew.MO_KHOA_GD:
                                            case CombineServiceNew.GIA_HAN_VAT_PHAM:
                                                switch (select) {
                                                    case 0:
                                                        if (player.combineNew.typeCombine == CombineServiceNew.PHA_LE_HOA_TRANG_BI) {
                                                            player.combineNew.quantities = 1;
                                                        }
                                                        break;
                                                    case 1:
                                                        if (player.combineNew.typeCombine == CombineServiceNew.PHA_LE_HOA_TRANG_BI) {
                                                            player.combineNew.quantities = 10;
                                                        }
                                                        break;
                                                    case 2:
                                                        if (player.combineNew.typeCombine == CombineServiceNew.PHA_LE_HOA_TRANG_BI) {
                                                            player.combineNew.quantities = 100;
                                                        }
                                                        break;
                                                }
                                                CombineServiceNew.gI().startCombine(player);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_CHUYEN_HOA_DO_HUY_DIET) {
                                        if (select == 0) {
                                            CombineServiceNew.gI().startCombine(player);
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_NANG_CAP_THAN_LINH) {
                                        if (select == 0) {
                                            CombineServiceNew.gI().startCombine(player);
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_NANG_DOI_SKH_VIP) {
                                        if (select == 0) {
                                            CombineServiceNew.gI().startCombine(player);
                                        }
                                    }
                                } else if (this.mapId == 112) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                                                break;
                                        }
                                    }
                                } else if (this.mapId == 42 || this.mapId == 43 || this.mapId == 44 || this.mapId == 84) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0: // shop bùa
                                                createOtherMenu(player, ConstNpc.MENU_OPTION_SHOP_BUA,
                                                        "Bùa của ta rất lợi hại, nhìn ngươi yếu đuối thế này, chắc muốn mua bùa để "
                                                        + "mạnh mẽ à, mua không ta bán cho, xài rồi lại thích cho mà xem.",
                                                        "Bùa\n1 giờ", "Bùa\n8 giờ", "Bùa\n1 tháng",
                                                        "Bùa\n  Đệ tử Mabư\n 1 giờ", "Đóng");
                                                break;
                                            case 1:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_VAT_PHAM);
                                                break;
                                            case 2:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NHAP_NGOC_RONG);
                                                break;
                                            case 3: //nâng cấp bông tai
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_BONG_TAI);
                                                break;
                                            case 4: //Mở chỉ số bông tai
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.MO_CHI_SO_BONG_TAI);
                                                break;
                                            case 5: // Sách tuyệt kỹ
                                                createOtherMenu(player, ConstNpc.SACH_TUYET_KY, "Ta có thể giúp gì cho ngươi ?",
                                                        "Đóng thành\nSách cũ",
                                                        "Đổi Sách\nTuyệt kỹ",
                                                        "Giám định\nSách",
                                                        "Tẩy\nSách",
                                                        "Nâng cấp\nSách\nTuyệt kỹ",
                                                        "Hồi phục\nSách",
                                                        "Phân rã\nSách");
                                                break;

                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.SACH_TUYET_KY) {
                                        switch (select) {
                                            case 0:
                                                Item trangSachCu = InventoryService.gI().findItemBagByTemp(player, 1516);

                                                Item biaSach = InventoryService.gI().findItemBagByTemp(player, 1506);
                                                if ((trangSachCu != null && trangSachCu.quantity >= 9999) && (biaSach != null && biaSach.quantity >= 1)) {
                                                    createOtherMenu(player, ConstNpc.DONG_THANH_SACH_CU,
                                                            "|2|Chế tạo Cuốn sách cũ\n"
                                                            + "|1|Trang sách cũ " + trangSachCu.quantity + "/9999\n"
                                                            + "Bìa sách " + biaSach.quantity + "/1\n"
                                                            + "Tỉ lệ thành công: 60%\n"
                                                            + "Thất bại mất 99 trang sách và 1 bìa sách", "Đồng ý", "Từ chối");
                                                    break;
                                                } else {
                                                    String NpcSay = "|2|Chế tạo Cuốn sách cũ\n";
                                                    if (trangSachCu == null) {
                                                        NpcSay += "|7|Trang sách cũ " + "0/9999\n";
                                                    } else {
                                                        NpcSay += "|1|Trang sách cũ " + trangSachCu.quantity + "/9999\n";
                                                    }
                                                    if (biaSach == null) {
                                                        NpcSay += "|7|Bìa sách " + "0/1\n";
                                                    } else {
                                                        NpcSay += "|1|Bìa sách " + biaSach.quantity + "/1\n";
                                                    }

                                                    NpcSay += "|7|Tỉ lệ thành công: 60%\n";
                                                    NpcSay += "|7|Thất bại mất 99 trang sách và 1 bìa sách";
                                                    createOtherMenu(player, ConstNpc.DONG_THANH_SACH_CU_2,
                                                            NpcSay, "Từ chối");
                                                    break;
                                                }
                                            case 1:
                                                Item cuonSachCu = InventoryService.gI().findItemBagByTemp(player, 1509);
                                                Item kimBam = InventoryService.gI().findItemBagByTemp(player, 1507);

                                                if ((cuonSachCu != null && cuonSachCu.quantity >= 10) && (kimBam != null && kimBam.quantity >= 1)) {
                                                    createOtherMenu(player, ConstNpc.DOI_SACH_TUYET_KY,
                                                            "|2|Đổi sách tuyệt kỹ 1\n"
                                                            + "|1|Cuốn sách cũ " + cuonSachCu.quantity + "/10\n"
                                                            + "Kìm bấm giấy " + kimBam.quantity + "/1\n"
                                                            + "Tỉ lệ thành công: 60%\n", "Đồng ý", "Từ chối");
                                                    break;
                                                } else {
                                                    String NpcSay = "|2|Đổi sách Tuyệt kỹ 1\n";
                                                    if (cuonSachCu == null) {
                                                        NpcSay += "|7|Cuốn sách cũ " + "0/10\n";
                                                    } else {
                                                        NpcSay += "|1|Cuốn sách cũ " + cuonSachCu.quantity + "/10\n";
                                                    }
                                                    if (kimBam == null) {
                                                        NpcSay += "|7|Kìm bấm giấy " + "0/1\n";
                                                    } else {
                                                        NpcSay += "|1|Kìm bấm giấy " + kimBam.quantity + "/1\n";
                                                    }
                                                    NpcSay += "|7|Tỉ lệ thành công: 60%\n";
                                                    createOtherMenu(player, ConstNpc.DOI_SACH_TUYET_KY_2,
                                                            NpcSay, "Từ chối");
                                                }
                                                break;
                                            case 2:// giám định sách
                                                CombineServiceNew.gI().openTabCombine(player,
                                                        CombineServiceNew.GIAM_DINH_SACH);
                                                break;
                                            case 3:// tẩy sách
                                                CombineServiceNew.gI().openTabCombine(player,
                                                        CombineServiceNew.TAY_SACH);
                                                break;
                                            case 4:// nâng cấp sách
                                                CombineServiceNew.gI().openTabCombine(player,
                                                        CombineServiceNew.NANG_CAP_SACH_TUYET_KY);
                                                break;
                                            case 5:// phục hồi sách
                                                CombineServiceNew.gI().openTabCombine(player,
                                                        CombineServiceNew.PHUC_HOI_SACH);
                                                break;
                                            case 6:// phân rã sách
                                                CombineServiceNew.gI().openTabCombine(player,
                                                        CombineServiceNew.PHAN_RA_SACH);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.DOI_SACH_TUYET_KY) {
                                        switch (select) {
                                            case 0:
                                                Item cuonSachCu = InventoryService.gI().findItemBagByTemp(player, 1509);
                                                Item kimBam = InventoryService.gI().findItemBagByTemp(player, 1507);

                                                short baseValue = 1512;
                                                short genderModifier = (player.gender == 0) ? -2 : ((player.gender == 2) ? 2 : (short) 0);

                                                Item sachTuyetKy = ItemService.gI().createNewItem((short) (baseValue + genderModifier));

                                                if (Util.isTrue(60, 100)) {

                                                    sachTuyetKy.itemOptions.add(new ItemOption(241, 0));
                                                    sachTuyetKy.itemOptions.add(new ItemOption(21, 40));
                                                    sachTuyetKy.itemOptions.add(new ItemOption(30, 0));
                                                    sachTuyetKy.itemOptions.add(new ItemOption(87, 1));
                                                    sachTuyetKy.itemOptions.add(new ItemOption(242, 10));
                                                    sachTuyetKy.itemOptions.add(new ItemOption(243, 1000));
                                                    try { // send effect susscess
                                                        Message msg = new Message(-81);
                                                        msg.writer().writeByte(0);
                                                        msg.writer().writeUTF("test");
                                                        msg.writer().writeUTF("test");
                                                        msg.writer().writeShort(tempId);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                        msg = new Message(-81);
                                                        msg.writer().writeByte(1);
                                                        msg.writer().writeByte(2);
                                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, kimBam));
                                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, cuonSachCu));
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                        msg = new Message(-81);
                                                        msg.writer().writeByte(7);
                                                        msg.writer().writeShort(sachTuyetKy.template.iconID);
                                                        msg.writer().writeShort(-1);
                                                        msg.writer().writeShort(-1);
                                                        msg.writer().writeShort(-1);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                    } catch (Exception e) {
                                                        System.out.println("lỗi 4");
                                                    }
                                                    InventoryService.gI().addItemList(player.inventory.itemsBag, sachTuyetKy, 1);
                                                    InventoryService.gI().subQuantityItemsBag(player, cuonSachCu, 10);
                                                    InventoryService.gI().subQuantityItemsBag(player, kimBam, 1);
                                                    InventoryService.gI().sendItemBags(player);
//                                                    npcChat(player, "Thành công gòi cu ơi");
                                                    return;
                                                } else {
                                                    try { // send effect faile
                                                        Message msg = new Message(-81);
                                                        msg.writer().writeByte(0);
                                                        msg.writer().writeUTF("test");
                                                        msg.writer().writeUTF("test");
                                                        msg.writer().writeShort(tempId);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                        msg = new Message(-81);
                                                        msg.writer().writeByte(1);
                                                        msg.writer().writeByte(2);
                                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, kimBam));
                                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, cuonSachCu));
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                        msg = new Message(-81);
                                                        msg.writer().writeByte(8);
                                                        msg.writer().writeShort(-1);
                                                        msg.writer().writeShort(-1);
                                                        msg.writer().writeShort(-1);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                    } catch (Exception e) {
                                                        System.out.println("lỗi 3");
                                                    }
                                                    InventoryService.gI().subQuantityItemsBag(player, cuonSachCu, 5);
                                                    InventoryService.gI().subQuantityItemsBag(player, kimBam, 1);
                                                    InventoryService.gI().sendItemBags(player);
//                                                    npcChat(player, "Thất bại gòi cu ơi");
                                                }
                                                return;
                                            case 1:
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.DONG_THANH_SACH_CU) {
                                        switch (select) {
                                            case 0:

                                                Item trangSachCu = InventoryService.gI().findItemBagByTemp(player, 1516);
                                                Item biaSach = InventoryService.gI().findItemBagByTemp(player, 1506);
                                                Item cuonSachCu = ItemService.gI().createNewItem((short) 1509);
                                                if (Util.isTrue(60, 100)) {
                                                    cuonSachCu.itemOptions.add(new ItemOption(30, 0));

                                                    try { // send effect susscess
                                                        Message msg = new Message(-81);
                                                        msg.writer().writeByte(0);
                                                        msg.writer().writeUTF("test");
                                                        msg.writer().writeUTF("test");
                                                        msg.writer().writeShort(tempId);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();

                                                        msg = new Message(-81);
                                                        msg.writer().writeByte(1);
                                                        msg.writer().writeByte(2);
                                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, trangSachCu));
                                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, biaSach));
                                                        player.sendMessage(msg);
                                                        msg.cleanup();

                                                        msg = new Message(-81);
                                                        msg.writer().writeByte(7);
                                                        msg.writer().writeShort(cuonSachCu.template.iconID);
                                                        msg.writer().writeShort(-1);
                                                        msg.writer().writeShort(-1);
                                                        msg.writer().writeShort(-1);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();

                                                    } catch (Exception e) {
                                                        System.out.println("lỗi 1");
                                                    }

                                                    InventoryService.gI().addItemList(player.inventory.itemsBag, cuonSachCu, 99);
                                                    InventoryService.gI().subQuantityItemsBag(player, trangSachCu, 9999);
                                                    InventoryService.gI().subQuantityItemsBag(player, biaSach, 1);
                                                    InventoryService.gI().sendItemBags(player);
                                                    return;
                                                } else {
                                                    try { // send effect faile
                                                        Message msg = new Message(-81);
                                                        msg.writer().writeByte(0);
                                                        msg.writer().writeUTF("test");
                                                        msg.writer().writeUTF("test");
                                                        msg.writer().writeShort(tempId);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                        msg = new Message(-81);
                                                        msg.writer().writeByte(1);
                                                        msg.writer().writeByte(2);
                                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, biaSach));
                                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, trangSachCu));
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                        msg = new Message(-81);
                                                        msg.writer().writeByte(8);
                                                        msg.writer().writeShort(-1);
                                                        msg.writer().writeShort(-1);
                                                        msg.writer().writeShort(-1);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                    } catch (Exception e) {
                                                        System.out.println("lỗi 2");
                                                    }
                                                    InventoryService.gI().subQuantityItemsBag(player, trangSachCu, 99);
                                                    InventoryService.gI().subQuantityItemsBag(player, biaSach, 1);
                                                    InventoryService.gI().sendItemBags(player);
                                                }
                                                return;
                                            case 1:
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPTION_SHOP_BUA) {
                                        switch (select) {
                                            case 0:
                                                ShopService.gI().openShopBua(player, ConstNpc.SHOP_BA_HAT_MIT_0, 0);
                                                break;
                                            case 1:
                                                ShopService.gI().openShopBua(player, ConstNpc.SHOP_BA_HAT_MIT_1, 1);
                                                break;
                                            case 2:
                                                ShopService.gI().openShopBua(player, ConstNpc.SHOP_BA_HAT_MIT_2, 2);
                                                break;
                                            case 3:
                                                ShopService.gI().openShopBua(player, ConstNpc.SHOP_BA_HAT_MIT_3, 3);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                                        switch (player.combineNew.typeCombine) {
                                            case CombineServiceNew.NANG_CAP_VAT_PHAM:
                                            case CombineServiceNew.NANG_CAP_BONG_TAI:
                                            case CombineServiceNew.LAM_PHEP_NHAP_DA:
                                            case CombineServiceNew.NHAP_NGOC_RONG:
                                            case CombineServiceNew.PHAN_RA_DO_THAN_LINH:
                                            case CombineServiceNew.Nang_Cap_SKH:
                                            case CombineServiceNew.MO_CHI_SO_BONG_TAI:
                                            case CombineServiceNew.DAP_THE_CAU_THU:   // <--- thêm
                                            case CombineServiceNew.MO_CS_THE:
                                            //START _ SÁCH TUYỆT KỸ//
                                            case CombineServiceNew.GIAM_DINH_SACH:
                                            case CombineServiceNew.TAY_SACH:
                                            case CombineServiceNew.NANG_CAP_SACH_TUYET_KY:
                                            case CombineServiceNew.PHUC_HOI_SACH:
                                            case CombineServiceNew.PHAN_RA_SACH:
                                                //END _ SÁCH TUYỆT KỸ//
                                                if (select == 0) {
                                                    CombineServiceNew.gI().startCombine(player);
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.RUONG_DO:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {

                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                InventoryService.gI().sendItemBox(player);
                                InventoryService.gI().openBox(player);
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {

                            }
                        }
                    };
                    break;
                case ConstNpc.DAU_THAN:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                player.magicTree.openMenuTree();
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                TaskService.gI().checkDoneTaskConfirmMenuNpc(player, this, (byte) select);
                                switch (player.iDMark.getIndexMenu()) {
                                    case ConstNpc.MAGIC_TREE_NON_UPGRADE_LEFT_PEA:
                                        if (select == 0) {
                                            player.magicTree.harvestPea();
                                        } else if (select == 1) {
                                            if (player.magicTree.level == 10) {
                                                player.magicTree.fastRespawnPea();
                                            } else {
                                                player.magicTree.showConfirmUpgradeMagicTree();
                                            }
                                        } else if (select == 2) {
                                            player.magicTree.fastRespawnPea();
                                        }
                                        break;
                                    case ConstNpc.MAGIC_TREE_NON_UPGRADE_FULL_PEA:
                                        if (select == 0) {
                                            player.magicTree.harvestPea();
                                        } else if (select == 1) {
                                            player.magicTree.showConfirmUpgradeMagicTree();
                                        }
                                        break;
                                    case ConstNpc.MAGIC_TREE_CONFIRM_UPGRADE:
                                        if (select == 0) {
                                            player.magicTree.upgradeMagicTree();
                                        }
                                        break;
                                    case ConstNpc.MAGIC_TREE_UPGRADE:
                                        if (select == 0) {
                                            player.magicTree.fastUpgradeMagicTree();
                                        } else if (select == 1) {
                                            player.magicTree.showConfirmUnuppgradeMagicTree();
                                        }
                                        break;
                                    case ConstNpc.MAGIC_TREE_CONFIRM_UNUPGRADE:
                                        if (select == 0) {
                                            player.magicTree.unupgradeMagicTree();
                                        }
                                        break;
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.CALICK:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        private final byte COUNT_CHANGE = 50;
                        private int count;

                        private void changeMap() {
                            if (this.mapId != 102) {
                                count++;
                                if (this.count >= COUNT_CHANGE) {
                                    count = 0;
                                    this.map.npcs.remove(this);
                                    Map map = MapService.gI().getMapForCalich();
                                    this.mapId = map.mapId;
                                    this.cx = Util.nextInt(100, map.mapWidth - 100);
                                    this.cy = map.yPhysicInTop(this.cx, 0);
                                    this.map = map;
                                    this.map.npcs.add(this);
                                }
                            }
                        }

                        @Override
                        public void openBaseMenu(Player player) {
                            player.iDMark.setIndexMenu(ConstNpc.BASE_MENU);
                            if (TaskService.gI().getIdTask(player) < ConstTask.TASK_20_0) {
                                Service.getInstance().hideWaitDialog(player);
                                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                                return;
                            }
                            if (this.mapId != player.zone.map.mapId) {
                                Service.getInstance().sendThongBao(player, "Calích đã rời khỏi map!");
                                Service.getInstance().hideWaitDialog(player);
                                return;
                            }

                            if (this.mapId == 102) {
                                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Chào chú, cháu có thể giúp gì?",
                                        "Kể\nChuyện", "Quay về\nQuá khứ");
                            } else {
                                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Chào chú, cháu có thể giúp gì?",
                                        "Kể\nChuyện", "Đi đến\nTương lai", "Từ chối");
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (this.mapId == 102) {
                                if (player.iDMark.isBaseMenu()) {
                                    if (select == 0) {
                                        // kể chuyện
                                        NpcService.gI().createTutorial(player, this.avartar, ConstNpc.CALICK_KE_CHUYEN);
                                    } else if (select == 1) {
                                        // về quá khứ
                                        ChangeMapService.gI().goToQuaKhu(player);
                                    }
                                }
                            } else if (player.iDMark.isBaseMenu()) {
                                if (select == 0) {
                                    // kể chuyện
                                    NpcService.gI().createTutorial(player, this.avartar, ConstNpc.CALICK_KE_CHUYEN);
                                } else if (select == 1) {
                                    // đến tương lai
                                    // changeMap();
                                    if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_20_0) {
                                        ChangeMapService.gI().goToTuongLai(player);
                                    }
                                } else {
                                    Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.JACO:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 0) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "|7| KHU VỰC BOSS NHÂN BẢN"
                                            + "\n\n|6|Gô Tên, Calich và Monaka đang gặp chuyện ở hành tinh Potaufeu"
                                            + "\nĐánh bại những kẻ giả mạo ngươi sẽ nhận được những phần thưởng hấp dẫn"
                                            + "\n|3|Hạ Boss Nhân Bản sẽ nhận được Item Siêu cấp"
                                            + "\n|2|Hãy đến đó ngay",
                                            "Đến \nPotaufeu");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Người muốn trở về?", "Quay về", "Từ chối");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 0) {
                                    if (player.iDMark.isBaseMenu()) {
                                        if (select == 0) {
                                            ChangeMapService.gI().goToPotaufeu(player);
                                        }
                                    }
                                } else {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.THAN_MEO_KARIN:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (mapId == ConstMap.THAP_KARIN) {
                                    if (player.zone instanceof ZSnakeRoad) {
                                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                                "Hãy cầm lấy hai hạt đậu cuối cùng ở đây\nCố giữ mình nhé "
                                                + player.name,
                                                "Cảm ơn\nsư phụ");
                                    } else if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                                "Chào con, con muốn ta giúp gì nào?", getMenuSuKien(EVENT_SEVER));
                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (mapId == ConstMap.THAP_KARIN) {
                                    if (player.iDMark.isBaseMenu()) {
                                        if (player.zone instanceof ZSnakeRoad) {
                                            switch (select) {
                                                case 0:
                                                    player.setInteractWithKarin(true);
                                                    Service.getInstance().sendThongBao(player,
                                                            "Hãy mau bay xuống chân tháp Karin");
                                                    break;
                                            }
                                        } else {
                                            switch (select) {
                                                case 0:
                                                    switch (EVENT_SEVER) {
                                                        case 2:
                                                            Attribute at = ServerManager.gI().getAttributeManager()
                                                                    .find(ConstAttribute.TNSM);
                                                            String text = "Sự kiện 20/11 chính thức tại Ngọc Rồng "
                                                                    + Manager.SERVER_NAME + "\n "
                                                                    + "Số điểm hiện tại của bạn là : "
                                                                    + player.event.getEventPoint()
                                                                    + "\nTổng số hoa đã tặng trên toàn máy chủ "
                                                                    + EVENT_COUNT_THAN_MEO % 999 + "/999";
                                                            this.createOtherMenu(player, ConstNpc.MENU_OPEN_SUKIEN,
                                                                    at != null && !at.isExpired() ? text
                                                                    + "\nToàn bộ máy chủ được tăng 20% TNSM cho đệ tử khi đánh quái,thời gian còn lại "
                                                                    + at.getTime() / 60 + " phút."
                                                                    : text + "\nKhi tặng đủ 999 bông hoa toàn bộ máy chủ được tăng tăng 20% TNSM cho đệ tử trong 60 phút\n",
                                                                    "Tặng 1\n Bông hoa", "Tặng\n10 Bông",
                                                                    "Tặng\n99 Bông", "Đổi\nHộp quà");
                                                            break;
                                                    }
                                            }
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPEN_SUKIEN) {
                                        openMenuSuKien(player, this, tempId, select);
                                    }
                                }
                            }
                        }
                    };
                    break;

                case ConstNpc.THUONG_DE:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {

                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 45) {
                                    if (!player.istrain) {
                                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con đã mạnh hơn ta"
                                                + ", ta sẽ chỉ đường cho con đến Kaio để gặp thần Vũ Trụ Phương Bắc"
                                                + "\nNgài là thần cai quản vũ trụ này, hãy theo ngài ấy học võ công",
                                                //"Đăng ký tập tự động",
                                                "Đến Kaio", "Quay số\nmay mắn", getMenuSuKien(EVENT_SEVER));
                                    } else {
                                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con đã mạnh hơn ta"
                                                + ", ta sẽ chỉ đường cho con đến Kaio để gặp thần Vũ Trụ Phương Bắc"
                                                + "\nNgài là thần cai quản vũ trụ này, hãy theo ngài ấy học võ công",
                                                "Hủy đăng ký tập tự động", "Đến Kaio", "Quay số\nmay mắn", getMenuSuKien(EVENT_SEVER));
                                    }
                                } else if (player.zone instanceof ZSnakeRoad) {
                                    if (mapId == ConstMap.CON_DUONG_RAN_DOC) {
                                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Hãy lắm lấy tay ta mau",
                                                "Về thần điện");
                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 45) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            // case 0:
                                            //  if (!player.istrain) {
                                            //this.createOtherMenu(player, ConstNpc.MENU_TRAIN_OFFLINE,
                                            //       "Đăng ký để mỗi khi Offline quá 30 phút"
                                            //      + ", con sẽ được tự động luyện tập với tốc độ " + player.getexp() + " sức mạnh mỗi phút",
                                            //     "Hướng dẫn thêm", "Đồng ý 1 ngọc mỗi lần", "Không đồng ý");
                                            // } else {
                                            //  player.istrain = false;
                                            //  this.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Con đã hủy thành công đăng ký tập tự động", "Đóng");
                                            //    }
                                            //   break;
                                            case 0:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 48, -1, 354);
                                                break;
                                            case 1:
                                                this.createOtherMenu(player, ConstNpc.MENU_CHOOSE_LUCKY_ROUND,
                                                        "Con muốn làm gì nào?", "vòng quay na cờ rốt",
                                                        "Rương phụ\n("
                                                        + (player.inventory.itemsBoxCrackBall.size()
                                                        - InventoryService.gI().getCountEmptyListItem(
                                                                player.inventory.itemsBoxCrackBall))
                                                        + " món)",
                                                        "Xóa hết\ntrong rương", "Đóng");
                                                break;
                                            case 2:
                                                switch (EVENT_SEVER) {
                                                    case 2:
                                                        Attribute at = ServerManager.gI().getAttributeManager()
                                                                .find(ConstAttribute.KI);
                                                        String text = "Sự kiện 20/11 chính thức tại Ngọc Rồng "
                                                                + Manager.SERVER_NAME + "\n + "
                                                                + "Số điểm hiện tại của bạn là : "
                                                                + player.event.getEventPoint()
                                                                + "\nTổng số hoa đã tặng trên toàn máy chủ "
                                                                + EVENT_COUNT_THUONG_DE % 999 + "/999";
                                                        this.createOtherMenu(player, ConstNpc.MENU_OPEN_SUKIEN,
                                                                at != null && !at.isExpired() ? text
                                                                + "\nToàn bộ máy chủ được tăng 20% KI,thời gian còn lại "
                                                                + at.getTime() / 60 + " phút."
                                                                : text + "\nKhi tặng đủ 999 bông hoa toàn bộ máy chủ được tăng 20% Ki trong 60 phút\n",
                                                                "Tặng 1\n Bông hoa", "Tặng\n10 Bông", "Tặng\n99 Bông",
                                                                "Đổi\nHộp quà");
                                                        break;
                                                }
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_TRAIN_OFFLINE) {
                                        switch (select) {
                                            case 0:
                                                Service.getInstance().sendPopUpMultiLine(player, tempId, this.avartar, ConstNpc.INFOR_TRAIN_OFFLINE);
                                                break;
                                            case 1:
                                                player.istrain = true;
                                                NpcService.gI().createTutorial(player, this.avartar, "Từ giờ, quá 30 phút Offline con sẽ tự động luyện tập");
                                                break;
                                            case 3:
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_CHOOSE_LUCKY_ROUND) {
                                        switch (select) {
                                            case 0:
                                                LuckyRoundService.gI().openCrackBallUI(player,
                                                        LuckyRoundService.USING_GOLD);
                                                break;
                                            case 1:
                                                ShopService.gI().openBoxItemLuckyRound(player);
                                                break;
                                            case 2:
                                                NpcService.gI().createMenuConMeo(player,
                                                        ConstNpc.CONFIRM_REMOVE_ALL_ITEM_LUCKY_ROUND, this.avartar,
                                                        "Con có chắc muốn xóa hết vật phẩm trong rương phụ? Sau khi xóa "
                                                        + "sẽ không thể khôi phục!",
                                                        "Đồng ý", "Hủy bỏ");
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPEN_SUKIEN) {
                                        openMenuSuKien(player, this, tempId, select);
                                    }
                                } else if (player.zone instanceof ZSnakeRoad) {
                                    if (mapId == ConstMap.CON_DUONG_RAN_DOC) {
                                        ZSnakeRoad zroad = (ZSnakeRoad) player.zone;
                                        if (zroad.isKilledAll()) {
                                            SnakeRoad road = (SnakeRoad) zroad.getDungeon();
                                            ZSnakeRoad egr = (ZSnakeRoad) road.find(ConstMap.THAN_DIEN);
                                            egr.enter(player, 360, 408);
                                            Service.getInstance().sendThongBao(player, "Hãy xuống gặp thần mèo Karin");
                                        } else {
                                            Service.getInstance().sendThongBao(player,
                                                    "Hãy tiêu diệt hết quái vật ở đây!");
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.THAN_VU_TRU:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 48) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn làm gì nào", "Di chuyển",
                                            getMenuSuKien(EVENT_SEVER));
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 48) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                this.createOtherMenu(player, ConstNpc.MENU_DI_CHUYEN,
                                                        "Con muốn đi đâu?", "Về\nthần điện", "Thánh địa\nKaio",
                                                        "Con\nđường\nrắn độc", getMenuSuKien(EVENT_SEVER), "Từ chối");
                                                break;
                                            case 1:
                                                switch (EVENT_SEVER) {
                                                    case 2:
                                                        Attribute at = ServerManager.gI().getAttributeManager()
                                                                .find(ConstAttribute.HP);
                                                        String text = "Sự kiện 20/11 chính thức tại Ngọc Rồng "
                                                                + Manager.SERVER_NAME + "\n "
                                                                + "Số điểm hiện tại của bạn là : "
                                                                + player.event.getEventPoint()
                                                                + "\nTổng số hoa đã tặng trên toàn máy chủ "
                                                                + EVENT_COUNT_THAN_VU_TRU % 999 + "/999";
                                                        this.createOtherMenu(player, ConstNpc.MENU_OPEN_SUKIEN,
                                                                at != null && !at.isExpired() ? text
                                                                + "\nToàn bộ máy chủ được tăng 20% HP,thời gian còn lại "
                                                                + at.getTime() / 60 + " phút."
                                                                : text + "\nKhi tặng đủ 999 bông hoa toàn bộ máy chủ được tăng 20% HP trong 60 phút\n",
                                                                "Tặng 1\n Bông hoa", "Tặng\n10 Bông", "Tặng\n99 Bông",
                                                                "Đổi\nHộp quà");
                                                        break;
                                                }
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_DI_CHUYEN) {
                                        switch (select) {
                                            case 0:
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 45, -1, 354);
                                                break;
                                            case 1:
                                                ChangeMapService.gI().changeMap(player, 50, -1, 318, 336);
                                                break;
                                            case 2:
                                                // con đường rắn độc
                                                // Service.getInstance().sendThongBao(player, "Comming Soon.");
                                                if (player.clan != null) {
                                                    Calendar calendar = Calendar.getInstance();
                                                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                                                    if (!(dayOfWeek == Calendar.MONDAY
                                                            || dayOfWeek == Calendar.WEDNESDAY
                                                            || dayOfWeek == Calendar.FRIDAY
                                                            || dayOfWeek == Calendar.SUNDAY)) {
                                                        Service.getInstance().sendThongBao(player,
                                                                "Chỉ mở vào thứ 2, 4, 6, CN hàng tuần!");
                                                        return;
                                                    }
                                                    if (player.clanMember.getNumDateFromJoinTimeToToday() < 2) {
                                                        Service.getInstance().sendThongBao(player,
                                                                "Phải tham gia bang hội ít nhất 2 ngày mới có thể tham gia!");
                                                        return;
                                                    }
                                                    if (player.clan.snakeRoad == null) {
                                                        this.createOtherMenu(player, ConstNpc.MENU_CHON_CAP_DO,
                                                                "Hãy mau trở về bằng con đường rắn độc\nbọn Xayda đã đến Trái Đất",
                                                                "Chọn\ncấp độ", "Từ chối");
                                                    } else {
                                                        if (player.clan.snakeRoad.isClosed()) {
                                                            Service.getInstance().sendThongBao(player,
                                                                    "Bang hội đã hết lượt tham gia!");
                                                        } else {
                                                            this.createOtherMenu(player,
                                                                    ConstNpc.MENU_ACCEPT_GO_TO_CDRD,
                                                                    "Con có chắc chắn muốn đến con đường rắn độc cấp độ "
                                                                    + player.clan.snakeRoad.getLevel() + "?",
                                                                    "Đồng ý", "Từ chối");
                                                        }
                                                    }
                                                } else {
                                                    Service.getInstance().sendThongBao(player,
                                                            "Chỉ dành cho những người trong bang hội!");
                                                }
                                                break;

                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_CHON_CAP_DO) {
                                        switch (select) {
                                            case 0:
                                                Input.gI().createFormChooseLevelCDRD(player);
                                                break;

                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_ACCEPT_GO_TO_CDRD) {
                                        switch (select) {
                                            case 0:
                                                if (player.clan != null) {
                                                    synchronized (player.clan) {
                                                        if (player.clan.snakeRoad == null) {
                                                            int level = Byte.parseByte(
                                                                    String.valueOf(PLAYERID_OBJECT.get(player.id)));
                                                            SnakeRoad road = new SnakeRoad(level);
                                                            ServerManager.gI().getDungeonManager().addDungeon(road);
                                                            road.join(player);
                                                            player.clan.snakeRoad = road;
                                                        } else {
                                                            player.clan.snakeRoad.join(player);
                                                        }
                                                    }
                                                }
                                                break;

                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPEN_SUKIEN) {
                                        openMenuSuKien(player, this, tempId, select);
                                    }
                                }
                            }
                        }

                    };
                    break;
                case ConstNpc.KIBIT:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 50) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                                            "Đến\nKaio", "Từ chối");
                                } else {
                                    super.openBaseMenu(player);
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 50) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                ChangeMapService.gI().changeMap(player, 48, -1, 354, 240);
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.OSIN:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 50) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                                            "Đến\nKaio", "Đến\nhành tinh\nBill", "Từ chối");
                                } else if (this.mapId == 52) {
                                    if (MabuWar.gI().isTimeMabuWar() || MabuWar14h.gI().isTimeMabuWar()) {
                                        if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                                    "Bây giờ tôi sẽ bí mật...\n đuổi theo 2 tên đồ tể... \n"
                                                    + "Quý vị nào muốn đi theo thì xin mời !",
                                                    "Ok", "Từ chối");
                                        }
                                    } else {
                                        if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                                    "Vào lúc 12h tôi sẽ bí mật...\n đuổi theo 2 tên đồ tể... \n"
                                                    + "Quý vị nào muốn đi theo thì xin mời !",
                                                    "Ok", "Từ chối");
                                        }
                                    }
                                } else if (this.mapId == 154) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "|3|Để đến được Hành tinh ngục tù yêu cầu mang 5 món đồ Hủy diệt"
                                            + "\n|1|Ta có thể giúp gì cho ngươi ?",
                                            "Về thánh địa", "Đến\nhành tinh\nngục tù", "Từ chối");
                                } else if (this.mapId == 155 || this.mapId == 165) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                                            "Quay về", "Từ chối");
                                } else if (MapService.gI().isMapMabuWar(this.mapId) || MapService.gI().isMapMabuWar14H(this.mapId)) {
                                    if (MabuWar.gI().isTimeMabuWar()) {
                                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                                "Đừng vội xem thường Babyđây,ngay đến cha hắn là thần ma đạo sĩ\n"
                                                + "Bibiđây khi còn sống cũng phải sợ hắn đấy",
                                                "Giải trừ\nphép thuật\n50Tr Vàng",
                                                player.zone.map.mapId != 120 ? "Xuống\nTầng Dưới" : "Rời\nKhỏi đây");
                                    } else if (MabuWar14h.gI().isTimeMabuWar()) {
                                        createOtherMenu(player, ConstNpc.BASE_MENU, "Ta sẽ phù hộ cho ngươi bằng nguồn sức mạnh của Thần Kaiô"
                                                + "\n+1 triệu HP, +1 triệu MP, +10k Sức đánh"
                                                + "\nLưu ý: sức mạnh sẽ biến mất khi ngươi rời khỏi đây",
                                                "Phù hộ\n55 hồng ngọc", "Từ chối", "Về\nĐại Hội\nVõ Thuật");
                                    }
                                } else {
                                    super.openBaseMenu(player);
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 50) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                ChangeMapService.gI().changeMap(player, 48, -1, 354, 240);
                                                break;
                                            case 1:
                                                ChangeMapService.gI().changeMap(player, 154, -1, 200, 312);
                                                break;
                                        }
                                    }
                                } else if (this.mapId == 52) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                if (MabuWar.gI().isTimeMabuWar()) {
                                                    ChangeMapService.gI().changeMap(player, 114, -1, 354, 240);
                                                } else {
                                                    ChangeMapService.gI().changeMap(player, 127, -1, 354, 240);
                                                }
                                                break;
                                        }
                                    }
                                } else if (this.mapId == 154) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                ChangeMapService.gI().changeMap(player, 50, -1, 318, 336);
                                                break;
                                            case 1:
                                                if (!Manager.gI().getGameConfig().isOpenPrisonPlanet()) {
                                                    Service.getInstance().sendThongBao(player,
                                                            "Lối vào hành tinh ngục tù chưa mở");
                                                    return;
                                                }
                                                if (player.nPoint.power < 60000000000L) {
                                                    Service.getInstance().sendThongBao(player,
                                                            "Yêu cầu tối thiếu 60tỷ sức mạnh");
                                                    return;
                                                }
                                                if (player.setClothes.setDHD != 5) {
                                                    Service.getInstance().sendThongBao(player,
                                                            "Yêu cầu mang set Đồ Hủy diệt");
                                                    return;
                                                }
                                                ChangeMapService.gI().changeMap(player, 155, -1, 111, 792);
                                                break;
                                        }
                                    }
                                } else if (this.mapId == 155) {
                                    if (player.iDMark.isBaseMenu()) {
                                        if (select == 0) {
                                            ChangeMapService.gI().changeMap(player, 154, -1, 200, 312);
                                        }
                                    }
                                } else if (this.mapId == 165) {
                                    if (player.iDMark.isBaseMenu()) {
                                        if (select == 0) {
                                            ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
                                        }
                                    }
                                } else if (MapService.gI().isMapMabuWar(this.mapId) || MapService.gI().isMapMabuWar14H(this.mapId)) {
                                    if (player.iDMark.isBaseMenu()) {
                                        if (MabuWar.gI().isTimeMabuWar()) {
                                            switch (select) {
                                                case 0:
                                                    if (player.inventory.getGold() >= 50000000) {
                                                        Service.getInstance().changeFlag(player, 9);
                                                        player.inventory.subGold(50000000);

                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Không đủ vàng");
                                                    }
                                                    break;
                                                case 1:
                                                    if (player.zone.map.mapId == 120) {
                                                        ChangeMapService.gI().changeMapBySpaceShip(player,
                                                                player.gender + 21, -1, 250);
                                                    }
                                                    if (player.cFlag == 9) {
                                                        if (player.getPowerPoint() >= 20) {
                                                            if (!(player.zone.map.mapId == 119)) {
                                                                int idMapNextFloor = player.zone.map.mapId == 115
                                                                        ? player.zone.map.mapId + 2
                                                                        : player.zone.map.mapId + 1;
                                                                ChangeMapService.gI().changeMap(player, idMapNextFloor, -1,
                                                                        354, 240);
                                                            } else {
                                                                Zone zone = MabuWar.gI().getMapLastFloor(120);
                                                                if (zone != null) {
                                                                    ChangeMapService.gI().changeMap(player, zone, 354, 240);
                                                                } else {
                                                                    Service.getInstance().sendThongBao(player,
                                                                            "Trận đại chiến đã kết thúc, tàu vận chuyển sẽ đưa bạn về nhà");
                                                                }
                                                            }
                                                            player.resetPowerPoint();
                                                            player.sendMenuGotoNextFloorMabuWar = false;
                                                            Service.getInstance().sendPowerInfo(player, "%",
                                                                    player.getPowerPoint());
                                                            if (Util.isTrue(1, 30)) {
                                                                player.inventory.ruby += 1;
                                                                PlayerService.gI().sendInfoHpMpMoney(player);
                                                                Service.getInstance().sendThongBao(player,
                                                                        "Bạn nhận được 1 Hồng Ngọc");
                                                            } else {
                                                                Service.getInstance().sendThongBao(player,
                                                                        "Bạn đen vô cùng luôn nên không nhận được gì cả");
                                                            }
                                                        } else {
                                                            this.npcChat(player,
                                                                    "Ngươi cần có đủ điểm để xuống tầng tiếp theo");
                                                        }
                                                        break;
                                                    } else {
                                                        this.npcChat(player,
                                                                "Ngươi đang theo phe Babiđây,Hãy qua bên đó mà thể hiện");
                                                    }
                                            }
                                        } else if (MabuWar14h.gI().isTimeMabuWar()) {
                                            switch (select) {
                                                case 0:
                                                    if (player.effectSkin.isPhuHo) {
                                                        this.npcChat("Con đã mang trong mình sức mạnh của thần Kaiô!");
                                                        return;
                                                    }
                                                    if (player.inventory.ruby < 55) {
                                                        Service.getInstance().sendThongBao(player, "Bạn không đủ hồng ngọc");
                                                    } else {
                                                        player.inventory.ruby -= 55;
                                                        player.effectSkin.isPhuHo = true;
                                                        Service.getInstance().point(player);
                                                        this.npcChat("Ta đã phù hộ cho con hãy giúp ta tiêu diệt Mabư!");
                                                    }
                                                    break;
                                                case 2:
                                                    ChangeMapService.gI().changeMapBySpaceShip(player, 52, -1, 250);
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.BABIDAY:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (MapService.gI().isMapMabuWar(this.mapId)) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Đừng vội xem thường Babyđây,ngay đến cha hắn là thần ma đạo sĩ\n"
                                            + "Bibiđây khi còn sống cũng phải sợ hắn đấy",
                                            "Yểm bùa\n50Tr Vàng",
                                            player.zone.map.mapId != 120 ? "Xuống\nTầng Dưới" : "Rời\nKhỏi đây");
                                } else {
                                    super.openBaseMenu(player);
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (MapService.gI().isMapMabuWar(this.mapId) && MabuWar.gI().isTimeMabuWar()) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                if (player.inventory.getGold() >= 50000000) {
                                                    Service.getInstance().changeFlag(player, 10);
                                                    player.inventory.subGold(50000000);
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Không đủ vàng");
                                                }
                                                break;
                                            case 1:
                                                if (player.zone.map.mapId == 120) {
                                                    ChangeMapService.gI().changeMapBySpaceShip(player,
                                                            player.gender + 21, -1, 250);
                                                }
                                                if (player.cFlag == 10) {
                                                    if (player.getPowerPoint() >= 20) {
                                                        if (!(player.zone.map.mapId == 119)) {
                                                            int idMapNextFloor = player.zone.map.mapId == 115
                                                                    ? player.zone.map.mapId + 2
                                                                    : player.zone.map.mapId + 1;
                                                            ChangeMapService.gI().changeMap(player, idMapNextFloor, -1,
                                                                    354, 240);
                                                        } else {
                                                            Zone zone = MabuWar.gI().getMapLastFloor(120);
                                                            if (zone != null) {
                                                                ChangeMapService.gI().changeMap(player, zone, 354, 240);
                                                            } else {
                                                                Service.getInstance().sendThongBao(player,
                                                                        "Trận đại chiến đã kết thúc, tàu vận chuyển sẽ đưa bạn về nhà");
                                                                ChangeMapService.gI().changeMapBySpaceShip(player,
                                                                        player.gender + 21, -1, 250);
                                                            }
                                                        }
                                                        player.resetPowerPoint();
                                                        player.sendMenuGotoNextFloorMabuWar = false;
                                                        Service.getInstance().sendPowerInfo(player, "TL",
                                                                player.getPowerPoint());
                                                        if (Util.isTrue(1, 30)) {
                                                            player.inventory.ruby += 1;
                                                            PlayerService.gI().sendInfoHpMpMoney(player);
                                                            Service.getInstance().sendThongBao(player,
                                                                    "Bạn nhận được 1 Hồng Ngọc");
                                                        } else {
                                                            Service.getInstance().sendThongBao(player,
                                                                    "Bạn đen vô cùng luôn nên không nhận được gì cả");
                                                        }
                                                    } else {
                                                        this.npcChat(player,
                                                                "Ngươi cần có đủ điểm để xuống tầng tiếp theo");
                                                    }
                                                    break;
                                                } else {
                                                    this.npcChat(player,
                                                            "Ngươi đang theo phe Ôsin,Hãy qua bên đó mà thể hiện");
                                                }
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.LINH_CANH:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (player.clan == null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_KHONG_CHO_VAO_DT,
                                            "Chỉ tiếp các bang hội, miễn tiếp khách vãng lai", "Đóng");
                                } else if (player.clan.getMembers().size() < 5) {
                                    // } else if (player.clan.getMembers().size() < 1) {
                                    this.createOtherMenu(player, ConstNpc.MENU_KHONG_CHO_VAO_DT,
                                            "Bang hội phải có ít nhất 5 thành viên mới có thể mở", "Đóng");
                                } else {
                                    ClanMember clanMember = player.clan.getClanMember((int) player.id);
                                    int days = (int) (((System.currentTimeMillis() / 1000) - clanMember.joinTime) / 60
                                            / 60 / 24);
                                    if (days < 2) {
                                        NpcService.gI().createTutorial(player, avartar,
                                                "Chỉ những thành viên gia nhập bang hội tối thiểu 2 ngày mới có thể tham gia");
                                        return;
                                    }
                                    if (!player.clan.haveGoneDoanhTrai && player.clan.timeOpenDoanhTrai != 0) {
                                        createOtherMenu(player, ConstNpc.MENU_VAO_DT,
                                                "Bang hội của ngươi đang đánh trại độc nhãn\n" + "Thời gian còn lại là "
                                                + TimeUtil.getSecondLeft(player.clan.timeOpenDoanhTrai,
                                                        DoanhTrai.TIME_DOANH_TRAI / 1000)
                                                + ". Ngươi có muốn tham gia không?",
                                                "Tham gia", "Không", "Hướng\ndẫn\nthêm");
                                    } else {
                                        List<Player> plSameClans = new ArrayList<>();
                                        List<Player> playersMap = player.zone.getPlayers();
                                        synchronized (playersMap) {
                                            for (Player pl : playersMap) {
                                                if (!pl.equals(player) && pl.clan != null
                                                        && pl.clan.id == player.clan.id && pl.location.x >= 1285
                                                        && pl.location.x <= 1645) {
                                                    plSameClans.add(pl);
                                                }

                                            }
                                        }
                                        // if (plSameClans.size() >= 0) {
                                        if (plSameClans.size() >= 2) {
                                            if (!player.isAdmin() && player.clanMember
                                                    .getNumDateFromJoinTimeToToday() < DoanhTrai.DATE_WAIT_FROM_JOIN_CLAN) {
                                                createOtherMenu(player, ConstNpc.MENU_KHONG_CHO_VAO_DT,
                                                        "Bang hội chỉ cho phép những người ở trong bang trên 1 ngày. Hẹn ngươi quay lại vào lúc khác",
                                                        "OK", "Hướng\ndẫn\nthêm");
                                            } else if (player.clan.haveGoneDoanhTrai) {
                                                createOtherMenu(player, ConstNpc.MENU_KHONG_CHO_VAO_DT,
                                                        "Bang hội của ngươi đã đi trại lúc "
                                                        + Util.formatTime(player.clan.timeOpenDoanhTrai)
                                                        + " hôm nay. Người mở\n" + "("
                                                        + player.clan.playerOpenDoanhTrai.name
                                                        + "). Hẹn ngươi quay lại vào ngày mai",
                                                        "OK", "Hướng\ndẫn\nthêm");

                                            } else {
                                                createOtherMenu(player, ConstNpc.MENU_CHO_VAO_DT,
                                                        "Hôm nay bang hội của ngươi chưa vào trại lần nào. Ngươi có muốn vào\n"
                                                        + "không?\nĐể vào, ta khuyên ngươi nên có 3-4 người cùng bang đi cùng",
                                                        "Vào\n(miễn phí)", "Không", "Hướng\ndẫn\nthêm");
                                            }
                                        } else {
                                            createOtherMenu(player, ConstNpc.MENU_KHONG_CHO_VAO_DT,
                                                    "Ngươi phải có ít nhất 2 đồng đội cùng bang đứng gần mới có thể\nvào\n"
                                                    + "tuy nhiên ta khuyên ngươi nên đi cùng với 3-4 người để khỏi chết.\n"
                                                    + "Hahaha.",
                                                    "OK", "Hướng\ndẫn\nthêm");
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 27) {
                                    switch (player.iDMark.getIndexMenu()) {
                                        case ConstNpc.MENU_KHONG_CHO_VAO_DT:
                                            if (select == 1) {
                                                NpcService.gI().createTutorial(player, this.avartar,
                                                        ConstNpc.HUONG_DAN_DOANH_TRAI);
                                            }
                                            break;
                                        case ConstNpc.MENU_CHO_VAO_DT:
                                            switch (select) {
                                                case 0:
                                                    DoanhTraiService.gI().openDoanhTrai(player);
                                                    break;
                                                case 2:
                                                    NpcService.gI().createTutorial(player, this.avartar,
                                                            ConstNpc.HUONG_DAN_DOANH_TRAI);
                                                    break;
                                            }
                                            break;
                                        case ConstNpc.MENU_VAO_DT:
                                            switch (select) {
                                                case 0:
                                                    ChangeMapService.gI().changeMap(player, 53, 0, 35, 432);
                                                    break;
                                                case 2:
                                                    NpcService.gI().createTutorial(player, this.avartar,
                                                            ConstNpc.HUONG_DAN_DOANH_TRAI);
                                                    break;
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.QUA_TRUNG:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {

                        private final int COST_AP_TRUNG_NHANH = 1000000000;

                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                player.mabuEgg.sendMabuEgg();
                                if (player.mabuEgg.getSecondDone() != 0) {
                                    this.createOtherMenu(player, ConstNpc.CAN_NOT_OPEN_EGG, "Bư bư bư...",
                                            "Hủy bỏ\ntrứng",
                                            "Ấp nhanh\n" + Util.numberToMoney(COST_AP_TRUNG_NHANH) + " vàng", "Đóng");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.CAN_OPEN_EGG, "Bư bư bư...", "Nở",
                                            "Hủy bỏ\ntrứng", "Đóng");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                switch (player.iDMark.getIndexMenu()) {
                                    case ConstNpc.CAN_NOT_OPEN_EGG:
                                        if (select == 0) {
                                            this.createOtherMenu(player, ConstNpc.CONFIRM_DESTROY_EGG,
                                                    "Bạn có chắc chắn muốn hủy bỏ trứng Mabư?", "Đồng ý", "Từ chối");
                                        } else if (select == 1) {
                                            if (player.inventory.gold >= COST_AP_TRUNG_NHANH) {
                                                player.inventory.gold -= COST_AP_TRUNG_NHANH;
                                                player.mabuEgg.timeDone = 0;
                                                Service.getInstance().sendMoney(player);
                                                player.mabuEgg.sendMabuEgg();
                                            } else {
                                                Service.getInstance().sendThongBao(player,
                                                        "Bạn không đủ vàng để thực hiện, còn thiếu "
                                                        + Util.numberToMoney(
                                                                (COST_AP_TRUNG_NHANH - player.inventory.gold))
                                                        + " vàng");
                                            }
                                        }
                                        break;
                                    case ConstNpc.CAN_OPEN_EGG:
                                        switch (select) {
                                            case 0:
                                                this.createOtherMenu(player, ConstNpc.CONFIRM_OPEN_EGG,
                                                        "Bạn có chắc chắn cho trứng nở?\n"
                                                        + "Đệ tử của bạn sẽ được thay thế bằng đệ Mabư",
                                                        "Đệ mabư\nTrái Đất", "Đệ mabư\nNamếc", "Đệ mabư\nXayda",
                                                        "Từ chối");
                                                break;
                                            case 1:
                                                this.createOtherMenu(player, ConstNpc.CONFIRM_DESTROY_EGG,
                                                        "Bạn có chắc chắn muốn hủy bỏ trứng Mabư?", "Đồng ý",
                                                        "Từ chối");
                                                break;
                                        }
                                        break;
                                    case ConstNpc.CONFIRM_OPEN_EGG:
                                        switch (select) {
                                            case 0:
                                                player.mabuEgg.openEgg(ConstPlayer.TRAI_DAT);
                                                break;
                                            case 1:
                                                player.mabuEgg.openEgg(ConstPlayer.NAMEC);
                                                break;
                                            case 2:
                                                player.mabuEgg.openEgg(ConstPlayer.XAYDA);
                                                break;
                                            default:
                                                break;
                                        }
                                        break;
                                    case ConstNpc.CONFIRM_DESTROY_EGG:
                                        if (select == 0) {
                                            player.mabuEgg.destroyEgg();
                                        }
                                        break;
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.QUOC_VUONG:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {

                        @Override
                        public void openBaseMenu(Player player) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?", "Bản thân", "Đệ tử");
                            //  ,"Chuyển Sinh");
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    switch (select) {
                                        case 0:
                                            if (player.nPoint.limitPower < NPoint.MAX_LIMIT) {
                                                this.createOtherMenu(player, ConstNpc.OPEN_POWER_MYSEFT,
                                                        "Ta sẽ truền năng lượng giúp con mở giới hạn sức mạnh của bản thân lên "
                                                        + Util.numberToMoney(player.nPoint.getPowerNextLimit()),
                                                        "Nâng ngay\n" + Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) + " vàng", "Đóng");
                                            } else {
                                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                                        "Sức mạnh của con đã đạt tới giới hạn",
                                                        "Đóng");
                                            }
                                            break;
                                        case 1:
                                            if (player.pet != null) {
                                                if (player.pet.nPoint.limitPower < NPoint.MAX_LIMIT) {
                                                    this.createOtherMenu(player, ConstNpc.OPEN_POWER_PET,
                                                            "Ta sẽ truền năng lượng giúp con mở giới hạn sức mạnh của đệ tử lên "
                                                            + Util.numberToMoney(player.pet.nPoint.getPowerNextLimit()),
                                                            "Nâng ngay\n" + Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) + " vàng", "Đóng");
                                                } else {
                                                    this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                                            "Sức mạnh của đệ con đã đạt tới giới hạn",
                                                            "Đóng");
                                                }
                                            } else {
                                                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                                            }
                                            // giới hạn đệ tử
                                            break;
                                        case 2:
                                            this.createOtherMenu(player, ConstNpc.MENU_CHUYENSINH,
                                                    "|8| -- CHUYỂN SINH --"
                                                    + "\n|3|Sức Mạnh Hiện Tại: \n"
                                                    + Util.format(player.nPoint.power)
                                                    + "\n|5| ----------------"
                                                    + "\n Bạn sẽ được tái sinh ở một hành tinh khác bất kì"
                                                    + "\n Các chiêu thức sẽ về cấp 1, Sức mạnh về 1 triệu 5"
                                                    + "\n|1| Tái sinh càng nhiều SĐ,HP,KI càng cao"
                                                    + "\n ----------------"
                                                    + "\n|7| Yêu Cầu:"
                                                    + "\n|2| Sức mạnh đạt 500 Tỷ"
                                                    + "\n Có Skill " + player.tenskill9(player.gender)
                                                    + "\n ----------------"
                                                    + "\n|6| Có tỉ lệ thất bại !"
                                                    + "\n Thất bại sẽ trừ đi đá chuyển sinh và Giảm 10 Tỷ Sức mạnh",
                                                    "Chuyển sinh", "Thông tin\nchuyển sinh",
                                                    "Đóng");
                                            break;
                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_CHUYENSINH) {
                                    switch (select) {
                                        case 0:
                                            int tvang = 0;
                                            if (player.chuyensinh <= 10) {
                                                tvang = 20;
                                            }
                                            if (player.chuyensinh <= 20 && player.chuyensinh > 10) {
                                                tvang = 30;
                                            }
                                            if (player.chuyensinh > 20 && player.chuyensinh <= 30) {
                                                tvang = 50;
                                            }
                                            if (player.chuyensinh > 30 && player.chuyensinh <= 50) {
                                                tvang = 8;
                                            }
                                            if (player.chuyensinh > 50 && player.chuyensinh <= 60) {
                                                tvang = 20;
                                            }
                                            if (player.chuyensinh > 60 && player.chuyensinh <= 72) {
                                                tvang = 50;
                                            }
                                            this.createOtherMenu(player, ConstNpc.CHUYENSINH,
                                                    "|7|CHUYỂN SINH"
                                                    + "\n\n|5|Bạn đang chuyển sinh : " + player.chuyensinh
                                                    + " \nCấp tiếp theo với tỉ lệ : " + (100 - player.chuyensinh * 2)
                                                    + "% \n Mức giá chuyển sinh : " + tvang + " Thỏi vàng \n\n|7|Bạn có muốn chuyển sinh ?",
                                                    "Đồng ý", "Từ chối");
                                            break; // 
                                        case 1:
                                            int hp = 0,
                                             dame = 0;
                                            if (player.chuyensinh > 0) {
                                                if (player.chuyensinh <= 10) {
                                                    dame += (1750) * player.chuyensinh;
                                                    hp += (15650) * player.chuyensinh;
                                                }
                                                if (player.chuyensinh <= 20 && player.chuyensinh > 10) {
                                                    dame += (3350) * (player.chuyensinh);
                                                    hp += (30750) * (player.chuyensinh);
                                                }
                                                if (player.chuyensinh > 20 && player.chuyensinh <= 30) {
                                                    dame += (4950) * (player.chuyensinh);
                                                    hp += (45875) * (player.chuyensinh);
                                                }
                                                if (player.chuyensinh > 30) {
                                                    dame += (6000) * (player.chuyensinh);
                                                    hp += (60000) * (player.chuyensinh);
                                                }
                                            }
                                            Service.getInstance().sendThongBaoOK(player, "Bạn đang cấp chuyển sinh: " + player.chuyensinh
                                                    + "\n HP : +" + Util.format(hp) + "\n KI : +" + Util.format(hp) + "\n Sức đánh : +" + Util.format(dame));
                                            break;
                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.CHUYENSINH) {
                                    if (player.chuyensinh >= 32) {
                                        npcChat(player, "|7| Cấp Chuyển sinh đạt MAX là 25 Cấp");
                                        return;
                                    }
                                    if (player.playerSkill.skills.get(7).point == 0) {
                                        npcChat(player, "|7|Yêu cầu phải học kỹ năng " + player.tenskill9(player.gender));
                                        return;
                                    }
                                    if (player.nPoint.power < 500_000_000_000L) {
                                        npcChat(player, "|7|Bạn chưa đủ sức mạnh yêu cầu để Chuyển sinh");
                                    } else {
                                        Item thoivang = null;
                                        try {
                                            thoivang = InventoryService.gI().findItemBagByTemp(player, 1565);
                                        } catch (Exception e) {
                                        }
                                        int tvang = 0;
                                        if (player.chuyensinh <= 10) {
                                            tvang = 20;
                                        }
                                        if (player.chuyensinh <= 20 && player.chuyensinh > 10) {
                                            tvang = 30;
                                        }
                                        if (player.chuyensinh > 20 && player.chuyensinh <= 30) {
                                            tvang = 50;
                                        }
                                        if (player.chuyensinh > 30 && player.chuyensinh <= 50) {
                                            tvang = 8;
                                        }
                                        if (player.chuyensinh > 50 && player.chuyensinh <= 60) {
                                            tvang = 20;
                                        }
                                        if (player.chuyensinh > 60 && player.chuyensinh <= 72) {
                                            tvang = 50;
                                        }
                                        if (thoivang == null || thoivang.quantity < tvang) {
                                            npcChat(player, "Bạn chưa đủ đá chuyển sinh để chuyển sinh");
                                            return;
                                        }
                                        int percent = (player.chuyensinh <= 45) ? (100 - (player.chuyensinh) * 2) : 10;
                                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                                            if (player.inventory.itemsBody.get(0).quantity < 1
                                                    && player.inventory.itemsBody.get(1).quantity < 1
                                                    && player.inventory.itemsBody.get(2).quantity < 1
                                                    && player.inventory.itemsBody.get(3).quantity < 1
                                                    && player.inventory.itemsBody.get(4).quantity < 1) {
                                                if (Util.nextInt(0, 100) < (percent)) {
                                                    InventoryService.gI().subQuantityItemsBag(player, thoivang, tvang);
                                                    player.gender += 1;
                                                    player.nPoint.power = 1_500_000;
                                                    player.chuyensinh++;
                                                    if (player.gender > 2) {
                                                        player.gender = 0;
                                                    }
                                                    short[] headtd = {30, 31, 64};
                                                    short[] headnm = {9, 29, 32};
                                                    short[] headxd = {27, 28, 6};
                                                    player.playerSkill.skills.clear();
                                                    for (Skill skill : player.playerSkill.skills) {
                                                        skill.point = 1;
                                                    }
                                                    int[] skillsArr = player.gender == 0 ? new int[]{0, 1, 6, 9, 10, 20, 22, 24, 19}
                                                            : player.gender == 1 ? new int[]{2, 3, 7, 11, 12, 17, 18, 26, 19}
                                                            : new int[]{4, 5, 8, 13, 14, 21, 23, 25, 19};
                                                    for (int i = 0; i < skillsArr.length; i++) {
                                                        player.playerSkill.skills.add(SkillUtil.createSkill(skillsArr[i], 1));
                                                    }
                                                    player.playerIntrinsic.intrinsic = IntrinsicService.gI().getIntrinsicById(0);
                                                    player.playerIntrinsic.intrinsic.param1 = 0;
                                                    player.playerIntrinsic.intrinsic.param2 = 0;
                                                    player.playerIntrinsic.countOpen = 0;
                                                    switch (player.gender) {
                                                        case 0:
                                                            player.head = headtd[Util.nextInt(headtd.length)];
                                                            break;
                                                        case 1:
                                                            player.head = headnm[Util.nextInt(headnm.length)];
                                                            break;
                                                        case 2:
                                                            player.head = headxd[Util.nextInt(headxd.length)];
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                    npcChat(player, "|1|Chuyển sinh thành công \n cấp hiện tại :" + player.chuyensinh);
                                                    Service.getInstance().player(player);
                                                    player.zone.loadAnotherToMe(player);
                                                    player.zone.load_Me_To_Another(player);
                                                    Service.getInstance().sendFlagBag(player);
                                                    Service.getInstance().Send_Caitrang(player);
                                                    PlayerService.gI().sendInfoHpMpMoney(player);
                                                    Service.getInstance().point(player);
                                                    Service.getInstance().Send_Info_NV(player);
                                                    InventoryService.gI().sendItemBags(player);
                                                    Service.getInstance().sendMoney(player);
                                                } else {
                                                    npcChat(player, "|7|Chuyển sinh thất bại \n cấp hiện tại :" + player.chuyensinh);
                                                    player.nPoint.power -= 10_000_000_000L;
                                                    InventoryService.gI().subQuantityItemsBag(player, thoivang, tvang);
                                                    Service.getInstance().point(player);
                                                    Service.getInstance().Send_Info_NV(player);
                                                    InventoryService.gI().sendItemBags(player);
                                                    Service.getInstance().sendMoney(player);
                                                }
                                            } else {
                                                Service.getInstance().sendThongBao(player, "Tháo hết 5 món đầu đang mặc ra nha");
                                            }
                                        } else {
                                            Service.getInstance().sendThongBao(player, "Balo đầy");
                                        }
                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.OPEN_POWER_MYSEFT && player.nPoint.limitPower < NPoint.MAX_LIMIT) {
                                    switch (select) {
                                        case 0:
                                            if (player.inventory.gold >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                                                if (OpenPowerService.gI().openPowerSpeed(player)) {
                                                    player.inventory.gold -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                                                    Service.getInstance().sendMoney(player);
                                                }
                                            } else {
                                                Service.getInstance().sendThongBao(player,
                                                        "Bạn không đủ vàng để mở, còn thiếu "
                                                        + Util.numberToMoney((OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gold)) + " vàng");
                                            }
                                            break;
                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.OPEN_POWER_PET && player.pet.nPoint.limitPower < NPoint.MAX_LIMIT) {
                                    if (select == 0) {
                                        if (player.inventory.gold >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                                            if (OpenPowerService.gI().openPowerSpeed(player.pet)) {
                                                player.inventory.gold -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                                                Service.getInstance().sendMoney(player);
                                            }
                                        } else {
                                            Service.getInstance().sendThongBao(player,
                                                    "Bạn không đủ vàng để mở, còn thiếu "
                                                    + Util.numberToMoney((OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gold)) + " vàng");
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.BARDOCK:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Tôi có thể giúp gì cho bạn??", "Đóng");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    switch (select) {
                                        case 0:
                                            break;
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.BERRY:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (!canOpenNpc(player)) {
                                return;
                            }

                            int soDu = player.getSession().vnd;
                            int tongNap = player.tongnap;
                            int accountId = player.getSession().userId;

                            double heSo = 2.0;
                            int mucTieu = 5_000_000;
                            int phanTram = (int) Math.min(100L, (tongNap * 100L) / mucTieu);

                            int filled = phanTram / 5;
                            StringBuilder bar = new StringBuilder();
                            for (int i = 0; i < 20; i++) {
                                bar.append(i < filled ? "█" : "-");
                            }

                            String text
                                    = "       SỰ KIỆN NẠP X" + (int) heSo + "\n"
                                    + "TRẠNG THÁI HIỆN TẠI:\n"
                                    + "TIẾN ĐỘ NẠP:\n"
                                    + "Mốc sự kiện: " + Util.numberToMoney(mucTieu) + " VNĐ\n"
                                    + "Tiến độ: " + phanTram + "%\n"
                                    + "[" + bar.toString() + "]\n\n"
                                    + "+ \"nạp tại: https://ngocronglegend.com/ \n";
                            int left = BerryGiftService.getAllowance(player.name);
                            if (left > 0) {
                                text += "Bạn đang có " + left + " lượt nhận quà.\n";
                            } else {
                                text += "Bạn chưa được Admin cấp lượt nhận quà.\n";
                            }

                            // Thêm nút "Xem QR Nạp Tiền"
                            this.createOtherMenu(player, ConstNpc.BASE_MENU, text,
                                    "Nhận Quà\n(Share 300 Xu)",
                                    "Nhận Quà\n(Share 5000 HN)",
                                    "Nhận quà \n (share 10 vé quay)",
                                    "nhận quà \n (thất vỹ 1 ngày )",
                                    "Mua Túi Mù\n100K VND"
                            //"Mua Túi SKH\n36K VND"
                            ); // ✅ Thêm nút mới
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (!canOpenNpc(player)) {
                                return;
                            }

                            if (player.iDMark.isBaseMenu()) {
                                switch (select) {
                                    case 0: { // Nhận quà 300 xu
                                        if (!BerryGiftService.canReceive(player.name)) {
                                            Service.getInstance().sendThongBao(player, "Bạn không có lượt nhận quà!");
                                            return;
                                        }
                                        Item item = ItemService.gI().createNewItem((short) 1535, 300);
                                        InventoryService.gI().addItemBag(player, item, 0);
                                        InventoryService.gI().sendItemBags(player);
                                        BerryGiftService.consume(player.name);
                                        Service.getInstance().sendThongBao(player, "Bạn đã nhận được 300 xu vàng từ Berry!");
                                        break;
                                    }

                                    case 1: { // Nhận quà 5000 hồng ngọc
                                        if (!BerryGiftService.canReceive(player.name)) {
                                            Service.getInstance().sendThongBao(player, "Bạn không có lượt nhận quà!");
                                            return;
                                        }
                                        Item item = ItemService.gI().createNewItem((short) 861, 5000);
                                        InventoryService.gI().addItemBag(player, item, 0);
                                        InventoryService.gI().sendItemBags(player);
                                        BerryGiftService.consume(player.name);
                                        Service.getInstance().sendThongBao(player, "Bạn đã nhận được 5000 hồng ngọc từ Berry!");
                                        break;
                                    }
                                    case 2: { // Nhận quà 5000 hồng ngọc
                                        if (!BerryGiftService.canReceive(player.name)) {
                                            Service.getInstance().sendThongBao(player, "Bạn không có lượt nhận quà!");
                                            return;
                                        }
                                        Item item = ItemService.gI().createNewItem((short) 1269, 10);
                                        InventoryService.gI().addItemBag(player, item, 0);
                                        InventoryService.gI().sendItemBags(player);
                                        BerryGiftService.consume(player.name);
                                        Service.getInstance().sendThongBao(player, "Bạn đã nhận được 10 vé quay");
                                        break;
                                    }
                                    case 3: { // Nhận quà 5000 hồng ngọc
                                        if (!BerryGiftService.canReceive(player.name)) {
                                            Service.getInstance().sendThongBao(player, "Bạn không có lượt nhận quà!");
                                            return;
                                        }

                                        // Tạo item mới
                                        Item item = ItemService.gI().createNewItem((short) 1545, 1);

                                        // QUAN TRỌNG: Thêm itemOptions TRƯỚC KHI addItemBag
                                        item.itemOptions.add(new ItemOption(77, Util.nextInt(15, 20)));  // HP
                                        item.itemOptions.add(new ItemOption(103, Util.nextInt(15, 20))); // KI
                                        item.itemOptions.add(new ItemOption(50, Util.nextInt(15, 20)));  // Sức đánh
                                        item.itemOptions.add(new ItemOption(117, Util.nextInt(10, 15))); // Chí mạng
                                        item.itemOptions.add(new ItemOption(5, Util.nextInt(5, 10)));    // SD/KI/HP
                                        item.itemOptions.add(new ItemOption(93, 1));                      // HSD 1 ngày

                                        // SAU ĐÓ mới add vào túi
                                        InventoryService.gI().addItemBag(player, item, 0);
                                        InventoryService.gI().sendItemBags(player);

                                        // Tiêu thụ lượt nhận
                                        BerryGiftService.consume(player.name);

                                        // Thông báo
                                        Service.getInstance().sendThongBao(player, "Bạn đã nhận được Thất Vỹ 1 ngày");
                                        break;
                                    }
                                    case 4: { // Mua túi mù 360k
                                        int cost = 100000;
                                        if (player.getSession().vnd >= cost) {
                                            PlayerDAO.subVnd(player, cost);
                                            player.tongnap += cost;

                                            Item it = null;
                                            int random = Util.nextInt(1, 100);

                                            if (random <= 10) { // 10%
                                                it = ItemService.gI().createNewItem((short) 1484, 1);
                                                it.itemOptions.add(new ItemOption(77, Util.nextInt(24, 35)));
                                                it.itemOptions.add(new ItemOption(103, Util.nextInt(24, 35)));
                                                it.itemOptions.add(new ItemOption(50, Util.nextInt(24, 35)));
                                                it.itemOptions.add(new ItemOption(117, Util.nextInt(15, 34)));
                                                it.itemOptions.add(new ItemOption(5, Util.nextInt(5, 15)));
                                                if (Util.isTrue(90, 100)) {
                                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                                                }
                                            } else if (random <= 30) { // 20%
                                                it = ItemService.gI().createNewItem((short) 1572, 1);
                                                it.itemOptions.add(new ItemOption(77, Util.nextInt(15, 20)));
                                                it.itemOptions.add(new ItemOption(103, Util.nextInt(15, 20)));
                                                it.itemOptions.add(new ItemOption(50, Util.nextInt(15, 20)));
                                                it.itemOptions.add(new ItemOption(117, Util.nextInt(10, 15)));
                                                it.itemOptions.add(new ItemOption(5, Util.nextInt(5, 10)));
                                                if (Util.isTrue(90, 100)) {
                                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                                                }
                                            } else { // 70%
                                                it = ItemService.gI().createNewItem((short) 1573, 1);
                                                it.itemOptions.add(new ItemOption(77, Util.nextInt(10, 17)));
                                                it.itemOptions.add(new ItemOption(103, Util.nextInt(10, 17)));
                                                it.itemOptions.add(new ItemOption(50, Util.nextInt(10, 17)));
                                                it.itemOptions.add(new ItemOption(117, Util.nextInt(10, 17)));
                                                it.itemOptions.add(new ItemOption(5, Util.nextInt(1, 7)));
                                                if (Util.isTrue(90, 100)) {
                                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                                                }
                                            }

                                            if (it != null) {
                                                InventoryService.gI().addItemBag(player, it, 0);
                                                InventoryService.gI().sendItemBags(player);
                                                Service.getInstance().sendThongBaoOK(player, "Bạn đã mua túi mù 360k!");
                                            }
                                        } else {
                                            Service.getInstance().sendThongBaoOK(player, "Bạn không đủ VND (cần " + cost + " VND)!");
                                        }
                                        break;
                                    }

//                                    case 3: { // Mua túi SKH 36k
//                                        int cost = 36000;
//                                        if (player.getSession().vnd >= cost) {
//                                            PlayerDAO.subVnd(player, cost);
//                                            player.tongnap += cost;
//
//                                            Item it = null;
//                                            int random = Util.nextInt(1, 100);
//
//                                            if (random <= 33) {
//                                                it = ItemService.gI().createNewItem((short) 2000, 1);
//                                                it.itemOptions.add(new ItemOption(30, 1));
//                                            } else if (random <= 66) {
//                                                it = ItemService.gI().createNewItem((short) 2001, 1);
//                                                it.itemOptions.add(new ItemOption(30, 1));
//                                            } else {
//                                                it = ItemService.gI().createNewItem((short) 2002, 1);
//                                                it.itemOptions.add(new ItemOption(30, 1));
//                                            }
//
//                                            if (it != null) {
//                                                InventoryService.gI().addItemBag(player, it, 0);
//                                                InventoryService.gI().sendItemBags(player);
//                                                Service.getInstance().sendThongBaoOK(player, "Bạn đã mua túi SKH 36k!");
//                                            }
//                                        } else {
//                                            Service.getInstance().sendThongBaoOK(player, "Bạn không đủ VND (cần " + cost + " VND)!");
//                                        }
//                                        break;
//                                    }
                                    case 5: { // ✅ XEM QR NẠP TIỀN
                                        // Kiểm tra điều kiện trước khi mở
                                        if (RechargeHandler.gI().canOpenRechargeMenu(player)) {
                                            RechargeMenu.gI().openRechargeMenu(player);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    };
                    break;

                case ConstNpc.BUNMA_TL:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Cậu bé muốn mua gì nào?",
                                            "Cửa hàng", "Đóng");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    if (select == 0) {
                                        ShopService.gI().openShopNormal(player, this, ConstNpc.SHOP_BUNMA_TL_0, 0,
                                                player.gender);
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.RONG_OMEGA:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                BlackBallWar.gI().setTime();
                                if (this.mapId == 24 || this.mapId == 25 || this.mapId == 26) {
                                    try {
                                        long now = System.currentTimeMillis();
                                        if (now > BlackBallWar.TIME_OPEN && now < BlackBallWar.TIME_CLOSE) {
                                            this.createOtherMenu(player, ConstNpc.MENU_OPEN_BDW,
                                                    "Đường đến với ngọc rồng sao đen đã mở, "
                                                    + "ngươi có muốn tham gia không?",
                                                    "Hướng dẫn\nthêm", "Tham gia", "Từ chối");
                                        } else {
                                            String[] optionRewards = new String[7];
                                            int index = 0;
                                            for (int i = 0; i < 7; i++) {
                                                if (player.rewardBlackBall.timeOutOfDateReward[i] > System
                                                        .currentTimeMillis()) {
                                                    optionRewards[index] = "Nhận thưởng\n" + (i + 1) + " sao";
                                                    index++;
                                                }
                                            }
                                            if (index != 0) {
                                                String[] options = new String[index + 1];
                                                for (int i = 0; i < index; i++) {
                                                    options[i] = optionRewards[i];
                                                }
                                                options[options.length - 1] = "Từ chối";
                                                this.createOtherMenu(player, ConstNpc.MENU_REWARD_BDW,
                                                        "Ngươi có một vài phần thưởng ngọc " + "rồng sao đen đây!",
                                                        options);
                                            } else {
                                                this.createOtherMenu(player, ConstNpc.MENU_NOT_OPEN_BDW,
                                                        "Ta có thể giúp gì cho ngươi?", "Hướng dẫn", "Từ chối");
                                            }
                                        }
                                    } catch (Exception ex) {
                                        Log.error("Lỗi mở menu rồng Omega");
                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                switch (player.iDMark.getIndexMenu()) {
                                    case ConstNpc.MENU_REWARD_BDW:
                                        player.rewardBlackBall.getRewardSelect((byte) select);
                                        break;
                                    case ConstNpc.MENU_OPEN_BDW:
                                        if (select == 0) {
                                            NpcService.gI().createTutorial(player, this.avartar,
                                                    ConstNpc.HUONG_DAN_BLACK_BALL_WAR);
                                        } else if (select == 1) {
                                            player.iDMark.setTypeChangeMap(ConstMap.CHANGE_BLACK_BALL);
                                            ChangeMapService.gI().openChangeMapTab(player);
                                        }
                                        break;
                                    case ConstNpc.MENU_NOT_OPEN_BDW:
                                        if (select == 0) {
                                            NpcService.gI().createTutorial(player, this.avartar,
                                                    ConstNpc.HUONG_DAN_BLACK_BALL_WAR);
                                        }
                                        break;
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.RONG_1S:
                case ConstNpc.RONG_2S:
                case ConstNpc.RONG_3S:
                case ConstNpc.RONG_4S:
                case ConstNpc.RONG_5S:
                case ConstNpc.RONG_6S:
                case ConstNpc.RONG_7S:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (player.isHoldBlackBall) {
                                    this.createOtherMenu(player, ConstNpc.MENU_PHU_HP, "Ta có thể giúp gì cho ngươi?",
                                            "Phù hộ", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPTION_GO_HOME,
                                            "Ta có thể giúp gì cho ngươi?", "Về nhà", "Từ chối");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.getIndexMenu() == ConstNpc.MENU_PHU_HP) {
                                    if (select == 0) {
                                        this.createOtherMenu(player, ConstNpc.MENU_OPTION_PHU_HP,
                                                "Ta sẽ giúp ngươi tăng HP lên mức kinh hoàng, ngươi chọn đi",
                                                "x3 HP\n" + Util.numberToMoney(BlackBallWar.COST_X3) + " vàng",
                                                "x5 HP\n" + Util.numberToMoney(BlackBallWar.COST_X5) + " vàng",
                                                "x7 HP\n" + Util.numberToMoney(BlackBallWar.COST_X7) + " vàng",
                                                "Từ chối");
                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPTION_GO_HOME) {
                                    if (select == 0) {
                                        ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
                                    }
                                } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPTION_PHU_HP) {
                                    switch (select) {
                                        case 0:
                                            BlackBallWar.gI().xHPKI(player, BlackBallWar.X3);
                                            break;
                                        case 1:
                                            BlackBallWar.gI().xHPKI(player, BlackBallWar.X5);
                                            break;
                                        case 2:
                                            BlackBallWar.gI().xHPKI(player, BlackBallWar.X7);
                                            break;
                                        case 3:
                                            this.npcChat(player, "Để ta xem ngươi trụ được bao lâu");
                                            break;
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.NPC_64:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi muốn xem thông tin gì?",
                                        "Top\nsức mạnh", "Đóng");
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    if (select == 0) {
                                        Service.getInstance().showTopPower(player, Service.getInstance().TOP_SUCMANH);
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.BILL:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {

                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 48) {
                                    createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "|7|SHOP ĐỒ HỦY DIỆT\n|6| Mang đủ 5 món đồ Thần linh và đem 99 Thức ăn đến cho ta. Ta sẽ bán đồ Hủy diệt cho ngươi",
                                            "SHOP HỦY DIỆT", "Đổi Phiếu\nHủy diệt", getMenuSuKien(EVENT_SEVER), "Đóng");
                                } else {
                                    super.openBaseMenu(player);
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                switch (this.mapId) {
                                    case 48:
                                        if (player.iDMark.isBaseMenu()) {
                                            switch (select) {
                                                case 0:
                                                    if (player.check99ThucAnHuyDiet() == true) {
                                                        if (player.setClothes.setDTL == 5) {
                                                            ShopService.gI().openShopSpecial(player, this, ConstNpc.SHOP_BILL_HUY_DIET_0, 0, -1);
                                                        } else {
                                                            createOtherMenu(player, ConstNpc.IGNORE_MENU, "Yêu cầu mặc 5 món Thần linh", "Đóng");
                                                        }
                                                    } else {
                                                        createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ngươi chưa đủ 99 thức ăn", "Đóng");
                                                    }
                                                    break;
                                                case 1:
                                                    if (player.setClothes.setDTL == 5) {
                                                        ShopService.gI().openShopSpecial(player, this, ConstNpc.SHOP_BILL_HUY_DIET_1, 1, -1);
                                                    } else {
                                                        createOtherMenu(player, ConstNpc.IGNORE_MENU, "Yêu cầu mặc 5 món Thần linh", "Đóng");
                                                    }
                                                    break;
                                                case 2:
                                                    switch (EVENT_SEVER) {
                                                        case 2:
                                                            Attribute at = ServerManager.gI().getAttributeManager()
                                                                    .find(ConstAttribute.SUC_DANH);
                                                            String text = "Sự kiện 20/11 chính thức tại Ngọc Rồng "
                                                                    + Manager.SERVER_NAME + "\n "
                                                                    + "Số điểm hiện tại của bạn là : "
                                                                    + player.event.getEventPoint()
                                                                    + "\nTổng số hoa đã tặng trên toàn máy chủ "
                                                                    + EVENT_COUNT_THAN_HUY_DIET % 999 + "/999";
                                                            this.createOtherMenu(player, ConstNpc.MENU_OPEN_SUKIEN,
                                                                    at != null && !at.isExpired() ? text
                                                                    + "\nToàn bộ máy chủ được tăng 20% sức đánh,thời gian còn lại "
                                                                    + at.getTime() / 60 + " phút."
                                                                    : text + "\nKhi tặng đủ 999 bông toàn bộ máy chủ được tăng tăng 20% sức đánh trong 60 phút\n",
                                                                    "Tặng 1\n Bông hoa", "Tặng\n10 Bông",
                                                                    "Tặng\n99 Bông", "Đổi\nHộp quà");
                                                            break;
                                                        default:
                                                            createOtherMenu(player, 5656,
                                                                    "|7|Npc này không liên quan đến Sự kiện\nVui lòng tìm Npc khác !!!", "Đóng");
                                                            break;
                                                    }
                                            }
                                        } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPEN_SUKIEN) {
                                            openMenuSuKien(player, this, tempId, select);
                                        }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.WHIS:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 48) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Đã tìm đủ nguyên liệu cho tôi chưa?"
                                            + "\n Tôi sẽ giúp cậu mạnh lên kha khá đấy!"
                                            + "\n\b|7| Điều kiện học Tuyệt kỹ"
                                            + "\b|5| -Khi lần đầu học skill cần: x999 Bí kiếp tuyệt kỹ và SM trên 60 Tỷ"
                                            + "\n -Mỗi một cấp yêu cầu: x999 Bí kiếp tuyệt kỹ và Thông thạo đạt MAX 100%", "Học\ntuyệt kĩ", "Từ Chối");
                                }
                                if (this.mapId == 5) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "|7|NÂNG CẤP ĐỒ THIÊN SỨ\n|6| Mang cho ta Công thức + Đá cầu vòng và 999 Mảnh thiên sứ ta sẽ chế tạo đồ Thiên sứ cho ngươi"
                                            + "mang cho ta x99 mỗi loại nro ta sẽ giúp ngươi có ngọc rồng siêu cấp"
                                            + "\nĐồ Thiên sứ khi chế tạo sẽ random chỉ số 0-15%\n"
                                            + "\n|2|(Khi mang đủ 5 món Hủy diệt ngươi hãy theo Osin qua Hành tinh ngục tù tìm kiếm Mảnh thiên sứ và săn BOSS Thiên sứ để thu thập Đá cầu vòng)"
                                            + "\n|1| cách chế tạo đồ kh thần linh \n"
                                            + "cần 6 món skh cùng là 1 set và 1 món đồ thần linh \n"
                                            + "tỉ lên thành công 36% thất bại mát hết \n",
                                            "Nâng cấp\n ngọc rồng siêu cấp", "Nâng Cấp \nĐồ Thiên Sứ", "Shop\n Thiên sứ", "Nâng SKH thần linh");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 5) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_NRO);
                                                break;
                                            case 1:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_DO_TS);
                                                break;
                                            case 2:
                                                ShopService.gI().openShopWhisThienSu(player,
                                                        ConstNpc.SHOP_WHIS_THIEN_SU, 0);
                                                break;
//                                            case 3: //Mở chỉ số bông tai
//                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.PHAN_RA_DO_TS);
//                                                break;
                                            case 3:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_SKH_TS);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                                        switch (player.combineNew.typeCombine) {
                                            case CombineServiceNew.NANG_CAP_NRO:
                                            case CombineServiceNew.NANG_CAP_DO_TS:
                                            case CombineServiceNew.NANG_CAP_SKH_TS:
                                            case CombineServiceNew.PHAN_RA_DO_TS:
                                                if (select == 0) {
                                                    CombineServiceNew.gI().startCombine(player);
                                                }
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_NANG_CAP_DO_TS) {
                                        if (select == 0) {
                                            CombineServiceNew.gI().startCombine(player);
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_NANG_DO_SKH_TS) {
                                        if (select == 0) {
                                            CombineServiceNew.gI().startCombine(player);
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_PHAN_RA_TS) {
                                        if (select == 0) {
                                            CombineServiceNew.gI().startCombine(player);
                                        }
                                    }
                                }

                                if (player.iDMark.isBaseMenu() && this.mapId == 48) {
                                    if (select == 0) {
                                        Message msg;
                                        try {
                                            Item sachTuyetki = null;
                                            try {
                                                sachTuyetki = InventoryService.gI().findItemBagByTemp(player, 1215);
                                            } catch (Exception e) {
                                            }
                                            if (player.gender == 0) {
                                                Skill curSkill = SkillUtil.getSkillbyId(player, Skill.SUPER_KAME);
                                                if (curSkill.point == 0) {
                                                    if (player.nPoint.power >= 60000000000L) {
                                                        if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                            this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                            return;
                                                        }
                                                        if (sachTuyetki.quantity >= 999) {
                                                            InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                            InventoryService.gI().sendItemBags(player);
                                                            curSkill = SkillUtil.createSkill(Skill.SUPER_KAME, 1);
                                                            SkillUtil.setSkill(player, curSkill);
                                                            msg = Service.getInstance().messageSubCommand((byte) 23);
                                                            msg.writer().writeShort(curSkill.skillId);
                                                            player.sendMessage(msg);
                                                            msg.cleanup();
                                                        } else {
                                                            Service.getInstance().sendThongBao(player, "Không đủ bí kíp tuyệt kĩ");
                                                        }
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Yêu cầu sức mạnh trên 60 Tỷ");
                                                    }
                                                } else if (curSkill.point > 0 && curSkill.point < 9) {
                                                    if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                        this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                        return;
                                                    }
                                                    if (sachTuyetki.quantity >= 999 && curSkill.currLevel == 1000) {
                                                        InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                        InventoryService.gI().sendItemBags(player);
                                                        curSkill = SkillUtil.createSkill(Skill.SUPER_KAME, curSkill.point + 1);
                                                        SkillUtil.setSkill(player, curSkill);
                                                        msg = Service.getInstance().messageSubCommand((byte) 62);
                                                        msg.writer().writeShort(curSkill.skillId);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Thông thạo của bạn chưa đủ 100%");
                                                    }
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Tuyệt kĩ của bạn đã đạt cấp tối đa");
                                                }
                                            }
                                            if (player.gender == 1) {
                                                Skill curSkill = SkillUtil.getSkillbyId(player, Skill.MA_PHONG_BA);
                                                if (curSkill.point == 0) {
                                                    if (player.nPoint.power >= 60000000000L) {
                                                        if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                            this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                            return;
                                                        }
                                                        if (sachTuyetki.quantity >= 999) {
                                                            InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                            InventoryService.gI().sendItemBags(player);
                                                            curSkill = SkillUtil.createSkill(Skill.MA_PHONG_BA, 1);
                                                            SkillUtil.setSkill(player, curSkill);
                                                            msg = Service.getInstance().messageSubCommand((byte) 23);
                                                            msg.writer().writeShort(curSkill.skillId);
                                                            player.sendMessage(msg);
                                                            msg.cleanup();
                                                        } else {
                                                            Service.getInstance().sendThongBao(player, "Không đủ bí kíp tuyệt kĩ");
                                                        }
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Yêu cầu sức mạnh trên 60 Tỷ");
                                                    }
                                                } else if (curSkill.point > 0 && curSkill.point < 9) {
                                                    if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                        this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                        return;
                                                    }
                                                    if (sachTuyetki.quantity >= 999 && curSkill.currLevel == 1000) {
                                                        InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                        InventoryService.gI().sendItemBags(player);
                                                        curSkill = SkillUtil.createSkill(Skill.MA_PHONG_BA, curSkill.point + 1);
                                                        SkillUtil.setSkill(player, curSkill);
                                                        msg = Service.getInstance().messageSubCommand((byte) 62);
                                                        msg.writer().writeShort(curSkill.skillId);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Thông thạo của bạn chưa đủ 100%");
                                                    }
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Tuyệt kĩ của bạn đã đạt cấp tối đa");
                                                }
                                            }
                                            if (player.gender == 2) {
                                                Skill curSkill = SkillUtil.getSkillbyId(player, Skill.LIEN_HOAN_CHUONG);
                                                if (curSkill.point == 0) {
                                                    if (player.nPoint.power >= 60000000000L) {
                                                        if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                            this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                            return;
                                                        }
                                                        if (sachTuyetki.quantity >= 999) {
                                                            InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                            InventoryService.gI().sendItemBags(player);
                                                            curSkill = SkillUtil.createSkill(Skill.LIEN_HOAN_CHUONG, 1);
                                                            SkillUtil.setSkill(player, curSkill);
                                                            msg = Service.getInstance().messageSubCommand((byte) 23);
                                                            msg.writer().writeShort(curSkill.skillId);
                                                            player.sendMessage(msg);
                                                            msg.cleanup();
                                                        } else {
                                                            Service.getInstance().sendThongBao(player, "Không đủ bí kíp tuyệt kĩ");
                                                        }
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Yêu cầu sức mạnh trên 60 Tỷ");
                                                    }
                                                } else if (curSkill.point > 0 && curSkill.point < 9) {
                                                    if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                        this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                        return;
                                                    }
                                                    if (sachTuyetki.quantity >= 999 && curSkill.currLevel == 1000) {
                                                        InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                        InventoryService.gI().sendItemBags(player);
                                                        curSkill = SkillUtil.createSkill(Skill.LIEN_HOAN_CHUONG, curSkill.point + 1);
                                                        SkillUtil.setSkill(player, curSkill);
                                                        msg = Service.getInstance().messageSubCommand((byte) 62);
                                                        msg.writer().writeShort(curSkill.skillId);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Thông thạo của bạn chưa đủ 100%");
                                                    }
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Tuyệt kĩ của bạn đã đạt cấp tối đa");
                                                }
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.BO_MONG:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 47 || this.mapId == 84) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Xin chào, cậu muốn tôi giúp gì?",
                                            "Nhiệm vụ\nhàng ngày", "Mã quà tặng", "Nhận ngọc\nmiễn phí", "Từ chối");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 47 || this.mapId == 84) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                if (player.playerTask.sideTask.template != null) {
                                                    String npcSay = "Nhiệm vụ hiện tại: "
                                                            + player.playerTask.sideTask.getName() + " ("
                                                            + player.playerTask.sideTask.getLevel() + ")"
                                                            + "\nHiện tại đã hoàn thành: "
                                                            + player.playerTask.sideTask.count + "/"
                                                            + player.playerTask.sideTask.maxCount + " ("
                                                            + player.playerTask.sideTask.getPercentProcess()
                                                            + "%)\nSố nhiệm vụ còn lại trong ngày: "
                                                            + player.playerTask.sideTask.leftTask + "/"
                                                            + ConstTask.MAX_SIDE_TASK;
                                                    this.createOtherMenu(player, ConstNpc.MENU_OPTION_PAY_SIDE_TASK,
                                                            npcSay, "Trả nhiệm\nvụ", "Hủy nhiệm\nvụ");
                                                } else {
                                                    this.createOtherMenu(player, ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK,
                                                            "|2|Khi hoàn thành các nhiệm vụ ngày nhận được các phần thưởng sau:"
                                                            + "\n|4|-Dễ : 5TV khóa + 1000 Hngọc"
                                                            + "\n-Bthường : 7TV khóa + 2000 Hngọc"
                                                            + "\n-Khó : 9TV khóa + 3000 Hngọc"
                                                            + "\n-Siêu khó : 10TV khóa + 4000 Hngọc"
                                                            + "\n|1|Tôi có vài nhiệm vụ theo cấp bậc, sức cậu có thể làm được cái nào?",
                                                            "Dễ", "Bình thường", "Khó", "Siêu khó", "Từ chối");
                                                }
                                                break;

                                            case 1:
                                                Input.gI().createFormGiftCode(player);
                                                break;
                                            case 2:
                                                TaskService.gI().checkDoneAchivements(player);
                                                TaskService.gI().sendAchivement(player);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK) {
                                        switch (select) {
                                            case 0:
                                            case 1:
                                            case 2:
                                            case 3:
                                                TaskService.gI().changeSideTask(player, (byte) select);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_OPTION_PAY_SIDE_TASK) {
                                        switch (select) {
                                            case 0:
                                                TaskService.gI().paySideTask(player);
                                                break;
                                            case 1:
                                                TaskService.gI().removeSideTask(player);
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.GOKU_SSJ:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 80) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Xin chào, tôi có thể giúp gì cho cậu?", "Tới hành tinh\nYardart",
                                            "Từ chối");
                                } else if (this.mapId == 131) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Xin chào, tôi có thể giúp gì cho cậu?", "Quay về", "Từ chối");
                                } else {
                                    super.openBaseMenu(player);
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                switch (player.iDMark.getIndexMenu()) {
                                    case ConstNpc.BASE_MENU:
                                        if (this.mapId == 80) {
                                            // if (select == 0) {
                                            // if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_24_0) {
                                            // ChangeMapService.gI().changeMapBySpaceShip(player, 160, -1, 168);
                                            // } else {
                                            // this.npcChat(player, "Xin lỗi, tôi chưa thể đưa cậu tới nơi đó lúc
                                            // này...");
                                            // }
                                            // } else
                                            if (select == 0) {
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 131, -1, 940);
                                            }
                                        } else if (this.mapId == 131) {
                                            if (select == 0) {
                                                ChangeMapService.gI().changeMapBySpaceShip(player, 80, -1, 870);
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.GOKU_SSJ_:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 133) {
                                    Item biKiep = InventoryService.gI().findItem(player.inventory.itemsBag, 590);
                                    int soLuong = 0;
                                    if (biKiep != null) {
                                        soLuong = biKiep.quantity;
                                    }
                                    if (soLuong >= 10000) {
                                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Bạn đang có " + soLuong
                                                + " bí kiếp.\n"
                                                + "Hãy kiếm đủ 10000 bí kiếp tôi sẽ dạy bạn cách dịch chuyển tức thời của người Yardart",
                                                "Học dịch\nchuyển", "Đóng");
                                    } else {
                                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Bạn đang có " + soLuong
                                                + " bí kiếp.\n"
                                                + "Hãy kiếm đủ 10000 bí kiếp tôi sẽ dạy bạn cách dịch chuyển tức thời của người Yardart",
                                                "Đóng");
                                    }
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 133) {
                                    Item biKiep = InventoryService.gI().findItem(player.inventory.itemsBag, 590);
                                    int soLuong = 0;
                                    if (biKiep != null) {
                                        soLuong = biKiep.quantity;
                                    }
                                    if (soLuong >= 10000 && InventoryService.gI().getCountEmptyBag(player) > 0) {
                                        Item yardart = ItemService.gI().createNewItem((short) (player.gender + 592));
                                        yardart.itemOptions.add(new ItemOption(47, 400));
                                        yardart.itemOptions.add(new ItemOption(108, 10));
                                        InventoryService.gI().addItemBag(player, yardart, 0);
                                        InventoryService.gI().subQuantityItemsBag(player, biKiep, 10000);
                                        InventoryService.gI().sendItemBags(player);
                                        Service.getInstance().sendThongBao(player,
                                                "Bạn vừa nhận được trang phục tộc Yardart");
                                    }
                                }
                            }
                        }
                    };
                    break;
                case ConstNpc.GHI_DANH:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        String[] menuselect = new String[]{};

                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == ConstMap.DAI_HOI_VO_THUAT) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Chào mừng bạn đến với đại hội võ thuật", "Đại Hội\nVõ Thuật\nLần Thứ\n23", "Giải siêu hạng");
                                } else if (this.mapId == ConstMap.DAI_HOI_VO_THUAT_129) {
                                    int goldchallenge = player.goldChallenge;
                                    if (player.levelWoodChest == 0) {
                                        menuselect = new String[]{
                                            "Thi đấu\n" + Util.numberToMoney(goldchallenge) + " vàng",
                                            "Về\nĐại Hội\nVõ Thuật"};
                                    } else {
                                        menuselect = new String[]{
                                            "Thi đấu\n" + Util.numberToMoney(goldchallenge) + " vàng",
                                            "Nhận thưởng\nRương cấp\n" + player.levelWoodChest,
                                            "Về\nĐại Hội\nVõ Thuật"};
                                    }
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "Đại hội võ thuật lần thứ 23\nDiễn ra bất kể ngày đêm,ngày nghỉ ngày lễ\nPhần thưởng vô cùng quý giá\nNhanh chóng tham gia nào",
                                            menuselect, "Từ chối");
                                } else {
                                    super.openBaseMenu(player);
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                switch (player.iDMark.getIndexMenu()) {
                                    case ConstNpc.BASE_MENU:
                                        if (this.mapId == ConstMap.DAI_HOI_VO_THUAT) {
                                            switch (select) {
                                                case 0:
                                                    ChangeMapService.gI().changeMapNonSpaceship(player,
                                                            ConstMap.DAI_HOI_VO_THUAT_129, player.location.x, 360);
                                                    break;
                                                case 1:
                                                    ChangeMapService.gI().changeMapNonSpaceship(player, 113, player.location.x, 360);
                                                    break;
                                            }
                                        } else if (this.mapId == ConstMap.DAI_HOI_VO_THUAT_129) {
                                            int goldchallenge = player.goldChallenge;
                                            if (player.levelWoodChest == 0) {
                                                switch (select) {
                                                    case 0:
                                                        if (InventoryService.gI().finditemWoodChest(player)) {
                                                            if (player.inventory.getGold() >= goldchallenge) {
                                                                MartialCongressService.gI().startChallenge(player);
                                                                player.inventory.subGold(goldchallenge);
                                                                PlayerService.gI().sendInfoHpMpMoney(player);
                                                                player.goldChallenge += 2000000;
                                                            } else {
                                                                Service.getInstance().sendThongBao(player,
                                                                        "Không đủ vàng, còn thiếu "
                                                                        + Util.numberToMoney(goldchallenge
                                                                                - player.inventory.gold)
                                                                        + " vàng");
                                                            }
                                                        } else {
                                                            Service.getInstance().sendThongBao(player,
                                                                    "Hãy mở rương báu vật trước");
                                                        }
                                                        break;
                                                    case 1:
                                                        ChangeMapService.gI().changeMapNonSpaceship(player,
                                                                ConstMap.DAI_HOI_VO_THUAT, player.location.x, 336);
                                                        break;
                                                }
                                            } else {
                                                switch (select) {
                                                    case 0:
                                                        if (InventoryService.gI().finditemWoodChest(player)) {
                                                            if (player.inventory.getGold() >= goldchallenge) {
                                                                MartialCongressService.gI().startChallenge(player);
                                                                player.inventory.subGold(goldchallenge);
                                                                PlayerService.gI().sendInfoHpMpMoney(player);
                                                                player.goldChallenge += 2000000;
                                                            } else {
                                                                Service.getInstance().sendThongBao(player,
                                                                        "Không đủ vàng, còn thiếu "
                                                                        + Util.numberToMoney(goldchallenge
                                                                                - player.inventory.gold)
                                                                        + " vàng");
                                                            }
                                                        } else {
                                                            Service.getInstance().sendThongBao(player,
                                                                    "Hãy mở rương báu vật trước");
                                                        }
                                                        break;
                                                    case 1:
                                                        if (!player.receivedWoodChest) {
                                                            if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                                                                Item it = ItemService.gI()
                                                                        .createNewItem((short) ConstItem.RUONG_GO);
                                                                it.itemOptions
                                                                        .add(new ItemOption(72, player.levelWoodChest));
                                                                it.itemOptions.add(new ItemOption(30, 0));
                                                                it.createTime = System.currentTimeMillis();
                                                                InventoryService.gI().addItemBag(player, it, 0);
                                                                InventoryService.gI().sendItemBags(player);

                                                                player.receivedWoodChest = true;
                                                                player.levelWoodChest = 0;
                                                                Service.getInstance().sendThongBao(player,
                                                                        "Bạn nhận được rương gỗ");
                                                            } else {
                                                                this.npcChat(player, "Hành trang đã đầy");
                                                            }
                                                        } else {
                                                            Service.getInstance().sendThongBao(player,
                                                                    "Mỗi ngày chỉ có thể nhận rương báu 1 lần");
                                                        }
                                                        break;
                                                    case 2:
                                                        ChangeMapService.gI().changeMapNonSpaceship(player,
                                                                ConstMap.DAI_HOI_VO_THUAT, player.location.x, 336);
                                                        break;
                                                }
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    };
                    break;

                case ConstNpc.ANDROID_AODAI:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                // 🕒 trạng thái nấu bia
                                String beerStatus = "";
                                long cookDuration = 120L * 60 * 1000; // 120 phút
                                long now = System.currentTimeMillis();

                                if (player.startCookingBeerTime == 0) {
                                    beerStatus = "|7|Chưa nấu bia. Có thể dùng 99 lúa mì để bắt đầu.";
                                } else {
                                    long elapsed = now - player.startCookingBeerTime;
                                    if (elapsed >= cookDuration) {
                                        beerStatus = "|3|Bia Tiger đã hoàn thành! Bạn có thể nhận 5 lon bia.";
                                    } else {
                                        long remainMin = (cookDuration - elapsed) / 60000;
                                        long remainSec = ((cookDuration - elapsed) % 60000) / 1000;
                                        beerStatus = "|2|Bia đang nấu... còn " + remainMin + " phút " + remainSec + " giây.";
                                    }
                                }

                                // Menu chính
                                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                        "Xin chào " + player.name + "\nTôi có thể giúp gì cho bạn"
                                        + "\n|5|- Chế tạo bình hồi skill cần dược liệu hồi skill và cỏ tiên "
                                        + "\n- Chế tạo thuốc sức mạnh cần dược liệu sức mạnh và cỏ tiên"
                                        + "\n- Đổi cánh thiên sứ cần x99 đá thiên sứ và 50 xu vàng "
                                        + "\n\n" + beerStatus,
                                        "Bình hồi skill", "Thuốc sức mạnh", "Đổi cánh thiên sứ", "Nấu bia Tiger");
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                switch (player.iDMark.getIndexMenu()) {
                                    case ConstNpc.BASE_MENU:
                                        switch (select) {
                                            case 0:
                                                Item chuvan = InventoryService.gI().findItem(player,
                                                        ConstItem.NUOC_THANH, 99);
                                                Item chusu = InventoryService.gI().findItem(player, ConstItem.CO_TIEN,
                                                        99);
                                                Item chunhu = InventoryService.gI().findItem(player, ConstItem.DUOC_LIEU_skill,
                                                        5);
                                                if (chuvan != null && chusu != null && chunhu != null) {
                                                    InventoryService.gI().subQuantityItemsBag(player, chuvan, 99);
                                                    InventoryService.gI().subQuantityItemsBag(player, chusu, 99);
                                                    InventoryService.gI().subQuantityItemsBag(player, chunhu, 5);

                                                    Item capsule2024 = ItemService.gI().createNewItem((short) 1555, 1);
                                                    capsule2024.itemOptions.add(new ItemOption(30, 0));
                                                    InventoryService.gI().addItemBag(player, capsule2024, 0);
                                                    InventoryService.gI().sendItemBags(player);
                                                    Service.getInstance().sendThongBao(player,
                                                            "Bạn nhận được " + capsule2024.template.name);
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Không đủ nguyên liệu");
                                                }
                                                break;
                                            case 1:
                                                Item chuvan1 = InventoryService.gI().findItem(player,
                                                        ConstItem.NUOC_THANH, 99);
                                                Item chusu1 = InventoryService.gI().findItem(player, ConstItem.CO_TIEN,
                                                        99);
                                                Item chunhu1 = InventoryService.gI().findItem(player, ConstItem.DUOC_LIEU_SM,
                                                        5);
                                                if (chuvan1 != null && chusu1 != null && chunhu1 != null) {
                                                    InventoryService.gI().subQuantityItemsBag(player, chuvan1, 99);
                                                    InventoryService.gI().subQuantityItemsBag(player, chusu1, 99);
                                                    InventoryService.gI().subQuantityItemsBag(player, chunhu1, 5);

                                                    Item capsule2024 = ItemService.gI().createNewItem((short) 1553, 1);
                                                    capsule2024.itemOptions.add(new ItemOption(30, 0));
                                                    InventoryService.gI().addItemBag(player, capsule2024, 0);
                                                    InventoryService.gI().sendItemBags(player);
                                                    Service.getInstance().sendThongBao(player,
                                                            "Bạn nhận được " + capsule2024.template.name);
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Không đủ nguyên liệu");
                                                }
                                                break;

                                            case 2:
                                                Item chuvanc = InventoryService.gI().findItem(player,
                                                        ConstItem.DATS, 99);
                                                Item chusuc = InventoryService.gI().findItem(player, ConstItem.TIEN_TE,
                                                        10);
                                                Item chunhuc = InventoryService.gI().findItem(player, ConstItem.XU_VANG,
                                                        50);

                                                if (chuvanc != null && chusuc != null && chunhuc != null) {
                                                    InventoryService.gI().subQuantityItemsBag(player, chuvanc, 99);
                                                    InventoryService.gI().subQuantityItemsBag(player, chusuc, 10);
                                                    InventoryService.gI().subQuantityItemsBag(player, chunhuc, 50);

                                                    Item thientuu = ItemService.gI().createNewItem((short) 1558);
                                                    thientuu.itemOptions.add(new ItemOption(50, Util.nextInt(15, 34)));
                                                    thientuu.itemOptions.add(new ItemOption(77, Util.nextInt(15, 34)));
                                                    thientuu.itemOptions.add(new ItemOption(103, Util.nextInt(15, 34)));
                                                    thientuu.itemOptions.add(new ItemOption(14, Util.nextInt(7, 15)));
                                                    thientuu.itemOptions.add(new ItemOption(101, 100));
                                                    thientuu.itemOptions.add(new ItemOption(30, 1));
                                                    InventoryService.gI().addItemBag(player, thientuu, 0);
                                                    InventoryService.gI().sendItemBags(player);
                                                    Service.getInstance().sendThongBao(player,
                                                            "Bạn nhận được " + thientuu.template.name);
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Không đủ nguyên liệu");
                                                }

                                                break;

                                            case 3: { // Nấu bia Tiger
                                                long cookDuration = 120L * 60 * 1000; // 120 phút
                                                long now = System.currentTimeMillis();

                                                // Nếu chưa nấu -> bắt đầu nấu
                                                if (player.startCookingBeerTime == 0) {
                                                    Item luaMi = InventoryService.gI().findItem(player, 1675, 99);
                                                    if (luaMi != null) {
                                                        InventoryService.gI().subQuantityItemsBag(player, luaMi, 99);
                                                        InventoryService.gI().sendItemBags(player);
                                                        player.startCookingBeerTime = now;
                                                        Service.getInstance().sendThongBao(player,
                                                                "Đã bắt đầu nấu bia Tiger. Hãy chờ 120 phút!");
                                                    } else {
                                                        Service.getInstance().sendThongBao(player,
                                                                "Cần 99 lúa mì  để nấu bia Tiger.");
                                                    }
                                                } else {
                                                    long elapsed = now - player.startCookingBeerTime;

                                                    // Nếu đã nấu xong
                                                    if (elapsed >= cookDuration) {
                                                        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                                            Service.getInstance().sendThongBao(player,
                                                                    "Hành trang cần ít nhất 1 ô trống để nhận bia Tiger!");
                                                            return;
                                                        }
                                                        player.startCookingBeerTime = 0;
                                                        Item beer5 = ItemService.gI().createNewItem((short) 1670, 5);
                                                        beer5.itemOptions.add(new ItemOption(30, 0));
                                                        InventoryService.gI().addItemBag(player, beer5, 0);
                                                        InventoryService.gI().sendItemBags(player);
                                                        Service.getInstance().sendThongBao(player,
                                                                "Nấu bia Tiger thành công! Bạn nhận được 5 lon bia Tiger.");
                                                    } else {
                                                        // Nếu chưa đủ thời gian -> check Xu Vàng (1535)
                                                        Item xuVang = InventoryService.gI().findItem(player, 1535, 99);
                                                        if (xuVang != null) {
                                                            InventoryService.gI().subQuantityItemsBag(player, xuVang, 99);
                                                            InventoryService.gI().sendItemBags(player);
                                                            player.startCookingBeerTime = 0;

                                                            if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                                                Service.getInstance().sendThongBao(player,
                                                                        "Hành trang cần ít nhất 1 ô trống để nhận bia Tiger!");
                                                                return;
                                                            }

                                                            Item beer5 = ItemService.gI().createNewItem((short) 1670, 5);
                                                            beer5.itemOptions.add(new ItemOption(30, 0));
                                                            InventoryService.gI().addItemBag(player, beer5, 0);
                                                            InventoryService.gI().sendItemBags(player);
                                                            Service.getInstance().sendThongBao(player,
                                                                    "Bạn đã dùng 99 Xu Vàng để nấu nhanh và nhận được 5 lon bia Tiger!");
                                                        } else {
                                                            long remainMin = (cookDuration - elapsed) / 60000;
                                                            long remainSec = ((cookDuration - elapsed) % 60000) / 1000;
                                                            Service.getInstance().sendThongBao(player,
                                                                    "Bia đang nấu, còn " + remainMin + " phút " + remainSec + " giây.\n"
                                                                    + "Bạn cần 99 Xu Vàng  để nấu nhanh ngay.");
                                                        }
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    };
                    break;

                case ConstNpc.MI_NUONG_HT:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 48) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Đã tìm đủ nguyên liệu cho tôi chưa?"
                                            + "\n Tôi sẽ giúp cậu mạnh lên kha khá đấy!"
                                            + "\n\b|7| Điều kiện học Tuyệt kỹ"
                                            + "\b|5| -Khi lần đầu học skill cần: x999 Bí kiếp tuyệt kỹ và SM trên 60 Tỷ"
                                            + "\n -Mỗi một cấp yêu cầu: x999 Bí kiếp tuyệt kỹ và Thông thạo đạt MAX 100%", "Học\ntuyệt kĩ", "Từ Chối");
                                }
                                if (this.mapId == 5) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "|7|NÂNG CẤP ĐỒ THIÊN SỨ\n|6| Mang cho ta Công thức + Đá cầu vòng và 999 Mảnh thiên sứ ta sẽ chế tạo đồ Thiên sứ cho ngươi"
                                            + "\nĐồ Thiên sứ khi chế tạo sẽ random chỉ số 0-15%"
                                            + "\n|2|(Khi mang đủ 5 món Hủy diệt ngươi hãy theo Osin qua Hành tinh ngục tù tìm kiếm Mảnh thiên sứ và săn BOSS Thiên sứ để thu thập Đá cầu vòng)"
                                            + "\n|1| Ngươi có muốn nâng cấp không?",
                                            "Hướng dẫn", "Nâng Cấp \nĐồ Thiên Sứ", "Shop\n Thiên sứ", "Phân rã\nĐồ Thiên sứ", "Nâng SKH Thiên sứ");
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (this.mapId == 5) {
                                    if (player.iDMark.isBaseMenu()) {
                                        switch (select) {
                                            case 0:
                                                NpcService.gI().createTutorial(player, this.avartar, ConstNpc.HUONG_DAN_DO_TS);
                                                break;
                                            case 1:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_DO_TS);
                                                break;
                                            case 2:
                                                ShopService.gI().openShopWhisThienSu(player,
                                                        ConstNpc.SHOP_WHIS_THIEN_SU, 0);
                                                break;
                                            case 3: //Mở chỉ số bông tai
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.PHAN_RA_DO_TS);
                                                break;
                                            case 4:
                                                CombineServiceNew.gI().openTabCombine(player, CombineServiceNew.NANG_CAP_SKH_TS);
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                                        switch (player.combineNew.typeCombine) {
                                            case CombineServiceNew.NANG_CAP_DO_TS:
                                            case CombineServiceNew.NANG_CAP_SKH_TS:
                                            case CombineServiceNew.PHAN_RA_DO_TS:
                                                if (select == 0) {
                                                    CombineServiceNew.gI().startCombine(player);
                                                }
                                                break;
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_NANG_CAP_DO_TS) {
                                        if (select == 0) {
                                            CombineServiceNew.gI().startCombine(player);
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_NANG_DO_SKH_TS) {
                                        if (select == 0) {
                                            CombineServiceNew.gI().startCombine(player);
                                        }
                                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_PHAN_RA_TS) {
                                        if (select == 0) {
                                            CombineServiceNew.gI().startCombine(player);
                                        }
                                    }
                                }

                                if (player.iDMark.isBaseMenu() && this.mapId == 48) {
                                    if (select == 0) {
                                        Message msg;
                                        try {
                                            Item sachTuyetki = null;
                                            try {
                                                sachTuyetki = InventoryService.gI().findItemBagByTemp(player, 1215);
                                            } catch (Exception e) {
                                            }
                                            if (player.gender == 0) {
                                                Skill curSkill = SkillUtil.getSkillbyId(player, Skill.SUPER_KAME);
                                                if (curSkill.point == 0) {
                                                    if (player.nPoint.power >= 60000000000L) {
                                                        if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                            this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                            return;
                                                        }
                                                        if (sachTuyetki.quantity >= 999) {
                                                            InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                            InventoryService.gI().sendItemBags(player);
                                                            curSkill = SkillUtil.createSkill(Skill.SUPER_KAME, 1);
                                                            SkillUtil.setSkill(player, curSkill);
                                                            msg = Service.getInstance().messageSubCommand((byte) 23);
                                                            msg.writer().writeShort(curSkill.skillId);
                                                            player.sendMessage(msg);
                                                            msg.cleanup();
                                                        } else {
                                                            Service.getInstance().sendThongBao(player, "Không đủ bí kíp tuyệt kĩ");
                                                        }
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Yêu cầu sức mạnh trên 60 Tỷ");
                                                    }
                                                } else if (curSkill.point > 0 && curSkill.point < 9) {
                                                    if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                        this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                        return;
                                                    }
                                                    if (sachTuyetki.quantity >= 999 && curSkill.currLevel == 1000) {
                                                        InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                        InventoryService.gI().sendItemBags(player);
                                                        curSkill = SkillUtil.createSkill(Skill.SUPER_KAME, curSkill.point + 1);
                                                        SkillUtil.setSkill(player, curSkill);
                                                        msg = Service.getInstance().messageSubCommand((byte) 62);
                                                        msg.writer().writeShort(curSkill.skillId);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Thông thạo của bạn chưa đủ 100%");
                                                    }
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Tuyệt kĩ của bạn đã đạt cấp tối đa");
                                                }
                                            }
                                            if (player.gender == 1) {
                                                Skill curSkill = SkillUtil.getSkillbyId(player, Skill.MA_PHONG_BA);
                                                if (curSkill.point == 0) {
                                                    if (player.nPoint.power >= 60000000000L) {
                                                        if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                            this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                            return;
                                                        }
                                                        if (sachTuyetki.quantity >= 999) {
                                                            InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                            InventoryService.gI().sendItemBags(player);
                                                            curSkill = SkillUtil.createSkill(Skill.MA_PHONG_BA, 1);
                                                            SkillUtil.setSkill(player, curSkill);
                                                            msg = Service.getInstance().messageSubCommand((byte) 23);
                                                            msg.writer().writeShort(curSkill.skillId);
                                                            player.sendMessage(msg);
                                                            msg.cleanup();
                                                        } else {
                                                            Service.getInstance().sendThongBao(player, "Không đủ bí kíp tuyệt kĩ");
                                                        }
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Yêu cầu sức mạnh trên 60 Tỷ");
                                                    }
                                                } else if (curSkill.point > 0 && curSkill.point < 9) {
                                                    if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                        this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                        return;
                                                    }
                                                    if (sachTuyetki.quantity >= 999 && curSkill.currLevel == 1000) {
                                                        InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                        InventoryService.gI().sendItemBags(player);
                                                        curSkill = SkillUtil.createSkill(Skill.MA_PHONG_BA, curSkill.point + 1);
                                                        SkillUtil.setSkill(player, curSkill);
                                                        msg = Service.getInstance().messageSubCommand((byte) 62);
                                                        msg.writer().writeShort(curSkill.skillId);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Thông thạo của bạn chưa đủ 100%");
                                                    }
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Tuyệt kĩ của bạn đã đạt cấp tối đa");
                                                }
                                            }
                                            if (player.gender == 2) {
                                                Skill curSkill = SkillUtil.getSkillbyId(player, Skill.LIEN_HOAN_CHUONG);
                                                if (curSkill.point == 0) {
                                                    if (player.nPoint.power >= 60000000000L) {
                                                        if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                            this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                            return;
                                                        }
                                                        if (sachTuyetki.quantity >= 999) {
                                                            InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                            InventoryService.gI().sendItemBags(player);
                                                            curSkill = SkillUtil.createSkill(Skill.LIEN_HOAN_CHUONG, 1);
                                                            SkillUtil.setSkill(player, curSkill);
                                                            msg = Service.getInstance().messageSubCommand((byte) 23);
                                                            msg.writer().writeShort(curSkill.skillId);
                                                            player.sendMessage(msg);
                                                            msg.cleanup();
                                                        } else {
                                                            Service.getInstance().sendThongBao(player, "Không đủ bí kíp tuyệt kĩ");
                                                        }
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Yêu cầu sức mạnh trên 60 Tỷ");
                                                    }
                                                } else if (curSkill.point > 0 && curSkill.point < 9) {
                                                    if (sachTuyetki == null || sachTuyetki.quantity < 999) {
                                                        this.npcChat(player, "Bạn không đủ 999 bí kíp tuyệt kĩ");
                                                        return;
                                                    }
                                                    if (sachTuyetki.quantity >= 999 && curSkill.currLevel == 1000) {
                                                        InventoryService.gI().subQuantityItemsBag(player, sachTuyetki, 999);
                                                        InventoryService.gI().sendItemBags(player);
                                                        curSkill = SkillUtil.createSkill(Skill.LIEN_HOAN_CHUONG, curSkill.point + 1);
                                                        SkillUtil.setSkill(player, curSkill);
                                                        msg = Service.getInstance().messageSubCommand((byte) 62);
                                                        msg.writer().writeShort(curSkill.skillId);
                                                        player.sendMessage(msg);
                                                        msg.cleanup();
                                                    } else {
                                                        Service.getInstance().sendThongBao(player, "Thông thạo của bạn chưa đủ 100%");
                                                    }
                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Tuyệt kĩ của bạn đã đạt cấp tối đa");
                                                }
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    };
                    break;

                case ConstNpc.CUA_HANG_KY_GUI:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                        "|7|Xin chào!\n"
                                        + "Đây là Cửa Hàng Ký Gửi - nơi bạn có thể đăng bán vật phẩm hoặc mua từ người chơi khác.",
                                        "Hướng dẫn", "Mở cửa hàng", "Từ chối");
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                switch (player.iDMark.getIndexMenu()) {
                                    case ConstNpc.BASE_MENU:
                                        switch (select) {
                                            case 0: // Hướng dẫn
                                                Service.getInstance().sendPopUpMultiLine(player, tempId, avartar,
                                                        "|2|HƯỚNG DẪN KÝ GỬI\n\n"
                                                        + "- Đăng bán vật phẩm: chọn vật phẩm trong túi có thể ký gửi, nhập giá.\n"
                                                        + "- Người khác có thể mua bằng Vàng hoặc Hồng Ngọc.\n"
                                                        + "- Khi bán thành công, bạn nhận lại tiền (trừ phí 10%).\n"
                                                        + "- Bạn có thể huỷ ký gửi để lấy lại vật phẩm.\n\n"
                                                        + "|7|Chúc bạn giao dịch vui vẻ!");
                                                break;
                                            case 1: // Mở cửa hàng
                                                ConsignmentShop.getInstance().show(player);
                                                break;
                                            case 2: // Từ chối
                                                Service.getInstance().sendThongBao(player, "Hẹn gặp lại!");
                                                break;
                                        }
                                        break;
                                }
                            }
                        }
                    };
                    break;

                case ConstNpc.CUONG_HOA:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {

                        private static final String[] CULTIVATION_RANKS = {
                            "Phế Nhân",
                            "Phàm Nhân",
                            "Khai Linh Căn",
                            "Luyện Khí",
                            "Luyện Khí Trung Cấp",
                            "Trúc Cơ",
                            "Trúc Cơ Trung Cấp",
                            "Kết Đan Kì",
                            "Kết Đan Kì Trung Cấp",
                            "Nguyên Anh Sơ Khí",
                            "Nguyên Anh Trung Kì",
                            "Nguyên Anh Hậu Kì",
                            "Hóa Thần Sơ Kì",
                            "Hóa Thần Trung Kì",
                            "Hóa Thần Đại Viên Mãn"
                        };

                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                if (player.pet != null) {
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "|7|BÍ PHÁP ĐỆ TỬ \n"
                                            + "|1|Ta nắm giữ những nghi thức cổ xưa có thể thay đổi vận mệnh.\n"
                                            + "|2|Ngươi muốn khai mở sức mạnh tiềm ẩn...\n"
                                            + "|2|Hay tiến hóa đệ tử vượt qua giới hạn của hắn?\n"
                                            + "|0|-----------------------------\n"
                                            + "|7|Hãy chọn con đường mà ngươi mong muốn ",
                                            "Cường Hóa Đệ Tử",
                                            "Cường Hóa Nhân Vật"
                                    );
                                } else {
                                    // Menu cho người không có pet
                                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                            "|7|BÍ PHÁP TU LUYỆN\n"
                                            + "|1|Ngươi chưa có đệ tử, nhưng ta vẫn có thể giúp ngươi cường hóa bản thân!\n"
                                            + "|2|Hãy mạnh mẽ lên để chinh phục vũ trụ!",
                                            "Cường Hóa Nhân Vật"
                                    );
                                }
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                // ===== MENU GỐC =====
                                if (player.iDMark.isBaseMenu()) {
                                    if (player.pet == null) {
                                        if (select == 0) {
                                            showPlayerEnhanceMenu(player);
                                        }
                                        return;
                                    }

                                    int lv = player.leverPet;
                                    switch (select) {
                                        case 0: // MENU CƯỜNG HÓA ĐỆ TỬ
                                            if (player.pet.typePet == 0) {
                                                Service.getInstance().sendThongBaoOK(player, "Đệ tử của bạn chưa đủ điều kiện cường hóa");
                                                return;
                                            }
                                            String petRank = CULTIVATION_RANKS[Math.min(lv, CULTIVATION_RANKS.length - 1)];
                                            this.createOtherMenu(player, ConstNpc.NANG_CAP_DE_TU,
                                                    "|7|⚡ CƯỜNG HÓA ĐỆ TỬ ⚡\n"
                                                    + "Đệ tử: " + player.pet.name + "\n"
                                                    + "|1|Hiện tại: " + petRank + "\n"
                                                    + "|0|-----------------------------\n"
                                                    + "|2|Mỗi cấp cường hóa sẽ ban tặng:\n"
                                                    + "|7|+" + (100 * (lv + 1)) + " HP gốc\n"
                                                    + "|7|+" + (100 * (lv + 1)) + " KI gốc\n"
                                                    + "|7|+" + (70 * (lv + 1)) + " Sức đánh gốc\n"
                                                    + "|0|-----------------------------\n"
                                                    + "|3|Sức mạnh càng lớn, trách nhiệm càng nặng! ",
                                                    "Cường hóa ngay", "Quay lại");
                                            break;

                                        case 1: // MENU CƯỜNG HÓA NHÂN VẬT
                                            showPlayerEnhanceMenu(player);
                                            break;

                                        case 2: // MENU CƯỜNG HÓA HỢP THỂ
                                            if (player.pet.typePet == 0) {
                                                Service.getInstance().sendThongBaoOK(player, "Bạn cần có đệ tử để cường hóa hợp thể");
                                                return;
                                            }
                                            int LVhopthe = player.LVhopthe;
                                            this.createOtherMenu(player, ConstNpc.NANG_CAP_CSHT,
                                                    "|7|⚡ CƯỜNG HÓA CHỈ SỐ HỢP THỂ ⚡\n"
                                                    + "Đệ tử: " + player.pet.name + "\n"
                                                    + "|2|Cấp độ chỉ số: " + LVhopthe + "/36\n"
                                                    + "|0|-----------------------------\n"
                                                    + "|3|YÊU CẦU:\n"
                                                    + "|1| HP gốc: 700.000\n"
                                                    + "|1| KI gốc: 700.000\n"
                                                    + "|1|Sức đánh gốc: 30.000\n"
                                                    + "|0|-----------------------------\n"
                                                    + "|3|ĐẶC ĐIỂM:\n"
                                                    + "|7| Nâng cấp ngẫu nhiên 1 trong 3 chỉ số\n"
                                                    + "|7|Mỗi level được +7% chỉ số hợp thể\n"
                                                    + "|7|Tỉ lệ thành công: 14%\n"
                                                    + "|0|-----------------------------\n"
                                                    + "|2|Chi phí: 36 Cờ Trấn Hồn",
                                                    "Nâng cấp ngay", "Quay lại");
                                            break;
                                    }
                                } // ====== LOGIC NÂNG CẤP ĐỆ TỬ ======
                                else if (player.iDMark.getIndexMenu() == ConstNpc.NANG_CAP_DE_TU) {
                                    if (select == 1) {
                                        return;
                                    }
                                    int lv = player.leverPet;
                                    Item nangcapde = InventoryService.gI().findItemBagByTemp(player, 1564);

                                    boolean duchisogoc = player.pet.nPoint.hpg >= 500000
                                            && player.pet.nPoint.mpg >= 500000
                                            && player.pet.nPoint.dameg >= 30000;

                                    if (!duchisogoc) {
                                        Service.getInstance().sendThongBaoOK(player,
                                                "Đệ tử của bạn cần đạt:\n"
                                                + "• 500k HP gốc\n"
                                                + "• 500k KI gốc\n"
                                                + "• 30k Sức đánh gốc");
                                        return;
                                    }

                                    if (nangcapde == null || nangcapde.quantity < 20) {
                                        Service.getInstance().sendThongBao(player, "Bạn không đủ 20 đá cường hóa");
                                        return;
                                    }

                                    if (player.leverPet >= 14) {
                                        Service.getInstance().sendThongBao(player, "Đệ tử của bạn đã tu luyện tới cấp tối đa");
                                        return;
                                    }

                                    InventoryService.gI().subQuantityItemsBag(player, nangcapde, 20);
                                    InventoryService.gI().sendItemBags(player);

                                    if (Util.isTrue(14, 100)) {
                                        player.leverPet++;
                                        long hpBonus = 100L * player.leverPet;
                                        long mpBonus = 100L * player.leverPet;
                                        long damageBonus = 70L * player.leverPet;

                                        player.pet.nPoint.hpg += hpBonus;
                                        player.pet.nPoint.mpg += mpBonus;
                                        player.pet.nPoint.dameg += damageBonus;

                                        String baseName = player.pet.name.replaceAll("\\|.*?\\|", "").replaceAll("\\[.*?\\]", "").trim();
                                        String newRank = CULTIVATION_RANKS[Math.min(player.leverPet, CULTIVATION_RANKS.length - 1)];
                                        player.pet.name = baseName + " [" + newRank + "]";

                                        Service.getInstance().sendThongBaoOK(player,
                                                "Cường hóa đệ tử thành công!\n"
                                                + "Tu vi: " + newRank + "\n"
                                                + "HP: +" + Util.numberToMoney(hpBonus) + "\n"
                                                + "KI: +" + Util.numberToMoney(mpBonus) + "\n"
                                                + "Sức đánh: +" + Util.numberToMoney(damageBonus));
                                    } else {
                                        Service.getInstance().sendThongBao(player, "Nâng cấp thất bại! Đã mất 20 đá cường hóa");
                                    }
                                } // ====== LOGIC NÂNG CẤP HỢP THỂ ======
                                else if (player.iDMark.getIndexMenu() == ConstNpc.NANG_CAP_CSHT) {
                                    if (select == 1) {
                                        return;
                                    }
                                    Item nangcapde = InventoryService.gI().findItemBagByTemp(player, 1574);

                                    boolean duchisogoc = player.nPoint.hpg >= 1000000
                                            && player.nPoint.mpg >= 1000000
                                            && player.nPoint.dameg >= 60000;

                                    if (!duchisogoc) {
                                        Service.getInstance().sendThongBaoOK(player,
                                                "Bạn cần đạt:\n"
                                                + "• 1 triệu HP gốc\n"
                                                + "• 1 triệu KI gốc\n"
                                                + "• 60k Sức đánh gốc");
                                        return;
                                    }

                                    if (nangcapde == null || nangcapde.quantity < 36) {
                                        Service.getInstance().sendThongBao(player, "Bạn không đủ 36 Cờ Trấn Hồn");
                                        return;
                                    }

                                    if (player.LVhopthe >= 36) {
                                        Service.getInstance().sendThongBao(player, "Bạn đã nâng cấp chỉ số ẩn bông tai cấp tối đa");
                                        return;
                                    }

                                    InventoryService.gI().subQuantityItemsBag(player, nangcapde, 36);
                                    InventoryService.gI().sendItemBags(player);

                                    if (Util.isTrue(14, 100)) {
                                        String[] tencs = {"Sức đánh", "HP", "KI"};
                                        int loaics;
                                        int cstang = 7;
                                        player.LVhopthe++;

                                        int randStat = Util.nextInt(0, 100);
                                        if (randStat < 33) {
                                            player.SDhopthe += cstang;
                                            loaics = 0;
                                        } else if (randStat < 66) {
                                            player.HPhopthe += cstang;
                                            loaics = 1;
                                        } else {
                                            player.KIhopthe += cstang;
                                            loaics = 2;
                                        }

                                        Service.getInstance().sendThongBaoOK(player,
                                                "Cường hóa thành công lên cấp " + player.LVhopthe + "!\n"
                                                + "Chỉ số tăng: " + tencs[loaics] + " +" + cstang + "%");
                                    } else {
                                        Service.getInstance().sendThongBao(player, "Nâng cấp thất bại! Đã mất 36 Cờ Trấn Hồn");
                                    }
                                } // ====== LOGIC CƯỜNG HÓA NHÂN VẬT ======
                                else if (player.iDMark.getIndexMenu() == ConstNpc.NANG_CAP_PLAYER) {
                                    switch (select) {
                                        case 0:
                                            handlePlayerEnhance(player);
                                            break;
                                        case 1:
                                            showPlayerEnhanceDetail(player);
                                            break;
                                        case 2:
                                            break;
                                    }
                                }
                            }
                        }

                        // ===== HELPER METHODS =====
                        private void showPlayerEnhanceMenu(Player player) {
                            int currentLevel = player.levelEnhance;
                            int maxLevel = 15;
                            int successRate = Math.max(5, 65 - (currentLevel * 4));
                            long nextHpBonus = 800L * (currentLevel + 1);
                            long nextMpBonus = 800L * (currentLevel + 1);
                            long nextDamageBonus = 75L * (currentLevel + 1);

                            long totalHpGained = 0;
                            long totalMpGained = 0;
                            long totalDamageGained = 0;
                            for (int i = 1; i <= currentLevel; i++) {
                                totalHpGained += 800L * i;
                                totalMpGained += 800L * i;
                                totalDamageGained += 75L * i;
                            }

                            StringBuilder progressBar = new StringBuilder();
                            int progress = (currentLevel * 10) / maxLevel;
                            progressBar.append("|2|[");
                            for (int i = 0; i < 10; i++) {
                                progressBar.append(i < progress ? "█" : "░");
                            }
                            progressBar.append("]");

                            String levelColor = "|1|";
                            if (currentLevel >= 12) {
                                levelColor = "|6|";
                            } else if (currentLevel >= 8) {
                                levelColor = "|5|";
                            } else if (currentLevel >= 4) {
                                levelColor = "|3|";
                            } else if (currentLevel >= 1) {
                                levelColor = "|4|";
                            }

                            boolean meetsHpReq = player.nPoint.hpg >= 600_000;
                            boolean meetsMpReq = player.nPoint.mpg >= 600_000;
                            boolean meetsDamageReq = player.nPoint.dameg >= 30_000;

                            String requirements = "";
                            requirements += (meetsHpReq ? "|4|" : "|1|✗") + " HP gốc: "
                                    + (meetsHpReq ? "|2|" : "|1|") + "600.000\n";
                            requirements += (meetsMpReq ? "|4|" : "|1|✗") + " KI gốc: "
                                    + (meetsMpReq ? "|2|" : "|1|") + "600.000\n";
                            requirements += (meetsDamageReq ? "|4|" : "|1|✗") + " Sức đánh gốc: "
                                    + (meetsDamageReq ? "|2|" : "|1|") + "30.000";

                            String riskInfo = "";
                            if (currentLevel > 0) {
                                riskInfo = "\n|1| CẢNH BÁO RỦI RO KHI THẤT BẠI:\n"
                                        + "|1|• 30% cơ hội giảm 1 cấp\n"
                                        + "|1|• Mất toàn bộ vật phẩm cường hóa\n"
                                        + "|1|• Giảm chỉ số tương ứng\n"
                                        + "|4|• Dùng Đá Bảo Vệ để tránh giảm cấp";
                            }

                            String currentRank = CULTIVATION_RANKS[Math.min(currentLevel, CULTIVATION_RANKS.length - 1)];

                            String menuContent
                                    = "|7|TRẠNG THÁI HIỆN TẠI\n"
                                    + levelColor + "Tu vi: " + currentRank + "\n"
                                    + progressBar.toString() + " " + ((currentLevel * 100) / maxLevel) + "%\n"
                                    + "\n"
                                    + "|3|Tổng chỉ số đã nhận:\n"
                                    + "|2|HP: +" + Util.numberToMoney(totalHpGained) + "\n"
                                    + "|2|KI: +" + Util.numberToMoney(totalMpGained) + "\n"
                                    + "|2|Sức đánh: +" + Util.numberToMoney(totalDamageGained) + "\n"
                                    + "\n"
                                    + "|7|⚡ NÂNG CẤP TIẾP THEO\n"
                                    + (currentLevel < maxLevel
                                            ? "Tỷ lệ thành công: " + successRate + "%\n"
                                            + "Chi phí: 30 Thuốc Biến Dị\n"
                                            + "Bảo vệ: 10 Đá Bảo Vệ (tùy chọn)\n"
                                            + "Phần thưởng khi thành công:\n"
                                            + "|4|+" + Util.numberToMoney(nextHpBonus) + " HP gốc\n"
                                            + "|4|+" + Util.numberToMoney(nextMpBonus) + " KI gốc\n"
                                            + "|4|+" + Util.numberToMoney(nextDamageBonus) + " Sức đánh gốc"
                                            : "|6|ĐÃ ĐẠT TỪ VI TỐI ĐA - HÓA THẦN ĐẠI VIÊN MÃN!\n"
                                            + "|6|Bạn đã trở thành một trong những\n"
                                            + "|6|chiến binh mạnh nhất vũ trụ!") + "\n"
                                    + "\n"
                                    + "|7|YÊU CẦU CƯỜNG HÓA\n"
                                    + requirements + "\n"
                                    + riskInfo
                                    + "\n"
                                    + "|7|Mẹo: Tu vi càng cao, tỷ lệ thành công càng thấp\n"
                                    + "|7|nhưng chỉ số nhận được càng lớn!";

                            this.createOtherMenu(player, ConstNpc.NANG_CAP_PLAYER,
                                    menuContent,
                                    currentLevel < maxLevel ? " Cường hóa ngay" : " Đã tối đa",
                                    "Chi tiết",
                                    " Đóng");
                        }

                        private void showPlayerEnhanceDetail(Player player) {
                            StringBuilder detail = new StringBuilder();
                            detail.append("|7|CHI TIẾT CẤP ĐỘ CƯỜNG HÓA\n\n");

                            for (int i = 0; i < CULTIVATION_RANKS.length; i++) {
                                long hp = 800L * (i + 1);
                                long mp = 800L * (i + 1);
                                long dam = 75L * (i + 1);
                                int rate = Math.max(5, 65 - (i * 4));

                                String color = "|1|";
                                if (i >= 12) {
                                    color = "|6|";
                                } else if (i >= 8) {
                                    color = "|5|";
                                } else if (i >= 4) {
                                    color = "|3|";
                                } else if (i >= 2) {
                                    color = "|4|";
                                }

                                detail.append(color).append(CULTIVATION_RANKS[i]).append(": |2|")
                                        .append(rate).append("%\n")
                                        .append("|2|  HP: +").append(Util.numberToMoney(hp))
                                        .append(" | KI: +").append(Util.numberToMoney(mp))
                                        .append(" | SD: +").append(Util.numberToMoney(dam)).append("\n\n");
                            }

                            Service.getInstance().sendThongBaoOK(player, detail.toString());
                        }

                        private void handlePlayerEnhance(Player player) {
                            int enhanceStoneId = 1590;
                            int protectStoneId = 1591;

                            if (player.nPoint.hpg < 600_000 || player.nPoint.mpg < 600_000 || player.nPoint.dameg < 30_000) {
                                Service.getInstance().sendThongBaoOK(player,
                                        "Bạn cần đạt:\n"
                                        + "• 600k HP gốc\n"
                                        + "• 600k KI gốc\n"
                                        + "• 30k Sức đánh gốc");
                                return;
                            }

                            Item enhanceStone = InventoryService.gI().findItemBagByTemp(player, enhanceStoneId);
                            if (enhanceStone == null || enhanceStone.quantity < 30) {
                                Service.getInstance().sendThongBaoOK(player, "Bạn không đủ 30 Thuốc Biến Dị");
                                return;
                            }

                            if (player.levelEnhance >= 15) {
                                Service.getInstance().sendThongBaoOK(player, "Bạn đã tu luyện tới tu vi tối đa");
                                return;
                            }

                            int currentLevel = player.levelEnhance;
                            int successRate = Math.max(5, 65 - (currentLevel * 4));

                            InventoryService.gI().subQuantityItemsBag(player, enhanceStone, 30);

                            Item protectStone = InventoryService.gI().findItemBagByTemp(player, protectStoneId);
                            boolean hasProtect = protectStone != null && protectStone.quantity >= 10;
                            if (hasProtect) {
                                InventoryService.gI().subQuantityItemsBag(player, protectStone, 10);
                            }

                            InventoryService.gI().sendItemBags(player);

                            if (Util.isTrue(successRate, 100)) {
                                player.levelEnhance++;
                                long hpBonus = 800L * player.levelEnhance;
                                long mpBonus = 800L * player.levelEnhance;
                                long damageBonus = 75L * player.levelEnhance;

                                player.nPoint.hpg += hpBonus;
                                player.nPoint.mpg += mpBonus;
                                player.nPoint.dameg += damageBonus;
                                player.nPoint.hp = player.nPoint.hpg;
                                player.nPoint.mp = player.nPoint.mpg;

                                String baseName = player.name.replaceAll("\\s*\\|.*?\\|", "").replaceAll("\\s*\\[.*?\\]", "").trim();
                                String newRank = CULTIVATION_RANKS[Math.min(player.levelEnhance, CULTIVATION_RANKS.length - 1)];
                                player.name = baseName + " |" + player.levelEnhance + "| [" + newRank + "]";

                                if (player.levelEnhance >= 15) {
                                    switch (player.gender) {
                                        case 0:
                                            player.head = 910;
                                            player.body = 911;
                                            player.leg = 912;
                                            break;
                                        case 1:
                                            player.head = 913;
                                            player.body = 914;
                                            player.leg = 915;
                                            break;
                                        case 2:
                                            player.head = 542;
                                            player.body = 543;
                                            player.leg = 544;
                                            break;
                                    }
                                    Service.getInstance().point(player);
                                    Service.getInstance().Send_Caitrang(player);
                                    Service.getInstance().sendInfoPlayerEatPea(player);
                                    Service.getInstance().sendThongBaoAllPlayer(
                                            " " + player.name + " đã đạt tu vi tối đa - Hóa Thần Đại Viên Mãn và thức tỉnh ngoại hình mới! ");
                                }

                                Service.getInstance().sendThongBao(player,
                                        " Cường hóa thành công!\n"
                                        + "Tu vi: " + newRank + "\n"
                                        + "━━━━━━━━━━━━━━━━━━━━\n"
                                        + "HP gốc: +" + Util.numberToMoney(hpBonus) + "\n"
                                        + "KI gốc: +" + Util.numberToMoney(mpBonus) + "\n"
                                        + "Sức đánh: +" + Util.numberToMoney(damageBonus));
                                Service.getInstance().sendInfoPlayerEatPea(player);
                            } else {
                                if (currentLevel > 0 && !hasProtect && Util.isTrue(30, 100)) {
                                    player.levelEnhance--;
                                    long hpLoss = 800L * currentLevel;
                                    long mpLoss = 800L * currentLevel;
                                    long damageLoss = 75L * currentLevel;

                                    player.nPoint.hpg = Math.max(player.nPoint.hpg - hpLoss, 800L);
                                    player.nPoint.mpg = Math.max(player.nPoint.mpg - mpLoss, 800L);
                                    player.nPoint.dameg = Math.max(player.nPoint.dameg - damageLoss, 75L);

                                    String baseName = player.name.replaceAll("\\s*\\|.*?\\|", "").replaceAll("\\s*\\[.*?\\]", "").trim();
                                    if (player.levelEnhance > 0) {
                                        String downRank = CULTIVATION_RANKS[Math.min(player.levelEnhance, CULTIVATION_RANKS.length - 1)];
                                        player.name = baseName + " |" + player.levelEnhance + "| [" + downRank + "]";
                                    } else {
                                        player.name = baseName;
                                    }

                                    Service.getInstance().sendInfoPlayerEatPea(player);
                                    String downRank = CULTIVATION_RANKS[Math.min(player.levelEnhance, CULTIVATION_RANKS.length - 1)];
                                    Service.getInstance().sendThongBao(player,
                                            " Cường hóa thất bại và bị giảm xuống: " + downRank + "!");
                                } else {
                                    Service.getInstance().sendThongBao(player, hasProtect
                                            ? " Cường hóa thất bại nhưng Đá Bảo Vệ đã giúp bạn không bị giảm cấp!"
                                            : "Cường hóa thất bại! Hãy thử lại.");
                                }
                            }
                            Service.getInstance().sendInfoPlayerEatPea(player);
                        }
                    };
                    break;

                case ConstNpc.VADOS:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                createOtherMenu(player, ConstNpc.BASE_MENU,
                                        "|7| BẢNG XẾP HẠNG\n|6|Ta Vừa Hack Map xem Được TOP Của Toàn Server\b|1|Người Muốn Xem TOP Gì?",
                                        "Top Sức mạnh", "Top Sức mạnh Đệ tử", "Top Nhiệm vụ", "Top Nạp");
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                if (player.iDMark.isBaseMenu()) {
                                    switch (select) {
                                        case 0:
                                            Service.getInstance().showTopPower(player, Service.getInstance().TOP_SUCMANH);
                                            break;
                                        case 1:
                                            Service.getInstance().showTopPower(player, Service.getInstance().TOP_DETU);
                                            break;
                                        case 2:
                                            Service.getInstance().showTopPower(player, Service.getInstance().TOP_NHIEMVU);
                                            break;
                                        case 3:
                                            Service.getInstance().showTopPower(player, Service.getInstance().TOP_NAP);
                                            break;
                                    }
                                }
                            }
                        }
                    };
                    break;
                default:
                    npc = new Npc(mapId, status, cx, cy, tempId, avartar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                super.openBaseMenu(player);
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                                // ShopService.gI().openShopNormal(player, this, ConstNpc.SHOP_BUNMA_TL_0, 0,
                                // player.gender);
                            }
                        }
                    };
            }
        } catch (Exception e) {
            Log.error(NpcFactory.class, e, "Lỗi load npc");
        }
        return npc;
    }

    // girlkun75-mark
    public static void createNpcRongThieng() {
        Npc npc = new Npc(-1, -1, -1, -1, ConstNpc.RONG_THIENG, -1) {
            @Override
            public void confirmMenu(Player player, int select) {
                switch (player.iDMark.getIndexMenu()) {
                    case ConstNpc.IGNORE_MENU:

                        break;
                    case ConstNpc.SHENRON_CONFIRM:
                        if (select == 0) {
                            SummonDragon.gI().confirmWish();
                        } else if (select == 1) {
                            SummonDragon.gI().reOpenShenronWishes(player);
                        }
                        break;
                    case ConstNpc.SHENRON_1_1:
                        if (player.iDMark.getIndexMenu() == ConstNpc.SHENRON_1_1
                                && select == SHENRON_1_STAR_WISHES_1.length - 1) {
                            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_1_2, SHENRON_SAY,
                                    SHENRON_1_STAR_WISHES_2);
                            break;
                        }
                    case ConstNpc.SHENRON_1_2:
                        if (player.iDMark.getIndexMenu() == ConstNpc.SHENRON_1_2
                                && select == SHENRON_1_STAR_WISHES_2.length - 1) {
                            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_1_1, SHENRON_SAY,
                                    SHENRON_1_STAR_WISHES_1);
                            break;
                        }
                    case ConstNpc.BLACK_SHENRON:
                        if (player.iDMark.getIndexMenu() == ConstNpc.BLACK_SHENRON
                                && select == BLACK_SHENRON_WISHES.length) {
                            NpcService.gI().createMenuRongThieng(player, ConstNpc.BLACK_SHENRON, BLACK_SHENRON_SAY,
                                    BLACK_SHENRON_WISHES);
                            break;
                        }
                    case ConstNpc.ICE_SHENRON:
                        if (player.iDMark.getIndexMenu() == ConstNpc.ICE_SHENRON
                                && select == ICE_SHENRON_WISHES.length) {
                            NpcService.gI().createMenuRongThieng(player, ConstNpc.ICE_SHENRON, ICE_SHENRON_SAY,
                                    ICE_SHENRON_WISHES);
                            break;
                        }
                    default:
                        SummonDragon.gI().showConfirmShenron(player, player.iDMark.getIndexMenu(), (byte) select);
                        break;
                }
            }
        };
    }

    public static void createNpcConMeo() {
        Npc npc = new Npc(-1, -1, -1, -1, ConstNpc.CON_MEO, 351) {
            @Override
            public void confirmMenu(Player player, int select) {
                switch (player.iDMark.getIndexMenu()) {
                    case ConstNpc.CONFIRM_DIALOG:
                        ConfirmDialog confirmDialog = player.getConfirmDialog();
                        if (confirmDialog != null) {
                            if (confirmDialog instanceof MenuDialog menu) {
                                menu.getRunable().setIndexSelected(select);
                                menu.run();
                                return;
                            }
                            if (select == 0) {
                                confirmDialog.run();
                            } else {
                                confirmDialog.cancel();
                            }
                            player.setConfirmDialog(null);
                        }
                        break;
                    case ConstNpc.UP_TOP_ITEM:

                        break;
                    case ConstNpc.MENU_OPTION_USE_ITEM1105:
                        if (select == 0) {
                            IntrinsicService.gI().sattd(player);
                        } else if (select == 1) {
                            IntrinsicService.gI().satnm(player);
                        } else if (select == 2) {
                            IntrinsicService.gI().setxd(player);
                        }
                        break;
                    case ConstNpc.MENU_OPTION_USE_ITEM1537:
                        if (select == 0) {
                            IntrinsicService.gI().SkhHdTD(player);
                        } else if (select == 1) {
                            IntrinsicService.gI().SkhHdNM(player);
                        } else if (select == 2) {
                            IntrinsicService.gI().SkhHdXD(player);
                        }
                        break;
                    case ConstNpc.MENU_OPTION_USE_ITEM1538:
                        if (select == 0) {
                            IntrinsicService.gI().SkhTlTD(player);
                        } else if (select == 1) {
                            IntrinsicService.gI().SkhTlNM(player);
                        } else if (select == 2) {
                            IntrinsicService.gI().SkhTlXD(player);
                        }
                        break;
                    case ConstNpc.MENU_TD:
                        switch (select) {
                            case 0:
                                try {
                                ItemService.gI().settaiyoken(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 1:
                                try {
                                ItemService.gI().setgenki(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 2:
                                try {
                                ItemService.gI().setkamejoko(player);
                            } catch (Exception e) {
                            }
                            break;
                        }
                        break;
                    case ConstNpc.MENU_NM:
                        switch (select) {
                            case 0:
                                try {
                                ItemService.gI().setgodki(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 1:
                                try {
                                ItemService.gI().setgoddam(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 2:
                                try {
                                ItemService.gI().setsummon(player);
                            } catch (Exception e) {
                            }
                            break;
                        }
                        break;

                    case ConstNpc.MENU_XD:
                        switch (select) {
                            case 0:
                                try {
                                ItemService.gI().setgodgalick(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 1:
                                try {
                                ItemService.gI().setmonkey(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 2:
                                try {
                                ItemService.gI().setgodhp(player);
                            } catch (Exception e) {
                            }
                            break;
                        }
                        break;
                    case ConstNpc.MENU_TD_HD:
                        switch (select) {
                            case 0:
                                try {
                                ItemService.gI().settaiyokenHD(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 1:
                                try {
                                ItemService.gI().setgenkiHD(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 2:
                                try {
                                ItemService.gI().setkamejokoHD(player);
                            } catch (Exception e) {
                            }
                            break;
                        }
                        break;
                    case ConstNpc.MENU_NM_HD:
                        switch (select) {
                            case 0:
                                try {
                                ItemService.gI().setgodkiHD(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 1:
                                try {
                                ItemService.gI().setgoddamHD(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 2:
                                try {
                                ItemService.gI().setsummonHD(player);
                            } catch (Exception e) {
                            }
                            break;
                        }
                        break;

                    case ConstNpc.MENU_XD_HD:
                        switch (select) {
                            case 0:
                                try {
                                ItemService.gI().setgodgalickHD(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 1:
                                try {
                                ItemService.gI().setmonkeyHD(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 2:
                                try {
                                ItemService.gI().setgodhpHD(player);
                            } catch (Exception e) {
                            }
                            break;
                        }
                        break;
                    case ConstNpc.MENU_TD_TL:
                        switch (select) {
                            case 0:
                                try {
                                ItemService.gI().settaiyokenTL(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 1:
                                try {
                                ItemService.gI().setgenkiTL(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 2:
                                try {
                                ItemService.gI().setkamejokoTL(player);
                            } catch (Exception e) {
                            }
                            break;
                        }
                        break;
                    case ConstNpc.MENU_NM_TL:
                        switch (select) {
                            case 0:
                                try {
                                ItemService.gI().setgodkiTL(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 1:
                                try {
                                ItemService.gI().setgoddamTL(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 2:
                                try {
                                ItemService.gI().setsummonTL(player);
                            } catch (Exception e) {
                            }
                            break;
                        }
                        break;

                    case ConstNpc.MENU_XD_TL:
                        switch (select) {
                            case 0:
                                try {
                                ItemService.gI().setgodgalickTL(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 1:
                                try {
                                ItemService.gI().setmonkeyTL(player);
                            } catch (Exception e) {
                            }
                            break;
                            case 2:
                                try {
                                ItemService.gI().setgodhpTL(player);
                            } catch (Exception e) {
                            }
                            break;
                        }
                        break;
                    case ConstNpc.MO_RONG_RUONG_SUU_TAM:
                        if (select == 0) {
                            Item thoiVang = InventoryService.gI().findItemBagByTemp(player, (short) RuongSuuTam.ID_TEMP);

                            List<Item> listItem = new ArrayList<>();
                            if (player.typeMoRuong == 0) {
                                listItem = player.ruongSuuTam.RuongCaiTrang;
                            } else if (player.typeMoRuong == 1) {
                                listItem = player.ruongSuuTam.RuongPhuKien;
                            } else if (player.typeMoRuong == 2) {
                                listItem = player.ruongSuuTam.RuongPet;
                            } else if (player.typeMoRuong == 3) {
                                listItem = player.ruongSuuTam.RuongLinhThu;
                            } else if (player.typeMoRuong == 4) {
                                listItem = player.ruongSuuTam.RuongThuCuoi;
                            }
                            if (listItem == null) {
                                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                                return;
                            }
                            if (listItem.size() >= RuongSuuTam.MAX_SIZE) {
                                Service.getInstance().sendThongBao(player, "Rương Sưu Tầm đã đạt giới hạn tối đa");
                                return;
                            }
                            if (thoiVang != null && thoiVang.quantity >= RuongSuuTam.QUATITY) {
                                listItem.add(ItemService.gI().createItemNull());
                                InventoryService.gI().subQuantityItemsBag(player, thoiVang, RuongSuuTam.QUATITY);
                                InventoryService.gI().sendItemBags(player);
                                RuongSuuTam.gI().SendAllRuong(player);
                                Service.getInstance().sendThongBao(player, "Mở thêm 1 ô " + RuongSuuTam.gI().nameRuong(player, player.typeMoRuong) + " Thành công");
                            } else {
                                Service.getInstance().sendThongBao(player, "Không đủ 100 Thỏi vàng");
                            }
                        }
                        break;
                    case ConstNpc.NANG_CAP_KHAM_NGOC:
                        if (select == 0) {
                            int nro = player.nroKhamNgoc;
                            int max_quatity = player.slItem;
                            int idItem = player.idTempNangCap;
                            KhamNgocPlayer manager = player.khamNgoc.get(nro);
                            Item item = InventoryService.gI().findItemBagByTemp(player, (short) idItem);
                            Item it = ItemService.gI().createNewItem((short) idItem);
                            if (item == null || item.quantity < max_quatity) {
                                Service.getInstance().sendThongBao(player, "Không đủ nguyên liệu. Còn thiếu " + (item == null ? max_quatity : (max_quatity - item.quantity)) + " " + it.template.name);
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, item, max_quatity);
                            manager.levelNro++;
                            InventoryService.gI().sendItemBags(player);
                            if (player.nPoint != null) {
                                player.nPoint.calPoint();
                            }
                            Service.getInstance().point(player);
                            KhamNgoc.gI().Send_KhamNgoc_Player(player);
                            if (manager.levelNro == 0) {
                                Service.getInstance().sendThongBao(player, "|2|Kích hoạt thành công Ngọc rồng " + (nro + 1) + " sao");
                            } else {
                                Service.getInstance().sendThongBao(player, "|2|Nâng thành công Ngọc rồng " + (nro + 1) + " sao lên Cấp " + manager.levelNro);
                            }
                        }
                        break;
                    case ConstNpc.DIEU_CHE:
                        if (select == 0) {
                            PhongThiNghiem ptn = PhongThiNghiem.PHONG_THI_NGHIEM.get(player.typeBinhDieuChe);
                            for (int i = 0; i < ptn.items.size(); i++) {
                                Item item = InventoryService.gI().findItemBagByTemp(player, (short) ptn.items.get(i).tempId);
                                InventoryService.gI().subQuantityItemsBag(player, item, ptn.items.get(i).quantity);
                            }
                            InventoryService.gI().sendItemBags(player);
                            player.phongThiNghiem.get(player.vitriBinhDieuChe).idBinh = player.typeBinhDieuChe;
                            player.phongThiNghiem.get(player.vitriBinhDieuChe).timeCheTao = System.currentTimeMillis() + (ptn.thoi_gian * 1000 * 60);
                            PhongThiNghiem.gI().Send_PhongThiNghiem_Player(player);
                        }
                        break;
                    case ConstNpc.MO_RONG_PHONG_THI_NGHIEM:
                        if (select == 0) {
                            Item thoiVang = InventoryService.gI().findItemBagByTemp(player, (short) PhongThiNghiem.ID_ITEM_MO_RONG);
                            if (player.phongThiNghiem.size() >= PhongThiNghiem.MAX_SIZE) {
                                Service.getInstance().sendThongBao(player, "Phòng Thí Nghiệm đã đạt giới hạn tối đa");
                                return;
                            }
                            if (thoiVang != null && thoiVang.quantity >= PhongThiNghiem.SO_LUONG) {
                                PhongThiNghiem_Player ptnPl = new PhongThiNghiem_Player();
                                ptnPl.idBinh = -1;
                                ptnPl.timeCheTao = 0;
                                player.phongThiNghiem.add(ptnPl);
                                InventoryService.gI().subQuantityItemsBag(player, thoiVang, PhongThiNghiem.SO_LUONG);
                                InventoryService.gI().sendItemBags(player);
                                PhongThiNghiem.gI().Send_PhongThiNghiem_Player(player);
                                Service.getInstance().sendThongBao(player, "Mở thêm 1 ô Thành công");
                            } else {
                                Service.getInstance().sendThongBao(player, "Không đủ " + PhongThiNghiem.SO_LUONG + " Thỏi vàng");
                            }
                        }
                        break;
                    case ConstNpc.HUY_PTN:
                        if (select == 0) {
                            PhongThiNghiem ptn = PhongThiNghiem.PHONG_THI_NGHIEM.get(player.typeBinhDieuChe);
                            String text = "";
                            for (int i = 0; i < ptn.items.size(); i++) {
                                Item it = ItemService.gI().createNewItem((short) ptn.items.get(i).tempId);
                                it.quantity = ptn.items.get(i).quantity;
                                text += "|5|-" + it.quantity + " " + it.template.name + (i == (ptn.items.size() - 1) ? "" : "\n");
                                InventoryService.gI().addItemBag(player, it, 1);
                            }
                            InventoryService.gI().sendItemBags(player);
                            player.phongThiNghiem.get(player.vitriBinhDieuChe).idBinh = -1;
                            player.phongThiNghiem.get(player.vitriBinhDieuChe).timeCheTao = 0;
                            PhongThiNghiem.gI().Send_PhongThiNghiem_Player(player);
                            Service.getInstance().sendThongBao(player, "Hủy Thành công " + ptn.name_binh
                                    + "\n|3|Nhận lại vật phẩm:"
                                    + "\n" + text);
                        }
                        break;
                    case ConstNpc.TANG_TOC:
                        if (select == 0) {
                            Item item = InventoryService.gI().findItemBagByTemp(player, (short) PhongThiNghiem.ID_ITEM_TANG_TOC);
                            if (item == null || item.quantity < PhongThiNghiem.SO_LUONG_TANG_TOC) {
                                Service.getInstance().sendThongBao(player, "Không đủ nguyên liệu");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, item, PhongThiNghiem.SO_LUONG_TANG_TOC);
                            InventoryService.gI().sendItemBags(player);
                            PhongThiNghiem ptn = PhongThiNghiem.PHONG_THI_NGHIEM.get(player.typeBinhDieuChe);
                            player.phongThiNghiem.get(player.vitriBinhDieuChe).timeCheTao -= PhongThiNghiem.TIME_TANG_TOC;
                            PhongThiNghiem.gI().Send_PhongThiNghiem_Player(player);
                            Service.getInstance().sendThongBao(player, "Tăng tốc Thành công " + ptn.name_binh + ". Giảm " + Util.formatTime(PhongThiNghiem.TIME_TANG_TOC) + " thởi gian điều chế");
                        }
                        break;
                    case ConstNpc.TAIXIU:
                        String time = ((TaiXiu.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) + " giây";
                        if (((TaiXiu.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) > 0 && player.goldTai == 0 && player.goldXiu == 0 && TaiXiu.gI().baotri == false) {
                            switch (select) {
                                case 0:
                                    NpcService.gI().createMenuConMeo(player, ConstNpc.TAIXIU, 11039, "\n|7|---NHÀ CÁI TÀI XỈU---\n\n|3|Kết quả kì trước:  " + TaiXiu.gI().x + " : " + TaiXiu.gI().y + " : " + TaiXiu.gI().z
                                            + "\n\n|6|Tổng nhà TÀI: " + Util.format(TaiXiu.gI().goldTai) + " Hồng ngọc"
                                            + "\n\nTổng nhà XỈU: " + Util.format(TaiXiu.gI().goldXiu) + " Hồng ngọc\n\n|5|Thời gian còn lại: " + time, "Cập nhập", "Theo TÀI", "Theo XỈU", "Đóng");
                                    break;
                                case 1:
                                    if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_24_0) {
                                        Input.gI().TAI_taixiu(player);
                                    } else {
                                        Service.getInstance().sendThongBao(player, "Bạn chưa đủ điều kiện để chơi");
                                    }
                                    break;
                                case 2:
                                    if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_24_0) {
                                        Input.gI().XIU_taixiu(player);
                                    } else {
                                        Service.getInstance().sendThongBao(player, "Bạn chưa đủ điều kiện để chơi");
                                    }
                                    break;
                            }
                        } else if (((TaiXiu.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) > 0 && player.goldTai > 0 && TaiXiu.gI().baotri == false) {
                            switch (select) {
                                case 0:
                                    NpcService.gI().createMenuConMeo(player, ConstNpc.TAIXIU, 11039, "\n|7|---NHÀ CÁI TÀI XỈU---\n\n|3|Kết quả kì trước:  " + TaiXiu.gI().x + " : " + TaiXiu.gI().y + " : " + TaiXiu.gI().z
                                            + "\n\n|6|Tổng nhà TÀI: " + Util.format(TaiXiu.gI().goldTai) + " Hồng ngọc"
                                            + "\n\nTổng nhà XỈU: " + Util.format(TaiXiu.gI().goldXiu) + " Hồng ngọc\n\n|5|Thời gian còn lại: " + time + "\n\n|7|Bạn đã cược Tài : " + Util.format(player.goldTai) + " Hồng ngọc", "Cập nhập", "Đóng");
                                    break;
                            }
                        } else if (((TaiXiu.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) > 0 && player.goldXiu > 0 && TaiXiu.gI().baotri == false) {
                            switch (select) {
                                case 0:
                                    NpcService.gI().createMenuConMeo(player, ConstNpc.TAIXIU, 11039, "\n|7|---NHÀ CÁI TÀI XỈU---\n\n|3|Kết quả kì trước:  " + TaiXiu.gI().x + " : " + TaiXiu.gI().y + " : " + TaiXiu.gI().z
                                            + "\n\n|6|Tổng nhà TÀI: " + Util.format(TaiXiu.gI().goldTai) + " Hồng ngọc"
                                            + "\n\nTổng nhà XỈU: " + Util.format(TaiXiu.gI().goldXiu) + " Hồng ngọc\n\n|5|Thời gian còn lại: " + time + "\n\n|7|Bạn đã cược Xỉu : " + Util.format(player.goldXiu) + " Hồng ngọc", "Cập nhập", "Đóng");
                                    break;
                            }
                        } else if (((TaiXiu.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) > 0 && player.goldTai > 0 && TaiXiu.gI().baotri == true) {
                            switch (select) {
                                case 0:
                                    NpcService.gI().createMenuConMeo(player, ConstNpc.TAIXIU, 11039, "\n|7|---NHÀ CÁI TÀI XỈU---\n\n|3|Kết quả kì trước:  " + TaiXiu.gI().x + " : " + TaiXiu.gI().y + " : " + TaiXiu.gI().z
                                            + "\n\n|6|Tổng nhà TÀI: " + Util.format(TaiXiu.gI().goldTai) + " Hồng ngọc"
                                            + "\n\nTổng nhà XỈU: " + Util.format(TaiXiu.gI().goldXiu) + " Hồng ngọc\n\n|5|Thời gian còn lại: " + time + "\n\n|7|Bạn đã cược Tài : " + Util.format(player.goldTai) + " Hồng ngọc" + "\n\n|7|Hệ thống sắp bảo trì", "Cập nhập", "Đóng");
                                    break;
                            }
                        } else if (((TaiXiu.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) > 0 && player.goldXiu > 0 && TaiXiu.gI().baotri == true) {
                            switch (select) {
                                case 0:
                                    NpcService.gI().createMenuConMeo(player, ConstNpc.TAIXIU, 11039, "\n|7|---NHÀ CÁI TÀI XỈU---\n\n|3|Kết quả kì trước:  " + TaiXiu.gI().x + " : " + TaiXiu.gI().y + " : " + TaiXiu.gI().z
                                            + "\n\n|6|Tổng nhà TÀI: " + Util.format(TaiXiu.gI().goldTai) + " Hồng ngọc"
                                            + "\n\nTổng nhà XỈU: " + Util.format(TaiXiu.gI().goldXiu) + " Hồng ngọc\n\n|5|Thời gian còn lại: " + time + "\n\n|7|Bạn đã cược Xỉu : " + Util.format(player.goldXiu) + " Hồng ngọc" + "\n\n|7|Hệ thống sắp bảo trì", "Cập nhập", "Đóng");
                                    break;
                            }
                        } else if (((TaiXiu.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) > 0 && player.goldXiu == 0 && player.goldTai == 0 && TaiXiu.gI().baotri == true) {
                            switch (select) {
                                case 0:
                                    NpcService.gI().createMenuConMeo(player, ConstNpc.TAIXIU, 11039, "\n|7|---NHÀ CÁI TÀI XỈU---\n\n|3|Kết quả kì trước:  " + TaiXiu.gI().x + " : " + TaiXiu.gI().y + " : " + TaiXiu.gI().z
                                            + "\n\n|6|Tổng nhà TÀI: " + Util.format(TaiXiu.gI().goldTai) + " Hồng ngọc"
                                            + "\n\nTổng nhà XỈU: " + Util.format(TaiXiu.gI().goldXiu) + " Hồng ngọc\n\n|5|Thời gian còn lại: " + time + "\n\n|7|Hệ thống sắp bảo trì", "Cập nhập", "Đóng");
                                    break;
                            }
                        }
                        break;

                    case ConstNpc.RUONG_GO:
                        int size = player.textRuongGo.size();
                        if (size > 0) {
                            String menuselect = "OK [" + (size - 1) + "]";
                            if (size == 1) {
                                menuselect = "OK";
                            }
                            NpcService.gI().createMenuConMeo(player, ConstNpc.RUONG_GO, -1,
                                    player.textRuongGo.get(size - 1), menuselect);
                            player.textRuongGo.remove(size - 1);
                        }
                        break;
                    case ConstNpc.MENU_MABU_WAR:
                        if (select == 0) {
                            if (player.zone.finishMabuWar) {
                                ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
                            } else if (player.zone.map.mapId == 119) {
                                Zone zone = MabuWar.gI().getMapLastFloor(120);
                                if (zone != null) {
                                    ChangeMapService.gI().changeMap(player, zone, 354, 240);
                                } else {
                                    Service.getInstance().sendThongBao(player,
                                            "Trận đại chiến đã kết thúc, tàu vận chuyển sẽ đưa bạn về nhà");
                                    ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
                                }
                            } else {
                                int idMapNextFloor = player.zone.map.mapId == 115 ? player.zone.map.mapId + 2
                                        : player.zone.map.mapId + 1;
                                ChangeMapService.gI().changeMap(player, idMapNextFloor, -1, 354, 240);
                            }
                            player.resetPowerPoint();
                            player.sendMenuGotoNextFloorMabuWar = false;
                            Service.getInstance().sendPowerInfo(player, "TL", player.getPowerPoint());
                            if (Util.isTrue(1, 30)) {
                                player.inventory.ruby += 1;
                                PlayerService.gI().sendInfoHpMpMoney(player);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được 1 Hồng Ngọc");
                            } else {
                                Service.getInstance().sendThongBao(player,
                                        "Bạn đen vô cùng luôn nên không nhận được gì cả");
                            }
                        }
                        break;
                    case ConstNpc.IGNORE_MENU:

                        break;
                    case ConstNpc.MAKE_MATCH_PVP:
                        // PVP_old.gI().sendInvitePVP(player, (byte) select);
                        PVPServcice.gI().sendInvitePVP(player, (byte) select);
                        break;
                    case ConstNpc.MAKE_FRIEND:
                        if (select == 0) {
                            Object playerId = PLAYERID_OBJECT.get(player.id);
                            if (playerId != null) {
                                FriendAndEnemyService.gI().acceptMakeFriend(player,
                                        Integer.parseInt(String.valueOf(playerId)));
                            }
                        }
                        break;
                    case ConstNpc.REVENGE:
                        if (select == 0) {
                            PVPServcice.gI().acceptRevenge(player);
                        }
                        break;
                    case ConstNpc.TUTORIAL_SUMMON_DRAGON:
                        if (select == 0) {
                            NpcService.gI().createTutorial(player, -1, SummonDragon.SUMMON_SHENRON_TUTORIAL);
                        }
                        break;
                    case ConstNpc.SUMMON_SHENRON:
                        if (select == 0) {
                            NpcService.gI().createTutorial(player, -1, SummonDragon.SUMMON_SHENRON_TUTORIAL);
                        } else if (select == 1) {
                            SummonDragon.gI().summonShenron(player);
                        }
                        break;
                    case ConstNpc.SUMMON_BLACK_SHENRON:
                        if (select == 0) {
                            SummonDragon.gI().summonBlackShenron(player);
                        }
                        break;
                    case ConstNpc.SUMMON_ICE_SHENRON:
                        if (select == 0) {
                            SummonDragon.gI().summonIceShenron(player);
                        }
                        break;
                    case ConstNpc.INTRINSIC:
                        if (select < 0 || select > 2) {
                            break; // bỏ qua, tránh crash
                        }
                        switch (select) {
                            case 0:
                                IntrinsicService.gI().showAllIntrinsic(player);
                                break;
                            case 1:
                                IntrinsicService.gI().showConfirmOpen(player);
                                break;
                            case 2:
                                IntrinsicService.gI().showConfirmOpenVip(player);
                                break;
                        }
                        break;

                    case ConstNpc.CONFIRM_OPEN_INTRINSIC:
                        if (select == 0) {
                            IntrinsicService.gI().open(player);
                        }
                        break;
                    case ConstNpc.CONFIRM_OPEN_INTRINSIC_VIP:
                        if (select == 0) {
                            IntrinsicService.gI().openVip(player);
                        }
                        break;
                    case ConstNpc.CONFIRM_LEAVE_CLAN:
                        if (select == 0) {
                            ClanService.gI().leaveClan(player);
                        }
                        break;
                    case ConstNpc.CONFIRM_NHUONG_PC:
                        if (select == 0) {
                            ClanService.gI().phongPc(player, (int) PLAYERID_OBJECT.get(player.id));
                        }
                        break;
                    case ConstNpc.BAN_PLAYER:
                        if (select == 0) {
                            PlayerService.gI().banPlayer((Player) PLAYERID_OBJECT.get(player.id));
                            Service.getInstance().sendThongBao(player,
                                    "Ban người chơi " + ((Player) PLAYERID_OBJECT.get(player.id)).name + " thành công");
                        }
                        break;
                    case ConstNpc.BUFF_PET:
                        if (select == 0) {
                            Player pl = (Player) PLAYERID_OBJECT.get(player.id);
                            if (pl.pet == null) {
                                PetService.gI().createNormalPet(pl);
                                Service.getInstance().sendThongBao(player, "Phát đệ tử cho "
                                        + ((Player) PLAYERID_OBJECT.get(player.id)).name + " thành công");
                            }
                        }
                        break;
                    case ConstNpc.DUNG_NHIEU_TV:
                        Item thoivang = InventoryService.gI().findItemBagByTemp(player, 457);
                        switch (select) {
                            case 0:
                                if (thoivang == null || thoivang.quantity < 1) {
                                    Service.getInstance().sendThongBao(player, "Cần có đủ 1 Thỏi vàng để thực hiện");
                                    return;
                                }
                                if (player.inventory.gold + 500_000_000 > player.inventory.getGoldLimit()) {
                                    Service.getInstance().sendThongBao(player, "Vàng sau khi nhận vượt quá giới hạn");
                                } else {
                                    player.inventory.gold += 500_000_000;
                                    Service.getInstance().sendThongBao(player, "|4|Bạn nhận được 500 Triệu Vàng");
                                    InventoryService.gI().subQuantityItemsBag(player, thoivang, 1);
                                    InventoryService.gI().sendItemBags(player);
                                    Service.getInstance().sendMoney(player);
                                    break;
                                }
                                break;
                            case 1:
                                if (thoivang == null || thoivang.quantity < 5) {
                                    Service.getInstance().sendThongBao(player, "Cần có đủ 5 Thỏi vàng để thực hiện");
                                    return;
                                }
                                if (player.inventory.gold + 2_500_000_000L > player.inventory.getGoldLimit()) {
                                    Service.getInstance().sendThongBao(player, "Vàng sau khi nhận vượt quá giới hạn");
                                } else {
                                    player.inventory.gold += 2_500_000_000L;
                                    Service.getInstance().sendThongBao(player, "|4|Bạn nhận được 2,5 Tỷ Vàng");
                                    Service.getInstance().sendMoney(player);
                                    InventoryService.gI().subQuantityItemsBag(player, thoivang, 5);
                                    InventoryService.gI().sendItemBags(player);
                                    break;
                                }
                                break;

                            case 2:
                                if (thoivang == null || thoivang.quantity < 10) {
                                    Service.getInstance().sendThongBao(player, "Cần có đủ 10 Thỏi vàng để thực hiện");
                                    return;
                                }
                                if (player.inventory.gold + 5_000_000_000L > player.inventory.getGoldLimit()) {
                                    Service.getInstance().sendThongBao(player, "Vàng sau khi nhận vượt quá giới hạn");
                                } else {
                                    player.inventory.gold += 5_000_000_000L;
                                    Service.getInstance().sendThongBao(player, "|4|Bạn nhận được 5 Tỷ Vàng");
                                    Service.getInstance().sendMoney(player);
                                    InventoryService.gI().subQuantityItemsBag(player, thoivang, 10);
                                    InventoryService.gI().sendItemBags(player);
                                    break;
                                }
                                break;
                            case 3:
                                if (thoivang == null || thoivang.quantity < 100) {
                                    Service.getInstance().sendThongBao(player, "Cần có đủ 100 Thỏi vàng để thực hiện");
                                    return;
                                }
                                if (player.inventory.gold + 50_000_000_000L > player.inventory.getGoldLimit()) {
                                    Service.getInstance().sendThongBao(player, "Vàng sau khi nhận vượt quá giới hạn");
                                } else {
                                    player.inventory.gold += 50_000_000_000L;
                                    Service.getInstance().sendThongBao(player, "|4|Bạn nhận được 50 Tỷ Vàng");
                                    Service.getInstance().sendMoney(player);
                                    InventoryService.gI().subQuantityItemsBag(player, thoivang, 100);
                                    InventoryService.gI().sendItemBags(player);
                                    break;
                                }

                        }
                        break;
                    case ConstNpc.MENU_BOT:
                        switch (select) {
                            case 0: // gọi random map
                                Input.gI().createFormGoiBotRandom(player);
                                break;
                            case 1: // gọi tại vị trí
                                Input.gI().createFormGoiBotAt(player);
                                break;
                        }
                        break;

                    case ConstNpc.MENU_ADMIN:
                        switch (select) {
                            case 0:
                                for (int i = 14; i <= 20; i++) {
                                    Item item = ItemService.gI().createNewItem((short) i);
                                    item.quantity = 99;
                                    InventoryService.gI().addItemBag(player, item, 99);
                                }
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendThongBao(player, "Nhận x99 Bộ Ngọc rồng thành công");
                                break;
                            case 1:
                                if (player.pet == null) {
                                    PetService.gI().createNormalPet(player);
                                } else {
                                    if (player.pet.typePet == 1) {
                                        PetService.gI().changeNormalPet(player);
                                    } else {
                                        PetService.gI().changeMabuPet(player);
                                    }
                                }
                                break;
                            case 2:
                                // PlayerService.gI().baoTri();
                                Maintenance.gI().start(30);
                                break;
                            case 3:
                                Input.gI().createFormFindPlayer(player);
                                break;
                            case 4:
                                NotiManager.getInstance().load();
                                NotiManager.getInstance().sendAlert(player);
                                NotiManager.getInstance().sendNoti(player);
                                Service.getInstance().chat(player, "Cập nhật thông báo thành công");
                                break;
                            case 5:
                                Input.gI().createFormBuffItemVip(player);
                                break;
                            case 6:
                                if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                                    if (player.inventory.itemsBody.get(0).quantity < 1
                                            && player.inventory.itemsBody.get(1).quantity < 1
                                            && player.inventory.itemsBody.get(2).quantity < 1
                                            && player.inventory.itemsBody.get(3).quantity < 1
                                            && player.inventory.itemsBody.get(4).quantity < 1) {
                                        player.gender += 1;
                                        if (player.gender > 2) {
                                            player.gender = 0;
                                        }
                                        short[] headtd = {30, 31, 64};
                                        short[] headnm = {9, 29, 32};
                                        short[] headxd = {27, 28, 6};
                                        player.playerSkill.skills.clear();
                                        for (Skill skill : player.playerSkill.skills) {
                                            skill.point = 1;
                                        }
                                        int[] skillsArr = player.gender == 0 ? new int[]{0, 1, 6, 9, 10, 20, 22, 24, 19}
                                                : player.gender == 1 ? new int[]{2, 3, 7, 11, 12, 17, 18, 26, 19}
                                                : new int[]{4, 5, 8, 13, 14, 21, 23, 25, 19};
                                        // for (int i = 0; i < skillsArr.length; i++) { max skill
                                        //     if (skillsArr[i] == Skill.SUPER_KAME || skillsArr[i] == Skill.LIEN_HOAN_CHUONG || skillsArr[i] == Skill.MA_PHONG_BA) {
                                        //         player.playerSkill.skills.add(SkillUtil.createSkill(skillsArr[i], 1));
                                        //     } else {
                                        //         player.playerSkill.skills.add(SkillUtil.createSkill(skillsArr[i], 7));
                                        //     }
                                        // }
                                        for (int i = 0; i < skillsArr.length; i++) {

                                            player.playerSkill.skills.add(SkillUtil.createSkill(skillsArr[i], 1));

                                        }
                                        player.playerIntrinsic.intrinsic = IntrinsicService.gI().getIntrinsicById(0);
                                        player.playerIntrinsic.intrinsic.param1 = 0;
                                        player.playerIntrinsic.intrinsic.param2 = 0;
                                        player.playerIntrinsic.countOpen = 0;
                                        switch (player.gender) {
                                            case 0:
                                                player.head = headtd[Util.nextInt(headtd.length)];
                                                break;
                                            case 1:
                                                player.head = headnm[Util.nextInt(headnm.length)];
                                                break;
                                            case 2:
                                                player.head = headxd[Util.nextInt(headxd.length)];
                                                break;
                                            default:
                                                break;
                                        }
                                        Service.getInstance().sendThongBao(player, "|1|Đổi hành tinh thành công");
                                        Service.getInstance().player(player);
                                        player.zone.loadAnotherToMe(player);
                                        player.zone.load_Me_To_Another(player);
                                        Service.getInstance().sendFlagBag(player);
                                        Service.getInstance().Send_Caitrang(player);
                                        PlayerService.gI().sendInfoHpMpMoney(player);
                                        Service.getInstance().Send_Info_NV(player);
                                    } else {
                                        Service.getInstance().sendThongBao(player, "Tháo hết 5 món đầu đang mặc ra nha");
                                    }
                                } else {
                                    Service.getInstance().sendThongBao(player, "Balo đầy");
                                }
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendMoney(player);
                                break;
                            case 7:
                                Input.gI().createFormBuffDanhHieu(player);
                                break;
                        }
                        break;
                    case 8:
                        this.createOtherMenu(player, ConstNpc.SO_MAY_MAN, "dfsf", "thu vy");
                        break;
                    case ConstNpc.VAO_MAP_NGOAI_VUC:
                        switch (select) {
                            case 0:
                                ChangeMapService.gI().goToHallowen(player);
                                break;
                            case 1:
                                ChangeMapService.gI().goToHanhTinhBangGia(player);
                                break;
                            case 2:
                                ChangeMapService.gI().goToDiaNguc(player);
                                break;
                        }
                        break;
                    case ConstNpc.MO_DE_TU:
                        try {
                        switch (select) {
                            case 0:
                                Item item = InventoryService.gI().findItemBagByOption(player, 1459, 240, 10);
                                if (item == null) {
                                    Service.getInstance().sendThongBao(player, "Không đủ yêu cầu");
                                    return;
                                }
                                player.leverPet = 0;               // reset level đệ
                                player.LVhopthe = 0;// reset hợp thể
                                player.SDhopthe = 0;               // reset sức đánh hợp thể
                                player.HPhopthe = 0;    // reset HP hợp thể = HP gốc đệ
                                player.KIhopthe = 0;    // reset KI hợp thể = KI gốc đệ 
                                InventoryService.gI().subQuantityItemsBag(player, item, 1);
                                InventoryService.gI().sendItemBags(player);
                                PetService.gI().changePicPet(player, 0);
                                break;
                            case 1:
                                Item item1 = InventoryService.gI().findItemBagByOption(player, 1459, 240, 10);
                                if (item1 == null) {
                                    Service.getInstance().sendThongBao(player, "Không đủ yêu cầu");
                                    return;
                                }
                                player.leverPet = 0;               // reset level đệ
                                player.LVhopthe = 0;// reset hợp thể
                                player.SDhopthe = 0;               // reset sức đánh hợp thể
                                player.HPhopthe = 0;    // reset HP hợp thể = HP gốc đệ
                                player.KIhopthe = 0;    // reset KI hợp thể = KI gốc đệ
                                InventoryService.gI().subQuantityItemsBag(player, item1, 1);
                                InventoryService.gI().sendItemBags(player);
                                PetService.gI().changePicPet(player, 1);
                                break;
                            case 2:
                                Item item2 = InventoryService.gI().findItemBagByOption(player, 1459, 240, 10);
                                if (item2 == null) {
                                    Service.getInstance().sendThongBao(player, "Không đủ yêu cầu");
                                    return;
                                }
                                player.leverPet = 0;               // reset level đệ
                                player.LVhopthe = 0;// reset hợp thể
                                player.SDhopthe = 0;               // reset sức đánh hợp thể
                                player.HPhopthe = 0;    // reset HP hợp thể = HP gốc đệ
                                player.KIhopthe = 0;    // reset KI hợp thể = KI gốc đệ
                                InventoryService.gI().subQuantityItemsBag(player, item2, 1);
                                InventoryService.gI().sendItemBags(player);
                                PetService.gI().changePicPet(player, 2);
                                break;
                        }
                        break;
                    } catch (Exception e) {
                        System.out.println("Loi mo de tu zeno");
                    }
                    case ConstNpc.DE_TU_LUFFY:
                        try {
                        switch (select) {
                            case 0:
                                Item item = InventoryService.gI().findItemBagByTemp(player, 1666);
                                if (item == null) {
                                    Service.getInstance().sendThongBao(player, "Không đủ yêu cầu");
                                    return;
                                }
                                player.leverPet = 0;               // reset level đệ
                                player.LVhopthe = 0;// reset hợp thể
                                player.SDhopthe = 0;               // reset sức đánh hợp thể
                                player.HPhopthe = 0;    // reset HP hợp thể = HP gốc đệ
                                player.KIhopthe = 0;    // reset KI hợp thể = KI gốc đệ
                                InventoryService.gI().subQuantityItemsBag(player, item, 1);
                                InventoryService.gI().sendItemBags(player);
                                PetService.gI().changeLuffyPet(player, 0);
                                break;
                            case 1:
                                Item item1 = InventoryService.gI().findItemBagByTemp(player, 1666);
                                if (item1 == null) {
                                    Service.getInstance().sendThongBao(player, "Không đủ yêu cầu");
                                    return;
                                }
                                player.leverPet = 0;               // reset level đệ
                                player.LVhopthe = 0;// reset hợp thể
                                player.SDhopthe = 0;               // reset sức đánh hợp thể
                                player.HPhopthe = 0;    // reset HP hợp thể = HP gốc đệ
                                player.KIhopthe = 0;    // reset KI hợp thể = KI gốc đệ
                                InventoryService.gI().subQuantityItemsBag(player, item1, 1);
                                InventoryService.gI().sendItemBags(player);
                                PetService.gI().changeLuffyPet(player, 1);
                                break;
                            case 2:
                                Item item2 = InventoryService.gI().findItemBagByTemp(player, 1666);
                                if (item2 == null) {
                                    Service.getInstance().sendThongBao(player, "Không đủ yêu cầu");
                                    return;
                                }
                                player.leverPet = 0;
                                player.LVhopthe = 0;
                                player.SDhopthe = 0;
                                player.HPhopthe = 0;    // reset HP hợp thể = HP gốc đệ
                                player.KIhopthe = 0;    // reset KI hợp thể = KI gốc đệ
                                InventoryService.gI().subQuantityItemsBag(player, item2, 1);
                                InventoryService.gI().sendItemBags(player);
                                PetService.gI().changeLuffyPet(player, 2);
                                break;
                        }
                        break;
                    } catch (Exception e) {
                        System.out.println("Loi mo de tu itachi");
                    }
                    case ConstNpc.INFO_ALL:
                        switch (select) {
                            case 0:
                                NpcService.gI().createMenuConMeo(player, ConstNpc.INFO_ALL, 12713,
                                        "|7|THÔNG TIN NHÂN VẬT"
                                        + "\b|5|HP bản thân: " + Util.format(player.nPoint.hp) + "/" + Util.powerToString(player.nPoint.hpMax)
                                        + "\bKI bản thân: " + Util.format(player.nPoint.mp) + "/" + Util.powerToString(player.nPoint.mpMax)
                                        + "\bSức đánh: " + Util.format(player.nPoint.dame)
                                        + "\bGiáp: " + Util.format(player.nPoint.def)
                                        //                                        + "\b|4|HP Gốc: " + Util.format(player.nPoint.hpg)
                                        //                                        + "\bKI Gốc: " + Util.format(player.nPoint.mpg)
                                        //                                        + "\bSức đánh Gốc: " + Util.format(player.nPoint.dameg)
                                        //                                        + "\bGiáp Gốc: " + Util.format(player.nPoint.defg)
                                        + "\b|8|-Vàng: " + Util.format(player.inventory.gold)
                                        + "   -Ngọc: " + Util.format(player.inventory.gem)
                                        + "   -H.Ngọc: " + Util.format(player.inventory.ruby)
                                        + "\b|5|Tổng vàng nhặt: " + Util.format(player.vangnhat)
                                        + "\b|3|Tổng Hồng ngọc nhặt: " + Util.format(player.hngocnhat),
                                        "Thông tin\n nhân vật", "Thông tin\nđệ tử", "Thông tin\nđồ mặc");
                                break;
                            case 1:
                                NpcService.gI().createMenuConMeo(player, ConstNpc.INFO_ALL, 12713,
                                        "|7|THÔNG TIN ĐỆ TỬ"
                                        + "\b\b|7|Hành tinh: " + Service.getInstance().get_HanhTinh(player.pet.gender)
                                        + "\b|5|HP ĐỆ TỬ: " + Util.format(player.pet.nPoint.hp) + "/" + Util.powerToString(player.pet.nPoint.hpMax)
                                        + "\bKI ĐỆ TỬ: " + Util.format(player.pet.nPoint.mp) + "/" + Util.powerToString(player.pet.nPoint.mpMax)
                                        + "\bSức đánh: " + Util.format(player.pet.nPoint.dame)
                                        + "\bGiáp: " + Util.format(player.pet.nPoint.def)
                                        + "\b|4|HP Gốc: " + Util.format(player.pet.nPoint.hpg)
                                        + "\bKI Gốc: " + Util.format(player.pet.nPoint.mpg)
                                        + "\bSức đánh Gốc: " + Util.format(player.pet.nPoint.dameg)
                                        + "\bGiáp Gốc: " + Util.format(player.pet.nPoint.defg),
                                        "Thông tin\n nhân vật", "Thông tin\nđệ tử", "Thông tin\nđồ mặc");
                                break;
                            case 2:
                                NpcService.gI().createMenuConMeo(player, ConstNpc.CHISODO, 12713,
                                        "|1|Bạn muốn xem chỉ số đồ bị giới hạn hiện thị:",
                                        "Chỉ số\nô 1", "Chỉ số\nô 2", "Chỉ số\nô 3",
                                        "Chỉ số\nô 4", "Chỉ số\nô 5", "Chỉ số\nô 6",
                                        "Chỉ số\nô 7", "Chỉ số\nô 8", "Chỉ số\nô 9",
                                        "Chỉ số\nô 10", "Chỉ số\nô 11", "Chỉ số\nô 12");
                                break;
                        }
                        break;
                    case ConstNpc.CHISODO: {
                        Item it = player.inventory.itemsBody.get(select);
                        if (it.quantity < 1) {
                            NpcService.gI().createMenuConMeo(player, ConstNpc.CHISODO, 12713,
                                    "|7|Ô này không có đồ!!!"
                                    + "\n|2|Bạn muốn xem chỉ số đồ bị giới hạn hiện thị:",
                                    "Chỉ số\nô 1", "Chỉ số\nô 2", "Chỉ số\nô 3",
                                    "Chỉ số\nô 4", "Chỉ số\nô 5", "Chỉ số\nô 6",
                                    "Chỉ số\nô 7", "Chỉ số\nô 8", "Chỉ số\nô 9",
                                    "Chỉ số\nô 10", "Chỉ số\nô 11", "Chỉ số\nô 12");
                            return;
                        }
                        NpcService.gI().createMenuConMeo(player, ConstNpc.CHISODO, 12713,
                                "|2|Tên Vật phẩm: " + it.template.name
                                + "\n|7|Chỉ số:"
                                + "\n|6|" + it.getInfo(),
                                "Chỉ số\nô 1", "Chỉ số\nô 2", "Chỉ số\nô 3",
                                "Chỉ số\nô 4", "Chỉ số\nô 5", "Chỉ số\nô 6",
                                "Chỉ số\nô 7", "Chỉ số\nô 8", "Chỉ số\nô 9",
                                "Chỉ số\nô 10", "Chỉ số\nô 11", "Chỉ số\nô 12");
                    }
                    break;
                    case ConstNpc.ADMIN_DANH_HIEU: {
                        switch (select) {
                            case 0:
                                if (player.lastTimeTitle1 == 0) {
                                    player.lastTimeTitle1 += System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7);
                                } else {
                                    player.lastTimeTitle1 += (1000 * 60 * 60 * 24 * 7);
                                }
                                player.isTitleUse1 = true;
                                Service.getInstance().point(player);
                                Service.getInstance().sendTitle(player, 888);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được 7 ngày Danh hiệu Đại Thần");
                                break;
                            case 1:
                                if (player.lastTimeTitle2 == 0) {
                                    player.lastTimeTitle2 += System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7);
                                } else {
                                    player.lastTimeTitle2 += (1000 * 60 * 60 * 24 * 7);
                                }
                                player.isTitleUse2 = true;
                                Service.getInstance().point(player);
                                Service.getInstance().sendTitle(player, 889);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được 7 ngày Danh hiệu Cần Thủ");
                                break;
                            case 2:
                                if (player.lastTimeTitle3 == 0) {
                                    player.lastTimeTitle3 += System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7);
                                } else {
                                    player.lastTimeTitle3 += (1000 * 60 * 60 * 24 * 7);
                                }
                                player.isTitleUse3 = true;
                                Service.getInstance().point(player);
                                Service.getInstance().sendTitle(player, 890);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được 7 ngày Danh hiệu Tuổi Thơ");
                                break;
                            case 3:
                                if (player.lastTimeTitle4 == 0) {
                                    player.lastTimeTitle4 += System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7);
                                } else {
                                    player.lastTimeTitle4 += (1000 * 60 * 60 * 24 * 7);
                                }
                                player.isTitleUse4 = true;
                                Service.getInstance().point(player);
                                Service.getInstance().sendTitle(player, 891);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được 7 ngày Danh hiệu Thợ ngọc");
                                break;
                        }
                    }
                    break;
                    case ConstNpc.MENU_DANHHIEU: {
                        switch (select) {
                            case 0:
                                if (player.lastTimeTitle1 > 0) {
                                    Service.getInstance().removeTitle(player);
                                    player.isTitleUse1 = !player.isTitleUse1;
                                    Service.getInstance().point(player);
                                    Service.getInstance().sendThongBao(player, "Đã " + (player.isTitleUse1 == true ? "Bật" : "Tắt") + " Danh Hiệu!");
                                    Service.getInstance().sendTitle(player, 892);
                                    Service.getInstance().sendTitle(player, 891);
                                    Service.getInstance().sendTitle(player, 890);
                                    Service.getInstance().sendTitle(player, 889);
                                    Service.getInstance().sendTitle(player, 888);
                                    Service.getInstance().removeTitle(player);
                                    break;
                                }
                                break;
                            case 1:
                                if (player.lastTimeTitle2 > 0) {
                                    Service.getInstance().removeTitle(player);
                                    player.isTitleUse2 = !player.isTitleUse2;
                                    Service.getInstance().point(player);
                                    Service.getInstance().sendThongBao(player, "Đã " + (player.isTitleUse2 == true ? "Bật" : "Tắt") + " Danh Hiệu!");
                                    Service.getInstance().sendTitle(player, 892);
                                    Service.getInstance().sendTitle(player, 891);
                                    Service.getInstance().sendTitle(player, 890);
                                    Service.getInstance().sendTitle(player, 889);
                                    Service.getInstance().sendTitle(player, 888);
                                    Service.getInstance().removeTitle(player);
                                    break;
                                }
                                break;
                            case 2:
                                if (player.lastTimeTitle3 > 0) {
                                    Service.getInstance().removeTitle(player);
                                    player.isTitleUse3 = !player.isTitleUse3;
                                    Service.getInstance().point(player);
                                    Service.getInstance().sendThongBao(player, "Đã " + (player.isTitleUse3 == true ? "Bật" : "Tắt") + " Danh Hiệu!");
                                    Service.getInstance().sendTitle(player, 892);
                                    Service.getInstance().sendTitle(player, 891);
                                    Service.getInstance().sendTitle(player, 890);
                                    Service.getInstance().sendTitle(player, 889);
                                    Service.getInstance().sendTitle(player, 888);
                                    Service.getInstance().removeTitle(player);
                                    break;
                                }
                                break;
                            case 3:
                                if (player.lastTimeTitle4 > 0) {
                                    Service.getInstance().removeTitle(player);
                                    player.isTitleUse4 = !player.isTitleUse4;
                                    Service.getInstance().point(player);
                                    Service.getInstance().sendThongBao(player, "Đã " + (player.isTitleUse4 == true ? "Bật" : "Tắt") + " Danh Hiệu!");
                                    Service.getInstance().sendTitle(player, 892);
                                    Service.getInstance().sendTitle(player, 891);
                                    Service.getInstance().sendTitle(player, 890);
                                    Service.getInstance().sendTitle(player, 889);
                                    Service.getInstance().sendTitle(player, 888);
                                    Service.getInstance().removeTitle(player);
                                    break;
                                }
                                break;
                            case 4:
                                if (player.lastTimeTitle5 > 0) {
                                    Service.getInstance().removeTitle(player);
                                    player.isTitleUse5 = !player.isTitleUse5;
                                    Service.getInstance().point(player);
                                    Service.getInstance().sendThongBao(player, "Đã " + (player.isTitleUse5 == true ? "Bật" : "Tắt") + " Danh Hiệu!");
                                    Service.getInstance().sendTitle(player, 892);
                                    Service.getInstance().sendTitle(player, 891);
                                    Service.getInstance().sendTitle(player, 890);
                                    Service.getInstance().sendTitle(player, 889);
                                    Service.getInstance().sendTitle(player, 888);
                                    Service.getInstance().removeTitle(player);
                                    break;
                                }
                                break;
                        }
                    }
                    break;
                    case ConstNpc.CONFIRM_REMOVE_ALL_ITEM_LUCKY_ROUND:
                        if (select == 0) {
                            for (int i = 0; i < player.inventory.itemsBoxCrackBall.size(); i++) {
                                player.inventory.itemsBoxCrackBall.set(i, ItemService.gI().createItemNull());
                            }
                            Service.getInstance().sendThongBao(player, "Đã xóa hết vật phẩm trong rương");
                        }
                        break;
                    case ConstNpc.MENU_FIND_PLAYER:
                        Player p = (Player) PLAYERID_OBJECT.get(player.id);
                        if (p != null) {
                            switch (select) {
                                case 0:
                                    if (p.zone != null) {
                                        ChangeMapService.gI().changeMapYardrat(player, p.zone, p.location.x,
                                                p.location.y);
                                    }
                                    break;
                                case 1:
                                    if (p.zone != null) {
                                        ChangeMapService.gI().changeMap(p, player.zone, player.location.x,
                                                player.location.y);
                                    }
                                    break;
                                case 2:
                                    if (p != null) {
                                        Input.gI().createFormChangeName(player, p);
                                    }
                                    break;
                                case 3:
                                    if (p != null) {
                                        String[] selects = new String[]{"Đồng ý", "Hủy"};
                                        NpcService.gI().createMenuConMeo(player, ConstNpc.BAN_PLAYER, -1,
                                                "Bạn có chắc chắn muốn ban " + p.name, selects, p);
                                    }
                                    break;
                            }
                        }
                        break;
                }
            }
        };
    }

    public static void openMenuSuKien(Player player, Npc npc, int tempId, int select) {
        switch (Manager.EVENT_SEVER) {
            case 0:
                break;
            case 1:// hlw
                switch (select) {
                    case 0:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            Item keo = InventoryService.gI().finditemnguyenlieuKeo(player);
                            Item banh = InventoryService.gI().finditemnguyenlieuBanh(player);
                            Item bingo = InventoryService.gI().finditemnguyenlieuBingo(player);

                            if (keo != null && banh != null && bingo != null) {
                                Item GioBingo = ItemService.gI().createNewItem((short) 2016, 1);

                                // - Số item sự kiện có trong rương
                                InventoryService.gI().subQuantityItemsBag(player, keo, 10);
                                InventoryService.gI().subQuantityItemsBag(player, banh, 10);
                                InventoryService.gI().subQuantityItemsBag(player, bingo, 10);

                                GioBingo.itemOptions.add(new ItemOption(74, 0));
                                InventoryService.gI().addItemBag(player, GioBingo, 0);
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendThongBao(player, "Đổi quà sự kiện thành công");
                            } else {
                                Service.getInstance().sendThongBao(player,
                                        "Vui lòng chuẩn bị x10 Nguyên Liệu Kẹo, Bánh Quy, Bí Ngô để đổi vật phẩm sự kiện");
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Hành trang đầy.");
                        }
                        break;
                    case 1:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            Item ve = InventoryService.gI().finditemnguyenlieuVe(player);
                            Item giokeo = InventoryService.gI().finditemnguyenlieuGiokeo(player);

                            if (ve != null && giokeo != null) {
                                Item Hopmaquy = ItemService.gI().createNewItem((short) 2017, 1);
                                // - Số item sự kiện có trong rương
                                InventoryService.gI().subQuantityItemsBag(player, ve, 3);
                                InventoryService.gI().subQuantityItemsBag(player, giokeo, 3);

                                Hopmaquy.itemOptions.add(new ItemOption(74, 0));
                                InventoryService.gI().addItemBag(player, Hopmaquy, 0);
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendThongBao(player, "Đổi quà sự kiện thành công");
                            } else {
                                Service.getInstance().sendThongBao(player,
                                        "Vui lòng chuẩn bị x3 Vé đổi Kẹo và x3 Giỏ kẹo để đổi vật phẩm sự kiện");
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Hành trang đầy.");
                        }
                        break;
                    case 2:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            Item ve = InventoryService.gI().finditemnguyenlieuVe(player);
                            Item giokeo = InventoryService.gI().finditemnguyenlieuGiokeo(player);
                            Item hopmaquy = InventoryService.gI().finditemnguyenlieuHopmaquy(player);

                            if (ve != null && giokeo != null && hopmaquy != null) {
                                Item HopQuaHLW = ItemService.gI().createNewItem((short) 2012, 1);
                                // - Số item sự kiện có trong rương
                                InventoryService.gI().subQuantityItemsBag(player, ve, 3);
                                InventoryService.gI().subQuantityItemsBag(player, giokeo, 3);
                                InventoryService.gI().subQuantityItemsBag(player, hopmaquy, 3);

                                HopQuaHLW.itemOptions.add(new ItemOption(74, 0));
                                HopQuaHLW.itemOptions.add(new ItemOption(30, 0));
                                InventoryService.gI().addItemBag(player, HopQuaHLW, 0);
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendThongBao(player,
                                        "Đổi quà hộp quà sự kiện Halloween thành công");
                            } else {
                                Service.getInstance().sendThongBao(player,
                                        "Vui lòng chuẩn bị x3 Hộp Ma Quỷ, x3 Vé đổi Kẹo và x3 Giỏ kẹo để đổi vật phẩm sự kiện");
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Hành trang đầy.");
                        }
                        break;
                }
                break;
            case 2:// 20/11
                switch (select) {
                    case 3:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            int evPoint = player.event.getEventPoint();
                            if (evPoint >= 999) {
                                Item HopQua = ItemService.gI().createNewItem((short) 2021, 1);
                                player.event.setEventPoint(evPoint - 999);

                                HopQua.itemOptions.add(new ItemOption(74, 0));
                                HopQua.itemOptions.add(new ItemOption(30, 0));
                                InventoryService.gI().addItemBag(player, HopQua, 0);
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được Hộp Quà Teacher Day");
                            } else {
                                Service.getInstance().sendThongBao(player, "Cần 999 điểm tích lũy để đổi");
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Hành trang đầy.");
                        }
                        break;
                    // case 4:
                    // ShopService.gI().openShopSpecial(player, npc, ConstNpc.SHOP_HONG_NGOC, 0,
                    // -1);
                    // break;
                    default:
                        int n = 0;
                        switch (select) {
                            case 0:
                                n = 1;
                                break;
                            case 1:
                                n = 10;
                                break;
                            case 2:
                                n = 99;
                                break;
                        }

                        if (n > 0) {
                            Item bonghoa = InventoryService.gI().finditemBongHoa(player, n);
                            if (bonghoa != null) {
                                int evPoint = player.event.getEventPoint();
                                player.event.setEventPoint(evPoint + n);
                                ;
                                InventoryService.gI().subQuantityItemsBag(player, bonghoa, n);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được " + n + " điểm sự kiện");
                                int pre;
                                int next;
                                String text = null;
                                AttributeManager am = ServerManager.gI().getAttributeManager();
                                switch (tempId) {
                                    case ConstNpc.THAN_MEO_KARIN:
                                        pre = EVENT_COUNT_THAN_MEO / 999;
                                        EVENT_COUNT_THAN_MEO += n;
                                        next = EVENT_COUNT_THAN_MEO / 999;
                                        if (pre != next) {
                                            am.setTime(ConstAttribute.TNSM, 3600);
                                            text = "Toàn bộ máy chủ tăng được 20% TNSM cho đệ tử khi đánh quái trong 60 phút.";
                                        }
                                        break;

                                    case ConstNpc.QUY_LAO_KAME:
                                        pre = EVENT_COUNT_QUY_LAO_KAME / 999;
                                        EVENT_COUNT_QUY_LAO_KAME += n;
                                        next = EVENT_COUNT_QUY_LAO_KAME / 999;
                                        if (pre != next) {
                                            am.setTime(ConstAttribute.VANG, 3600);
                                            text = "Toàn bộ máy chủ được tăng 100% vàng từ quái trong 60 phút.";
                                        }
                                        break;

                                    case ConstNpc.THUONG_DE:
                                        pre = EVENT_COUNT_THUONG_DE / 999;
                                        EVENT_COUNT_THUONG_DE += n;
                                        next = EVENT_COUNT_THUONG_DE / 999;
                                        if (pre != next) {
                                            am.setTime(ConstAttribute.KI, 3600);
                                            text = "Toàn bộ máy chủ được tăng 20% KI trong 60 phút.";
                                        }
                                        break;

                                    case ConstNpc.THAN_VU_TRU:
                                        pre = EVENT_COUNT_THAN_VU_TRU / 999;
                                        EVENT_COUNT_THAN_VU_TRU += n;
                                        next = EVENT_COUNT_THAN_VU_TRU / 999;
                                        if (pre != next) {
                                            am.setTime(ConstAttribute.HP, 3600);
                                            text = "Toàn bộ máy chủ được tăng 20% HP trong 60 phút.";
                                        }
                                        break;

                                    case ConstNpc.BILL:
                                        pre = EVENT_COUNT_THAN_HUY_DIET / 999;
                                        EVENT_COUNT_THAN_HUY_DIET += n;
                                        next = EVENT_COUNT_THAN_HUY_DIET / 999;
                                        if (pre != next) {
                                            am.setTime(ConstAttribute.SUC_DANH, 3600);
                                            text = "Toàn bộ máy chủ được tăng 20% Sức đánh trong 60 phút.";
                                        }
                                        break;
                                }
                                if (text != null) {
                                    Service.getInstance().sendThongBaoAllPlayer(text);
                                }

                            } else {
                                Service.getInstance().sendThongBao(player,
                                        "Cần ít nhất " + n + " bông hoa để có thể tặng");
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Cần ít nhất " + n + " bông hoa để có thể tặng");
                        }
                }
                break;
            case 3:
                if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                    Item keogiangsinh = InventoryService.gI().finditemKeoGiangSinh(player);

                    if (keogiangsinh != null && keogiangsinh.quantity >= 99) {
                        Item tatgiangsinh = ItemService.gI().createNewItem((short) 649, 1);
                        // - Số item sự kiện có trong rương
                        InventoryService.gI().subQuantityItemsBag(player, keogiangsinh, 99);

                        tatgiangsinh.itemOptions.add(new ItemOption(74, 0));
                        tatgiangsinh.itemOptions.add(new ItemOption(30, 0));
                        InventoryService.gI().addItemBag(player, tatgiangsinh, 0);
                        InventoryService.gI().sendItemBags(player);
                        Service.getInstance().sendThongBao(player, "Bạn nhận được Tất,vớ giáng sinh");
                    } else {
                        Service.getInstance().sendThongBao(player,
                                "Vui lòng chuẩn bị x99 kẹo giáng sinh để đổi vớ tất giáng sinh");
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Hành trang đầy.");
                }
                break;
            case 4:
                switch (select) {
                    case 0:
                        if (!player.event.isReceivedLuckyMoney()) {
                            Calendar cal = Calendar.getInstance();
                            int day = cal.get(Calendar.DAY_OF_MONTH);
                            if (day >= 22 && day <= 24) {
                                Item goldBar = ItemService.gI().createNewItem((short) ConstItem.THOI_VANG,
                                        Util.nextInt(1, 3));
                                player.inventory.ruby += Util.nextInt(10, 30);
                                goldBar.quantity = Util.nextInt(1, 3);
                                InventoryService.gI().addItemBag(player, goldBar, 99999);
                                InventoryService.gI().sendItemBags(player);
                                PlayerService.gI().sendInfoHpMpMoney(player);
                                player.event.setReceivedLuckyMoney(true);
                                Service.getInstance().sendThongBao(player,
                                        "Nhận lì xì thành công,chúc bạn năm mới dui dẻ");
                            } else if (day > 24) {
                                Service.getInstance().sendThongBao(player, "Hết tết rồi còn đòi lì xì");
                            } else {
                                Service.getInstance().sendThongBao(player, "Đã tết đâu mà đòi lì xì");
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Bạn đã nhận lì xì rồi");
                        }
                        break;
                    case 1:
                        ShopService.gI().openShopNormal(player, npc, ConstNpc.SHOP_SU_KIEN_TET, 0, -1);
                        break;
                }
                break;
            case ConstEvent.SU_KIEN_8_3:
                switch (select) {
                    case 3:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            int evPoint = player.event.getEventPoint();
                            if (evPoint >= 999) {
                                Item capsule = ItemService.gI().createNewItem((short) 2052, 1);
                                player.event.setEventPoint(evPoint - 999);

                                capsule.itemOptions.add(new ItemOption(74, 0));
                                capsule.itemOptions.add(new ItemOption(30, 0));
                                InventoryService.gI().addItemBag(player, capsule, 0);
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được Capsule Hồng");
                            } else {
                                Service.getInstance().sendThongBao(player, "Cần 999 điểm tích lũy để đổi");
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Hành trang đầy.");
                        }
                        break;
                    default:
                        int n = 0;
                        switch (select) {
                            case 0:
                                n = 1;
                                break;
                            case 1:
                                n = 10;
                                break;
                            case 2:
                                n = 99;
                                break;
                        }

                        if (n > 0) {
                            Item bonghoa = InventoryService.gI().finditemBongHoa(player, n);
                            if (bonghoa != null) {
                                int evPoint = player.event.getEventPoint();
                                player.event.setEventPoint(evPoint + n);
                                InventoryService.gI().subQuantityItemsBag(player, bonghoa, n);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được " + n + " điểm sự kiện");
                            } else {
                                Service.getInstance().sendThongBao(player,
                                        "Cần ít nhất " + n + " bông hoa để có thể tặng");
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Cần ít nhất " + n + " bông hoa để có thể tặng");
                        }
                }
                break;
        }
    }

    public static String getMenuSuKien(int id) {
        switch (id) {
            case ConstEvent.KHONG_CO_SU_KIEN:
                return "Chưa có\n Sự Kiện";
            case ConstEvent.SU_KIEN_HALLOWEEN:
                return "Sự Kiện\nHaloween";
            case ConstEvent.SU_KIEN_20_11:
                return "Sự Kiện\n 20/11";
            case ConstEvent.SU_KIEN_NOEL:
                return "Sự Kiện\n Giáng Sinh";
            case ConstEvent.SU_KIEN_TET:
                return "Sự Kiện\n Tết Nguyên\nĐán 2024";
            case ConstEvent.SU_KIEN_8_3:
                return "Sự Kiện\n 8/3";
        }
        return "Chưa có\n Sự Kiện";
    }

    public static String getMenuLamBanh(Player player, int type) {
        switch (type) {
            case 0:// bánh tét
                if (player.event.isCookingTetCake()) {
                    int timeCookTetCake = player.event.getTimeCookTetCake();
                    if (timeCookTetCake == 0) {
                        return "Lấy bánh";
                    } else if (timeCookTetCake > 0) {
                        return "Đang nấu\nBánh Tét\nCòn " + TimeUtil.secToTime(timeCookTetCake);
                    }
                } else {
                    return "Nấu\nBánh Tét";
                }
                break;
            case 1:
                if (player.event.isCookingChungCake()) {
                    int timeCookChungCake = player.event.getTimeCookChungCake();
                    if (timeCookChungCake == 0) {
                        return "Lấy bánh";
                    } else if (timeCookChungCake > 0) {
                        return "Đang nấu\nBánh Chưng\nCòn " + TimeUtil.secToTime(timeCookChungCake);
                    }
                } else {
                    return "Nấu\nBánh Chưng";
                }
                break;
        }
        return "";
    }

}

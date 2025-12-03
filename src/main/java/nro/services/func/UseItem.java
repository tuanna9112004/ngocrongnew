package nro.services.func;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import nro.consts.*;
import nro.dialog.MenuDialog;
import nro.dialog.MenuRunable;
import nro.event.Event;
import nro.lib.RandomCollection;
import nro.manager.MiniPetManager;
import nro.manager.NamekBallManager;
import nro.manager.PetFollowManager;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.item.MinipetTemplate;
import nro.models.map.*;
import nro.models.map.dungeon.zones.ZSnakeRoad;
import nro.models.map.war.NamekBallWar;
import nro.models.player.Inventory;
import nro.models.player.MiniPet;
import nro.models.player.PetFollow;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.server.Manager;
import nro.server.io.Message;
import nro.server.io.Session;
import nro.services.*;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.TimeUtil;
import nro.utils.Util;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Random;
import nro.data.ItemData;
import nro.jdbc.DBService;
import nro.models.boss.BossManager;
import nro.models.npc.specialnpc.MabuEgg;

/**
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 */
public class UseItem {

    private static final int VND_ITEM_AMOUNT = 10000;
    private static final int ITEM_BOX_TO_BODY_OR_BAG = 0;
    private static final int ITEM_BAG_TO_BOX = 1;
    private static final int ITEM_BODY_TO_BOX = 3;
    private static final int ITEM_BAG_TO_BODY = 4;
    private static final int ITEM_BODY_TO_BAG = 5;
    private static final int ITEM_BAG_TO_PET_BODY = 6;
    private static final int ITEM_BODY_PET_TO_BAG = 7;
    private static final int HOI_SKILL_COOLDOWN_FAST = 60_000; // 30 giây
    private static final int HOI_SKILL_COOLDOWN_NORMAL = 60_000; // 60 giây

    private static final byte DO_USE_ITEM = 0;
    private static final byte DO_THROW_ITEM = 1;
    private static final byte ACCEPT_THROW_ITEM = 2;
    private static final byte ACCEPT_USE_ITEM = 3;
// Thêm vào class Player
    public long lastHoiSkillTime = 0;

    private static UseItem instance;
    private static final Logger logger = Logger.getLogger(UseItem.class);

    private UseItem() {

    }

    public static UseItem gI() {
        if (instance == null) {
            instance = new UseItem();
        }
        return instance;
    }

    public void getItem(Session session, Message msg) {
        Player player = session.player;
        TransactionService.gI().cancelTrade(player);
        try {
            int type = msg.reader().readByte();
            int index = msg.reader().readByte();
            if (index == -1) {
                return;
            }
            switch (type) {
                case ITEM_BOX_TO_BODY_OR_BAG:
                    InventoryService.gI().itemBoxToBodyOrBag(player, index);
                    TaskService.gI().checkDoneTaskGetItemBox(player);
                    break;
                case ITEM_BAG_TO_BOX:
                    InventoryService.gI().itemBagToBox(player, index);
                    break;
                case ITEM_BODY_TO_BOX:
                    InventoryService.gI().itemBodyToBox(player, index);
                    break;
                case ITEM_BAG_TO_BODY:
                    InventoryService.gI().itemBagToBody(player, index);
                    break;
                case ITEM_BODY_TO_BAG:
                    InventoryService.gI().itemBodyToBag(player, index);
                    break;
                case ITEM_BAG_TO_PET_BODY:
                    InventoryService.gI().itemBagToPetBody(player, index);
                    break;
                case ITEM_BODY_PET_TO_BAG:
                    InventoryService.gI().itemPetBodyToBag(player, index);
                    break;
            }
            player.setClothes.setup();
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            player.setClanMember();
            PlayerService.gI().sendPetFollow(player);
            Service.getInstance().point(player);
        } catch (Exception e) {
            Log.error(UseItem.class, e);

        }
    }

    public void doItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg;
        try {
            byte type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();
            switch (type) {
                case DO_USE_ITEM:
                    if (player != null && player.inventory != null) {
                        if (index != -1) {
                            if (index >= 0 && index < player.inventory.itemsBag.size()) {
                                Item item = player.inventory.itemsBag.get(index);
                                if (item.isNotNullItem()) {
                                    if (ItemData.IdMiniPet.contains((int) item.template.id)) {
                                        MinipetTemplate temp = MiniPetManager.gI().findByID(item.getId());
                                        if (temp == null) {
                                            System.err.println("khong tim thay minipet id: " + item.getId());
                                        }
                                        MiniPet.callMiniPet(player, item.template.id);
                                        InventoryService.gI().itemBagToBody(player, index);
                                        return;
                                    }
                                    if (item.template.type == 22) {
                                        msg = new Message(-43);
                                        msg.writer().writeByte(type);
                                        msg.writer().writeByte(where);
                                        msg.writer().writeByte(index);
                                        msg.writer().writeUTF("Bạn có muốn dùng " + player.inventory.itemsBag.get(index).template.name + "?");
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                    } else if (item.template.type == 7) {
                                        msg = new Message(-43);
                                        msg.writer().writeByte(type);
                                        msg.writer().writeByte(where);
                                        msg.writer().writeByte(index);
                                        msg.writer().writeUTF("Bạn chắc chắn học " + player.inventory.itemsBag.get(index).template.name + "?");
                                        player.sendMessage(msg);
                                    } else if (player.isVersionAbove(220) && item.template.type == 23 || item.template.type == 24 || item.template.type == 11) {
                                        InventoryService.gI().itemBagToBody(player, index);
                                    } else if (item.template.id == 401) {
                                        msg = new Message(-43);
                                        msg.writer().writeByte(type);
                                        msg.writer().writeByte(where);
                                        msg.writer().writeByte(index);
                                        msg.writer().writeUTF("Sau khi đổi đệ sẽ mất toàn bộ trang bị trên người đệ tử nếu chưa tháo");
                                        player.sendMessage(msg);
                                    } else if (item.getType() == 72) {
                                        PetFollow pet = PetFollowManager.gI().findByID(item.getId());
                                        player.setPetFollow(pet);
                                        InventoryService.gI().itemBagToBody(player, index);
                                        PlayerService.gI().sendPetFollow(player);
                                    } else if (item.template.type == 74) {
                                        Service.getInstance().sendFoot(player, item.template.id);
                                        InventoryService.gI().itemBagToBody(player, index);
                                    } else if (item.template.type == 35) {
                                        InventoryService.gI().itemBagToBody(player, index);
                                    } else if (item.template.type == 21) {
                                        InventoryService.gI().itemBagToBody(player, index);
                                    } else if (item.template.type == 72) {
                                        InventoryService.gI().itemBagToBody(player, index);
                                    }else if (item.template.type == 76) {
                                        InventoryService.gI().itemBagToBody(player, index);
                                    } else {
                                        useItem(player, item);
                                    }
                                }
                            }
                        } else {
                            InventoryService.gI().eatPea(player);
                        }
                    }
                    break;
                case DO_THROW_ITEM:
                    if (!(player.zone.map.mapId == 21 || player.zone.map.mapId == 22 || player.zone.map.mapId == 23)) {
                        Item item = null;
                        if (where == 0) {
                            if (index >= 0 && index < player.inventory.itemsBody.size()) {
                                item = player.inventory.itemsBody.get(index);
                            }
                        } else {
                            if (index >= 0 && index < player.inventory.itemsBag.size()) {
                                item = player.inventory.itemsBag.get(index);
                            }
                        }
                        if (item != null && item.isNotNullItem()) {
                            msg = new Message(-43);
                            msg.writer().writeByte(type);
                            msg.writer().writeByte(where);
                            msg.writer().writeByte(index);
                            msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "?");
                            player.sendMessage(msg);
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }
                    break;
                case ACCEPT_THROW_ITEM:
                    InventoryService.gI().throwItem(player, where, index);
                    break;
                case ACCEPT_USE_ITEM:
                    if (index >= 0 && index < player.inventory.itemsBag.size()) {
                        Item item = player.inventory.itemsBag.get(index);
                        if (item.isNotNullItem()) {
                            useItem(player, item);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            Log.error(UseItem.class, e);
        }
    }

    public void useSatellite(Player player, Item item) {
        Satellite satellite = null;
        if (player.zone != null) {
            int count = player.zone.getSatellites().size();
            if (count < 3) {
                switch (item.template.id) {
                    case ConstItem.VE_TINH_TRI_LUC:
                        satellite = new SatelliteMP(player.zone, ConstItem.VE_TINH_TRI_LUC, player.location.x, player.location.y, player);
                        break;

                    case ConstItem.VE_TINH_TRI_TUE:
                        satellite = new SatelliteExp(player.zone, ConstItem.VE_TINH_TRI_TUE, player.location.x, player.location.y, player);
                        break;

                    case ConstItem.VE_TINH_PHONG_THU:
                        satellite = new SatelliteDefense(player.zone, ConstItem.VE_TINH_PHONG_THU, player.location.x, player.location.y, player);
                        break;

                    case ConstItem.VE_TINH_SINH_LUC:
                        satellite = new SatelliteHP(player.zone, ConstItem.VE_TINH_SINH_LUC, player.location.x, player.location.y, player);
                        break;
                }
                if (satellite != null) {
                    InventoryService.gI().subQuantityItemsBag(player, item, 1);
                    Service.getInstance().dropItemMapForMe(player, satellite);
                    Service.getInstance().dropItemMap(player.zone, satellite);
                }
            } else {
                Service.getInstance().sendThongBaoOK(player, "Số lượng vệ tinh có thể đặt trong khu vực đã đạt mức tối đa.");
            }
        }
    }

    private void useItem(Player pl, Item item) {
        if (Event.isEvent() && Event.getInstance().useItem(pl, item)) {
            return;
        }
        if (item.template.strRequire <= pl.nPoint.power) {
            int type = item.getType();
            if (type == 6) {
                InventoryService.gI().eatPea(pl);
            } else if (type == 33) {
                RadaService.getInstance().useItemCard(pl, item);
            } else if (type == 5) {
                Service.getInstance().Send_Caitrang(pl);
            } else if (type == 22) {
                useSatellite(pl, item);
            } else if (type == 72) {
            } else if (type == 74) {
                Service.getInstance().sendFoot(pl, item.template.id);
            } else {
                switch (item.template.id) {
                    case ConstItem.GOI_10_RADA_DO_NGOC:
                        findNamekBall(pl, item);
                        break;
                    case 627:
                        capsule8thang3(pl, item);
                        break;
                    case ConstItem.CAPSULE_THOI_TRANG_30_NGAY:
                        capsuleThoiTrang(pl, item);
                        break;
                    case 1445:
                        openboxsukien(pl, item, ConstEvent.SU_KIEN_TET);
                        break;
                    case 570:
                        openWoodChest(pl, item);
                        break;
                    case 648:
                        openboxsukien(pl, item, 3);
                        break;
                    case 668:
                        hopQuaTanThu(pl, item);
                        break;
                    case 992:
                        if (TaskService.gI().getIdTask(pl) == ConstTask.TASK_32_0) {
                            TaskService.gI().doneTask(pl, ConstTask.TASK_32_0);
                        }
                        ChangeMapService.gI().goToPrimaryForest(pl);
                        break;
                    case 1483:
                        NpcService.gI().createMenuConMeo(pl, ConstNpc.VAO_MAP_NGOAI_VUC, -1,
                                "Bạn muốn Di chuyển đến mơi nào???",
                                "Map Hallowen", "Hành tinh\nBăng giá", "Map Địa Ngục");
                        break;
                    case 1444:
                        if (!pl.getSession().actived) {
                            Service.getInstance().sendThongBao(pl, "Vui lòng kích hoạt tài khoản để có thể sử dụng");
                            return;
                        }
                        Input.gI().createFormTangRuby(pl);
                        break;
                    case 628: //phiếu cải trang hải tặc
                        openPhieuCaiTrangHaiTac(pl, item);
                        break;
                    case 1433: //Hop Qua Kich Hoat
                        openboxsukien(pl, item, 1);
                        break;
                    case 1555:
                        hoiskill(pl, item);
                        break;
                    case 1581:
                        useMilkForChild(pl, item);
                        break;
                    case 1670: // Item tăng sát thương Boss
                        useItemBuffBoss(pl, item);
                        break;

                    case 1441: //phiếu cải trang 20/10
                        openbox2010(pl, item);
                        break;
                    case 1442:
                        openboxsukien(pl, item, 2);
                        break;
                    case 457:
                        NpcService.gI().createMenuConMeo(pl, ConstNpc.DUNG_NHIEU_TV, 7710,
                                "|7|THỎI VÀNG\n"
                                + "|-1|Theo nguyện vọng góp ý từ các chiến binh, ta được Admin\n"
                                + "giao cho trọng trách hỗ trợ Sử dụng Thỏi vàng số lượng nhiều\n"
                                + "|0|Ngươi muốn Sử dụng bao nhiêu Thỏi vàng?",
                                "X1 Thỏi\n(500 Triệu)", "X5 Thỏi\n(2,5 Tỷ)", "X10 Thỏi\n(5 Tỷ)",
                                "X100 Thỏi\n(50 Tỷ)");
                        return;
                    case 211: //nho tím
                    case 212: //nho xanh
                        eatGrapes(pl, item);
                        break;
                    case 2000:
                    case 2001:
                    case 2002:
                        openSkhThuong(pl, item);
                        break;
                    case 568: //cskb
                        openMabuEgg(pl, item);
                        break;
                    case 380: //cskb
                        openCSKB(pl, item);
                        break;
                    case 573: //capsule bạc
                        openCapsuleBac(pl, item);
                        break;
                    case 1603:
                        thechienbinh(pl, item);
                        break;
                    case 2050: //capsule bạc
                        danangcap(pl, item);
                        break;
                    case ConstItem.TUI_VANG: //capsule bạc
                        tuivang(pl, item);
                        break;
                    case 574: //capsule vàng
                        openCapsuleVang(pl, item);
                        break;
                    case 381: //cuồng nộ
                    case 382: //bổ huyết
                    case 383: //bổ khí
                    case 384: //giáp xên
                    case 385: //ẩn danh
                    case 379: //máy dò
                    case 1109: //máy dò skh
                    case 663: //bánh pudding
                    case 664: //xúc xíc
                    case 665: //kem dâu
                    case 666: //mì ly
                    case 667: //sushi
                    case ConstItem.BANH_CHUNG_CHIN:
                    case ConstItem.BANH_TET_CHIN:
                    case ConstItem.CUONG_NO_2:
                    case ConstItem.BO_HUYET_2:
                    case ConstItem.GIAP_XEN_BO_HUNG_2:
                    case ConstItem.BO_KHI_2:
                    case ConstItem.TN_SM:
                    case ConstItem.tnsm:
                    case 579:
                    case 1385:
                    case 899:
                    case 1317:
                    case 1201:
                    case 1386:
                    case 465:
                    case 466:
                    case 472:
                    case 473:
                        useItemTime(pl, item);
                        break;
                    case 521: //tdlt
                        useTDLT(pl, item);
                        break;
                    case 1105://hop qua skh, item 2002 xd
                        UseItem.gI().Hopts(pl, item);
                        break;
                    case 1587://hộp skh hủy diệt
                        HopSKHHuyDiet(pl, item);
                        break;
                    case 1586://hộp skh hủy diệt
                        UseItem.gI().HopSKHThanLinh(pl, item);
                        break;
                    case 454: //bông tai
                        usePorata(pl);
                        break;
                    case 921: //bông tai
                        UseItem.gI().usePorata2(pl);
                        break;
                    case 1165: //bông tai
                        UseItem.gI().usePorata3(pl);
                        break;
                    case 1129: //bông tai
                        UseItem.gI().usePorata4(pl);
                        break;

                    case 1550: //bông tai cấp 5
                        UseItem.gI().usePorata5(pl);
                        break;
                    case 193: //gói 10 viên capsule
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    case 194: //capsule đặc biệt
                        openCapsuleUI(pl);
                        break;
                    case 401: //đổi đệ tử
                        changePet(pl, item);
                        break;
                    case 402: //sách nâng chiêu 1 đệ tử
                    case 403: //sách nâng chiêu 2 đệ tử
                    case 404: //sách nâng chiêu 3 đệ tử
                    case 759: //sách nâng chiêu 4 đệ tử
                        upSkillPet(pl, item);
                        break;
                    case 1241: //đổi skill
                        doiskill4(pl, item);
                        break;
                    case 1459: //đổi đệ tử
                        changePetZeno(pl, item);
                        break;
                    case 1666: //đổi đệ tử
                        changePetLuffy(pl, item);
                        break;
                    case 1108: //đổi đệ tử
                        changePetBerus(pl, item);
                        break;
                    // case 1800: //đổi đệ tử
                    //  chienthan(pl, item);
                    //  break;
                    case 1237: //pháp sư
                        openphapsu(pl, item);
                        break;
                    case 1525: //hộp sách tuyệt kĩ
                        OpenNguyenLieu(pl, item);
                        break;
                    case 1526: //hộp cải trang Hit
                        OpenHit(pl, item);
                        break;
                    case 1667:
                        buffClan(pl, item);
                        break;
                    case 1575:
                        openNgocTrai(pl, item);
                        break;
                    case 2006:
                        Input.gI().createFormChangeNameByItem(pl);
                        break;
                    case 1479: //cskb
                        RuongSaoPhaLe(pl, item);
                        break;
                    case 1334: //hộp đồ thần linh
                        hopthanlinh(pl, item);
                        break;
                    case 1460: //hộp đồ thần linh
                        hopHuyDiet(pl, item);
                        break;
                    case 1399: //rương skh vip
                        ruongskhVIP(pl, item);
                        break;
                    case 1407: //rương skh vip
                        RuongSkhThanhTon(pl, item);
                        break;
                    case 1296: //cskb
                        maydoboss(pl);
                        break;
                    case 2026:
                        hoplinhthu(pl, item);
                        break;
                    case ConstItem.CAPSULE_TET_2022:
                        openCapsuleTet2022(pl, item);
                        break;
                    default:
                        switch (item.template.type) {
                            case 7: //sách học, nâng skill
                                learnSkill(pl, item);
                                break;
                            case 12: //ngọc rồng các loại
//                                Service.getInstance().sendThongBaoOK(pl, "Bảo trì tính năng.");
                                controllerCallRongThan(pl, item);
                                break;
                            case 11: //item flag bag
                                useItemChangeFlagBag(pl, item);
                                break;
                        }
                }
            }
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.getInstance().sendThongBaoOK(pl, "Sức mạnh không đủ yêu cầu");
        }
    }

    private void Hopts(Player pl, Item item) {//hop qua do huy diet
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Chọn hành tinh của mày đi", "Set trái đất", "Set namec", "Set xayda", "Từ chổi");
    }

    private void HopSKHHuyDiet(Player pl, Item item) {//hop qua do huy diet
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Chọn hành tinh của mày đi", "Set trái đất", "Set namec", "Set xayda", "Từ chổi");
    }

    private void HopSKHThanLinh(Player pl, Item item) {//hop qua do huy diet
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Chọn hành tinh của mày đi", "Set trái đất", "Set namec", "Set xayda", "Từ chổi");
    }

    private void changePet(Player player, Item item) {
        if (InventoryService.gI().getCountEmptyBody(player.pet) == 10) {
            if (player.pet != null) {
                int gender = player.pet.gender + 1;
                if (gender > 2) {
                    gender = 0;
                }
                player.leverPet = 0;
                player.LVhopthe = 0;
                player.SDhopthe = 0;
                player.HPhopthe = 0;
                player.KIhopthe = 0;
                PetService.gI().changeNormalPet(player, gender);
                InventoryService.gI().subQuantityItemsBag(player, item, 1);
            } else {
                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Vui lòng tháo hết đồ đệ tử");
        }
    }

    private void changePetBerus(Player player, Item item) {
        if (InventoryService.gI().getCountEmptyBody(player.pet) == 10) {
            if (player.pet != null) {
                int gender = player.pet.gender;
                // if (gender > 2) {
                // gender = 0;
                // }
                player.leverPet = 0;
                player.LVhopthe = 0;
                player.SDhopthe = 0;
                player.HPhopthe = 0;
                player.KIhopthe = 0;
                PetService.gI().changeBerusPet(player, gender);
                InventoryService.gI().subQuantityItemsBag(player, item, 1);
                InventoryService.gI().sendItemBags(player);
            } else {
                Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Vui lòng tháo hết đồ đệ tử");
        }
    }

    private void changePetZeno(Player player, Item item) {
        int diem = 0;
        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 240) {
                diem = io.param;
                break;
            }
        }
        if (player.pet == null) {
            Service.getInstance().sendThongBao(player, "Yêu cầu có đệ tử");
            return;
        }
        if (InventoryService.gI().getCountEmptyBody(player.pet) == 10) {
            if (player.pet != null) {
                if (diem == 10) {
                    NpcService.gI().createMenuConMeo(player, ConstNpc.MO_DE_TU, 16405,
                            "Vui lòng Chọn Hành tinh Đệ tử Zeno !!!",
                            "Trái Đất", "Namec", "Xayda");
                } else {
                    Service.getInstance().sendThongBao(player, "Bạn chưa đủ Điểm để mở. Còn thiếu " + (10 - diem) + " Điểm");
                }
            } else {
                Service.getInstance().sendThongBao(player, "Yêu cầu có đệ tử");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Vui lòng tháo hết đồ đệ tử");
        }
    }

    private void changePetLuffy(Player player, Item item) {
        if (player.pet == null) {
            Service.getInstance().sendThongBao(player, "Yêu cầu có đệ tử");
            return;
        }
        if (InventoryService.gI().getCountEmptyBody(player.pet) == 10) {
            if (player.pet != null) {
                NpcService.gI().createMenuConMeo(player, ConstNpc.DE_TU_LUFFY, 15418,
                        "Vui lòng Chọn Hành tinh Đệ tử  !!!",
                        "Trái Đất", "Namec", "Xayda");
            } else {
                Service.getInstance().sendThongBao(player, "Yêu cầu có đệ tử");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Vui lòng tháo hết đồ đệ tử");
        }
    }

    private void OpenNguyenLieu(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] thuong = {1506, 1507};
            short trangsach = 1516;
            short bua = 1508;
            byte index = (byte) Util.nextInt(0, thuong.length - 1);
            if (Util.isTrue(60, 100)) {
                Item it = ItemService.gI().createNewItem(trangsach, 50);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 1);
                Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            } else if (Util.isTrue(15, 100)) {
                Item it = ItemService.gI().createNewItem(bua);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 1);
                Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            } else {
                Item it = ItemService.gI().createNewItem(thuong[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 1);
                Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void hoplinhthu(Player pl, Item src) {
        if (pl == null || src == null) {
            return;
        }

        // Cần còn chỗ trống (hoặc bạn đổi sang kiểm tra stack nếu item có thể stack)
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.getInstance().sendThongBao(pl, "Hành trang đã đầy");
            return;
        }
        if (src.quantity <= 0) {
            Service.getInstance().sendThongBao(pl, "Vật phẩm không hợp lệ");
            return;
        }

        // ===== ID vật phẩm thưởng =====
        final short ID_THUONG = 2015;
        final short ID_TRANGSACH = 1383;
        final short ID_BUA = 2016;

        // ===== Tỉ lệ (tổng 100%) – chỉnh theo ý bạn =====
        final int P_THUONG = 5; // 30%
        final int P_BUA = 50; // 50%
        final int P_TRANGSACH = 35; // 20%

        // Rút 1 lần cho phần thưởng
        int roll = Util.nextInt(1, 100);
        short rewardId;
        if (roll <= P_THUONG) {
            rewardId = ID_THUONG;
        } else if (roll <= P_THUONG + P_BUA) {
            rewardId = ID_BUA;
        } else {
            rewardId = ID_TRANGSACH;
        }

        Item it = ItemService.gI().createNewItem(rewardId, 1);
        if (it == null) {
            Service.getInstance().sendThongBao(pl, "Tạo vật phẩm thất bại");
            return;
        }

        // Gắn options theo loại phần thưởng
        if (rewardId == ID_TRANGSACH) {
            it.itemOptions.add(new ItemOption(0, Util.nextInt(1000, 1500)));
            it.itemOptions.add(new ItemOption(6, Util.nextInt(1000, 1500)));
            it.itemOptions.add(new ItemOption(7, Util.nextInt(1000, 1500)));
            // it.itemOptions.add(new ItemOption(4,  Util.nextInt(5, 7)));
            //it.itemOptions.add(new ItemOption(5,  Util.nextInt(5, 7)));

        } else if (rewardId == ID_BUA) {
            it.itemOptions.add(new ItemOption(0, Util.nextInt(2000, 3000)));
            it.itemOptions.add(new ItemOption(6, Util.nextInt(2000, 3000)));
            it.itemOptions.add(new ItemOption(7, Util.nextInt(2000, 3000)));
        } else { // ID_THUONG
            it.itemOptions.add(new ItemOption(50, Util.nextInt(7, 11)));
            it.itemOptions.add(new ItemOption(77, Util.nextInt(7, 11)));
            it.itemOptions.add(new ItemOption(103, Util.nextInt(7, 11)));
            it.itemOptions.add(new ItemOption(5, Util.nextInt(1, 7)));

        }

        InventoryService.gI().addItemBag(pl, it, 1);

        InventoryService.gI().subQuantityItemsBag(pl, src, 1);
        InventoryService.gI().sendItemBags(pl);

        Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + (it.template != null ? it.template.name : "vật phẩm"));
    }

    private void OpenHit(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] thuong = {16, 15};
            byte index = (byte) Util.nextInt(0, thuong.length - 1);
            if (Util.isTrue(50, 100)) {
                Item it = ItemService.gI().createNewItem(thuong[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 1);
                Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            } else {
                Item it = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_HIT);
                if (Util.isTrue(40, 100)) {
                    it.itemOptions.add(new ItemOption(5, Util.nextInt(10, 60)));
                } else {
                    if (Util.isTrue(60, 100)) {
                        it.itemOptions.add(new ItemOption(5, Util.nextInt(60, 100)));
                    } else {
                        it.itemOptions.add(new ItemOption(5, Util.nextInt(100, 150)));
                    }
                }
                it.itemOptions.add(new ItemOption(50, Util.nextInt(5, 20)));
                it.itemOptions.add(new ItemOption(14, Util.nextInt(5, 10)));
                if (Util.isTrue(90, 100)) {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(2, 5)));
                } else {
                    it.itemOptions.add(new ItemOption(73, 1));
                }
                InventoryService.gI().addItemBag(pl, it, 1);
                Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void openNgocTrai(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int tile = Util.nextInt(1, 100); // random từ 1-100
            Item it = null;

            if (tile <= 11) { // 9%
                it = ItemService.gI().createNewItem((short) 1566);
                it.itemOptions.add(new ItemOption(0, 50));

            } else if (tile <= 42) { // 35% (10 -> 44)
                it = ItemService.gI().createNewItem((short) 1567);
                it.itemOptions.add(new ItemOption(6, 70));

            } else if (tile <= 42) { // 4% (45 -> 48)
                it = ItemService.gI().createNewItem((short) 1568);
                it.itemOptions.add(new ItemOption(7, 70));

            } else { // 52% (49 -> 100)
                it = ItemService.gI().createNewItem((short) 1569);
                it.itemOptions.add(new ItemOption(5, 2));
            }

            // Thêm vào hành trang
            InventoryService.gI().addItemBag(pl, it, 1);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);

            // Trừ hộp
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

        } else {
            Service.getInstance().sendThongBao(pl, "Hành trang đã đầy");
        }
    }

    private void hoiskill(Player pl, Item item) {
        try {
            long now = System.currentTimeMillis();

            // Nếu item là loại hồi nhanh
            if (item.template.id == 1555) {
                if (now - pl.lastHoiSkillTime < HOI_SKILL_COOLDOWN_FAST) {
                    long remain = (HOI_SKILL_COOLDOWN_FAST - (now - pl.lastHoiSkillTime)) / 1000;
                    Service.getInstance().sendThongBao(pl,
                            "|7|Bạn phải chờ " + remain + " giây nữa mới có thể dùng lại vật phẩm này!");
                    return;
                }
            }

            // Reset skill cooldown (đúng cách)
            Message msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                // đặt lastTimeUseThisSkill về quá khứ = đã hồi xong
                skill.lastTimeUseThisSkill = now - skill.coolDown;

                msg.writer().writeShort(skill.skillId);
                msg.writer().writeInt(0); // báo client: hồi ngay lập tức
            }
            pl.sendMessage(msg);

            // Clear các cờ skill đặc biệt (nếu có)
            pl.playerSkill.prepareQCKK = false;
            pl.playerSkill.prepareLaze = false;
            pl.playerSkill.prepareTuSat = false;

            // Hồi full KI + HP
            pl.nPoint.setMp(pl.nPoint.mpMax);
            pl.nPoint.setHp(pl.nPoint.hpMax);
            PlayerService.gI().sendInfoHpMpMoney(pl);
            msg.cleanup();

            // Trừ item
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            // Thông báo
            Service.getInstance().sendThongBao(pl,
                    "|3|Bạn đã được hồi tất cả skill và 100% KI/HP");

            // Lưu thời gian dùng
            pl.lastHoiSkillTime = now;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void useItemBuffBoss(Player pl, Item item) {
        long now = System.currentTimeMillis();
        long buffDuration = 300000; // 5 phút

        // Nếu buff còn thì không cho dùng thêm
        if (pl.buffBossDamageTime > now) {
            long remain = (pl.buffBossDamageTime - now) / 1000;
            Service.getInstance().sendThongBao(pl,
                    "|7|Hiệu ứng còn " + remain + " giây, không thể dùng thêm!");
            return;
        }

        // Kích hoạt buff
        pl.buffBossDamageTime = now + buffDuration;

        // Trừ item
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);

        // Thông báo kích hoạt
        Service.getInstance().sendThongBao(pl,
                "|3|Bạn đã dùng " + item.template.name + "\nTăng 20% sát thương lên Boss trong 5 phút!");

        // 🕒 Tạo task thông báo hết buff sau 5 phút
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
            @Override
            public void run() {
                // Chỉ thông báo nếu buff thật sự đã hết
                if (pl.buffBossDamageTime <= System.currentTimeMillis()) {
                    Service.getInstance().sendThongBao(pl,
                            "|7|Vitamin Hùng Hăng đã hết hiệu lực!");
                }
            }
        },
                buffDuration // delay đúng bằng thời gian buff
        );
    }

    private void useMilkForChild(Player pl, Item item) {
        if (item == null || pl == null) {
            return;
        }

        // Kiểm tra đã kết hôn & có con chưa
        if (pl.family == null || !pl.family.isMarried() || !pl.family.hasChild()) {
            Service.getInstance().sendThongBao(pl, "|7|Bạn chưa có con để nuôi!");
            return;
        }

        // Trừ 1 item sữa (ID = 1587)
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);

        // Cộng exp cho con
        int expAdd = 50;
        FamilyService.gI().addChildExp(pl, expAdd);

        // Thông báo
        Service.getInstance().sendThongBao(pl,
                "🍼 Bạn đã cho con uống " + item.template.name + " (+ " + expAdd + " EXP)");
    }

    private void openphapsu(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] manh = {1232, 1233, 1234};
            short da = 1235;
            short bua = 1236;
            short[] rac = {579, 1201, 15};
            byte index = (byte) Util.nextInt(0, manh.length - 1);
            byte index2 = (byte) Util.nextInt(0, rac.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (Util.isTrue(35, 100)) {
                Item it = ItemService.gI().createNewItem(rac[index2]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 1);
                icon[1] = it.template.iconID;
                Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            } else if (Util.isTrue(13, 100)) {
                Item it = ItemService.gI().createNewItem(da);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 1);
                icon[1] = it.template.iconID;
                Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            } else if (Util.isTrue(3, 100)) {
                Item it = ItemService.gI().createNewItem(bua);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 1);
                icon[1] = it.template.iconID;
                Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            } else {
                Item it = ItemService.gI().createNewItem(manh[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 1);
                icon[1] = it.template.iconID;
                Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void RuongSaoPhaLe(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] manh = {1480, 1481, 1482};
            byte index = (byte) Util.nextInt(0, manh.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            Item it = ItemService.gI().createNewItem(manh[index]);
            switch (it.template.id) {
                case 1480:
                    it.itemOptions.add(new ItemOption(77, 8));
                    break;
                case 1481:
                    it.itemOptions.add(new ItemOption(103, 8));
                    break;
                case 1482:
                    it.itemOptions.add(new ItemOption(50, 5));
                    break;
                default:
                    break;
            }
            InventoryService.gI().addItemBag(pl, it, 1);
            icon[1] = it.template.iconID;
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void hopthanlinh(Player player, Item item) {
        byte randomDo = (byte) new Random().nextInt(Manager.itemIds_TL.length);
        Item thanlinh = Util.ratiItemTL(Manager.itemIds_TL[randomDo]);
        short[] icon = new short[2];
        icon[0] = item.template.iconID;
        if (InventoryService.gI().getCountEmptyBag(player) > 1) {
            thanlinh.itemOptions.add(new ItemOption(30, 1));
            InventoryService.gI().addItemBag(player, thanlinh, 1);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được " + thanlinh.template.name);
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
            icon[1] = thanlinh.template.iconID;
            InventoryService.gI().sendItemBags(player);
            CombineServiceNew.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    public void hopHuyDiet(Player player, Item item) {
        byte randomDo = (byte) new Random().nextInt(Manager.itemIds_TL.length);
        Item thanlinh = Util.ratiItemHuyDiet(Manager.itemIds_HuyDiet[randomDo]);
        short[] icon = new short[2];
        icon[0] = item.template.iconID;
        if (InventoryService.gI().getCountEmptyBag(player) > 1) {
            thanlinh.itemOptions.add(new ItemOption(30, 1));
            InventoryService.gI().addItemBag(player, thanlinh, 1);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được " + thanlinh.template.name);
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
            icon[1] = thanlinh.template.iconID;
            InventoryService.gI().sendItemBags(player);
            CombineServiceNew.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    private void ruongskhVIP(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short itemId;
            int[] idhender = new int[]{0, 1, 2, 3, 4};
            int idr = new Random().nextInt(idhender.length);
            if (pl.gender == 3 || idr == 4) {
                itemId = Manager.radaSKHVip[Util.nextInt(0, 5)];
                if (Util.isTrue(1, (int) 100)) {
                    itemId = Manager.radaSKHVip[6];
                }
            } else {
                itemId = Manager.doSKHVip[pl.gender][idr][Util.nextInt(0, 5)];
                if (Util.isTrue(1, 100)) {
                    itemId = Manager.doSKHVip[pl.gender][idr][6];
                }
            }
            int skhId = ItemService.gI().randomSKHId(pl.gender);
            Item items;
            if (new Item(itemId).isDTL()) {
                items = Util.ratiItemTL(itemId);
                items.itemOptions.add(new ItemOption(skhId, 1));
                items.itemOptions.add(new ItemOption(ItemService.gI().optionIdSKH(skhId), 1));
                items.itemOptions.remove(items.itemOptions.stream().filter(itemOption -> itemOption.optionTemplate.id == 21).findFirst().get());
                items.itemOptions.add(new ItemOption(21, 15));
                items.itemOptions.add(new ItemOption(30, 1));
            } else {
                items = ItemService.gI().itemSKH(itemId, skhId);
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().addItemBag(pl, items, 1);
            InventoryService.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "|1| Bạn nhận được " + items.template.name);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void RuongSkhThanhTon(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short itemId;
            itemId = Manager.DoThanhTon[Util.nextInt(0, 4)];
            int skhId = ItemService.gI().randomSKHThanhTon(pl.gender);
            Item items;
            if (new Item(itemId).isThanhTon()) {
                items = Util.ratiItemThanhTon(itemId);
                items.itemOptions.add(new ItemOption(skhId, 1));
                items.itemOptions.add(new ItemOption(ItemService.gI().optionIdSKHThanhTon(skhId), 1));
                items.itemOptions.remove(items.itemOptions.stream().filter(itemOption -> itemOption.optionTemplate.id == 21).findFirst().get());
                items.itemOptions.add(new ItemOption(21, 200));
                items.itemOptions.add(new ItemOption(30, 1));
            } else {
                items = ItemService.gI().itemSKH(itemId, skhId);
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().addItemBag(pl, items, 1);
            InventoryService.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "|1| Bạn nhận được " + items.template.name);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public boolean maydoboss(Player pl) {
        try {
            BossManager.gI().showListBossMember(pl);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    private void findNamekBall(Player pl, Item item) {
        List<NamekBall> balls = NamekBallManager.gI().getList();
        StringBuffer sb = new StringBuffer();
        for (NamekBall namekBall : balls) {
            Map m = namekBall.zone.map;
            sb.append(namekBall.getIndex() + 1).append(" Sao: ").append(m.mapName).append(namekBall.getHolderName() == null ? "" : " - " + namekBall.getHolderName()).append("\n");
        }
        final int star = Util.nextInt(0, 6);
        final NamekBall ball = NamekBallManager.gI().findByIndex(star);
        final Inventory inventory = pl.inventory;
        MenuDialog menu = new MenuDialog(sb.toString(), new String[]{"Đến ngay\nViên " + (star + 1) + " Sao\n 50tr Vàng", "Đến ngay\nViên " + (star + 1) + " Sao\n 5 Hồng ngọc"}, new MenuRunable() {
            @Override
            public void run() {
                switch (getIndexSelected()) {
                    case 0:
                        if (inventory.gold < 50000000) {
                            Service.getInstance().sendThongBao(pl, "Không đủ tiền");
                            return;
                        }
                        inventory.subGold(50000000);
                        ChangeMapService.gI().changeMap(pl, ball.zone, ball.x, ball.y);
                        break;
                    case 1:
                        if (inventory.ruby < 5) {
                            Service.getInstance().sendThongBao(pl, "Không đủ tiền");
                            return;
                        }
                        inventory.subRuby(5);
                        ChangeMapService.gI().changeMap(pl, ball.zone, ball.x, ball.y);
                        break;
                }
                if (pl.isHoldNamecBall) {
                    NamekBallWar.gI().dropBall(pl);
                }
                Service.getInstance().sendMoney(pl);
            }
        });
        menu.show(pl);
        InventoryService.gI().sendItemBags(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
    }

    private void capsuleThoiTrang(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item it = ItemService.gI().createNewItem((short) Util.nextInt(ConstItem.CAI_TRANG_GOKU_THOI_TRANG, ConstItem.CAI_TRANG_CA_DIC_THOI_TRANG));
            it.itemOptions.add(new ItemOption(50, 30));
            it.itemOptions.add(new ItemOption(77, 30));
            it.itemOptions.add(new ItemOption(103, 30));
            it.itemOptions.add(new ItemOption(106, 0));
            InventoryService.gI().addItemBag(pl, it, 0);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            short icon1 = item.template.iconID;
            short icon2 = it.template.iconID;
            CombineServiceNew.gI().sendEffectOpenItem(pl, icon1, icon2);
        } else {
            Service.getInstance().sendThongBao(pl, "Hãy chừa 1 ô trống để mở.");
        }

    }

    private void openCapsuleTet2022(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
            Service.getInstance().sendThongBao(pl, "Hãy chừa 1 ô trống để mở.");
            return;
        }
        RandomCollection<Integer> rdItemID = new RandomCollection<>();
        rdItemID.add(1, ConstItem.PHAO_HOA);
        rdItemID.add(1, ConstItem.CAY_TRUC);
        rdItemID.add(1, ConstItem.NON_HO_VANG);
        if (pl.gender == 0) {
            rdItemID.add(1, ConstItem.NON_TRAU_MAY_MAN);
            rdItemID.add(1, ConstItem.NON_CHUOT_MAY_MAN);
        } else if (pl.gender == 1) {
            rdItemID.add(1, ConstItem.NON_TRAU_MAY_MAN_847);
            rdItemID.add(1, ConstItem.NON_CHUOT_MAY_MAN_755);
        } else {
            rdItemID.add(1, ConstItem.NON_TRAU_MAY_MAN_848);
            rdItemID.add(1, ConstItem.NON_CHUOT_MAY_MAN_756);
        }
        rdItemID.add(1, ConstItem.CAI_TRANG_HO_VANG);
        rdItemID.add(1, ConstItem.HO_MAP_VANG);
        rdItemID.add(2, ConstItem.SAO_PHA_LE);
        rdItemID.add(2, ConstItem.SAO_PHA_LE_442);
        rdItemID.add(2, ConstItem.SAO_PHA_LE_443);
        rdItemID.add(2, ConstItem.SAO_PHA_LE_444);
        rdItemID.add(2, ConstItem.SAO_PHA_LE_445);
        rdItemID.add(2, ConstItem.SAO_PHA_LE_446);
        rdItemID.add(2, ConstItem.SAO_PHA_LE_447);
        rdItemID.add(2, ConstItem.DA_LUC_BAO);
        rdItemID.add(2, ConstItem.DA_SAPHIA);
        rdItemID.add(2, ConstItem.DA_TITAN);
        rdItemID.add(2, ConstItem.DA_THACH_ANH_TIM);
        rdItemID.add(2, ConstItem.DA_RUBY);
        rdItemID.add(3, ConstItem.VANG_190);
        int itemID = rdItemID.next();
        Item newItem = ItemService.gI().createNewItem((short) itemID);
        if (newItem.template.type == 9) {
            newItem.quantity = Util.nextInt(10, 50) * 1000000;
        } else if (newItem.template.type == 14 || newItem.template.type == 30) {
            newItem.quantity = 10;
        } else {
            switch (itemID) {
                case ConstItem.CAY_TRUC: {
                    RandomCollection<ItemOption> rdOption = new RandomCollection<>();
                    rdOption.add(2, new ItemOption(77, 15));//%hp
                    rdOption.add(2, new ItemOption(103, 15));//%hp
                    rdOption.add(1, new ItemOption(50, 15));//%hp
                    newItem.itemOptions.add(rdOption.next());
                }
                break;

                case ConstItem.HO_MAP_VANG: {
                    newItem.itemOptions.add(new ItemOption(77, Util.nextInt(10, 20)));
                    newItem.itemOptions.add(new ItemOption(103, Util.nextInt(10, 20)));
                    newItem.itemOptions.add(new ItemOption(50, Util.nextInt(10, 20)));
                }
                break;

                case ConstItem.NON_HO_VANG:
                case ConstItem.CAI_TRANG_HO_VANG:
                case ConstItem.NON_TRAU_MAY_MAN:
                case ConstItem.NON_TRAU_MAY_MAN_847:
                case ConstItem.NON_TRAU_MAY_MAN_848:
                case ConstItem.NON_CHUOT_MAY_MAN:
                case ConstItem.NON_CHUOT_MAY_MAN_755:
                case ConstItem.NON_CHUOT_MAY_MAN_756:
                    newItem.itemOptions.add(new ItemOption(77, 30));
                    newItem.itemOptions.add(new ItemOption(103, 30));
                    newItem.itemOptions.add(new ItemOption(50, 30));
                    break;
            }
            RandomCollection<Integer> rdDay = new RandomCollection<>();
            rdDay.add(6, 3);
            rdDay.add(3, 7);
            rdDay.add(1, 15);
            int day = rdDay.next();
            newItem.itemOptions.add(new ItemOption(93, day));
        }
        short icon1 = item.template.iconID;
        short icon2 = newItem.template.iconID;
        if (newItem.template.type == 9) {
            Service.getInstance().sendThongBao(pl, "Bạn nhận được " + Util.numberToMoney(newItem.quantity) + " " + newItem.template.name);
        } else if (newItem.quantity == 1) {
            Service.getInstance().sendThongBao(pl, "Bạn nhận được " + newItem.template.name);
        } else {
            Service.getInstance().sendThongBao(pl, "Bạn nhận được x" + newItem.quantity + " " + newItem.template.name);
        }
        CombineServiceNew.gI().sendEffectOpenItem(pl, icon1, icon2);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().addItemBag(pl, newItem, 99);
        InventoryService.gI().sendItemBags(pl);
    }

    private int randClothes(int level) {
        return ConstItem.LIST_ITEM_CLOTHES[Util.nextInt(0, 2)][Util.nextInt(0, 4)][level - 1];
    }

    private void openWoodChest(Player pl, Item item) {
        int time = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
        if (time != 0) {
            Item itemReward = null;
            int param = item.itemOptions.get(0).param;
            int gold = 0;
            int[] listItem = {441, 442, 443, 444, 445, 446, 447, 220, 221, 222, 223, 224, 225};
            int[] listClothesReward;
            int[] listItemReward;
            String text = "Bạn nhận được\n";
            if (param < 8) {
                gold = 100000 * param;
                listClothesReward = new int[]{randClothes(param)};
                listItemReward = Util.pickNRandInArr(listItem, 3);
            } else if (param < 10) {
                gold = 250000 * param;
                listClothesReward = new int[]{randClothes(param), randClothes(param)};
                listItemReward = Util.pickNRandInArr(listItem, 4);
            } else {
                gold = 500000 * param;
                listClothesReward = new int[]{randClothes(param), randClothes(param), randClothes(param)};
                listItemReward = Util.pickNRandInArr(listItem, 5);
                int ruby = Util.nextInt(1, 5);
                pl.inventory.ruby += ruby;
                pl.textRuongGo.add(text + "|1| " + ruby + " Hồng Ngọc");
            }
            for (var i : listClothesReward) {
                itemReward = ItemService.gI().createNewItem((short) i);
                RewardService.gI().initBaseOptionClothes(itemReward.template.id, itemReward.template.type, itemReward.itemOptions);
                RewardService.gI().initStarOption(itemReward, new RewardService.RatioStar[]{new RewardService.RatioStar((byte) 1, 1, 2), new RewardService.RatioStar((byte) 2, 1, 3), new RewardService.RatioStar((byte) 3, 1, 4), new RewardService.RatioStar((byte) 4, 1, 5),});
                InventoryService.gI().addItemBag(pl, itemReward, 0);
                pl.textRuongGo.add(text + itemReward.getInfoItem());
            }
            for (var i : listItemReward) {
                itemReward = ItemService.gI().createNewItem((short) i);
                RewardService.gI().initBaseOptionSaoPhaLe(itemReward);
                itemReward.quantity = Util.nextInt(1, 5);
                InventoryService.gI().addItemBag(pl, itemReward, 0);
                pl.textRuongGo.add(text + itemReward.getInfoItem());
            }
            if (param == 11) {
                itemReward = ItemService.gI().createNewItem((short) ConstItem.MANH_NHAN);
                itemReward.quantity = Util.nextInt(1, 3);
                InventoryService.gI().addItemBag(pl, itemReward, 0);
                pl.textRuongGo.add(text + itemReward.getInfoItem());
            }
            NpcService.gI().createMenuConMeo(pl, ConstNpc.RUONG_GO, -1, "Bạn nhận được\n|1|+" + Util.numberToMoney(gold) + " vàng", "OK [" + pl.textRuongGo.size() + "]");
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            pl.inventory.addGold(gold);
            InventoryService.gI().sendItemBags(pl);
            PlayerService.gI().sendInfoHpMpMoney(pl);
        } else {
            Service.getInstance().sendThongBao(pl, "Vì bạn quên không lấy chìa nên cần đợi 24h để bẻ khóa");
        }
    }

    private void useItemChangeFlagBag(Player player, Item item) {
        switch (item.template.id) {
            case 865: //kiem Z
                if (!player.effectFlagBag.useKiemz) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useKiemz = !player.effectFlagBag.useKiemz;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 994: //vỏ ốc
                break;
            case 995: //cây kem
                break;
            case 996: //cá heo
                break;
            case 997: //con diều
                break;
            case 998: //diều rồng
                if (!player.effectFlagBag.useDieuRong) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useDieuRong = !player.effectFlagBag.useDieuRong;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 999: //mèo mun
                if (!player.effectFlagBag.useMeoMun) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useMeoMun = !player.effectFlagBag.useMeoMun;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 1000: //xiên cá
                if (!player.effectFlagBag.useXienCa) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useXienCa = !player.effectFlagBag.useXienCa;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 1001: //phóng heo
                if (!player.effectFlagBag.usePhongHeo) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.usePhongHeo = !player.effectFlagBag.usePhongHeo;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 954:
                if (!player.effectFlagBag.useHoaVang) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useHoaVang = !player.effectFlagBag.useHoaVang;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 955:
                if (!player.effectFlagBag.useHoaHong) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useHoaHong = !player.effectFlagBag.useHoaHong;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 852:
                if (!player.effectFlagBag.useGayTre) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useGayTre = !player.effectFlagBag.useGayTre;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
        }
        Service.getInstance().point(player);
        Service.getInstance().sendFlagBag(player);
    }

    public void hopQuaTanThu(Player pl, Item it) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 14) {
            int gender = pl.gender;
            int[] id = {gender, 6 + gender, 21 + gender, 27 + gender, 12, 194, 441, 442, 443, 444, 445, 446, 447};
            int[] soluong = {1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10};
            int[] option = {0, 0, 0, 0, 0, 73, 95, 96, 97, 98, 99, 100, 101};
            int[] param = {0, 0, 0, 0, 0, 0, 5, 5, 5, 3, 3, 5, 5};
            int arrLength = id.length - 1;

            for (int i = 0; i < arrLength; i++) {
                if (i < 5) {
                    Item item = ItemService.gI().createNewItem((short) id[i]);
                    RewardService.gI().initBaseOptionClothes(item.template.id, item.template.type, item.itemOptions);
                    item.itemOptions.add(new ItemOption(107, 3));
                    InventoryService.gI().addItemBag(pl, item, 0);
                } else {
                    Item item = ItemService.gI().createNewItem((short) id[i]);
                    item.quantity = soluong[i];
                    item.itemOptions.add(new ItemOption(option[i], param[i]));
                    InventoryService.gI().addItemBag(pl, item, 0);
                }
            }

            int[] idpet = {916, 917, 918, 942, 943, 944, 1046, 1039, 1040};

            Item item = ItemService.gI().createNewItem((short) idpet[Util.nextInt(0, idpet.length - 1)]);
            item.itemOptions.add(new ItemOption(50, Util.nextInt(5, 10)));
            item.itemOptions.add(new ItemOption(77, Util.nextInt(5, 10)));
            item.itemOptions.add(new ItemOption(103, Util.nextInt(5, 10)));
            item.itemOptions.add(new ItemOption(93, 3));
            InventoryService.gI().addItemBag(pl, item, 0);

            InventoryService.gI().subQuantityItemsBag(pl, it, 1);
            InventoryService.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "Chúc bạn chơi game vui vẻ");
        } else {
            Service.getInstance().sendThongBao(pl, "Cần tối thiểu 14 ô trống để nhận thưởng");
        }
    }

    private void openbox2010(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {17, 16, 15, 675, 676, 677, 678, 679, 680, 681, 580, 581, 582};
            int[][] gold = {{5000, 20000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            if (temp[index] >= 15 && temp[index] <= 17) {
                it.itemOptions.add(new ItemOption(73, 0));

            } else if (temp[index] >= 580 && temp[index] <= 582 || temp[index] >= 675 && temp[index] <= 681) { // cải trang

                it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 30)));
                it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 30)));
                it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30)));
                it.itemOptions.add(new ItemOption(95, Util.nextInt(5, 15)));
                it.itemOptions.add(new ItemOption(96, Util.nextInt(5, 15)));

                if (Util.isTrue(1, 200)) {
                    it.itemOptions.add(new ItemOption(74, 0));
                } else {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                }

            } else {
                it.itemOptions.add(new ItemOption(73, 0));
            }
            InventoryService.gI().addItemBag(pl, it, 0);
            icon[1] = it.template.iconID;

            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void capsule8thang3(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {17, 16, 15, 675, 676, 677, 678, 679, 680, 681, 580, 581, 582, 1154, 1155, 1156, 860, 1041, 1042, 1043, 1103, 1104, 1105, 1106, 954, 955};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            if (Util.isTrue(30, 100)) {
                int ruby = Util.nextInt(1, 5);
                pl.inventory.ruby += ruby;
                CombineServiceNew.gI().sendEffectOpenItem(pl, item.template.iconID, (short) 7743);
                PlayerService.gI().sendInfoHpMpMoney(pl);
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                InventoryService.gI().sendItemBags(pl);
                Service.getInstance().sendThongBao(pl, "Bạn nhận được " + ruby + " Hồng Ngọc");
                return;
            }
            if (it.template.type == 5) { // cải trang

                it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 35)));
                it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 35)));
                it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 35)));
                it.itemOptions.add(new ItemOption(117, Util.nextInt(10, 20)));

            } else if (it.template.id == 954 || it.template.id == 955) {
                it.itemOptions.add(new ItemOption(50, Util.nextInt(10, 20)));
                it.itemOptions.add(new ItemOption(77, Util.nextInt(10, 20)));
                it.itemOptions.add(new ItemOption(103, Util.nextInt(10, 20)));
            }

            if (it.template.type == 5 || it.template.id == 954 || it.template.id == 955) {
                if (Util.isTrue(1, 200)) {
                    it.itemOptions.add(new ItemOption(74, 0));
                } else {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                }
            }
            InventoryService.gI().addItemBag(pl, it, 0);
            icon[1] = it.template.iconID;

            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void openboxsukien(Player pl, Item item, int idsukien) {
        try {
            switch (idsukien) {
                case 1:
                    if (Manager.EVENT_SEVER == idsukien) {
                        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                            short[] temp = {16, 15, 865, 999, 1000, 1001, 739, 742, 743};
                            int[][] gold = {{5000, 20000}};
                            byte index = (byte) Util.nextInt(0, temp.length - 1);
                            short[] icon = new short[2];
                            icon[0] = item.template.iconID;

                            Item it = ItemService.gI().createNewItem(temp[index]);

                            if (temp[index] >= 15 && temp[index] <= 16) {
                                it.itemOptions.add(new ItemOption(73, 0));

                            } else if (temp[index] == 865) {

                                it.itemOptions.add(new ItemOption(30, 0));

                                if (Util.isTrue(1, 30)) {
                                    it.itemOptions.add(new ItemOption(93, 365));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 999) { // mèo mun
                                it.itemOptions.add(new ItemOption(77, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 1000) { // xiên cá
                                it.itemOptions.add(new ItemOption(103, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 1001) { // Phóng heo
                                it.itemOptions.add(new ItemOption(50, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }

                            } else if (temp[index] == 739) { // cải trang Billes

                                it.itemOptions.add(new ItemOption(77, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(50, Util.nextInt(30, 45)));

                                if (Util.isTrue(1, 100)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }

                            } else if (temp[index] == 742) { // cải trang Caufila

                                it.itemOptions.add(new ItemOption(77, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(50, Util.nextInt(30, 45)));

                                if (Util.isTrue(1, 100)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 743) { // chổi bay
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }

                            } else {
                                it.itemOptions.add(new ItemOption(73, 0));
                            }
                            InventoryService.gI().addItemBag(pl, it, 0);
                            icon[1] = it.template.iconID;

                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            InventoryService.gI().sendItemBags(pl);

                            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
                        } else {
                            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
                        }
                        break;
                    } else {
                        Service.getInstance().sendThongBao(pl, "Sự kiện đã kết thúc");
                    }
                case ConstEvent.SU_KIEN_20_11:
                    if (Manager.EVENT_SEVER == idsukien) {
                        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                            short[] temp = {16, 15, 1039, 954, 955, 710, 711, 1040, 2023, 999, 1000, 1001};
                            byte index = (byte) Util.nextInt(0, temp.length - 1);
                            short[] icon = new short[2];
                            icon[0] = item.template.iconID;
                            Item it = ItemService.gI().createNewItem(temp[index]);
                            if (temp[index] >= 15 && temp[index] <= 16) {
                                it.itemOptions.add(new ItemOption(73, 0));
                            } else if (temp[index] == 1039) {
                                it.itemOptions.add(new ItemOption(50, 10));
                                it.itemOptions.add(new ItemOption(77, 10));
                                it.itemOptions.add(new ItemOption(103, 10));
                                it.itemOptions.add(new ItemOption(30, 0));
                                it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                            } else if (temp[index] == 954) {
                                it.itemOptions.add(new ItemOption(50, 15));
                                it.itemOptions.add(new ItemOption(77, 15));
                                it.itemOptions.add(new ItemOption(103, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(79, 80)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 955) {
                                it.itemOptions.add(new ItemOption(50, 20));
                                it.itemOptions.add(new ItemOption(77, 20));
                                it.itemOptions.add(new ItemOption(103, 20));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(79, 80)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 710) {//cải trang quy lão kame
                                it.itemOptions.add(new ItemOption(50, 22));
                                it.itemOptions.add(new ItemOption(77, 20));
                                it.itemOptions.add(new ItemOption(103, 20));
                                it.itemOptions.add(new ItemOption(194, 0));
                                it.itemOptions.add(new ItemOption(160, 35));
                                if (Util.isTrue(99, 100)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 711) { // cải trang jacky chun
                                it.itemOptions.add(new ItemOption(50, 23));
                                it.itemOptions.add(new ItemOption(77, 21));
                                it.itemOptions.add(new ItemOption(103, 21));
                                it.itemOptions.add(new ItemOption(195, 0));
                                it.itemOptions.add(new ItemOption(160, 50));
                                if (Util.isTrue(99, 100)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 1040) {
                                it.itemOptions.add(new ItemOption(50, 10));
                                it.itemOptions.add(new ItemOption(77, 10));
                                it.itemOptions.add(new ItemOption(103, 10));
                                it.itemOptions.add(new ItemOption(30, 0));
                                it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                            } else if (temp[index] == 2023) {
                                it.itemOptions.add(new ItemOption(30, 0));
                            } else if (temp[index] == 999) { // mèo mun
                                it.itemOptions.add(new ItemOption(77, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 1000) { // xiên cá
                                it.itemOptions.add(new ItemOption(103, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 1001) { // Phóng heo
                                it.itemOptions.add(new ItemOption(50, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else {
                                it.itemOptions.add(new ItemOption(73, 0));
                            }
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            icon[1] = it.template.iconID;
                            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
                            InventoryService.gI().addItemBag(pl, it, 0);
                            int ruby = Util.nextInt(1, 5);
                            pl.inventory.ruby += ruby;
                            InventoryService.gI().sendItemBags(pl);
                            PlayerService.gI().sendInfoHpMpMoney(pl);
                            Service.getInstance().sendThongBao(pl, "Bạn được tặng kèm " + ruby + " Hồng Ngọc");
                        } else {
                            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
                        }
                    } else {
                        Service.getInstance().sendThongBao(pl, "Sự kiện đã kết thúc");
                    }
                    break;
                case ConstEvent.SU_KIEN_NOEL:
                    if (Manager.EVENT_SEVER == idsukien) {
                        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                            int spl = Util.nextInt(441, 445);
                            int dnc = Util.nextInt(220, 224);
                            int nr = Util.nextInt(16, 18);
                            int nrBang = Util.nextInt(926, 931);

                            if (Util.isTrue(5, 90)) {
                                int ruby = Util.nextInt(1, 3);
                                pl.inventory.ruby += ruby;
                                CombineServiceNew.gI().sendEffectOpenItem(pl, item.template.iconID, (short) 7743);
                                PlayerService.gI().sendInfoHpMpMoney(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBags(pl);
                                Service.getInstance().sendThongBao(pl, "Bạn nhận được " + ruby + " Hồng Ngọc");
                            } else {
                                int[] temp = {spl, dnc, nr, nrBang, 387, 390, 393, 821, 822, 746, 380, 999, 1000, 1001, 936, 2022};
                                byte index = (byte) Util.nextInt(0, temp.length - 1);
                                short[] icon = new short[2];
                                icon[0] = item.template.iconID;
                                Item it = ItemService.gI().createNewItem((short) temp[index]);

                                if (temp[index] >= 441 && temp[index] <= 443) {// sao pha le
                                    it.itemOptions.add(new ItemOption(temp[index] - 346, 5));
                                    it.quantity = 10;
                                } else if (temp[index] >= 444 && temp[index] <= 445) {
                                    it.itemOptions.add(new ItemOption(temp[index] - 346, 3));
                                    it.quantity = 10;
                                } else if (temp[index] >= 220 && temp[index] <= 224) { // da nang cap
                                    it.quantity = 10;
                                } else if (temp[index] >= 387 && temp[index] <= 393) { // mu noel do
                                    it.itemOptions.add(new ItemOption(50, Util.nextInt(30, 40)));
                                    it.itemOptions.add(new ItemOption(77, Util.nextInt(30, 40)));
                                    it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 40)));
                                    it.itemOptions.add(new ItemOption(80, Util.nextInt(10, 20)));
                                    it.itemOptions.add(new ItemOption(106, 0));
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
                                    it.itemOptions.add(new ItemOption(199, 0));
                                } else if (temp[index] == 936) { // tuan loc
                                    it.itemOptions.add(new ItemOption(50, Util.nextInt(5, 10)));
                                    it.itemOptions.add(new ItemOption(77, Util.nextInt(5, 10)));
                                    it.itemOptions.add(new ItemOption(103, Util.nextInt(5, 10)));
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(3, 30)));
                                } else if (temp[index] == 822) { //cay thong noel
                                    it.itemOptions.add(new ItemOption(50, Util.nextInt(10, 20)));
                                    it.itemOptions.add(new ItemOption(77, Util.nextInt(10, 20)));
                                    it.itemOptions.add(new ItemOption(103, Util.nextInt(10, 20)));
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(3, 30)));
                                    it.itemOptions.add(new ItemOption(30, 0));
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else if (temp[index] == 746) { // xe truot tuyet
                                    it.itemOptions.add(new ItemOption(74, 0));
                                    it.itemOptions.add(new ItemOption(30, 0));
                                    if (Util.isTrue(99, 100)) {
                                        it.itemOptions.add(new ItemOption(93, Util.nextInt(30, 360)));
                                    }
                                } else if (temp[index] == 999) { // mèo mun
                                    it.itemOptions.add(new ItemOption(77, 15));
                                    it.itemOptions.add(new ItemOption(74, 0));
                                    it.itemOptions.add(new ItemOption(30, 0));
                                    if (Util.isTrue(99, 100)) {
                                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                    }
                                } else if (temp[index] == 1000) { // xiên cá
                                    it.itemOptions.add(new ItemOption(103, 15));
                                    it.itemOptions.add(new ItemOption(74, 0));
                                    it.itemOptions.add(new ItemOption(30, 0));
                                    if (Util.isTrue(99, 100)) {
                                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                    }
                                } else if (temp[index] == 1001) { // Phóng heo
                                    it.itemOptions.add(new ItemOption(50, 15));
                                    it.itemOptions.add(new ItemOption(74, 0));
                                    it.itemOptions.add(new ItemOption(30, 0));
                                    if (Util.isTrue(99, 100)) {
                                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                    }
                                } else if (temp[index] == 2022 || temp[index] == 821) {
                                    it.itemOptions.add(new ItemOption(30, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(73, 0));
                                }
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                icon[1] = it.template.iconID;
                                CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
                                InventoryService.gI().addItemBag(pl, it, 0);
                                InventoryService.gI().sendItemBags(pl);
                            }
                        } else {
                            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
                        }
                    } else {
                        Service.getInstance().sendThongBao(pl, "Sự kiện đã kết thúc");
                    }
                    break;
                case ConstEvent.SU_KIEN_TET:
                    if (Manager.EVENT_SEVER == idsukien) {
                        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                            short[] icon = new short[2];
                            icon[0] = item.template.iconID;
                            RandomCollection<Integer> rd = Manager.HOP_QUA_TET;
                            int tempID = rd.next();
                            Item it = ItemService.gI().createNewItem((short) tempID);
                            if (it.template.type == 11) {//FLAGBAG
                                it.itemOptions.add(new ItemOption(50, Util.nextInt(5, 20)));
                                it.itemOptions.add(new ItemOption(77, Util.nextInt(5, 20)));
                                it.itemOptions.add(new ItemOption(103, Util.nextInt(5, 20)));
                            } else if (tempID >= 1159 && tempID <= 1161) {
                                it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30)));
                                it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 30)));
                                it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 30)));
                                it.itemOptions.add(new ItemOption(106, 0));
                            } else if (tempID == ConstItem.CAI_TRANG_SSJ_3_WHITE) {
                                it.itemOptions.add(new ItemOption(50, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(77, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(5, Util.nextInt(10, 25)));
                                it.itemOptions.add(new ItemOption(104, Util.nextInt(5, 15)));
                            }
                            int type = it.template.type;
                            if (type == 5 || type == 11) {// cải trang & flagbag
                                if (Util.isTrue(199, 200)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                                it.itemOptions.add(new ItemOption(199, 0));//KHÔNG THỂ GIA HẠN
                            } else if (type == 23) {// thú cưỡi
                                if (Util.isTrue(199, 200)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 5)));
                                }
                            }
                            if (tempID >= ConstItem.MANH_AO && tempID <= ConstItem.MANH_GANG_TAY) {
                                it.quantity = Util.nextInt(5, 15);
                            } else {
                                it.itemOptions.add(new ItemOption(74, 0));
                            }
                            icon[1] = it.template.iconID;
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
                            InventoryService.gI().addItemBag(pl, it, 0);
                            InventoryService.gI().sendItemBags(pl);
                            break;
                        } else {
                            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
                        }
                    } else {
                        Service.getInstance().sendThongBao(pl, "Sự kiện đã kết thúc");
                    }
                    break;
            }
        } catch (Exception e) {
            logger.error("Lỗi mở hộp quà", e);
        }
    }

    private void openboxkichhoat(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {76, 188, 189, 190, 441, 442, 447, 2010, 2009, 865, 938, 939, 940, 16, 17, 18, 19, 20, 946, 947, 948, 382, 383, 384, 385};
            int[][] gold = {{5000, 20000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index <= 3 && index >= 0) {
                pl.inventory.addGold(Util.nextInt(gold[0][0], gold[0][1]));
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {

                Item it = ItemService.gI().createNewItem(temp[index]);
                if (temp[index] == 441) {
                    it.itemOptions.add(new ItemOption(95, 5));
                } else if (temp[index] == 442) {
                    it.itemOptions.add(new ItemOption(96, 5));
                } else if (temp[index] == 447) {
                    it.itemOptions.add(new ItemOption(101, 5));
                } else if (temp[index] >= 2009 && temp[index] <= 2010) {
                    it.itemOptions.add(new ItemOption(30, 0));
                } else if (temp[index] == 865) {
                    it.itemOptions.add(new ItemOption(30, 0));
                    if (Util.isTrue(1, 20)) {
                        it.itemOptions.add(new ItemOption(93, 365));
                    } else {
                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                    }
                } else if (temp[index] >= 938 && temp[index] <= 940) {
                    it.itemOptions.add(new ItemOption(77, 35));
                    it.itemOptions.add(new ItemOption(103, 35));
                    it.itemOptions.add(new ItemOption(50, 35));
                    if (Util.isTrue(1, 50)) {
                        it.itemOptions.add(new ItemOption(116, 0));
                    } else {
                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                    }
                } else if (temp[index] >= 946 && temp[index] <= 948) {
                    it.itemOptions.add(new ItemOption(77, 35));
                    it.itemOptions.add(new ItemOption(103, 35));
                    it.itemOptions.add(new ItemOption(50, 35));
                    if (Util.isTrue(1, 20)) {
                        it.itemOptions.add(new ItemOption(93, 365));
                    } else {
                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                    }
                } else {
                    it.itemOptions.add(new ItemOption(73, 0));
                }
                InventoryService.gI().addItemBag(pl, it, 0);
                icon[1] = it.template.iconID;

            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void openPhieuCaiTrangHaiTac(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item ct = ItemService.gI().createNewItem((short) Util.nextInt(618, 626));
            ct.itemOptions.add(new ItemOption(147, 3));
            ct.itemOptions.add(new ItemOption(77, 3));
            ct.itemOptions.add(new ItemOption(103, 3));
            ct.itemOptions.add(new ItemOption(149, 0));
            if (item.template.id == 2006) {
                ct.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
            } else if (item.template.id == 2007) {
                ct.itemOptions.add(new ItemOption(93, Util.nextInt(7, 30)));
            }
            InventoryService.gI().addItemBag(pl, ct, 0);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, item.template.iconID, ct.template.iconID);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void eatGrapes(Player pl, Item item) {
        int percentCurrentStatima = pl.nPoint.stamina * 100 / pl.nPoint.maxStamina;
        if (percentCurrentStatima > 50) {
            Service.getInstance().sendThongBao(pl, "Thể lực vẫn còn trên 50%");
            return;
        } else if (item.template.id == 211) {
            pl.nPoint.stamina = pl.nPoint.maxStamina;
            Service.getInstance().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 100%");
        } else if (item.template.id == 212) {
            pl.nPoint.stamina += (pl.nPoint.maxStamina * 20 / 100);
            Service.getInstance().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 20%");
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendCurrentStamina(pl);
    }

    private void buffClan(Player player, Item item) {
        try {
            if (player.clan != null) {
                // Gọi service buff clan
                ClanService.gI().buffClan(player);

                // Trừ 1 item sau khi sử dụng
                InventoryService.gI().subQuantityItemsBag(player, item, 1);
                InventoryService.gI().sendItemBags(player);
                PlayerService.gI().sendCurrentStamina(player);

                Service.getInstance().sendThongBao(player, "Bạn đã sử dụng vật phẩm buff toàn clan!");
            } else {
                Service.getInstance().sendThongBao(player, "Bạn chưa có bang hội để sử dụng vật phẩm này!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCapsuleVang(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {1066, 1067, 1068, 1069, 1070};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (Util.isTrue(8, 100)) {
                Item it = ItemService.gI().createNewItem((short) 934, 99);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 0);
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = it.template.iconID;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 0);
                icon[1] = it.template.iconID;
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void openCapsuleBac(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {18, 17, 16, 15, 1346, 1099, 1100, 1101, 1102};
            int[][] gold = {{20000000, 400000000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (Util.isTrue(20, 100)) {
                pl.inventory.addGold(Util.nextInt(gold[0][0], gold[0][1]));
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 0);
                icon[1] = it.template.iconID;
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

private void thechienbinh(Player pl, Item item) {
    if (pl == null || item == null) {
        return;
    }
    
    // Kiểm tra số lượng item trước khi xử lý
    if (item.quantity <= 0) {
        Service.getInstance().sendThongBao(pl, "Vật phẩm không hợp lệ!");
        return;
    }
    
    final int accountId = pl.getSession().userId;
    final int amountVND = VND_ITEM_AMOUNT;
    
    // 1) Cộng VND vào DB
    try (Connection con = DBService.gI().getConnectionForGame(); 
         PreparedStatement ps = con.prepareStatement(
                "UPDATE account SET vnd = vnd + ? WHERE id = ?"
         )) {
        ps.setInt(1, amountVND);
        ps.setInt(2, accountId);
        int updated = ps.executeUpdate();
        if (updated <= 0) {
            Service.getInstance().sendThongBao(pl, "Không tìm thấy tài khoản để cộng VNĐ.");
            return;
        }
    } catch (Exception e) {
        e.printStackTrace();
        Service.getInstance().sendThongBao(pl, "Có lỗi khi cộng VNĐ, thử lại sau!");
        return;
    }
    
    // 2) Trừ item - XỬ LÝ ĐẶC BIỆT KHI CÒN 1 CÁI
    try {
        if (item.quantity == 1) {
            // Nếu còn 1 cái, xóa luôn khỏi túi
            InventoryService.gI().removeItemBag(pl, item);
        } else {
            // Nếu > 1, trừ số lượng bình thường
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        }
        InventoryService.gI().sendItemBags(pl);
    } catch (Exception e) {
        e.printStackTrace();
        // Nếu lỗi ở đây, cần rollback VND trong DB
        Service.getInstance().sendThongBao(pl, "Có lỗi khi xử lý vật phẩm!");
        return;
    }
    
    // 3) Đồng bộ RAM
    try {
        pl.getSession().vnd += amountVND;
    } catch (Exception ignored) {
    }
    
    // 4) Hiệu ứng + thông báo
    CombineServiceNew.gI().sendEffectOpenItem(pl, (short) item.template.iconID, (short) 930);
    Service.getInstance().sendThongBao(pl, "Bạn đã nhận +" + amountVND + " VNĐ");
    Service.getInstance().sendMoney(pl);
}
    private void danangcap(Player pl, Item item) {
        // Danh sách id item sẽ cấp
        short[] temp = {220, 221, 222, 223, 224};

        // Kiểm tra số ô trống (nếu item stackable thì chỉ cần 5 ô)
        if (InventoryService.gI().getCountEmptyBag(pl) < temp.length) {
            Service.getInstance().sendThongBao(pl, "Hành trang không đủ chỗ để mở hộp");
            return;
        }

        // Lấy icon gốc (hộp)
        short[] icon = new short[2];
        icon[0] = item.template.iconID;

        // Tạo & add tất cả item
        for (short id : temp) {
            Item it = ItemService.gI().createNewItem(id);
            it.quantity = 9999; // set số lượng 9999 (cần chắc class Item có field quantity)

            // Nếu muốn add option mặc định, ví dụ:
            // it.itemOptions.add(new ItemOption(73, 0));
            InventoryService.gI().addItemBag(pl, it, 0);
            icon[1] = it.template.iconID; // icon của item cuối (chỉ để show effect)
        }

        // Trừ đi 1 hộp
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);

        // Hiện hiệu ứng mở hộp
        CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
    }

    private void openSkhThuong(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] dotd = {0, 6, 21, 27, 12};
            short[] donm = {1, 7, 22, 28, 12};
            short[] doxd = {2, 8, 23, 29, 12};
            switch (item.template.id) {
                case 2000: {
                    Item it = ItemService.gI().createNewItem(dotd[(byte) Util.nextInt(0, dotd.length - 1)]);
                    RewardService.gI().initBaseOptionClothes(it.template.id, it.template.type, it.itemOptions);
                    addOptionSkh(0, it.template.type, it);
                    //it.itemOptions.add(new ItemOption(107, 5));
                    InventoryService.gI().addItemBag(pl, it, 0);
                    break;
                }
                case 2001: {
                    Item it = ItemService.gI().createNewItem(donm[(byte) Util.nextInt(0, donm.length - 1)]);
                    RewardService.gI().initBaseOptionClothes(it.template.id, it.template.type, it.itemOptions);
                    addOptionSkh(1, it.template.type, it);
                    // it.itemOptions.add(new ItemOption(107, 5));
                    InventoryService.gI().addItemBag(pl, it, 0);
                    break;
                }
                case 2002: {
                    Item it = ItemService.gI().createNewItem(doxd[(byte) Util.nextInt(0, doxd.length - 1)]);
                    RewardService.gI().initBaseOptionClothes(it.template.id, it.template.type, it.itemOptions);
                    addOptionSkh(2, it.template.type, it);
                    //it.itemOptions.add(new ItemOption(107, 5));
                    InventoryService.gI().addItemBag(pl, it, 0);
                    break;
                }
                default:
                    break;
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.getInstance().sendThongBao(pl, "Hành trang không đủ chổ trống");
        }
    }

    private static final int[][][] ACTIVATION_SET_NO = {{{129, 141}, {127, 139}, {128, 140}}, //songoku - thien xin hang - kirin
    {{131, 143}, {132, 144}, {130, 142}}, //oc tieu - pikkoro daimao - picolo
    {{135, 138}, {133, 136}, {134, 137}} //kakarot - cadic - nappa
};

    public void addOptionSkh(int gender, int type, Item item) {
        if (type <= 4) {
            int[] idOption = ACTIVATION_SET_NO[gender][Util.nextInt(0, 2)];
            item.itemOptions.add(new ItemOption(idOption[0], 1)); //tên set
            item.itemOptions.add(new ItemOption(idOption[1], 1)); //hiệu ứng set
            item.itemOptions.add(new ItemOption(30, 1)); //không thể giao dịch
        }
    }

    private void tuivang(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int[][] gold = {{300000000, 500000000}};
            pl.inventory.addGold(Util.nextInt(gold[0][0], gold[0][1]));
            PlayerService.gI().sendInfoHpMpMoney(pl);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void openCSKB(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {76, 188, 189, 190, 381, 382, 383, 384, 385};
            int[][] gold = {{5000, 20000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index <= 3) {
                pl.inventory.addGold(Util.nextInt(gold[0][0], gold[0][1]));
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 0);
                icon[1] = it.template.iconID;
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void openMabuEgg(Player pl, Item item) {
        if (pl.mabuEgg == null) {
            MabuEgg.createMabuEgg(pl);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "Bạn đã bắt đầu ấp Trứng Mabu");
        } else {
            Service.getInstance().sendThongBao(pl, "Vui lòng Hủy hoặc Nở trứng Mabu ở nhà");
        }
    }

    private void useItemTime(Player pl, Item item) {
        boolean updatePoint = false;
        switch (item.template.id) {
            case 382: //bổ huyết
                if (pl.itemTime.isUseBoHuyet2) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoHuyet = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet = true;
                updatePoint = true;
                break;
            case 383: //bổ khí
                if (pl.itemTime.isUseBoKhi2) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoKhi = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi = true;
                updatePoint = true;
                break;

            case 384: //giáp xên
                if (pl.itemTime.isUseGiapXen2) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeGiapXen = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen = true;
                updatePoint = true;
                break;
            case 381: //cuồng nộ
                if (pl.itemTime.isUseCuongNo2) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeCuongNo = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo = true;
                updatePoint = true;
                break;
            case 385: //ẩn danh
                pl.itemTime.lastTimeAnDanh = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh = true;
                break;
            case ConstItem.BO_HUYET_2: //bổ huyết 2
                if (pl.itemTime.isUseBoHuyet) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet2 = true;
                updatePoint = true;
                break;
            case ConstItem.BO_KHI_2: //bổ khí 2
                if (pl.itemTime.isUseBoKhi) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoKhi2 = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi2 = true;
                updatePoint = true;
                break;

            case ConstItem.GIAP_XEN_BO_HUNG_2: //giáp xên 2
                if (pl.itemTime.isUseGiapXen) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeGiapXen2 = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen2 = true;
                updatePoint = true;
                break;
            case ConstItem.CUONG_NO_2: //cuồng nộ 222071
                if (pl.itemTime.isUseCuongNo) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeCuongNo2 = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo2 = true;
                updatePoint = true;
                break;
            case ConstItem.TN_SM:
                if (pl.itemTime.isUsetnsm) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimetnsm = System.currentTimeMillis();
                pl.itemTime.isUsetnsm = true;
                updatePoint = true;
                break;

            case ConstItem.tnsm:
                if (pl.itemTime.isbinhx4) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimebinhx4 = System.currentTimeMillis();
                pl.itemTime.isbinhx4 = true;
                updatePoint = true;
                break;

            case 379: //máy dò
                 if (pl.itemTime.isUseMayDoskh) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeUseMayDo = System.currentTimeMillis();
                pl.itemTime.isUseMayDo = true;
                break;
                
            case 1109: //máy dò
                 if (pl.itemTime.isUseMayDo) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeUseMayDoskh = System.currentTimeMillis();
                pl.itemTime.isUseMayDoskh = true;
                break;

            case 663: //bánh pudding
            case 664: //xúc xíc
            case 665: //kem dâu
            case 666: //mì ly
            case 667: //sushi
                pl.itemTime.lastTimeEatMeal = System.currentTimeMillis();
                pl.itemTime.isEatMeal = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal);
                pl.itemTime.iconMeal = item.template.iconID;
                updatePoint = true;
                break;
            case ConstItem.BANH_CHUNG_CHIN:
                pl.itemTime.lastTimeBanhChung = System.currentTimeMillis();
                pl.itemTime.isUseBanhChung = true;
                updatePoint = true;
                break;
            case ConstItem.BANH_TET_CHIN:
                pl.itemTime.lastTimeBanhTet = System.currentTimeMillis();
                pl.itemTime.isUseBanhTet = true;
                updatePoint = true;
                break;
            case 1317:// cn
                pl.itemTimesieucap.lastTimeUseXiMuoi = System.currentTimeMillis();
                pl.itemTimesieucap.isUseXiMuoi = true;
                updatePoint = true;
                break;
            case 1385:
                pl.itemTimesieucap.lastTimeCaRot = System.currentTimeMillis();
                pl.itemTimesieucap.isUseCaRot = true;
                updatePoint = true;
                break;
            case 899:
                pl.itemTimesieucap.lastTimeKeo = System.currentTimeMillis();
                pl.itemTimesieucap.isKeo = true;
                updatePoint = true;
                break;
            case 1386:
                EffectSkillService.gI().sendEffectBienhinh(pl);
                EffectSkillService.gI().setIsBienhinh(pl);
                EffectSkillService.gI().sendEffectBienhinh(pl);

                Service.getInstance().sendSpeedPlayer(pl, 0);
                Service.getInstance().Send_Caitrang(pl);
                Service.getInstance().sendSpeedPlayer(pl, -1);
                PlayerService.gI().sendInfoHpMp(pl);
                Service.getInstance().point(pl);
                Service.getInstance().Send_Info_NV(pl);
                //Service.getInstance().sendPartPlayer(pl);

                Service.getInstance().sendFlagBag(pl);
                Service.getInstance().sendInfoPlayerEatPea(pl);

                updatePoint = true;
                break;

            case 579: // đuôi khỉ
                pl.itemTimesieucap.lastTimeDuoikhi = System.currentTimeMillis();
                pl.itemTimesieucap.isDuoikhi = true;
                updatePoint = true;
                break;
            case 1201: //Đá ngục tù
                pl.itemTimesieucap.lastTimeDaNgucTu = System.currentTimeMillis();
                pl.itemTimesieucap.isDaNgucTu = true;
                updatePoint = true;
                break;
            // bánh trung thu
            case 465:
            case 466:
            case 472:
            case 473:
                pl.itemTimesieucap.lastTimeUseBanh = System.currentTimeMillis();
                pl.itemTimesieucap.isUseTrungThu = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTimesieucap.iconBanh);
                pl.itemTimesieucap.iconBanh = item.template.iconID;
                updatePoint = true;
                break;
        }
        if (updatePoint) {
            Service.getInstance().point(pl);
        }
        ItemTimeService.gI().sendAllItemTime(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
    }

    private void controllerCallRongThan(Player pl, Item item) {
        int tempId = item.template.id;
        if (tempId >= SummonDragon.NGOC_RONG_1_SAO && tempId <= SummonDragon.NGOC_RONG_7_SAO) {
            switch (tempId) {
                case SummonDragon.NGOC_RONG_1_SAO:
                case SummonDragon.NGOC_RONG_2_SAO:
                case SummonDragon.NGOC_RONG_3_SAO:
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) (tempId - 13), SummonDragon.DRAGON_SHENRON);
                    break;
                default:
                    NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_SUMMON_DRAGON, -1, "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao", "Hướng\ndẫn thêm\n(mới)", "OK");
                    break;
            }
        } else if (tempId == SummonDragon.NGOC_RONG_SIEU_CAP) {
            SummonDragon.gI().openMenuSummonShenron(pl, (byte) 1015, SummonDragon.DRAGON_BLACK_SHENRON);
        } else if (tempId >= SummonDragon.NGOC_RONG_BANG[0] && tempId <= SummonDragon.NGOC_RONG_BANG[6]) {
            switch (tempId) {
                case 925:
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) 925, SummonDragon.DRAGON_ICE_SHENRON);
                    break;
                default:
                    Service.getInstance().sendThongBao(pl, "Bạn chỉ có thể gọi rồng băng từ ngọc 1 sao");
                    break;
            }
        }
    }

    private void learnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                String[] subName = item.template.name.split("");
                byte level = Byte.parseByte(subName[subName.length - 1]);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 7) {
                    Service.getInstance().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.getInstance().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Skill skillNeed = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            Service.getInstance().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " cấp " + skillNeed.point + " trước!");
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            //System.out.println(curSkill.template.name + " - " + curSkill.point);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.getInstance().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Service.getInstance().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp " + (curSkill.point + 1) + " trước!");
                        }
                    }
                    InventoryService.gI().sendItemBags(pl);
                }
            } else {
                Service.getInstance().sendThongBao(pl, "Không thể thực hiện");

            }
        } catch (Exception e) {
            Log.error(UseItem.class, e);
        }
    }

    private void useTDLT(Player pl, Item item) {
        if (pl.itemTime.isUseTDLT) {
            ItemTimeService.gI().turnOffTDLT(pl, item);
        } else {
            ItemTimeService.gI().turnOnTDLT(pl, item);
        }
    }

    private void usePorata(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata2(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion2(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata3(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion3(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata4(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion4(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata5(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion5(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void openCapsuleUI(Player pl) {
        if (pl.isHoldNamecBall) {
            NamekBallWar.gI().dropBall(pl);
            Service.getInstance().sendFlagBag(pl);
        }
        pl.iDMark.setTypeChangeMap(ConstMap.CHANGE_CAPSULE);
        ChangeMapService.gI().openChangeMapTab(pl);
    }

    public void choseMapCapsule(Player pl, int index) {
        int zoneId = -1;
        if (index < 0 || index >= pl.mapCapsule.size()) {
            return;
        }
        Zone zoneChose = pl.mapCapsule.get(index);
        if (index != 0 || zoneChose.map.mapId == 21 || zoneChose.map.mapId == 22 || zoneChose.map.mapId == 23) {
            if (!(pl.zone != null && pl.zone instanceof ZSnakeRoad)) {
                pl.mapBeforeCapsule = pl.zone;
            } else {
                pl.mapBeforeCapsule = null;
            }
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        ChangeMapService.gI().changeMapBySpaceShip(pl, pl.mapCapsule.get(index).map.mapId, zoneId, -1);
    }

    private void doiskill4(Player pl, Item item) {
        if (pl.pet.nPoint.power > 20000000000L) {
            if (pl.pet != null) {
                if (pl.pet.playerSkill.skills.get(2).skillId != -1) {
                    pl.pet.openSkill4();
                    Service.getInstance().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    InventoryService.gI().sendItemBags(pl);
                } else {
                    Service.getInstance().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                }
            } else {
                Service.getInstance().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
            }
        } else {
            Service.getInstance().sendThongBao(pl, "Yêu cầu đệ tử có skill 4");
        }
    }

    private void upSkillPet(Player pl, Item item) {
        if (pl.pet == null) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        try {
            switch (item.template.id) {
                case 402: //skill 1
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 0)) {
                        Service.getInstance().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 403: //skill 2
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 1)) {
                        Service.getInstance().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 404: //skill 3
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 2)) {
                        Service.getInstance().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 759: //skill 4
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 3)) {
                        Service.getInstance().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
            }
        } catch (Exception e) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        }
    }
}

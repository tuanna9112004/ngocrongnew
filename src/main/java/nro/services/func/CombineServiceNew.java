package nro.services.func;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import nro.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.npc.Npc;
import nro.models.npc.NpcManager;
import nro.models.player.Player;
import nro.server.ServerNotify;
import nro.server.io.Message;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.RewardService;
import nro.services.Service;
import nro.utils.Util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nro.data.ItemData;
import nro.server.Manager;
import nro.services.PlayerService;
import nro.utils.Log;

/**
 *
 * @copyright 💖 GirlkuN 💖
 */
public class CombineServiceNew {

    private static final int COST_DOI_VE_DOI_DO_HUY_DIET = 500000000;
    private static final int COST_DAP_DO_KICH_HOAT = 500000000;
    private static final int COST_DOI_MANH_KICH_HOAT = 500000000;

    private static final int COST = 500000000;

    private static final byte MAX_STAR_ITEM = 9;
    private static final byte MAX_LEVEL_ITEM = 8;

    private static final byte OPEN_TAB_COMBINE = 0;
    private static final byte REOPEN_TAB_COMBINE = 1;
    private static final byte COMBINE_SUCCESS = 2;
    private static final byte COMBINE_FAIL = 3;
    private static final byte COMBINE_CHANGE_OPTION = 4;
    private static final byte COMBINE_DRAGON_BALL = 5;
    public static final byte OPEN_ITEM = 6;

    public static final int EP_SAO_TRANG_BI = 500;
    public static final int PHA_LE_HOA_TRANG_BI = 501;
    public static final int CHUYEN_HOA_TRANG_BI = 502;
    public static final int PHA_LE_HOA_TRANG_BI_X100 = 503;
    public static final int MO_CS_VY_THU = 504;
//    public static final int DAP_SET_KICH_HOAT = 504;
//    public static final int DOI_MANH_KICH_HOAT = 505;
//    public static final int DOI_CHUOI_KIEM = 506;
//    public static final int DOI_LUOI_KIEM = 507;
    public static final int NANG_CAP_NRO = 508;
//    public static final int OPTION_PORATA = 508;

    public static final int NANG_CAP_VAT_PHAM = 510;
    public static final int NANG_CAP_BONG_TAI = 511;
    public static final int LAM_PHEP_NHAP_DA = 512;
    public static final int NHAP_NGOC_RONG = 513;
    public static final int PHAN_RA_DO_THAN_LINH = 514;
    public static final int NANG_CAP_DO_TS = 515;
    public static final int Nang_Cap_SKH = 516;
    public static final int AN_TRANG_BI = 517;
    public static final int PHAP_SU_HOA = 518;
    public static final int TAY_PHAP_SU = 519;
    public static final int MO_CHI_SO_BONG_TAI = 520;
    public static final int NANG_CAP_SKH_TS = 521;

    public static final int NANG_CAP_CHAN_MENH = 523;
    public static final int CHUYEN_HOA_DO_HUY_DIET = 524;
    public static final int NANG_CAP_THAN_LINH = 525;
    public static final int NANG_CAP_HUY_DIET = 526;
    public static final int GIA_HAN_VAT_PHAM = 527;
    public static final int DE_TU_VIP = 534;
    public static final int PHAN_RA_DO_TS = 528;

    // START _ SÁCH TUYỆT KỸ //
    public static final int GIAM_DINH_SACH = 529;
    public static final int TAY_SACH = 530;
    public static final int NANG_CAP_SACH_TUYET_KY = 531;
    public static final int PHUC_HOI_SACH = 532;
    public static final int PHAN_RA_SACH = 533;
    public static final int DUC_LO_TRANG_BI = 536;
    public static final int KHAM_DA_TRANG_BI = 535;

    public static final int NANG_CAP_SKH = 537;
    public static final int MO_KHOA_GD = 538;
    public static final int DAP_THE_CAU_THU = 539;
    public static final int MO_CS_THE = 540;
    // END _ SÁCH TUYỆT KỸ //s

    private final Npc baHatMit;
    private final Npc npcwhists;

    private static CombineServiceNew i;

    public CombineServiceNew() {
        this.baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);
        this.npcwhists = NpcManager.getNpc(ConstNpc.WHIS);
    }

    private static final ConcurrentHashMap<Long, ReentrantLock> COMBINE_LOCKS = new ConcurrentHashMap<>();

    private static Lock lockOf(Player p) {
        return COMBINE_LOCKS.computeIfAbsent((long) p.id, k -> new ReentrantLock());
    }

    public static void releaseCombineLock(Player p) {
        ReentrantLock lk = COMBINE_LOCKS.remove((long) p.id);
        if (lk != null && lk.isHeldByCurrentThread()) {
            lk.unlock();
        }
    }

    public static CombineServiceNew gI() {
        if (i == null) {
            i = new CombineServiceNew();
        }
        return i;
    }

    /**
     * Mở tab đập đồ
     *
     * @param player
     * @param type kiểu đập đồ
     */
    public void openTabCombine(Player player, int type) {
        player.combineNew.setTypeCombine(type);
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_TAB_COMBINE);
            msg.writer().writeUTF(getTextInfoTabCombine(type));
            msg.writer().writeUTF(getTextTopTabCombine(type));
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    /**
     * Hiển thị thông tin đập đồ
     *
     * @param player
     * @param index
     */
    public void showInfoCombine(Player player, int[] index) {
        player.combineNew.clearItemCombine();
        if (index.length > 0) {
            for (int j = 0; j < index.length; j++) {
                player.combineNew.itemsCombine.add(player.inventory.itemsBag.get(index[j]));
            }
        }
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item trangBi = null;
                    Item daPhaLe = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (isTrangBiPhaLeHoa(item)) {
                            trangBi = item;
                        } else if (isDaPhaLe(item)) {
                            daPhaLe = item;
                        }
                    }
                    int star = 0; //sao pha lê đã ép
                    int starEmpty = 0; //lỗ sao pha lê
                    if (trangBi != null && daPhaLe != null) {
                        for (ItemOption io : trangBi.itemOptions) {
                            if (io.optionTemplate.id == 102) {
                                star = io.param;
                            } else if (io.optionTemplate.id == 107) {
                                starEmpty = io.param;
                            }
                        }
                        if (star < starEmpty) {
                            player.combineNew.gemCombine = getGemEpSao(star);
                            String npcSay = trangBi.template.name + "\n|2|";
                            for (ItemOption io : trangBi.itemOptions) {
                                if (io.optionTemplate.id != 102) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                            }
                            if (daPhaLe.template.type == 30) {
                                for (ItemOption io : daPhaLe.itemOptions) {
                                    npcSay += "|7|" + io.getOptionString() + "\n";
                                }
                            } else {
// --- TÍNH CHỈ SỐ THEO CẤP SAO --- //
                                int baseParam = getParamDaPhaLe(daPhaLe);
                                int previewParam = baseParam;

                                if (star >= 6 && star < 8) {
                                    previewParam = baseParam * 2;      // Sao 6–7–8 => x2
                                } else if (star >= 8) {
                                    previewParam = baseParam * 3;      // Sao 9 trở lên => x3
                                }

// --- HIỆN THỊ LÊN MENU --- //
                                npcSay += "|7|"
                                        + ItemService.gI().getItemOptionTemplate(getOptionDaPhaLe(daPhaLe)).name.replaceAll("#", previewParam + "")
                                        + "\n";
                            }
                            npcSay += "|6|*Từ sao thứ 6 trở đi, chỉ số ép vào sẽ được x2 , x 3 ở sao thứ 9*\n";
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.gemCombine) + " ngọc";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc");

                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                }
                break;
            case DAP_THE_CAU_THU: {
                List<Item> items = player.combineNew.itemsCombine;
                if (items.isEmpty() || items.size() > 6) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần từ 1 đến 6 thẻ cầu thủ (type 76)", "Đóng");
                    return;
                }

                // check type & lấy main
                Item main = items.get(0);
                if (main == null || !main.isNotNullItem() || main.template.type != 76) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Thẻ đầu tiên phải là thẻ cầu thủ (type 76)", "Đóng");
                    return;
                }
                // lấy Over & Level
                int overMain = getParamOption(main, 223);
                int level = getParamOption(main, 72);
                if (overMain <= 0) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Thẻ cầu thủ không hợp lệ (không có Over)", "Đóng");
                    return;
                }
                if (level >= 5) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Thẻ đã đạt cấp tối đa", "Đóng");
                    return;
                }

                // ----- TỈ LỆ MAX KHI ĐỦ 5 VẠCH PHÔI -----
                int baseRate;
                switch (level) {
                    case 0: // 0 -> 1
                        baseRate = 100;
                        break;
                    case 1: // 1 -> 2
                        baseRate = 80;
                        break;
                    case 2: // 2 -> 3
                        baseRate = 70;
                        break;
                    case 3: // 3 -> 4
                        baseRate = 60;
                        break;
                    case 4: // 4 -> 5
                        baseRate = 50;
                        break;
                    default:
                        baseRate = 0;
                }

                // ----- TÍNH VẠCH PHÔI THEO ROLE + PHÁT HIỆN THỪA PHÔI -----
                double totalVachRaw = 0.0;        // tổng vạch (chưa clamp)
                double accDetect = 0.0;          // dùng để phát hiện thừa
                List<Integer> redundantSlots = new ArrayList<>(); // lưu index phôi thừa (trong list items)

                for (int i = 1; i < items.size(); i++) {
                    Item phoi = items.get(i);
                    if (phoi == null || !phoi.isNotNullItem() || phoi.template.type != 76) {
                        continue;
                    }
                    int overPhoi = getParamOption(phoi, 223);
                    if (overPhoi <= 0) {
                        continue;
                    }

                    double vach = getVachPhoi(level, overMain, overPhoi);

                    // PHÁT HIỆN PHÔI THỪA:
                    // - Nếu trước khi cộng đã >=5 vạch -> phôi này chắc chắn thừa
                    // - Nếu trước đó <5 vạch và sau khi cộng mới >5 vạch -> phôi này là phôi "chốt" để lên full vạch, KHÔNG tính là thừa
                    if (accDetect >= 5.0) {
                        // Đã đủ 5 vạch rồi mà còn cộng thêm -> phôi thừa
                        redundantSlots.add(i);
                    } else {
                        // accDetect < 5.0
                        // nếu accDetect + vach > 5.0 => phôi này là phôi giúp vượt ngưỡng lên (hoặc vượt nhẹ) => vẫn giữ
                    }

                    accDetect += vach;
                    totalVachRaw += vach;
                }

                // chuẩn hoá theo 5 vạch để tính tỉ lệ
                double totalVachNorm = Math.min(totalVachRaw, 5.0) / 5.0;

                // TỈ LỆ CUỐI: baseRate * (vạch/5)
                double finalRateD = baseRate * totalVachNorm;
                if (finalRateD < 0) {
                    finalRateD = 0;
                } else if (finalRateD > 100) {
                    finalRateD = 100;
                }
                player.combineNew.ratioCombine = (int) Math.round(finalRateD);

                int phoiPercent = (int) Math.round(totalVachNorm * 100);

                // ----- GHÉP THÊM CÂU THÔNG BÁO THỪA PHÔI (NẾU CÓ) -----
                String extraWarn = "";
                if (!redundantSlots.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n|7|Bạn đang thừa vạch phôi, có thể bỏ bớt:\n");
                    for (int j = 0; j < redundantSlots.size(); j++) {
                        int idx = redundantSlots.get(j);
                        Item phoiThua = items.get(idx);
                        String ten = phoiThua != null && phoiThua.template != null
                                ? phoiThua.template.name : "Cầu thủ";
                        int displayIndex = idx + 1; // vì 0 là thẻ chính => người chơi thấy ô 1 là main, ô 2 là phôi 1...

                        sb.append("- Cầu thủ ").append(ten)
                                .append(" ở ô số ").append(displayIndex);
                    }
                    extraWarn = sb.toString();
                }

                String npcSay = "\n|7|ĐẬP THẺ CẦU THỦ\n"
                        + "|1|Thẻ chính: " + main.template.name + "\n"
                        + "|0|Over: " + overMain + "  -  Cấp: " + level + "\n"
                        + "|0|Vạch phôi:\n"
                        + progressBar(phoiPercent) + "\n"
                        + "|2|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%"
                        + extraWarn;

                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Đập thẻ", "Từ chối");
                break;
            }

            case MO_CS_THE: {
                List<Item> items = player.combineNew.itemsCombine;
                if (items.size() != 1) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần đúng 1 thẻ cầu thủ (type 76)", "Đóng");
                    return;
                }
                Item main = items.get(0);
                if (main == null || !main.isNotNullItem() || main.template.type != 76) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần đúng 1 thẻ cầu thủ (type 76)", "Đóng");
                    return;
                }
                int over = getParamOption(main, 223);
                int level = getParamOption(main, 72);
                if (over <= 0) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Thẻ cầu thủ không có Over", "Đóng");
                    return;
                }
                if (level <= 0) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Thẻ chưa được nâng cấp, không có chỉ số để mở", "Đóng");
                    return;
                }

                int posOtp = -1;
                String posName = "";
                if (hasOption(main, 220)) {
                    posOtp = 220;
                    posName = "ST";
                } else if (hasOption(main, 221)) {
                    posOtp = 221;
                    posName = "CAM";
                } else if (hasOption(main, 222)) {
                    posOtp = 222;
                    posName = "CB";
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Thẻ cầu thủ chưa có vị trí (220/221/222)", "Đóng");
                    return;
                }

                int param = over * level;
                if (param <= 0) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Chỉ số mới không hợp lệ", "Đóng");
                    return;
                }

                String statName;
                String statUnit; // %HP, %KI, %SD

                switch (posOtp) {
                    case 220:
                        statName = "ST";
                        statUnit = "%SD";
                        break;
                    case 221:
                        statName = "CAM";
                        statUnit = "%HP";
                        break;
                    case 222:
                        statName = "CB";
                        statUnit = "%KI";
                        break;
                    default:
                        statName = "Chỉ số";
                        statUnit = "%";
                }

                // param = over * level
                String reason = "|1|Cách tính: Over(" + over + ") × Cấp thẻ(" + level + ") = " + param + statUnit;

                String npcSay = "|7|MỞ CHỈ SỐ CẦU THỦ\n"
                        + "|1|Thẻ: " + main.template.name + "\n"
                        + "|0|Vị trí: " + posName + "\n"
                        + "|0|Over: " + over + "  -  Cấp: " + level + "\n"
                        + "|2|" + statName + " +" + param + statUnit + "\n"
                        + reason;

                // deterministic nên set 100%
                player.combineNew.ratioCombine = 100;

                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Mở chỉ số", "Từ chối");
                break;
            }

            case MO_KHOA_GD: {
                if (player.combineNew.itemsCombine.size() != 2) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 trang bị và Đá mở khóa", "Đóng");
                    break;
                }

                Item trangBi = null;
                Item daMoKhoa = null;

                // XÁC ĐỊNH 2 ITEM
                for (Item it : player.combineNew.itemsCombine) {
                    if (it.template.id == 1613) {
                        daMoKhoa = it;
                    } else {
                        trangBi = it;
                    }
                }

                // KIỂM TRA TRANG BỊ HỢP LỆ
                if (trangBi == null || !trangBi.isNotNullItem()
                        || trangBi.template.type < 0 || trangBi.template.type > 5) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Chỉ mở khóa trang bị từ type 0 → 5", "Đóng");
                    break;
                }

                // KIỂM TRA OPT 30
                boolean hasLock = false;
                for (ItemOption io : trangBi.itemOptions) {
                    if (io.optionTemplate.id == 30) {
                        hasLock = true;
                        break;
                    }
                }

                if (!hasLock) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Trang bị này không bị khóa giao dịch", "Đóng");
                    break;
                }

                // KIỂM TRA ĐÁ
                if (daMoKhoa == null || daMoKhoa.quantity < 10) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 10 Đá Mở Khóa (ID 1613)", "Đóng");
                    break;
                }

                // ====== TÍNH NĂNG HỢP LỆ → HIỆN INFO ======
                player.combineNew.goldCombine = 0;
                player.combineNew.gemCombine = 0;
                player.combineNew.ratioCombine = 100;

                String npcSay = "|7|" + trangBi.template.name + "\n|2|";

                // Hiển thị danh sách opt hiện tại
                for (ItemOption io : trangBi.itemOptions) {
                    npcSay += io.getOptionString() + "\n";
                }

                // Hiển thị thông báo tính năng
                npcSay += "|6|Mở khóa giúp trang bị giao dịch được\n";
                npcSay += "|6|Tỉ lệ thành công: 30%\n";
                npcSay += "|1|Cần 10 Đá mở khóa (ID 1613)";

                baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                        npcSay, "Mở khóa");

                break;
            }

            case NANG_CAP_SKH: {
                List<Item> items = player.combineNew.itemsCombine;
                Item base = null;
                int baseId = -1;

                if (items != null && !items.isEmpty()) {
                    base = items.get(0);
                    if (base != null && base.template != null) {
                        baseId = base.template.id;
                    }
                }

                // ====== DANH SÁCH ID THEO HẠNG ======
                int[] CAP12_IDS = {233, 237, 241, 245, 249, 253, 257, 261, 265, 269, 273, 277, 281};

                int[] TL_IDS = {
                    555, 556, 557, 558, 559, 560,
                    561, 562, 563, 564, 565, 566, 567
                };

                int[] HD_IDS = {
                    650, 651, 652, 653, 654, 655,
                    656, 657, 658, 659, 660, 661, 662
                };

                int[] TS_IDS = {
                    1048, 1049, 1050, 1051, 1052, 1053,
                    1054, 1055, 1056, 1057, 1058, 1059,
                    1060, 1061, 1062
                };

                java.util.function.IntPredicate isCap12 = id -> {
                    for (int v : CAP12_IDS) {
                        if (v == id) {
                            return true;
                        }
                    }
                    return false;
                };
                java.util.function.IntPredicate isTL = id -> {
                    for (int v : TL_IDS) {
                        if (v == id) {
                            return true;
                        }
                    }
                    return false;
                };
                java.util.function.IntPredicate isHD = id -> {
                    for (int v : HD_IDS) {
                        if (v == id) {
                            return true;
                        }
                    }
                    return false;
                };
                java.util.function.IntPredicate isTS = id -> {
                    for (int v : TS_IDS) {
                        if (v == id) {
                            return true;
                        }
                    }
                    return false;
                };

                String tierName = "Không xác định";
                String nextTierName = "bậc cao hơn";

                if (baseId != -1) {
                    if (isCap12.test(baseId)) {
                        tierName = "Cấp 12";
                        nextTierName = "Thần Linh";
                    } else if (isTL.test(baseId)) {
                        tierName = "Thần Linh";
                        nextTierName = "Hủy Diệt";
                    } else if (isHD.test(baseId)) {
                        tierName = "Hủy Diệt";
                        nextTierName = "Thiên Sứ";
                    } else if (isTS.test(baseId)) {
                        tierName = "Thiên Sứ";
                        nextTierName = "Không thể nâng cấp thêm";
                    }
                }

                // cost 500M vàng
                player.combineNew.goldCombine = (int) 500_000_000L;

                String npcSay = "|7|Nâng cấp đồ Set Kích Hoạt theo hạng\n";
                npcSay += "|2|Điều kiện:\n";
                npcSay += "• Cần đặt đúng 3 món trang bị\n";
                npcSay += "• 3 món phải cùng hạng: Cấp 12 / Thần Linh / Hủy Diệt / Thiên Sứ\n";
                npcSay += "• 3 món phải cùng hành tinh và đều có Set Kích Hoạt\n";
                npcSay += "|1|Hạng hiện tại (theo món đầu): " + tierName + "\n";
                npcSay += "|1|Hạng sau nâng cấp: " + nextTierName + "\n";
                npcSay += "|1|Chi phí: " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng\n";
                npcSay += "|4|Lưu ý: Sau khi xác nhận sẽ trừ 3 món nguyên liệu và 500M vàng.";

                String button = "Nâng cấp\n(500M vàng)";

                this.baHatMit.createOtherMenu(
                        player,
                        ConstNpc.MENU_START_COMBINE,
                        npcSay,
                        button
                );
            }
            break;

            case MO_CS_VY_THU: {

                // phải có ít nhất 1 item trong ô ghép
                if (player.combineNew.itemsCombine.size() != 1) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Hãy đặt 1 Vỹ Thú vào để nâng cấp", "Đóng");
                    break;
                }

                Item vyThu = player.combineNew.itemsCombine.get(0);

                // danh sách ID Vỹ Thú theo đúng thứ tự bạn muốn (mạnh dần)
                List<Short> vtRank = Arrays.asList(
                        (short) 1539, (short) 1540, (short) 1541, (short) 1542,
                        (short) 1543, (short) 1544, (short) 1545, (short) 1546,
                        (short) 1524 // mạnh nhất
                );

                if (!vtRank.contains(vyThu.template.id)) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Đây không phải là Vỹ Thú hợp lệ!", "Đóng");
                    break;
                }

                int rank = vtRank.indexOf(vyThu.template.id) + 1;
                int addParam = rank * 5;

                String msg = "|2|Nâng cấp Vỹ Thú\n";
                msg += "|7|ID: " + vyThu.template.id + "\n";
                msg += "|7|Cấp bậc: " + rank + "\n";
                msg += "|7|Sẽ tăng " + addParam + " % chỉ số cho 3 dòng:\n";
                msg += "|1|- HP \n";
                msg += "|1|- KI \n";
                msg += "|1|- Sức đánh \n";

                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, msg,
                        "Nâng cấp", "Hủy bỏ");

                player.combineNew.typeCombine = MO_CS_VY_THU;
                break;
            }

            case PHA_LE_HOA_TRANG_BI_X100:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item item = player.combineNew.itemsCombine.get(0);
                    if (isTrangBiPhaLeHoa(item)) {
                        int star = 0;
                        for (ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 107) {
                                star = io.param;
                                break;
                            }
                        }
                        if (star < MAX_STAR_ITEM) {
                            player.combineNew.goldCombine = getGoldPhaLeHoa(star);
                            player.combineNew.gemCombine = getGemPhaLeHoa(star);
                            player.combineNew.ratioCombine = Manager.TILE_NCAP == 0 ? getRatioPhaLeHoa(star) : Manager.TILE_NCAP;

                            String npcSay = item.template.name + "\n|2|";
                            for (ItemOption io : item.itemOptions) {
                                if (io.optionTemplate.id != 102) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                            }
                            // ----- TÍNH TỈ LỆ HIỂN THỊ ----- //
                            float baseRatio = player.combineNew.ratioCombine;     // Tỉ lệ gốc
                            float displayRatio = baseRatio * 2;                   // Luôn x2 cho UI

// Nếu dưới 1% thì hiển thị là 1%
                            if (displayRatio < 1f) {
                                displayRatio = 1f;
                            }

// Làm tròn đẹp
                            int finalDisplay = Math.round(displayRatio);

// ----- HIỂN THỊ TỈ LỆ LÊN MENU ----- //
                            npcSay += "|7|Tỉ lệ thành công: " + finalDisplay + "%\n";

                            if (player.combineNew.goldCombine <= player.inventory.gold) {
                                npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                                baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                        "Nâng cấp\n1 lần\n(" + (player.combineNew.gemCombine) + " ngọc" + ")",
                                        "Nâng cấp\n10 lần\n(" + (player.combineNew.gemCombine * 10) + " ngọc" + ")",
                                        "Nâng cấp\n100 lần\n(" + (player.combineNew.gemCombine * 100) + " ngọc" + ")",
                                        "Nâng cấp\n1000 lần\n(" + (player.combineNew.gemCombine * 1000) + " ngọc" + ")");
                            } else {
                                npcSay += "Còn thiếu "
                                        + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold)
                                        + " vàng";
                                baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Vật phẩm đã đạt tối đa sao pha lê", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể đục lỗ",
                                "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy hãy chọn 1 vật phẩm để pha lê hóa",
                            "Đóng");
                }
                break;

            case DUC_LO_TRANG_BI: {
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item trangBi = player.combineNew.itemsCombine.get(0);
                    if (CombineServiceNew.gI().isTrangBiKham(trangBi)) {
                        ItemOption opLo = null;
                        ItemOption opLoKham = null;
                        for (ItemOption op : trangBi.itemOptions) {
                            if (op.optionTemplate.id == 245) {
                                opLo = op; // số lỗ đã đục
                            }
                            if (op.optionTemplate.id == 246) {
                                opLoKham = op; // số lỗ đã khảm
                            }
                        }
                        int current = (opLo == null ? 0 : opLo.param);
                        if (current >= 7) {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Trang bị đã đạt tối đa 7 lỗ", "Đóng");
                            return;
                        }
                        // kiểm tra nguyên liệu id = 1559 (25 cái)
                        Item nguyenLieu = InventoryService.gI().findItemBagByTemp(player, 1559);
                        if (nguyenLieu == null || nguyenLieu.quantity < 25) {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Cần 25 tiền tệ để đục lỗ", "Đóng");
                            return;
                        }
                        if (player.inventory.ruby < 2000) {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Không đủ 2000 hồng ngọc", "Đóng");
                            return;
                        }

                        // Tính tỉ lệ thành công giảm dần theo số lỗ
                        // Lỗ 0->1: 50%, 1->2: 43%, 2->3: 36%, 3->4: 30%, 4->5: 23%, 5->6: 16%, 6->7: 10%
                        int[] tiLe = {50, 43, 36, 30, 23, 16, 10};
                        player.combineNew.ratioCombine = tiLe[current];

                        String npcSay = "Trang bị: " + trangBi.template.name + "\n"
                                + "Số lỗ hiện tại: " + current + "/7\n"
                                + "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n"
                                + "Chi phí: 2000 hồng ngọc\n"
                                + "Nguyên liệu: x25 tiền tệ";
                        this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                                npcSay, "Thực hiện", "Đóng");
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Vật phẩm này không thể đục lỗ", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Hãy đặt 1 trang bị vào để đục lỗ", "Đóng");
                }
                break;
            }

            case KHAM_DA_TRANG_BI: {
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item trangBi = player.combineNew.itemsCombine.get(0);
                    Item da = player.combineNew.itemsCombine.get(1);

                    if (da.template.id < 1566 || da.template.id > 1569) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Chỉ có thể dùng ngọc trai", "Đóng");
                        return;
                    }

                    if (CombineServiceNew.gI().isTrangBiKham(trangBi)) {
                        ItemOption opDuc = null, opKham = null;
                        for (ItemOption op : trangBi.itemOptions) {
                            if (op.optionTemplate.id == 245) {
                                opDuc = op;
                            }
                            if (op.optionTemplate.id == 246) {
                                opKham = op;
                            }
                        }
                        int soLoDuc = (opDuc != null) ? opDuc.param : 0;
                        int soLoKham = (opKham != null) ? opKham.param : 0;

                        if (soLoDuc <= 0) {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Trang bị chưa có lỗ để khảm", "Đóng");
                            return;
                        }
                        if (soLoKham >= soLoDuc) {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Đã khảm đủ số lỗ (" + soLoKham + "/" + soLoDuc + ")", "Đóng");
                            return;
                        }

                        Item nguyenLieu = InventoryService.gI().findItemBagByTemp(player, 1559);
                        if (nguyenLieu == null || nguyenLieu.quantity < 20) {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Cần 20 Tiền tệ", "Đóng");
                            return;
                        }

                        player.combineNew.goldCombine = 100;   // 100 ruby
                        player.combineNew.ratioCombine = 70;   // 70%

                        String npcSay = "Trang bị: " + trangBi.template.name + "\n"
                                + "Số lỗ đã khảm: " + soLoKham + "/" + soLoDuc + "\n"
                                + "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n"
                                + "Chi phí: " + player.combineNew.goldCombine + " hồng ngọc\n"
                                + "Nguyên liệu: x20 Tiền tệ";

                        if (player.inventory.ruby >= player.combineNew.goldCombine) {
                            this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                                    npcSay, "Thực hiện", "Đóng");
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Không đủ " + player.combineNew.goldCombine + " hồng ngọc", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Vật phẩm này không thể khảm", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Hãy đặt 1 trang bị và 1 viên đá vào để khảm", "Đóng");
                }
                break;
            }

            case PHA_LE_HOA_TRANG_BI:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item item = player.combineNew.itemsCombine.get(0);
                    if (isTrangBiPhaLeHoa(item)) {
                        int star = 0;
                        for (ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 107) {
                                star = io.param;
                                break;
                            }
                        }
                        if (star < MAX_STAR_ITEM) {
                            boolean isSpecialItem = (item.template.id >= 1401 && item.template.id <= 1405);

                            if (isSpecialItem) {
                                // 🔹 Item đặc biệt → dùng item thay thế
                                int requireItemId = 1559; // ID nguyên liệu thay thế
                                int requireQuantity = 10;
                                int haveQuantity = InventoryService.gI().getQuantity(player, requireItemId);

                                String npcSay = item.template.name + "\n|2|";
                                for (ItemOption io : item.itemOptions) {
                                    if (io.optionTemplate.id != 102) {
                                        npcSay += io.getOptionString() + "\n";
                                    }
                                }
                                // ----- TÍNH TỈ LỆ HIỂN THỊ ----- //
                                float baseRatio = player.combineNew.ratioCombine;     // Tỉ lệ gốc
                                float displayRatio = baseRatio * 2;                   // Luôn x2 cho UI

// Nếu dưới 1% thì hiển thị là 1%
                                if (displayRatio < 1f) {
                                    displayRatio = 1f;
                                }

// Làm tròn đẹp
                                int finalDisplay = Math.round(displayRatio);

// ----- HIỂN THỊ TỈ LỆ LÊN MENU ----- //
                                npcSay += "|7|Tỉ lệ thành công: " + finalDisplay + "%\n";

                                if (haveQuantity >= requireQuantity) {
                                    npcSay += "|1|Cần " + requireQuantity + " "
                                            + ItemService.gI().getTemplate(requireItemId).name;
                                    baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                            "Nâng cấp\n1 lần",
                                            "Nâng cấp\n10 lần",
                                            "Nâng cấp\n100 lần");
                                } else {
                                    npcSay += "Còn thiếu " + (requireQuantity - haveQuantity) + " "
                                            + ItemService.gI().getTemplate(requireItemId).name;
                                    baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                                }

                            } else {
                                // 🔹 Item thường → vàng + ngọc
                                player.combineNew.goldCombine = getGoldPhaLeHoa(star);
                                player.combineNew.gemCombine = getGemPhaLeHoa(star);
                                player.combineNew.ratioCombine = Manager.TILE_NCAP == 0 ? getRatioPhaLeHoa(star) : Manager.TILE_NCAP;

                                String npcSay = item.template.name + "\n|2|";
                                for (ItemOption io : item.itemOptions) {
                                    if (io.optionTemplate.id != 102) {
                                        npcSay += io.getOptionString() + "\n";
                                    }
                                }
                                // ----- TÍNH TỈ LỆ HIỂN THỊ ----- //
                                float baseRatio = player.combineNew.ratioCombine;     // Tỉ lệ gốc
                                float displayRatio = baseRatio * 2;                   // Luôn x2 cho UI

// Nếu dưới 1% thì hiển thị là 1%
                                if (displayRatio < 1f) {
                                    displayRatio = 1f;
                                }

// Làm tròn đẹp
                                int finalDisplay = Math.round(displayRatio);

// ----- HIỂN THỊ TỈ LỆ LÊN MENU ----- //
                                npcSay += "|7|Tỉ lệ thành công: " + finalDisplay + "%\n";
                                if (player.combineNew.goldCombine <= player.inventory.gold) {
                                    npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                                    baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                            "Nâng cấp\n1 lần\n(" + (player.combineNew.gemCombine) + " ngọc)",
                                            "Nâng cấp\n10 lần\n(" + (player.combineNew.gemCombine * 10) + " ngọc)",
                                            "Nâng cấp\n100 lần\n(" + (player.combineNew.gemCombine * 100) + " ngọc)");
                                } else {
                                    npcSay += "Còn thiếu "
                                            + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                                    baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                                }
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm đã đạt tối đa sao pha lê", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể đục lỗ", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 vật phẩm để pha lê hóa", "Đóng");
                }
                break;
            case NHAP_NGOC_RONG:
                if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 1) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        if (item != null) {
                            int soluong = 7;
                            if (item.isNotNullItem() && (item.template.id > 14 && item.template.id <= 20) && item.quantity >= soluong) {
                                String npcSay = "|2|Con có muốn biến " + soluong + " " + item.template.name + " thành\n"
                                        + "1 viên " + ItemService.gI().getTemplate((short) (item.template.id - 1)).name + "\n"
                                        + "|7|Cần " + soluong + " " + item.template.name;
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Không có ép lên được nữa !!!", "Đóng");
                            }
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 7 viên ngọc rồng cùng sao trở lên", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
                break;

            case NANG_CAP_NRO:
                if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine != null && player.combineNew.itemsCombine.size() == 7) {
                        boolean canCombine = true;
                        String errorMsg = "";

                        // Kiểm tra có đủ 7 loại ngọc từ 1-7 sao không
                        boolean[] hasNgoc = new boolean[7]; // index 0-6 tương ứng ngọc 1-7 sao

                        for (Item item : player.combineNew.itemsCombine) {
                            if (item == null || !item.isNotNullItem()) {
                                canCombine = false;
                                errorMsg = "Vật phẩm không hợp lệ";
                                break;
                            }

                            // Kiểm tra id trong range 14-20 (ngọc 1-7 sao)
                            if (item.template.id < 14 || item.template.id > 20) {
                                canCombine = false;
                                errorMsg = "Chỉ chấp nhận ngọc rồng từ 1 đến 7 sao";
                                break;
                            }

                            // Kiểm tra số lượng mỗi item >= 99
                            if (item.quantity < 99) {
                                canCombine = false;
                                errorMsg = "Mỗi loại ngọc cần tối thiểu 99 viên\n" + item.template.name + " chỉ có " + item.quantity + " viên";
                                break;
                            }

                            // Đánh dấu loại ngọc này đã có
                            int ngocIndex = item.template.id - 14; // id 14 = ngọc 1 sao (index 0)
                            if (hasNgoc[ngocIndex]) {
                                canCombine = false;
                                errorMsg = "Không được đặt 2 loại ngọc giống nhau";
                                break;
                            }
                            hasNgoc[ngocIndex] = true;
                        }

                        // Kiểm tra có đủ 7 loại ngọc khác nhau không
                        if (canCombine) {
                            for (int i = 0; i < 7; i++) {
                                if (!hasNgoc[i]) {
                                    canCombine = false;
                                    errorMsg = "Thiếu ngọc " + (i + 1) + " sao";
                                    break;
                                }
                            }
                        }

                        if (canCombine) {
                            // Tạo chuỗi hiển thị items
                            StringBuilder itemsInfo = new StringBuilder();
                            for (Item item : player.combineNew.itemsCombine) {
                                itemsInfo.append("99 ").append(item.template.name).append("\n");
                            }

                            String npcSay = "|2|Con có muốn biến\n"
                                    + itemsInfo.toString()
                                    + "thành 1 viên Ngọc Rồng Hoàn Hảo?\n"
                                    + "|7|Cần 99 mỗi loại ngọc rồng từ 1 sao đến 7 sao";
                            this.npcwhists.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                                    npcSay, "Làm phép", "Từ chối");
                        } else {
                            this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    errorMsg, "Đóng");
                        }
                    } else {
                        String msg = "Cần đúng 7 loại ngọc rồng từ 1 sao đến 7 sao\n";
                        if (player.combineNew.itemsCombine != null) {
                            msg += "Hiện tại chỉ có " + player.combineNew.itemsCombine.size() + " loại";
                        }
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, msg, "Đóng");
                    }
                } else {
                    this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
                break;
            case AN_TRANG_BI:
                if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 2) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        Item dangusac = player.combineNew.itemsCombine.get(1);
                        if (isTrangBiAn(item)) {
                            if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && (dangusac.template.id == 1232 || dangusac.template.id == 1233 || dangusac.template.id == 1234) && dangusac.quantity >= 99) {
                                String npcSay = item.template.name + "\n|2|";
                                for (ItemOption io : item.itemOptions) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                                npcSay += "|1|Con có muốn biến trang bị " + item.template.name + " thành\n"
                                        + "trang bị Ấn không?\b|4|Đục là lên\n"
                                        + "|7|Cần 99 " + dangusac.template.name;
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Bạn chưa bỏ đủ vật phẩm !!!", "Đóng");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể hóa ấn", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần bỏ đủ vật phẩm yêu cầu", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
                break;
            case NANG_CAP_VAT_PHAM:
                if (player.combineNew.itemsCombine.size() >= 2 && player.combineNew.itemsCombine.size() < 4) {
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type < 5).count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ nâng cấp", "Đóng");
                        break;
                    }
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type == 14).count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đá nâng cấp", "Đóng");
                        break;
                    }
                    if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 987).count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ nâng cấp", "Đóng");
                        break;
                    }
                    Item itemDo = null;
                    Item itemDNC = null;
                    Item itemDBV = null;
                    for (int j = 0; j < player.combineNew.itemsCombine.size(); j++) {
                        if (player.combineNew.itemsCombine.get(j).isNotNullItem()) {
                            if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.get(j).template.id == 987) {
                                itemDBV = player.combineNew.itemsCombine.get(j);
                                continue;
                            }
                            if (player.combineNew.itemsCombine.get(j).template.type < 5) {
                                itemDo = player.combineNew.itemsCombine.get(j);
                            } else {
                                itemDNC = player.combineNew.itemsCombine.get(j);
                            }
                        }
                    }
                    if (isCoupleItemNangCapCheck(itemDo, itemDNC)) {
                        int level = 0;
                        for (ItemOption io : itemDo.itemOptions) {
                            if (io.optionTemplate.id == 72) {
                                level = io.param;
                                break;
                            }
                        }
                        if (level < MAX_LEVEL_ITEM) {
                            player.combineNew.goldCombine = getGoldNangCapDo(level);
                            player.combineNew.ratioCombine = Manager.TILE_NCAP == 0 ? (float) getTileNangCapDo(level) : Manager.TILE_NCAP;
                            player.combineNew.countDaNangCap = getCountDaNangCapDo(level);
                            player.combineNew.countDaBaoVe = (short) getCountDaBaoVe(level);
                            String npcSay = "|2|Hiện tại " + itemDo.template.name + " (+" + level + ")\n|0|";
                            for (ItemOption io : itemDo.itemOptions) {
                                if (io.optionTemplate.id != 72) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                            }
                            String option = null;
                            int param = 0;
                            for (ItemOption io : itemDo.itemOptions) {
                                if (io.optionTemplate.id == 47
                                        || io.optionTemplate.id == 6
                                        || io.optionTemplate.id == 0
                                        || io.optionTemplate.id == 7
                                        || io.optionTemplate.id == 14
                                        || io.optionTemplate.id == 22
                                        || io.optionTemplate.id == 23) {
                                    option = io.optionTemplate.name;
                                    param = io.param + (io.param * 10 / 100);
                                    break;
                                }
                            }
                            npcSay += "|2|Sau khi nâng cấp (+" + (level + 1) + ")\n|7|"
                                    + option.replaceAll("#", String.valueOf(param))
                                    + "\n|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n"
                                    + (player.combineNew.countDaNangCap > itemDNC.quantity ? "|7|" : "|1|")
                                    + "Cần " + player.combineNew.countDaNangCap + " " + itemDNC.template.name
                                    + "\n" + (player.combineNew.goldCombine > player.inventory.gold ? "|7|" : "|1|")
                                    + "Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";

                            String daNPC = player.combineNew.itemsCombine.size() == 3 && itemDBV != null ? String.format("\nCần tốn %s đá bảo vệ", player.combineNew.countDaBaoVe) : "";
                            if ((level == 2 || level == 4 || level == 6) && !(player.combineNew.itemsCombine.size() == 3 && itemDBV != null)) {
                                npcSay += "\nNếu thất bại sẽ rớt xuống (+" + (level - 1) + ")";
                                npcSay += "\nVà giảm 5% chỉ số gốc";
                            }
                            if (player.combineNew.countDaNangCap > itemDNC.quantity) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay, "Còn thiếu\n" + (player.combineNew.countDaNangCap - itemDNC.quantity) + " " + itemDNC.template.name);
                            } else if (player.combineNew.goldCombine > player.inventory.gold) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay, "Còn thiếu\n" + Util.numberToMoney((player.combineNew.goldCombine - player.inventory.gold)) + " vàng");
                            } else if (player.combineNew.itemsCombine.size() == 3 && Objects.nonNull(itemDBV) && itemDBV.quantity < player.combineNew.countDaBaoVe) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay, "Còn thiếu\n" + (player.combineNew.countDaBaoVe - itemDBV.quantity) + " đá bảo vệ");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                                        npcSay, "Nâng cấp\n" + Util.numberToMoney(player.combineNew.goldCombine) + " vàng" + daNPC, "Từ chối");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Trang bị của ngươi đã đạt cấp tối đa", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 trang bị và 1 loại đá nâng cấp", "Đóng");
                    }
                } else {
                    if (player.combineNew.itemsCombine.size() > 3) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cất đi con ta không thèm", "Đóng");
                        break;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 trang bị và 1 loại đá nâng cấp", "Đóng");
                }
                break;
            case NANG_CAP_CHAN_MENH:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item bongTai = null;
                    Item manhVo = null;
                    int star = 0;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item.template.id == 1318) {
                            manhVo = item;
                        } else if (item.template.id >= 1300 && item.template.id <= 1308) {
                            bongTai = item;
                            star = item.template.id - 1300;
                        }
                    }
                    if (bongTai != null && bongTai.template.id == 1308) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Chân Mệnh đã đạt cấp tối đa", "Đóng");
                        return;
                    }
                    player.combineNew.DiemNangcap = getDiemNangcapChanmenh(star);
                    player.combineNew.DaNangcap = getDaNangcapChanmenh(star);
                    player.combineNew.TileNangcap = Manager.TILE_NCAP == 0 ? getTiLeNangcapChanmenh(star) : Manager.TILE_NCAP;
                    if (bongTai != null && manhVo != null && (bongTai.template.id >= 1300 && bongTai.template.id < 1308)) {
                        String npcSay = bongTai.template.name + "\n|2|";
                        for (ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.TileNangcap + "%" + "\n";
                        if (player.combineNew.DiemNangcap <= player.inventory.ruby) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.DiemNangcap) + " Hồng ngọc";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.DaNangcap + " Đá Hoàng Kim");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.DiemNangcap - player.inventory.ruby) + " Hồng ngọc";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Chân Mệnh và Đá Hoàng Kim", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Chân Mệnh và Đá Hoàng Kim", "Đóng");
                }
                break;
            case NANG_CAP_BONG_TAI:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item bongTai = null;
                    Item bongTai5 = null;
                    Item manhVo = null;
                    Item manhVo5 = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        switch (item.template.id) {
                            case 454:
                                bongTai = item;
                                break;
                            case 933:
                                manhVo = item;
                                break;
                            case 1549:
                                manhVo5 = item;
                                break;
                            case 921:
                                bongTai = item;
                                break;
                            case 1165:
                                bongTai = item;
                                break;
                            case 1129:
                                bongTai5 = item;
                                break;
                            default:
                                break;
                        }
                    }
                    if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 454) {

                        player.combineNew.goldCombine = 500000000;
                        player.combineNew.gemCombine = 1000;
                        player.combineNew.ratioCombine = 50;

                        String npcSay = "Bông tai Porata cấp 2" + "\n|2|";
                        for (ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " Hồng ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 921) {

                        player.combineNew.goldCombine = 1000000000;
                        player.combineNew.gemCombine = 5000;
                        player.combineNew.ratioCombine = 20;

                        String npcSay = "Bông tai Porata cấp 3" + "\n|2|";
                        for (ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " Hồng ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 1165) {

                        player.combineNew.goldCombine = 1000000000;
                        player.combineNew.gemCombine = 15000;
                        player.combineNew.ratioCombine = 10;

                        String npcSay = "Bông tai Porata cấp 4" + "\n|2|";
                        for (ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " Hồng ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else if (bongTai5 != null && manhVo5 != null && manhVo5.quantity >= 20000 && bongTai5.template.id == 1129) {

                        player.combineNew.goldCombine = 2000000000;
                        player.combineNew.gemCombine = 50000;
                        player.combineNew.ratioCombine = 10;

                        String npcSay = "Bông tai Porata cấp 5" + "\n|2|";
                        for (ItemOption io : bongTai5.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " Hồng ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Bông tai Porata cấp 1, 2, 3, 4 và X9999 Mảnh vỡ bông tai (Riêng BTC5 cần 20.000 Mảnh BTC5)", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Bông tai Porata cấp 1, 2, 3, 4 và X9999 Mảnh vỡ bông tai (Riêng BTC5 cần 20.000 Mảnh BTC5)", "Đóng");
                }
                break;
            case MO_CHI_SO_BONG_TAI:
                if (player.combineNew.itemsCombine.size() == 3) {
                    Item bongTai = null;
                    Item manhHon = null;
                    Item daXanhLam = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        switch (item.template.id) {
                            case 1550:
                                bongTai = item;
                                break;
                            case 1129:
                                bongTai = item;
                                break;
                            case 1165:
                                bongTai = item;
                                break;
                            case 921:
                                bongTai = item;
                                break;
                            case 934:
                                manhHon = item;
                                break;
                            case 935:
                                daXanhLam = item;
                                break;
                            default:
                                break;
                        }
                    }
                    if (bongTai != null && manhHon != null && daXanhLam != null && manhHon.quantity >= 99) {

                        player.combineNew.goldCombine = 2000000000;
                        player.combineNew.gemCombine = 1000;

                        String npcSay;
                        switch (bongTai.template.id) {
                            case 1550:
                                npcSay = "Bông tai Porata cấp 5" + "\n|2|";
                                player.combineNew.ratioCombine = 40;
                                break;
                            case 1129:
                                npcSay = "Bông tai Porata cấp 4" + "\n|2|";
                                player.combineNew.ratioCombine = 70;
                                break;
                            case 1165:
                                npcSay = "Bông tai Porata cấp 3" + "\n|2|";
                                player.combineNew.ratioCombine = 70;
                                break;
                            default:
                                npcSay = "Bông tai Porata cấp 2" + "\n|2|";
                                player.combineNew.ratioCombine = 70;
                                break;
                        }
                        for (ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " Hồng ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Bông tai Porata, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Bông tai Porata, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
                }
                break;
            case CHUYEN_HOA_DO_HUY_DIET:
                if (player.combineNew.itemsCombine.size() == 0) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Con hãy đưa ta đồ Hủy diệt", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 1) {
                    int huydietok = 0;
                    Item item = player.combineNew.itemsCombine.get(0);
                    if (item.isNotNullItem()) {
                        if (item.template.id >= 650 && item.template.id <= 662) {
                            huydietok = 1;
                        }
                    }
                    if (huydietok == 0) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ta chỉ có thể chuyển hóa đồ Hủy diệt thôi", "Đóng");
                        return;
                    }
                    String npcSay = "|2|Sau khi chuyển hóa vật phẩm\n|7|"
                            + "Bạn sẽ nhận được : 1 " + " Phiếu Hủy diệt Tương ứng\n"
                            + (500000000 > player.inventory.gold ? "|7|" : "|1|")
                            + "Cần " + Util.numberToMoney(500000000) + " vàng";

                    if (player.inventory.gold < 500000000) {
                        this.baHatMit.npcChat(player, "Hết tiền rồi\nẢo ít thôi con");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.MENU_CHUYEN_HOA_DO_HUY_DIET,
                            npcSay, "Nâng cấp\n" + Util.numberToMoney(500000000) + " vàng", "Từ chối");
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ta chỉ có thể chuyển hóa 1 lần 1 món đồ Hủy diệt", "Đóng");
                }
                break;
            case PHAN_RA_DO_TS:
                if (player.combineNew.itemsCombine.size() == 0) {
                    this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Con hãy đưa ta đồ Thiên sứ", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 1) {
                    int dothiensu = 0;
                    Item item = player.combineNew.itemsCombine.get(0);

                    if (!item.isDTS()) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ Thiên sứ", "Đóng");
                        return;
                    }
                    if (item.isNotNullItem()) {
                        if (item.isDTS()) {
                            dothiensu = 1;
                        }
                    }
                    if (dothiensu == 0) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ta chỉ có thể chuyển hóa đồ Thiên sứ thôi", "Đóng");
                        return;
                    }
                    String npcSay = "|2|Sau khi chuyển hóa vật phẩm\n|7|"
                            + "Bạn sẽ nhận được : 500 " + " Mảnh thiên sứ Tương ứng\n"
                            + (500000000 > player.inventory.gold ? "|7|" : "|1|")
                            + "Cần " + Util.numberToMoney(500000000) + " vàng";

                    if (player.inventory.gold < 500000000) {
                        this.npcwhists.npcChat(player, "Hết tiền rồi\nẢo ít thôi con");
                        return;
                    }
                    this.npcwhists.createOtherMenu(player, ConstNpc.MENU_PHAN_RA_TS,
                            npcSay, "Nâng cấp\n" + Util.numberToMoney(500000000) + " vàng", "Từ chối");
                } else {
                    this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ta chỉ có thể chuyển hóa 1 lần 1 món đồ Hủy diệt", "Đóng");
                }
                break;
            case NANG_CAP_DO_TS:
                if (player.combineNew.itemsCombine.size() == 0) {
                    this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy đưa ta 2 món Hủy Diệt bất kì và 1 món Thần Linh cùng loại", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isCongThuc()).count() < 1) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu mảnh Công thức", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1083).count() < 1) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đá cầu vòng", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999).count() < 1) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu mảnh thiên sứ", "Đóng");
                        return;
                    }

                    String npcSay = "|2|Con có muốn đổi các món nguyên liệu ?\n|7|"
                            + "Và nhận được " + player.combineNew.itemsCombine.stream().filter(Item::isManhTS).findFirst().get().typeNameManh() + " thiên sứ tương ứng\n"
                            + "|1|Cần " + Util.numberToMoney(COST) + " vàng";

                    if (player.inventory.gold < COST) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hết tiền rồi\nẢo ít thôi con", "Đóng");
                        return;
                    }
                    this.npcwhists.createOtherMenu(player, ConstNpc.MENU_NANG_CAP_DO_TS,
                            npcSay, "Nâng cấp\n" + Util.numberToMoney(COST) + " vàng", "Từ chối");
                } else {
                    if (player.combineNew.itemsCombine.size() > 3) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cất đi con ta không thèm", "Đóng");
                        return;
                    }
                    this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Còn thiếu nguyên liệu để nâng cấp hãy quay lại sau", "Đóng");
                }
                break;
            case Nang_Cap_SKH:
                if (player.combineNew.itemsCombine.size() == 0) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy đưa ta 3 món Hủy diệt", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDHD()).count() < 3) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ hủy diệt", "Đóng");
                        return;
                    }
                    Item thoivang = null;
                    try {
                        thoivang = InventoryService.gI().findItemBagByTemp(player, 457);
                    } catch (Exception e) {
                    }
                    String npcSay = "|2|Con có muốn đổi các món nguyên liệu ?\n|7|"
                            + "Và nhận được\n|0|"
                            + player.combineNew.itemsCombine.stream().filter(Item::isDHD).findFirst().get().typeName() + " kích hoạt VIP tương ứng\n"
                            + ((thoivang == null || thoivang.quantity < 30) ? "|7|" : "|1|")
                            + "Cần 30 Thỏi vàng";

                    if (player.inventory.gem < 1000) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hết tiền rồi\nẢo ít thôi con", "Đóng");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.MENU_NANG_DOI_SKH_VIP,
                            npcSay, "Nâng cấp\n" + 1000 + " ngọc", "Từ chối");
                } else {
                    if (player.combineNew.itemsCombine.size() > 3) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Nguyên liệu không phù hợp", "Đóng");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Còn thiếu nguyên liệu để nâng cấp hãy quay lại sau", "Đóng");
                }
                break;
            case NANG_CAP_SKH_TS: {
                try {
                    // Kiểm tra có item trong combine
                    if (player.combineNew.itemsCombine == null || player.combineNew.itemsCombine.isEmpty()) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "|2|Hướng dẫn nâng cấp SKH Thiên Sứ:\n\n"
                                + "|7|Hãy đặt đủ 7 món vào ô luyện:\n"
                                + "- 1 món đích Thần Linh (555-567) chưa có SKH\n"
                                + "- 6 món nguồn cùng loại, có SKH giống nhau\n\n"
                                + "|1|Nguyên liệu cần:\n"
                                + "- 2000 Hồng Ngọc\n"
                                + "- 20 Tiền tệ (ID: 1559)\n"
                                + "- 99 Đá nâng cấp (ID: 1554)\n"
                                + "- 99 Thức ăn (1 trong 5 loại: 663-667)\n\n"
                                + "|3|Tỉ lệ thành công: 36%\n"
                                + "|0|Thất bại sẽ mất tất cả trang bị!",
                                "Đóng");
                        return;
                    }

                    int itemCount = player.combineNew.itemsCombine.size();

                    // Kiểm tra số lượng chính xác
                    if (itemCount != 7) {
                        String message = itemCount < 7
                                ? "Thiếu món! Cần đúng 7 món (1 đích + 6 nguồn)"
                                : "Thừa món! Chỉ được đặt tối đa 7 món";
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, message, "Đóng");
                        return;
                    }

                    // Phân tích sơ bộ để hiển thị thông tin
                    int targetCount = 0;
                    int sourceCount = 0;

                    for (Item it : player.combineNew.itemsCombine) {
                        if (it == null || it.template == null) {
                            this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Có item không hợp lệ trong danh sách!", "Đóng");
                            return;
                        }

                        int id = it.template.id;
                        if (id >= 555 && id <= 567) {
                            targetCount++;
                        } else {
                            sourceCount++;
                        }
                    }

                    // Kiểm tra cấu trúc cơ bản
                    if (targetCount == 0) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Thiếu món đích Thần Linh (ID: 555-567)!", "Đóng");
                        return;
                    }

                    if (targetCount > 1) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Chỉ được có 1 trang bị Thần Linh làm đích!", "Đóng");
                        return;
                    }

                    if (sourceCount != 6) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần đúng 6 món nguồn (hiện có " + sourceCount + " món)!", "Đóng");
                        return;
                    }

                    // Kiểm tra nguyên liệu trước khi confirm
                    final int RUBY_COST = 2000;
                    final int TOKEN_ID = 1559, TOKEN_NEED = 20;
                    final int STONE_ID = 1554, STONE_NEED = 99;
                    final int[] FOOD_IDS = {663, 664, 665, 666, 667};
                    final int FOOD_NEED = 99;

                    // Kiểm tra Hồng Ngọc
                    if (player.inventory.ruby < RUBY_COST) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Không đủ " + RUBY_COST + " Hồng Ngọc!\n"
                                + "Hiện có: " + player.inventory.ruby, "Đóng");
                        return;
                    }

                    // Đếm nguyên liệu
                    int tokenTotal = 0, stoneTotal = 0;
                    java.util.Map<Integer, Integer> foodTotals = new java.util.HashMap<>();

                    for (Item it : player.inventory.itemsBag) {
                        if (it == null || it.template == null) {
                            continue;
                        }

                        if (it.template.id == TOKEN_ID) {
                            tokenTotal += it.quantity;
                        }
                        if (it.template.id == STONE_ID) {
                            stoneTotal += it.quantity;
                        }
                        for (int foodId : FOOD_IDS) {
                            if (it.template.id == foodId) {
                                foodTotals.put(foodId, foodTotals.getOrDefault(foodId, 0) + it.quantity);
                            }
                        }
                    }

                    // Kiểm tra thiếu nguyên liệu
                    java.util.List<String> missing = new java.util.ArrayList<>();
                    if (tokenTotal < TOKEN_NEED) {
                        missing.add("Thiếu " + (TOKEN_NEED - tokenTotal) + " Tiền tệ");
                    }
                    if (stoneTotal < STONE_NEED) {
                        missing.add("Thiếu " + (STONE_NEED - stoneTotal) + " Đá nâng cấp");
                    }

                    boolean hasFoodEnough = false;
                    for (int foodId : FOOD_IDS) {
                        if (foodTotals.getOrDefault(foodId, 0) >= FOOD_NEED) {
                            hasFoodEnough = true;
                            break;
                        }
                    }
                    if (!hasFoodEnough) {
                        int maxFood = 0;
                        for (int qty : foodTotals.values()) {
                            maxFood = Math.max(maxFood, qty);
                        }
                        missing.add("Thiếu " + (FOOD_NEED - maxFood) + " Thức ăn");
                    }

                    if (!missing.isEmpty()) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "|1|Không đủ nguyên liệu:\n\n" + String.join("\n", missing), "Đóng");
                        return;
                    }

                    // Tất cả điều kiện OK → Hiển thị confirm
                    String npcSay = "|2|Con có chắc muốn nâng cấp SKH Thiên Sứ?\n\n"
                            + "|7|Trang bị:\n"
                            + "- 1 món đích Thần Linh (chưa có SKH)\n"
                            + "- 6 món nguồn cùng loại & cùng SKH\n\n"
                            + "|1|Nguyên liệu tiêu hao:\n"
                            + "- 2000 Hồng Ngọc\n"
                            + "- 20 Tiền tệ\n"
                            + "- 99 Đá nâng cấp\n"
                            + "- 99 Thức ăn\n\n"
                            + "|3|Tỉ lệ thành công: 36%\n"
                            + "|0|CẢNH BÁO: Thất bại sẽ mất tất cả trang bị!";

                    this.npcwhists.createOtherMenu(player, ConstNpc.MENU_NANG_DO_SKH_TS,
                            npcSay, "Đồng ý", "Hủy bỏ");

                } catch (Exception e) {
                    e.printStackTrace();
                    Service.getInstance().sendThongBao(player, "Có lỗi khi xử lý nâng cấp SKH!");
                }
                break;
            }

            case NANG_CAP_THAN_LINH:
                if (player.combineNew.itemsCombine.isEmpty()) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy đưa ta 1 món Thần linh", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 1) {
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDTL()).count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ Thần linh", "Đóng");
                        return;
                    }
                    Item doThanLinh = player.combineNew.itemsCombine.get(0);
                    String npcSay = "|2|Con có muốn nâng cấp " + doThanLinh.template.name + " Thành" + "\n|7|"
                            + doThanLinh.typeName() + " Hủy diệt " + Service.getInstance().get_HanhTinh(doThanLinh.template.gender) + "\n|0|"
                            + doThanLinh.typeOption() + "+?\n"
                            + "Yêu cầu sức mạnh 80 tỉ\n"
                            + "Không thể giao dịch\n"
                            + ((player.inventory.ruby < 10000) ? "|7|" : "|1|")
                            + "Cần 2Tỷ vàng";

                    if (player.inventory.gold < 2_000_000_000) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hết tiền rồi\nẢo ít thôi con", "Đóng");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.MENU_NANG_CAP_THAN_LINH,
                            npcSay, "Nâng cấp\n2Tỷ vàng", "Từ chối");
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Còn thiếu nguyên liệu để nâng cấp hãy quay lại sau", "Đóng");
                }
                break;
            case GIA_HAN_VAT_PHAM:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item thegh = null;
                    Item itemGiahan = null;
                    for (Item item_ : player.combineNew.itemsCombine) {
                        if (item_.template.id == 1346) {
                            thegh = item_;
                        } else if (item_.isTrangBiHSD()) {
                            itemGiahan = item_;
                        }
                    }
                    if (thegh == null) {
                        Service.getInstance().sendThongBaoOK(player, "Cần 1 trang bị có hạn sử dụng và 1 phiếu Gia hạn");
                        return;
                    }
                    if (itemGiahan == null) {
                        Service.getInstance().sendThongBaoOK(player, "Cần 1 trang bị có hạn sử dụng và 1 phiếu Gia hạn");
                        return;
                    }
                    for (ItemOption itopt : itemGiahan.itemOptions) {
                        if (itopt.optionTemplate.id == 93 || itopt.optionTemplate.id == 63) {
                            if (itopt.param < 0 || itopt == null) {
                                Service.getInstance().sendThongBaoOK(player, "Trang bị này không phải trang bị có Hạn Sử Dụng");
                                return;
                            }
                        }
                    }
                    String npcSay = "Trang bị được gia hạn \"" + itemGiahan.template.name + "\"\n|1|";
                    npcSay += itemGiahan.template.name + "\n|2|";
                    for (ItemOption io : itemGiahan.itemOptions) {
                        npcSay += io.getOptionString() + "\n";
                    }
                    npcSay += "\n|0|Sau khi gia hạn +1 ngày\n";

                    npcSay += "|0|Tỉ lệ thành công: 100%" + "\n";
                    if (player.inventory.gold > 200000000) {
                        npcSay += "|2|Cần 200Tr vàng";
                        this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Nâng cấp", "Từ chối");

                    } else if (player.inventory.gold < 200000000) {
                        int SoVangThieu2 = (int) (200000000 - player.inventory.gold);
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Bạn còn thiếu " + SoVangThieu2 + " vàng");
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 1 trang bị có hạn sử dụng và 1 phiếu Gia hạn");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống");
                }
                break;

            case DE_TU_VIP:
                if (player.combineNew.itemsCombine.size() == 3) {
                    Item trungBu = null;
                    Item honBill = null;
                    Item xu = null;

                    for (Item item_ : player.combineNew.itemsCombine) {
                        if (item_.template.id == 568) {
                            trungBu = item_;
                        } else if (item_.template.id == 1108) {
                            honBill = item_;
                        } else if (item_.template.id == 1535) {
                            xu = item_;
                        }
                    }

                    // Kiểm tra từng nguyên liệu
                    if (trungBu == null || trungBu.quantity < 15) {
                        Service.getInstance().sendThongBaoOK(player, "Bạn cần có ít nhất 15 Trứng Bư");
                        return;
                    }
                    if (honBill == null || honBill.quantity < 5) {
                        Service.getInstance().sendThongBaoOK(player, "Bạn cần có ít nhất 5 Hồn Bill");
                        return;
                    }
                    if (xu == null || xu.quantity < 99) {
                        Service.getInstance().sendThongBaoOK(player, "Bạn cần có ít nhất 99 Xu");
                        return;
                    }

                    // Xây nội dung thông báo
                    String npcSay = "Nguyên liệu hợp lệ!\n";
                    npcSay += "|0|Sau khi ghép sẽ có cơ hội nhận được vật phẩm đặc biệt (ID 1999)\n";
                    npcSay += "|0|Cần:\n";
                    npcSay += "- 15 Trứng Bư\n";
                    npcSay += "- 5 Hồn Bill\n";
                    npcSay += "- 99 Xu\n";
                    npcSay += "- 20k Ruby\n";
                    npcSay += "|0|Tỉ lệ thành công: 30%\n";

                    if (player.inventory.ruby >= 20000) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Tiến hành", "Từ chối");
                    } else {
                        int soRubyThieu = (int) (20000 - player.inventory.ruby);
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Bạn còn thiếu " + soRubyThieu + " Ruby để thực hiện");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Bạn cần cho đủ 3 vật phẩm (Trứng Bư, Hồn Bill, Xu) vào để hợp thể");
                }
                break;

            case PHAP_SU_HOA: {

                if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                    break;
                }

                if (player.combineNew.itemsCombine.size() < 2
                        || player.combineNew.itemsCombine.size() > 3) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần trang bị, Đá Pháp Sư và (tùy chọn) Đá Bảo Vệ", "Đóng");
                    break;
                }

                Item trangBi = null;
                Item daPhapSu = null;
                Item daBaoVe = null;

                for (Item it : player.combineNew.itemsCombine) {
                    if (it == null || !it.isNotNullItem()) {
                        continue;
                    }
                    if (it.template.id == 1235) {
                        daPhapSu = it;
                    } else if (it.template.id == 987) {
                        daBaoVe = it;
                    } else {
                        trangBi = it;
                    }
                }

                if (trangBi == null || daPhapSu == null || !isTrangBiPhapsu(trangBi)) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Nguyên liệu không hợp lệ", "Đóng");
                    break;
                }

                // ===== cấp hiện tại =====
                int capHienTai = 0;
                for (ItemOption io : trangBi.itemOptions) {
                    if (io.optionTemplate.id == 233) {
                        capHienTai = io.param;
                        break;
                    }
                }

                if (capHienTai >= 7) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Pháp sư hóa đã đạt cấp cao nhất", "Đóng");
                    break;
                }

                int capTiepTheo = capHienTai + 1;
                int daCan = 3 * capTiepTheo;

                int successRate = 60 - capHienTai * 10;
                if (successRate < 10) {
                    successRate = 10;
                }

                boolean isRiskLevel
                        = capHienTai == 2 || capHienTai == 4 || capHienTai == 6;

                boolean hasBaoVe = daBaoVe != null && daBaoVe.quantity >= 10;

                // ===== xác định option chính + hiện tại =====
                int type = trangBi.template.type;
                int mainOptionId = -1;
                int currentMain = 0;

                for (ItemOption io : trangBi.itemOptions) {
                    int id = io.optionTemplate.id;
                    if (type == 2 && (id == 196 || id == 0)) {
                        mainOptionId = id;
                        currentMain = io.param;
                        break;
                    }
                    if (type == 1 && (id == 6 || id == 22)) {
                        mainOptionId = id;
                        currentMain = io.param;
                        break;
                    }
                    if (type == 3 && (id == 7 || id == 23)) {
                        mainOptionId = id;
                        currentMain = io.param;
                        break;
                    }
                    if (type == 0 && id == 47) {
                        mainOptionId = 47;
                        currentMain = io.param;
                        break;
                    }
                    if (type == 4 && id == 14) {
                        mainOptionId = 14;
                        currentMain = io.param;
                        break;
                    }
                }

                // ===== % tăng chính =====
                int percentMain = 12 + Math.max(0, capHienTai - 1) * 2;
                int addMain = (currentMain * percentMain) / 100;

                // ===== bonus phụ theo type =====
                int bonusId = -1;
                int bonusPer = 0;
                if (type == 0) {
                    bonusId = 94;
                    bonusPer = 2;
                } else if (type == 1) {
                    bonusId = 77;
                    bonusPer = 5;
                } else if (type == 2) {
                    bonusId = 50;
                    bonusPer = 3;
                } else if (type == 3) {
                    bonusId = 103;
                    bonusPer = 5;
                } else if (type == 4) {
                    bonusId = 5;
                    bonusPer = 3;
                }

                // ===== HIỂN THỊ =====
                StringBuilder sb = new StringBuilder();
                sb.append(trangBi.template.name).append("\n|2|");
                for (ItemOption io : trangBi.itemOptions) {
                    sb.append(io.getOptionString()).append("\n");
                }

                sb.append("|1|Pháp Sư Hóa: Cấp ")
                        .append(capHienTai).append(" → ").append(capTiepTheo).append("\n");

                sb.append("|1|Thành công:\n");
                sb.append("- ").append(trangBi.template.name)
                        .append(" +").append(addMain)
                        .append(" (").append(percentMain).append("%)\n");

                sb.append("- Bonus +").append(bonusPer)
                        .append("% (OTP ").append(bonusId).append(")\n");

                sb.append("|7|Tỉ lệ thành công: ")
                        .append(successRate).append("%\n");

                sb.append("|7|Cần ").append(daCan)
                        .append(" Đá Pháp Sư Hóa\n");

                if (isRiskLevel && !hasBaoVe) {
                    sb.append("|7|⚠ Ở cấp này có thể bị xịt giảm chỉ số.\n");
                    sb.append("|7|Có thể thêm 10 Đá Bảo Vệ để tránh xịt.\n");
                }

                this.baHatMit.createOtherMenu(
                        player,
                        ConstNpc.MENU_START_COMBINE,
                        sb.toString(),
                        "Làm phép",
                        "Từ chối"
                );

                break;
            }

            case TAY_PHAP_SU:
                if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 2) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        Item dangusac = player.combineNew.itemsCombine.get(1);
                        if (isTrangBiPhapsu(item)) {
                            if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && dangusac.template.id == 1236 && dangusac.quantity >= 1) {
                                String npcSay = item.template.name + "\n|2|";
                                for (ItemOption io : item.itemOptions) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                                npcSay += "|1|Con có muốn tẩy trang bị " + item.template.name + " về\n"
                                        + "lúc chưa Pháp sư hóa không?\n"
                                        + "|7|Cần 1 " + dangusac.template.name;
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Bạn chưa bỏ đủ vật phẩm !!!", "Đóng");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể thực hiện", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần bỏ đủ vật phẩm yêu cầu", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
                break;

            // START _ SÁCH TUYỆT KỸ //
            case GIAM_DINH_SACH:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item sachTuyetKy = null;
                    Item buaGiamDinh = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (issachTuyetKy(item)) {
                            sachTuyetKy = item;
                        } else if (item.template.id == 1508) {
                            buaGiamDinh = item;
                        }
                    }
                    if (sachTuyetKy != null && buaGiamDinh != null) {

                        String npcSay = "|1|" + sachTuyetKy.getName() + "\n";
                        npcSay += "|2|" + buaGiamDinh.getName() + " " + buaGiamDinh.quantity + "/1";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Giám định", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ và bùa giám định");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ và bùa giám định");
                    return;
                }
                break;
            case TAY_SACH:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item sachTuyetKy = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (issachTuyetKy(item)) {
                            sachTuyetKy = item;
                        }
                    }
                    if (sachTuyetKy != null) {
                        String npcSay = "|2|Tẩy Sách Tuyệt Kỹ";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Đồng ý", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ để tẩy");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ để tẩy");
                    return;
                }
                break;

            case NANG_CAP_SACH_TUYET_KY:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item sachTuyetKy = null;
                    Item kimBamGiay = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (issachTuyetKy(item) && (item.template.id == 1510 || item.template.id == 1512 || item.template.id == 1514)) {
                            sachTuyetKy = item;
                        } else if (item.template.id == 1507) {
                            kimBamGiay = item;
                        }
                    }
                    if (sachTuyetKy != null && kimBamGiay != null) {
                        String npcSay = "|2|Nâng cấp sách tuyệt kỹ\n";
                        npcSay += "Cần 10 Kìm bấm giấy\n"
                                + "Tỉ lệ thành công: 30%\n"
                                + "Nâng cấp thất bại sẽ mất 10 Kìm bấm giấy";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Nâng cấp", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ 1 và 10 Kìm bấm giấy.");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ 1 và 10 Kìm bấm giấy.");
                    return;
                }
                break;
            case PHUC_HOI_SACH:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item sachTuyetKy = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (issachTuyetKy(item)) {
                            sachTuyetKy = item;
                        }
                    }
                    if (sachTuyetKy != null) {
                        String npcSay = "|2|Phục hồi " + sachTuyetKy.getName() + "\n"
                                + "Cần 10 cuốn sách cũ\n"
                                + "Phí phục hồi 10 triệu vàng";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Đồng ý", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player, "Không tìm thấy vật phẩm");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Không tìm thấy vật phẩm");
                    return;
                }
                break;
            case PHAN_RA_SACH:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item sachTuyetKy = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (issachTuyetKy(item)) {
                            sachTuyetKy = item;
                        }
                    }
                    if (sachTuyetKy != null) {
                        String npcSay = "|2|Phân rã sách\n"
                                + "Nhận lại 5 cuốn sách cũ\n"
                                + "Phí rã 10 triệu vàng";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Đồng ý", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player, "Không tìm thấy vật phẩm");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Không tìm thấy vật phẩm");
                    return;
                }
                break;

            // END _ SÁCH TUYỆT KỸ //
        }
    }

    /**
     * Bắt đầu đập đồ - điều hướng từng loại đập đồ
     *
     * @param player
     */
    public void startCombine(Player player) {
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI:
                epSaoTrangBi(player);
                break;
            case PHA_LE_HOA_TRANG_BI:
                phaLeHoaTrangBi(player);
                break;
            case DUC_LO_TRANG_BI:
                ducLoTrangBi(player);
                break;
            case MO_CS_VY_THU:
                nangCapVyThu(player);
                break;
            case KHAM_DA_TRANG_BI:
                khamDaTrangBi(player);
                break;
            case NANG_CAP_SKH:
                NangCapSKH(player);
                break;
            case PHA_LE_HOA_TRANG_BI_X100:
                phaLeHoaTrangBix100(player);
                break;
            case CHUYEN_HOA_TRANG_BI:

                break;
            case NHAP_NGOC_RONG:
                nhapNgocRong(player);
                break;
            case NANG_CAP_NRO:
                nangcapnro(player);
                break;
            case AN_TRANG_BI:
                antrangbi(player);
                break;
            case CHUYEN_HOA_DO_HUY_DIET:
                chuyenhoahuydiet(player);
                break;
            case PHAN_RA_DO_TS:
                PhanRaDoTS(player);
                break;
            case NANG_CAP_DO_TS:
                openDTS(player);
                break;
            case Nang_Cap_SKH:
                NangCapSKH(player);
                break;
            case MO_KHOA_GD:
                mơkhoagd(player);
                break;
            case NANG_CAP_SKH_TS:
                openSKHts(player);
                break;
            case NANG_CAP_THAN_LINH:
                NcapDoThanLinh(player);
                break;
            case NANG_CAP_VAT_PHAM:
                nangCapVatPham(player);
                break;
            case NANG_CAP_BONG_TAI:
                nangCapBongTai(player);
                break;
            case MO_CHI_SO_BONG_TAI:
                moChiSoBongTai2345(player);
            case PHAP_SU_HOA:
                phapsuhoa(player);
                break;
            case TAY_PHAP_SU:
                tayphapsu(player);
                break;
            case NANG_CAP_CHAN_MENH:
                nangCapChanMenh(player);
                break;
            case GIA_HAN_VAT_PHAM:
                GiaHanTrangBi(player);
                break;
            case DAP_THE_CAU_THU:
                dapTheCauThu(player);
                break;
            case MO_CS_THE:
                moChiSoThe(player);
                break;

            case DE_TU_VIP:
                detuvip(player);
                break;
//            case OPTION_PORATA:
//                nangCapVatPham(player);
//                break;   
            // START _ SÁCH TUYỆT KỸ //
            case GIAM_DINH_SACH:
                giamDinhSach(player);
                break;
            case TAY_SACH:
                taySach(player);
                break;
            case NANG_CAP_SACH_TUYET_KY:
                nangCapSachTuyetKy(player);
                break;
            case PHUC_HOI_SACH:
                phucHoiSach(player);
                break;
            case PHAN_RA_SACH:
                phanRaSach(player);
                break;
            // END _ SÁCH TUYỆT KỸ //
        }

        player.iDMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        player.combineNew.clearParamCombine();
        player.combineNew.lastTimeCombine = System.currentTimeMillis();

    }

    public void GetTrangBiKichHoathuydiet(Player player, int id) {
        Item item = ItemService.gI().createNewItem((short) id);
        int[][] optionNormal = {{127, 128}, {130, 132}, {133, 135}};
        int[][] paramNormal = {{139, 140}, {142, 144}, {136, 138}};
        int[][] optionVIP = {{129}, {131}, {134}};
        int[][] paramVIP = {{141}, {143}, {137}};
        int random = Util.nextInt(optionNormal.length);
        int randomSkh = Util.nextInt(100);
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(47, Util.nextInt(1500, 2000)));
        }
        if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(22, Util.nextInt(100, 150)));
        }
        if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(0, Util.nextInt(9000, 11000)));
        }
        if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(23, Util.nextInt(90, 150)));
        }
        if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(14, Util.nextInt(15, 20)));
        }
        if (randomSkh <= 20) {//tile ra do kich hoat
            if (randomSkh <= 5) { // tile ra option vip
                item.itemOptions.add(new ItemOption(optionVIP[player.gender][0], 0));
                item.itemOptions.add(new ItemOption(paramVIP[player.gender][0], 0));
                item.itemOptions.add(new ItemOption(30, 0));
            } else {// 
                item.itemOptions.add(new ItemOption(optionNormal[player.gender][random], 0));
                item.itemOptions.add(new ItemOption(paramNormal[player.gender][random], 0));
                item.itemOptions.add(new ItemOption(30, 0));
            }
        }

        InventoryService.gI().addItemBag(player, item, 0);
        InventoryService.gI().sendItemBags(player);
    }

    public void GetTrangBiKichHoatthiensu(Player player, int id) {
        Item item = ItemService.gI().createNewItem((short) id);
        int[][] optionNormal = {{127, 128}, {130, 132}, {133, 135}};
        int[][] paramNormal = {{139, 140}, {142, 144}, {136, 138}};
        int[][] optionVIP = {{129}, {131}, {134}};
        int[][] paramVIP = {{141}, {143}, {137}};
        int random = Util.nextInt(optionNormal.length);
        int randomSkh = Util.nextInt(100);
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(47, Util.nextInt(2000, 2500)));
        }
        if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(22, Util.nextInt(150, 200)));
        }
        if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(0, Util.nextInt(18000, 20000)));
        }
        if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(23, Util.nextInt(150, 200)));
        }
        if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(14, Util.nextInt(20, 25)));
        }
        if (randomSkh <= 20) {//tile ra do kich hoat
            if (randomSkh <= 5) { // tile ra option vip
                item.itemOptions.add(new ItemOption(optionVIP[player.gender][0], 0));
                item.itemOptions.add(new ItemOption(paramVIP[player.gender][0], 0));
                item.itemOptions.add(new ItemOption(30, 0));
            } else {// 
                item.itemOptions.add(new ItemOption(optionNormal[player.gender][random], 0));
                item.itemOptions.add(new ItemOption(paramNormal[player.gender][random], 0));
                item.itemOptions.add(new ItemOption(30, 0));
            }
        }

        InventoryService.gI().addItemBag(player, item, 0);
        InventoryService.gI().sendItemBags(player);
    }

    private void doiManhKichHoat(Player player) {
        if (player.combineNew.itemsCombine.size() == 2 || player.combineNew.itemsCombine.size() == 3) {
            Item nr1s = null, doThan = null, buaBaoVe = null;
            for (Item it : player.combineNew.itemsCombine) {
                if (it.template.id == 14) {
                    nr1s = it;
                } else if (it.template.id == 2010) {
                    buaBaoVe = it;
                } else if (it.template.id >= 555 && it.template.id <= 567) {
                    doThan = it;
                }
            }

            if (nr1s != null && doThan != null) {
                if (InventoryService.gI().getCountEmptyBag(player) > 0
                        && player.inventory.gold >= COST_DOI_MANH_KICH_HOAT) {
                    player.inventory.gold -= COST_DOI_MANH_KICH_HOAT;
                    int tiLe = buaBaoVe != null ? 100 : 50;
                    if (Util.isTrue(tiLe, 100)) {
                        sendEffectSuccessCombine(player);
                        Item item = ItemService.gI().createNewItem((short) 2009);
                        item.itemOptions.add(new ItemOption(30, 0));
                        InventoryService.gI().addItemBag(player, item, 0);
                    } else {
                        sendEffectFailCombine(player);
                    }
                    InventoryService.gI().subQuantityItemsBag(player, nr1s, 1);
                    InventoryService.gI().subQuantityItemsBag(player, doThan, 1);
                    if (buaBaoVe != null) {
                        InventoryService.gI().subQuantityItemsBag(player, buaBaoVe, 1);
                    }
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            } else {
                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 trang bị thần linh và 1 viên ngọc rồng 1 sao", "Đóng");
            }
        }
    }

    private void chuyenhoahuydiet(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            player.inventory.gold -= 500000000;
            Item item = player.combineNew.itemsCombine.get(0);
            Item phieu = null;
            switch (item.template.id) {
                case 650:
                case 652:
                case 654:
                    phieu = ItemService.gI().createNewItem((short) 1327);
                    break;
                case 651:
                case 653:
                case 655:
                    phieu = ItemService.gI().createNewItem((short) 1328);
                    break;
                case 657:
                case 659:
                case 661:
                    phieu = ItemService.gI().createNewItem((short) 1329);
                    break;
                case 658:
                case 660:
                case 662:
                    phieu = ItemService.gI().createNewItem((short) 1330);
                    break;
                default:
                    phieu = ItemService.gI().createNewItem((short) 1331);
                    break;
            }
            sendEffectSuccessCombine(player);
            this.baHatMit.npcChat(player, "Con đã nhận được 1 " + phieu.template.name);
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
            player.combineNew.itemsCombine.clear();
            InventoryService.gI().addItemBag(player, phieu, 0);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            reOpenItemCombine(player);
        }
    }

    private void PhanRaDoTS(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            player.inventory.gold -= 500000000;
            Item item = player.combineNew.itemsCombine.get(0);
            Item manhts = null;
            switch (item.template.id) {
                case 1048:
                case 1049:
                case 1050:
                    manhts = ItemService.gI().createNewItem((short) 1066);
                    break;
                case 1051:
                case 1052:
                case 1053:
                    manhts = ItemService.gI().createNewItem((short) 1067);
                    break;
                case 1054:
                case 1055:
                case 1056:
                    manhts = ItemService.gI().createNewItem((short) 1070);
                    break;
                case 1057:
                case 1058:
                case 1059:
                    manhts = ItemService.gI().createNewItem((short) 1068);
                    break;
                default:
                    manhts = ItemService.gI().createNewItem((short) 1069);
                    break;
            }
            sendEffectSuccessCombine(player);
            manhts.quantity = 500;
            this.npcwhists.npcChat(player, "Con đã nhận được 500 " + manhts.template.name);
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
            player.combineNew.itemsCombine.clear();
            InventoryService.gI().addItemBag(player, manhts, 999);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            reOpenItemCombine(player);
        }
    }

    public void openDTS(Player player) {
        //check sl đồ tl, đồ hd
        // new update 2 mon huy diet + 1 mon than linh(skh theo style) +  5 manh bat ki
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.getInstance().sendThongBao(player, "Thiếu đồ");
            return;
        }
        if (player.inventory.gold < COST) {
            Service.getInstance().sendThongBao(player, "Ảo ít thôi con...");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }
        Item itemTL = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isCongThuc()).findFirst().get();
        Item itemHDs = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1083).findFirst().get();
        Item itemManh = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999).findFirst().get();

        player.inventory.gold -= COST;
        sendEffectSuccessCombine(player);
        short[][] itemIds = {{1048, 1051, 1054, 1057, 1060}, {1049, 1052, 1055, 1058, 1061}, {1050, 1053, 1056, 1059, 1062}}; // thứ tự td - 0,nm - 1, xd - 2

        Item itemTS = ItemService.gI().DoThienSu(itemIds[itemTL.template.gender > 2 ? player.gender : itemTL.template.gender][itemManh.typeIdManh()], itemTL.template.gender);
        InventoryService.gI().addItemBag(player, itemTS, 0);

        InventoryService.gI().subQuantityItemsBag(player, itemTL, 1);
        InventoryService.gI().subQuantityItemsBag(player, itemManh, 999);
        InventoryService.gI().subQuantityItemsBag(player, itemHDs, 1);
        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);
        Service.getInstance().sendThongBao(player, "Bạn đã nhận được " + itemTS.template.name);
        player.combineNew.itemsCombine.clear();
        reOpenItemCombine(player);
    }

    public void NangCapSKH(Player player) {

        // ==================== 1. CHECK SỐ LƯỢNG MÓN ======================
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.getInstance().sendThongBao(player, "Cần đúng 3 món để nâng cấp SKH");
            return;
        }

        List<Item> list = player.combineNew.itemsCombine;

        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống");
            return;
        }

        // ==================== 2. CHECK VÀNG ======================
        if (player.inventory.gold < 500_000_000) {
            Service.getInstance().sendThongBao(player, "Không đủ 500 triệu vàng");
            return;
        }

        // ==================== 3. CHECK CÙNG HÀNH TINH ======================
        Set<Byte> genderSet = new HashSet<>();
        for (Item it : list) {
            if (it.template.type != 4) { // bỏ rada
                genderSet.add(it.template.gender);
            }
        }
        if (genderSet.size() > 1) {
            Service.getInstance().sendThongBao(player, "3 món phải cùng hành tinh");
            return;
        }

        // ==================== 4. CHECK 3 TYPE KHÁC NHAU ======================
        Set<Integer> typeSet = new HashSet<>();
        for (Item it : list) {
            if (it != null && it.template != null && it.isNotNullItem()) {
                typeSet.add((int) it.template.type);
            }
        }

        if (typeSet.size() < 3) {
            Service.getInstance().sendThongBao(player, "3 món phải là 3 loại khác nhau (Áo - Quần - Găng - Giày - Nhẫn)");
            return;
        }

        // ==================== DANH SÁCH ID THEO HẠNG ======================
        int[] CAP12 = {233, 237, 241, 245, 249, 253, 257, 261, 265, 269, 273, 277, 281};
        int[] TL = {555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 566, 567};
        int[] HD = {650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 662};
        int[] TS = {1048, 1049, 1050, 1051, 1052, 1053, 1054, 1055, 1056, 1057, 1058, 1059, 1060, 1061, 1062};

        boolean allC12 = true, allTL = true, allHD = true, allTSx = true;

        for (Item it : list) {
            int id = it.template.id;
            allC12 &= IntStream.of(CAP12).anyMatch(x -> x == id);
            allTL &= IntStream.of(TL).anyMatch(x -> x == id);
            allHD &= IntStream.of(HD).anyMatch(x -> x == id);
            allTSx &= IntStream.of(TS).anyMatch(x -> x == id);
        }

        if (!(allC12 || allTL || allHD || allTSx)) {
            Service.getInstance().sendThongBao(player, "3 món phải cùng hạng: Cấp 12 / Thần Linh / Hủy Diệt / Thiên Sứ");
            return;
        }

        // ==================== 5. MAP SKH CẶP ======================
        Map<Integer, Integer> SET_SKH = new HashMap<Integer, Integer>() {
            {
                put(127, 139);
                put(139, 127);
                put(128, 140);
                put(140, 128);
                put(129, 141);
                put(141, 129);
                put(130, 142);
                put(142, 130);
                put(131, 143);
                put(143, 131);
                put(132, 144);
                put(144, 132);
                put(133, 136);
                put(136, 133);
                put(134, 137);
                put(137, 134);
                put(135, 138);
                put(138, 135);
                put(250, 253);
                put(253, 250);
                put(251, 254);
                put(254, 251);
                put(233, 234);
            }
        };

        // ==================== 6. LẤY SKH TỪ 3 MÓN ======================
        List<Integer> skhIds = new ArrayList<>();
        for (Item it : list) {
            boolean found = false;
            for (ItemOption io : it.itemOptions) {
                if (io != null && SET_SKH.containsKey(io.optionTemplate.id)) {
                    skhIds.add(io.optionTemplate.id);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Service.getInstance().sendThongBao(player, "Mỗi món phải có ít nhất 1 dòng SKH");
                return;
            }
        }

        // ==================== 7. MAP CẤP 12 → TL ======================
        Map<Integer, Integer> MAP_C12_TL = new HashMap<Integer, Integer>() {
            {
                put(233, 555);
                put(237, 556);
                put(241, 562);
                put(245, 563);
                put(249, 561);
                put(253, 557);
                put(257, 558);
                put(261, 564);
                put(265, 565);
                put(269, 561);
                put(273, 559);
                put(277, 560);
                put(281, 566);
            }
        };

        // ==================== 8. MAP TL → HD ======================
        Map<Integer, Integer> MAP_TL_HD = new HashMap<Integer, Integer>() {
            {
                put(555, 650);
                put(556, 651);
                put(557, 652);
                put(558, 653);
                put(559, 654);
                put(560, 655);
                put(561, 656);
                put(562, 657);
                put(564, 659);
                put(566, 661);
                put(563, 658);
                put(565, 660);
                put(567, 662);
            }
        };

        // ==================== 9. MAP HD → TS ======================
        Map<Integer, Integer> MAP_HD_TS = new HashMap<Integer, Integer>() {
            {
                put(650, 1048);
                put(652, 1049);
                put(654, 1050);
                put(651, 1051);
                put(653, 1052);
                put(655, 1053);
                put(657, 1054);
                put(659, 1055);
                put(661, 1056);
                put(658, 1057);
                put(660, 1058);
                put(662, 1059);
            }
        };

        // ==================== 10. XÁC ĐỊNH ID MỚI ======================
        Item base = list.get(0);
        int oldId = base.template.id;
        int nextId = -1;

        if (allC12) {
            nextId = MAP_C12_TL.get(oldId);
        } else if (allTL) {
            nextId = MAP_TL_HD.get(oldId);
        } else if (allHD) {

            if (oldId == 656) { // NHẪN HD gender 3 → theo hành tinh 2 món còn lại
                int planet = list.get(1).template.gender;
                nextId = 1060 + planet; // 1060,1061,1062
            } else {
                if (!MAP_HD_TS.containsKey(oldId)) {
                    Service.getInstance().sendThongBao(player, "Không thể nâng cấp món này!");
                    return;
                }
                nextId = MAP_HD_TS.get(oldId);
            }
        } else {
            Service.getInstance().sendThongBao(player, "Thiên Sứ không thể nâng cấp thêm");
            return;
        }

        // ==================== 11. TRỪ NGUYÊN LIỆU ======================
        player.inventory.gold -= 500_000_000;
        Service.getInstance().sendMoney(player);

        for (Item it : list) {
            InventoryService.gI().subQuantityItemsBag(player, it, 1);
        }

        // ==================== 12. TẠO ITEM MỚI + CHỈ SỐ ======================
        Item newItem = ItemService.gI().createNewItem((short) nextId);

        // OTP 21 theo hạng
        if (allC12) {
            newItem.itemOptions.add(new ItemOption(21, 19));   // Thần Linh
        } else if (allTL) {
            newItem.itemOptions.add(new ItemOption(21, 60));   // Hủy Diệt
        } else if (allHD) {
            newItem.itemOptions.add(new ItemOption(21, 120));  // Thiên Sứ
        }

        // Khóa giao dịch
        int baseStat = 0;
        int finalStat;
        byte type = newItem.template.type;

        // CHỈ SỐ GỐC THEO HẠNG + TYPE
        if (allC12) { // Thần Linh
            switch (type) {
                case 0:
                    baseStat = 1200;
                    break;      // áo - giáp
                case 1:
                    baseStat = 56000;
                    break;     // quần - HP
                case 2:
                    baseStat = 4500;
                    break;      // găng - SD
                case 3:
                    baseStat = 56000;
                    break;     // giày - KI
                case 4:
                    baseStat = 16;
                    break;        // nhẫn - CM
            }
        } else if (allTL) { // Hủy Diệt
            switch (type) {
                case 0:
                    baseStat = 1700;
                    break;
                case 1:
                    baseStat = 102;
                    break;
                case 2:
                    baseStat = 8800;
                    break;      // găng HD cố định 8k8
                case 3:
                    baseStat = 102;
                    break;
                case 4:
                    baseStat = 19;
                    break;
            }
        } else if (allHD) { // Thiên Sứ
            switch (type) {
                case 0:
                    baseStat = 2400;
                    break;
                case 1:
                    baseStat = 104;
                    break;
                case 2:
                    baseStat = 15500;
                    break;
                case 3:
                    baseStat = 104;
                    break;
                case 4:
                    baseStat = 24;
                    break;
            }
        }

        // TÍNH CHỈ SỐ CUỐI
        // RANDOM BONUS % TRƯỚC KHI TÍNH CHỈ SỐ
        int bonusPercent = Util.nextInt(1, 15);

        if (type == 4) {
            // chí mạng làm tròn lên
            finalStat = baseStat + (int) Math.ceil(baseStat * bonusPercent / 100.0);
        } else {
            finalStat = baseStat + (baseStat * bonusPercent / 100);
        }

        // APPLY OPTION THEO TYPE
        switch (type) {
            case 0: // áo - giáp
                newItem.itemOptions.add(new ItemOption(47, finalStat));
                break;
            case 1: // quần - HP
                newItem.itemOptions.add(new ItemOption(22, finalStat));
                // nếu muốn giữ 27 7043 thì mở lại:
                // newItem.itemOptions.add(new ItemOption(27, 7043));
                break;
            case 2: // găng - sức đánh
                newItem.itemOptions.add(new ItemOption(0, finalStat));
                break;
            case 3: // giày - KI
                newItem.itemOptions.add(new ItemOption(23, finalStat));
                // newItem.itemOptions.add(new ItemOption(28, 7043));
                break;
            case 4: // nhẫn - chí mạng
                newItem.itemOptions.add(new ItemOption(14, finalStat));
                break;
        }
        newItem.itemOptions.add(new ItemOption(30, 1));

        // 1 - 15%
        // ==================== 13. THÊM SKH RANDOM ======================
        int chosenSKH = skhIds.get(Util.nextInt(0, skhIds.size() - 1));
        newItem.itemOptions.add(new ItemOption(chosenSKH, 0));
        newItem.itemOptions.add(new ItemOption(SET_SKH.get(chosenSKH), 0));

        // ==================== 14. GỬI VỀ NGƯỜI CHƠI ======================
        InventoryService.gI().addItemBag(player, newItem, 1);
        InventoryService.gI().sendItemBags(player);

        // Sửa lại gọi effect theo CombineService (không phải CombineServiceNew)
        CombineServiceNew.gI().sendEffectOpenItem(player, base.template.iconID, newItem.template.iconID);

        player.combineNew.itemsCombine.clear();
        reOpenItemCombine(player);
    }

    private void openSKHts(Player player) {
        if (player == null) {
            return;
        }

        // Khóa theo player để chống race
        java.util.concurrent.locks.Lock lk = lockOf(player);
        if (!lk.tryLock()) {
            Service.getInstance().sendThongBao(player, "Đang có thao tác ghép khác, vui lòng thử lại...");
            return;
        }
        try {
            // ===== B1: Kiểm tra input =====
            if (player.combineNew == null || player.combineNew.itemsCombine == null
                    || player.combineNew.itemsCombine.size() != 7) {
                Service.getInstance().sendThongBao(player, "Cần đúng 7 món để nâng cấp!");
                return;
            }

            final java.util.List<Item> picks = new java.util.ArrayList<>(player.combineNew.itemsCombine);

            // (A) Không trùng reference & map mỗi item -> index trong túi
            java.util.Set<Item> uniqueRef = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            java.util.Map<Item, Integer> bagIdx = new java.util.IdentityHashMap<>();

            for (Item it : picks) {
                if (it == null || !uniqueRef.add(it)) {
                    Service.getInstance().sendThongBao(player, "Không được sử dụng trùng item!");
                    return;
                }
                int idx = -1;
                for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
                    if (player.inventory.itemsBag.get(i) == it) {
                        idx = i;
                        break;
                    }
                }
                if (idx == -1) {
                    Service.getInstance().sendThongBao(player, "Item không tồn tại trong túi đồ!");
                    return;
                }
                if (bagIdx.putIfAbsent(it, idx) != null) {
                    Service.getInstance().sendThongBao(player, "Phát hiện trùng slot túi!");
                    return;
                }
            }

            // ===== B2: Phân loại đích & nguồn =====
            Item target = null;
            java.util.List<Item> sources = new java.util.ArrayList<>(6);

            for (Item it : picks) {
                if (it.template == null) {
                    Service.getInstance().sendThongBao(player, "Có item không hợp lệ!");
                    return;
                }
                int id = it.template.id;
                if (id >= 555 && id <= 567) {
                    if (target != null) {
                        Service.getInstance().sendThongBao(player, "Chỉ được có 1 trang bị Thần Linh!");
                        return;
                    }
                    target = it;
                } else {
                    sources.add(it);
                }
            }
            if (target == null) {
                Service.getInstance().sendThongBao(player, "Thiếu trang bị đích Thần Linh!");
                return;
            }
            if (sources.size() != 6) {
                Service.getInstance().sendThongBao(player, "Cần đúng 6 món nguồn và 1 món đích!");
                return;
            }

            // ===== B3: Đích không có option đặc biệt =====
            if (target.itemOptions != null) {
                for (ItemOption io : target.itemOptions) {
                    if (io == null || io.optionTemplate == null) {
                        continue;
                    }
                    int op = io.optionTemplate.id;
                    if ((op >= 127 && op <= 135) || (op >= 136 && op <= 144) || (op >= 211 && op <= 219) || (op >= 220 && op <= 228)) {
                        Service.getInstance().sendThongBao(player, "Trang bị đích đã có option đặc biệt!");
                        return;
                    }
                }
            }

            // ===== B4: Thu thập option đặc biệt từ 6 món nguồn (tránh trùng) =====
            int sourceTemplateId = sources.get(0).template.id;
            java.util.List<ItemOption> collected = new java.util.ArrayList<>();
            java.util.Set<Integer> seenOps = new java.util.HashSet<>();

            for (Item s : sources) {
                if (s.template == null || s.template.id != sourceTemplateId) {
                    Service.getInstance().sendThongBao(player, "6 món nguồn phải cùng loại!");
                    return;
                }
                if (s.itemOptions == null) {
                    continue;
                }
                for (ItemOption io : s.itemOptions) {
                    if (io == null || io.optionTemplate == null) {
                        continue;
                    }
                    int op = io.optionTemplate.id;
                    if ((op >= 127 && op <= 135) || (op >= 136 && op <= 144) || (op >= 211 && op <= 219) || (op >= 220 && op <= 228)) {
                        if (seenOps.add(op)) {
                            collected.add(new ItemOption((short) op, io.param));
                        }
                    }
                }
            }
            if (collected.isEmpty()) {
                Service.getInstance().sendThongBao(player, "Các món nguồn không có option đặc biệt!");
                return;
            }

            // ===== B5: Kiểm tra & trừ nguyên liệu =====
            final int RUBY_COST = 2000;
            final int TOKEN_ID = 1559, TOKEN_NEED = 20;
            final int STONE_ID = 1554, STONE_NEED = 99;

            // Thêm yêu cầu thức ăn
            final int[] FOOD_IDS = {663, 664, 665, 666, 667};
            final int FOOD_NEED = 99;

            if (player.inventory.ruby < RUBY_COST) {
                Service.getInstance().sendThongBao(player, "Không đủ " + RUBY_COST + " Hồng Ngọc!");
                return;
            }

            int tokenTotal = 0, stoneTotal = 0;
            java.util.Map<Integer, Integer> foodTotals = new java.util.HashMap<>();

            for (Item it : player.inventory.itemsBag) {
                if (it == null || it.template == null) {
                    continue;
                }
                if (it.template.id == TOKEN_ID) {
                    tokenTotal += it.quantity;
                }
                if (it.template.id == STONE_ID) {
                    stoneTotal += it.quantity;
                }
                // Kiểm tra thức ăn
                for (int foodId : FOOD_IDS) {
                    if (it.template.id == foodId) {
                        foodTotals.put(foodId, foodTotals.getOrDefault(foodId, 0) + it.quantity);
                    }
                }
            }

            if (tokenTotal < TOKEN_NEED) {
                Service.getInstance().sendThongBao(player, "Thiếu " + TOKEN_NEED + " tiền tệ!");
                return;
            }
            if (stoneTotal < STONE_NEED) {
                Service.getInstance().sendThongBao(player, "Thiếu " + STONE_NEED + " Đá nâng cấp!");
                return;
            }

            // Kiểm tra có ít nhất 1 loại thức ăn đủ 99
            boolean hasFoodEnough = false;
            int selectedFoodId = -1;
            for (int foodId : FOOD_IDS) {
                if (foodTotals.getOrDefault(foodId, 0) >= FOOD_NEED) {
                    hasFoodEnough = true;
                    selectedFoodId = foodId;
                    break;
                }
            }
            if (!hasFoodEnough) {
                Service.getInstance().sendThongBaoOK(player, "Thiếu " + FOOD_NEED + " thức ăn (cần 1 trong 5 loại thức ăn!");
                return;
            }

            // Helper: trừ vật phẩm ID từ túi (slot rỗng để tránh ghost)
            java.util.function.BiFunction<Integer, Integer, Boolean> subById = (id, needQty) -> {
                int need = needQty;
                for (Item it : player.inventory.itemsBag) {
                    if (need == 0) {
                        break;
                    }
                    if (it != null && it.template != null && it.template.id == id && it.quantity > 0) {
                        int take = Math.min(need, it.quantity);
                        it.quantity -= take;
                        need -= take;
                        if (it.quantity <= 0) {
                            it.itemOptions.clear();
                            it.createTime = 0;
                            it.template = null;
                        }
                    }
                }
                return need == 0;
            };

            // Helper: xoá theo INDEX (không remove list)
            java.util.function.Consumer<java.util.Collection<Integer>> deleteSlots = (idxs) -> {
                for (int idx : idxs) {
                    if (idx >= 0 && idx < player.inventory.itemsBag.size()) {
                        Item slot = player.inventory.itemsBag.get(idx);
                        if (slot != null) {
                            slot.itemOptions.clear();
                            slot.quantity = 0;
                            slot.createTime = 0;
                            slot.template = null;
                        }
                    }
                }
            };

            // Chốt index để xoá/ghi
            int targetIdx = bagIdx.get(target);
            java.util.List<Integer> sourceIdxs = new java.util.ArrayList<>(6);
            for (Item s : sources) {
                sourceIdxs.add(bagIdx.get(s));
            }

            // ===== B6: Trừ nguyên liệu trước (theo thiết kế gốc) =====
            if (!subById.apply(TOKEN_ID, TOKEN_NEED) || !subById.apply(STONE_ID, STONE_NEED)
                    || !subById.apply(selectedFoodId, FOOD_NEED)) {
                Service.getInstance().sendThongBao(player, "Kho nguyên liệu đã thay đổi, vui lòng thử lại!");
                return;
            }
            player.inventory.ruby -= RUBY_COST;

            // ===== B7: RNG 36% =====
            boolean success = Util.isTrue(36, 100);

            if (success) {
                // Tiêu hao 6 món nguồn (bắt buộc) → NGĂN DUP
                deleteSlots.accept(sourceIdxs);

                // Ghi option vào món đích (xác nhận slot chưa đổi)
                Item targetInBag = player.inventory.itemsBag.get(targetIdx);
                if (targetInBag != target || targetInBag == null) {
                    Service.getInstance().sendThongBao(player, "Slot trang bị đã thay đổi, hủy thao tác!");
                    return;
                }

                // Thêm toàn bộ option đã thu thập
                if (targetInBag.itemOptions == null) {
                    targetInBag.itemOptions = new java.util.ArrayList<>();
                }
                for (ItemOption opt : collected) {
                    targetInBag.itemOptions.add(new ItemOption(opt.optionTemplate.id, opt.param));
                }

                CombineServiceNew.gI().sendEffectOpenItem(player,
                        (short) targetInBag.template.iconID, (short) targetInBag.template.iconID);
                sendEffectSuccessCombine(player);
                Service.getInstance().sendThongBao(player, "Thành công! Đã chuyển " + collected.size() + " option sang trang bị đích.");
            } else {
                // Thất bại: mất cả 7 món
                deleteSlots.accept(sourceIdxs);
                deleteSlots.accept(java.util.Collections.singleton(targetIdx));
                Service.getInstance().sendThongBao(player, "Thất bại! Tất cả trang bị và nguyên liệu đã tan biến.");
            }

            // ===== B8: Đồng bộ =====
            player.combineNew.itemsCombine.clear();
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            reOpenItemCombine(player);

        } catch (Exception e) {
            e.printStackTrace();
            Service.getInstance().sendThongBao(player, "Có lỗi khi nâng cấp SKH!");
        } finally {
            lk.unlock();
        }
    }

    private void mơkhoagd(Player player) {

        if (player.combineNew.itemsCombine.size() != 2) {
            Service.getInstance().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }

        Item item = null;
        Item da = null;

        for (Item it : player.combineNew.itemsCombine) {
            if (it.template.id == 1613) {
                da = it;
            } else {
                item = it;
            }
        }

        if (item == null || da == null || da.quantity < 10) {
            Service.getInstance().sendThongBao(player, "Cần 10 Đá Mở Khóa");
            return;
        }

        // ===== TRỪ 10 ĐÁ =====
        InventoryService.gI().subQuantityItemsBag(player, da, 10);

        // ===== KIỂM TRA TỈ LỆ THÀNH CÔNG 30% =====
        boolean success = Util.isTrue(30, 100);

        if (success) {

            // ===== XÓA OTP 30 =====
            ItemOption optLock = null;
            ItemOption opt250 = null;

            for (ItemOption io : item.itemOptions) {
                if (io.optionTemplate.id == 30) {
                    optLock = io;
                }
                if (io.optionTemplate.id == 250) {
                    opt250 = io;
                }
            }

            if (optLock != null) {
                item.itemOptions.remove(optLock);
            }

            // ===== THÊM hoặc TĂNG OPT 250 =====
            if (opt250 == null) {
                item.itemOptions.add(new ItemOption(250, 3));
            } else {
                opt250.param += 3;
            }

            sendEffectSuccessCombine(player);
            Service.getInstance().sendThongBao(player, "Mở khóa thành công!");

        } else {
            // ===== THẤT BẠI – KHÔNG ảnh hưởng trang bị =====
            sendEffectFailCombine(player);
            Service.getInstance().sendThongBao(player, "Thất bại! Trang bị không bị ảnh hưởng.");
        }

        // ===== Đồng bộ dữ liệu =====
        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);

        player.combineNew.itemsCombine.clear();
        reOpenItemCombine(player);
    }

    public void NcapDoThanLinh(Player player) {
        // 1 thiên sứ + 2 món kích hoạt -- món đầu kh làm gốc
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.getInstance().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDTL()).count() != 1) {
            Service.getInstance().sendThongBao(player, "Thiếu đồ Thần linh");
            return;
        }
        Item doThanLinh = player.combineNew.itemsCombine.get(0);
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.inventory.gold < 2_000_000_000) {
                Service.getInstance().sendThongBao(player, "Con cần thêm vàng để đổi...");
                return;
            }
            player.inventory.gold -= 2_000_000_000;
            CombineServiceNew.gI().sendEffectOpenItem(player, doThanLinh.template.iconID, doThanLinh.template.iconID);
            Item item = Util.ratiItemHuyDiet(Manager.doHuyDiet[doThanLinh.template.gender][doThanLinh.template.type]);
            item.itemOptions.add(new ItemOption(30, 1));
            InventoryService.gI().addItemBag(player, item, 0);
            InventoryService.gI().subQuantityItemsBag(player, doThanLinh, 1);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            player.combineNew.itemsCombine.clear();
            reOpenItemCombine(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    public void randomskh(Player player) {
        // 1 thiên sứ + 2 món kích hoạt -- món đầu kh làm gốc
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.getInstance().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDTL()).count() != 3) {
            Service.getInstance().sendThongBao(player, "Thiếu đồ Thần linh");
            return;
        }
        Item montldau = player.combineNew.itemsCombine.get(0);
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.inventory.gold < 1) {
                Service.getInstance().sendThongBao(player, "Con cần thêm vàng để đổi...");
                return;
            }
            if (player.inventory.gold < 1) {
                Service.getInstance().sendThongBao(player, "Con cần thêm vàng để đổi...");
                return;
            }
            player.inventory.gold -= COST;
            List<Item> itemDTL = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDTL()).collect(Collectors.toList());
            CombineServiceNew.gI().sendEffectOpenItem(player, montldau.template.iconID, montldau.template.iconID);
            short itemId;
            if (player.gender == 3 || montldau.template.type == 4) {
                itemId = Manager.radaSKHThuong[0];
            } else {
                itemId = Manager.doSKHThuong[player.gender][montldau.template.type];
            }
            int skhId = ItemService.gI().randomSKHId(player.gender);
            Item item = ItemService.gI().itemSKH(itemId, skhId);
            InventoryService.gI().addItemBag(player, item, 0);
            itemDTL.forEach(i -> InventoryService.gI().subQuantityItemsBag(player, i, 1));
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            player.combineNew.itemsCombine.clear();
            reOpenItemCombine(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    private void GiaHanTrangBi(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            Service.getInstance().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isTrangBiHSD()).count() != 1) {
            Service.getInstance().sendThongBao(player, "Thiếu trang bị HSD");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1346).count() != 1) {
            Service.getInstance().sendThongBao(player, "Thiếu Bùa Gia Hạn");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            Item thegh = player.combineNew.itemsCombine.stream().filter(item -> item.template.id == 1346).findFirst().get();
            Item tbiHSD = player.combineNew.itemsCombine.stream().filter(Item::isTrangBiHSD).findFirst().get();
            if (thegh == null) {
                Service.getInstance().sendThongBao(player, "Thiếu Bùa Gia Hạn");
                return;
            }
            if (tbiHSD == null) {
                Service.getInstance().sendThongBao(player, "Thiếu trang bị HSD");
                return;
            }
            if (tbiHSD != null) {
                for (ItemOption itopt : tbiHSD.itemOptions) {
                    if (itopt.optionTemplate.id == 93 || itopt.optionTemplate.id == 63) {
                        if (itopt.param < 0 || itopt == null) {
                            Service.getInstance().sendThongBao(player, "Không Phải Trang Bị Có HSD");
                            return;
                        }
                    }
                }
            }
            if (Util.isTrue(100, 100)) {
                sendEffectSuccessCombine(player);
                for (ItemOption itopt : tbiHSD.itemOptions) {
                    if (itopt.optionTemplate.id == 93 || itopt.optionTemplate.id == 63) {
                        itopt.param += 1;
                        break;
                    }
                }
            } else {
                sendEffectFailCombine(player);
            }
            InventoryService.gI().subQuantityItemsBag(player, thegh, 1);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            reOpenItemCombine(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    private void detuvip(Player player) {
        // kiểm tra có đúng 3 vật phẩm trong combine
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.getInstance().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }

        // kiểm tra Trứng bư (id 568, cần 15)
        Item trungBu = player.combineNew.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.template.id == 568)
                .findFirst().orElse(null);

        if (trungBu == null || trungBu.quantity < 15) {
            Service.getInstance().sendThongBaoOK(player, "Thiếu trứng bư (cần 15)");
            return;
        }

        // kiểm tra Hồn bill (id 1108, cần 5)
        Item honBill = player.combineNew.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.template.id == 1108)
                .findFirst().orElse(null);

        if (honBill == null || honBill.quantity < 5) {
            Service.getInstance().sendThongBaoOK(player, "Thiếu hồn bill (cần 5)");
            return;
        }

        // kiểm tra Xu (id 1535, cần 99)
        Item xu = player.combineNew.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.template.id == 1535)
                .findFirst().orElse(null);

        if (xu == null || xu.quantity < 99) {
            Service.getInstance().sendThongBaoOK(player, "Thiếu xu (cần 99)");
            return;
        }

        // kiểm tra còn chỗ trống trong hành trang
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.getInstance().sendThongBaoOK(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }

        // trừ nguyên liệu trước khi ghép
        InventoryService.gI().subQuantityItemsBag(player, trungBu, 15);
        InventoryService.gI().subQuantityItemsBag(player, honBill, 5);
        InventoryService.gI().subQuantityItemsBag(player, xu, 99);

        // tiến hành ghép (tỉ lệ thành công 30%)
        if (Util.isTrue(30, 100)) {
            // thành công
            Item newItem = ItemService.gI().createNewItem((short) 1666);
            InventoryService.gI().addItemBag(player, newItem, 1);
            InventoryService.gI().sendItemBags(player);
            sendEffectSuccessCombine(player);
            Service.getInstance().sendThongBaoOK(player, "Chúc mừng bạn đã ghép thành công vật phẩm mới!");
        } else {
            // thất bại
            sendEffectFailCombine(player);
            Service.getInstance().sendThongBaoOK(player, "Ghép thất bại, nguyên liệu đã bị mất!");
        }

        // cập nhật túi và tiền
        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);

        // mở lại tab combine
        reOpenItemCombine(player);
    }

    public void khamDaTrangBi(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            return;
        }

        Item trangBi = player.combineNew.itemsCombine.get(0);
        Item da = player.combineNew.itemsCombine.get(1);

        // check đá
        if (da.template.id < 1566 || da.template.id > 1569) {
            Service.getInstance().sendThongBao(player, "Chỉ có thể dùng Ngọc trai");
            return;
        }

        // check trang bị hợp lệ
        if (!isTrangBiKham(trangBi)) {
            Service.getInstance().sendThongBao(player, "Vật phẩm này không thể khảm");
            return;
        }

        // lấy option số lỗ đục (245) và số lỗ khảm (246)
        ItemOption opDuc = null, opKham = null;
        for (ItemOption op : trangBi.itemOptions) {
            if (op.optionTemplate.id == 245) {
                opDuc = op;
            }
            if (op.optionTemplate.id == 246) {
                opKham = op;
            }
        }
        int soLoDuc = (opDuc != null) ? opDuc.param : 0;
        int soLoKham = (opKham != null) ? opKham.param : 0;

        if (soLoDuc <= 0) {
            Service.getInstance().sendThongBao(player, "Trang bị chưa được đục lỗ");
            return;
        }
        if (soLoKham >= soLoDuc) {
            Service.getInstance().sendThongBao(player, "Đã khảm đủ số lỗ (" + soLoKham + "/" + soLoDuc + ")");
            return;
        }

        // check nguyên liệu 1559 (20 cái)
        Item nguyenLieu = InventoryService.gI().findItemBagByTemp(player, 1559);
        if (nguyenLieu == null || nguyenLieu.quantity < 20) {
            Service.getInstance().sendThongBao(player, "Cần 20 Tiền tệ");
            return;
        }

        // check ruby
        if (player.inventory.ruby < 2000) {
            Service.getInstance().sendThongBao(player, "Không đủ " + player.combineNew.goldCombine + " hồng ngọc");
            return;
        }

        // ✅ trừ nguyên liệu
        InventoryService.gI().subQuantityItemsBag(player, nguyenLieu, 20);
        InventoryService.gI().subQuantityItemsBag(player, da, 1);
        player.inventory.ruby -= player.combineNew.goldCombine;

        // cập nhật client
        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);

        // random thành công
        if (Util.nextInt(0, 100) < player.combineNew.ratioCombine) {
            // cập nhật số lỗ đã khảm
            if (opKham == null) {
                trangBi.itemOptions.add(new ItemOption(246, 1));
            } else {
                opKham.param++;
            }

            // thêm/cộng chỉ số từ đá khảm
            for (ItemOption opDa : da.itemOptions) {
                boolean tonTai = false;
                for (ItemOption opTB : trangBi.itemOptions) {
                    if (opTB.optionTemplate.id == opDa.optionTemplate.id
                            && opTB.optionTemplate.id != 245
                            && opTB.optionTemplate.id != 246) {
                        opTB.param += opDa.param;
                        tonTai = true;
                        break;
                    }
                }
                if (!tonTai) {
                    trangBi.itemOptions.add(new ItemOption(opDa.optionTemplate.id, opDa.param));
                }
            }

            Service.getInstance().sendThongBao(player,
                    "Khảm thành công! (" + (soLoKham + 1) + "/" + soLoDuc + ")");
            sendEffectSuccessCombine(player);

        } else {
            Service.getInstance().sendThongBao(player, "Khảm thất bại");
            sendEffectFailCombine(player);
        }

        // clear combine
        player.combineNew.itemsCombine.clear();
        reOpenItemCombine(player);
    }

    public void ducLoTrangBi(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            return;
        }
        Item trangBi = player.combineNew.itemsCombine.get(0);
        if (!isTrangBiKham(trangBi)) {
            Service.getInstance().sendThongBao(player, "Vật phẩm này không thể đục lỗ");
            return;
        }
        // check nguyên liệu 25 tiền tệ (id = 1559)
        Item nguyenLieu = InventoryService.gI().findItemBagByTemp(player, 1559);
        if (nguyenLieu == null || nguyenLieu.quantity < 25) {
            Service.getInstance().sendThongBao(player, "Bạn cần 25 tiền tệ");
            return;
        }
        // check hồng ngọc
        if (player.inventory.ruby < 2000) {
            Service.getInstance().sendThongBao(player, "Không đủ hồng ngọc");
            return;
        }

        // lấy option số lỗ TRƯỚC KHI trừ tài nguyên
        ItemOption opLo = null;
        ItemOption opLoKham = null;
        for (ItemOption op : trangBi.itemOptions) {
            if (op.optionTemplate.id == 245) {
                opLo = op;       // số lỗ đã đục
            }
            if (op.optionTemplate.id == 246) {
                opLoKham = op;   // số lỗ đã khảm
            }
        }
        int current = (opLo == null ? 0 : opLo.param);
        if (current >= 7) {
            Service.getInstance().sendThongBao(player, "Trang bị đã đạt tối đa 7 lỗ");
            return;
        }

        // trừ tài nguyên - SỬA: trừ ruby thay vì gem
        player.inventory.ruby -= 2000;
        Service.getInstance().sendMoney(player);
        InventoryService.gI().subQuantityItemsBag(player, nguyenLieu, 25);
        InventoryService.gI().sendItemBags(player);

        // xử lý tỉ lệ
        if (Util.nextInt(0, 100) < player.combineNew.ratioCombine) {
            if (opLo == null) {
                trangBi.itemOptions.add(new ItemOption(245, 1));
            } else {
                opLo.param++;
            }
            // nếu chưa có số lỗ đã khảm thì thêm mới (mặc định = 0)
            if (opLoKham == null) {
                trangBi.itemOptions.add(new ItemOption(246, 0));
            }
            Service.getInstance().sendThongBao(player,
                    "Đục lỗ thành công! (" + (current + 1) + "/7)");
            sendEffectSuccessCombine(player);
        } else {
            Service.getInstance().sendThongBao(player, "Đục lỗ thất bại");
            sendEffectFailCombine(player);
        }
        player.combineNew.itemsCombine.clear();
    }

    private void epSaoTrangBi(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.getInstance().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (isTrangBiPhaLeHoa(item)) {
                    trangBi = item;
                } else if (isDaPhaLe(item)) {
                    daPhaLe = item;
                }
            }
            int star = 0; //sao pha lê đã ép
            int starEmpty = 0; //lỗ sao pha lê
            if (trangBi != null && daPhaLe != null) {
                ItemOption optionStar = null;
                for (ItemOption io : trangBi.itemOptions) {
                    if (io.optionTemplate.id == 102) {
                        star = io.param;
                        optionStar = io;
                    } else if (io.optionTemplate.id == 107) {
                        starEmpty = io.param;
                    }
                }
                if (star < starEmpty) {
                    player.inventory.gem -= gem;
                    int optionId = getOptionDaPhaLe(daPhaLe);
                    int param = getParamDaPhaLe(daPhaLe);
                    if (star >= 6 && star < 8) {
                        // Sao 6 - 7 - 8: x2
                        param *= 2;
                    } else if (star >= 8) {
                        // Sao 9 trở lên: x3
                        param *= 3;
                    }
                    ItemOption option = null;
                    for (ItemOption io : trangBi.itemOptions) {
                        if (io.optionTemplate.id == optionId) {
                            option = io;
                            break;
                        }
                    }
                    if (option != null) {
                        option.param += param;
                    } else {
                        trangBi.itemOptions.add(new ItemOption(optionId, param));
                    }
                    if (optionStar != null) {
                        optionStar.param++;
                    } else {
                        trangBi.itemOptions.add(new ItemOption(102, 1));
                    }

                    InventoryService.gI().subQuantityItemsBag(player, daPhaLe, 1);
                    sendEffectSuccessCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void nangCapVyThu(Player player) {

        // Chỉ đặt đúng 1 item vào ô ghép
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.getInstance().sendThongBao(player, "Hãy đặt đúng 1 Vỹ Thú vào ô ghép");
            return;
        }

        Item vyThu = player.combineNew.itemsCombine.get(0);
        short id = vyThu.template.id;

        // Thứ tự cấp độ Vỹ Thú Y -> Mạnh
        List<Short> vtRank = Arrays.asList(
                (short) 1539, (short) 1540, (short) 1541, (short) 1542,
                (short) 1543, (short) 1544, (short) 1545, (short) 1546,
                (short) 1524
        );

        if (!vtRank.contains(id)) {
            Service.getInstance().sendThongBao(player, "Đây không phải là Vỹ Thú hợp lệ!");
            return;
        }

        // ❗ Check nếu đã có chỉ số 50 → KHÔNG được nâng cấp nữa
        for (ItemOption op : vyThu.itemOptions) {
            if (op.optionTemplate.id == 50) {
                Service.getInstance().sendThongBao(player,
                        "Vỹ Thú này đã được nâng cấp, không thể nâng thêm!");
                return;
            }
        }

        // Rank dựa theo thứ tự ID
        int rank = vtRank.indexOf(id) + 1;
        int addParam = rank * 5; // +5 mỗi cấp độ

        // Thứ tự đúng yêu cầu: KI(77) → HP(103) → SD(50)
        int[] orderedOps = {77, 103, 50};

        for (int opId : orderedOps) {

            boolean found = false;

            for (ItemOption op : vyThu.itemOptions) {
                if (op.optionTemplate.id == opId) {
                    op.param += addParam;
                    found = true;
                    break;
                }
            }

            if (!found) {
                vyThu.itemOptions.add(new ItemOption(opId, addParam));
            }
        }

        // Sắp xếp lại đúng thứ tự OTP: 77 → 103 → 50
        vyThu.itemOptions.sort((a, b) -> {
            int[] order = {77, 103, 50};

            int indexA = 999, indexB = 999;
            for (int i = 0; i < order.length; i++) {
                if (a.optionTemplate.id == order[i]) {
                    indexA = i;
                }
                if (b.optionTemplate.id == order[i]) {
                    indexB = i;
                }
            }
            return Integer.compare(indexA, indexB);
        });

        InventoryService.gI().sendItemBags(player);
        sendEffectSuccessCombine(player);

        Service.getInstance().sendThongBao(player,
                "Nâng cấp thành công!\n"
                + "Tăng +" + addParam + " cho KI, HP và Sức đánh.");

        reOpenItemCombine(player);
    }

    private void phaLeHoaTrangBi(Player player) {
        boolean flag = false; // đánh dấu thành công
        int solandap = player.combineNew.quantities; // số lần đập đã chọn

        while (player.combineNew.quantities > 0
                && !player.combineNew.itemsCombine.isEmpty()
                && !flag) {

            Item item = player.combineNew.itemsCombine.get(0);

            if (!isTrangBiPhaLeHoa(item)) {
                break; // không phải trang bị có thể pha lê hóa
            }

            // 🔹 Lấy số sao hiện tại
            int star = 0;
            ItemOption optionStar = null;
            for (ItemOption io : item.itemOptions) {
                if (io.optionTemplate.id == 107) {
                    star = io.param;
                    optionStar = io;
                    break;
                }
            }

            if (star >= MAX_STAR_ITEM) {
                Service.getInstance().sendThongBao(player, "Vật phẩm đã đạt tối đa sao pha lê");
                break;
            }

            boolean isSpecialItem = (item.template.id >= 1401 && item.template.id <= 1405);
            boolean enoughResource = false;

            // 🔹 Trường hợp đặc biệt: dùng item thay thế (id 2000)
            if (isSpecialItem) {
                int requireItemId = 1559;   // ID nguyên liệu thay thế
                int requireQuantity = 10;    // số lượng cần cho mỗi lần đập

                Item it = InventoryService.gI().findItemBagByTemp(player, requireItemId);
                int haveQuantity = (it != null ? it.quantity : 0);

                if (haveQuantity >= requireQuantity) {
                    InventoryService.gI().subQuantityItemsBag(player, it, requireQuantity);
                    InventoryService.gI().sendItemBags(player);
                    enoughResource = true;
                } else {
                    Service.getInstance().sendThongBao(player,
                            "Không đủ " + ItemService.gI().getTemplate(requireItemId).name);
                    break;
                }

            } else {
                // 🔹 Trang bị thường: trừ vàng + ngọc
                int gold = player.combineNew.goldCombine;
                int gem = player.combineNew.gemCombine;

                if (player.inventory.gold < gold) {
                    Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện");
                    break;
                } else if (player.inventory.gem < gem) {
                    Service.getInstance().sendThongBao(player, "Không đủ ngọc để thực hiện");
                    break;
                } else {
                    player.inventory.gold -= gold;
                    player.inventory.gem -= gem;
                    enoughResource = true;
                }
            }

            // 🔹 Nếu đã đủ tài nguyên
            if (enoughResource) {
                float ratio = getRatioPhaLeHoa(star);
                float epint = player.combineNew.ratioCombine > 0 ? player.combineNew.ratioCombine : ratio;

                flag = Util.isTrue(epint, 100); // tỉ lệ thành công

                if (flag) {
                    if (optionStar == null) {
                        item.itemOptions.add(new ItemOption(107, 1));
                    } else {
                        optionStar.param++;
                    }
                    sendEffectSuccessCombine(player);
                    Service.getInstance().sendThongBao(player,
                            "Lên cấp sau " + (solandap - player.combineNew.quantities + 1) + " lần đập");

                    // Thông báo toàn server nếu đạt 8 sao trở lên
                    if (optionStar != null && optionStar.param >= 6) {
                        ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa pha lê hóa "
                                + "thành công " + item.template.name + " lên "
                                + optionStar.param + " sao pha lê");
                    }
                } else {
                    sendEffectFailCombine(player);
                }
            }

            player.combineNew.quantities -= 1;
        }

        if (!flag) {
            sendEffectFailCombine(player);
        }

        // Cập nhật lại túi đồ & tiền cho client
        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);
        reOpenItemCombine(player);
    }

    private void phaLeHoaTrangBix100(Player player) {
        if (!player.combineNew.itemsCombine.isEmpty()) {
            int gold = player.combineNew.goldCombine;
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gold < gold) {
                Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            } else if (player.inventory.gem < gem) {
                Service.getInstance().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item item = player.combineNew.itemsCombine.get(0);
            if (isTrangBiPhaLeHoa(item)) {
                int star = 0;
                ItemOption optionStar = null;
                for (ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 107) {
                        star = io.param;
                        optionStar = io;
                        break;
                    }
                }
                if (star < MAX_STAR_ITEM) {
                    player.inventory.gold -= gold;
                    player.inventory.gem -= gem;
                    byte ratio = (optionStar != null && optionStar.param > 4) ? (byte) 2 : 1;
                    if (Util.isTrue(player.combineNew.ratioCombine, 100 * ratio)) {
                        if (optionStar == null) {
                            item.itemOptions.add(new ItemOption(107, 1));
                        } else {
                            optionStar.param++;
                        }
                        sendEffectSuccessCombine(player);
                        if (optionStar != null && optionStar.param >= 10) {
                            ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa pha lê hóa "
                                    + "thành công " + item.template.name + " lên " + optionStar.param + " sao pha lê");
                        }
                    } else {
                        sendEffectFailCombine(player);
                    }
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    public void nangcapnro(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.combineNew.itemsCombine != null && !player.combineNew.itemsCombine.isEmpty()) {
                boolean canCombine = true;

                // Kiểm tra số lượng item phải chính xác là 7
                if (player.combineNew.itemsCombine.size() != 7) {
                    Service.getInstance().sendThongBao(player, "Cần đủ 7 loại ngọc");
                    canCombine = false;
                } else {
                    // Kiểm tra từng item
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item == null || !item.isNotNullItem()) {
                            Service.getInstance().sendThongBao(player, "Vật phẩm không hợp lệ");
                            canCombine = false;
                            break;
                        }
                        // Kiểm tra id trong range 14-20
                        if (item.template.id < 14 || item.template.id > 20) {
                            Service.getInstance().sendThongBao(player, "Chỉ nhận ngọc rồng từ 1-7 sao");
                            canCombine = false;
                            break;
                        }
                        // Kiểm tra số lượng mỗi item >= 99
                        if (item.quantity < 99) {
                            Service.getInstance().sendThongBao(player, "Mỗi loại ngọc cần tối thiểu 99 viên");
                            canCombine = false;
                            break;
                        }
                    }
                }

                if (canCombine) {
                    // Lưu icon trước khi xóa items
                    int iconID = player.combineNew.itemsCombine.get(0).template.iconID;

                    // Tạo list copy để tránh ConcurrentModificationException
                    List<Item> itemsToRemove = new ArrayList<>(player.combineNew.itemsCombine);

                    // Trừ số lượng items
                    for (Item item : itemsToRemove) {
                        InventoryService.gI().subQuantityItemsBag(player, item, 99);
                    }

                    // Tạo và thêm Ngọc Rồng Hoàn Hảo
                    Item nr = ItemService.gI().createNewItem((short) 1015);
                    nr.quantity = 1; // Đảm bảo quantity = 1
                    InventoryService.gI().addItemBag(player, nr, 0);
                    InventoryService.gI().sendItemBags(player);

                    // Clear danh sách combine
                    player.combineNew.itemsCombine.clear();

                    // Hiệu ứng thành công
                    sendEffectCombineDB(player, (short) iconID);
                    Service.getInstance().sendThongBao(player, "Nâng cấp thành công Ngọc Rồng Hoàn Hảo!");

                    // Reopen sau khi hoàn thành
                    reOpenItemCombine(player);
                }
            } else {
                Service.getInstance().sendThongBao(player, "Hãy đặt ngọc rồng vào ô ghép");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Hành trang đã đầy");
        }
    }

    private void nhapNgocRong(Player player) {
        // Kiểm tra túi đồ còn chỗ trống
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.getInstance().sendThongBao(player, "Hành trang đã đầy");
            return;
        }

        // Kiểm tra có vật phẩm trong combine
        if (player.combineNew.itemsCombine.isEmpty()) {
            Service.getInstance().sendThongBao(player, "Hãy đặt vật phẩm vào");
            return;
        }

        // Lấy item từ combine
        Item item = player.combineNew.itemsCombine.get(0);

        // Validate item
        if (item == null || !item.isNotNullItem()) {
            Service.getInstance().sendThongBao(player, "Vật phẩm không hợp lệ");
            return;
        }

        // Kiểm tra ID vật phẩm (15-20)
        if (item.template.id <= 14 || item.template.id > 20) {
            Service.getInstance().sendThongBao(player, "Chỉ nhập được ngọc rồng 2-7 sao");
            return;
        }

        int soLuongCan = 7;

        // Kiểm tra số lượng
        if (item.quantity < soLuongCan) {
            Service.getInstance().sendThongBao(player, "Cần " + soLuongCan + " viên ngọc");
            return;
        }

        // Lưu thông tin cần thiết TRƯỚC KHI thao tác
        short newItemId = (short) (item.template.id - 1);
        short iconID = (short) item.template.iconID;  // Đổi thành short

        try {
            // Bước 1: Tìm item trong bag để trừ trực tiếp
            Item itemInBag = null;
            for (Item it : player.inventory.itemsBag) {
                if (it.isNotNullItem() && it.template.id == item.template.id) {
                    itemInBag = it;
                    break;
                }
            }

            if (itemInBag == null || itemInBag.quantity < soLuongCan) {

                System.out.println(itemInBag == null ? "itemInBag is null" : "itemInBag quantity: " + itemInBag.quantity);
                Service.getInstance().sendThongBao(player, "Không tìm thấy vật phẩm trong hành trang");
                return;
            }

            // Bước 2: Trừ số lượng
            InventoryService.gI().subQuantityItemsBag(player, itemInBag, soLuongCan);

            // Bước 3: Tạo ngọc rồng mới (giảm 1 sao)
            Item ngocRongMoi = ItemService.gI().createNewItem(newItemId);
            if (ngocRongMoi != null) {
                ngocRongMoi.quantity = 1;
            }

            // Bước 4: Thêm vào túi
            InventoryService.gI().addItemBag(player, ngocRongMoi, 0);

            // Bước 5: Clear danh sách combine để tránh bug
            player.combineNew.itemsCombine.clear();

            // Bước 6: Gửi update túi đồ
            InventoryService.gI().sendItemBags(player);

            // Bước 7: Mở lại giao diện
            reOpenItemCombine(player);

            // Bước 8: Hiệu ứng thành công
            sendEffectCombineDB(player, iconID);  // Giờ đã đúng kiểu short

            // Thông báo thành công
            Service.getInstance().sendThongBao(player, "Nhập ngọc rồng thành công");

        } catch (Exception e) {
            e.printStackTrace();
            Service.getInstance().sendThongBao(player, "Có lỗi xảy ra, vui lòng thử lại");
        }
    }

    private void antrangbi(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (!player.combineNew.itemsCombine.isEmpty()) {
                Item item = player.combineNew.itemsCombine.get(0);
                Item dangusac = player.combineNew.itemsCombine.get(1);
                int star = 0;
                ItemOption optionStar = null;
                for (ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 34 || io.optionTemplate.id == 35 || io.optionTemplate.id == 35) {
                        star = io.param;
                        optionStar = io;
                        break;
                    }
                }
                if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && (dangusac.template.id == 1232 || dangusac.template.id == 1233 || dangusac.template.id == 1234) && dangusac.quantity >= 99) {
                    if (optionStar == null) {
                        if (dangusac.template.id == 1232) {
                            item.itemOptions.add(new ItemOption(34, 1));
                            sendEffectSuccessCombine(player);
                        } else if (dangusac.template.id == 1233) {
                            item.itemOptions.add(new ItemOption(35, 1));
                            sendEffectSuccessCombine(player);
                        } else if (dangusac.template.id == 1234) {
                            item.itemOptions.add(new ItemOption(36, 1));
                            sendEffectSuccessCombine(player);
                        }
//                    InventoryService.gI().addItemBag(player, item, 0);
                        InventoryService.gI().subQuantityItemsBag(player, dangusac, 99);
                        InventoryService.gI().sendItemBags(player);
                        reOpenItemCombine(player);
//                    sendEffectCombineDB(player, item.template.iconID);
                    } else {
                        Service.getInstance().sendThongBao(player, "Trang bị của bạn có ấn rồi mà !!!");
                    }
                }
            }
        }
    }

    // START _ SÁCH TUYỆT KỸ
    private void giamDinhSach(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {

            Item sachTuyetKy = null;
            Item buaGiamDinh = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (issachTuyetKy(item)) {
                    sachTuyetKy = item;
                } else if (item.template.id == 1508) {
                    buaGiamDinh = item;
                }
            }
            if (sachTuyetKy != null && buaGiamDinh != null) {
                Item sachTuyetKy_2 = ItemService.gI().createNewItem((short) sachTuyetKy.template.id);
                if (checkHaveOption(sachTuyetKy, 0, 241)) {
                    int tyle = new Random().nextInt(100);
                    if (tyle >= 0 && tyle <= 33) {
                        sachTuyetKy_2.itemOptions.add(new ItemOption(50, new Random().nextInt(5, 10)));
                    } else if (tyle > 33 && tyle <= 66) {
                        sachTuyetKy_2.itemOptions.add(new ItemOption(77, new Random().nextInt(10, 15)));
                    } else {
                        sachTuyetKy_2.itemOptions.add(new ItemOption(103, new Random().nextInt(10, 15)));
                    }
                    for (int i = 1; i < sachTuyetKy.itemOptions.size(); i++) {
                        sachTuyetKy_2.itemOptions.add(new ItemOption(sachTuyetKy.itemOptions.get(i).optionTemplate.id, sachTuyetKy.itemOptions.get(i).param));
                    }
                    sendEffectSuccessCombine(player);
                    InventoryService.gI().addItemBag(player, sachTuyetKy_2, 1);
                    InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                    InventoryService.gI().subQuantityItemsBag(player, buaGiamDinh, 1);
                    InventoryService.gI().sendItemBags(player);
                    reOpenItemCombine(player);
                } else {
                    Service.getInstance().sendThongBao(player, "Vui lòng tẩy sách trước khi giảm định lần nữa");
                }
            }
        }
    }

    private void nangCapSachTuyetKy(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {

            Item sachTuyetKy = null;
            Item kimBamGiay = null;

            for (Item item : player.combineNew.itemsCombine) {
                if (issachTuyetKy(item)) {
                    sachTuyetKy = item;
                } else if (item.template.id == 1507) {
                    kimBamGiay = item;
                }
            }
            Item sachTuyetKy_2 = ItemService.gI().createNewItem((short) ((short) sachTuyetKy.template.id + 1));
            if (sachTuyetKy != null && kimBamGiay != null) {
                if (kimBamGiay.quantity < 10) {
                    Service.getInstance().sendThongBao(player, "Không đủ Kìm bấm giấy mà đòi nâng cấp");
                    return;
                }
                if (checkHaveOption(sachTuyetKy, 0, 241)) {
                    Service.getInstance().sendThongBao(player, "Chưa giám định mà đòi nâng cấp");
                    return;
                }
                if (Util.isTrue(30, 100)) {
                    for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
                        sachTuyetKy_2.itemOptions.add(new ItemOption(sachTuyetKy.itemOptions.get(i).optionTemplate.id, sachTuyetKy.itemOptions.get(i).param));
                    }
                    sendEffectSuccessCombine(player);
                    InventoryService.gI().addItemBag(player, sachTuyetKy_2, 1);
                    InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                    InventoryService.gI().subQuantityItemsBag(player, kimBamGiay, 10);
                } else {
                    InventoryService.gI().subQuantityItemsBag(player, kimBamGiay, 10);
                    sendEffectFailCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void phucHoiSach(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item cuonSachCu = InventoryService.gI().findItemBagByTemp(player, (short) 1509);
            int goldPhanra = 10_000_000;
            Item sachTuyetKy = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (issachTuyetKy(item)) {
                    sachTuyetKy = item;
                }
            }
            if (sachTuyetKy != null) {
                int doBen = 0;
                ItemOption optionLevel = null;
                for (ItemOption io : sachTuyetKy.itemOptions) {
                    if (io.optionTemplate.id == 243) {
                        doBen = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                if (cuonSachCu == null) {
                    Service.getInstance().sendThongBaoOK(player, "Cần sách tuyệt kỹ và 10 cuốn sách cũ");
                    return;
                }
                if (cuonSachCu.quantity < 10) {
                    Service.getInstance().sendThongBaoOK(player, "Cần sách tuyệt kỹ và 10 cuốn sách cũ");
                    return;
                }
                if (player.inventory.gold < goldPhanra) {
                    Service.getInstance().sendThongBao(player, "Không có tiền mà đòi phục hồi à");
                    return;
                }
                if (doBen != 1000) {
                    for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
                        if (sachTuyetKy.itemOptions.get(i).optionTemplate.id == 243) {
                            sachTuyetKy.itemOptions.get(i).param = 1000;
                            break;
                        }
                    }
                    player.inventory.gold -= 10_000_000;
                    InventoryService.gI().subQuantityItemsBag(player, cuonSachCu, 10);
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    sendEffectSuccessCombine(player);
                    reOpenItemCombine(player);
                } else {
                    Service.getInstance().sendThongBao(player, "Còn dùng được nên không thể phục hồi");
                    return;
                }
            }
        }
    }
// Gợi ý: thêm field vào Player
// public final Object combineLock = new Object();
// public volatile boolean isCombining = false;

    private void nangcapskh2(Player player) {
        if (player == null) {
            return;
        }

        final Lock lk = lockOf(player);
        if (!lk.tryLock()) {
            Service.getInstance().sendThongBao(player, "Đang có thao tác ghép khác, vui lòng thử lại...");
            return;
        }
        try {
            // ===== B1: check input =====
            if (player.combineNew == null || player.combineNew.itemsCombine == null
                    || player.combineNew.itemsCombine.size() != 5) {
                Service.getInstance().sendThongBao(player, "Cần đúng 5 món để nâng cấp!");
                return;
            }

            // Sao chép reference (không deep copy)
            final List<Item> picks = new ArrayList<>(player.combineNew.itemsCombine);

            // (1) Chống chọn trùng cùng một object (theo reference)
            Set<Item> uniqueRef = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Item it : picks) {
                if (it == null || !uniqueRef.add(it)) {
                    Service.getInstance().sendThongBao(player, "Không được chọn trùng một món!");
                    return;
                }
            }

            // (2) Map mỗi item đã chọn -> index trong túi, đảm bảo thuộc bag & slot khác nhau
            Map<Item, Integer> bagIndexOfPick = new IdentityHashMap<>();
            for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
                Item bagIt = player.inventory.itemsBag.get(i);
                if (bagIt == null) {
                    continue;
                }
                for (Item pick : picks) {
                    if (bagIt == pick) { // so sánh theo reference
                        if (bagIndexOfPick.putIfAbsent(pick, i) != null) {
                            Service.getInstance().sendThongBao(player, "Phát hiện trùng slot túi!");
                            return;
                        }
                    }
                }
            }
            if (bagIndexOfPick.size() != 5) {
                Service.getInstance().sendThongBao(player, "Các món phải lấy trực tiếp từ túi đồ!");
                return;
            }

            // ===== B2: tìm base có SKH + level =====
            Item base = null;
            ItemOption skhOnBase = null;
            int opId = -1, level = -1;
            for (Item it : picks) {
                if (it.itemOptions == null) {
                    continue;
                }
                for (ItemOption io : it.itemOptions) {
                    if (io == null || io.optionTemplate == null) {
                        continue;
                    }
                    int id = io.optionTemplate.id;
                    if (id >= 136 && id <= 144) {
                        base = it;
                        opId = id;
                        level = 0;
                        skhOnBase = io;
                        break;
                    }
                    if (id >= 211 && id <= 219) {
                        base = it;
                        opId = id;
                        level = 1;
                        skhOnBase = io;
                        break;
                    }
                    if (id >= 220 && id <= 228) {
                        base = it;
                        opId = id;
                        level = 2;
                        skhOnBase = io;
                        break;
                    }
                }
                if (base != null) {
                    break;
                }
            }
            if (base == null || opId == -1) {
                Service.getInstance().sendThongBao(player, "Không tìm thấy trang bị SKH!");
                return;
            }
            if (level == 2) {
                Service.getInstance().sendThongBao(player, "SKH đã tối đa (LV2).");
                return;
            }

            // ===== B3: 4 món còn lại phải cùng template & cùng SKH =====
            int templateId = base.template.id;
            for (Item it : picks) {
                if (it == base) {
                    continue;
                }
                if (it.template == null || it.template.id != templateId) {
                    Service.getInstance().sendThongBao(player, "5 món phải cùng loại!");
                    return;
                }
                boolean ok = false;
                if (it.itemOptions != null) {
                    for (ItemOption io : it.itemOptions) {
                        if (io != null && io.optionTemplate != null && io.optionTemplate.id == opId) {
                            ok = true;
                            break;
                        }
                    }
                }
                if (!ok) {
                    Service.getInstance().sendThongBao(player, "5 món phải cùng SKH!");
                    return;
                }
            }

            // ===== B4: check nguyên liệu =====
            final int RUBY_COST = 2000;
            final int TOKEN_ID = 1559, TOKEN_NEED = 20;
            final int STONE_ID = 1557, STONE_NEED = 99;
            final int PROTECT_ID = 1556, PROTECT_NEED = 10;

            if (player.inventory.ruby < RUBY_COST) {
                Service.getInstance().sendThongBao(player, "Không đủ Hồng ngọc!");
                return;
            }

            int tokenTotal = 0, stoneTotal = 0, protectTotal = 0;
            for (Item it : player.inventory.itemsBag) {
                if (it == null || it.template == null) {
                    continue;
                }
                if (it.template.id == TOKEN_ID) {
                    tokenTotal += it.quantity;
                }
                if (it.template.id == STONE_ID) {
                    stoneTotal += it.quantity;
                }
                if (it.template.id == PROTECT_ID) {
                    protectTotal += it.quantity;
                }
            }
            if (tokenTotal < TOKEN_NEED) {
                Service.getInstance().sendThongBao(player, "Thiếu Token!");
                return;
            }
            if (stoneTotal < STONE_NEED) {
                Service.getInstance().sendThongBao(player, "Thiếu Đá nâng cấp!");
                return;
            }
            boolean hasProtect = protectTotal >= PROTECT_NEED;

            // ===== B5: xác suất + mapping =====
            boolean success = Util.isTrue(25, 100);
            int newOpId = -1;
            switch (opId) {
                // LV0 -> LV1
                case 136:
                    newOpId = 211;
                    break;
                case 137:
                    newOpId = 212;
                    break;
                case 138:
                    newOpId = 213;
                    break;
                case 139:
                    newOpId = 214;
                    break;
                case 140:
                    newOpId = 215;
                    break;
                case 141:
                    newOpId = 216;
                    break;
                case 142:
                    newOpId = 217;
                    break;
                case 143:
                    newOpId = 218;
                    break;
                case 144:
                    newOpId = 219;
                    break;
                // LV1 -> LV2
                case 211:
                    newOpId = 220;
                    break;
                case 212:
                    newOpId = 221;
                    break;
                case 213:
                    newOpId = 222;
                    break;
                case 214:
                    newOpId = 223;
                    break;
                case 215:
                    newOpId = 224;
                    break;
                case 216:
                    newOpId = 225;
                    break;
                case 217:
                    newOpId = 226;
                    break;
                case 218:
                    newOpId = 227;
                    break;
                case 219:
                    newOpId = 228;
                    break;
            }

            // Helper: trừ vật phẩm theo ID an toàn từ túi (tiêu thụ thật)
            java.util.function.BiFunction<Integer, Integer, Boolean> subById = (id, needQty) -> {
                int need = needQty;
                for (Item it : player.inventory.itemsBag) {
                    if (need == 0) {
                        break;
                    }
                    if (it != null && it.template != null && it.template.id == id && it.quantity > 0) {
                        int take = Math.min(need, it.quantity);
                        it.quantity -= take;
                        need -= take;
                        if (it.quantity <= 0) {
                            // làm rỗng slot để tránh ghost
                            it.itemOptions.clear();
                            it.createTime = 0;
                            it.template = null;
                        }
                    }
                }
                return need == 0;
            };

            // Helper: xoá theo index slot (không remove() list, chỉ rỗng slot)
            java.util.function.Consumer<Collection<Integer>> deleteSlots = (idxs) -> {
                for (int idx : idxs) {
                    if (idx >= 0 && idx < player.inventory.itemsBag.size()) {
                        Item slot = player.inventory.itemsBag.get(idx);
                        if (slot != null) {
                            slot.itemOptions.clear();
                            slot.quantity = 0;
                            slot.createTime = 0;
                            slot.template = null;
                        }
                    }
                }
            };

            // Tập index 4 món còn lại + index base
            int baseIdx = bagIndexOfPick.get(base);
            List<Integer> otherIdx = new ArrayList<>();
            for (Item it : picks) {
                if (it != base) {
                    otherIdx.add(bagIndexOfPick.get(it));
                }
            }

            // ===== B6: xử lý =====
            if (success && newOpId != -1) {
                // 1) Trừ nguyên liệu (token + stone) trước, nếu fail thì không thay đổi gì
                if (!subById.apply(TOKEN_ID, TOKEN_NEED) || !subById.apply(STONE_ID, STONE_NEED)) {
                    Service.getInstance().sendThongBao(player, "Kho nguyên liệu đã thay đổi, vui lòng thử lại!");
                    return;
                }
                // 2) Trừ ruby
                player.inventory.ruby -= RUBY_COST;

                // 3) Nâng cấp op của BASE (tại đúng slot trong bag)
                Item baseInBag = player.inventory.itemsBag.get(baseIdx);
                if (baseInBag != base || baseInBag == null || baseInBag.itemOptions == null) {
                    Service.getInstance().sendThongBao(player, "Slot trang bị đã thay đổi, hủy thao tác!");
                    return;
                }

                int oldParam = (skhOnBase != null ? skhOnBase.param : 1);
                for (Iterator<ItemOption> it = baseInBag.itemOptions.iterator(); it.hasNext();) {
                    ItemOption io = it.next();
                    if (io != null && io.optionTemplate != null && io.optionTemplate.id == opId) {
                        it.remove();
                    }
                }
                baseInBag.itemOptions.add(new ItemOption((short) newOpId, oldParam));

                // 4) Xoá 4 món còn lại theo INDEX
                deleteSlots.accept(otherIdx);

                sendEffectSuccessCombine(player);
                Service.getInstance().sendThongBao(player, "✅ Thành công! SKH đã nâng cấp.");
            } else {
                // ===== Thất bại =====
                if (hasProtect) {
                    // Bảo vệ: trừ bảo vệ + token + stone, KHÔNG xoá item
                    if (!subById.apply(PROTECT_ID, PROTECT_NEED)
                            || !subById.apply(TOKEN_ID, TOKEN_NEED)
                            || !subById.apply(STONE_ID, STONE_NEED)) {
                        Service.getInstance().sendThongBao(player, "Kho nguyên liệu đã thay đổi, vui lòng thử lại!");
                        return;
                    }
                    player.inventory.ruby -= RUBY_COST;
                    Service.getInstance().sendThongBao(player, "❗ Thất bại nhưng đã bảo vệ trang bị!");
                } else {
                    // Không bảo vệ: trừ token + stone + xoá cả 5 món
                    if (!subById.apply(TOKEN_ID, TOKEN_NEED) || !subById.apply(STONE_ID, STONE_NEED)) {
                        Service.getInstance().sendThongBao(player, "Kho nguyên liệu đã thay đổi, vui lòng thử lại!");
                        return;
                    }
                    player.inventory.ruby -= RUBY_COST;

                    // Xoá cả 5 món theo INDEX
                    deleteSlots.accept(otherIdx);
                    deleteSlots.accept(Collections.singleton(baseIdx));

                    Service.getInstance().sendThongBao(player, "❌ Thất bại, mất toàn bộ trang bị!");
                }
            }

            // ===== B7: đồng bộ + clear slot combine để khỏi chạy lại bằng ref cũ =====
            player.combineNew.itemsCombine.clear();
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            reOpenItemCombine(player);

        } catch (Exception e) {
            e.printStackTrace();
            Service.getInstance().sendThongBao(player, "Có lỗi khi nâng cấp!");
        } finally {
            lk.unlock();
        }
    }

    private void phanRaSach(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item cuonSachCu = ItemService.gI().createNewItem((short) 1509, 5);
            int goldPhanra = 10_000_000;
            Item sachTuyetKy = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (issachTuyetKy(item)) {
                    sachTuyetKy = item;
                }
            }
            if (sachTuyetKy != null) {
                int luotTay = 0;
                ItemOption optionLevel = null;
                for (ItemOption io : sachTuyetKy.itemOptions) {
                    if (io.optionTemplate.id == 242) {
                        luotTay = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                if (player.inventory.gold < goldPhanra) {
                    Service.getInstance().sendThongBao(player, "Không có tiền mà đòi phân rã à");
                    return;
                }
                if (luotTay == 0) {

                    player.inventory.gold -= goldPhanra;
                    InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                    InventoryService.gI().addItemBag(player, cuonSachCu, 999);
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    sendEffectSuccessCombine(player);
                    reOpenItemCombine(player);

                } else {
                    Service.getInstance().sendThongBao(player, "Còn dùng được phân rã ăn cứt à");
                    return;
                }
            }
        }
    }

    private void taySach(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item sachTuyetKy = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (issachTuyetKy(item)) {
                    sachTuyetKy = item;
                }
            }
            if (sachTuyetKy != null) {
                int luotTay = 0;
                ItemOption optionLevel = null;
                for (ItemOption io : sachTuyetKy.itemOptions) {
                    if (io.optionTemplate.id == 242) {
                        luotTay = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                if (luotTay == 0) {
                    Service.getInstance().sendThongBao(player, "Còn cái nịt mà tẩy");
                    return;
                }
                Item sachTuyetKy_2 = ItemService.gI().createNewItem((short) sachTuyetKy.template.id);
                if (checkHaveOption(sachTuyetKy, 0, 241)) {
                    Service.getInstance().sendThongBao(player, "Còn cái nịt mà tẩy");
                    return;
                }
                int tyle = new Random().nextInt(10);
                for (int i = 1; i < sachTuyetKy.itemOptions.size(); i++) {
                    if (sachTuyetKy.itemOptions.get(i).optionTemplate.id == 242) {
                        sachTuyetKy.itemOptions.get(i).param -= 1;
                    }
                }
                sachTuyetKy_2.itemOptions.add(new ItemOption(241, 0));
                for (int i = 1; i < sachTuyetKy.itemOptions.size(); i++) {
                    sachTuyetKy_2.itemOptions.add(new ItemOption(sachTuyetKy.itemOptions.get(i).optionTemplate.id, sachTuyetKy.itemOptions.get(i).param));
                }
                sendEffectSuccessCombine(player);
                InventoryService.gI().addItemBag(player, sachTuyetKy_2, 1);
                InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                InventoryService.gI().sendItemBags(player);
                reOpenItemCombine(player);
            }
        }
    }

    private boolean checkHaveOption(Item item, int viTriOption, int idOption) {
        if (item != null && item.isNotNullItem()) {
            if (item.itemOptions.get(viTriOption).optionTemplate.id == idOption) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // END _ SÁCH TUYỆT KỸ
    //    private void phanradothanlinh(Player player) {
//        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
//            if (!player.combineNew.itemsCombine.isEmpty()) {
//                Item item = player.combineNew.itemsCombine.get(0);
//                if (item != null && item.isNotNullItem() && (item.template.id > 0 && item.template.id <= 3) && item.quantity >= 1) {
//                    Item nr = ItemService.gI().createNewItem((short) (item.template.id - 78));
//                    InventoryService.gI().addItemBag(player, nr, 0);
//                    InventoryService.gI().subQuantityItemsBag(player, item, 1);
//                    InventoryService.gI().sendItemBags(player);
//                    reOpenItemCombine(player);
//                    sendEffectCombineDB(player, item.template.iconID);
//                    Service.getInstance().sendThongBao(player, "Đã nhận được 1 điểm");
//
//                }
//            }
//        }
//    }
    private void moChiSoBongTai2345(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            int gold = player.combineNew.goldCombine;
            if (player.inventory.gold < gold) {
                Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }
            int ruby = player.combineNew.gemCombine;
            if (player.inventory.ruby < ruby) {
                Service.getInstance().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item BongTai = null;
            Item ManhHon = null;
            Item DaXanhLam = null;
            for (Item item : player.combineNew.itemsCombine) {
                switch (item.template.id) {
                    case 1550:
                        BongTai = item;
                        break;
                    case 1129:
                        BongTai = item;
                        break;
                    case 1165:
                        BongTai = item;
                        break;
                    case 921:
                        BongTai = item;
                        break;
                    case 934:
                        ManhHon = item;
                        break;
                    case 935:
                        DaXanhLam = item;
                        break;
                    default:
                        break;
                }
            }
            if (BongTai != null && ManhHon != null && DaXanhLam != null && DaXanhLam.quantity >= 1 && ManhHon.quantity >= 99) {
                player.inventory.gold -= gold;
                player.inventory.ruby -= ruby;
                InventoryService.gI().subQuantityItemsBag(player, ManhHon, 99);
                InventoryService.gI().subQuantityItemsBag(player, DaXanhLam, 1);
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    BongTai.itemOptions.clear();
                    BongTai.itemOptions.add(new ItemOption(72, 2));
                    int rdUp = Util.nextInt(0, 7);
                    switch (rdUp) {
                        case 0:
                            BongTai.itemOptions.add(new ItemOption(50, (BongTai.template.id == 921 ? Util.nextInt(1, 3) : BongTai.template.id == 1165 ? Util.nextInt(3, 6) : BongTai.template.id == 1129 ? Util.nextInt(5, 7) : Util.nextInt(7, 10))));
                            break;
                        case 1:
                            BongTai.itemOptions.add(new ItemOption(77, (BongTai.template.id == 921 ? Util.nextInt(1, 3) : BongTai.template.id == 1165 ? Util.nextInt(3, 6) : BongTai.template.id == 1129 ? Util.nextInt(5, 7) : Util.nextInt(7, 10))));
                            break;
                        case 2:
                            BongTai.itemOptions.add(new ItemOption(103, (BongTai.template.id == 921 ? Util.nextInt(1, 3) : BongTai.template.id == 1165 ? Util.nextInt(3, 6) : BongTai.template.id == 1129 ? Util.nextInt(5, 7) : Util.nextInt(7, 10))));
                            break;
                        case 3:
                            BongTai.itemOptions.add(new ItemOption(108, (BongTai.template.id == 921 ? Util.nextInt(1, 3) : BongTai.template.id == 1165 ? Util.nextInt(3, 6) : BongTai.template.id == 1129 ? Util.nextInt(5, 7) : Util.nextInt(7, 10))));
                            break;
                        case 4:
                            BongTai.itemOptions.add(new ItemOption(94, (BongTai.template.id == 921 ? Util.nextInt(1, 3) : BongTai.template.id == 1165 ? Util.nextInt(3, 6) : BongTai.template.id == 1129 ? Util.nextInt(5, 7) : Util.nextInt(7, 10))));
                            break;
                        case 5:
                            BongTai.itemOptions.add(new ItemOption(14, (BongTai.template.id == 921 ? Util.nextInt(1, 3) : BongTai.template.id == 1165 ? Util.nextInt(3, 6) : BongTai.template.id == 1129 ? Util.nextInt(5, 7) : Util.nextInt(7, 10))));
                            break;
                        case 6:
                            BongTai.itemOptions.add(new ItemOption(80, (BongTai.template.id == 921 ? Util.nextInt(1, 3) : BongTai.template.id == 1165 ? Util.nextInt(3, 6) : BongTai.template.id == 1129 ? Util.nextInt(5, 7) : Util.nextInt(7, 10))));
                            break;
                        case 7:
                            BongTai.itemOptions.add(new ItemOption(81, (BongTai.template.id == 921 ? Util.nextInt(1, 3) : BongTai.template.id == 1165 ? Util.nextInt(10, 17) : BongTai.template.id == 1129 ? Util.nextInt(15, 20) : Util.nextInt(7, 10))));
                            break;
                        default:
                            break;
                    }
                    sendEffectSuccessCombine(player);
                } else {
                    sendEffectFailCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void nangCapBongTai(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int gold = player.combineNew.goldCombine;
            if (player.inventory.gold < gold) {
                Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }

            int gem = player.combineNew.gemCombine;
            if (player.inventory.ruby < gem) {
                Service.getInstance().sendThongBao(player, "Không đủ Hồng ngọc để thực hiện");
                return;
            }

            Item bongTai = null;
            Item manhVo = null;
            Item bongTai5 = null;
            Item manhVo5 = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.id == 454) {
                    bongTai = item;
                } else if (item.template.id == 933) {
                    manhVo = item;
                } else if (item.template.id == 1549) {
                    manhVo5 = item;
                } else if (item.template.id == 921) {
                    bongTai = item;
                } else if (item.template.id == 1165) {
                    bongTai = item;
                } else if (item.template.id == 1129) {
                    bongTai5 = item;
                }
            }

            if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 454) {
                Item findItemBag = InventoryService.gI().findItemBagByTemp(player, 921); //Khóa btc2
                if (findItemBag != null) {
                    Service.getInstance().sendThongBao(player, "Ngươi đã có bông tai Porata cấp 2 trong hàng trang rồi, không thể nâng cấp nữa.");
                    return;
                }
                player.inventory.gold -= gold;
                player.inventory.ruby -= gem;
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    InventoryService.gI().subQuantityItemsBag(player, manhVo, 9999);
                    bongTai.template = ItemService.gI().getTemplate(921);
                    sendEffectSuccessCombine(player);
                } else {
                    InventoryService.gI().subQuantityItemsBag(player, manhVo, 99);
                    sendEffectFailCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            } else if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 921) {
                Item findItemBag = InventoryService.gI().findItemBagByTemp(player, 1165); //Khóa btc2
                if (findItemBag != null) {
                    Service.getInstance().sendThongBao(player, "Ngươi đã có bông tai Porata cấp 3 trong hàng trang rồi, không thể nâng cấp nữa.");
                    return;
                }
                player.inventory.gold -= gold;
                player.inventory.ruby -= gem;
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    InventoryService.gI().subQuantityItemsBag(player, manhVo, 9999);
                    bongTai.template = ItemService.gI().getTemplate(1165);
                    sendEffectSuccessCombine(player);
                } else {
                    InventoryService.gI().subQuantityItemsBag(player, manhVo, 99);
                    sendEffectFailCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            } else if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 1165) {
                Item findItemBag = InventoryService.gI().findItemBagByTemp(player, 1129); //Khóa btc2
                if (findItemBag != null) {
                    Service.getInstance().sendThongBao(player, "Ngươi đã có bông tai Porata cấp 4 trong hàng trang rồi, không thể nâng cấp nữa.");
                    return;
                }
                player.inventory.gold -= gold;
                player.inventory.ruby -= gem;
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    InventoryService.gI().subQuantityItemsBag(player, manhVo, 9999);
                    bongTai.template = ItemService.gI().getTemplate(1129);
                    sendEffectSuccessCombine(player);
                } else {
                    InventoryService.gI().subQuantityItemsBag(player, manhVo, 99);
                    sendEffectFailCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            } else if (bongTai5 != null && manhVo5 != null && manhVo5.quantity >= 20000 && bongTai5.template.id == 1129) {
                Item findItemBag = InventoryService.gI().findItemBagByTemp(player, 1550);
                if (findItemBag != null) {
                    Service.getInstance().sendThongBao(player, "Ngươi đã có bông tai Porata cấp 5 trong hàng trang rồi, không thể nâng cấp nữa.");
                    return;
                }
                player.inventory.gold -= gold;
                player.inventory.ruby -= gem;
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    InventoryService.gI().subQuantityItemsBag(player, manhVo5, 20000);
                    bongTai5.template = ItemService.gI().getTemplate(1550);
                    sendEffectSuccessCombine(player);
                } else {
                    InventoryService.gI().subQuantityItemsBag(player, manhVo5, 300);
                    sendEffectFailCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void nangCapChanMenh(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int diem = player.combineNew.DiemNangcap;
            if (player.inventory.ruby < diem) {
                Service.getInstance().sendThongBao(player, "Không đủ Hồng ngọc để thực hiện");
                return;
            }
            Item chanmenh = null;
            Item dahoangkim = null;
            int capbac = 0;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.id == 1318) {
                    dahoangkim = item;
                } else if (item.template.id >= 1300 && item.template.id < 1308) {
                    chanmenh = item;
                    capbac = item.template.id - 1299;
                }
            }
            int soluongda = player.combineNew.DaNangcap;
            if (dahoangkim != null && dahoangkim.quantity >= soluongda) {
                if (chanmenh != null && (chanmenh.template.id >= 1300 && chanmenh.template.id < 1308)) {
                    player.inventory.ruby -= diem;
                    if (Util.isTrue(player.combineNew.TileNangcap, 100)) {
                        InventoryService.gI().subQuantityItemsBag(player, dahoangkim, soluongda);

                        // LƯU LẠI CHỈ SỐ CŨ
                        int oldHP = 0;
                        int oldSD = 0;
                        int oldKI = 0;
                        for (ItemOption option : chanmenh.itemOptions) {
                            if (option.optionTemplate.id == 0) {
                                oldHP = option.param;
                            }
                            if (option.optionTemplate.id == 6) {
                                oldSD = option.param;
                            }
                            if (option.optionTemplate.id == 7) {
                                oldKI = option.param;
                            }
                        }

                        // NÂNG CẤP TEMPLATE
                        chanmenh.template = ItemService.gI().getTemplate(chanmenh.template.id + 1);
                        chanmenh.itemOptions.clear();

                        // CỘNG THÊM CHỈ SỐ MỚI VÀO CHỈ SỐ CŨ
                        chanmenh.itemOptions.add(new ItemOption(0, oldHP + (5 + capbac * 50)));
                        chanmenh.itemOptions.add(new ItemOption(6, oldSD + (7 + capbac * 60)));
                        chanmenh.itemOptions.add(new ItemOption(7, oldKI + (7 + capbac * 60)));
                        chanmenh.itemOptions.add(new ItemOption(30, 1));
                        sendEffectSuccessCombine(player);
                    } else {
                        InventoryService.gI().subQuantityItemsBag(player, dahoangkim, soluongda);
                        sendEffectFailCombine(player);
                    }
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            } else {
                Service.getInstance().sendThongBao(player, "Không đủ Đá Hoàng Kim để thực hiện");
            }
        }
    }

    private void nangCapVatPham(Player player) {
        if (player.combineNew.itemsCombine.size() >= 2 && player.combineNew.itemsCombine.size() < 4) {
            if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type < 5).count() != 1) {
                return;
            }
            if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type == 14).count() != 1) {
                return;
            }
            if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 987).count() != 1) {
                return;//admin
            }
            Item itemDo = null;
            Item itemDNC = null;
            Item itemDBV = null;
            for (int j = 0; j < player.combineNew.itemsCombine.size(); j++) {
                if (player.combineNew.itemsCombine.get(j).isNotNullItem()) {
                    if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.get(j).template.id == 987) {
                        itemDBV = player.combineNew.itemsCombine.get(j);
                        continue;
                    }
                    if (player.combineNew.itemsCombine.get(j).template.type < 5) {
                        itemDo = player.combineNew.itemsCombine.get(j);
                    } else {
                        itemDNC = player.combineNew.itemsCombine.get(j);
                    }
                }
            }
            if (isCoupleItemNangCapCheck(itemDo, itemDNC)) {
                int countDaNangCap = player.combineNew.countDaNangCap;
                int gold = player.combineNew.goldCombine;
                short countDaBaoVe = player.combineNew.countDaBaoVe;
                if (player.inventory.gold < gold) {
                    Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện");
                    return;
                }

                if (itemDNC.quantity < countDaNangCap) {
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (Objects.isNull(itemDBV)) {
                        return;
                    }
                    if (itemDBV.quantity < countDaBaoVe) {
                        return;
                    }
                }

                int level = 0;
                ItemOption optionLevel = null;
                for (ItemOption io : itemDo.itemOptions) {
                    if (io.optionTemplate.id == 72) {
                        level = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                if (level < MAX_LEVEL_ITEM) {
                    player.inventory.gold -= gold;
                    ItemOption option = null;
                    ItemOption option2 = null;
                    for (ItemOption io : itemDo.itemOptions) {
                        if (io.optionTemplate.id == 47
                                || io.optionTemplate.id == 6
                                || io.optionTemplate.id == 0
                                || io.optionTemplate.id == 7
                                || io.optionTemplate.id == 14
                                || io.optionTemplate.id == 22
                                || io.optionTemplate.id == 23) {
                            option = io;
                        } else if (io.optionTemplate.id == 27
                                || io.optionTemplate.id == 28) {
                            option2 = io;
                        }
                    }
                    if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                        option.param += (option.param * 10 / 100);
                        if (option2 != null) {
                            option2.param += (option2.param * 10 / 100);
                        }
                        if (optionLevel == null) {
                            itemDo.itemOptions.add(new ItemOption(72, 1));
                        } else {
                            optionLevel.param++;
                        }
//                        if (optionLevel != null && optionLevel.param >= 5) {
//                            ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa nâng cấp "
//                                    + "thành công " + trangBi.template.name + " lên +" + optionLevel.param);
//                        }
                        sendEffectSuccessCombine(player);
                    } else {
                        if ((level == 2 || level == 4 || level == 6) && (player.combineNew.itemsCombine.size() != 3)) {
                            option.param -= (option.param * 15 / 100);
                            if (option2 != null) {
                                option2.param -= (option2.param * 15 / 100);
                            }
                            optionLevel.param--;
                        }
                        sendEffectFailCombine(player);
                    }
                    if (player.combineNew.itemsCombine.size() == 3) {
                        InventoryService.gI().subQuantityItemsBag(player, itemDBV, countDaBaoVe);
                    }
                    InventoryService.gI().subQuantityItemsBag(player, itemDNC, player.combineNew.countDaNangCap);
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    private void phapsuhoa(Player player) {

        // ===== CHECK Ô TRỐNG =====
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.getInstance().sendThongBao(player,
                    "Hành trang cần ít nhất 1 chỗ trống");
            return;
        }

        // ===== CHECK SỐ ITEM =====
        if (player.combineNew.itemsCombine.size() < 2
                || player.combineNew.itemsCombine.size() > 3) {
            Service.getInstance().sendThongBao(player,
                    "Cần trang bị, Đá Pháp Sư và (tùy chọn) Đá Bảo Vệ");
            return;
        }

        Item trangBi = null;
        Item daPhapSu = null;
        Item daBaoVe = null;

        // ===== PHÂN LOẠI ITEM =====
        for (Item it : player.combineNew.itemsCombine) {
            if (it == null || !it.isNotNullItem()) {
                continue;
            }

            if (it.template.id == 1235) {
                daPhapSu = it;
            } else if (it.template.id == 987) {
                daBaoVe = it;
            } else {
                trangBi = it;
            }
        }

        // ===== VALIDATE =====
        if (trangBi == null || daPhapSu == null || !isTrangBiPhapsu(trangBi)) {
            Service.getInstance().sendThongBao(player,
                    "Nguyên liệu không hợp lệ");
            return;
        }

        // ===== LẤY CẤP HIỆN TẠI (233) =====
        ItemOption capOpt = null;
        int capHienTai = 0;
        for (ItemOption io : trangBi.itemOptions) {
            if (io.optionTemplate.id == 233) {
                capOpt = io;
                capHienTai = io.param;
                break;
            }
        }

        // ===== MAX CẤP 7 =====
        if (capHienTai >= 7) {
            Service.getInstance().sendThongBao(player,
                    "Pháp sư hóa đã đạt cấp cao nhất");
            return;
        }

        int capTiepTheo = capHienTai + 1;

        // ===== TÍNH ĐÁ CẦN =====
        int daCan = 3 * capTiepTheo;
        if (daPhapSu.quantity < daCan) {
            Service.getInstance().sendThongBao(player,
                    "Cần " + daCan + " Đá Pháp Sư Hóa");
            return;
        }

        boolean hasBaoVe = daBaoVe != null && daBaoVe.quantity >= 10;

        // ===== TỈ LỆ THÀNH CÔNG =====
        int successRate = 60 - capHienTai * 10;
        if (successRate < 10) {
            successRate = 10;
        }

        // ===== CÁC MỐC CÓ THỂ XỊT =====
        boolean isRiskLevel
                = capHienTai == 2 || capHienTai == 4 || capHienTai == 6;

        // ===== TRỪ NGUYÊN LIỆU =====
        InventoryService.gI().subQuantityItemsBag(player, daPhapSu, daCan);
        if (hasBaoVe) {
            InventoryService.gI().subQuantityItemsBag(player, daBaoVe, 10);
        }

        boolean success = Util.isTrue(successRate, 100);

        // ===== XÁC ĐỊNH OTP CHÍNH =====
        int type = trangBi.template.type;
        ItemOption mainOpt = null;
        int mainOptionId = -1;

        for (ItemOption io : trangBi.itemOptions) {
            int id = io.optionTemplate.id;

            if (type == 2 && (id == 196 || id == 0)) {
                mainOpt = io;
                mainOptionId = id;
                break;
            }
            if (type == 1 && (id == 6 || id == 22)) {
                mainOpt = io;
                mainOptionId = id;
                break;
            }
            if (type == 3 && (id == 7 || id == 23)) {
                mainOpt = io;
                mainOptionId = id;
                break;
            }
            if (type == 0 && id == 47) {
                mainOpt = io;
                mainOptionId = 47;
                break;
            }
            if (type == 4 && id == 14) {
                mainOpt = io;
                mainOptionId = 14;
                break;
            }
        }

        if (mainOpt == null) {
            Service.getInstance().sendThongBao(player,
                    "Trang bị không hỗ trợ Pháp Sư Hóa");
            return;
        }

        int currentMain = mainOpt.param;

        // ===== OTP PHỤ THEO TYPE =====
        int bonusId = -1;
        int bonusPer = 0;

        if (type == 0) {
            bonusId = 94;
            bonusPer = 2;
        } else if (type == 1) {
            bonusId = 77;
            bonusPer = 5;
        } else if (type == 2) {
            bonusId = 50;
            bonusPer = 3;
        } else if (type == 3) {
            bonusId = 103;
            bonusPer = 5;
        } else if (type == 4) {
            bonusId = 5;
            bonusPer = 3;
        }

        ItemOption bonusOpt = null;
        for (ItemOption io : trangBi.itemOptions) {
            if (io.optionTemplate.id == bonusId) {
                bonusOpt = io;
                break;
            }
        }

        // ===== % CHÍNH =====
        int percentMain = 12 + Math.max(0, capHienTai - 1) * 2;

        // =========================
        // ===== XỬ LÝ KẾT QUẢ =====
        // =========================
        if (success) {

            // ===== TĂNG CẤP =====
            if (capOpt != null) {
                capOpt.param++;
            } else {
                trangBi.itemOptions.add(new ItemOption(233, 1));
            }

            // ===== CỘNG OTP CHÍNH =====
            int addMain = (currentMain * percentMain) / 100;
            if (addMain < 1) {
                addMain = 1;
            }
            mainOpt.param += addMain;

            // đưa OTP chính lên đầu
            trangBi.itemOptions.remove(mainOpt);
            trangBi.itemOptions.add(0, mainOpt);

            // ===== CỘNG OTP PHỤ =====
            if (bonusPer > 0) {
                if (bonusOpt != null) {
                    bonusOpt.param += bonusPer;
                } else {
                    trangBi.itemOptions.add(new ItemOption(bonusId, bonusPer));
                }
            }

            sendEffectSuccessCombine(player);

        } else {

            // ===== FAIL =====
            if (isRiskLevel && !hasBaoVe && capHienTai > 0) {

                // ===== TỤT CẤP =====
                if (capOpt != null && capOpt.param > 0) {
                    capOpt.param--;
                }

                // ===== TRỪ OTP PHỤ =====
                if (bonusOpt != null) {
                    bonusOpt.param -= bonusPer;
                    if (bonusOpt.param < 0) {
                        bonusOpt.param = 0;
                    }
                    if (bonusOpt.param == 0) {
                        trangBi.itemOptions.remove(bonusOpt);
                    }
                }

                // ===== TRỪ OTP CHÍNH =====
                int percentTru = percentMain + 5;
                int subMain = (mainOpt.param * percentTru) / 100;
                if (subMain < 1) {
                    subMain = 1;
                }
                mainOpt.param -= subMain;
                if (mainOpt.param < 0) {
                    mainOpt.param = 0;
                }

                // đưa OTP chính lên đầu
                trangBi.itemOptions.remove(mainOpt);
                trangBi.itemOptions.add(0, mainOpt);
            }

            sendEffectFailCombine(player);
        }

        InventoryService.gI().sendItemBags(player);
        reOpenItemCombine(player);
    }

    private void tayphapsu(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (!player.combineNew.itemsCombine.isEmpty()) {
                Item item = player.combineNew.itemsCombine.get(0);
                Item dangusac = player.combineNew.itemsCombine.get(1);
                ItemOption optionStar = null;

                for (ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 229 || io.optionTemplate.id == 230 || io.optionTemplate.id == 231 || io.optionTemplate.id == 232 || io.optionTemplate.id == 233) {
                        optionStar = io;
                        break;
                    }
                }

                if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && (dangusac.template.id == 1236) && dangusac.quantity >= 1) {
                    if (optionStar == null) {
                        Service.getInstance().sendThongBao(player, "Có gì đâu mà tẩy !!!");
                    } else {

                        if (item.itemOptions != null) {

                            Iterator<ItemOption> iterator = item.itemOptions.iterator();
                            while (iterator.hasNext()) {
                                ItemOption ioo = iterator.next();
                                if (ioo.optionTemplate.id == 229 || ioo.optionTemplate.id == 230 || ioo.optionTemplate.id == 231 || ioo.optionTemplate.id == 232 || ioo.optionTemplate.id == 233) {
                                    iterator.remove();
                                }
                            }

                        }
                        //item.itemOptions.add(new ItemOption(73 , 1));  
                        sendEffectSuccessCombine(player);
                        InventoryService.gI().subQuantityItemsBag(player, dangusac, 1);
                        InventoryService.gI().sendItemBags(player);
                        reOpenItemCombine(player);

                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Thiếu vật phẩm gòi !!!");
                }

            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * r
     * Hiệu ứng mở item
     *
     * @param player
     * @param icon1
     * @param icon2
     */
    public void sendEffectOpenItem(Player player, short icon1, short icon2) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_ITEM);
            msg.writer().writeShort(icon1);
            msg.writer().writeShort(icon2);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public boolean isTrangBiKham(Item item) {
        if (item == null) {
            return false;
        }
        // type 0,1,2,3,4 mới được đục lỗ/khảm
        return item.template.type >= 0 && item.template.type <= 4;
    }

    /**
     * Hiệu ứng đập đồ thành công
     *
     * @param player
     */
    private void sendEffectSuccessCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_SUCCESS);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    /**
     * Hiệu ứng đập đồ thất bại
     *
     * @param player
     */
    private void sendEffectFailCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_FAIL);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    /**
     * Gửi lại danh sách đồ trong tab combine
     *
     * @param player
     */
    private void reOpenItemCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(REOPEN_TAB_COMBINE);
            msg.writer().writeByte(player.combineNew.itemsCombine.size());
            for (Item it : player.combineNew.itemsCombine) {
                for (int j = 0; j < player.inventory.itemsBag.size(); j++) {
                    if (it == player.inventory.itemsBag.get(j)) {
                        msg.writer().writeByte(j);
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    /**
     * Hiệu ứng ghép ngọc rồng
     *
     * @param player
     * @param icon
     */
    private void sendEffectCombineDB(Player player, short icon) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_DRAGON_BALL);
            msg.writer().writeShort(icon);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    //--------------------------------------------------Chân mệnh/////
    private int getDiemNangcapChanmenh(int star) {
        switch (star) {
            case 0:
                return 500;
            case 1:
                return 1000;
            case 2:
                return 2000;
            case 3:
                return 2500;
            case 4:
                return 3000;
            case 5:
                return 3500;
            case 6:
                return 4000;
            case 7:
                return 4500;
        }
        return 0;
    }

    private int getDaNangcapChanmenh(int star) {
        switch (star) {
            case 0:
                return 30;
            case 1:
                return 35;
            case 2:
                return 40;
            case 3:
                return 45;
            case 4:
                return 50;
            case 5:
                return 60;
            case 6:
                return 65;
            case 7:
                return 80;
        }
        return 0;
    }

    private float getTiLeNangcapChanmenh(int star) {
        switch (star) {
            case 0:
                return 60f;
            case 1:
                return 40f;
            case 2:
                return 30f;
            case 3:
                return 20f;
            case 4:
                return 10f;
            case 5:
                return 8f;
            case 6:
                return 4f;
            case 7:
                return 2f;
        }
        return 0;
    }

    //--------------------------------------------------------------------------Ratio, cost combine
    private int getGoldPhaLeHoa(int star) {
        switch (star) {
            case 0:
                return 50000000;
            case 1:
                return 60000000;
            case 2:
                return 70000000;
            case 3:
                return 100000000;
            case 4:
                return 180000000;
            case 5:
                return 200000000;
            case 6:
                return 210000000;
            case 7:
                return 230000000;
            case 8:
                return 250000000;
            case 9:
                return 300000000;
        }
        return 0;
    }

    private float getRatioPhaLeHoa(int star) {
        switch (star) {
            case 0:
                return 50;
            case 1:
                return 20;
            case 2:
                return 10;
            case 3:
                return 5;
            case 4:
                return 3;
            case 5:
                return 2;
            case 6:
                return 1f;
            case 7:
                return 0.5f;
            case 8:
                return 0.2f;
            case 9:
                return 0.1f;
        }

        return 0;
    }

    private int getGemPhaLeHoa(int star) {
        switch (star) {
            case 0:
                return 30;
            case 1:
                return 40;
            case 2:
                return 50;
            case 3:
                return 60;
            case 4:
                return 70;
            case 5:
                return 75;
            case 6:
                return 80;
            case 7:
                return 80;
        }
        return 0;
    }

    private int getGemEpSao(int star) {
        switch (star) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 5;
            case 3:
                return 10;
            case 4:
                return 25;
            case 5:
                return 50;
            case 6:
                return 100;
            case 7:
                return 110;
        }
        return 0;
    }

    private double getTileNangCapDo(int level) {
        switch (level) {
            case 0:
                return 95;
            case 1:
                return 80;
            case 2:
                return 70;
            case 3:
                return 60;
            case 4:
                return 35;
            case 5:
                return 15;
            case 6:
                return 5;
            case 7: // 7 sao
                return 1;
        }
        return 0;
    }

    private int getCountDaNangCapDo(int level) {
        switch (level) {
            case 0:
                return 3;
            case 1:
                return 7;
            case 2:
                return 11;
            case 3:
                return 17;
            case 4:
                return 23;
            case 5:
                return 35;
            case 6:
                return 50;
            case 7:
                return 60;
        }
        return 0;
    }

    private int getCountDaBaoVe(int level) {
        return level + 1;
    }

    private int getGoldNangCapDo(int level) {
        switch (level) {
            case 0:
                return 10000000;
            case 1:
                return 17000000;
            case 2:
                return 30000000;
            case 3:
                return 40000000;
            case 4:
                return 70000000;
            case 5:
                return 80000000;
            case 6:
                return 100000000;
            case 7:
                return 250000000;
        }
        return 0;
    }

    //--------------------------------------------------------------------------check
    private boolean isCoupleItemNangCap(Item item1, Item item2) {
        Item trangBi = null;
        Item daNangCap = null;
        if (item1 != null && item1.isNotNullItem()) {
            if (item1.template.type < 5) {
                trangBi = item1;
            } else if (item1.template.type == 14) {
                daNangCap = item1;
            }
        }
        if (item2 != null && item2.isNotNullItem()) {
            if (item2.template.type < 5) {
                trangBi = item2;
            } else if (item2.template.type == 14) {
                daNangCap = item2;
            }
        }
        if (trangBi != null && daNangCap != null) {
            if (trangBi.template.type == 0 && daNangCap.template.id == 223) {
                return true;
            } else if (trangBi.template.type == 1 && daNangCap.template.id == 222) {
                return true;
            } else if (trangBi.template.type == 2 && daNangCap.template.id == 224) {
                return true;
            } else if (trangBi.template.type == 3 && daNangCap.template.id == 221) {
                return true;
            } else if (trangBi.template.type == 4 && daNangCap.template.id == 220) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isCoupleItemNangCapCheck(Item trangBi, Item daNangCap) {
        if (trangBi != null && daNangCap != null) {
            if (trangBi.template.type == 0 && daNangCap.template.id == 223) {
                return true;
            } else if (trangBi.template.type == 1 && daNangCap.template.id == 222) {
                return true;
            } else if (trangBi.template.type == 2 && daNangCap.template.id == 224) {
                return true;
            } else if (trangBi.template.type == 3 && daNangCap.template.id == 221) {
                return true;
            } else if (trangBi.template.type == 4 && daNangCap.template.id == 220) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean issachTuyetKy(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.type == 35) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDaPhaLe(Item item) {
        return item != null && (item.template.type == 30 || (item.template.id >= 14 && item.template.id <= 20) || (item.template.id >= 1185 && item.template.id <= 1191));
    }

    private boolean isTrangBiPhaLeHoa(Item item) {
        if (item != null && item.isNotNullItem()) {
            if ((item.template.type < 5 || item.template.type == 32 || item.template.type == 4)) {// && !item.isTrangBiHSD()
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isTrangBiAn(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id >= 1048 && item.template.id <= 1062) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isTrangBiPhapsu(Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }

        // Không cho phép trang bị HSD
        if (item.isTrangBiHSD()) {
            return false;
        }

        int type = item.template.type;

        // Chỉ cho phép: Áo, Quần, Găng, Giày, Rada
        return type == 0 // Áo
                || type == 1 // Quần
                || type == 2 // Găng
                || type == 3 // Giày
                || type == 4; // Rada
    }

    private int getParamDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.get(0).param;
        }
        switch (daPhaLe.template.id) {
            case 20:
                return 5; // +5%hp
            case 19:
                return 5; // +5%ki
            case 18:
                return 5; // +5%hp/30s
            case 17:
                return 5; // +5%ki/30s
            case 16:
                return 3; // +3%sđ
            case 15:
                return 2; // +2%giáp
            case 14:
                return 2; // +2%né đòn
            case 1187:
                return 4; // +4%sđ
            case 1185:
                return 2; // +2%cm
            case 1190:
                return 7; // +7%ki
            case 1191:
                return 7; // +7%hp
            default:
                return -1;
        }
    }

    private int getOptionDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.get(0).optionTemplate.id;
        }
        switch (daPhaLe.template.id) {
            case 20:
                return 77;
            case 19:
                return 103;
            case 18:
                return 80;
            case 17:
                return 81;
            case 16:
                return 50;
            case 15:
                return 94;
            case 14:
                return 108;
            case 1187:
                return 50; //sd
            case 1185:
                return 14; //chi mang
            case 1190:
                return 103; //ki
            case 1191:
                return 77; //hp
            default:
                return -1;
        }
    }

    /**
     * Trả về id item c0
     *
     * @param gender
     * @param type
     * @return
     */
    private int getTempIdItemC0(int gender, int type) {
        if (type == 4) {
            return 12;
        }
        switch (gender) {
            case 0:
                switch (type) {
                    case 0:
                        return 0;
                    case 1:
                        return 6;
                    case 2:
                        return 21;
                    case 3:
                        return 27;
                }
                break;
            case 1:
                switch (type) {
                    case 0:
                        return 1;
                    case 1:
                        return 7;
                    case 2:
                        return 22;
                    case 3:
                        return 28;
                }
                break;
            case 2:
                switch (type) {
                    case 0:
                        return 2;
                    case 1:
                        return 8;
                    case 2:
                        return 23;
                    case 3:
                        return 29;
                }
                break;
        }
        return -1;
    }

    //Trả về tên đồ c0
    private String getNameItemC0(int gender, int type) {
        if (type == 4) {
            return "Rada cấp 1";
        }
        switch (gender) {
            case 0:
                switch (type) {
                    case 0:
                        return "Áo vải 3 lỗ";
                    case 1:
                        return "Quần vải đen";
                    case 2:
                        return "Găng thun đen";
                    case 3:
                        return "Giầy nhựa";
                }
                break;
            case 1:
                switch (type) {
                    case 0:
                        return "Áo sợi len";
                    case 1:
                        return "Quần sợi len";
                    case 2:
                        return "Găng sợi len";
                    case 3:
                        return "Giầy sợi len";
                }
                break;
            case 2:
                switch (type) {
                    case 0:
                        return "Áo vải thô";
                    case 1:
                        return "Quần vải thô";
                    case 2:
                        return "Găng vải thô";
                    case 3:
                        return "Giầy vải thô";
                }
                break;
        }
        return "";
    }

    //--------------------------------------------------------------------------Text tab combine
    private String getTextTopTabCombine(int type) {
        switch (type) {
            case EP_SAO_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case NANG_CAP_SKH:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case MO_CS_VY_THU:
                return "Ta sẽ CHO ngươi vỹ thú mạnh nhất ";
            case DUC_LO_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case KHAM_DA_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case PHA_LE_HOA_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị pha lê";
            case AN_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị Ấn";
            case NHAP_NGOC_RONG:
                return "Ta sẽ phù phép\ncho viên Ngọc Rồng cấp thấp\nthành 1 viên Ngọc Rồng cấp cao";
            case NANG_CAP_NRO:
                return "Ta sẽ phù phép\ncho viên Ngọc Rồng cấp thấp\nthành 1 viên Ngọc Rồng cấp cao";
            case NANG_CAP_VAT_PHAM:
                return "Ta sẽ phù phép cho trang bị của ngươi trở lên mạnh mẽ";
            case NANG_CAP_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata của ngươi\nthành Bông tai cấp cao hơn 1 bậc";
            case MO_CHI_SO_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 2,3,4,5 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case PHAN_RA_DO_THAN_LINH:
                return "Ta sẽ phân rã \n  trang bị của người thành điểm!";
            case CHUYEN_HOA_DO_HUY_DIET:
                return "Ta sẽ phân rã \n  trang bị Hủy diệt của ngươi\nthành Phiếu hủy diệt!";
            case PHAN_RA_DO_TS:
                return "Ta sẽ phân rã \n  trang bị Thiên sứ của ngươi\nthành 500 mảnh thiên sứ cùng hệ!";
            case NANG_CAP_DO_TS:
                return "Ta sẽ nâng cấp \n  trang bị của người thành\n đồ thiên sứ!";
            case Nang_Cap_SKH:
                return "Thiên sứ nhờ ta nâng cấp \n  trang bị của người thành\n SKH VIP!";
            case NANG_CAP_SKH_TS:
                return "Thiên sứ nhờ ta nâng cấp \n  trang bị của người thành\n SKH VIP!";
            case NANG_CAP_THAN_LINH:
                return "Ta sẽ nâng cấp \n trang bị Thần linh của ngươi\n thành món Hủy diệt Tương ứng!";
            case PHAP_SU_HOA:
                return "Pháp sư hóa trang bị\nTa sẽ phù phép cho trang bị của ngươi trở lên mạnh mẽ";
            case TAY_PHAP_SU:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở về lúc chưa 'Pháp sư hóa'";
            case NANG_CAP_CHAN_MENH:
                return "Ta sẽ Nâng cấp\nChân Mệnh của ngươi\ncao hơn một bậc";
            case GIA_HAN_VAT_PHAM:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\nthêm hạn sử dụng";
            case DE_TU_VIP:
                return "ta sẽ giúp ngươi có đệ tử mạnh hơn ";
            // START_ SÁCH TUYỆT KỸ //
            case GIAM_DINH_SACH:
                return "Ta sẽ giám định\nSách Tuyệt Kỹ cho ngươi";
            case TAY_SACH:
                return "Ta sẽ phù phép\ntẩy sách đó cho ngươi";
            case NANG_CAP_SACH_TUYET_KY:
                return "Ta sẽ nâng cấp\nSách Tuyệt Kỹ cho ngươi";
            case PHUC_HOI_SACH:
                return "Ta sẽ phục hồi\nsách cho ngươi";
            case PHAN_RA_SACH:
                return "Ta sẽ phân rã\nsách cho ngươi";
            case DAP_THE_CAU_THU:
                return "Ta sẽ giúp ngươi\nnâng cấp thẻ cầu thủ";
            case MO_CS_THE:
                return "Ta sẽ mở chỉ số\ntheo vị trí cầu thủ";
            // END _ SÁCH TUYỆT KỸ //
            default:
                return "";
        }
    }

    private String getTextInfoTabCombine(int type) {
        switch (type) {
            case EP_SAO_TRANG_BI:
                return "Chọn trang bị\n"
                        + "(Áo, quần, găng, giày hoặc rađa) có ô đặt sao pha lê\n"
                        + "Chọn loại sao pha lê\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case DUC_LO_TRANG_BI:
                return "Chọn trang bị\n"
                        + "(Áo, quần, găng, giày hoặc rađa)\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case NANG_CAP_SKH:
                return "Chọn 3 hợp lệ trang bị\n"
                        + "Chọn 'Nâng cấp' để tiếp tục";

            case KHAM_DA_TRANG_BI:
                return "Chọn trang bị\n"
                        + "(Áo, quần, găng, giày\n đã đục lỗ "
                        + ", rađa hoặc Cải trang)\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case MO_CS_VY_THU:
                return "cho vào 1 vỹ thú và chọn nâng cấp";
            case MO_KHOA_GD:
                return "Hãy cho vào đây 1 món đồ GD đã khóa  + đá mở khóa \n ta sẽ mở khóa GD cho ngươi";
            case PHA_LE_HOA_TRANG_BI:
                return "Chọn trang bị\n"
                        + "(Áo, quần, găng, giày\n"
                        + ", rađa hoặc Cải trang)\n"
                        + "Sau đó chọn 'Nâng cấp' \n"
                        + " đồ jren cần có 10 tiền tệ để pha lê hoá";
            case AN_TRANG_BI:
                return "Vào hành trang\n"
                        + "Chọn 1 Trang bị THIÊN SỨ và 99 mảnh Ấn\n"
                        + "Sau đó chọn 'Làm phép'\n"
                        + "-Tinh ấn (5 món +7% chỉ số)\n"
                        + "-Nhật ấn (5 món +15%  chỉ số\n"
                        + "-Nguyệt ấn (5 món +25% chỉ số)";
            case NHAP_NGOC_RONG:
                return "Vào hành trang\n"
                        + "Chọn 7 viên ngọc cùng sao\n"
                        + "Sau đó chọn 'Làm phép'";

            case NANG_CAP_NRO:
                return "Vào hành trang\n"
                        + "Chọn 99 viên từ 1 đến 7 sao\n"
                        + "ta sẽ giúp ngươi có ngọc rồng siêu cấp\n"
                        + "Sau đó chọn 'nâng cấp'";
            case NANG_CAP_VAT_PHAM:
                return "Vào hành trang\n"
                        + "Chọn trang bị\n"
                        + "(Áo, quần, găng, giày hoặc rađa)\n"
                        + "Chọn loại đá để nâng cấp\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case DAP_THE_CAU_THU:
                return "ĐẬP THẺ CẦU THỦ\n"
                        + "• Cho tối đa 6 thẻ cầu thủ \n"
                        + "• Thẻ đầu tiên là thẻ chính sẽ được đập\n"
                        + "• Các thẻ sau là phôi, Over càng cao % càng lớn\n"
                        + "• Chọn xong rồi bấm 'Nâng cấp'";
            case MO_CS_THE:
                return "MỞ CHỈ SỐ CẦU THỦ\n"
                        + "• Cho vào 1 thẻ cầu thủ \n"
                        + "• Thẻ phải có vị trí ST/CAM/CB tương ứng SD/HP/KI\n"
                        + "• Chỉ số được tính: Over * Cấp\n"
                        + "• Sau đó chọn 'Nâng cấp' để mở chỉ số";
            case NANG_CAP_BONG_TAI:
                return "Vào hành trang\n"
                        + "Chọn bông tai Porata 1, 2, 3, 4\n"
                        + "Chọn mảnh bông tai để nâng cấp(Số lượng: 9999)\n"
                        + "Sau đó chọn 'Nâng cấp'\n"
                        + "Nếu thất bại sẽ bị trừ đi 99 Mảnh bông tai\n"
                        + "Sau khi thành công Bông tai của ngươi sẽ tăng 1 bậc";
            case MO_CHI_SO_BONG_TAI:
                return "Vào hành trang\n"
                        + "Chọn bông tai Porata cấp 2,3,4 hoặc 5\n"
                        + "Chọn mảnh hồn bông tai số lượng 99 cái\n"
                        + "và đá xanh lam để nâng cấp\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case PHAN_RA_DO_THAN_LINH:
                return "Vào hành trang\n"
                        + "Chọn trang bị\n"
                        + "(Áo, quần, găng, giày hoặc rađa)\n"
                        + "Chọn loại đá để phân rã\n"
                        + "Sau đó chọn 'Phân Rã'";
            case CHUYEN_HOA_DO_HUY_DIET:
                return "Vào hành trang\n"
                        + "Chọn trang bị\n"
                        + "(Áo, quần, găng, giày hoặc rađa) Hủy diệt\n"
                        + "Sau đó chọn 'Chuyển hóa'";
            case PHAN_RA_DO_TS:
                return "Vào hành trang\n"
                        + "Chọn trang bị\n"
                        + "(Áo, quần, găng, giày hoặc nhẫn) Thiên sứ\n"
                        + "Sau đó chọn 'Chuyển hóa'";
            case NANG_CAP_DO_TS:
                return "Vào hành trang\n"
                        + "Chọn 1 Công thức theo Hành tinh + 1 Đá cầu vòng\n"
                        + " và 999 mảnh thiên sứ\n "
                        + "sẽ cho ra đồ thiên sứ từ 0-15% chỉ số\n"
                        + "(Có tỉ lệ thêm dòng chỉ số ẩn)\n"
                        + "Sau đó chọn 'Nâng Cấp'";
            case Nang_Cap_SKH:
                return "Vào hành trang\n"
                        + "Chọn 3 trang bị Hủy diệt bất kì\n"
                        + "Đồ SKH VIP sẽ cùng loại với đồ Hủy diệt!\n"
                        + "Chọn 'Nâng Cấp'";
            case DE_TU_VIP:
                return "Vào hành trang\n"
                        + "Chọn 3 vật phẩm\n"
                        + "buw\n"
                        + "bill"
                        + "xu";
            case NANG_CAP_SKH_TS:
                return "Vào hành trang\n"
                        + "chọn 6 trang bị có cùng skh \n"
                        + "và 1 món trang bị  thần linh (không phải là skh)\n"
                        + "Sẽ cho ra đồ SKH Thần linh, Hủy diệt, thiên sứ\n"
                        // + "hoặc Thiên sứ ngẫu nhiên"
                        + "Đồ SKH VIP sẽ cùng loại !\n"
                        + "Chọn 'Nâng Cấp'";
            case NANG_CAP_THAN_LINH:
                return "Vào hành trang\n"
                        + "Chọn 1 món Thần linh bất kì\n"
                        + " Đồ Hủy diệt sẽ cùng loại và hành tinh của món đó\n"
                        + "Chọn 'Nâng Cấp'";
            case PHAP_SU_HOA:
                return "Vào hành trang\n"
                        + "Chọn trang bị\n"
                        + "(Pet, VP đeo, Danh hiệu, Linh thú, Cải trang)\n"
                        + "Chọn Đá Pháp Sư\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case TAY_PHAP_SU:
                return "Vào hành trang\n"
                        + "Chọn trang bị\n"
                        + "(Pet, VP đeo, Danh hiệu, Linh thú, Cải trang 'đã Pháp sư hóa')\n"
                        + "Chọn Bùa Tẩy Pháp Sư\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case NANG_CAP_CHAN_MENH:
                return "Vào hành trang\n"
                        + "Chọn Chân mệnh muốn nâng cấp\n"
                        + "Chọn Đá Hoàng Kim\n"
                        + "Sau đó chọn 'Nâng cấp'\n"
                        + "Lưu ý: Khi Nâng cấp Thành công SD tăng 3%, HP,KI tăng 4% chỉ số của cấp trước đó";
            case GIA_HAN_VAT_PHAM:
                return "Vào hành trang\n"
                        + "Chọn 1 trang bị có hạn sử dụng\n"
                        + "Chọn thẻ gia hạn\n"
                        + "Sau đó chọn 'Gia hạn'";
            // START_ SÁCH TUYỆT KỸ //
            case GIAM_DINH_SACH:
                return "Vào hành trang chọn\n1 Sách cần giám định\n"
                        + "Sau đó chọn Bùa Giám định";
            case TAY_SACH:
                return "Vào hành trang chọn\n1 sách cần tẩy";
            case NANG_CAP_SACH_TUYET_KY:
                return "Vào hành trang chọn\nSách Tuyệt Kỹ 1 cần nâng cấp và 10 Kìm bấm giấy";
            case PHUC_HOI_SACH:
                return "Vào hành trang chọn\nCác Sách Tuyệt Kỹ cần phục hồi\n"
                        + "Sau đó chọn 10 Cuốn sách cũ";
            case PHAN_RA_SACH:
                return "Vào hành trang chọn\n1 sách cần phân rã";
            // END _ SÁCH TUYỆT KỸ //
            default:
                return "";
        }
    }

    private int getParamOption(Item item, int optionId) {
        if (item == null || item.itemOptions == null) {
            return 0;
        }
        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == optionId) {
                return io.param;
            }
        }
        return 0;
    }

    private ItemOption getOption(Item item, int optionId) {
        if (item == null || item.itemOptions == null) {
            return null;
        }
        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == optionId) {
                return io;
            }
        }
        return null;
    }

    private boolean hasOption(Item item, int optionId) {
        return getOption(item, optionId) != null;
    }

    private void dapTheCauThu(Player player) {

        if (player.combineNew.itemsCombine == null || player.combineNew.itemsCombine.isEmpty()) {
            Service.getInstance().sendThongBao(player, "Chưa cho thẻ cầu thủ vào");
            return;
        }

        List<Item> items = player.combineNew.itemsCombine;

        // ==== BẮT BUỘC TẤT CẢ PHẢI LÀ TYPE 76 ====
        for (Item it : items) {
            if (it == null || !it.isNotNullItem() || it.template.type != 76) {
                Service.getInstance().sendThongBao(player,
                        "Tất cả vật phẩm phải là thẻ cầu thủ (type 76)");
                return;
            }
        }

        if (items.size() > 6) {
            Service.getInstance().sendThongBao(player, "Chỉ được cho tối đa 6 thẻ cầu thủ");
            return;
        }

        // Thẻ chính
        Item main = items.get(0);
        int overMain = getParamOption(main, 223);
        if (overMain <= 0) {
            Service.getInstance().sendThongBao(player, "Thẻ cầu thủ không hợp lệ (không có Over)");
            return;
        }

        ItemOption optLevel = getOption(main, 72);
        int level = optLevel != null ? optLevel.param : 0;

        if (level >= 5) {
            Service.getInstance().sendThongBao(player, "Thẻ đã đạt cấp tối đa");
            return;
        }

        // ----------------------
        // TỈ LỆ MAX KHI ĐỦ 5 VẠCH PHÔI
        // ----------------------
        int baseRate;
        switch (level) {
            case 0: // 0 -> 1
                baseRate = 100;
                break;
            case 1: // 1 -> 2
                baseRate = 80;
                break;
            case 2: // 2 -> 3
                baseRate = 70;
                break;
            case 3: // 3 -> 4
                baseRate = 60;
                break;
            case 4: // 4 -> 5
                baseRate = 50;
                break;
            default:
                baseRate = 0;
        }

        // ----------------------
        // TÍNH VẠCH PHÔI THEO ROLE
        // ----------------------
        double totalVach = 0.0; // đơn vị: vạch (0..5 mỗi phôi)

        for (int i = 1; i < items.size(); i++) {
            Item phoi = items.get(i);
            int overPhoi = getParamOption(phoi, 223);

            if (overPhoi <= 0) {
                continue;
            }

            totalVach += getVachPhoi(level, overMain, overPhoi);
        }

        // Chuẩn hoá về 0..1 với tối đa 5 vạch
        double totalVachNorm = Math.min(totalVach, 5.0) / 5.0;

        // TỈ LỆ CUỐI: baseRate * (vạch/5)
        double finalRateD = baseRate * totalVachNorm;
        if (finalRateD < 0) {
            finalRateD = 0;
        }
        if (finalRateD > 100) {
            finalRateD = 100;
        }
        int finalRate = (int) Math.round(finalRateD);

        // ----------------------
        // XÓA PHÔI (trừ số lượng trong bag)
        // ----------------------
        for (int i = 1; i < items.size(); i++) {
            Item phoi = items.get(i);
            if (phoi != null && phoi.isNotNullItem()) {
                InventoryService.gI().subQuantityItemsBag(player, phoi, 1);
            }
        }

        // ----------------------
        // TÍNH LẠI BASE OVER
        // ----------------------
        int[] sumInc = {0, 1, 2, 3, 5, 7};
        int baseOver = overMain - sumInc[level];
        if (baseOver < 0) {
            baseOver = 0;
        }

        boolean success = Util.isTrue(finalRate, 100);

        // ---------------------------------------
        //     ⭐⭐ BẮT BUỘC XÓA CÁC OTP CHỈ SỐ ⭐⭐
        // ---------------------------------------
        removeOption(main, 50);
        removeOption(main, 77);
        removeOption(main, 103);

        if (success) {
            int newLevel = level + 1;
            int plusOver = (level <= 2 ? 1 : 2);

            // Cập nhật level
            if (optLevel == null) {
                optLevel = new ItemOption(72, newLevel);
                main.itemOptions.add(optLevel);
            } else {
                optLevel.param = newLevel;
            }

            // Cập nhật Over
            ItemOption optOver = getOption(main, 223);
            if (optOver == null) {
                optOver = new ItemOption(223, overMain + plusOver);
                main.itemOptions.add(optOver);
            } else {
                optOver.param = overMain + plusOver;
            }

            sendEffectSuccessCombine(player);

        } else {

            int newLevel = Util.nextInt(0, level);
            int newOver = baseOver + sumInc[newLevel];

            // giảm cấp
            if (optLevel == null) {
                main.itemOptions.add(new ItemOption(72, newLevel));
            } else {
                optLevel.param = newLevel;
            }

            // giảm Over về đúng base theo newLevel
            ItemOption optOver = getOption(main, 223);
            if (optOver == null) {
                main.itemOptions.add(new ItemOption(223, newOver));
            } else {
                optOver.param = newOver;
            }

            sendEffectFailCombine(player);
        }

        // ============================
        // GIỮ LẠI DUY NHẤT THẺ CHÍNH Ở Ô NÂNG CẤP
        // ============================
        player.combineNew.itemsCombine.clear();
        player.combineNew.itemsCombine.add(main);

        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);
        reOpenItemCombine(player);
    }

    private void moChiSoThe(Player player) {
        if (player.combineNew.itemsCombine == null || player.combineNew.itemsCombine.size() != 1) {
            Service.getInstance().sendThongBao(player, "Cần đúng 1 thẻ cầu thủ");
            return;
        }
        Item main = player.combineNew.itemsCombine.get(0);
        if (main == null || !main.isNotNullItem() || main.template.type != 76) {
            Service.getInstance().sendThongBao(player, "Cần đúng 1 thẻ cầu thủ (type 76)");
            return;
        }

        int over = getParamOption(main, 223);
        int level = getParamOption(main, 72);
        if (over <= 0) {
            Service.getInstance().sendThongBao(player, "Thẻ cầu thủ không hợp lệ (không có Over)");
            return;
        }
        if (level <= 0) {
            Service.getInstance().sendThongBao(player, "Thẻ chưa được nâng cấp, không có chỉ số để mở");
            return;
        }

        int posOtp = -1;
        int statOtp = -1;
        if (hasOption(main, 220)) { // ST
            posOtp = 220;
            statOtp = 50;
        } else if (hasOption(main, 221)) { // CAM
            posOtp = 221;
            statOtp = 77;
        } else if (hasOption(main, 222)) { // CB
            posOtp = 222;
            statOtp = 103;
        } else {
            Service.getInstance().sendThongBao(player, "Thẻ chưa có vị trí ST/CAM/CB (220/221/222)");
            return;
        }

        int param = over * level;
        if (param <= 0) {
            Service.getInstance().sendThongBao(player, "Chỉ số mới không hợp lệ");
            return;
        }

        ItemOption optStat = getOption(main, statOtp);
        if (optStat == null) {
            main.itemOptions.add(new ItemOption(statOtp, param));
        } else {
            optStat.param = param;
        }

        sendEffectSuccessCombine(player);
        Service.getInstance().sendThongBao(player, "Đã cập nhật chỉ số cho thẻ cầu thủ!");

        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);
        reOpenItemCombine(player);
    }

    private String progressBar(int percent) {
        int filled = Math.min(10, Math.max(0, percent / 10));
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            b.append(i < filled ? "■" : "□");
        }
        b.append("] ").append(percent).append("%");
        return b.toString();
    }

    private void removeOption(Item item, int optionId) {
        item.itemOptions.removeIf(op -> op.optionTemplate.id == optionId);
    }

    // ================== BẢNG ROLE TÍNH VẠCH PHÔI THẺ CẦU THỦ ==================
    // Trả về số "vạch" phôi đóng góp cho 1 lần đập (1 vạch = 1.0, tối đa 5.0)
    private double getVachPhoi(int level, int overMain, int overPhoi) {
        int diff = overPhoi - overMain; // >0: phôi hơn over, <0: phôi kém over

        switch (level) {
            case 0: // nâng 0 -> 1
                return getVachLevel0(diff);
            case 1: // nâng 1 -> 2
                return getVachLevel1(diff);
            case 2: // nâng 2 -> 3
                return getVachLevel2(diff);
            case 3: // nâng 3 -> 4
                return getVachLevel3(diff);
            case 4: // nâng 4 -> 5
                return getVachLevel4(diff);
            default:
                return 0.0;
        }
    }

    // -------------- BẢNG ROLE 0 -> 1 --------------
    private double getVachLevel0(int diff) {
        if (diff <= -9) {
            return 1.0 / 5.0;      // Phôi <9 over: 1/5 vạch
        } else if (diff == -8) {
            return 0.30;      // <8 over: 30% 1 vạch
        } else if (diff == -7) {
            return 0.40;      // <7 over: 40%
        } else if (diff == -6) {
            return 0.50;      // <6 over: 1/2
        } else if (diff == -5) {
            return 0.75;      // <5 over: 3/4
        } else if (diff == -4) {
            return 1.0;       // <4 over: 1 vạch
        } else if (diff == -3) {
            return 1.3;       // <3 over: 1.3 vạch
        } else if (diff == -2) {
            return 1.8;       // <2 over: 1.8 vạch
        } else if (diff == -1) {
            return 2.2;       // <1 over: 2.2 vạch
        } else if (diff == 0) {
            return 3.0;       // = over: 3 vạch
        } else if (diff == 1) {
            return 0.70 * 5;  // >1 over: 70% của 5 vạch
        } else if (diff == 2) {
            return 0.85 * 5;  // >2 over: 85% của 5 vạch
        } else /* diff >= 3 */ {
            return 5.0;       // >3 over: 100% của 5 vạch (max)
        }
    }

    // -------------- BẢNG ROLE 1 -> 2 --------------
    private double getVachLevel1(int diff) {
        if (diff <= -8) {
            return 1.0 / 5.0;      // <8 over
        } else if (diff == -7) {
            return 0.33;      // <7 over: 33%
        } else if (diff == -6) {
            return 0.25;      // <6 over: 1/4
        } else if (diff == -5) {
            return 0.50;      // <5 over: 1/2
        } else if (diff == -4) {
            return 0.75;      // <4 over: 3/4
        } else if (diff == -3) {
            return 1.0;       // <3 over: 1 vạch
        } else if (diff == -2) {
            return 1.3;       // <2 over
        } else if (diff == -1) {
            return 1.6;       // <1 over
        } else if (diff == 0) {
            return 2.5;       // = over: 2.5 vạch
        } else if (diff == 1) {
            return 0.70 * 5;  // >1 over
        } else if (diff == 2) {
            return 0.85 * 5;  // >2 over
        } else /* diff >= 3 */ {
            return 5.0;       // >3 over (max)
        }
    }

    // -------------- BẢNG ROLE 2 -> 3 --------------
    private double getVachLevel2(int diff) {
        if (diff <= -7) {
            return 1.0 / 5.0;      // <7 over
        } else if (diff == -6) {
            return 0.25;      // <6 over: 25%
        } else if (diff == -5) {
            return 1.0 / 3.0; // <5 over: 1/3 vạch
        } else if (diff == -4) {
            return 0.50;      // <4 over: 1/2
        } else if (diff == -3) {
            return 0.75;      // <3 over: 3/4
        } else if (diff == -2) {
            return 1.0;       // <2 over
        } else if (diff == -1) {
            return 1.2;       // <1 over
        } else if (diff == 0) {
            return 1.6;       // = over
        } else if (diff == 1) {
            return 2.2;       // >1 over
        } else if (diff == 2) {
            return 3.0;       // >2 over
        } else if (diff == 3) {
            return 4.0;       // >3 over
        } else /* diff >= 4 */ {
            return 5.0;       // >4 over (max)
        }
    }

    // -------------- BẢNG ROLE 3 -> 4 --------------
    private double getVachLevel3(int diff) {
        if (diff <= -6) {
            return 1.0 / 5.0;      // <6 over
        } else if (diff == -5) {
            return 0.25;      // <5 over: 1/4
        } else if (diff == -4) {
            return 0.30;      // <4 over: 30%
        } else if (diff == -3) {
            return 0.40;      // <3 over: 40%
        } else if (diff == -2) {
            return 0.65;      // <2 over: 65%
        } else if (diff == -1) {
            return 0.95;      // <1 over: 95%
        } else if (diff == 0) {
            return 1.2;       // = over
        } else if (diff == 1) {
            return 1.7;       // >1 over
        } else if (diff == 2) {
            return 2.2;       // >2 over
        } else if (diff == 3) {
            return 3.0;       // >3 over
        } else if (diff == 4) {
            return 4.0;       // >4 over
        } else /* diff >= 5 */ {
            return 5.0;       // >5 over (max)
        }
    }

    private void handleConvertToK(Item item) {

        // ===== ATK → K ATK (196) =====
        int atk = 0;
        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 0) {
                atk = io.param;
                break;
            }
        }
        if (atk > 65000) {
            item.itemOptions.removeIf(io -> io.optionTemplate.id == 0 || io.optionTemplate.id == 196);
            item.itemOptions.add(new ItemOption(196, atk / 1000));
        }

        // ===== HP → K HP (22) =====
        int hp = 0;
        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 6) {
                hp = io.param;
                break;
            }
        }
        if (hp > 65000) {
            item.itemOptions.removeIf(io -> io.optionTemplate.id == 6);
            item.itemOptions.add(new ItemOption(22, hp / 1000));
        }

        // ===== KI → K KI (23) =====
        int ki = 0;
        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 7) {
                ki = io.param;
                break;
            }
        }
        if (ki > 65000) {
            item.itemOptions.removeIf(io -> io.optionTemplate.id == 7);
            item.itemOptions.add(new ItemOption(23, ki / 1000));
        }
    }

    // -------------- BẢNG ROLE 4 -> 5 --------------
    private double getVachLevel4(int diff) {
        if (diff <= -5) {
            return 1.0 / 5.0;      // <5 over
        } else if (diff == -4) {
            return 0.25;      // <4 over: 25%
        } else if (diff == -3) {
            return 0.30;      // <3 over: 30%
        } else if (diff == -2) {
            return 0.50;      // <2 over: 50%
        } else if (diff == -1) {
            return 0.75;      // <1 over: 75%
        } else if (diff == 0) {
            return 1.0;       // = over: 1 vạch
        } else if (diff == 1) {
            return 1.25;      // >1 over
        } else if (diff == 2) {
            return 1.33;      // >2 over
        } else if (diff == 3) {
            return 2.45;      // >3 over
        } else if (diff == 4) {
            return 3.65;      // >4 over
        } else if (diff == 5) {
            return 4.3;       // >5 over
        } else /* diff >= 6 */ {
            return 5.0;       // >6 over (max)
        }
    }

}

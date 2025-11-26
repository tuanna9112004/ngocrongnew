package nro.models.player;

import nro.card.Card;
import nro.card.CollectionBook;
import nro.consts.ConstAchive;
import nro.consts.ConstPlayer;
import nro.consts.ConstTask;
import nro.data.DataGame;
import nro.dialog.ConfirmDialog;
import nro.models.clan.Buff;
import nro.models.item.CaiTrang;
import nro.models.item.FlagBag;
import nro.models.boss.event.EscortedBoss;
import nro.models.clan.Clan;
import nro.models.clan.ClanMember;
import nro.models.intrinsic.IntrinsicPlayer;
import nro.models.item.Item;
import nro.models.item.ItemTime;
import nro.models.map.ItemMap;
import nro.models.map.TrapMap;
import nro.models.map.Zone;
import nro.models.map.war.BlackBallWar;
import nro.models.map.mabu.MabuWar;
import nro.models.map.war.NamekBallWar;
import nro.models.mob.MobMe;
import nro.models.npc.specialnpc.MabuEgg;
import nro.models.npc.specialnpc.MagicTree;
import nro.models.pvp.PVP;
import nro.models.skill.PlayerSkill;
import nro.models.task.TaskPlayer;
import nro.server.Client;
import nro.server.Manager;
import nro.server.io.Message;
import nro.server.io.Session;
import nro.services.*;
import nro.services.func.ChangeMapService;
import nro.services.func.CombineNew;
import nro.services.func.PVPServcice;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.Util;
import lombok.Getter;
import lombok.Setter;
import nro.services.ClanService;
import nro.models.player.Family; 
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import nro.consts.ConstNpc;
import nro.models.item.ItemOption;
import nro.models.item.ItemTimeSieuCap;
import nro.models.map.Map;
import nro.models.map.mabu.MabuWar14h;
import nro.models.mob.Mob;
import nro.models.npc.Npc;
import nro.models.phuban.DragonNamecWar.TranhNgoc;
import nro.models.phuban.DragonNamecWar.TranhNgocService;
import nro.models.skill.Skill;
import nro.sendEff.SendEffect;
import static nro.services.func.ChangeMapService.AUTO_SPACE_SHIP;
import nro.services.func.SummonDragon;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class Player {
    public Date firstTimeLogin;
    public int typeBinhDieuChe;
    public int vitriBinhDieuChe;
    public List<PhongThiNghiem_Player> phongThiNghiem = new ArrayList<>();

    public Date weekTimeLogin;

    public RuongSuuTamPlayer ruongSuuTam;
    public byte typeMoRuong;
    public byte active_ruong_suu_tam;

    public List<KhamNgocPlayer> khamNgoc = new ArrayList<>();
    public byte active_kham_ngoc;
    public byte nroKhamNgoc;
    public int idTempNangCap;
    public int slItem;

    public int phutOnline;
    public long lastTimeOnline = System.currentTimeMillis();
    public List<Integer> listOnline = new ArrayList<>();
    public List<Integer> listDiemDanh = new ArrayList<>();

    public int[] list_id_nhan = new int[14];
    public int[] checkNhan_TamBao;
    public int idPhucLoi;
    public int diem_quay;
    public List<Integer> listNhan = new ArrayList<>();
    public int idTamBao;
    public List<Integer> listNhan_TamBao = new ArrayList<>();
    public int[] checkNhan;

    public List<Integer> listPhucLoiNgay = new ArrayList<>();
    public int[] checkPhucLoiNgay;
    ////////////////////////////////
    public int bongtai;
    public int thiensu;
    
    public long lastTimeUpdateBallWar;
    public long lastTimeNotifyTimeHoldBlackBall;
    public long lastTimeHoldBlackBall;
    public int tempIdBlackBallHold = -1;
    public boolean isHoldBlackBall;
    public boolean isHoldNamecBall;
   
    public boolean isHoldNamecBallTranhDoat;
    public int tempIdNamecBallHoldTranhDoat = -1;
    // Chiến Thần của player
    // dữ liệu (level, exp, stage, chỉ số…)
  // thực thể hiển thị trong map

    public long timeoff = 0;
    public boolean isdem = false;
    public byte typetrain;
    public int expoff;
    public boolean istrain;
    public long tuSatCooldownEnd; // thời điểm hết hồi chiêu nội bộ của Tự Sát

    public int sochon;
    public List<Integer> soMayMan = new ArrayList<>();
    public int rankSieuHang;
    public long timesieuhang;
    public boolean isnhanthuong1;
 public Family family;
    public boolean DH1 = false;
    public boolean DH2 = false;
    public boolean DH3 = false;
    public boolean DH4 = false;
    public boolean DH5 = false;
    public boolean isNotifiedFullBag = false;

    public boolean isTitleUse1;
    public long lastTimeTitle1;
    public int IdDanhHieu_1;
    public boolean isTitleUse2;
    public long lastTimeTitle2;
    public int IdDanhHieu_2;
    public boolean isTitleUse3;
    public long lastTimeTitle3;
    public int IdDanhHieu_3;
    public boolean isTitleUse4;
    public long lastTimeTitle4;
    public int IdDanhHieu_4;
    public boolean isTitleUse5;
    public long lastTimeTitle5;
    public int IdDanhHieu_5;
    public int leverPet,LVhopthe,SDhopthe,HPhopthe,KIhopthe;

    public boolean resetdame = false;
    public boolean hitdau = false;
    public long lastTimeDame;
    public long dametong = 0;
    public int chuyensinh;
    public int MaxGoldTradeDay;
    public int chuaco2;
    public int chuaco3;
    public int chuaco4;
    public int levelEnhance;
    public int transformstate;
    public int aura;
    public int mot;
    public int hai;
    public int ba;
    public int bon;
    public int nam;

    public int sau;
    public int bay;
    public int tam;
    public int chin;
    public int muoi;

    public int tongnap;
    public long lastTimeUseOption;
    public int goldTai;
    public int goldXiu;

    public long vangnhat = 0;
    public long hngocnhat = 0;
    public String Hppl = "\n";

    public int server;
    public byte[] buyLimit;

    public PlayerEvent event;
    public List<String> textRuongGo = new ArrayList<>();
    public boolean receivedWoodChest;
    public int goldChallenge;
    public int levelWoodChest;
    public boolean isInvisible;
    public boolean sendMenuGotoNextFloorMabuWar;
    public long lastTimeBabiday;
    public long lastTimeChangeZone;
    public long lastTimeChatGlobal;
    public long lastTimeChatPrivate;
    public long lastTimeChangeMap;
    private Session session;
    public byte countSaveFail;
    public boolean beforeDispose;
    public long buffBossDamageTime; // thời gian buff còn hiệu lực
    public long startCookingBeerTime; // thời gian bắt đầu nấu bia



    public long timeFixInventory;
    public boolean isPet;
    public boolean isBoss;
    public boolean isMiniPet;
    public boolean isBot = false;

    public int playerTradeId = -1;
    public Player playerTrade;

    public int mapIdBeforeLogout;
    public List<Zone> mapBlackBall;
    public Zone zone;
    public Zone mapBeforeCapsule;
    public List<Zone> mapCapsule;
    public Pet pet;
    public MiniPet minipet;

    public MobMe mobMe;
    public Location location;
    public SetClothes setClothes;
    public EffectSkill effectSkill;
    public MabuEgg mabuEgg;
    public TaskPlayer playerTask;
    public ItemTime itemTime;
    public ItemTimeSieuCap itemTimesieucap;
    
    public Fusion fusion;
    public MagicTree magicTree;
    public IntrinsicPlayer playerIntrinsic;
    public Inventory inventory;
    public PlayerSkill playerSkill;
    public CombineNew combineNew;
    public IDMark iDMark;
    public Charms charms;
    public EffectSkin effectSkin;
    public Gift gift;
    public NPoint nPoint;
    public RewardBlackBall rewardBlackBall;
    public EffectFlagBag effectFlagBag;

    public Clan clan;
    public ClanMember clanMember;
// trong class Player
public String cccd;
public long cccdDate;
public String cccdPlace;
public long accountCreatedAt;
// Thêm method này vào class chứa itemsBody

    public ListFriendEnemy<Friend> friends;
    public ListFriendEnemy<Enemy> enemies;

    protected boolean actived = false;
    public boolean loaded;

    public long id;
    public String name;
    public byte gender;
    public boolean isNewMember = true;
    public short head;

    public byte typePk;
    // Thêm vào trong class Player
public boolean isBuffClan = false;


    public long lastTimePickTranhNgoc;
    public int tempIdPickTranhNgoc = -1;
    public boolean isPickTranhNgoc;

    public byte cFlag;
    public long lastTimeChangeFlag;
    public long lastTimeTrade;

    public boolean haveTennisSpaceShip;
    private byte useSpaceShip;

    public boolean isGoHome;

    public boolean justRevived;
    public long lastTimeRevived;
    public boolean immortal;

    public long lastTimeBan;
    public long lastTimeUpdate;
    public boolean isBan;
public long lastHoiSkillTime = 0;
    public SkillSpecial skillSpecial;
    public boolean isGotoFuture;
    public long lastTimeGoToFuture;
    public boolean isgotoPrimaryForest;
    public long lastTimePrimaryForest;

    public boolean isgotoHanhTinhBang;
    public long lastTimeHanhTinhBang;
    
    public boolean isgotoDiaNguc;
    public long lastTimeDiaNguc;

    public boolean isgotoHallowen;
    public long lastTimeHallowen;

    public boolean isGoToBDKB;
    public long lastTimeGoToBDKB;
    public long lastTimeAnXienTrapBDKB;
    private short powerPoint;
    private short percentPowerPont;
    public short body;
    public short leg;

    public boolean PorataVIP;

    public long lastTimePickItem;
    @Setter
    @Getter
    private CollectionBook collectionBook;
    @Getter
    @Setter
    private boolean isSaving, isDisposed;
    @Getter
    @Setter
    private boolean interactWithKarin;
    @Getter
    @Setter
    private EscortedBoss escortedBoss;
    @Setter
    @Getter
    private ConfirmDialog confirmDialog;
    @Getter
    @Setter
    public byte[] rewardLimit;
    @Setter
    @Getter
    private PetFollow petFollow;
    @Setter
    @Getter
    private Buff buff;
    public int[] nangcapDetu;

    public Player() {

        this.listPhucLoiNgay = new ArrayList<>();
        ruongSuuTam = new RuongSuuTamPlayer();
        /////////////////////////
        lastTimeUseOption = System.currentTimeMillis();
        location = new Location();
        nPoint = new NPoint(this);
        inventory = new Inventory(this);
        playerSkill = new PlayerSkill(this);
        setClothes = new SetClothes(this);
        effectSkill = new EffectSkill(this);
        fusion = new Fusion(this);
        playerIntrinsic = new IntrinsicPlayer(this);
        rewardBlackBall = new RewardBlackBall(this);
        effectFlagBag = new EffectFlagBag(this);
        //----------------------------------------------------------------------
        iDMark = new IDMark();
        combineNew = new CombineNew();
        playerTask = new TaskPlayer(this);
        friends = new ListFriendEnemy<>(this);
        enemies = new ListFriendEnemy<>(this);
        itemTime = new ItemTime(this);
        itemTimesieucap = new ItemTimeSieuCap(this);
        charms = new Charms(this);
        gift = new Gift(this);
        effectSkin = new EffectSkin(this);
        skillSpecial = new SkillSpecial(this);
        event = new PlayerEvent(this);
        buyLimit = new byte[13];
        buff = Buff.NONE;
    }
        // Lấy cánh (ô body[5])
   
    
// ==========================================
// Buff Damage với Clan
// ==========================================
public long getDamageWithClan() {
    // base damage của player
    long base = this.nPoint.dame;

    // dame sau khi cộng thêm từ Clan
    long dame = nro.services.ClanService.gI().getClanDamage(this, base);

    return dame;
}
// Thêm method này vào class chứa itemsBody

// ==========================================
// Buff HP với Clan
// ==========================================
public long getHutHPClan() {
    return nro.services.ClanService.gI().getClanHutHP(this);
}

// ==========================================
// Buff KI với Clan
// ==========================================
public long getHutKIClan() {
    return nro.services.ClanService.gI().getClanHutKI(this);
}


    //--------------------------------------------------------------------------
    public short getPowerPoint() {
        return powerPoint;
    }

    public void addPowerPoint(int value) {
        powerPoint += value;
    }

    public short getPercentPowerPont() {
        return percentPowerPont;
    }

    public void addPercentPowerPoint(int value) {
        percentPowerPont += value;
    }

    public void resetPowerPoint() {
        percentPowerPont = 0;
        powerPoint = 0;
    }

    public void setUseSpaceShip(byte useSpaceShip) {
        // 0 - không dùng
        // 1 - tàu vũ trụ theo hành tinh
        // 2 - dịch chuyển tức thời
        // 3 - tàu tenis
        this.useSpaceShip = useSpaceShip;
    }

    public byte getUseSpaceShip() {
        return this.useSpaceShip;
    }

    public boolean isDie() {
        if (this.nPoint != null) {
            return this.nPoint.hp < 1;
        } else {
            return true;
        }
    }

    public boolean check99ThucAnHuyDiet() {
        for (Item item : this.inventory.itemsBag) {
            if (item != null && item.template != null && item.template.id >= 663 && item.template.id <= 667 && item.quantity >= 99) {
                return true;
            }
        }
        return false;
    }

    //--------------------------------------------------------------------------
    public void setSession(Session session) {
        this.session = session;
    }

    public void sendMessage(Message msg) {
        if (this.session != null) {
            session.sendMessage(msg);
        }
    }

    public Session getSession() {
        return this.session;
    }

    public int version() {
        if (this.session == null) {
            return 0;
        }
        return this.session.version;
    }
    public boolean isVersionAbove(int ver) {
         if (this.session == null) {
            return false;
    }
         return this.session.version >= ver;
    }

    public void update() {
        final Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(11);
        if (this.isBot) {
            active();
        }
        if (this != null && this.name != null && !this.beforeDispose && !isBot) {
            try {
                if (this.istrain && !MapService.gI().isMapTrainOff(this, this.zone.map.mapId)) {
                    ChangeMapService.gI().changeMapBySpaceShip(this, MapService.gI().getMapTrainOff(this), -1, 250);
                    congExpOff();
                    this.timeoff = 0;
                }
                if (!isdem && (hour >= 18 || hour < 5)) {
                    SummonDragon.gI().activeNight(this);
                    isdem = true;
                    Service.getInstance().sendThongBao(this, "Chúc bạn buổi tối vui vẻ");
                } else if (isdem && (hour >= 5 && hour < 18)) {
                    SummonDragon.gI().activeDay(this);
                    isdem = false;
                    Service.getInstance().sendThongBao(this, "Chúc bạn buổi sáng tốt lành");
                }
                if (this.isPl()) {
                    if (Util.canDoWithTime(lastTimeOnline, 60000)) {
                        this.phutOnline++;
                        this.lastTimeOnline = System.currentTimeMillis();
                    }
                }
                if (!isBan) {
                    SendEffect.getInstance().update(this);
                    if (nPoint != null) {
                        nPoint.update();
                    }
                    if (fusion != null) {
                        fusion.update();
                    }
                    if (effectSkin != null) {
                        effectSkill.update();
                    }
                    if (mobMe != null) {
                        mobMe.update();
                    }
                    if (effectSkin != null) {
                        effectSkin.update();
                    }
                    if (pet != null) {
                        pet.update();
                    }
                    if (minipet != null) {
                        minipet.update();
                    }
                    if (magicTree != null) {
                        magicTree.update();
                    }
                    if (itemTime != null) {
                        itemTime.update();
                    }
                    if (itemTimesieucap != null) {
                        itemTimesieucap.update();
                    }
                    if (event != null) {
                        event.update();
                    }
                    if (this.isPl()) {
                        MabuWar.gI().update(this);
                        MabuWar14h.gI().update(this);
                        TranhNgoc.gI().update(this);
                    }
                    if (!this.isBoss && !this.isMiniPet) {
                        if (pet != null && this.inventory.itemsBody.get(5).isNotNullItem() && this.pet.inventory.itemsBody.get(5).isNotNullItem()) {
                            if ((this.inventory.itemsBody.get(5).template.id == 1409 && this.pet.inventory.itemsBody.get(5).template.id == 1410)
                                    || (this.inventory.itemsBody.get(5).template.id == 1410 && this.pet.inventory.itemsBody.get(5).template.id == 1409)) {
                                this.PorataVIP = true;
                            } else {
                                this.PorataVIP = false;
                            }
                        } else {
                            this.PorataVIP = false;
                        }
                    }
                    BlackBallWar.gI().update(this);
                    if (!this.isBoss && !this.isPet && !this.isMiniPet) {
                        if (this.server != Manager.SERVER) {
                            PlayerService.gI().banPlayer(this);
                        }
//                        checkLocation();
                        if (Util.canDoWithTime(lastTimeUpdate, 60000)) {
                            this.playerTask.achivements.get(ConstAchive.HOAT_DONG_CHAM_CHI).count++;
                        }
                    }
                    if (isGotoFuture && Util.canDoWithTime(lastTimeGoToFuture, 6000)) {
                        ChangeMapService.gI().changeMapBySpaceShip(this, 102, -1, Util.nextInt(60, 200));
                        this.isGotoFuture = false;
                    }
                    if (isGoToBDKB && Util.canDoWithTime(lastTimeGoToBDKB, 6000)) {
                        ChangeMapService.gI().changeMapBySpaceShip(this, 135, -1, 35);
                        this.isGoToBDKB = false;
                    }
                    if (isgotoPrimaryForest && Util.canDoWithTime(lastTimePrimaryForest, 6000)) {
                        ChangeMapService.gI().changeMap(this, 161, -1, 169, 312);
                        this.isgotoPrimaryForest = false;
                    }
                    if (isgotoHanhTinhBang && Util.canDoWithTime(lastTimeHanhTinhBang, 6000)) {
                        ChangeMapService.gI().changeMap(this, 211, -1, 290, 768);
                        this.isgotoHanhTinhBang = false;
                    }
                    if (isgotoDiaNguc && Util.canDoWithTime(lastTimeDiaNguc, 6000)) {
                        ChangeMapService.gI().changeMap(this, 212, -1, 110, 480);
                        this.isgotoDiaNguc = false;
                    }
                    if (isgotoHallowen && Util.canDoWithTime(lastTimeHallowen, 6000)) {
                        ChangeMapService.gI().changeMap(this, 210, -1, 258, 192);
                        this.isgotoHallowen = false;
                    }
                    if (this.zone != null) {
                        TrapMap trap = this.zone.isInTrap(this);
                        if (trap != null) {
                            trap.doPlayer(this);
                        }
                    }
                    if (Util.canDoWithTime(this.lastTimeDame, 5000) && this.dametong != 0 && this.hitdau == true) {
                        Service.getInstance().sendThongBao(this, "\n|1|DAME/5giây: \b|7|" + Util.powerToStringnew(this.dametong));
                        this.dametong = 0;
                        this.resetdame = true;
                        this.hitdau = false;
                    }
                } else {
                    if (Util.canDoWithTime(lastTimeBan, 5000)) {
                        Client.gI().kickSession(session);
                    }
                }
            } catch (Exception e) {
                Log.error(Player.class, e, "Lỗi tại player: " + this.name);
            }
        }
    }

    private void checkLocation() {
        if (this.location.x > this.zone.map.mapWidth || this.location.x < 0
                || this.location.y > this.zone.map.mapHeight || this.location.y < 0) {
            if (this.inventory.gold >= 500000000) {
                this.inventory.subGold(500000000);
            } else {
                this.inventory.gold = 0;
            }
            PlayerService.gI().sendInfoHpMpMoney(this);
            ChangeMapService.gI().changeMapNonSpaceship(this, this.gender + 21, 400, 336);
            Service.getInstance().sendBigMessage(this, 1139, "|1|Do phát hiện có hành vi bất thường nên\n "
                    + "chúng tôi đã đưa bạn về nhà và xử phạt 500Tr vàng\n"
                    + "|7|nếu còn tiếp tục tái phạm sẽ khóa vĩnh viễn");
        }
    }

    public boolean isPl() {
        return !isPet && !isBoss && !isMiniPet;
    }
    //--------------------------------------------------------------------------
    /*
     * {380, 381, 382}: ht lưỡng long nhất thể xayda trái đất
     * {383, 384, 385}: ht porata xayda trái đất
     * {391, 392, 393}: ht namếc
     * {870, 871, 872}: ht c2 trái đất
     * {873, 874, 875}: ht c2 namếc
     * {867, 878, 869}: ht c2 xayda
     */
    private static final short[][] idOutfitFusion = {
        {380, 381, 382}, {383, 384, 385}, {391, 392, 393},// btc1
        {1204, 1205, 1206}, {1207, 1208, 1209}, {1210, 1211, 1212}, //btc2
        {1375, 1376, 1377}, {1372, 1373, 1374}, {1369, 1370, 1371}, //btc3
        {1255, 1256, 1257}, {1249, 1250, 1251}, {1246, 1247, 1248}, //btc4
        {1802, 1805, 1806}, {1807, 1810, 1811}, {1812, 1815, 1816} //btc5
    };
    
public byte getAura() {
    // Kiểm tra điều kiện cơ bản
    if (!this.isPl() && !this.isBot && !(this instanceof TestDame)) {
        return -1;
    }
    
    // Kiểm tra TOP_PLAYERS
    if (Manager.TOP_PLAYERS.contains(this.name)) {
        return 50;
    }
    
    // Kiểm tra Collection Book
    CollectionBook book = getCollectionBook();
    if (book != null) {
        Card card = book.getCards().stream()
            .filter(t -> t.isUse() && t.getCardTemplate().getAura() != -1)
            .findAny()
            .orElse(null);
        if (card != null) {
            return (byte) card.getCardTemplate().getAura();
        }
    }
    
    // Kiểm tra item body
    Item item = this.inventory.itemsBody.get(5);
    if (item != null && item.isNotNullItem()) {
        switch (item.template.id) {
            case 1583:
                return 14;
            case 1589:
                return 24;
            case 1609: 
                return 32;
          
        }
    }
        if (nPoint.power >= 250000000000L) return 14;   // 250 tỷ
        if (nPoint.power >= 200000000000L) return 56;   // 200 tỷ
        if (nPoint.power >= 150000000000L) return 30;   // 150 tỷ
        if (nPoint.power >= 100000000000L) return 56;   // 100 tỷ
        if (nPoint.power >= 80000000000L) return 24;    // 80 tỷ
        if (nPoint.power >= 50000000000L) return 25;    // 50 tỷ
        if (nPoint.power >= 30000000000L) return 13;    // 30 tỷ
        if (nPoint.power >= 10000000000L) return 10;    // 10 tỷ
        
//       if (this.levelEnhance >= 15) {
//        switch (this.gender) {
//            case 0: return 14; // aura Trái đất
//            case 1: return 14; // aura Namek
//            case 2: return 14; // aura Xayda
//        }
//    }
    
    return -1;
}

    public boolean checkSkinFusion() {
        if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            Short idct = inventory.itemsBody.get(5).template.id;
            if (idct >= 601 && idct <= 603 || idct >= 639 && idct <= 641) {
                return true;
            }
        }
        return false;
    }

    public short getHead() {
        if (this.id == 1000000) {
            return 412;
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        } else if (this.itemTimesieucap.isBienhinh) {
            return 1618;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 412;
        } else if (effectSkill != null && effectSkill.isBinh) {
            return 1321;
        } else if (effectSkin != null && effectSkin.isHoaDa) {
            return 454;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (checkSkinFusion()) {
                CaiTrang ct = Manager.getCaiTrangByItemId(inventory.itemsBody.get(5).template.id);
                return (short) (ct.getID()[0] != -1 ? ct.getID()[0] : inventory.itemsBody.get(5).template.part);
            } else if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA && this.PorataVIP == false) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 && this.PorataVIP == false) {
                return idOutfitFusion[3 + this.gender][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3 && this.PorataVIP == false) {
                return idOutfitFusion[6 + this.gender][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4 && this.PorataVIP == false) {
                return idOutfitFusion[9 + this.gender][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA5 && this.PorataVIP == false) {
                return idOutfitFusion[12 + this.gender][0];
            } else if (this.PorataVIP == true) {
                return 1092;
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            CaiTrang ct = Manager.getCaiTrangByItemId(inventory.itemsBody.get(5).template.id);
            if (checkSkinFusion()) {
                return this.head;
            }
            if (ct != null) {
                return (short) (ct.getID()[0] != -1 ? ct.getID()[0] : inventory.itemsBody.get(5).template.part);
            }
        }
        return this.head;
    }

    public short getBody() {
        if (this.id == 1000000) {
            return 413;
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        } else if (this.itemTimesieucap.isBienhinh) {
            return 1619;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 413;
        } else if (effectSkill != null && effectSkill.isBinh) {
            return 1322;
        } else if (effectSkin != null && effectSkin.isHoaDa) {
            return 455;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (checkSkinFusion()) {
                CaiTrang ct = Manager.getCaiTrangByItemId(inventory.itemsBody.get(5).template.id);
                return (short) ct.getID()[1];
            }
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA && this.PorataVIP == false) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 && this.PorataVIP == false) {
                return idOutfitFusion[3 + this.gender][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3 && this.PorataVIP == false) {
                return idOutfitFusion[6 + this.gender][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4 && this.PorataVIP == false) {
                return idOutfitFusion[9 + this.gender][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA5 && this.PorataVIP == false) {
                return idOutfitFusion[12 + this.gender][1];
            } else if (this.PorataVIP == true) {
                return 1093;
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            CaiTrang ct = Manager.getCaiTrangByItemId(inventory.itemsBody.get(5).template.id);
            if (checkSkinFusion()) {
                if (inventory != null && inventory.itemsBody.get(0).isNotNullItem()) {
                    if (inventory != null && inventory.itemsBody.get(0).isNotNullItem()) {
                        return inventory.itemsBody.get(0).template.part;
                    }
                } else {
                    return (short) (gender == ConstPlayer.NAMEC ? 59 : 57);
                }
            }
            if (ct != null && ct.getID()[1] != -1) {
                return (short) ct.getID()[1];
            }
        }
        if (inventory != null && inventory.itemsBody.get(0).isNotNullItem()) {
            return inventory.itemsBody.get(0).template.part;
        }
        return (short) (gender == ConstPlayer.NAMEC ? 59 : 57);
    }

    public short getLeg() {
        if (this.id == 1000000) {
            return 414;
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        } else if (this.itemTimesieucap.isBienhinh) {
            return 1620;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 414;
        } else if (effectSkill != null && effectSkill.isBinh) {
            return 1323;
        } else if (effectSkin != null && effectSkin.isHoaDa) {
            return 456;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (checkSkinFusion()) {
                CaiTrang ct = Manager.getCaiTrangByItemId(inventory.itemsBody.get(5).template.id);
                return (short) ct.getID()[2];
            }
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA && this.PorataVIP == false) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 && this.PorataVIP == false) {
                return idOutfitFusion[3 + this.gender][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3 && this.PorataVIP == false) {
                return idOutfitFusion[6 + this.gender][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4 && this.PorataVIP == false) {
                return idOutfitFusion[9 + this.gender][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA5 && this.PorataVIP == false) {
                return idOutfitFusion[12 + this.gender][2];
            } else if (this.PorataVIP == true) {
                return 1094;
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            if (checkSkinFusion()) {
                if (inventory != null && inventory.itemsBody.get(1).isNotNullItem()) {
                    return inventory.itemsBody.get(1).template.part;
                } else {
                    return (short) (gender == ConstPlayer.NAMEC ? 60 : 58);
                }
            }
            CaiTrang ct = Manager.getCaiTrangByItemId(inventory.itemsBody.get(5).template.id);
            if (ct != null && ct.getID()[2] != -1) {
                return (short) ct.getID()[2];
            }
        }
        if (inventory != null && inventory.itemsBody.get(1).isNotNullItem()) {
            return inventory.itemsBody.get(1).template.part;
        }
        return (short) (gender == 1 ? 60 : 58);
    }
// Player.java
public short getFlagBag() {
    try {
//        // Ưu tiên các trạng thái đặc biệt
        if (this.levelEnhance >= 15) {
            return 137;
        }
        if (this.isHoldBlackBall) {
            return 31;
        }
        if (this.isHoldNamecBall) {
            return 30;
        }

        // ĐỒNG NHẤT slot kiểm tra: dùng cùng 1 slot index (ví dụ slot 8)
        if (this.inventory != null
                && this.inventory.itemsBody != null
                && this.inventory.itemsBody.size() > 8) {
            Item flagItem = this.inventory.itemsBody.get(8);
            if (flagItem != null && flagItem.isNotNullItem() && flagItem.template != null) {
                FlagBag f = FlagBagService.gI().getFlagBagByName(flagItem.template.name);
                if (f != null) {
                    return (short) f.id;
                }
            }
        }

        // Nhiệm vụ/Clan
        if (TaskService.gI().getIdTask(this) == ConstTask.TASK_3_2) {
            return 28;
        }
        if (this.clan != null) {
            return (short) this.clan.imgId;
        }
    } catch (Exception ignore) {}
    return -1;
}

  public short getMount() {
    
    try {
      //  System.out.println("[Player.getMount] → Kiểm tra itemsBody (Size: " + inventory.itemsBody.size() + ")");
        for (int i = 0; i < inventory.itemsBody.size(); i++) {
            Item item = inventory.itemsBody.get(i);

            if (item == null || !item.isNotNullItem()) {
              //  System.out.println("[Player.getMount]   Slot " + i + ": NULL");
                continue;
            }

           

            // ✅ Ưu tiên: Linh thú / Thú cưỡi / Pet cưỡi
            if (item.template.type == 21 || item.template.type == 72 || item.template.type == 73) {
              //  System.out.println("[Player.getMount] ✅ Tìm thấy LINH THÚ tại slot " + i);
                return (short) item.template.iconID;
            }

            // ✅ Type 24: Cờ / Flag (nếu hợp giới tính)
            if (item.template.type == 24) {
                if (item.template.gender == 3 || item.template.gender == this.gender) {
                  //  System.out.println("[Player.getMount] ✅ Type 24 (Flag): " + item.template.name);
                    return (short) item.template.id;
                }
            }

            // ✅ Type 23: Xe cưỡi
            if (item.template.type == 24) {
                if (item.template.id < 500) {
                  //  System.out.println("[Player.getMount] ✅ Type 23 (Mount): " + item.template.name);
                    return (short) item.template.id;
                } else {
                    Object mount = DataGame.MAP_MOUNT_NUM.get(String.valueOf(item.template.id));
                    if (mount != null) {
                       // System.out.println("[Player.getMount] ✅ Type 23 (Mount map): " + mount);
                        return (short) mount;
                    }
                }
            }
        }

        //System.out.println("[Player.getMount] ⚠ Không tìm thấy mount trong itemsBody");
        return -1;

    } catch (Exception e) {
        e.printStackTrace();
        Log.error(Player.class, e);
        return -1;
    } finally {
        //S/ystem.out.println("[Player.getMount] ========== KẾT THÚC ==========\n");
    }
}

    public Mob mobTarget;

    public long lastTimeTargetMob;

    public long timeTargetMob;

    public long lastTimeAttack;
    public long lastTimeMap;
    public Player isBotAttckBoss;
    public PVP pvp;
    private long lastTimeTargetPlayer;
    private int timeTargetPlayer;

    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(40, 60);
        if (isBot) {
            move = (byte) (move * (byte) 2);
        }
        PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y + (Util.isTrue(3, 10) ? -50 : 0));
    }

    public Mob getMobAttack() {
        if (this.mobTarget != null && (this.mobTarget.isDie() || !this.zone.equals(this.mobTarget.zone))) {
            this.mobTarget = null;
        }
        if (this.mobTarget == null && Util.canDoWithTime(lastTimeTargetMob, timeTargetMob)) {
            this.mobTarget = this.zone.getRandomMobInMap();
            this.lastTimeTargetMob = System.currentTimeMillis();
            this.timeTargetMob = 500;
        }
        return this.mobTarget;
    }

    public void active() {
        if (this.isBot) {
            if (this.pet != null) {
                this.pet.update();
            }
            if (this.isDie()) {
                Service.getInstance().sendMoney(this);
                PlayerService.gI().hoiSinh(this);
                Service.getInstance().hsChar(this, this.nPoint.hpMax, this.nPoint.mpMax);
                PlayerService.gI().sendInfoHpMp(this);
            }
            if (this.nPoint.mp <= this.nPoint.mpMax * 30 / 100) {
                this.nPoint.mp = this.nPoint.mpMax;
            }
            this.BayCs();
            this.AttackBoss();
            this.attack();
        }
    }

    public int getRangeCanAttackWithSkillSelect() {
        int skillId = this.playerSkill.skillSelect.template.id;
        if (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC) {
            return Skill.RANGE_ATTACK_CHIEU_CHUONG;
        } else {
            return Skill.RANGE_ATTACK_CHIEU_DAM;
        }
    }

    public void BayCs() {
        if (this.isBot) {
            this.mobTarget = this.getMobAttack();
            if (Util.canDoWithTime(lastTimeMap, Util.nextInt(60000, 120000))) {
                byte RandomMap = (byte) new Random().nextInt(0, Manager.IdMapSpam.length - 1);
                try {
                    this.lastTimeMap = System.currentTimeMillis();
                    ChangeMapService.gI().changeMap(this, Manager.IdMapSpam[RandomMap], -1, 0, 100);
                } catch (Exception e) {
                }
            }
        }
    }

    public void AttackBoss() {
        try {
            if (this.isBot) {
                List<Player> playersMap = null;
                playersMap = new ArrayList<>(this.zone.getHumanoids()); // Tạo một bản sao của danh sách playersMap

                if (!this.zone.map.isMapOffline) {
                    Iterator<Player> iterator = playersMap.iterator();
                    while (iterator.hasNext()) {
                        Player pl = iterator.next();
                        if (pl != null && pl != this && pl.isBoss && pl.location != null) {
                            this.playerSkill.skillSelect = this.playerSkill.skills.get(1);
                            if (SkillService.gI().canUseSkillWithCooldown(this)) {
                                PlayerService.gI().playerMove(this, pl.location.x + Util.nextInt(-60, 60),
                                        pl.location.y);
                                SkillService.gI().useSkill(this, pl, null, null);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public Player getBotAttackBOSS() {
        if (this.isBotAttckBoss != null && (this.isBotAttckBoss.isDie() || !this.zone.equals(this.isBotAttckBoss.zone)
                || (this.isBotAttckBoss.pvp == null || this.pvp == null)
                || (this.isBotAttckBoss.typePk != ConstPlayer.PK_ALL || this.typePk != ConstPlayer.PK_ALL)
                || ((this.isBotAttckBoss.cFlag == 0 && this.cFlag == 0)
                && (this.isBotAttckBoss.cFlag != 8 || this.cFlag == this.isBotAttckBoss.cFlag)))
                || this.isBotAttckBoss == this) {
            this.isBotAttckBoss = null;
        }
        if (this.zone != null
                && (this.isBotAttckBoss == null || this.isBotAttckBoss == this)
                || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
            this.isBotAttckBoss = this.zone.PlayerPKinmap();
            this.lastTimeTargetPlayer = System.currentTimeMillis();
            this.timeTargetPlayer = Util.nextInt(40000, 45000);
        }
        return this.isBotAttckBoss;
    }

    public void attack() {
        try {
            if (this.isBot) {
                this.mobTarget = this.getMobAttack();
                if (Util.canDoWithTime(lastTimeAttack, 100) && this.mobTarget != null) {

                    this.lastTimeAttack = System.currentTimeMillis();

                    Mob m = this.getMobAttack();
                    if (m == null || m.isDie()) {
                        return;
                    }
                    this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                   // System.out.println(m.name);
                    if (Util.isTrue(80, 100)) {
                        this.playerSkill.skillSelect = this.playerSkill.skills.get(0);
                    }
                    if (Util.getDistance(this, m) <= this.getRangeCanAttackWithSkillSelect()) {
                        if (Util.isTrue(5, 20)) {
                            if (SkillUtil.isUseSkillChuong(this)) {
                                this.moveTo(m.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                        Util.nextInt(10) % 2 == 0 ? m.location.y : m.location.y - Util.nextInt(0, 50));
                            } else {
                                this.moveTo(m.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                        Util.nextInt(10) % 2 == 0 ? m.location.y : m.location.y - Util.nextInt(0, 50));
                            }
                        }
                        SkillService.gI().useSkill(this, null, m, null);
                    } else {
                        this.moveTo(m.location.x, m.location.y);
                    }
                } else {
                    this.mobTarget = getMobAttack();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------
    public long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        int mstChuong = this.nPoint.mstChuong;
        int giamst = this.nPoint.tlGiamst;
        if (!this.isDie()) {
            if (this.isMiniPet) {
                return 0;
            }
            if (plAtt != null) {
                if (this.pet != null && this.pet.status < 3) {
                    this.pet.angry(plAtt);
                }
                if (!this.isBoss && plAtt.nPoint.xDameChuong && SkillUtil.isUseSkillChuong(plAtt)) {
                    damage = plAtt.nPoint.tlDameChuong * damage;
                    plAtt.nPoint.xDameChuong = false;
                }
                if (mstChuong > 0 && SkillUtil.isUseSkillChuong(plAtt)) {
                    PlayerService.gI().hoiPhuc(this, 0, damage * mstChuong / 70);
                    damage = 0;
                }
            }
            if (!SkillUtil.isUseSkillBoom(plAtt)) {
                if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 100)) {
                    return 0;
                }
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (isMobAttack && this.charms.tdBatTu > System.currentTimeMillis() && damage >= this.nPoint.hp) {
                damage = this.nPoint.hp - 1;
            }
            if (plAtt != null && plAtt.clan != null) {
    // Buff tăng sức đánh
 damage = ClanService.gI().getClanDamage(this, damage);


    // Buff hút HP
    long hutHP = ClanService.gI().getClanHutHP(plAtt);
    if (hutHP > 0) {
        plAtt.nPoint.hp += (damage * hutHP / 100);
        if (plAtt.nPoint.hp > plAtt.nPoint.hpMax) {
            plAtt.nPoint.hp = plAtt.nPoint.hpMax;
        }
    }

    // Buff hút KI
    long hutKI = ClanService.gI().getClanHutKI(plAtt);
    if (hutKI > 0) {
        plAtt.nPoint.mp += (damage * hutKI / 100);
        if (plAtt.nPoint.mp > plAtt.nPoint.mpMax) {
            plAtt.nPoint.mp = plAtt.nPoint.mpMax;
        }
    }
}

            if (giamst > 0) {
                damage -= nPoint.calPercent(damage, giamst);
            }
            if (this.effectSkill.isHoldMabu) {
                damage = 1;
            }
            if (this.effectSkill.isHoldMabu && Util.isTrue(30, 150)) {
                Service.getInstance().removeMabuEat(this);
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                if (plAtt != null) {
                    if (MapService.gI().isMapMabuWar(plAtt.zone.map.mapId)) {
                        plAtt.addPowerPoint(5);
                        Service.getInstance().sendPowerInfo(plAtt, "TL", plAtt.getPowerPoint());
                    }
                }
                setDie(plAtt);
            }
            return damage;
        } else {
            return 0;
        }
    }

    private void setDie(Player plAtt) {
        //xóa phù
        if (this.effectSkin.xHPKI > 1) {
            this.effectSkin.xHPKI = 1;
            Service.getInstance().point(this);
        }
        //xóa tụ skill đặc biệt
        this.playerSkill.prepareQCKK = false;
        this.playerSkill.prepareLaze = false;
        this.playerSkill.prepareTuSat = false;
        //xóa hiệu ứng skill
        this.effectSkill.removeSkillEffectWhenDie();
        //
        nPoint.setHp(0);
        nPoint.setMp(0);
        //xóa trứng
        if (this.mobMe != null) {
            this.mobMe.mobMeDie();
        }
        Service.getInstance().charDie(this);
        //add kẻ thù
        if (!this.isPet && !this.isBoss && plAtt != null && !plAtt.isPet && !plAtt.isBoss) {
            if (!plAtt.itemTime.isUseAnDanh) {
                FriendAndEnemyService.gI().addEnemy(this, plAtt);
            }
        }
        if (this.effectSkin.isSocola) {
            reward(plAtt);
        }
        if (MapService.gI().isMapMabuWar(this.zone.map.mapId)) {
            if (this.powerPoint < 20) {
                this.powerPoint = 0;
            }
            if (this.percentPowerPont < 100) {
                this.percentPowerPont = 0;
            }
        }
        //kết thúc pk
        PVPServcice.gI().finishPVP(this, PVP.TYPE_DIE);
        BlackBallWar.gI().dropBlackBall(this);
        if (isHoldNamecBall) {
            NamekBallWar.gI().dropBall(this);
        }
        if (isHoldNamecBallTranhDoat) {
            TranhNgocService.getInstance().dropBall(this, (byte) -1);
            TranhNgocService.getInstance().sendUpdateLift(this);
        }
    }

    public void reward(Player pl) {
        if (pl != null) {
            int x = this.location.x;
            int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
            ItemMap itemMap = new ItemMap(this.zone, 516, 1, x, y, pl.id);
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);
            if (itemMap != null) {
                Service.getInstance().dropItemMap(zone, itemMap);
            }
        }
    }

    public String tenskill9(int gender) {
        switch (gender) {
            case 2:
                return "Cadic Liên hoàn chưởng";
            case 1:
                return "Ma Phong Ba";
            case 0:
                return "Super Kamejoko";
            default:
                return "";
        }
    }

    //--------------------------------------------------------------------------
    public void setClanMember() {
        if (this.clanMember != null) {
            this.clanMember.powerPoint = this.nPoint.power;
            this.clanMember.head = this.getHead();
            this.clanMember.body = this.getBody();
            this.clanMember.leg = this.getLeg();
        }
    }
    
    public void congExpOff() {
        long exp = this.getexp() * this.timeoff;
        Service.getInstance().addSMTN(this, (byte) 2, exp, false);
        NpcService.gI().createTutorial(this, 536, "Bạn tăng được " + exp + " sức mạnh trong thời gian " + this.timeoff + " phút tập luyện Offline");
    }

    public boolean isAdmin() {
        return this.session.isAdmin;
    }

    public void setJustRevivaled() {
        this.justRevived = true;
        this.lastTimeRevived = System.currentTimeMillis();
        this.immortal = true;
    }

    public void dispose() {
        if (escortedBoss != null) {
            escortedBoss.stopEscorting();
        }
        if (skillSpecial != null) {
            skillSpecial.dispose();
            skillSpecial = null;
        }
        isDisposed = true;
        if (pet != null) {
            pet.dispose();
            pet = null;
        }
        playerTrade = null;
        if (mapBlackBall != null) {
            mapBlackBall.clear();
            mapBlackBall = null;
        }
        zone = null;
        mapBeforeCapsule = null;
        if (mapCapsule != null) {
            mapCapsule.clear();
            mapCapsule = null;
        }
        if (mobMe != null) {
            mobMe.dispose();
            mobMe = null;
        }
        location = null;
        if (setClothes != null) {
            setClothes.dispose();
            setClothes = null;
        }
        if (effectSkill != null) {
            effectSkill.dispose();
            effectSkill = null;
        }
        if (mabuEgg != null) {
            mabuEgg.dispose();
            mabuEgg = null;
        }
        if (playerTask != null) {
            playerTask.dispose();
            playerTask = null;
        }
        if (itemTime != null) {
            itemTime.dispose();
            itemTime = null;
        }
        if (fusion != null) {
            fusion.dispose();
            fusion = null;
        }
        if (magicTree != null) {
            magicTree.dispose();
            magicTree = null;
        }
        if (playerIntrinsic != null) {
            playerIntrinsic.dispose();
            playerIntrinsic = null;
        }
        if (inventory != null) {
            inventory.dispose();
            inventory = null;
        }
        if (playerSkill != null) {
            playerSkill.dispose();
            playerSkill = null;
        }
        if (combineNew != null) {
            combineNew.dispose();
            combineNew = null;
        }
        iDMark = null;
        if (charms != null) {
            charms.dispose();
            charms = null;
        }
        if (effectSkin != null) {
            effectSkin.dispose();
            effectSkin = null;
        }
        if (gift != null) {
            gift.dispose();
            gift = null;
        }
        if (nPoint != null) {
            nPoint.dispose();
            nPoint = null;
        }
        if (rewardBlackBall != null) {
            rewardBlackBall.dispose();

            rewardBlackBall = null;
        }
        if (effectFlagBag != null) {
            effectFlagBag.dispose();
            effectFlagBag = null;
        }
        effectFlagBag = null;
        clan = null;
        clanMember = null;
        friends = null;
        enemies = null;
        session = null;
        name = null;
    }
    public void TangDiem(Player player) {
        try {
            Item item = InventoryService.gI().findItemBagByTemp(player, 1459);
            if (item == null) {
                return;
            }
            for (ItemOption io : item.itemOptions) {
                if (io.optionTemplate.id == 240 && io.param < 10) {
                    io.param++;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Loi tang diem boss");
        }
    }
    
    public int getexp() {
        int[] expTable = {5000, 10000, 20000, 40000, 80000, 120000, 240000, 500000};
        if (this.typetrain >= 0 && this.typetrain < expTable.length) {
            return expTable[this.typetrain];
        } else {
            return 0;
        }
    }
}
    

  
package nro.models.boss;

import java.io.IOException;
import nro.consts.ConstItem;
import nro.consts.ConstMap;
import nro.consts.ConstPlayer;
import nro.consts.ConstRatio;
import nro.event.Event;
import nro.lib.RandomCollection;
import nro.models.boss.cdrd.CBoss;
import nro.models.boss.iboss.BossInterface;
import nro.models.boss.mabu_war.BossMabuWar;
import nro.models.boss.nappa.Kuku;
import nro.models.boss.nappa.MapDauDinh;
import nro.models.boss.nappa.Rambo;
import nro.models.item.Item;
import nro.services.FamilyService;
import nro.services.Service;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.server.ServerNotify;
import nro.services.*;
import nro.services.*;
import nro.services.func.ChangeMapService;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.Util;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import nro.models.boss.event.SantaClaus;
import nro.models.map.mabu.MabuWar14h;
import nro.server.io.Message;

/**
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 * Enhanced Boss AI System
 */
public abstract class Boss extends Player implements BossInterface {

    //type dame
    public static final byte DAME_NORMAL = 0;
    public static final byte DAME_PERCENT_HP_HUND = 1;
    public static final byte DAME_PERCENT_MP_HUND = 2;
    public static final byte DAME_PERCENT_HP_THOU = 3;
    public static final byte DAME_PERCENT_MP_THOU = 4;
    public static final byte DAME_TIME_PLAYER_WITH_HIGHEST_HP_IN_CLAN = 5;
    public long lastTimeFight = 0;
    
    //type hp
    public static final byte HP_NORMAL = 0;
    public static final byte HP_TIME_PLAYER_WITH_HIGHEST_DAME_IN_CLAN = 1;

    protected static final byte DO_NOTHING = 71;
    protected static final byte RESPAWN = 77;
    protected static final byte JUST_RESPAWN = 75;
    protected static final byte REST = 0;
    public static final byte JUST_JOIN_MAP = 1;
    protected static final byte TALK_BEFORE = 2;
    public static final byte ATTACK = 3;
    protected static final byte IDLE = 4;
    protected static final byte DIE = 5;
    protected static final byte TALK_AFTER = 6;
    protected static final byte LEAVE_MAP = 7;

    // ===== ENHANCED AI VARIABLES =====
    protected long lastTimeMoveRandom = 0;
    protected long lastTimeDodge = 0;
    protected long lastTimeChangeStrategy = 0;
    protected int currentStrategy = 0;
    protected boolean isRetreating = false;
    protected long retreatStartTime = 0;
    protected int consecutiveHits = 0;
    protected long lastTimeHit = 0;
    
    // AI Strategy constants
    private static final int STRATEGY_AGGRESSIVE = 0;
    private static final int STRATEGY_DEFENSIVE = 1;
    private static final int STRATEGY_KITING = 2;
    private static final int STRATEGY_BERSERK = 3;

    //--------------------------------------------------------------------------
    public BossData data;
    @Setter
    protected byte status;
    protected short[] outfit;
    protected byte typeDame;
    protected byte typeHp;
    protected int percentDame;
    protected short[] mapJoin;

    protected byte indexTalkBefore;
    protected String[] textTalkBefore;
    protected byte indexTalkAfter;
    protected String[] textTalkAfter;
    protected String[] textTalkMidle;

    protected long lastTimeTalk;
    protected int timeTalk;
    protected byte indexTalk;
    protected boolean doneTalkBefore;
    protected boolean doneTalkAffter;

    private long lastTimeRest;
    protected int secondTimeRestToNextTimeAppear = 1800;

    protected int maxIdle;
    protected int countIdle;

    private final List<Skill> skillsAttack;
    private final List<Skill> skillsSpecial;

    protected Player plAttack;
    protected int targetCountChangePlayerAttack;
    protected int countChangePlayerAttack;

    private long lastTimeStartLeaveMap;
    private int timeDelayLeaveMap = 2000;

    protected boolean joinMapIdle;

    private int timeAppear = 0;
    private long lastTimeUpdate;
    private int TIME_RESEND_LOCATION = 15;

    public long timeStartDie;
    public boolean startDie = true;
    public boolean isMabuBoss;
    public int zoneHold;
    public boolean isUseSpeacialSkill;
    public long lastTimeUseSpeacialSkill;

    public void changeStatus(byte status) {
        this.status = status;
    }

    public Boss(int id, BossData data) {
        super();
        this.id = id;
        this.skillsAttack = new ArrayList<>();
        this.skillsSpecial = new ArrayList<>();
        this.data = data;
        this.isBoss = true;
        this.initTalk();
        this.respawn();
        setJustRest();
        if (!(this instanceof CBoss)) {
            BossManager.gI().addBoss(this);
        }
    }

    @Override
    public void init() {
        this.name = data.name.replaceAll("%1", String.valueOf(Util.nextInt(0, 100)));
        this.gender = data.gender;
        this.typeDame = data.typeDame;
        this.typeHp = data.typeHp;
        this.nPoint.power = 1;
        this.nPoint.mpg = 752002;
        int dame = data.dame;
        long hp = 1;
        if (data.secondsRest != -1) {
            this.secondTimeRestToNextTimeAppear = data.secondsRest;
        }

        long[] arrHp = data.hp[Util.nextInt(0, data.hp.length - 1)];
        if (arrHp.length == 1) {
            hp = arrHp[0];
        } else {
            hp = Util.nextdame(arrHp[0], arrHp[1]);
        }
        switch (this.typeHp) {
            case HP_NORMAL:
                this.nPoint.hpg = hp;
                break;
            case HP_TIME_PLAYER_WITH_HIGHEST_DAME_IN_CLAN:
                break;
        }

        switch (this.typeDame) {
            case DAME_NORMAL:
                this.nPoint.dameg = dame;
                break;
            case DAME_PERCENT_HP_HUND:
                this.percentDame = dame;
                this.nPoint.dameg = this.nPoint.hpg * dame / 100;
                break;
            case DAME_PERCENT_MP_HUND:
                this.percentDame = dame;
                this.nPoint.dameg = this.nPoint.mpg * dame / 100;
                break;
            case DAME_PERCENT_HP_THOU:
                this.percentDame = dame;
                this.nPoint.dameg = this.nPoint.hp * dame / 1000;
                break;
            case DAME_PERCENT_MP_THOU:
                this.percentDame = dame;
                this.nPoint.dameg = this.nPoint.mpg * dame / 1000;
                break;
            case DAME_TIME_PLAYER_WITH_HIGHEST_HP_IN_CLAN:
                break;
        }
        this.nPoint.calPoint();
        this.outfit = data.outfit;
        this.mapJoin = data.mapJoin;
        if (data.timeDelayLeaveMap != -1) {
            this.timeDelayLeaveMap = data.timeDelayLeaveMap;
        }
        this.joinMapIdle = data.joinMapIdle;
        initSkill();
    }

    @Override
    public int version() {
        return 214;
    }

    protected void initSkill() {
        this.playerSkill.skills.clear();
        this.skillsAttack.clear();
        this.skillsSpecial.clear();
        int[][] skillTemp = data.skillTemp;
        for (int i = 0; i < skillTemp.length; i++) {
            Skill skill = SkillUtil.createSkill(skillTemp[i][0], skillTemp[i][1]);
            skill.coolDown = skillTemp[i][2];
            this.playerSkill.skills.add(skill);
            switch (skillTemp[i][0]) {
                case Skill.DRAGON:
                case Skill.DEMON:
                case Skill.GALICK:
                case Skill.KAMEJOKO:
                case Skill.MASENKO:
                case Skill.ANTOMIC:
                case Skill.LIEN_HOAN:
                case Skill.KAIOKEN:
                    this.skillsAttack.add(skill);
                    break;
                case Skill.TAI_TAO_NANG_LUONG:
                case Skill.THAI_DUONG_HA_SAN:
                case Skill.DICH_CHUYEN_TUC_THOI:
                case Skill.BIEN_KHI:
                case Skill.THOI_MIEN:
                case Skill.TROI:
                case Skill.KHIEN_NANG_LUONG:
                case Skill.SOCOLA:
                case Skill.DE_TRUNG:
                    this.skillsSpecial.add(skill);
                    break;
            }
        }
    }
    
    public long lastTimeChat;

    private void BaoHpBoss() {
        try {
            Message msg;
            if (this.isBoss && this.nPoint.hp >= 2123456789) {
                if (Util.canDoWithTime(lastTimeChat, 1500)) {
                    String text = "|2|<-" + this.name + "->" + "\n\n"
                            + "|7|Máu Còn lại : " + Util.powerToStringnew(this.nPoint.hp) + "\n"
                            + "|3|< " + Util.format(this.nPoint.hp) + " >";
                    msg = new Message(44);
                    msg.writer().writeInt((int) this.id);
                    msg.writer().writeUTF(text);
                    Service.getInstance().sendMessAllPlayerInMap(this, msg);
                    msg.cleanup();
                    lastTimeChat = System.currentTimeMillis();
                }
            }
        } catch (IOException e) {
            Log.error(SkillService.class, e);
        }
    }

    // ===== ENHANCED AI: Dynamic Strategy Selection =====
    protected void updateAIStrategy() {
        if (!Util.canDoWithTime(lastTimeChangeStrategy, 10000)) {
            return;
        }
        
        int hpPercent = (int)(this.nPoint.hp * 100 / this.nPoint.hpg);
        int playerCount = this.zone.getHumanoids().size();
        
        // Thay đổi chiến thuật dựa trên HP và số lượng người chơi
        if (hpPercent < 20) {
            currentStrategy = STRATEGY_BERSERK; // Điên cuồng khi sắp chết
        } else if (hpPercent < 40) {
            currentStrategy = STRATEGY_DEFENSIVE; // Phòng thủ khi máu thấp
        } else if (playerCount > 3) {
            currentStrategy = STRATEGY_KITING; // Kite khi bị đông người
        } else {
            currentStrategy = STRATEGY_AGGRESSIVE; // Tấn công khi có lợi thế
        }
        
        lastTimeChangeStrategy = System.currentTimeMillis();
    }

    // ===== ENHANCED AI: Smart Movement =====
    protected void smartMovement(Player target) {
        if (target == null || target.isDie()) return;
        
        long now = System.currentTimeMillis();
        int distance = Util.getDistance(this, target);
        
        switch (currentStrategy) {
            case STRATEGY_AGGRESSIVE:
                // Di chuyển tấn công: tiến gần mục tiêu
                if (distance > 100 && Util.canDoWithTime(lastTimeMoveRandom, 2000)) {
                    goToPlayer(target, false);
                    lastTimeMoveRandom = now;
                }
                break;
                
            case STRATEGY_DEFENSIVE:
                // Phòng thủ: giữ khoảng cách an toàn
                if (distance < 150 && Util.canDoWithTime(lastTimeMoveRandom, 1500)) {
                    retreatFromPlayer(target);
                    lastTimeMoveRandom = now;
                }
                break;
                
            case STRATEGY_KITING:
                // Kite: đánh rồi chạy
                if (!isRetreating && distance < 200) {
                    isRetreating = true;
                    retreatStartTime = now;
                    retreatFromPlayer(target);
                } else if (isRetreating && now - retreatStartTime > 3000) {
                    isRetreating = false;
                }
                break;
                
            case STRATEGY_BERSERK:
                // Điên cuồng: dịch chuyển liên tục
                if (Util.canDoWithTime(lastTimeMoveRandom, 800)) {
                    randomTeleport();
                    lastTimeMoveRandom = now;
                }
                break;
        }
        
        // Random dodge khi bị tấn công liên tục
        if (consecutiveHits > 3 && Util.canDoWithTime(lastTimeDodge, 5000)) {
            dodgeMove(target);
            consecutiveHits = 0;
            lastTimeDodge = now;
        }
    }

    // Lùi lại khỏi người chơi
    protected void retreatFromPlayer(Player pl) {
        int dirX = this.location.x - pl.location.x;
        int retreatDistance = Util.nextInt(80, 150);
        int newX = this.location.x + (dirX > 0 ? retreatDistance : -retreatDistance);
        
        // Giới hạn trong map
        if (newX < 50) newX = 50;
        if (newX > zone.map.mapWidth - 50) newX = zone.map.mapWidth - 50;
        
        goToXY(newX, pl.location.y, false);
    }

    // Di chuyển né tránh
    protected void dodgeMove(Player pl) {
        if (this.skillsSpecial.stream().anyMatch(s -> s.template.id == Skill.DICH_CHUYEN_TUC_THOI)) {
            // Teleport nếu có skill
            randomTeleport();
        } else {
            // Di chuyển nhanh sang hướng ngẫu nhiên
            int offsetX = Util.getOne(-1, 1) * Util.nextInt(100, 200);
            int offsetY = Util.nextInt(-50, 50);
            goToXY(this.location.x + offsetX, this.location.y + offsetY, false);
        }
    }

    // Dịch chuyển ngẫu nhiên
    protected void randomTeleport() {
        if (this.zone == null) return;
        
        int x = Util.nextInt(100, this.zone.map.mapWidth - 100);
        int y = this.zone.map.yPhysicInTop(x, 0);
        
        // Sử dụng skill dịch chuyển nếu có
        Skill teleportSkill = getSkillById(Skill.DICH_CHUYEN_TUC_THOI);
        if (teleportSkill != null) {
            this.playerSkill.skillSelect = teleportSkill;
            ChangeMapService.gI().changeMapYardrat(this, this.zone, x, y);
        } else {
            goToXY(x, y, true);
        }
    }

    @Override
    public void update() {
        super.update();
        try {
            if (!this.effectSkill.isStun) {
                //this.BaoHpBoss();
            }
            if (!this.effectSkill.isHaveEffectSkill()
                    && !this.effectSkill.isCharging) {
                this.immortalMp();
                
                // Update AI strategy
                if (this.status == ATTACK) {
                    updateAIStrategy();
                }
                
                switch (this.status) {
                    case RESPAWN:
                        respawn();
                        break;
                    case JUST_RESPAWN:
                        this.changeStatus(REST);
                        break;
                    case REST:
                        if (Util.canDoWithTime(lastTimeRest, secondTimeRestToNextTimeAppear * 1000)) {
                            this.changeStatus(JUST_JOIN_MAP);
                        }
                        break;
                    case JUST_JOIN_MAP:
                        joinMap();
                        if (this.zone != null) {
                            changeStatus(TALK_BEFORE);
                        }
                        break;
                    case TALK_BEFORE:
                        if (talk()) {
                            if (!this.joinMapIdle) {
                                changeToAttack();
                            } else {
                                this.changeStatus(IDLE);
                            }
                        }
                        break;
                    case ATTACK:
                        this.talk();
                        if (this.playerSkill.prepareTuSat || this.playerSkill.prepareLaze
                                || this.playerSkill.prepareQCKK) {
                            break;
                        } else {
                            this.attack();
                        }
                        break;
                    case IDLE:
                        this.idle();
                        break;
                    case DIE:
                        if (this.joinMapIdle) {
                            this.changeToIdle();
                        }
                        if (MabuWar14h.gI().isTimeMabuWar() && this.isMabuBoss && this.zone.map.mapId == 127) {
                            nextMabu(this.isDie());
                            return;
                        }
                        changeStatus(TALK_AFTER);
                        break;
                    case TALK_AFTER:
                        if (talk()) {
                            changeStatus(LEAVE_MAP);
                            this.lastTimeStartLeaveMap = System.currentTimeMillis();
                        }
                        break;
                    case LEAVE_MAP:
                        if (Util.canDoWithTime(lastTimeStartLeaveMap, timeDelayLeaveMap)) {
                            this.leaveMap();
                            this.changeStatus(RESPAWN);
                        }
                        break;
                    case DO_NOTHING:
                        break;
                }
            }
            if (Util.canDoWithTime(lastTimeUpdate, 60000)) {
                if (timeAppear >= TIME_RESEND_LOCATION) {
                    if (this.zone != null && !(this instanceof BossMabuWar)) {
                        ServerNotify.gI().notify("Boss " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
                        timeAppear = 0;
                    }
                } else {
                    timeAppear++;
                }
                lastTimeUpdate = System.currentTimeMillis();
            }
        } catch (Exception e) {
            this.leaveMap();
            BossManager.gI().removeBoss(this);
            System.out.println("Loi boss " + this.name);
            Log.error(Boss.class, e);
        }
    }

    public List<Player> getListPlayerAttack(int dis) {
        List<Player> Players = new ArrayList<>();
        for (int i = 0; i < this.zone.getHumanoids().size(); i++) {
            Player pl = this.zone.getHumanoids().get(i);
            if (pl != null && !pl.isDie() && !pl.effectSkill.isHoldMabu && Util.getDistance(this, pl) <= dis) {
                Players.add(pl);
            }
        }
        return Players;
    }

    public void nextMabu(boolean isDie) {
        if ((isDie ? this.isDie() : true) && this.head != 427 && !Util.canDoWithTime(this.timeStartDie, 3200)) {
            if (this.startDie) {
                this.startDie = false;
                Service.getInstance().hsChar(this, -1, -1);
                EffectSkillService.gI().startCharge(this);
            }
            return;
        }
        this.startDie = false;
        EffectSkillService.gI().stopCharge(this);
        int id = (int) this.id;
        switch (id) {
            case BossFactory.MABU_MAP:
                this.leaveMap();
                this.id = BossFactory.SUPER_BU;
                this.data = BossData.SUPER_BU;
                this.changeStatus(RESPAWN);
                break;
            case BossFactory.SUPER_BU:
                this.leaveMap();
                this.id = BossFactory.KID_BU;
                this.data = BossData.KID_BU;
                this.changeStatus(RESPAWN);
                break;
            case BossFactory.KID_BU:
                this.leaveMap();
                this.id = BossFactory.BU_TENK;
                this.data = BossData.BU_TENK;
                this.changeStatus(RESPAWN);
                break;
            case BossFactory.BU_TENK:
                this.leaveMap();
                this.id = BossFactory.BU_HAN;
                this.data = BossData.BU_HAN;
                this.changeStatus(RESPAWN);
                break;
            default:
                if (isDie) {
                    this.leaveMap();
                }
                break;
        }
    }

    public long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        long dame = 0;
        if (this.isDie()) {
            return dame;
        } else {
            final long now = System.currentTimeMillis();
            final boolean hasBossDmgBuff = (plAtt != null && plAtt.buffBossDamageTime > now);

            // Track consecutive hits for dodge system
            if (now - lastTimeHit < 2000) {
                consecutiveHits++;
            } else {
                consecutiveHits = 1;
            }
            lastTimeHit = now;

            // Skill check
            if (Util.isTrue(1, 99) && plAtt != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.TU_SAT:
                    case Skill.QUA_CAU_KENH_KHI:
                    case Skill.MAKANKOSAPPO:
                        break;
                    default:
                        return 0;
                }
            }

            // DEF calculation
            if (this.nPoint != null) {
                if (this.nPoint.def <= 0) {
                    this.nPoint.def = 50000;
                }
                if (!piercing) {
                    damage -= this.nPoint.def;
                    if (damage < 1) damage = 1;
                }
            }

            // Damage reduction
            if (!hasBossDmgBuff) {
                damage = (long)(damage * 0.5);
            }

            // Buff damage increase
            if (hasBossDmgBuff) {
                damage = (long)(damage * 1.2);
            }

            dame = super.injured(plAtt, damage, piercing, isMobAttack);

            // Boss death
            if (this.isDie()) {
                if (plAtt != null) {
                    rewards(plAtt);
                    notifyPlayeKill(plAtt);
                    FamilyService.gI().addChildExp(plAtt, 5);
                    // Service.getInstance().sendThongBao(plAtt,
                    //     "papa giỏi quá papa mama đừng bỏ con như J97 nhé!");
                } else {
                    System.err.println("Boss " + this.name + " died but plAtt is null");
                }
                die();
            }
            return dame;
        }
    }

    protected void notifyPlayeKill(Player player) {
        if (player != null) {
            ServerNotify.gI().notify(player.name + " vừa tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ");
        }
    }

    protected boolean charge() {
        if (this.effectSkill.isCharging && Util.isTrue(50, 100)) {
            this.effectSkill.isCharging = false;
            return false;
        }
        
        // Smart charging based on HP
        int hpPercent = (int)(this.nPoint.hp * 100 / this.nPoint.hpg);
        int chargeChance = hpPercent < 30 ? 90 : 70; // Cao hơn khi máu thấp
        
        if (Util.isTrue(chargeChance, 100)) {
            for (Skill skill : this.playerSkill.skills) {
                if (skill.template.id == Skill.TAI_TAO_NANG_LUONG || 
                    skill.template.id == Skill.KHIEN_NANG_LUONG || 
                    skill.template.id == Skill.THAI_DUONG_HA_SAN ||
                    skill.template.id == Skill.THOI_MIEN) {
                    this.playerSkill.skillSelect = skill;
                    if (this.nPoint.getCurrPercentHP() < Util.nextInt(0, 100) && 
                        SkillService.gI().canUseSkillWithCooldown(this) &&
                        SkillService.gI().useSkill(this, null, null, null)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public long injuredNotCheckDie(Player plAtt, long damage, boolean piercing) {
        if (this.isDie()) {
            return 0;
        } else {
            long dame = super.injured(plAtt, damage, piercing, false);
            return dame;
        }
    }

    protected Skill getSkillAttack() {
        return skillsAttack.get(Util.nextInt(0, skillsAttack.size() - 1));
    }

    protected Skill getSkillSpecial() {
        return skillsSpecial.get(Util.nextInt(0, skillsSpecial.size() - 1));
    }

    protected Skill getSkillById(int skillId) {
        return SkillUtil.getSkillbyId(this, skillId);
    }

    @Override
    public void die() {
        setJustRest();
        changeStatus(DIE);
        consecutiveHits = 0;
        isRetreating = false;
    }

    @Override
    public void joinMap() {
        if (this.zone == null) {
            this.zone = getMapCanJoin(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
        }
        if (this.zone != null) {
            ChangeMapService.gI().changeMapBySpaceShip(this, this.zone, ChangeMapService.TENNIS_SPACE_SHIP);
            ServerNotify.gI().notify("Boss " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
           // System.out.println("Boss " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName + " khu vực " + this.zone.zoneId);
        }
    }

    public Zone getMapCanJoin(int mapId) {
        Zone map = MapService.gI().getMapWithRandZone(mapId);
        if (map.isBossCanJoin(this)) {
            return map;
        } else {
            return getMapCanJoin(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
        }
    }

    public Zone getMapJoin() {
        int mapId = this.data.mapJoin[Util.nextInt(0, this.data.mapJoin.length - 1)];
        Zone map = MapService.gI().getMapWithRandZone(mapId);
        return map;
    }

    @Override
    public void leaveMap() {
        MapService.gI().exitMap(this);
    }

    @Override
    public boolean talk() {
        switch (status) {
            case TALK_BEFORE:
                if (this.textTalkBefore == null || this.textTalkBefore.length == 0) {
                    return true;
                }
                if (Util.canDoWithTime(lastTimeTalk, 5000)) {
                    if (indexTalkBefore < textTalkBefore.length) {
                        this.chat(textTalkBefore[indexTalkBefore++]);
                        if (indexTalkBefore >= textTalkBefore.length) {
                            return true;
                        }
                        lastTimeTalk = System.currentTimeMillis();
                    } else {
                        return true;
                    }
                }
                break;
            case IDLE:
            case ATTACK:
                if (this.textTalkMidle == null || this.textTalkMidle.length == 0 || !Util.isTrue(1, 30)) {
                    return true;
                }
                if (Util.canDoWithTime(lastTimeTalk, Util.nextInt(15000, 20000))) {
                    this.chat(textTalkMidle[Util.nextInt(0, textTalkMidle.length - 1)]);
                    lastTimeTalk = System.currentTimeMillis();
                }
                break;
            case TALK_AFTER:
                if (this.textTalkAfter == null || this.textTalkAfter.length == 0) {
                    return true;
                }
                if (Util.canDoWithTime(lastTimeTalk, 5000)) {
                    this.chat(textTalkAfter[indexTalkAfter++]);
                    if (indexTalkAfter >= textTalkAfter.length) {
                        return true;
                    }
                    if (indexTalkAfter > textTalkAfter.length - 1) {
                        indexTalkAfter = 0;
                    }
                    lastTimeTalk = System.currentTimeMillis();
                }
                break;
        }
        return false;
    }

    @Override
    public void respawn() {
        this.init();
        this.indexTalkBefore = 0;
        this.indexTalkAfter = 0;
        this.nPoint.setFullHpMp();
        this.changeStatus(JUST_RESPAWN);
        // Reset AI variables
        this.consecutiveHits = 0;
        this.isRetreating = false;
        this.currentStrategy = STRATEGY_AGGRESSIVE;
    }

    protected void goToPlayer(Player pl, boolean isTeleport) {
        goToXY(pl.location.x, pl.location.y, isTeleport);
    }

    protected void goToXY(int x, int y, boolean isTeleport) {
        if (!isTeleport) {
            byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
            byte move = (byte) Util.nextInt(50, 100);
            PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
        } else {
            ChangeMapService.gI().changeMapYardrat(this, this.zone, x, y);
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

    @Override
    public Player getPlayerAttack() throws Exception {
        if (countChangePlayerAttack < targetCountChangePlayerAttack
                && plAttack != null && plAttack.zone != null
                && plAttack.zone.equals(this.zone)) {
            if (!plAttack.isDie() && !plAttack.effectSkin.isVoHinh && !plAttack.isMiniPet) {
                this.countChangePlayerAttack++;
                return plAttack;
            } else {
                plAttack = null;
            }
        } else {
            this.targetCountChangePlayerAttack = Util.nextInt(10, 20);
            this.countChangePlayerAttack = 0;
            plAttack = this.zone.getRandomPlayerInMap();
            if (plAttack != null && plAttack.effectSkin.isVoHinh) {
                plAttack = null;
            }
        }
        return plAttack;
    }

    @Override
    public void attack() {
        try {
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie() || pl.isMiniPet) return;

            long now = System.currentTimeMillis();
            int distance = Util.getDistance(this, pl);
            
            // ====== ENHANCED AI: Smart Movement ======
            smartMovement(pl);

            // ====== A. Check skill đặc biệt (ưu tiên cao hơn) ======
            if (this.skillsSpecial != null && !this.skillsSpecial.isEmpty()) {
                // Tăng tần suất dùng skill đặc biệt khi máu thấp
                int hpPercent = (int)(this.nPoint.hp * 100 / this.nPoint.hpg);
                int specialSkillDelay = hpPercent < 30 ? 8000 : 13000;
                int specialSkillChance = hpPercent < 30 ? 70 : 50;
                
                if (now - this.lastTimeUseSpeacialSkill > specialSkillDelay && Util.isTrue(specialSkillChance, 100)) {
                    Skill skillSpec = this.getSkillSpecial();
                    this.playerSkill.skillSelect = skillSpec;
                    this.lastTimeUseSpeacialSkill = now;

                    // Mở rộng range cho skill đặc biệt
                    if (distance <= this.getRangeCanAttackWithSkillSelect() + 50) {
                        SkillService.gI().useSkill(this, pl, null, null);
                        checkPlayerDie(pl);
                        
                        // Di chuyển sau khi dùng skill theo strategy
                        if (currentStrategy == STRATEGY_KITING || currentStrategy == STRATEGY_BERSERK) {
                            retreatFromPlayer(pl);
                        }
                        return;
                    }
                }
            }

            // ====== B. Tấn công thường với movement thông minh ======
            this.playerSkill.skillSelect = this.getSkillAttack();
            int attackRange = this.getRangeCanAttackWithSkillSelect();
            
            if (distance <= attackRange) {
                // Thêm movement tactics trước khi tấn công
                boolean shouldMove = false;
                
                switch (currentStrategy) {
                    case STRATEGY_AGGRESSIVE:
                        shouldMove = Util.isTrue(15, 100);
                        break;
                    case STRATEGY_DEFENSIVE:
                        shouldMove = Util.isTrue(30, 100);
                        break;
                    case STRATEGY_KITING:
                        shouldMove = Util.isTrue(50, 100);
                        break;
                    case STRATEGY_BERSERK:
                        shouldMove = Util.isTrue(70, 100);
                        break;
                }
                
                if (shouldMove) {
                    if (SkillUtil.isUseSkillChuong(this)) {
                        // Skill tầm xa: di chuyển xa hơn
                        goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(40, 120)),
                                Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70), false);
                    } else {
                        // Skill cận chiến: di chuyển gần
                        goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 50)),
                                Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50), false);
                    }
                }
                
                SkillService.gI().useSkill(this, pl, null, null);
                checkPlayerDie(pl);
                
                // Movement sau khi tấn công dựa vào strategy
                if (currentStrategy == STRATEGY_KITING && Util.isTrue(60, 100)) {
                    retreatFromPlayer(pl);
                } else if (currentStrategy == STRATEGY_BERSERK && Util.isTrue(40, 100)) {
                    randomTeleport();
                }
            } else {
                // Ngoài tầm đánh: di chuyển theo strategy
                if (currentStrategy == STRATEGY_DEFENSIVE && distance < 300) {
                    // Phòng thủ: giữ khoảng cách
                    if (Util.isTrue(50, 100)) {
                        retreatFromPlayer(pl);
                    }
                } else if (!isRetreating) {
                    // Tiến lại gần
                    goToPlayer(pl, false);
                }
            }
        } catch (Exception ex) {
            Log.error(Boss.class, ex);
        }
    }

    private void immortalMp() {
        this.nPoint.mp = this.nPoint.mpg;
    }

    protected abstract boolean useSpecialSkill();

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public short getHead() {
        return this.outfit[0];
    }

    @Override
    public short getBody() {
        return this.outfit[1];
    }

    @Override
    public short getLeg() {
        return this.outfit[2];
    }

    protected void changeIdle() {
        this.changeStatus(IDLE);
    }

    protected void changeAttack() {
        this.changeStatus(ATTACK);
    }

    public void setJustRest() {
        this.lastTimeRest = System.currentTimeMillis();
    }

    public void setJustRestToFuture() {
        this.lastTimeRest = System.currentTimeMillis() + 8640000000L;
    }

    @Override
    public void dropItemReward(int tempId, int playerId, int... quantity) {
        if (!this.zone.map.isMapOffline && this.zone.map.type == ConstMap.MAP_NORMAL) {
            int x = this.location.x + Util.nextInt(-30, 30);
            if (x < 30) {
                x = 30;
            } else if (x > zone.map.mapWidth - 30) {
                x = zone.map.mapWidth - 30;
            }
            int y = this.location.y;
            if (y > 24) {
                y = this.zone.map.yPhysicInTop(x, y - 24);
            }
            ItemMap itemMap = new ItemMap(this.zone, tempId, (quantity != null && quantity.length == 1) ? quantity[0] : 1, x, y, playerId);
            Service.getInstance().dropItemMap(itemMap.zone, itemMap);
        }
    }

    @Override
    public void generalRewards(Player player) {
        if (player == null) return;

        ItemMap itemMap = null;
        ItemMap setDrop = null;

        final int x = this.location.x;
        final int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        // Set rewards
        if (Util.isTrue(1, 200)) {
            int[] set1 = {562, 564, 566, 561};
            int itemId = set1[Util.nextInt(0, set1.length - 1)];
            setDrop = new ItemMap(this.zone, itemId, 1, x, y, player.id);
            RewardService.gI().initBaseOptionClothes(setDrop.itemTemplate.id, setDrop.itemTemplate.type, setDrop.options);
            Service.getInstance().dropItemMap(zone, setDrop);
        } else if (Util.isTrue(1, 200)) {
            int[] set2 = {555, 556, 563, 557, 558, 565, 559, 567, 560};
            int itemId = set2[Util.nextInt(0, set2.length - 1)];
            setDrop = new ItemMap(this.zone, itemId, 1, x, y, player.id);
            RewardService.gI().initBaseOptionClothes(setDrop.itemTemplate.id, setDrop.itemTemplate.type, setDrop.options);
            Service.getInstance().dropItemMap(zone, setDrop);
        }

        // Default rewards
        if (!(this instanceof Kuku) && !(this instanceof Rambo) && !(this instanceof MapDauDinh)) {
            RandomCollection<Integer> rd = new RandomCollection<>();
            rd.add(10, ConstItem.CUONG_NO_2);
            rd.add(10, ConstItem.BO_HUYET_2);
            rd.add(10, ConstItem.BO_KHI_2);
            rd.add(10, ConstItem.DUOI_KHI);
            rd.add(1, ConstItem.CUONG_BAO);
            rd.add(3, ConstItem.BIEN_DI);
            rd.add(10, ConstItem.DA_NGUC_TU_1);
            rd.add(100, ConstItem.THOI_VANG);
            rd.add(1, ConstItem.dch);
            rd.add(1, ConstItem.dcs);
            rd.add(1, ConstItem.DASKH);
            rd.add(1, ConstItem.XU_VANG);
            rd.add(200, ConstItem.HONG_NGOC);
            if (Event.isEvent()) rd.add(1, ConstItem.QUE_DOT);

            int rwID = rd.next();
            if (rwID == ConstItem.XU_VANG) {
                itemMap = new ItemMap(this.zone, rwID, 5, x, y, player.id);
            } else {
                itemMap = new ItemMap(this.zone, rwID, 1, x, y, player.id);
            }
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);
        }

        if (itemMap != null) {
            Service.getInstance().dropItemMap(zone, itemMap);
        }
    }

    public void changeToAttack() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
        changeStatus(ATTACK);
    }

    public void changeToIdle() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
        changeStatus(IDLE);
    }

    protected void chat(String text) {
        Service.getInstance().chat(this, text);
    }
}
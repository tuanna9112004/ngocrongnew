package nro.services;

import nro.consts.Cmd;
import nro.consts.ConstAchive;
import nro.jdbc.DBService;
import nro.jdbc.daos.AccountDAO;
import nro.jdbc.daos.PlayerDAO;
import nro.models.player.PetFollow;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.io.Message;
import nro.utils.Log;
import nro.utils.Util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import nro.consts.ConstTranhNgocNamek;
import nro.models.map.Zone;
import nro.models.phuban.DragonNamecWar.TranhNgocService;

/**
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 */
public class PlayerService {

    private static PlayerService i;

    public PlayerService() {
    }

    public static PlayerService gI() {
        if (i == null) {
            i = new PlayerService();
        }
        return i;
    }

    public void dailyLogin(Player player) {
        if (Util.compareDay(Date.from(Instant.now()), player.firstTimeLogin)) {
            player.goldChallenge = 2000000;
            player.levelWoodChest = 0;
            player.receivedWoodChest = false;
            player.event.setReceivedLuckyMoney(false);
            player.setRewardLimit(new byte[player.getRewardLimit().length]);
            player.buyLimit = new byte[player.buyLimit.length];
            player.bongtai = 0;
            player.thiensu = 0;
            player.phutOnline = 0;
            player.listOnline.clear();
            player.firstTimeLogin = Date.from(Instant.now());
        }
    }


    public void checkNewWeek(Player player) {
        // Lấy ngày hiện tại
        Calendar currentCalendar = Calendar.getInstance();
        int currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR);  // Lấy số tuần trong năm của ngày hiện tại

        // Lấy tuần của lần đăng nhập trước
        Calendar lastLoginCalendar = Calendar.getInstance();
        lastLoginCalendar.setTime(player.weekTimeLogin);
        int lastLoginWeek = lastLoginCalendar.get(Calendar.WEEK_OF_YEAR);

        // Kiểm tra nếu tuần hiện tại khác tuần của lần đăng nhập trước (tức là đã sang tuần mới)
        if (currentWeek != lastLoginWeek) {
            player.listDiemDanh.clear();
            player.weekTimeLogin = new Date();
        }
    }
    public void sendTNSM(Player player, byte type, long param) {
        if (param > 0) {
            Message msg;
            try {
                msg = new Message(-3);
                msg.writer().writeByte(type);// 0 là cộng sm, 1 cộng tn, 2 là cộng cả 2
                msg.writer().writeLong(param);// số tn cần cộng
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    public void sendMessageAllPlayer(Message msg) {
        for (Player pl : Client.gI().getPlayers()) {
            if (pl != null) {
                pl.sendMessage(msg);
            }
        }
        msg.cleanup();

    }

    public void sendMessageIgnore(Player plIgnore, Message msg) {
        for (Player pl : Client.gI().getPlayers()) {
            if (pl != null && !pl.equals(plIgnore)) {
                pl.sendMessage(msg);
            }
        }
        msg.cleanup();

    }

    public void sendInfoHp(Player player) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 5);
            msg.writer().writeLong(player.nPoint.hp);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(PlayerService.class, e);
        }
    }

    public void sendInfoMp(Player player) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 6);
            msg.writer().writeLong(player.nPoint.mp);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(PlayerService.class, e);
        }
    }

    public void sendInfoHpMp(Player player) {
        sendInfoHp(player);
        sendInfoMp(player);
    }

    public void hoiPhuc(Player player, long hp, long mp) {
        if (!player.isDie()) {
            player.nPoint.addHp(hp);
            player.nPoint.addMp(mp);
            Service.getInstance().Send_Info_NV(player);
            if (!player.isPet) {
                PlayerService.gI().sendInfoHpMp(player);
            }
        }
    }

    public void sendInfoHpMpMoney(Player player) {
        Message msg;
        try {
            long gold = player.inventory.getGoldDisplay();
            msg = Service.getInstance().messageSubCommand((byte) 4);
            if (!player.isBot && player.isVersionAbove(214)) {
                msg.writer().writeLong(gold);//xu
            } else {
                msg.writer().writeInt((int) gold);//xu
            }
            msg.writer().writeInt(player.inventory.gem);//luong
            msg.writer().writeLong(player.nPoint.hp);//chp
            msg.writer().writeLong(player.nPoint.mp);//cmp
            msg.writer().writeInt(player.inventory.ruby);//ruby
            player.sendMessage(msg);
        } catch (Exception e) {
            Log.error(PlayerService.class, e);
        }
    }
    
    

    public void playerMove(Player player, int x, int y) {
        if (player.zone == null) {
            return;
        }
        player.zone.playerMove(player, x, y);
    }

    public void sendCurrentStamina(Player player) {
        Message msg;
        try {
            msg = new Message(-68);
            msg.writer().writeShort(player.nPoint.stamina);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(PlayerService.class, e);
        }
    }

    public void sendMaxStamina(Player player) {
        Message msg;
        try {
            msg = new Message(-69);
            msg.writer().writeShort(player.nPoint.maxStamina);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(PlayerService.class, e);
        }
    }

    public void changeAndSendTypePK(Player player, int type) {
        changeTypePK(player, type);
        sendTypePk(player);
    }

    public void changeTypePK(Player player, int type) {
        player.typePk = (byte) type;
    }

    public void sendTypePk(Player player) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeByte(player.typePk);
            Service.getInstance().sendMessAllPlayerInMap(player.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void banPlayer(Player playerBaned) {
        AccountDAO.banAccount(playerBaned.getSession());
        Service.getInstance().sendThongBao(playerBaned,
                "Tài khoản của bạn đã bị khóa\nGame sẽ mất kết nối sau 5 giây...");
        playerBaned.lastTimeBan = System.currentTimeMillis();
        playerBaned.isBan = true;
    }

    private static final int COST_GOLD_HOI_SINH = 20000000;

    public void hoiSinh(Player player) {
        if (player.isDie()) {
            if (MapService.gI().isMapMabuWar14H(player.zone.map.mapId)) {
                Service.getInstance().sendThongBao(player, "Bạn không thể hồi sinh");
                return;
            }
            boolean canHs = false;
            if (MapService.gI().isMapBlackBallWar(player.zone.map.mapId)
                    || MapService.gI().isMapMabuWar(player.zone.map.mapId)) {
                if (player.inventory.gold >= COST_GOLD_HOI_SINH) {
                    player.inventory.gold -= COST_GOLD_HOI_SINH;
                    canHs = true;
                } else {
                    Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện, còn thiếu " + Util.numberToMoney(COST_GOLD_HOI_SINH
                            - player.inventory.gold) + " vàng");
                    return;
                }
            }
            if (!canHs) {
                if (player.inventory.gem > 1) {
                    player.inventory.gem -= 1;
                    canHs = true;
                } else {
                    Service.getInstance().sendThongBao(player, "Bạn không đủ ngọc xanh để hồi sinh");
                }
            }
            if (canHs) {
                Service.getInstance().sendMoney(player);
                Service.getInstance().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
                if (player.zone.map.mapId == ConstTranhNgocNamek.MAP_ID) {
                    TranhNgocService.getInstance().sendUpdateLift(player);
                }
                player.playerTask.achivements.get(ConstAchive.THANH_HOI_SINH).count++;
            }
        }
    }

    public boolean createPlayer(Connection con, int userId, String name, int gender, int hair) {
        PlayerDAO.createNewPlayer(con, userId, name, (byte) gender, hair);
        return true;
    }

    public boolean savePlayer(Player player) {
        try {
            PlayerDAO.updateTimeLogout = true;
            PlayerDAO.updatePlayer(player, DBService.gI().getConnectionForLogout());
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void setPos(Player player, int x, int y, int effID) {
        Message msg = new Message(Cmd.SET_POS);
        try {
            DataOutputStream ds = msg.writer();
            ds.writeInt((int) player.id);
            ds.writeShort(x);
            ds.writeShort(y);
            ds.writeByte(effID);
            ds.flush();
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }
    public void sendPetFollow(Player player) {
        PetFollow pet = player.getPetFollow();
        int type = 1;
        if (pet == null) type = 0;
        Message msg = new Message(Cmd.STATUS_PET);
        DataOutputStream ds = msg.writer();
        try {
            ds.writeInt((int) player.id);
            ds.writeByte(type);
            if (type == 1) {
                ds.writeShort(pet.getIconID());
                ds.writeByte(1);
                byte nFrames = pet.getNFrame();
                ds.writeByte(nFrames);
                for (int i = 0; i < nFrames; i++) {
                    ds.writeByte(i);
                }
                ds.writeShort(pet.getWidth());
                ds.writeShort(pet.getHeight());
            }
            ds.flush();
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPetFollow(Player me,Player info) {
        PetFollow pet = info.getPetFollow();
        int type = 1;
        if (pet == null) type = 0;
        Message msg = new Message(Cmd.STATUS_PET);
        DataOutputStream ds = msg.writer();
        try {
            ds.writeInt((int) info.id);
            ds.writeByte(type);
            if (type == 1) {
                ds.writeShort(pet.getIconID());
                ds.writeByte(1);
                byte nFrames = pet.getNFrame();
                ds.writeByte(nFrames);
                for (int i = 0; i < nFrames; i++) {
                    ds.writeByte(i);
                }
                ds.writeShort(pet.getWidth());
                ds.writeShort(pet.getHeight());
            }
            ds.flush();
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

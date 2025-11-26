package nro.server.io;

import nro.data.DataGame;
import nro.jdbc.daos.GodGK;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;
import nro.resources.Resources;
import nro.server.*;
import nro.server.model.AntiLogin;
import nro.services.*;
import nro.utils.Log;
import nro.utils.Util;
import lombok.Setter;
import nro.services.ItemService;
import nro.services.ItemTimeService;
import nro.services.Service;
import nro.services.TaskService;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Session {

    private static final Map<String, AntiLogin> ANTILOGIN = new HashMap<>();
    private static final int TIME_WAIT_READ_MESSAGE = 180000;

    // Thread pool TÁI SỬ DỤNG cho tất cả sessions
    private static final ExecutorService SEND_THREAD_POOL;
    private static final ExecutorService RECEIVE_THREAD_POOL;
    
    static {
        // Pool cho Send threads - số lượng = số core * 2
        int cores = Runtime.getRuntime().availableProcessors();
        SEND_THREAD_POOL = Executors.newFixedThreadPool(cores * 2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("Session-Send-Pool");
            return t;
        });
        
        // Pool cho Receive threads - số lượng = số core * 2
        RECEIVE_THREAD_POOL = Executors.newFixedThreadPool(cores * 2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("Session-Receive-Pool");
            return t;
        });
    }

    public boolean logCheck;

    private static int baseId = 0;
    public int id;
    public Player player;

    public byte timeWait = 50;

    public boolean connected;

    static final byte[] KEYS = {0};
    byte curR, curW;

    private Socket socket;
    private MessageCollector collector;
    private MessageSender sender;
    
    // Giữ reference để quản lý lifecycle
    private Future<?> senderTask;
    private Future<?> receiverTask;
    
    // Flag để đánh dấu session đang disconnect
    private volatile boolean disconnecting = false;

    Controller controller;

    public String ipAddress;
    public boolean isAdmin;
    public int userId;
    public String uu;
    public String pp;

    public int typeClient;
    public byte zoomLevel;
    public boolean isSetClientType;

    public long lastTimeLogout;
    public boolean loginSuccess, joinedGame, dataLoadFailed;

    public long lastTimeReadMessage;

    public boolean actived;

    public int goldBar;
    public List<Item> itemsReward;
    public String dataReward;
    public int ruby;
    public int diemTichNap;
    public int server;
    public int version;
    public int vnd;
    public int tongnap1;
    
    @Setter
    private boolean logging;

    public void update() {
        if (Util.canDoWithTime(lastTimeReadMessage, TIME_WAIT_READ_MESSAGE)) {
//            Client.gI().kickSession(this);
        }
    }

    public void initItemsReward() {
        try {
            this.itemsReward = new ArrayList<>();
            String[] itemsReward = dataReward.split(";");
            for (String itemInfo : itemsReward) {
                if (itemInfo == null || itemInfo.equals("")) {
                    continue;
                }
                String[] subItemInfo = itemInfo.replaceAll("[{}\\[\\]]", "").split("\\|");
                String[] baseInfo = subItemInfo[0].split(":");
                int itemId = Integer.parseInt(baseInfo[0]);
                int quantity = Integer.parseInt(baseInfo[1]);
                Item item = ItemService.gI().createNewItem((short) itemId, quantity);
                if (subItemInfo.length == 2) {
                    String[] options = subItemInfo[1].split(",");
                    for (String opt : options) {
                        if (opt == null || opt.equals("")) {
                            continue;
                        }
                        String[] optInfo = opt.split(":");
                        int tempIdOption = Integer.parseInt(optInfo[0]);
                        int param = Integer.parseInt(optInfo[1]);
                        item.itemOptions.add(new ItemOption(tempIdOption, param));
                    }
                }
                this.itemsReward.add(item);
            }
        } catch (Exception e) {
            // Silent catch
        }
    }

    public int getNumOfMessages() {
        return this.sender != null ? this.sender.getNumMessage() : 0;
    }

    public Session(Socket socket, Controller controller, String ip) {
        try {
            socket.setTcpNoDelay(true);
            this.id = baseId++;
            this.socket = socket;
            this.controller = controller;
            this.disconnecting = false;
            
            // Tạo sender và collector NHƯNG CHƯA submit vào pool
            this.sender = new MessageSender(this, socket);
            this.collector = new MessageCollector(this, socket);
            
            // Chỉ submit receiver vào pool, sender sẽ được start sau khi sendSessionKey
            this.receiverTask = RECEIVE_THREAD_POOL.submit(this.collector);
            
            Client.gI().put(this);
        } catch (Exception e) {
            Log.error(Session.class, e);
        }
    }
    
    /**
     * Start sender thread - được gọi từ MessageSender.sendSessionKey()
     */
    public void startSenderThread() {
        if (this.sender != null && this.senderTask == null && !disconnecting) {
            this.senderTask = SEND_THREAD_POOL.submit(this.sender);
        }
    }

    public void sendMessage(Message msg) {
        if (this.sender != null && !disconnecting) {
            sender.addMessage(msg);
        }
    }

    public void doSendMessage(Message msg) {
        if (this.sender != null && !disconnecting) {
            sender.doSendMessage(msg);
        }
    }

    public void disconnect() {
        if (connected && !disconnecting) {
            disconnecting = true;
            connected = false;
            curR = 0;
            curW = 0;
            
            try {
                // Đóng socket trước để ngắt I/O operations
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                
                // Đợi sender và collector dừng hoàn toàn
                if (this.sender != null) {
                    this.sender.close();
                }
                if (this.collector != null) {
                    this.collector.close();
                }
                
                // Cancel các task với timeout
                if (this.senderTask != null) {
                    this.senderTask.cancel(true);
                    try {
                        // Đợi tối đa 2 giây để task kết thúc
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    this.senderTask = null;
                }
                
                if (this.receiverTask != null) {
                    this.receiverTask.cancel(true);
                    try {
                        // Đợi tối đa 2 giây để task kết thúc
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    this.receiverTask = null;
                }
                
                // Clear references
                this.socket = null;
                this.sender = null;
                this.collector = null;
                this.uu = null;
                this.pp = null;
                
                // Clear items reward
                if (this.itemsReward != null) {
                    this.itemsReward.clear();
                    this.itemsReward = null;
                }
                
                // Clear player reference
                this.player = null;
                
            } catch (Exception e) {
                Log.error(Session.class, e);
            } finally {
                disconnecting = false;
            }
        }
    }

    public void setClientType(Message msg) {
        try {
            if (!isSetClientType) {
                this.typeClient = (msg.reader().readByte());
                this.zoomLevel = msg.reader().readByte();
                msg.reader().readBoolean();
                msg.reader().readInt();
                msg.reader().readInt();
                msg.reader().readBoolean();
                msg.reader().readBoolean();
                String platform = msg.reader().readUTF();
                String[] arrPlatform = platform.split("\\|");
                this.version = Integer.parseInt(arrPlatform[1].replaceAll("\\.", ""));
                isSetClientType = true;
                Resources.getInstance().sendResVersion(this);
            }
        } catch (Exception e) {
            // Silent catch
        } finally {
            msg.cleanup();
        }
        DataGame.sendLinkIP(this);
    }

    public boolean isVersionAbove(int version) {
        return this.version >= version;
    }

    public String getName() {
        if (this.player != null) {
            return this.player.name;
        } else {
            return this.socket != null ? String.valueOf(this.socket.getPort()) : "Unknown";
        }
    }

    public void sendSessionKey() {
        if (this.sender != null) {
            this.sender.sendSessionKey();
        }
    }

    public boolean canConnectWithIp() {
        Object o = ServerManager.CLIENTS.get(ipAddress);
        if (o == null) {
            ServerManager.CLIENTS.put(ipAddress, 1);
            return true;
        } else {
            int n = Integer.parseInt(String.valueOf(o));
            if (n < Manager.MAX_PER_IP) {
                n++;
                ServerManager.CLIENTS.put(ipAddress, n);
                return true;
            } else {
                return false;
            }
        }
    }

    public void login(String username, String password) {
        if (Maintenance.isRuning || !ServerManager.gI().getLogin().isConnected()) {
            Service.getInstance().sendThongBaoOK(this, "Máy chủ đang tiến hành bảo trì, vui lòng thử lại sau!");
            return;
        }
        if (!isSetClientType || logging || loginSuccess) {
            return;
        }
        logging = true;
        AntiLogin al = ANTILOGIN.get(this.ipAddress);
        if (al == null) {
            al = new AntiLogin();
            ANTILOGIN.put(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            Service.getInstance().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }
        if (!this.isAdmin && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.getInstance().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }
        if (this.player != null) {
            return;
        } else {
            Player player = null;
            try {
                long st = System.currentTimeMillis();
                this.uu = username;
                this.pp = password;
                ServerManager.gI().getLogin().getService().login(Manager.SERVER, this.id, username, password);
            } catch (Exception e) {
                e.printStackTrace();
                if (player != null) {
                    player.dispose();
                }
            }
        }
    }

    public void finishUpdate() {
        if (loginSuccess && !joinedGame) {
            player = GodGK.loadPlayer(this);
            if (!dataLoadFailed) {
                if (player != null) {
                    enter();
                } else {
                    Service.getInstance().switchToCreateChar(this);
                }
            } else {
                Service.getInstance().sendThongBaoOK(this, "Lỗi tải dữ liệu vui lòng báo với admin.");
            }
        }
    }

    public void enter() {
        if (!joinedGame) {
            joinedGame = true;
            player.nPoint.initPowerLimit();
            if (player.pet != null) {
                player.pet.nPoint.initPowerLimit();
            }
            player.nPoint.calPoint();
            player.nPoint.setHp(player.nPoint.hp);
            player.nPoint.setMp(player.nPoint.mp);
            player.zone.addPlayer(player);
            player.loaded = true;
            if (player.pet != null) {
                player.pet.nPoint.calPoint();
                player.pet.nPoint.setHp(player.pet.nPoint.hp);
                player.pet.nPoint.setMp(player.pet.nPoint.mp);
            }

            player.setSession(this);
            Client.gI().put(player);
            controller.sendInfo(this);
            Service.getInstance().player(player);
            Service.getInstance().Send_Caitrang(player);
            PhucLoi.gI().Send_PhucLoi(player);
            BangTin.gI().Send_BangTin(player);
            TamBao.gI().Send_TamBao(player);
            TamBao.gI().Send_MocTamBao(player);
            TamBao.gI().Send_QuayThuong(player);
            KhamNgoc.gI().Send_KhamNgocTemplate(player);
            KhamNgoc.gI().Send_KhamNgoc_Player(player);
            RuongSuuTam.gI().Send_RuongSuuTamTemplate(player);
            RuongSuuTam.gI().SendAllRuong(player);
            PhongThiNghiem.gI().Send_PhongThiNghiem_Template(player);
            PhongThiNghiem.gI().Send_PhongThiNghiem_Player(player);
            GameDuDoan.gI().Send_TaiXiu(player);
            GameDuDoan.gI().thongbao("");
            
            // -64 my flag bag
            Service.getInstance().sendFlagBag(player);

            // -113 skill shortcut
            player.playerSkill.sendSkillShortCut();
            
            // item time
            ItemTimeService.gI().sendAllItemTime(player);

            // send current task
            TaskService.gI().sendInfoCurrentTask(player);   
        }
    }
    
    public void loadPlayer() {
        controller.sendInfo(this);
        Service.getInstance().player(player);
        Service.getInstance().Send_Caitrang(player);

        // -64 my flag bag
        Service.getInstance().sendFlagBag(player);

        // -113 skill shortcut
        player.playerSkill.sendSkillShortCut();
    }
    
    /**
     * Shutdown thread pools khi server tắt
     * Gọi method này trong ServerManager.close()
     */
    public static void shutdownThreadPools() {
        try {
            if (SEND_THREAD_POOL != null && !SEND_THREAD_POOL.isShutdown()) {
                SEND_THREAD_POOL.shutdown();
                if (!SEND_THREAD_POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                    List<Runnable> pendingTasks = SEND_THREAD_POOL.shutdownNow();
                    System.out.println("Force shutdown SEND_THREAD_POOL. Pending tasks: " + pendingTasks.size());
                }
            }
            if (RECEIVE_THREAD_POOL != null && !RECEIVE_THREAD_POOL.isShutdown()) {
                RECEIVE_THREAD_POOL.shutdown();
                if (!RECEIVE_THREAD_POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                    List<Runnable> pendingTasks = RECEIVE_THREAD_POOL.shutdownNow();
                    System.out.println("Force shutdown RECEIVE_THREAD_POOL. Pending tasks: " + pendingTasks.size());
                }
            }
        } catch (Exception e) {
            Log.error(Session.class, e);
        }
    }
}
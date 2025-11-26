package nro.server;

import nro.attr.AttributeManager;
import nro.jdbc.DBService;
import nro.jdbc.daos.AccountDAO;
import nro.jdbc.daos.HistoryTransactionDAO;
import nro.jdbc.daos.PlayerDAO;
import nro.login.LoginSession;
import nro.manager.ConsignManager;
import nro.manager.TopManager;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import nro.models.map.challenge.MartialCongressManager;
import nro.models.map.dungeon.DungeonManager;
import nro.models.map.phoban.BanDoKhoBau;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Player;
import nro.netty.NettyServer;
import nro.server.io.Session;
import nro.services.ClanService;
import nro.utils.Log;
import nro.utils.TimeUtil;
import nro.utils.Util;

import javax.swing.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import nro.models.map.challenge.sieuhang.SieuHangManager;
import nro.services.Service;

public class ServerManager {

    public static String timeStart;
    public static final Map<String, Integer> CLIENTS = new HashMap<>();
    public static String NAME = "nro";
    public static int PORT = 14445;

    private Controller controller;
    private static ServerManager instance;
    public static ServerSocket listenSocket;
    public static boolean isRunning;

    @Getter
    private LoginSession login;
    public static boolean updateTimeLogin;
    @Getter @Setter
    private AttributeManager attributeManager;
    private long lastUpdateAttribute;
    @Getter
    private DungeonManager dungeonManager;

    // Thread pool tối ưu - TÁI SỬ DỤNG HOÀN TOÀN
    private ScheduledExecutorService gameLoopExecutor;      // Game loops (4 threads)
    private ExecutorService commandExecutor;                 // Commands (2 threads)
    private ScheduledExecutorService autoSaveExecutor;       // Auto save (1 thread)
    private ExecutorService ioExecutor;                      // I/O operations (2 threads)

    public void init() {
        Manager.gI();
        HistoryTransactionDAO.deleteHistory();
        BossFactory.initBoss();
        this.controller = new Controller();
        if (updateTimeLogin) {
            AccountDAO.updateLastTimeLoginAllAccount();
        }
        
        // Thread pool cho game loops - 4 threads
        gameLoopExecutor = Executors.newScheduledThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("GameLoop-Thread");
            t.setPriority(Thread.NORM_PRIORITY + 1); // Ưu tiên cao hơn
            return t;
        });
        
        // Thread pool cho commands - 2 threads
        commandExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("Command-Thread");
            return t;
        });
        
        // Thread pool cho auto save - 1 thread
        autoSaveExecutor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("AutoSave-Thread");
            return t;
        });
        
        // Thread pool cho I/O operations - 2 threads
        ioExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("IO-Thread");
            return t;
        });
    }

    public static ServerManager gI() {
        if (instance == null) {
            instance = new ServerManager();
            instance.init();
        }
        return instance;
    }

    public static void main(String[] args) {
        timeStart = TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss");
        ServerManager.gI().run();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[SHUTDOWN] Đang tắt server...");
            Service.getInstance().shutdown();
        }, "Shutdown-Hook"));
    }

    public void run() {
        isRunning = true;

        // Mở giao diện - sử dụng SwingUtilities (không cần fix)
        SwingUtilities.invokeLater(() -> {
            AntiDdosFrame frame = new AntiDdosFrame();
            frame.setVisible(true);
            bindButtons(frame);
        });

        activeCommandLine();
        activeGame();
        activeLogin();
        autoTask();
        
        // Recharge HTTP - dùng ioExecutor thay vì tạo thread mới
        ioExecutor.submit(() -> {
            try { 
                nro.recharge.RechargeHttp.start(); 
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
        });
        
        NettyServer nettyServer = new NettyServer();
        nettyServer.start();
        activeServerSocket();
    }

   private void bindButtons(AntiDdosFrame frame) {
    // Nút Bảo trì - sử dụng commandExecutor
    frame.btnBaoTri.addActionListener(e -> {
        frame.appendServerLog("👉 Đã kích hoạt bảo trì!");
        commandExecutor.submit(() -> Maintenance.gI().start(10));
    });

    // Nút Đá all player - sử dụng commandExecutor
    frame.btnDaAll.addActionListener(e -> {
        frame.appendServerLog("👉 Đã đá toàn bộ người chơi!");
        commandExecutor.submit(() -> Client.gI().close());
    });

    // ✅ NÚT THU HỒI VẬT PHẨM - THÊM MỚI
    frame.btnItemRecovery.addActionListener(e -> {
        frame.appendServerLog("👉 Đã mở panel thu hồi vật phẩm!");
        SwingUtilities.invokeLater(() -> {
            ItemRecoveryPanel panel = new ItemRecoveryPanel();
            panel.setVisible(true);
        });
    });
}

    public void activeLogin() {
        login = new LoginSession();
        login.connect(Manager.loginHost, Manager.loginPort);
    }

    private void activeServerSocket() {
        try {
            Log.log("Start server......... Current thread: " + Thread.activeCount());
            listenSocket = new ServerSocket(PORT);
            while (isRunning) {
                try {
                    Socket sc = listenSocket.accept();
                    String ip = (((InetSocketAddress) sc.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
                    if (canConnectWithIp(ip)) {
                        Session session = new Session(sc, controller, ip);
                        session.ipAddress = ip;
                    } else {
                        sc.close();
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            listenSocket.close();
        } catch (Exception e) {
            Log.error(ServerManager.class, e, "Lỗi mở port");
            System.exit(0);
        }
    }

    private boolean canConnectWithIp(String ipAddress) {
        synchronized (CLIENTS) { // Thêm sync để thread-safe
            Object o = CLIENTS.get(ipAddress);
            if (o == null) {
                CLIENTS.put(ipAddress, 1);
                return true;
            } else {
                int n = Integer.parseInt(String.valueOf(o));
                if (n < Manager.MAX_PER_IP) {
                    n++;
                    CLIENTS.put(ipAddress, n);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public void disconnect(Session session) {
        synchronized (CLIENTS) { // Thêm sync để thread-safe
            Object o = CLIENTS.get(session.ipAddress);
            if (o != null) {
                int n = Integer.parseInt(String.valueOf(o));
                n--;
                if (n < 0) n = 0;
                CLIENTS.put(session.ipAddress, n);
            }
        }
    }

    private void activeCommandLine() {
        // Command line - SỬ DỤNG commandExecutor thay vì tạo thread mới
        commandExecutor.submit(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                try {
                    String line = sc.nextLine();
                    if (line.equals("baotri")) {
                        commandExecutor.submit(() -> Maintenance.gI().start(5));
                    } else if (line.equals("athread")) {
                        ServerNotify.gI().notify("Debug server: " + Thread.activeCount());
                    } else if (line.equals("nplayer")) {
                        Log.error("Player in game: " + Client.gI().getPlayers().size());
                    } else if (line.equals("a")) {
                        commandExecutor.submit(() -> Client.gI().close());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void activeGame() {
        long delay = 500;
        
        // Dungeon manager - SỬ DỤNG gameLoopExecutor thay vì tạo thread mới
        dungeonManager = new DungeonManager();
        dungeonManager.start();
        gameLoopExecutor.submit(dungeonManager); // SỬ DỤNG executor thay vì new Thread
        
        // Game Loop 1: Update Boss
        gameLoopExecutor.scheduleWithFixedDelay(() -> {
            try {
                BossManager.gI().updateAllBoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, delay, TimeUnit.MILLISECONDS);
        
        // Game Loop 2: Update Pho Ban
        gameLoopExecutor.scheduleWithFixedDelay(() -> {
            try {
                for (DoanhTrai dt : DoanhTrai.DOANH_TRAIS) {
                    dt.update();
                }
                for (BanDoKhoBau bdkb : BanDoKhoBau.BAN_DO_KHO_BAUS) {
                    bdkb.update();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, delay, TimeUnit.MILLISECONDS);
        
        // Game Loop 3: Update Attribute
        gameLoopExecutor.scheduleWithFixedDelay(() -> {
            try {
                if (attributeManager != null) {
                    attributeManager.update();
                    if (Util.canDoWithTime(lastUpdateAttribute, 600000)) {
                        Manager.gI().updateAttributeServer();
                        lastUpdateAttribute = System.currentTimeMillis();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, delay, TimeUnit.MILLISECONDS);
        
        // Game Loop 4: Update Martial Congress
        gameLoopExecutor.scheduleWithFixedDelay(() -> {
            try {
                MartialCongressManager.gI().update();
                //VoDaiSinhTuManager.gI().update();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, delay, TimeUnit.MILLISECONDS);
    }

    public void close(long delay) {
        try {
            dungeonManager.shutdown();
        } catch (Exception e) {
            Log.error(ServerManager.class, e);
        }
        try {
            Manager.gI().updateEventCount();
        } catch (Exception e) {
            Log.error(ServerManager.class, e);
        }
        try {
            Manager.gI().updateAttributeServer();
        } catch (Exception e) {
            Log.error(ServerManager.class, e);
        }
        try {
            Client.gI().close();
        } catch (Exception e) {
            Log.error(ServerManager.class, e);
        }
        try {
            ClanService.gI().close();
        } catch (Exception e) {
            Log.error(ServerManager.class, e);
        }
        try {
            ConsignManager.getInstance().close();
        } catch (Exception e) {
            Log.error(ServerManager.class, e);
        }
        
        // Shutdown thread pools theo thứ tự
        shutdownExecutor(gameLoopExecutor, "GameLoop");
        shutdownExecutor(commandExecutor, "Command");
        shutdownExecutor(autoSaveExecutor, "AutoSave");
        shutdownExecutor(ioExecutor, "IO");
        
        Client.gI().close();
        Log.success("BẢO TRÌ THÀNH CÔNG!...................................");
        System.exit(0);
    }
    
    // Helper method để shutdown executor an toàn
    private void shutdownExecutor(ExecutorService executor, String name) {
        if (executor == null) return;
        try {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                Log.log("Force shutdown " + name + " executor...");
                executor.shutdownNow();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    Log.error(name + " executor did not terminate");
                }
            }
        } catch (Exception e) {
            Log.error(ServerManager.class, e);
            executor.shutdownNow();
        }
    }

    public void saveAll(boolean updateTimeLogout) {
        try {
            List<Player> list = Client.gI().getPlayers();
            try (Connection conn = DBService.gI().getConnectionForAutoSave()) {
                for (Player player : list) {
                    try {
                        PlayerDAO.updateTimeLogout = updateTimeLogout;
                        PlayerDAO.updatePlayer(player, conn);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void autoTask() {
        // Auto save mỗi 5 phút
        autoSaveExecutor.scheduleWithFixedDelay(() -> {
            try {
                saveAll(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 300000, 300000, TimeUnit.MILLISECONDS);
    }
}
package nro.server;


import nro.jdbc.DBService;
import nro.models.player.Player;
import nro.services.Service;
import nro.server.Client;
import nro.server.io.Session;
import nro.utils.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRecoveryPanel extends JFrame {
    
    private JComboBox<PlayerItem> playerComboBox;
    private JComboBox<ItemTemplate> itemComboBox;
    private JCheckBox chkBody, chkBag, chkBox, chkPetBody;
    private JButton btnRecover, btnRefresh, btnKickOnly;
    private JTable playerTable, itemTable;
    private DefaultTableModel playerTableModel, itemTableModel;
    
    private static final String EMPTY_ITEM = "{\"quantity\":0,\"create_time\":0,\"temp_id\":-1,\"option\":[]}";
    
    public ItemRecoveryPanel() {
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setTitle("Thu Hồi Vật Phẩm - NRO");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top Panel - Selection
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Chọn Thu Hồi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Player Selection
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Người chơi:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        playerComboBox = new JComboBox<>();
        playerComboBox.addItem(new PlayerItem(-1, "--- TẤT CẢ NGƯỜI CHƠI ---"));
        topPanel.add(playerComboBox, gbc);
        
        // Item Selection
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        topPanel.add(new JLabel("Vật phẩm:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        itemComboBox = new JComboBox<>();
        topPanel.add(itemComboBox, gbc);
        
        // Location Checkboxes
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel locationPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        locationPanel.setBorder(BorderFactory.createTitledBorder("Vị trí thu hồi"));
        
        // Player items
        JPanel playerItemsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        playerItemsPanel.setBorder(BorderFactory.createTitledBorder("Người chơi"));
        chkBody = new JCheckBox("Body", true);
        chkBag = new JCheckBox("Bag", true);
        chkBox = new JCheckBox("Box", true);
        playerItemsPanel.add(chkBody);
        playerItemsPanel.add(chkBag);
        playerItemsPanel.add(chkBox);
        
        // Pet items
        JPanel petItemsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        petItemsPanel.setBorder(BorderFactory.createTitledBorder("Đệ tử"));
        chkPetBody = new JCheckBox("Pet Body", true);
        petItemsPanel.add(chkPetBody);
        
        locationPanel.add(playerItemsPanel);
        locationPanel.add(petItemsPanel);
        topPanel.add(locationPanel, gbc);
        
        // Buttons
        gbc.gridy = 3;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        btnRecover = new JButton("🔄 Thu Hồi Vật Phẩm");
        btnRecover.setBackground(new Color(231, 76, 60));
        btnRecover.setForeground(Color.WHITE);
        btnRecover.setFont(new Font("Arial", Font.BOLD, 14));
        btnRecover.setFocusPainted(false);
        btnRecover.addActionListener(e -> recoverItems());
        
        btnKickOnly = new JButton("👢 Chỉ Đá Player");
        btnKickOnly.setBackground(new Color(230, 126, 34));
        btnKickOnly.setForeground(Color.WHITE);
        btnKickOnly.setFont(new Font("Arial", Font.BOLD, 14));
        btnKickOnly.setFocusPainted(false);
        btnKickOnly.addActionListener(e -> kickPlayersOnly());
        
        btnRefresh = new JButton("🔃 Làm Mới");
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadData());
        
        buttonPanel.add(btnRecover);
        buttonPanel.add(btnKickOnly);
        buttonPanel.add(btnRefresh);
        topPanel.add(buttonPanel, gbc);
        
        // Center Panel - Tables
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Player Table
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setBorder(BorderFactory.createTitledBorder("Danh Sách Người Chơi"));
        
        String[] playerColumns = {"ID", "Tên", "Trạng thái"};
        playerTableModel = new DefaultTableModel(playerColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        playerTable = new JTable(playerTableModel);
        playerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane playerScrollPane = new JScrollPane(playerTable);
        playerPanel.add(playerScrollPane, BorderLayout.CENTER);
        
        // Item Table
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBorder(BorderFactory.createTitledBorder("Danh Sách Vật Phẩm"));
        
        String[] itemColumns = {"ID", "Tên Vật Phẩm"};
        itemTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemTable = new JTable(itemTableModel);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane itemScrollPane = new JScrollPane(itemTable);
        itemPanel.add(itemScrollPane, BorderLayout.CENTER);
        
        centerPanel.add(playerPanel);
        centerPanel.add(itemPanel);
        
        // Add to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void loadData() {
        loadPlayers();
        loadItems();
    }
    
    private void loadPlayers() {
        playerComboBox.removeAllItems();
        playerComboBox.addItem(new PlayerItem(-1, "--- TẤT CẢ NGƯỜI CHƠI ---"));
        playerTableModel.setRowCount(0);
        
        String sql = "SELECT id, name FROM player ORDER BY name";
        
        try (Connection conn = DBService.gI().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                
                // Check if player is online
                Player player = Client.gI().getPlayer(id);
                String status = (player != null && player.getSession() != null) ? "🟢 Online" : "⚫ Offline";
                
                playerComboBox.addItem(new PlayerItem(id, name));
                playerTableModel.addRow(new Object[]{id, name, status});
            }
            
            Log.success("Đã load " + (playerComboBox.getItemCount() - 1) + " người chơi");
            
        } catch (Exception e) {
            Log.error(ItemRecoveryPanel.class, e);
            JOptionPane.showMessageDialog(this, 
                "Lỗi load danh sách người chơi: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadItems() {
        itemComboBox.removeAllItems();
        itemTableModel.setRowCount(0);
        
        String sql = "SELECT id, name FROM item_template ORDER BY id";
        
        try (Connection conn = DBService.gI().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                
                itemComboBox.addItem(new ItemTemplate(id, name));
                itemTableModel.addRow(new Object[]{id, name});
            }
            
            Log.success("Đã load " + itemComboBox.getItemCount() + " vật phẩm");
            
        } catch (Exception e) {
            Log.error(ItemRecoveryPanel.class, e);
            JOptionPane.showMessageDialog(this,
                "Lỗi load danh sách vật phẩm: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * CHỈ ĐÁ PLAYER - KHÔNG THU HỒI
     */
    private void kickPlayersOnly() {
        PlayerItem selectedPlayer = (PlayerItem) playerComboBox.getSelectedItem();
        
        if (selectedPlayer == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người chơi!",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String playerName = selectedPlayer.id == -1 ? "TẤT CẢ NGƯỜI CHƠI" : selectedPlayer.name;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đá:\n" +
            "- Người chơi: " + playerName + "\n\n" +
            "⚠️ Player sẽ bị kickSession ngay lập tức!",
            "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        btnKickOnly.setEnabled(false);
        btnKickOnly.setText("Đang đá...");
        
        new Thread(() -> {
            try {
                int kickedCount = kickPlayersBySession(selectedPlayer.id);
                
                SwingUtilities.invokeLater(() -> {
                    String message = "✅ Đá thành công!\n\n" +
                                   "👢 Đã đá: " + kickedCount + " player";
                    
                    JOptionPane.showMessageDialog(this, message,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    
                    btnKickOnly.setEnabled(true);
                    btnKickOnly.setText("👢 Chỉ Đá Player");
                    
                    // Refresh trạng thái
                    refreshPlayerStatus();
                });
                
            } catch (Exception e) {
                Log.error(ItemRecoveryPanel.class, e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Lỗi khi đá player: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    
                    btnKickOnly.setEnabled(true);
                    btnKickOnly.setText("👢 Chỉ Đá Player");
                });
            }
        }).start();
    }
    
    /**
     * REFRESH TRẠNG THÁI PLAYER - KHÔNG LOAD LẠI DATABASE
     */
    private void refreshPlayerStatus() {
        SwingUtilities.invokeLater(() -> {
            // Update table status
            for (int i = 0; i < playerTableModel.getRowCount(); i++) {
                int playerId = (int) playerTableModel.getValueAt(i, 0);
                Player player = Client.gI().getPlayer(playerId);
                String status = (player != null && player.getSession() != null) ? "🟢 Online" : "⚫ Offline";
                playerTableModel.setValueAt(status, i, 2);
            }
        });
    }
    
    /**
     * ĐÁ PLAYER BẰNG kickSession - GIỐNG HỆT CHỨC NĂNG ĐÁ ALL
     * @param playerId ID của player cần đá (-1 = all)
     * @return Số lượng player đã đá
     */
    private int kickPlayersBySession(int playerId) {
        int kickedCount = 0;
        
        if (playerId == -1) {
            // ĐÁ ALL - GIỐNG HỆT Client.gI().close()
            List<Session> sessionsToKick = new ArrayList<>(Client.gI().getSessions());
            
            for (Session session : sessionsToKick) {
                try {
                    if (session != null && session.player != null) {
                        Log.log("👢 Đá session: " + session.player.name + " (ID: " + session.player.id + ")");
                        Client.gI().kickSession(session);
                        kickedCount++;
                    }
                } catch (Exception e) {
                    Log.error(ItemRecoveryPanel.class, e, "Lỗi đá session");
                }
            }
        } else {
            // ĐÁ 1 PLAYER CỤ THỂ
            Player player = Client.gI().getPlayer(playerId);
            if (player != null && player.getSession() != null) {
                try {
                    Log.log("👢 Đá session: " + player.name + " (ID: " + player.id + ")");
                    Client.gI().kickSession(player.getSession());
                    kickedCount = 1;
                } catch (Exception e) {
                    Log.error(ItemRecoveryPanel.class, e, "Lỗi đá session: " + player.name);
                }
            } else {
                Log.log("ℹ️ Player ID " + playerId + " không online");
            }
        }
        
        return kickedCount;
    }
    
    /**
     * THU HỒI VẬT PHẨM + ĐÁ PLAYER
     */
    private void recoverItems() {
        PlayerItem selectedPlayer = (PlayerItem) playerComboBox.getSelectedItem();
        ItemTemplate selectedItem = (ItemTemplate) itemComboBox.getSelectedItem();
        
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vật phẩm cần thu hồi!",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!chkBody.isSelected() && !chkBag.isSelected() && 
            !chkBox.isSelected() && !chkPetBody.isSelected()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một vị trí thu hồi!",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String playerName = selectedPlayer.id == -1 ? "TẤT CẢ" : selectedPlayer.name;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn thu hồi vật phẩm:\n" +
            "- Vật phẩm: " + selectedItem.name + " (ID: " + selectedItem.id + ")\n" +
            "- Người chơi: " + playerName + "\n" +
            "- Vị trí: " + getSelectedLocations() + "\n\n" +
            "⚠️ QUY TRÌNH:\n" +
            "1. Tìm player có item từ database\n" +
            "2. Gửi thông báo cho player\n" +
            "3. Countdown 5 giây\n" +
            "4. Đá player (kickSession)\n" +
            "5. Đợi 3 giây\n" +
            "6. Thu hồi vật phẩm từ database\n" +
            "7. Player có thể login lại ngay!",
            "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        btnRecover.setEnabled(false);
        btnRecover.setText("Đang xử lý...");
        
        new Thread(() -> {
            try {
                RecoveryResult result = performRecovery(selectedPlayer.id, selectedItem.id);
                
                SwingUtilities.invokeLater(() -> {
                    String message = "✅ Thu hồi hoàn tất!\n\n" +
                                   "📊 Tổng: " + result.totalAffected + " người chơi\n" +
                                   "👢 Đã đá: " + result.kickedPlayers + " player\n" +
                                   "💾 Đã thu hồi: " + result.dbUpdated + " player\n\n" +
                                   "ℹ️ Player có thể đăng nhập lại ngay!";
                    
                    JOptionPane.showMessageDialog(this, message,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    
                    btnRecover.setEnabled(true);
                    btnRecover.setText("🔄 Thu Hồi Vật Phẩm");
                    
                    // Refresh trạng thái
                    refreshPlayerStatus();
                });
                
            } catch (Exception e) {
                Log.error(ItemRecoveryPanel.class, e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Lỗi khi thu hồi: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    
                    btnRecover.setEnabled(true);
                    btnRecover.setText("🔄 Thu Hồi Vật Phẩm");
                });
            }
        }).start();
    }
    
    /**
     * QUY TRÌNH THU HỒI:
     * 1. Tìm player có item từ DATABASE (query 1 lần)
     * 2. ĐÁ player online bằng kickSession (không update DB)
     * 3. Đợi 3 giây
     * 4. Thu hồi vật phẩm từ DATABASE
     */
    private RecoveryResult performRecovery(int playerId, int itemId) throws SQLException, Exception {
        RecoveryResult result = new RecoveryResult();
        JSONParser parser = new JSONParser();
        
        // Map lưu player cần thu hồi
        Map<Integer, PlayerDataForRecovery> playersNeedRecovery = new HashMap<>();
        
        // Build WHERE clause
        String whereClause = playerId == -1 ? "" : " WHERE id = ?";
        String sql = "SELECT id, name, items_body, items_bag, items_box, pet_body FROM player" + whereClause;
        
        Connection conn = null;
        
        try {
            conn = DBService.gI().getConnection();
            
            // ========== BƯỚC 1: TÌM PLAYER CÓ ITEM - QUERY 1 LẦN ==========
            Log.log("🔍 BƯỚC 1: Query database tìm player có item " + itemId + "...");
            
            try (PreparedStatement selectStmt = conn.prepareStatement(sql)) {
                if (playerId != -1) {
                    selectStmt.setInt(1, playerId);
                }
                
                ResultSet rs = selectStmt.executeQuery();
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    
                    String itemsBody = chkBody.isSelected() ? rs.getString("items_body") : null;
                    String itemsBag = chkBag.isSelected() ? rs.getString("items_bag") : null;
                    String itemsBox = chkBox.isSelected() ? rs.getString("items_box") : null;
                    String petBody = chkPetBody.isSelected() ? rs.getString("pet_body") : null;
                    
                    // Kiểm tra player có item không
                    if (hasItemToRecover(itemsBody, itemsBag, itemsBox, petBody, itemId)) {
                        PlayerDataForRecovery playerData = new PlayerDataForRecovery();
                        playerData.id = id;
                        playerData.name = name;
                        playerData.itemsBody = itemsBody;
                        playerData.itemsBag = itemsBag;
                        playerData.itemsBox = itemsBox;
                        playerData.petBody = petBody;
                        
                        playersNeedRecovery.put(id, playerData);
                        result.totalAffected++;
                    }
                }
                rs.close();
            }
            
            Log.success("✅ Tìm thấy " + result.totalAffected + " player cần thu hồi");
            
            if (result.totalAffected == 0) {
                Log.log("ℹ️ Không có player nào có item này!");
                return result;
            }
            
            // ========== BƯỚC 2: THÔNG BÁO CHO PLAYER ==========
            Log.log("📢 BƯỚC 2: Gửi thông báo cho player online...");
            
            List<Player> playersToKick = new ArrayList<>();
            for (PlayerDataForRecovery playerData : playersNeedRecovery.values()) {
                Player player = Client.gI().getPlayer(playerData.id);
                if (player != null && player.getSession() != null) {
                    playersToKick.add(player);
                    try {
                        Service.getInstance().sendThongBao(player, 
                            "️ Admin đang thu hồi vật phẩm!");
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
            
            Log.success(" Đã gửi thông báo cho " + playersToKick.size() + " player");
            
            // ========== BƯỚC 3: COUNTDOWN 5 GIÂY ==========
            if (!playersToKick.isEmpty()) {
                Log.log(" BƯỚC 3: Countdown 5 giây...");
                
                for (int i = 5; i > 0; i--) {
                    final int countdown = i;
                    for (Player player : playersToKick) {
                        try {
                            if (player != null && player.getSession() != null) {
                                Service.getInstance().sendThongBao(player, 
                                    " Bạn sẽ bị đá va thu hoi sau " + countdown + " giây...");
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                Log.success(" Countdown hoàn tất!");
            }
            
            // ========== BƯỚC 4: ĐÁ PLAYER BẰNG kickSession ==========
            Log.log("BƯỚC 4: Đá player bằng kickSession...");
            
            for (Player player : playersToKick) {
                try {
                    if (player != null && player.getSession() != null) {
                        Client.gI().kickSession(player.getSession());
                        result.kickedPlayers++;
                        Log.log(" Đã đá: " + player.name + " (ID: " + player.id + ")");
                    }
                } catch (Exception e) {
                    Log.error(ItemRecoveryPanel.class, e, "Lỗi đá player: " + player.name);
                }
            }
            
            Log.success(" Đã đá " + result.kickedPlayers + " player");
            
            // ========== BƯỚC 5: ĐỢI 3 GIÂY ĐỂ SESSION ĐÓNG HOÀN TOÀN ==========
            if (result.kickedPlayers > 0) {
                Log.log(" BƯỚC 5: Đợi 3 giây để session đóng hoàn toàn...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Log.success(" Đã đợi xong!");
            }
            
            // ========== BƯỚC 6: THU HỒI TỪ DATABASE ==========
            Log.log(" BƯỚC 6: Thu hồi vật phẩm từ database...");
            
            conn.setAutoCommit(false);
            
            try {
                for (PlayerDataForRecovery playerData : playersNeedRecovery.values()) {
                    boolean modified = false;
                    
                    String newItemsBody = playerData.itemsBody;
                    String newItemsBag = playerData.itemsBag;
                    String newItemsBox = playerData.itemsBox;
                    String newPetBody = playerData.petBody;
                    
                    // Thu hồi từng vị trí
                    if (newItemsBody != null && !newItemsBody.isEmpty()) {
                        String result_body = removeItemFromJson(parser, newItemsBody, itemId);
                        if (result_body != null) {
                            newItemsBody = result_body;
                            modified = true;
                        }
                    }
                    
                    if (newItemsBag != null && !newItemsBag.isEmpty()) {
                        String result_bag = removeItemFromJson(parser, newItemsBag, itemId);
                        if (result_bag != null) {
                            newItemsBag = result_bag;
                            modified = true;
                        }
                    }
                    
                    if (newItemsBox != null && !newItemsBox.isEmpty()) {
                        String result_box = removeItemFromJson(parser, newItemsBox, itemId);
                        if (result_box != null) {
                            newItemsBox = result_box;
                            modified = true;
                        }
                    }
                    
                    if (newPetBody != null && !newPetBody.isEmpty()) {
                        String result_pet = removeItemFromJson(parser, newPetBody, itemId);
                        if (result_pet != null) {
                            newPetBody = result_pet;
                            modified = true;
                        }
                    }
                    
                    // Update database
                    if (modified) {
                        try {
                            // Validate
                            validateJsonStrings(parser, newItemsBody, newItemsBag, newItemsBox, newPetBody);
                            
                            // Backup
                            backupPlayerData(conn, playerData.id, playerData.itemsBody, 
                                           playerData.itemsBag, playerData.itemsBox, playerData.petBody);
                            
                            // Update
                            updatePlayerItems(conn, playerData.id, newItemsBody, newItemsBag, 
                                            newItemsBox, newPetBody);
                            
                            result.dbUpdated++;
                            Log.success("✓ Thu hồi từ: " + playerData.name + " (ID: " + playerData.id + ")");
                            
                        } catch (Exception e) {
                            Log.error(ItemRecoveryPanel.class, e, 
                                "⚠️ LỖI update " + playerData.name + ": " + e.getMessage());
                        }
                    }
                }
                
                conn.commit();
                Log.success("✅ HOÀN TẤT! Thu hồi từ " + result.dbUpdated + " player");
                
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
            
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        return result;
    }
    
    private boolean hasItemToRecover(String itemsBody, String itemsBag, 
                                     String itemsBox, String petBody, int itemId) {
        return (itemsBody != null && containsItem(itemsBody, itemId)) ||
               (itemsBag != null && containsItem(itemsBag, itemId)) ||
               (itemsBox != null && containsItem(itemsBox, itemId)) ||
               (petBody != null && containsItem(petBody, itemId));
    }
    
    private boolean containsItem(String jsonStr, int itemId) {
        if (jsonStr == null || jsonStr.isEmpty()) return false;
        
        try {
            JSONParser parser = new JSONParser();
            JSONArray items = (JSONArray) parser.parse(jsonStr);
            
            for (int i = 0; i < items.size(); i++) {
                JSONObject item = (JSONObject) items.get(i);
                if (item == null) continue;
                
                Object tempIdObj = item.get("temp_id");
                int tempId = getIntValue(tempIdObj);
                
                if (tempId == itemId) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        return false;
    }
    
    private String removeItemFromJson(JSONParser parser, String jsonStr, int itemId) {
        if (jsonStr == null || jsonStr.isEmpty()) return null;
        
        try {
            JSONArray items = (JSONArray) parser.parse(jsonStr);
            if (items == null || items.isEmpty()) return null;
            
            boolean found = false;
            JSONArray newItems = new JSONArray();
            
            for (int i = 0; i < items.size(); i++) {
                Object itemObj = items.get(i);
                
                if (itemObj == null) {
                    JSONObject emptyItem = (JSONObject) parser.parse(EMPTY_ITEM);
                    newItems.add(emptyItem);
                    continue;
                }
                
                JSONObject item = (JSONObject) itemObj;
                int tempId = getIntValue(item.get("temp_id"));
                
                if (tempId == itemId) {
                    // Thay bằng EMPTY_ITEM
                    JSONObject emptyItem = (JSONObject) parser.parse(EMPTY_ITEM);
                    newItems.add(emptyItem);
                    found = true;
                } else {
                    // Giữ nguyên
                    newItems.add(item);
                }
            }
            
            if (!found) {
                return null;
            }
            
            String result = newItems.toJSONString();
            
            // Validate
            try {
                JSONArray testParse = (JSONArray) parser.parse(result);
                if (testParse.size() != items.size()) {
                    Log.error(ItemRecoveryPanel.class, null, 
                        "⚠️ LỖI: Số lượng item không khớp!");
                    return null;
                }
            } catch (Exception e) {
                Log.error(ItemRecoveryPanel.class, e, "⚠️ LỖI: JSON không hợp lệ!");
                return null;
            }
            
            return result;
            
        } catch (Exception e) {
            Log.error(ItemRecoveryPanel.class, e, "Lỗi parse JSON");
            return null;
        }
    }
    
    private int getIntValue(Object obj) {
        if (obj instanceof Long) {
            return ((Long) obj).intValue();
        } else if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    private void validateJsonStrings(JSONParser parser, String... jsonStrings) throws Exception {
        for (String json : jsonStrings) {
            if (json != null && !json.isEmpty()) {
                parser.parse(json);
            }
        }
    }
    
    private void backupPlayerData(Connection conn, int playerId, 
                                  String itemsBody, String itemsBag, 
                                  String itemsBox, String petBody) {
        try {
            String sql = "INSERT INTO player_item_backup " +
                        "(player_id, items_body, items_bag, items_box, pet_body, backup_time) " +
                        "VALUES (?, ?, ?, ?, ?, NOW())";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, playerId);
                stmt.setString(2, itemsBody);
                stmt.setString(3, itemsBag);
                stmt.setString(4, itemsBox);
                stmt.setString(5, petBody);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            // Ignore nếu không có bảng backup
        }
    }
    
    private void updatePlayerItems(Connection conn, int playerId, 
                                   String itemsBody, String itemsBag, 
                                   String itemsBox, String petBody) throws SQLException {
        
        StringBuilder sql = new StringBuilder("UPDATE player SET ");
        List<String> updates = new ArrayList<>();
        
        if (itemsBody != null) updates.add("items_body = ?");
        if (itemsBag != null) updates.add("items_bag = ?");
        if (itemsBox != null) updates.add("items_box = ?");
        if (petBody != null) updates.add("pet_body = ?");
        
        if (updates.isEmpty()) return;
        
        sql.append(String.join(", ", updates));
        sql.append(" WHERE id = ?");
        
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            
            if (itemsBody != null) stmt.setString(paramIndex++, itemsBody);
            if (itemsBag != null) stmt.setString(paramIndex++, itemsBag);
            if (itemsBox != null) stmt.setString(paramIndex++, itemsBox);
            if (petBody != null) stmt.setString(paramIndex++, petBody);
            
            stmt.setInt(paramIndex, playerId);
            stmt.executeUpdate();
        }
    }
    
    private String getSelectedLocations() {
        List<String> locations = new ArrayList<>();
        if (chkBody.isSelected()) locations.add("Body");
        if (chkBag.isSelected()) locations.add("Bag");
        if (chkBox.isSelected()) locations.add("Box");
        if (chkPetBody.isSelected()) locations.add("Pet Body");
        return String.join(", ", locations);
    }
    
    // ========== INNER CLASSES ==========
    
    private static class RecoveryResult {
        int totalAffected = 0;
        int kickedPlayers = 0;
        int dbUpdated = 0;
    }
    
    private static class PlayerDataForRecovery {
        int id;
        String name;
        String itemsBody;
        String itemsBag;
        String itemsBox;
        String petBody;
    }
    
    private static class PlayerItem {
        int id;
        String name;
        
        PlayerItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name + (id == -1 ? "" : " (ID: " + id + ")");
        }
    }
    
    private static class ItemTemplate {
        int id;
        String name;
        
        ItemTemplate(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        @Override
        public String toString() {
            return id + " - " + name;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ItemRecoveryPanel panel = new ItemRecoveryPanel();
            panel.setVisible(true);
        });
    }
}
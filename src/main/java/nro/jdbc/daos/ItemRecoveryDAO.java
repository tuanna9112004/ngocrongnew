package nro.jdbc.daos;



import nro.jdbc.DBService;
import nro.models.player.Player;
import nro.services.Service;
import nro.server.Client;
import nro.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemRecoveryDAO {
    
    private static final String EMPTY_ITEM = "{\"quantity\":0,\"create_time\":0,\"temp_id\":-1,\"option\":[]}";
    
  
    public static int recoverItemsByTempId(int playerId, int tempId, List<String> locations) {
        if (locations == null || locations.isEmpty()) {
            return 0;
        }
        
        int affectedPlayers = 0;
        String whereClause = playerId == -1 ? "" : " WHERE id = " + playerId;
        
        try (Connection conn = DBService.gI().getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                for (String location : locations) {
                    String selectSql = "SELECT id, name, " + location + " FROM player" + whereClause;
                    
                    try (Statement selectStmt = conn.createStatement();
                         ResultSet rs = selectStmt.executeQuery(selectSql)) {
                        
                        PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE player SET " + location + " = ? WHERE id = ?"
                        );
                        
                        int batchCount = 0;
                        
                        while (rs.next()) {
                            int currentPlayerId = rs.getInt("id");
                            String playerName = rs.getString("name");
                            String itemsJson = rs.getString(location);
                            
                            if (itemsJson != null && !itemsJson.isEmpty()) {
                                String newJson = removeItemByTempId(itemsJson, tempId);
                                
                                if (!newJson.equals(itemsJson)) {
                                    updateStmt.setString(1, newJson);
                                    updateStmt.setInt(2, currentPlayerId);
                                    updateStmt.addBatch();
                                    
                                    batchCount++;
                                    affectedPlayers++;
                                    
                                    // Execute batch every 100 records
                                    if (batchCount % 100 == 0) {
                                        updateStmt.executeBatch();
                                        updateStmt.clearBatch();
                                    }
                                    
                                    // Notify player if online
                                    notifyAndRefreshPlayer(currentPlayerId, playerName);
                                    
                                    Log.log("Thu hồi vật phẩm " + tempId + " từ " + playerName + 
                                           " tại " + location);
                                }
                            }
                        }
                        
                        // Execute remaining batch
                        if (batchCount % 100 != 0) {
                            updateStmt.executeBatch();
                        }
                        
                        updateStmt.close();
                    }
                }
                
                conn.commit();
                Log.success("Thu hồi thành công vật phẩm " + tempId + 
                           " từ " + affectedPlayers + " người chơi");
                
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
            
        } catch (Exception e) {
            Log.error(ItemRecoveryDAO.class, e);
        }
        
        return affectedPlayers;
    }
    
    /**
     * Xóa item khỏi JSON array theo temp_id
     */
    private static String removeItemByTempId(String itemsJson, int tempId) {
        try {
            JSONArray items = new JSONArray(itemsJson);
            JSONArray newItems = new JSONArray();
            boolean modified = false;
            
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                int currentTempId = item.optInt("temp_id", -1);
                
                if (currentTempId == tempId) {
                    // Replace with empty item
                    newItems.put(new JSONObject(EMPTY_ITEM));
                    modified = true;
                } else {
                    // Keep original item
                    newItems.put(item);
                }
            }
            
            return modified ? newItems.toString() : itemsJson;
            
        } catch (Exception e) {
            // Fallback to manual parsing if JSON fails
            return removeItemByTempIdManual(itemsJson, tempId);
        }
    }
    
    /**
     * Fallback method - xóa item bằng string manipulation
     */
    private static String removeItemByTempIdManual(String itemsJson, int tempId) {
        try {
            itemsJson = itemsJson.trim();
            if (!itemsJson.startsWith("[")) {
                return itemsJson;
            }
            
            StringBuilder result = new StringBuilder("[");
            int depth = 0;
            int itemStart = -1;
            boolean modified = false;
            
            for (int i = 1; i < itemsJson.length(); i++) {
                char c = itemsJson.charAt(i);
                
                if (c == '{') {
                    if (depth == 0) {
                        itemStart = i;
                    }
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0 && itemStart != -1) {
                        String item = itemsJson.substring(itemStart, i + 1);
                        
                        // Check if this item has the temp_id
                        if (item.contains("\"temp_id\":" + tempId) || 
                            item.contains("\"temp_id\": " + tempId)) {
                            // Replace with empty item
                            if (result.length() > 1) result.append(",");
                            result.append(EMPTY_ITEM);
                            modified = true;
                        } else {
                            // Keep original item
                            if (result.length() > 1) result.append(",");
                            result.append(item);
                        }
                        itemStart = -1;
                    }
                }
            }
            
            result.append("]");
            return modified ? result.toString() : itemsJson;
            
        } catch (Exception e) {
            Log.error(ItemRecoveryDAO.class, e);
            return itemsJson;
        }
    }
    
    /**
     * Notify và refresh player nếu đang online
     */
    private static void notifyAndRefreshPlayer(int playerId, String playerName) {
        try {
            Player player = Client.gI().getPlayer(playerId);
            if (player != null && player.getSession() != null) {
                // Send notification
                Service.getInstance().sendThongBao(player, 
                    "Admin đã thu hồi vật phẩm của bạn!");
                
//                // Refresh player items
//                try {
//                    player.requestItemInfo((byte) 3); // Refresh inventory
//                } catch (Exception e) {
//                    // Nếu không có method này, reload player
//                    player.reloadPlayer();
//                }
//                
                Log.log("Đã thông báo cho " + playerName + " về việc thu hồi vật phẩm");
            }
        } catch (Exception e) {
            // Player not online or error, ignore
        }
    }
    
    /**
     * Lấy danh sách player có vật phẩm cụ thể
     */
    public static List<PlayerItemInfo> getPlayersWithItem(int tempId, List<String> locations) {
        List<PlayerItemInfo> result = new ArrayList<>();
        
        if (locations == null || locations.isEmpty()) {
            return result;
        }
        
        try (Connection conn = DBService.gI().getConnection()) {
            for (String location : locations) {
                String sql = "SELECT id, name, " + location + " FROM player";
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        int playerId = rs.getInt("id");
                        String playerName = rs.getString("name");
                        String itemsJson = rs.getString(location);
                        
                        if (itemsJson != null && itemsJson.contains("\"temp_id\":" + tempId)) {
                            int count = countItemInJson(itemsJson, tempId);
                            if (count > 0) {
                                result.add(new PlayerItemInfo(playerId, playerName, location, count));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.error(ItemRecoveryDAO.class, e);
        }
        
        return result;
    }
    
    /**
     * Đếm số lượng item trong JSON
     */
    private static int countItemInJson(String itemsJson, int tempId) {
        int count = 0;
        try {
            JSONArray items = new JSONArray(itemsJson);
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                if (item.optInt("temp_id", -1) == tempId) {
                    count++;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return count;
    }
    
    /**
     * Inner class chứa thông tin player có item
     */
    public static class PlayerItemInfo {
        public int playerId;
        public String playerName;
        public String location;
        public int count;
        
        public PlayerItemInfo(int playerId, String playerName, String location, int count) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.location = location;
            this.count = count;
        }
        
        @Override
        public String toString() {
            return playerName + " (" + location + ": " + count + ")";
        }
    }
}
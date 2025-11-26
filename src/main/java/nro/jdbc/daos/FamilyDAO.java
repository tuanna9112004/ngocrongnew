package nro.jdbc.daos;

import nro.jdbc.DBService;
import nro.models.player.Family;
import nro.models.player.Player;
import nro.utils.Log;
import java.sql.*;

/**
 * DAO quản lý bảng player_family
 */
public class FamilyDAO {
    
    /** Load thông tin Family từ DB cho player */
    public static void load(Player p) {
        if (p == null) return;
        try (Connection con = DBService.gI().getConnectionForGame();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT spouse_id, married_at, child_level, child_exp, status, custody, " +
                             "is_pregnant, pregnancy_start " +
                             "FROM player_family WHERE player_id = ?")) {
            ps.setLong(1, p.id);
            //System.out.println("→ Loading family for player: " + p.name + " (ID:" + p.id + ")");
            try (ResultSet rs = ps.executeQuery()) {
                Family fam = new Family();
                if (rs.next()) {
                    fam.spouseId = rs.getLong("spouse_id");
                    if (rs.wasNull()) fam.spouseId = 0;
                    fam.marriedAt = rs.getLong("married_at");
                    fam.childLevel = rs.getByte("child_level");
                    fam.childExp = rs.getInt("child_exp");
                    fam.status = rs.getByte("status");
                    fam.custody = rs.getByte("custody");
                    fam.isPregnant = rs.getBoolean("is_pregnant");
                    fam.pregnancyStart = rs.getLong("pregnancy_start");
                   // System.out.println("✓ LOADED from DB: spouse=" + fam.spouseId + 
                                  //   " status=" + fam.status + " childLv=" + fam.childLevel);
                } else {
                    // Khởi tạo giá trị mặc định
                   // System.out.println("⚠ NO DATA in DB for player " + p.name + " - Creating default");
                    fam.spouseId = 0;
                    fam.marriedAt = 0;
                    fam.childLevel = 0;
                    fam.childExp = 0;
                    fam.status = Family.STATUS_SINGLE;
                    fam.custody = 0;
                    fam.isPregnant = false;
                    fam.pregnancyStart = 0;
                }
                p.family = fam;
            }
        } catch (Exception e) {
            Log.error(FamilyDAO.class, e, "Load family error for player " + (p != null ? p.name : "null"));
        }
    }
    
    /** 
     * Lưu Family vào DB - Dùng UPSERT để đảm bảo luôn lưu được 
     * QUAN TRỌNG: Đây là FIX chính cho vấn đề không lưu được dữ liệu
     */
    public static void save(Player p) {
        if (p == null || p.family == null) return;
        
        try (Connection con = DBService.gI().getConnectionForSaveData();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO player_family " +
                     "(player_id, spouse_id, married_at, child_level, child_exp, status, custody, is_pregnant, pregnancy_start) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "spouse_id=VALUES(spouse_id), married_at=VALUES(married_at), " +
                     "child_level=VALUES(child_level), child_exp=VALUES(child_exp), " +
                     "status=VALUES(status), custody=VALUES(custody), " +
                     "is_pregnant=VALUES(is_pregnant), pregnancy_start=VALUES(pregnancy_start)")) {
            
            ps.setLong(1, p.id);
            
            if (p.family.spouseId <= 0) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, p.family.spouseId);
            }
            
            ps.setLong(3, p.family.marriedAt);
            ps.setByte(4, p.family.childLevel);
            ps.setInt(5, p.family.childExp);
            ps.setByte(6, p.family.status);
            ps.setByte(7, p.family.custody);
            ps.setBoolean(8, p.family.isPregnant);
            ps.setLong(9, p.family.pregnancyStart);
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                //System.out.println("✓ Saved family for " + p.name + " (ID:" + p.id + 
                        // ") spouse:" + p.family.spouseId + " status:" + p.family.status);
            } else {
                Log.error(FamilyDAO.class, new Exception("No rows affected"), 
                         "Save failed for " + p.name);
            }
            
        } catch (Exception e) {
            Log.error(FamilyDAO.class, e, "Save family error for player " + p.name);
        }
    }
    
    /**
     * Lưu CẢ 2 PLAYER trong 1 transaction - Đảm bảo tính nhất quán
     * Dùng cho kết hôn, ly hôn, cập nhật con
     */
    public static boolean saveBoth(Player p1, Player p2) {
        if (p1 == null || p2 == null || p1.family == null || p2.family == null) {
            Log.error(FamilyDAO.class, new Exception("Null player or family"), 
                     "saveBoth failed");
            return false;
        }
        
        Connection con = null;
        try {
            con = DBService.gI().getConnectionForSaveData();
            con.setAutoCommit(false); // Bắt đầu transaction
            
            // Lưu player 1
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO player_family " +
                    "(player_id, spouse_id, married_at, child_level, child_exp, status, custody, is_pregnant, pregnancy_start) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "spouse_id=VALUES(spouse_id), married_at=VALUES(married_at), " +
                    "child_level=VALUES(child_level), child_exp=VALUES(child_exp), " +
                    "status=VALUES(status), custody=VALUES(custody), " +
                    "is_pregnant=VALUES(is_pregnant), pregnancy_start=VALUES(pregnancy_start)")) {
                
                ps.setLong(1, p1.id);
                ps.setLong(2, p1.family.spouseId <= 0 ? 0 : p1.family.spouseId);
                ps.setLong(3, p1.family.marriedAt);
                ps.setByte(4, p1.family.childLevel);
                ps.setInt(5, p1.family.childExp);
                ps.setByte(6, p1.family.status);
                ps.setByte(7, p1.family.custody);
                ps.setBoolean(8, p1.family.isPregnant);
                ps.setLong(9, p1.family.pregnancyStart);
                ps.executeUpdate();
            }
            
            // Lưu player 2
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO player_family " +
                    "(player_id, spouse_id, married_at, child_level, child_exp, status, custody, is_pregnant, pregnancy_start) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "spouse_id=VALUES(spouse_id), married_at=VALUES(married_at), " +
                    "child_level=VALUES(child_level), child_exp=VALUES(child_exp), " +
                    "status=VALUES(status), custody=VALUES(custody), " +
                    "is_pregnant=VALUES(is_pregnant), pregnancy_start=VALUES(pregnancy_start)")) {
                
                ps.setLong(1, p2.id);
                ps.setLong(2, p2.family.spouseId <= 0 ? 0 : p2.family.spouseId);
                ps.setLong(3, p2.family.marriedAt);
                ps.setByte(4, p2.family.childLevel);
                ps.setInt(5, p2.family.childExp);
                ps.setByte(6, p2.family.status);
                ps.setByte(7, p2.family.custody);
                ps.setBoolean(8, p2.family.isPregnant);
                ps.setLong(9, p2.family.pregnancyStart);
                ps.executeUpdate();
            }
            
            con.commit(); // Commit transaction
            
           // System.out.println("✓ Saved BOTH players: " + p1.name + " (ID:" + p1.id + 
                  //  ") <-> " + p2.name + " (ID:" + p2.id + ")");
            return true;
            
        } catch (Exception e) {
            Log.error(FamilyDAO.class, e, "saveBoth failed: " + p1.name + " <-> " + p2.name);
            if (con != null) {
                try {
                    con.rollback();
                    //System.out.println("Transaction rolled back");
                } catch (SQLException ex) {
                    Log.error(FamilyDAO.class, ex, "Rollback failed");
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    Log.error(FamilyDAO.class, e, "Close connection failed");
                }
            }
        }
    }
    
    /**
     * Load spouse từ DB (không cần online)
     * Hữu ích khi người kia offline
     */
    public static long getSpouseId(long playerId) {
        try (Connection con = DBService.gI().getConnectionForGame();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT spouse_id FROM player_family WHERE player_id = ?")) {
            ps.setLong(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long spouseId = rs.getLong("spouse_id");
                    return rs.wasNull() ? 0 : spouseId;
                }
            }
        } catch (Exception e) {
            Log.error(FamilyDAO.class, e, "Get spouse ID error for player_id=" + playerId);
        }
        return 0;
    }
    
    /**
     * Kiểm tra 2 player có đang kết hôn với nhau không
     */
    public static boolean isMarried(long playerId1, long playerId2) {
        try (Connection con = DBService.gI().getConnectionForGame();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) as cnt FROM player_family " +
                     "WHERE (player_id=? AND spouse_id=? AND status=?) " +
                     "OR (player_id=? AND spouse_id=? AND status=?)")) {
            ps.setLong(1, playerId1);
            ps.setLong(2, playerId2);
            ps.setByte(3, Family.STATUS_MARRIED);
            ps.setLong(4, playerId2);
            ps.setLong(5, playerId1);
            ps.setByte(6, Family.STATUS_MARRIED);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") == 2; // Phải có cả 2 bản ghi
                }
            }
        } catch (Exception e) {
            Log.error(FamilyDAO.class, e, "Check marriage error");
        }
        return false;
    }
}
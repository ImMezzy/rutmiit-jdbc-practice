package com.gamerentals.dao;
import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.Box;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoxDao {

    public List<Box> findAll() throws SQLException {
        String sql = "SELECT id, game_id, status FROM boxes ORDER BY status DESC";
        List<Box> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public void insert(Box box) throws SQLException {
        String sql = "INSERT INTO boxes (id, game_id, status) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, box.getId());
            ps.setInt(2, box.getGameId());
            ps.setFloat(3, box.getStatus());
            ps.executeUpdate();
        }
    }

    /** Batch insert — массовая вставка коробок. */
    public int batchInsert(List<Box> boxes) throws SQLException {
        String sql = "INSERT INTO boxes (game_id, status) VALUES (?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Box b : boxes) {
                ps.setInt(1, b.getGameId());
                ps.setFloat(2, b.getStatus());
                ps.addBatch();
            }
            int[] counts = ps.executeBatch();
            conn.commit();
            return counts.length;
        }
    }

    public boolean update(Box box) throws SQLException {
        // game_id — часть PK, его не меняем. Обновляем только status
        String sql = "UPDATE boxes SET status = ? WHERE id = ? AND game_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, box.getStatus());
            ps.setInt(2, box.getId());
            ps.setInt(3, box.getGameId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int gameId) throws SQLException {
        String sql = "DELETE FROM boxes WHERE id = ? AND game_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, gameId);
            return ps.executeUpdate() > 0;
        }
    }

    private Box mapRow(ResultSet rs) throws SQLException {
        return new Box(
                rs.getInt("id"),
                rs.getInt("game_id"),
                rs.getFloat("status")
        );
    }

}

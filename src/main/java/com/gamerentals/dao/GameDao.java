package com.gamerentals.dao;
import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.Game;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameDao {

    public List<Game> findAll() throws SQLException {
        String sql = "SELECT * FROM games ORDER BY id";
        List<Game> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Game> findById(int id) throws SQLException {
        String sql = "SELECT id, name, authors, difficulty, avg_game_time FROM games WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Game game) throws SQLException {
        String sql = "INSERT INTO games (name, authors, difficulty, avg_game_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, game.getName());
            ps.setString(2, game.getAuthors());
            ps.setString(3, game.getDifficulty());
            ps.setTime(4, Time.valueOf(game.getAvgGameTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    game.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ключ");
        }
    }

    public boolean update(Game game) throws SQLException {
        String sql = "UPDATE games SET name = ?, authors = ?, difficulty = ?, avg_game_time = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.getName());
            ps.setString(2, game.getAuthors());
            ps.setString(3, game.getDifficulty());
            ps.setTime(4, Time.valueOf(game.getAvgGameTime()));
            ps.setInt(5, game.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM games WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    //

    private Game mapRow(ResultSet rs) throws SQLException {
        return new Game(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("authors"),
                rs.getString("difficulty"),
                rs.getTime("avg_game_time").toLocalTime()
        );
    }

}

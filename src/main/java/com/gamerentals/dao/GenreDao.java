package com.gamerentals.dao;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы Жанр.
 */
public class GenreDao {

    public List<Genre> findAll() throws SQLException {
        String sql = "SELECT id_жанра, название FROM Жанр ORDER BY id_жанра";
        List<Genre> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Genre> findById(int id) throws SQLException {
        String sql = "SELECT id_жанра, название FROM Жанр WHERE id_жанра = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Genre genre) throws SQLException {
        String sql = "INSERT INTO Жанр (название) VALUES (?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, genre.getName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    genre.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ключ");
        }
    }

    public boolean update(Genre genre) throws SQLException {
        String sql = "UPDATE Жанр SET название = ? WHERE id_жанра = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, genre.getName());
            ps.setInt(2, genre.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Жанр WHERE id_жанра = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Genre mapRow(ResultSet rs) throws SQLException {
        return new Genre(rs.getInt("id_жанра"), rs.getString("название"));
    }
}

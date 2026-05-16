package com.gamerentals.dao;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.Visitor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы Посетитель.
 * Демонстрирует: PreparedStatement, RETURN_GENERATED_KEYS, CRUD.
 */
public class VisitorDao {

    public List<Visitor> findAll() throws SQLException {
        String sql = "SELECT id_посетителя, имя, номер_телефона, email FROM Посетитель ORDER BY id_посетителя";
        List<Visitor> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public Optional<Visitor> findById(int id) throws SQLException {
        String sql = "SELECT id_посетителя, имя, номер_телефона, email FROM Посетитель WHERE id_посетителя = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Visitor visitor) throws SQLException {
        String sql = "INSERT INTO Посетитель (имя, номер_телефона, email) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, visitor.getName());
            ps.setString(2, visitor.getPhone());
            ps.setString(3, visitor.getEmail());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    visitor.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить сгенерированный ключ");
        }
    }

    public boolean update(Visitor visitor) throws SQLException {
        String sql = "UPDATE Посетитель SET имя = ?, номер_телефона = ?, email = ? WHERE id_посетителя = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, visitor.getName());
            ps.setString(2, visitor.getPhone());
            ps.setString(3, visitor.getEmail());
            ps.setInt(4, visitor.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Посетитель WHERE id_посетителя = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Visitor mapRow(ResultSet rs) throws SQLException {
        return new Visitor(
                rs.getInt("id_посетителя"),
                rs.getString("имя"),
                rs.getString("номер_телефона"),
                rs.getString("email")
        );
    }
}

package com.gamerentals.dao;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.Hall;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы Зал.
 */
public class HallDao {

    public List<Hall> findAll() throws SQLException {
        String sql = "SELECT номер_зала, название, вместимость, тип_зала FROM Зал ORDER BY номер_зала";
        List<Hall> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Hall> findById(short id) throws SQLException {
        String sql = "SELECT номер_зала, название, вместимость, тип_зала FROM Зал WHERE номер_зала = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setShort(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public short insert(Hall hall) throws SQLException {
        String sql = "INSERT INTO Зал (название, вместимость, тип_зала) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, hall.getName());
            ps.setShort(2, hall.getCapacity());
            ps.setShort(3, hall.getType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    short id = keys.getShort(1);
                    hall.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить сгенерированный ключ");
        }
    }

    public boolean update(Hall hall) throws SQLException {
        String sql = "UPDATE Зал SET название = ?, вместимость = ?, тип_зала = ? WHERE номер_зала = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hall.getName());
            ps.setShort(2, hall.getCapacity());
            ps.setShort(3, hall.getType());
            ps.setShort(4, hall.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(short id) throws SQLException {
        String sql = "DELETE FROM Зал WHERE номер_зала = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setShort(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Hall mapRow(ResultSet rs) throws SQLException {
        return new Hall(
                rs.getShort("номер_зала"),
                rs.getString("название"),
                rs.getShort("вместимость"),
                rs.getShort("тип_зала")
        );
    }
}

package com.gamerentals.dao;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы Место.
 * Демонстрирует: составной ключ, batch insert.
 */
public class SeatDao {

    public List<Seat> findAll() throws SQLException {
        String sql = "SELECT номер_места, номер_зала, ряд, тип_места, коэффициент_цены FROM Место ORDER BY номер_зала, номер_места";
        List<Seat> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public List<Seat> findByHall(short hallNumber) throws SQLException {
        String sql = "SELECT номер_места, номер_зала, ряд, тип_места, коэффициент_цены FROM Место WHERE номер_зала = ? ORDER BY номер_места";
        List<Seat> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setShort(1, hallNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public Optional<Seat> findByKey(short seatNumber, short hallNumber) throws SQLException {
        String sql = "SELECT номер_места, номер_зала, ряд, тип_места, коэффициент_цены FROM Место WHERE номер_места = ? AND номер_зала = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setShort(1, seatNumber);
            ps.setShort(2, hallNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public void insert(Seat seat) throws SQLException {
        String sql = "INSERT INTO Место (номер_места, номер_зала, ряд, тип_места, коэффициент_цены) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setShort(1, seat.getSeatNumber());
            ps.setShort(2, seat.getHallNumber());
            ps.setShort(3, seat.getRow());
            ps.setShort(4, seat.getType());
            ps.setBigDecimal(5, seat.getPriceCoefficient());
            ps.executeUpdate();
        }
    }

    /** Batch insert — массовая вставка мест. */
    public int batchInsert(List<Seat> seats) throws SQLException {
        String sql = "INSERT INTO Место (номер_места, номер_зала, ряд, тип_места, коэффициент_цены) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Seat s : seats) {
                ps.setShort(1, s.getSeatNumber());
                ps.setShort(2, s.getHallNumber());
                ps.setShort(3, s.getRow());
                ps.setShort(4, s.getType());
                ps.setBigDecimal(5, s.getPriceCoefficient());
                ps.addBatch();
            }
            int[] counts = ps.executeBatch();
            conn.commit();
            return counts.length;
        }
    }

    public boolean update(Seat seat) throws SQLException {
        String sql = "UPDATE Место SET ряд = ?, тип_места = ?, коэффициент_цены = ? WHERE номер_места = ? AND номер_зала = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setShort(1, seat.getRow());
            ps.setShort(2, seat.getType());
            ps.setBigDecimal(3, seat.getPriceCoefficient());
            ps.setShort(4, seat.getSeatNumber());
            ps.setShort(5, seat.getHallNumber());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(short seatNumber, short hallNumber) throws SQLException {
        String sql = "DELETE FROM Место WHERE номер_места = ? AND номер_зала = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setShort(1, seatNumber);
            ps.setShort(2, hallNumber);
            return ps.executeUpdate() > 0;
        }
    }

    private Seat mapRow(ResultSet rs) throws SQLException {
        return new Seat(
                rs.getShort("номер_места"),
                rs.getShort("номер_зала"),
                rs.getShort("ряд"),
                rs.getShort("тип_места"),
                rs.getBigDecimal("коэффициент_цены")
        );
    }
}

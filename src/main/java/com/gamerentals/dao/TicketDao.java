package com.gamerentals.dao;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.Ticket;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы Билет.
 * Демонстрирует: FK-связи, транзакционная покупка.
 */
public class TicketDao {

    private static final String BASE_SELECT =
            "SELECT id_билета, id_посетителя, id_сеанса, номер_места, номер_зала, дата_и_время FROM Билет";

    public List<Ticket> findAll() throws SQLException {
        List<Ticket> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY id_билета")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Ticket> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE id_билета = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Ticket> findByVisitor(int visitorId) throws SQLException {
        String sql = BASE_SELECT + " WHERE id_посетителя = ? ORDER BY дата_и_время DESC";
        List<Ticket> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, visitorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<Ticket> findByScreening(int screeningId) throws SQLException {
        String sql = BASE_SELECT + " WHERE id_сеанса = ? ORDER BY номер_места";
        List<Ticket> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, screeningId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public int insert(Ticket ticket) throws SQLException {
        String sql = "INSERT INTO Билет (id_посетителя, id_сеанса, номер_места, номер_зала, дата_и_время) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ticket.getVisitorId());
            ps.setInt(2, ticket.getScreeningId());
            ps.setShort(3, ticket.getSeatNumber());
            ps.setShort(4, ticket.getHallNumber());
            ps.setObject(5, ticket.getPurchaseDateTime() != null
                    ? ticket.getPurchaseDateTime() : OffsetDateTime.now());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ticket.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ключ");
        }
    }

    /**
     * Транзакционная покупка билета:
     * 1. Проверяем что место свободно на данный сеанс
     * 2. Создаём билет
     */
    public int purchaseTicket(int visitorId, int screeningId, short seatNumber, short hallNumber) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM Билет WHERE id_сеанса = ? AND номер_места = ? AND номер_зала = ?";
        String insertSql = "INSERT INTO Билет (id_посетителя, id_сеанса, номер_места, номер_зала) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Проверяем свободность места
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, screeningId);
                    ps.setShort(2, seatNumber);
                    ps.setShort(3, hallNumber);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            conn.rollback();
                            throw new SQLException("Место " + seatNumber + " в зале " + hallNumber
                                    + " уже занято на сеанс " + screeningId);
                        }
                    }
                }
                // Создаём билет
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, visitorId);
                    ps.setInt(2, screeningId);
                    ps.setShort(3, seatNumber);
                    ps.setShort(4, hallNumber);
                    ps.executeUpdate();
                    conn.commit();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) return keys.getInt(1);
                    }
                    throw new SQLException("Не удалось получить ключ билета");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Билет WHERE id_билета = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getInt("id_билета"),
                rs.getInt("id_посетителя"),
                rs.getInt("id_сеанса"),
                rs.getShort("номер_места"),
                rs.getShort("номер_зала"),
                rs.getObject("дата_и_время", OffsetDateTime.class)
        );
    }
}

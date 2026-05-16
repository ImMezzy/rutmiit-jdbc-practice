package com.gamerentals.dao;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.Screening;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы Сеанс.
 * Демонстрирует: работа с MONEY типом, TIMESTAMPTZ, фильтрация.
 */
public class ScreeningDao {

    private static final String BASE_SELECT =
            "SELECT id_сеанса, номер_зала, id_фильма, дата_и_время, базовая_стоимость::NUMERIC AS базовая_стоимость FROM Сеанс";

    public List<Screening> findAll() throws SQLException {
        List<Screening> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY дата_и_время")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Screening> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE id_сеанса = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Screening> findByDate(String date) throws SQLException {
        String sql = BASE_SELECT + " WHERE дата_и_время::DATE = ?::DATE ORDER BY дата_и_время";
        List<Screening> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<Screening> findByFilm(int filmId) throws SQLException {
        String sql = BASE_SELECT + " WHERE id_фильма = ? ORDER BY дата_и_время";
        List<Screening> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, filmId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<Screening> findByHall(short hallNumber) throws SQLException {
        String sql = BASE_SELECT + " WHERE номер_зала = ? ORDER BY дата_и_время";
        List<Screening> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setShort(1, hallNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public int insert(Screening screening) throws SQLException {
        String sql = "INSERT INTO Сеанс (номер_зала, id_фильма, дата_и_время, базовая_стоимость) VALUES (?, ?, ?, ?::MONEY)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setShort(1, screening.getHallNumber());
            ps.setInt(2, screening.getFilmId());
            ps.setObject(3, screening.getDateTime());
            ps.setBigDecimal(4, screening.getBasePrice());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    screening.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ключ");
        }
    }

    public boolean update(Screening screening) throws SQLException {
        String sql = "UPDATE Сеанс SET номер_зала = ?, id_фильма = ?, дата_и_время = ?, базовая_стоимость = ?::MONEY WHERE id_сеанса = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setShort(1, screening.getHallNumber());
            ps.setInt(2, screening.getFilmId());
            ps.setObject(3, screening.getDateTime());
            ps.setBigDecimal(4, screening.getBasePrice());
            ps.setInt(5, screening.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Сеанс WHERE id_сеанса = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Screening mapRow(ResultSet rs) throws SQLException {
        return new Screening(
                rs.getInt("id_сеанса"),
                rs.getShort("номер_зала"),
                rs.getInt("id_фильма"),
                rs.getObject("дата_и_время", OffsetDateTime.class),
                rs.getBigDecimal("базовая_стоимость")
        );
    }
}

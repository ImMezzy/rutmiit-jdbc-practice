package com.gamerentals.dao;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.BoxRent;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoxRentDao {
    private static final String BASE_SELECT =
            "SELECT id, box_id, game_id, client_pass_number, date_of_rent, date_of_return, fine FROM box_rent";

    public List<BoxRent> findAll() throws SQLException {
        List<BoxRent> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY id")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<BoxRent> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<BoxRent> findByClient(String clientPassNumber) throws SQLException {
        String sql = BASE_SELECT + " WHERE client_pass_number = ? ORDER BY date_of_rent DESC";
        List<BoxRent> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clientPassNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<BoxRent> findByGame(int gameId) throws SQLException {
        String sql = BASE_SELECT + " WHERE game_id = ? ORDER BY date_of_rent";
        List<BoxRent> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public int insert(BoxRent boxRent) throws SQLException {
        String sql = "INSERT INTO box_rent (id, box_id, game_id, client_pass_number, date_of_rent, date_of_return, fine) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, boxRent.getId());
            ps.setInt(2, boxRent.getBoxId());
            ps.setInt(3, boxRent.getGameId());
            ps.setString(4, boxRent.getClientPassNumber());
            ps.setObject(5, boxRent.getDateOfRent() != null
                    ? boxRent.getDateOfRent() : OffsetDateTime.now());
            ps.setObject(6, boxRent.getDateOfReturn() != null
                    ? boxRent.getDateOfReturn() : OffsetDateTime.now());
            ps.setInt(7, boxRent.getFine());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    boxRent.setId(id);
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
    public int rentBox(String clientPassNumber, int boxId, int gameId,
                       OffsetDateTime dateOfRent, OffsetDateTime dateOfReturn)
            throws SQLException {

        String checkBoxSql = "SELECT status FROM boxes WHERE id = ? AND game_id = ?";
        String checkActiveRentSql = "SELECT COUNT(*) FROM box_rent WHERE box_id = ? AND date_of_return IS NULL";
        String insertRentSql = "INSERT INTO box_rent (box_id, game_id, client_pass_number, date_of_rent, date_of_return, fine) " +
                "VALUES (?, ?, ?, ?, ?, 0)";
        String updateBoxStatusSql = "UPDATE boxes SET status = ? WHERE id = ?";

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Проверяем существование коробки и её статус
                try (PreparedStatement ps = conn.prepareStatement(checkBoxSql)) {
                    ps.setInt(1, boxId);
                    ps.setInt(2, gameId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Коробка с id=" + boxId + " для игры " + gameId + " не найдена");
                        }
                        float status = rs.getFloat("status");
                        if (status <= 0) {
                            throw new SQLException("Коробка " + boxId + " недоступна для аренды (статус: " + status + ")");
                        }
                    }
                }

                // 2. Проверяем, нет ли активной аренды этой коробки
                try (PreparedStatement ps = conn.prepareStatement(checkActiveRentSql)) {
                    ps.setInt(1, boxId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            throw new SQLException("Коробка " + boxId + " уже арендована и не возвращена");
                        }
                    }
                }

                // 3. Создаём запись аренды
                try (PreparedStatement ps = conn.prepareStatement(insertRentSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, boxId);
                    ps.setInt(2, gameId);
                    ps.setString(3, clientPassNumber);
                    ps.setObject(4, dateOfRent);
                    ps.setObject(5, dateOfReturn);
                    ps.executeUpdate();

                    // 4. Обновляем статус коробки (например, уменьшаем на 1.0 или ставим 0 = занята)
                    try (PreparedStatement updatePs = conn.prepareStatement(updateBoxStatusSql)) {
                        updatePs.setFloat(1, 0f); // или status - 1.0, в зависимости от логики
                        updatePs.setInt(2, boxId);
                        updatePs.executeUpdate();
                    }

                    conn.commit();

                    // Возвращаем ID созданной аренды
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            return keys.getInt(1);
                        }
                    }
                    throw new SQLException("Не удалось получить ключ аренды");
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM box_rent WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private BoxRent mapRow(ResultSet rs) throws SQLException {
        return new BoxRent(
                rs.getInt("id"),
                rs.getInt("box_id"),
                rs.getInt("game_id"),
                rs.getString("client_pass_number"),
                rs.getObject("date_of_rent", OffsetDateTime.class),
                rs.getObject("date_of_return", OffsetDateTime.class),
                rs.getInt("fine")
        );
    }

}

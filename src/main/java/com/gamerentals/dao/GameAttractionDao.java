package com.gamerentals.dao;
import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.GameAttraction;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameAttractionDao {

    private static final String BASE_SELECT =
            "SELECT id, game_id, client_pass_number, start_time, end_time FROM game_attraction";

    public List<GameAttraction> findAll() throws SQLException {
        List<GameAttraction> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY id")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<GameAttraction> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<GameAttraction> findByClient(String clientPassNumber) throws SQLException {
        String sql = BASE_SELECT + " WHERE client_pass_number = ? ORDER BY start_time DESC";
        List<GameAttraction> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clientPassNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<GameAttraction> findByGame(int gameId) throws SQLException {
        String sql = BASE_SELECT + " WHERE game_id = ? ORDER BY start_time";
        List<GameAttraction> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    /**
     * Вставляет запись, если id = 0 — поле игнорируется (auto-generated)
     */
    public int insert(GameAttraction gameAttraction) throws SQLException {
        String sql = "INSERT INTO game_attraction (game_id, client_pass_number, start_time, end_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, gameAttraction.getGameId());
            ps.setString(2, gameAttraction.getClientPassNumber());
            ps.setObject(3, gameAttraction.getStartTime() != null
                    ? gameAttraction.getStartTime() : OffsetDateTime.now());
            ps.setObject(4, gameAttraction.getEndTime() != null
                    ? gameAttraction.getEndTime() : OffsetDateTime.now());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    gameAttraction.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить сгенерированный ключ");
        }
    }

    /**
     * Транзакционная запись клиента на игротеку:
     * 1. Проверяем, что времена корректны (end > start)
     * 2. Проверяем отсутствие пересекающихся сессий у клиента для этой игры
     * 3. Создаём запись
     */
    public int bookGameAttraction(String clientPassNumber, int gameId,
                                  OffsetDateTime startTime, OffsetDateTime endTime)
            throws SQLException {

        // Валидация входных данных
        if (startTime == null || endTime == null) {
            throw new SQLException("Время начала и окончания не могут быть null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new SQLException("Время окончания должно быть позже времени начала");
        }

        String checkOverlapSql = """
                SELECT COUNT(*) FROM game_attraction 
                WHERE client_pass_number = ? 
                  AND game_id = ? 
                  AND start_time < ? 
                  AND end_time > ?
                """;
        String insertSql = "INSERT INTO game_attraction (game_id, client_pass_number, start_time, end_time) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Проверяем пересечение с существующими сессиями клиента
                try (PreparedStatement ps = conn.prepareStatement(checkOverlapSql)) {
                    ps.setString(1, clientPassNumber);
                    ps.setInt(2, gameId);
                    ps.setObject(3, endTime);   // новая сессия начинается до конца старой
                    ps.setObject(4, startTime); // новая сессия заканчивается после начала старой
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            throw new SQLException(
                                    String.format("У клиента %s уже есть сессия для игры %d, пересекающаяся с [%s; %s]",
                                            clientPassNumber, gameId, startTime, endTime));
                        }
                    }
                }

                // 2. Создаём запись об игротке
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, gameId);
                    ps.setString(2, clientPassNumber);
                    ps.setObject(3, startTime);
                    ps.setObject(4, endTime);
                    ps.executeUpdate();

                    conn.commit();

                    // Возвращаем ID созданной записи
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            return keys.getInt(1);
                        }
                    }
                    throw new SQLException("Не удалось получить ключ записи");
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM game_attraction WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(GameAttraction gameAttraction) throws SQLException {
        String sql = "UPDATE game_attraction SET game_id = ?, client_pass_number = ?, start_time = ?, end_time = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameAttraction.getGameId());
            ps.setString(2, gameAttraction.getClientPassNumber());
            ps.setObject(3, gameAttraction.getStartTime());
            ps.setObject(4, gameAttraction.getEndTime());
            ps.setInt(5, gameAttraction.getId());
            return ps.executeUpdate() > 0;
        }
    }

    private GameAttraction mapRow(ResultSet rs) throws SQLException {
        return new GameAttraction(
                rs.getInt("id"),
                rs.getInt("game_id"),
                rs.getString("client_pass_number"),
                rs.getObject("start_time", OffsetDateTime.class),
                rs.getObject("end_time", OffsetDateTime.class)
        );
    }
}
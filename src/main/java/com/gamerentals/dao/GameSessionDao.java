package com.gamerentals.dao;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.GameSession;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameSessionDao {
    private static final String BASE_SELECT =
            "SELECT id, game_id, client_pass_number, start_time, end_time, game_result FROM game_session";

    public List<GameSession> findAll() throws SQLException {
        List<GameSession> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY id")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<GameSession> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<GameSession> findByClient(String clientPassNumber) throws SQLException {
        String sql = BASE_SELECT + " WHERE client_pass_number = ? ORDER BY start_time DESC";
        List<GameSession> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clientPassNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<GameSession> findByGame(int gameId) throws SQLException {
        String sql = BASE_SELECT + " WHERE game_id = ? ORDER BY start_time";
        List<GameSession> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public int insert(GameSession session) throws SQLException {
        String sql = "INSERT INTO game_session (game_id, client_pass_number, start_time, end_time, game_result) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, session.getGameId());
            ps.setString(2, session.getClientPassNumber());
            ps.setObject(3, session.getStartTime());
            ps.setObject(4, session.getEndTime());
            ps.setBoolean(5, session.isGameResult());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    session.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить сгенерированный ключ");
        }
    }

    public int recordGameSession(String clientPassNumber, int gameId,
                                 OffsetDateTime startTime, OffsetDateTime endTime, boolean result)
            throws SQLException {

        if (startTime == null || endTime == null) {
            throw new SQLException("Время начала и окончания не могут быть null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new SQLException("Время окончания должно быть позже времени начала");
        }

        String checkOverlapSql = """
                SELECT COUNT(*) FROM game_session 
                WHERE client_pass_number = ? 
                  AND game_id = ? 
                  AND start_time < ? 
                  AND end_time > ?
                """;
        String insertSql = "INSERT INTO game_session (game_id, client_pass_number, start_time, end_time, game_result) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(checkOverlapSql)) {
                    ps.setString(1, clientPassNumber);
                    ps.setInt(2, gameId);
                    ps.setObject(3, endTime);
                    ps.setObject(4, startTime);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            throw new SQLException(
                                    String.format("У клиента %s уже есть сессия для игры %d, пересекающаяся с [%s; %s]",
                                            clientPassNumber, gameId, startTime, endTime));
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, gameId);
                    ps.setString(2, clientPassNumber);
                    ps.setObject(3, startTime);
                    ps.setObject(4, endTime);
                    ps.setBoolean(5, result);
                    ps.executeUpdate();

                    conn.commit();

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

    public boolean completeSession(int sessionId, OffsetDateTime endTime, boolean result) throws SQLException {
        String sql = "UPDATE game_session SET end_time = ?, game_result = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, endTime);
            ps.setBoolean(2, result);
            ps.setInt(3, sessionId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM game_session WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(GameSession session) throws SQLException {
        String sql = "UPDATE game_session SET game_id = ?, client_pass_number = ?, start_time = ?, end_time = ?, game_result = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, session.getGameId());
            ps.setString(2, session.getClientPassNumber());
            ps.setObject(3, session.getStartTime());
            ps.setObject(4, session.getEndTime());
            ps.setBoolean(5, session.isGameResult());
            ps.setInt(6, session.getId());
            return ps.executeUpdate() > 0;
        }
    }

    private GameSession mapRow(ResultSet rs) throws SQLException {
        return new GameSession(
                rs.getInt("id"),
                rs.getInt("game_id"),
                rs.getString("client_pass_number"),
                rs.getObject("start_time", OffsetDateTime.class),
                rs.getObject("end_time", OffsetDateTime.class),
                rs.getBoolean("game_result")
        );
    }
}
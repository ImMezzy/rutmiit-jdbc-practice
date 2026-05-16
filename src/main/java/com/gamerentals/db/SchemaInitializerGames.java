package com.gamerentals.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

/**
 * Инициализация схемы БД и заполнение тестовыми данными.
 */

public class SchemaInitializerGames {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializerGames.class);

    public static void initialize() throws SQLException {
        log.info("Инициализация схемы БД для игр...");
        executeSqlFile("schema_games.sql");
        seedTestData();
        log.info("Схема БД для игр создана и заполнена тестовыми данными");
    }

    private static void executeSqlFile(String fileName) throws SQLException {
        String sql;
        try (InputStream is = SchemaInitializerGames.class.getClassLoader()
                .getResourceAsStream(fileName)) {
            if (is == null) throw new RuntimeException("SQL-файл не найден: " + fileName);
            sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения SQL-файла: " + fileName, e);
        }

        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.info("Выполнен SQL-файл: {}", fileName);
        }
    }

    private static void seedTestData() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                seedGames(conn);
                seedBoxes(conn);
                seedClients(conn);
                seedBoxRents(conn);
                seedGameAttractions(conn);
                seedGameSessions(conn);
                conn.commit();
                log.info("Тестовые данные для игр загружены");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static void seedGames(Connection conn) throws SQLException {
        String sql = "INSERT INTO games (name, authors, difficulty, avg_game_time) VALUES (?, ?, ?, ?::TIME)";
        Object[][] games = {
                {"Монополия", "Charles Darrow", "Легко", "01:30:00"},
                {"Каркассон", "Klaus-Jürgen Wrede", "Нормально", "00:45:00"},
                {"Колонизаторы", "Klaus Teuber", "Нормально", "01:15:00"},
                {"Уно", "Merle Robbins", "Легко", "00:20:00"},
                {"Диксит", "Jean-Louis Roubira", "Легко", "00:30:00"},
                {"Пандемия", "Matt Leacock", "Сложно", "01:00:00"},
                {"7 Чудес", "Antoine Bauza", "Сложно", "00:50:00"},
                {"Тик-так-бумм", "Philippe Keyaerts", "Легко", "00:25:00"},
                {"Экивоки", "Александр Ройтбурд", "Нормально", "01:00:00"},
                {"Имаджинариум", "Владимир Марыгин", "Легко", "00:40:00"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] g : games) {
                ps.setString(1, (String) g[0]);
                ps.setString(2, (String) g[1]);
                ps.setString(3, (String) g[2]);
                ps.setString(4, (String) g[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        log.info("Загружено {} игр", games.length);
    }

    private static void seedBoxes(Connection conn) throws SQLException {
        // Добавляем id в список колонок
        String sql = "INSERT INTO boxes (id, game_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Игра 1 (Монополия): коробки 1, 2, 3
            ps.setInt(1, 1); ps.setInt(2, 1); ps.setFloat(3, 1.0f); ps.addBatch();
            ps.setInt(1, 2); ps.setInt(2, 1); ps.setFloat(3, 0.0f); ps.addBatch();
            ps.setInt(1, 3); ps.setInt(2, 1); ps.setFloat(3, 1.0f); ps.addBatch();

            // Игра 2 (Каркассон): коробки 1, 2
            ps.setInt(1, 1); ps.setInt(2, 2); ps.setFloat(3, 1.0f); ps.addBatch();
            ps.setInt(1, 2); ps.setInt(2, 2); ps.setFloat(3, 0.5f); ps.addBatch();

            // Игра 3 (Колонизаторы): коробки 1, 2
            ps.setInt(1, 1); ps.setInt(2, 3); ps.setFloat(3, 1.0f); ps.addBatch();
            ps.setInt(1, 2); ps.setInt(2, 3); ps.setFloat(3, 1.0f); ps.addBatch();

            // Игра 4 (Уно): коробки 1..4
            for (int i = 1; i <= 4; i++) {
                ps.setInt(1, i); ps.setInt(2, 4); ps.setFloat(3, 1.0f); ps.addBatch();
            }

            // Игра 5 (Диксит): коробки 1, 2
            ps.setInt(1, 1); ps.setInt(2, 5); ps.setFloat(3, 1.0f); ps.addBatch();
            ps.setInt(1, 2); ps.setInt(2, 5); ps.setFloat(3, 0.0f); ps.addBatch();

            // Игра 6 (Пандемия): коробки 1, 2
            ps.setInt(1, 1); ps.setInt(2, 6); ps.setFloat(3, 1.0f); ps.addBatch();
            ps.setInt(1, 2); ps.setInt(2, 6); ps.setFloat(3, 1.0f); ps.addBatch();

            // Игра 7 (7 Чудес): коробка 1
            ps.setInt(1, 1); ps.setInt(2, 7); ps.setFloat(3, 0.0f); ps.addBatch();

            // Игра 8 (Тик-так-бумм): коробки 1..3
            for (int i = 1; i <= 3; i++) {
                ps.setInt(1, i); ps.setInt(2, 8); ps.setFloat(3, 1.0f); ps.addBatch();
            }

            // Игра 9 (Экивоки): коробки 1, 2
            ps.setInt(1, 1); ps.setInt(2, 9); ps.setFloat(3, 1.0f); ps.addBatch();
            ps.setInt(1, 2); ps.setInt(2, 9); ps.setFloat(3, 1.0f); ps.addBatch();

            // Игра 10 (Имаджинариум): коробки 1, 2
            ps.setInt(1, 1); ps.setInt(2, 10); ps.setFloat(3, 1.0f); ps.addBatch();
            ps.setInt(1, 2); ps.setInt(2, 10); ps.setFloat(3, 0.5f); ps.addBatch();

            ps.executeBatch();
        }
        log.info("Загружены коробки для игр");
    }

    private static void seedClients(Connection conn) throws SQLException {
        String sql = "INSERT INTO clients (pass_number, name, last_name, patronymic) VALUES (?, ?, ?, ?)";
        Object[][] clients = {
                {"8120 900311", "Алексей", "Иванов", "Сергеевич"},
                {"8120 900312", "Мария", "Петрова", "Александровна"},
                {"8120 900313", "Дмитрий", "Сидоров", "Павлович"},
                {"8120 900314", "Екатерина", "Козлова", "Дмитриевна"},
                {"8120 900315", "Андрей", "Волков", "Игоревич"},
                {"8120 900316", "Анна", "Новикова", "Андреевна"},
                {"8120 900317", "Михаил", "Морозов", "Владимирович"},
                {"8120 900318", "Ольга", "Соколова", "Николаевна"},
                {"8120 900319", "Павел", "Лебедев", "Алексеевич"},
                {"8120 900320", "Юлия", "Попова", "Евгеньевна"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] c : clients) {
                ps.setString(1, (String) c[0]);
                ps.setString(2, (String) c[1]);
                ps.setString(3, (String) c[2]);
                ps.setString(4, (String) c[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        log.info("Загружено {} клиентов", clients.length);
    }

    private static void seedBoxRents(Connection conn) throws SQLException {
        String sql = "INSERT INTO box_rent (box_id, game_id, client_pass_number, date_of_rent, date_of_return, fine) VALUES (?, ?, ?, ?, ?, ?)";
        OffsetDateTime baseDate = OffsetDateTime.of(2026, 4, 24, 10, 0, 0, 0, ZoneOffset.ofHours(3));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Формат: (box_id, game_id, passport, start, end, fine)
            // Важно: box_id должен существовать в boxes ДЛЯ ЭТОГО game_id

            ps.setInt(1, 2); ps.setInt(2, 1);  ps.setString(3, "8120 900311");
            ps.setObject(4, baseDate); ps.setObject(5, baseDate.plusDays(3)); ps.setInt(6, 0); ps.addBatch();

            ps.setInt(1, 1); ps.setInt(2, 2);  ps.setString(3, "8120 900312");
            ps.setObject(4, baseDate.minusDays(5)); ps.setObject(5, baseDate.minusDays(3)); ps.setInt(6, 0); ps.addBatch();

            ps.setInt(1, 1); ps.setInt(2, 3);  ps.setString(3, "8120 900313");
            ps.setObject(4, baseDate.minusDays(10)); ps.setObject(5, baseDate.minusDays(6)); ps.setInt(6, 150); ps.addBatch();

            ps.setInt(1, 1); ps.setInt(2, 4);  ps.setString(3, "8120 900314");
            ps.setObject(4, baseDate.plusHours(2)); ps.setObject(5, baseDate.plusHours(2).plusDays(2)); ps.setInt(6, 0); ps.addBatch();

            ps.setInt(1, 1); ps.setInt(2, 5);  ps.setString(3, "8120 900315");
            ps.setObject(4, baseDate); ps.setObject(5, baseDate.plusDays(1)); ps.setInt(6, 0); ps.addBatch();

            ps.setInt(1, 1); ps.setInt(2, 6);  ps.setString(3, "8120 900316");
            ps.setObject(4, baseDate.minusDays(14)); ps.setObject(5, baseDate.minusDays(7)); ps.setInt(6, 500); ps.addBatch();

            ps.setInt(1, 1); ps.setInt(2, 7);  ps.setString(3, "8120 900317");
            ps.setObject(4, baseDate.minusDays(1)); ps.setObject(5, baseDate.plusDays(2)); ps.setInt(6, 0); ps.addBatch();

            ps.setInt(1, 1); ps.setInt(2, 8);  ps.setString(3, "8120 900318");
            ps.setObject(4, baseDate.minusDays(2)); ps.setObject(5, baseDate.minusDays(1)); ps.setInt(6, 0); ps.addBatch();

            ps.setInt(1, 1); ps.setInt(2, 9);  ps.setString(3, "8120 900319");
            ps.setObject(4, baseDate.plusHours(5)); ps.setObject(5, baseDate.plusHours(5).plusDays(2)); ps.setInt(6, 0); ps.addBatch();

            ps.setInt(1, 1); ps.setInt(2, 10); ps.setString(3, "8120 900320");
            ps.setObject(4, baseDate.minusDays(7)); ps.setObject(5, baseDate.minusDays(4)); ps.setInt(6, 75); ps.addBatch();

            // Повторные аренды
            ps.setInt(1, 2); ps.setInt(2, 4);  ps.setString(3, "8120 900311");
            ps.setObject(4, baseDate.minusDays(20)); ps.setObject(5, baseDate.minusDays(18)); ps.setInt(6, 0); ps.addBatch();

            ps.setInt(1, 1); ps.setInt(2, 1);  ps.setString(3, "8120 900312");
            ps.setObject(4, baseDate.minusDays(30)); ps.setObject(5, baseDate.minusDays(27)); ps.setInt(6, 100); ps.addBatch();

            ps.executeBatch();
        }
        log.info("Загружены записи об аренде коробок");
    }

    private static void seedGameAttractions(Connection conn) throws SQLException {
        String sql = "INSERT INTO game_attraction (game_id, client_pass_number, start_time, end_time) VALUES (?, ?, ?, ?)";
        OffsetDateTime baseDate = OffsetDateTime.of(2026, 4, 24, 10, 0, 0, 0, ZoneOffset.ofHours(3));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, 1); ps.setString(2, "8120 900311");
            ps.setObject(3, baseDate); ps.setObject(4, baseDate.plusMinutes(90));
            ps.addBatch();

            ps.setInt(1, 4); ps.setString(2, "8120 900312");
            ps.setObject(3, baseDate.plusHours(1)); ps.setObject(4, baseDate.plusHours(1).plusMinutes(30));
            ps.addBatch();

            ps.setInt(1, 2); ps.setString(2, "8120 900313");
            ps.setObject(3, baseDate.plusHours(2)); ps.setObject(4, baseDate.plusHours(2).plusMinutes(50));
            ps.addBatch();

            ps.setInt(1, 5); ps.setString(2, "8120 900314");
            ps.setObject(3, baseDate.plusHours(3)); ps.setObject(4, baseDate.plusHours(3).plusMinutes(40));
            ps.addBatch();

            ps.setInt(1, 6); ps.setString(2, "8120 900315");
            ps.setObject(3, baseDate.plusHours(4)); ps.setObject(4, baseDate.plusHours(4).plusMinutes(75));
            ps.addBatch();

            ps.setInt(1, 8); ps.setString(2, "8120 900316");
            ps.setObject(3, baseDate.plusHours(8)); ps.setObject(4, baseDate.plusHours(8).plusMinutes(30));
            ps.addBatch();

            ps.setInt(1, 9); ps.setString(2, "8120 900317");
            ps.setObject(3, baseDate.plusHours(9)); ps.setObject(4, baseDate.plusHours(9).plusMinutes(35));
            ps.addBatch();

            ps.setInt(1, 3); ps.setString(2, "8120 900318");
            ps.setObject(3, baseDate.plusDays(1)); ps.setObject(4, baseDate.plusDays(1).plusMinutes(80));
            ps.addBatch();

            ps.setInt(1, 10); ps.setString(2, "8120 900319");
            ps.setObject(3, baseDate.plusDays(1).plusHours(2)); ps.setObject(4, baseDate.plusDays(1).plusHours(2).plusMinutes(45));
            ps.addBatch();

            ps.setInt(1, 7); ps.setString(2, "8120 900320");
            ps.setObject(3, baseDate.plusDays(1).plusHours(4)); ps.setObject(4, baseDate.plusDays(1).plusHours(4).plusMinutes(55));
            ps.addBatch();

            ps.executeBatch();
        }
        log.info("Загружены сессии игротки");
    }

    private static void seedGameSessions(Connection conn) throws SQLException {
        String sql = "INSERT INTO game_session (game_id, client_pass_number, start_time, end_time, game_result) VALUES (?, ?, ?, ?, ?)";
        OffsetDateTime baseDate = OffsetDateTime.of(2026, 4, 24, 10, 0, 0, 0, ZoneOffset.ofHours(3));

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Алексей: Монополия
            ps.setInt(1, 1); ps.setString(2, "8120 900311");
            ps.setObject(3, baseDate.minusDays(5)); ps.setObject(4, baseDate.minusDays(5).plusMinutes(120)); ps.setBoolean(5, true);
            ps.addBatch();
            ps.setInt(1, 1); ps.setString(2, "8120 900311");
            ps.setObject(3, baseDate.minusDays(3)); ps.setObject(4, baseDate.minusDays(3).plusMinutes(90)); ps.setBoolean(5, true);
            ps.addBatch();
            ps.setInt(1, 1); ps.setString(2, "8120 900311");
            ps.setObject(3, baseDate.minusDays(1)); ps.setObject(4, baseDate.minusDays(1).plusMinutes(100)); ps.setBoolean(5, false);
            ps.addBatch();
            ps.setInt(1, 1); ps.setString(2, "8120 900311");
            ps.setObject(3, baseDate.plusHours(1)); ps.setObject(4, baseDate.plusHours(1).plusMinutes(110)); ps.setBoolean(5, true);
            ps.addBatch();

            // Мария: Уно (8120 900312)
            ps.setInt(1, 4); ps.setString(2, "8120 900312");
            ps.setObject(3, baseDate.minusDays(4)); ps.setObject(4, baseDate.minusDays(4).plusMinutes(25)); ps.setBoolean(5, true);
            ps.addBatch();
            ps.setInt(1, 4); ps.setString(2, "8120 900312");
            ps.setObject(3, baseDate.minusDays(2)); ps.setObject(4, baseDate.minusDays(2).plusMinutes(20)); ps.setBoolean(5, false);
            ps.addBatch();
            ps.setInt(1, 4); ps.setString(2, "8120 900312");
            ps.setObject(3, baseDate); ps.setObject(4, baseDate.plusMinutes(22)); ps.setBoolean(5, true);
            ps.addBatch();
            ps.setInt(1, 4); ps.setString(2, "8120 900312");
            ps.setObject(3, baseDate.plusHours(3)); ps.setObject(4, baseDate.plusHours(3).plusMinutes(18)); ps.setBoolean(5, false);
            ps.addBatch();

            // Дмитрий: Каркассон (8120 900313)
            ps.setInt(1, 2); ps.setString(2, "8120 900313");
            ps.setObject(3, baseDate.minusDays(6)); ps.setObject(4, baseDate.minusDays(6).plusMinutes(50)); ps.setBoolean(5, false);
            ps.addBatch();
            ps.setInt(1, 2); ps.setString(2, "8120 900313");
            ps.setObject(3, baseDate.minusDays(4)); ps.setObject(4, baseDate.minusDays(4).plusMinutes(45)); ps.setBoolean(5, true);
            ps.addBatch();
            ps.setInt(1, 2); ps.setString(2, "8120 900313");
            ps.setObject(3, baseDate.minusDays(1)); ps.setObject(4, baseDate.minusDays(1).plusMinutes(55)); ps.setBoolean(5, false);
            ps.addBatch();
            ps.setInt(1, 2); ps.setString(2, "8120 900313");
            ps.setObject(3, baseDate.plusHours(2)); ps.setObject(4, baseDate.plusHours(2).plusMinutes(48)); ps.setBoolean(5, false);
            ps.addBatch();

            // Екатерина: Диксит (8120 900314) — только победы
            for (int i = 0; i < 4; i++) {
                ps.setInt(1, 5); ps.setString(2, "8120 900314");
                ps.setObject(3, baseDate.minusDays(7 + i));
                ps.setObject(4, baseDate.minusDays(7 + i).plusMinutes(35));
                ps.setBoolean(5, true);
                ps.addBatch();
            }

            // Андрей: Пандемия (8120 900315)
            ps.setInt(1, 6); ps.setString(2, "8120 900315");
            ps.setObject(3, baseDate.minusDays(3)); ps.setObject(4, baseDate.minusDays(3).plusMinutes(70)); ps.setBoolean(5, true);
            ps.addBatch();
            ps.setInt(1, 6); ps.setString(2, "8120 900315");
            ps.setObject(3, baseDate.plusHours(4)); ps.setObject(4, baseDate.plusHours(4).plusMinutes(65)); ps.setBoolean(5, false);
            ps.addBatch();

            // Михаил: 7 Чудес (8120 900317)
            ps.setInt(1, 7); ps.setString(2, "8120 900317");
            ps.setObject(3, baseDate.minusDays(5)); ps.setObject(4, baseDate.minusDays(5).plusMinutes(55)); ps.setBoolean(5, true);
            ps.addBatch();
            ps.setInt(1, 7); ps.setString(2, "8120 900317");
            ps.setObject(3, baseDate.minusDays(2)); ps.setObject(4, baseDate.minusDays(2).plusMinutes(50)); ps.setBoolean(5, false);
            ps.addBatch();
            ps.setInt(1, 7); ps.setString(2, "8120 900317");
            ps.setObject(3, baseDate.plusHours(1)); ps.setObject(4, baseDate.plusHours(1).plusMinutes(52)); ps.setBoolean(5, true);
            ps.addBatch();

            // Тик-так-бумм (8120 900318)
            for (int i = 0; i < 5; i++) {
                ps.setInt(1, 8); ps.setString(2, "8120 900318");
                ps.setObject(3, baseDate.minusDays(10 - i * 2));
                ps.setObject(4, baseDate.minusDays(10 - i * 2).plusMinutes(25 + (i % 3) * 5));
                ps.setBoolean(5, i % 2 == 0);
                ps.addBatch();
            }

            ps.executeBatch();
        }
        log.info("Загружены игровые сессии с результатами");
    }
}
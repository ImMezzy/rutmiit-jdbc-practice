package com.gamerentals.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

/**
 * Инициализация схемы БД и заполнение тестовыми данными.
 */
public class SchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    public static void initialize() throws SQLException {
        log.info("Инициализация схемы БД...");
        executeSqlFile("schema.sql");
        seedTestData();
        log.info("Схема БД создана и заполнена тестовыми данными");
    }

    private static void executeSqlFile(String fileName) throws SQLException {
        String sql;
        try (InputStream is = SchemaInitializer.class.getClassLoader().getResourceAsStream(fileName)) {
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
                seedGenres(conn);
                seedFilms(conn);
                seedFilmGenres(conn);
                seedHalls(conn);
                seedSeats(conn);
                seedVisitors(conn);
                seedScreenings(conn);
                seedTickets(conn);
                conn.commit();
                log.info("Тестовые данные загружены");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static void seedGenres(Connection conn) throws SQLException {
        String sql = "INSERT INTO Жанр (название) VALUES (?)";
        String[] genres = {"Боевик", "Комедия", "Драма", "Фантастика", "Ужасы",
                "Триллер", "Мелодрама", "Анимация", "Документальный", "Приключения"};
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String g : genres) {
                ps.setString(1, g);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedFilms(Connection conn) throws SQLException {
        String sql = "INSERT INTO Фильм (название, продолжительность) VALUES (?, ?::TIME)";
        Object[][] films = {
                {"Интерстеллар", "02:49:00"},
                {"Начало", "02:28:00"},
                {"Матрица", "02:16:00"},
                {"Аватар", "02:42:00"},
                {"Джокер", "02:02:00"},
                {"Дюна", "02:35:00"},
                {"Оппенгеймер", "03:00:00"},
                {"Паразиты", "02:12:00"},
                {"Головоломка 2", "01:40:00"},
                {"Дэдпул и Росомаха", "02:08:00"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] f : films) {
                ps.setString(1, (String) f[0]);
                ps.setString(2, (String) f[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedFilmGenres(Connection conn) throws SQLException {
        String sql = "INSERT INTO Жанр_фильма (id_жанра, id_фильма) VALUES (?, ?)";
        int[][] links = {
                {4, 1}, {3, 1},   // Интерстеллар: Фантастика, Драма
                {4, 2}, {1, 2}, {6, 2},  // Начало: Фантастика, Боевик, Триллер
                {4, 3}, {1, 3},   // Матрица: Фантастика, Боевик
                {4, 4}, {10, 4},  // Аватар: Фантастика, Приключения
                {3, 5}, {6, 5},   // Джокер: Драма, Триллер
                {4, 6}, {10, 6}, {3, 6}, // Дюна: Фантастика, Приключения, Драма
                {3, 7},           // Оппенгеймер: Драма
                {3, 8}, {6, 8}, {2, 8},  // Паразиты: Драма, Триллер, Комедия
                {8, 9}, {2, 9},   // Головоломка 2: Анимация, Комедия
                {1, 10}, {2, 10}, {4, 10} // Дэдпул: Боевик, Комедия, Фантастика
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int[] l : links) {
                ps.setInt(1, l[0]);
                ps.setInt(2, l[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedHalls(Connection conn) throws SQLException {
        String sql = "INSERT INTO Зал (название, вместимость, тип_зала) VALUES (?, ?, ?)";
        Object[][] halls = {
                {"Большой зал", (short) 120, (short) 1},
                {"Малый зал", (short) 60, (short) 1},
                {"VIP зал", (short) 30, (short) 2},
                {"IMAX", (short) 200, (short) 3}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] h : halls) {
                ps.setString(1, (String) h[0]);
                ps.setShort(2, (short) h[1]);
                ps.setShort(3, (short) h[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedSeats(Connection conn) throws SQLException {
        String sql = "INSERT INTO Место (номер_места, номер_зала, ряд, тип_места, коэффициент_цены) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Большой зал (1): 10 рядов × 12 мест
            addSeatsForHall(ps, 1, 10, 12);
            // Малый зал (2): 6 рядов × 10 мест
            addSeatsForHall(ps, 2, 6, 10);
            // VIP зал (3): 5 рядов × 6 мест
            addSeatsForHall(ps, 3, 5, 6);
            // IMAX (4): 12 рядов × 16 мест (только первые 30 для простоты)
            addSeatsForHall(ps, 4, 10, 20);
            ps.executeBatch();
        }
    }

    private static void addSeatsForHall(PreparedStatement ps, int hallId, int rows, int seatsPerRow) throws SQLException {
        int seatNum = 1;
        for (int row = 1; row <= rows; row++) {
            for (int s = 1; s <= seatsPerRow; s++) {
                ps.setShort(1, (short) seatNum);
                ps.setShort(2, (short) hallId);
                ps.setShort(3, (short) row);
                // VIP-места в последних 2 рядах
                short seatType = (row >= rows - 1) ? (short) 2 : (short) 1;
                ps.setShort(4, seatType);
                // Коэффициент: VIP = 1.5, обычный = 1.0
                ps.setBigDecimal(5, seatType == 2
                        ? java.math.BigDecimal.valueOf(1.50)
                        : java.math.BigDecimal.valueOf(1.00));
                ps.addBatch();
                seatNum++;
            }
        }
    }

    private static void seedVisitors(Connection conn) throws SQLException {
        String sql = "INSERT INTO Посетитель (имя, номер_телефона, email) VALUES (?, ?, ?)";
        Object[][] visitors = {
                {"Иванов Иван", "+79001234567", "ivanov@mail.ru"},
                {"Петрова Анна", "+79009876543", "petrova@gmail.com"},
                {"Сидоров Пётр", "+79005551234", "sidorov@yandex.ru"},
                {"Козлова Мария", "+79003334455", "kozlova@mail.ru"},
                {"Волков Алексей", "+79007778899", "volkov@gmail.com"},
                {"Новикова Елена", "+79002223344", "novikova@mail.ru"},
                {"Морозов Дмитрий", "+79006665577", "morozov@yandex.ru"},
                {"Соколова Ольга", "+79001112233", "sokolova@gmail.com"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] v : visitors) {
                ps.setString(1, (String) v[0]);
                ps.setString(2, (String) v[1]);
                ps.setString(3, (String) v[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedScreenings(Connection conn) throws SQLException {
        String sql = "INSERT INTO Сеанс (номер_зала, id_фильма, дата_и_время, базовая_стоимость) VALUES (?, ?, ?::TIMESTAMPTZ, ?::MONEY)";
        Object[][] screenings = {
                {1, 1, "2026-04-24 10:00:00+03", "450.00"},
                {1, 2, "2026-04-24 14:00:00+03", "400.00"},
                {2, 3, "2026-04-24 11:00:00+03", "350.00"},
                {2, 5, "2026-04-24 15:00:00+03", "350.00"},
                {3, 6, "2026-04-24 19:00:00+03", "800.00"},
                {4, 4, "2026-04-24 12:00:00+03", "600.00"},
                {4, 7, "2026-04-24 18:00:00+03", "650.00"},
                {1, 9, "2026-04-25 10:00:00+03", "300.00"},
                {2, 10, "2026-04-25 14:00:00+03", "400.00"},
                {3, 8, "2026-04-25 20:00:00+03", "750.00"},
                {4, 1, "2026-04-25 16:00:00+03", "550.00"},
                {1, 6, "2026-04-26 11:00:00+03", "450.00"},
                {2, 2, "2026-04-26 13:00:00+03", "350.00"},
                {3, 7, "2026-04-26 19:00:00+03", "850.00"},
                {4, 10, "2026-04-26 21:00:00+03", "500.00"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] s : screenings) {
                ps.setShort(1, ((Integer) s[0]).shortValue());
                ps.setInt(2, (Integer) s[1]);
                ps.setString(3, (String) s[2]);
                ps.setBigDecimal(4, new BigDecimal((String) s[3]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedTickets(Connection conn) throws SQLException {
        String sql = "INSERT INTO Билет (id_посетителя, id_сеанса, номер_места, номер_зала, дата_и_время) " +
                "VALUES (?, ?, ?, ?, ?::TIMESTAMPTZ)";
        // (visitor_id, screening_id, seat_number, hall_number, purchase_time)
        // Зал 1: места 1-120, Зал 2: места 1-60, Зал 3: места 1-30, Зал 4: места 1-200
        Object[][] tickets = {
                {1, 1, 5, 1, "2026-04-23 09:00:00+03"},
                {1, 1, 6, 1, "2026-04-23 09:00:00+03"},
                {2, 1, 15, 1, "2026-04-23 10:30:00+03"},
                {3, 2, 10, 1, "2026-04-23 12:00:00+03"},
                {4, 3, 3, 2, "2026-04-23 09:15:00+03"},
                {5, 3, 4, 2, "2026-04-23 09:15:00+03"},
                {6, 4, 20, 2, "2026-04-23 14:00:00+03"},
                {2, 5, 1, 3, "2026-04-23 18:30:00+03"},
                {7, 5, 2, 3, "2026-04-23 18:30:00+03"},
                {8, 6, 50, 4, "2026-04-23 11:00:00+03"},
                {1, 6, 51, 4, "2026-04-23 11:00:00+03"},
                {3, 7, 100, 4, "2026-04-23 17:00:00+03"},
                {5, 7, 101, 4, "2026-04-23 17:00:00+03"},
                {4, 8, 25, 1, "2026-04-24 09:00:00+03"},
                {6, 9, 8, 2, "2026-04-24 13:00:00+03"},
                {7, 10, 5, 3, "2026-04-24 19:30:00+03"},
                {8, 11, 80, 4, "2026-04-24 15:00:00+03"},
                {1, 12, 30, 1, "2026-04-25 10:00:00+03"},
                {2, 13, 12, 2, "2026-04-25 12:00:00+03"},
                {3, 14, 10, 3, "2026-04-25 18:00:00+03"},
                {5, 15, 120, 4, "2026-04-25 20:00:00+03"},
                {1, 2, 20, 1, "2026-04-23 13:00:00+03"},
                {2, 4, 25, 2, "2026-04-23 14:30:00+03"},
                {7, 6, 55, 4, "2026-04-23 11:30:00+03"},
                {8, 1, 30, 1, "2026-04-23 09:45:00+03"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] t : tickets) {
                ps.setInt(1, (Integer) t[0]);
                ps.setInt(2, (Integer) t[1]);
                ps.setShort(3, ((Integer) t[2]).shortValue());
                ps.setShort(4, ((Integer) t[3]).shortValue());
                ps.setString(5, (String) t[4]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}






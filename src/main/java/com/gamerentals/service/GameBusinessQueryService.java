package com.gamerentals.service;

import com.gamerentals.db.ConnectionManager;

import java.sql.*;
import java.time.format.DateTimeFormatter;

import static com.gamerentals.service.CrudDemoService.truncate;

public class GameBusinessQueryService {

    private static final DateTimeFormatter DT_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    // Выручка от аренды коробок по играм
    public void revenueByGame() throws SQLException {
        System.out.println("=== Выручка от аренды по играм ===");
        String sql = """
                SELECT
                    g.name AS игра,
                    COUNT(br.id) AS аренд,
                    SUM(CASE WHEN br.fine > 0 THEN br.fine ELSE 0 END) AS штрафы,
                    ROUND(AVG(EXTRACT(EPOCH FROM (br.date_of_return - br.date_of_rent))/3600), 1) AS сред_часов
                FROM games g
                JOIN boxes b ON g.id = b.game_id
                JOIN box_rent br ON b.id = br.box_id AND b.game_id = br.game_id
                GROUP BY g.id, g.name
                ORDER BY аренд DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-24s %-10s %-12s %-12s%n", "Игра", "Аренд", "Штрафы (₽)", "Сред. время (ч)");
            while (rs.next()) {
                System.out.printf("%-24s %-10d %-12d %-12.1f%n",
                        truncate(rs.getString("игра"), 23),
                        rs.getInt("аренд"),
                        rs.getInt("штрафы"),
                        rs.getDouble("сред_часов"));
            }
        }
        System.out.println();
    }

    // Загруженность коробок по играм
    public void boxUtilization() throws SQLException {
        System.out.println("=== Загруженность коробок по играм ===");
        String sql = """
                SELECT
                    g.name AS игра,
                    COUNT(DISTINCT b.id) AS всего_коробок,
                    COUNT(DISTINCT CASE WHEN b.status > 0 THEN b.id END) AS доступно,
                    ROUND(COUNT(DISTINCT CASE WHEN b.status = 0 THEN b.id END) * 100.0 / 
                          NULLIF(COUNT(DISTINCT b.id), 0), 1) AS процент_занято
                FROM games g
                LEFT JOIN boxes b ON g.id = b.game_id
                GROUP BY g.id, g.name
                ORDER BY процент_занято DESC NULLS LAST
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-24s %-8s %-10s %-12s%n", "Игра", "Всего", "Доступно", "Занято (%)");
            while (rs.next()) {
                System.out.printf("%-24s %-8d %-10d %-12.1f%n",
                        truncate(rs.getString("игра"), 23),
                        rs.getInt("всего_коробок"),
                        rs.getInt("доступно"),
                        rs.getDouble("процент_занято"));
            }
        }
        System.out.println();
    }

    // Топ-5 популярных игр по количеству аренд
    public void top5Games() throws SQLException {
        System.out.println("=== Топ-5 популярных игр ===");
        String sql = """
                SELECT
                    g.name,
                    g.difficulty,
                    COUNT(br.id) AS аренд,
                    COUNT(DISTINCT br.client_pass_number) AS уникальных_клиентов
                FROM games g
                JOIN boxes b ON g.id = b.game_id
                LEFT JOIN box_rent br ON b.id = br.box_id AND b.game_id = br.game_id
                GROUP BY g.id, g.name, g.difficulty
                ORDER BY аренд DESC
                LIMIT 5
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-4s %-24s %-12s %-10s %-12s%n", "#", "Игра", "Сложность", "Аренд", "Клиентов");
            int rank = 1;
            while (rs.next()) {
                System.out.printf("%-4s %-24s %-12s %-10d %-12d%n",
                        "#" + rank,
                        truncate(rs.getString("name"), 23),
                        rs.getString("difficulty"),
                        rs.getInt("аренд"),
                        rs.getInt("уникальных_клиентов"));
                rank++;
            }
        }
        System.out.println();
    }

    // Активные аренды на дату
    public void activeRentsByDate(String date) throws SQLException {
        System.out.println("=== Активные аренды на " + date + " ===");
        String sql = """
                SELECT
                    br.id,
                    g.name AS игра,
                    c.name || ' ' || c.last_name AS клиент,
                    br.box_id,
                    TO_CHAR(br.date_of_rent, 'HH24:MI') AS взята,
                    TO_CHAR(br.date_of_return, 'HH24:MI') AS вернуть_до,
                    br.fine
                FROM box_rent br
                JOIN games g ON br.game_id = g.id
                JOIN clients c ON br.client_pass_number = c.pass_number
                WHERE br.date_of_rent::DATE <= ?::DATE 
                  AND br.date_of_return::DATE >= ?::DATE
                  AND br.date_of_return IS NOT NULL
                ORDER BY br.date_of_rent
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ps.setString(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-5s %-22s %-20s %-8s %-8s %-10s %-8s%n",
                        "ID", "Игра", "Клиент", "Коробка", "Взята", "Вернуть", "Штраф");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-5d %-22s %-20s %-8d %-8s %-10s %-8d%n",
                            rs.getInt("id"),
                            truncate(rs.getString("игра"), 21),
                            truncate(rs.getString("клиент"), 19),
                            rs.getInt("box_id"),
                            rs.getString("взята"),
                            rs.getString("вернуть_до"),
                            rs.getInt("fine"));
                }
                if (!found) System.out.println("Активных аренд не найдено");
            }
        }
        System.out.println();
    }

    // История аренд клиента
    public void clientRentalHistory(String passNumber) throws SQLException {
        System.out.println("=== История аренд клиента " + passNumber + " ===");
        String sql = """
                SELECT
                    g.name AS игра,
                    br.box_id,
                    TO_CHAR(br.date_of_rent, 'DD.MM.YYYY HH24:MI') AS взята,
                    TO_CHAR(br.date_of_return, 'DD.MM.YYYY HH24:MI') AS возвращена,
                    CASE WHEN br.fine > 0 THEN br.fine ELSE 0 END AS штраф
                FROM box_rent br
                JOIN games g ON br.game_id = g.id
                WHERE br.client_pass_number = ?
                ORDER BY br.date_of_rent DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passNumber);
            try (ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (first) {
                        System.out.printf("%-24s %-10s %-20s %-20s %-10s%n",
                                "Игра", "Коробка", "Взята", "Возвращена", "Штраф");
                        first = false;
                    }
                    System.out.printf("%-24s %-10d %-20s %-20s %-10d%n",
                            truncate(rs.getString("игра"), 23),
                            rs.getInt("box_id"),
                            rs.getString("взята"),
                            rs.getString("возвращена"),
                            rs.getInt("штраф"));
                }
                if (first) System.out.println("Нет данных");
            }
        }
        System.out.println();
    }

    // Популярность по уровню сложности
    public void difficultyPopularity() throws SQLException {
        System.out.println("=== Популярность по сложности ===");
        String sql = """
                SELECT
                    g.difficulty,
                    COUNT(DISTINCT g.id) AS игр,
                    COUNT(br.id) AS аренд,
                    ROUND(AVG(EXTRACT(EPOCH FROM (br.date_of_return - br.date_of_rent))/3600), 1) AS сред_время_ч
                FROM games g
                LEFT JOIN boxes b ON g.id = b.game_id
                LEFT JOIN box_rent br ON b.id = br.box_id AND b.game_id = br.game_id
                GROUP BY g.difficulty
                ORDER BY аренд DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-12s %-8s %-10s %-15s%n", "Сложность", "Игр", "Аренд", "Сред. время (ч)");
            while (rs.next()) {
                System.out.printf("%-12s %-8d %-10d %-15.1f%n",
                        rs.getString("difficulty"),
                        rs.getInt("игр"),
                        rs.getInt("аренд"),
                        rs.getDouble("сред_время_ч"));
            }
        }
        System.out.println();
    }

    // Доступные коробки для игры
    public void availableBoxes(int gameId) throws SQLException {
        System.out.println("=== Доступные коробки для игры #" + gameId + " ===");
        String sql = """
                SELECT
                    b.id AS коробка,
                    b.status,
                    CASE WHEN br.id IS NULL THEN 'свободна' ELSE 'занята' END AS состояние
                FROM boxes b
                LEFT JOIN box_rent br ON b.id = br.box_id 
                    AND b.game_id = br.game_id 
                    AND br.date_of_return IS NULL
                WHERE b.game_id = ?
                ORDER BY b.status DESC, b.id
                LIMIT 20
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-10s %-10s %-12s%n", "Коробка", "Статус", "Состояние");
                int count = 0;
                while (rs.next()) {
                    System.out.printf("%-10d %-10.2f %-12s%n",
                            rs.getInt("коробка"),
                            rs.getDouble("status"),
                            rs.getString("состояние"));
                    count++;
                }
                System.out.println("Показано: " + count + " коробок (LIMIT 20)");
            }
        }
        System.out.println();
    }


    public void topClients() throws SQLException {
        System.out.println("=== Самые активные клиенты ===");
        String sql = """
                SELECT
                    c.name || ' ' || c.last_name AS клиент,
                    c.pass_number,
                    COUNT(br.id) AS аренд,
                    COUNT(DISTINCT br.game_id) AS разных_игр,
                    SUM(br.fine) AS всего_штрафов,
                    MIN(br.date_of_rent)::DATE AS первая_аренда,
                    MAX(br.date_of_rent)::DATE AS последняя_аренда
                FROM clients c
                JOIN box_rent br ON c.pass_number = br.client_pass_number
                GROUP BY c.pass_number, c.name, c.last_name
                HAVING COUNT(br.id) >= 2
                ORDER BY аренд DESC, всего_штрафов DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-24s %-12s %-8s %-12s %-10s %-12s %-12s%n",
                    "Клиент", "Паспорт", "Аренд", "Разных игр", "Штрафы", "Первая", "Последняя");
            while (rs.next()) {
                System.out.printf("%-24s %-12s %-8d %-12d %-10d %-12s %-12s%n",
                        truncate(rs.getString("клиент"), 23),
                        rs.getString("pass_number"),
                        rs.getInt("аренд"),
                        rs.getInt("разных_игр"),
                        rs.getInt("всего_штрафов"),
                        rs.getDate("первая_аренда"),
                        rs.getDate("последняя_аренда"));
            }
        }
        System.out.println();
    }

    // 10. Конфликты игровых сессий (пересечения по времени)
    public void sessionConflicts() throws SQLException {
        System.out.println("=== Конфликты игровых сессий (пересечения) ===");
        String sql = """
                SELECT
                    g.name AS игра,
                    c1.name || ' ' || c1.last_name AS клиент_1,
                    TO_CHAR(gs1.start_time, 'DD.MM HH24:MI') AS начало_1,
                    TO_CHAR(gs1.end_time, 'HH24:MI') AS конец_1,
                    c2.name || ' ' || c2.last_name AS клиент_2,
                    TO_CHAR(gs2.start_time, 'DD.MM HH24:MI') AS начало_2
                FROM game_session gs1
                JOIN game_session gs2 ON gs1.game_id = gs2.game_id 
                    AND gs1.id < gs2.id
                    AND gs1.client_pass_number != gs2.client_pass_number
                JOIN games g ON gs1.game_id = g.id
                JOIN clients c1 ON gs1.client_pass_number = c1.pass_number
                JOIN clients c2 ON gs2.client_pass_number = c2.pass_number
                WHERE gs2.start_time < gs1.end_time
                  AND gs2.end_time > gs1.start_time
                ORDER BY gs1.start_time
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-20s %-18s %-12s %-8s %-18s %-12s%n",
                    "Игра", "Клиент 1", "Начало 1", "Конец 1", "Клиент 2", "Начало 2");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-20s %-18s %-12s %-8s %-18s %-12s%n",
                        truncate(rs.getString("игра"), 19),
                        truncate(rs.getString("клиент_1"), 17),
                        rs.getString("начало_1"),
                        rs.getString("конец_1"),
                        truncate(rs.getString("клиент_2"), 17),
                        rs.getString("начало_2"));
            }
            if (!found) System.out.println("Конфликтов не найдено");
        }
        System.out.println();
    }

    // 11. Статистика по игротке (GameAttraction)
    public void gameAttractionStats() throws SQLException {
        System.out.println("=== Статистика по игротке ===");
        String sql = """
                SELECT
                    g.name AS игра,
                    COUNT(ga.id) AS сессий,
                    COUNT(DISTINCT ga.client_pass_number) AS клиентов,
                    ROUND(AVG(EXTRACT(EPOCH FROM (ga.end_time - ga.start_time))/60), 1) AS сред_длит_мин,
                    SUM(CASE WHEN EXTRACT(HOUR FROM ga.start_time) BETWEEN 18 AND 23 THEN 1 ELSE 0 END) AS вечерних
                FROM games g
                JOIN game_attraction ga ON g.id = ga.game_id
                GROUP BY g.id, g.name
                ORDER BY сессий DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-24s %-10s %-12s %-15s %-10s%n",
                    "Игра", "Сессий", "Клиентов", "Сред. длительн.", "Вечерних");
            while (rs.next()) {
                System.out.printf("%-24s %-10d %-12d %-15.1f %-10d%n",
                        truncate(rs.getString("игра"), 23),
                        rs.getInt("сессий"),
                        rs.getInt("клиентов"),
                        rs.getDouble("сред_длит_мин"),
                        rs.getInt("вечерних"));
            }
        }
        System.out.println();
    }

    // 12. Win-rate по играм (из GameSession)
    public void gameWinRate() throws SQLException {
        System.out.println("=== Win-rate по играм ===");
        String sql = """
                SELECT
                    g.name AS игра,
                    COUNT(gs.id) AS всего_сессий,
                    SUM(CASE WHEN gs.game_result = true THEN 1 ELSE 0 END) AS побед,
                    ROUND(SUM(CASE WHEN gs.game_result = true THEN 1 ELSE 0 END) * 100.0 / 
                          NULLIF(COUNT(gs.id), 0), 1) AS винрейт
                FROM games g
                JOIN game_session gs ON g.id = gs.game_id
                GROUP BY g.id, g.name
                HAVING COUNT(gs.id) >= 5
                ORDER BY винрейт DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-24s %-10s %-10s %-10s%n", "Игра", "Всего", "Побед", "Win-rate %");
            while (rs.next()) {
                System.out.printf("%-24s %-10d %-10d %-10.1f%n",
                        truncate(rs.getString("игра"), 23),
                        rs.getInt("всего_сессий"),
                        rs.getInt("побед"),
                        rs.getDouble("винрейт"));
            }
        }
        System.out.println();
    }

}
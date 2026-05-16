package com.gamerentals.service;

import com.gamerentals.db.ConnectionManager;

import java.sql.*;

public class BusinessQueryService {

    // 1. Выручка по фильмам
    public void revenueByFilm() throws SQLException {
        System.out.println("=== Выручка по фильмам ===");
        String sql = """
                SELECT
                    ф.название AS фильм,
                    COUNT(б.id_билета) AS билетов,
                    SUM(с.базовая_стоимость::NUMERIC * м.коэффициент_цены) AS выручка
                FROM Билет б
                JOIN Сеанс с ON б.id_сеанса = с.id_сеанса
                JOIN Фильм ф ON с.id_фильма = ф.id_фильма
                JOIN Место м ON б.номер_места = м.номер_места AND б.номер_зала = м.номер_зала
                GROUP BY ф.id_фильма, ф.название
                ORDER BY выручка DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-24s %-10s %-15s%n", "Фильм", "Билетов", "Выручка (₽)");
            while (rs.next()) {
                System.out.printf("%-24s %-10d %-15.2f%n",
                        rs.getString("фильм"),
                        rs.getInt("билетов"),
                        rs.getBigDecimal("выручка"));
            }
        }
        System.out.println();
    }

    // 2. Заполняемость залов
    public void hallOccupancy() throws SQLException {
        System.out.println("=== Заполняемость залов по сеансам ===");
        String sql = """
                SELECT
                    с.id_сеанса,
                    ф.название AS фильм,
                    з.название AS зал,
                    з.вместимость,
                    COUNT(б.id_билета) AS продано,
                    ROUND(COUNT(б.id_билета) * 100.0 / з.вместимость, 1) AS процент
                FROM Сеанс с
                JOIN Фильм ф ON с.id_фильма = ф.id_фильма
                JOIN Зал з ON с.номер_зала = з.номер_зала
                LEFT JOIN Билет б ON с.id_сеанса = б.id_сеанса
                GROUP BY с.id_сеанса, ф.название, з.название, з.вместимость
                ORDER BY процент DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-5s %-22s %-14s %-6s %-8s %-6s%n",
                    "ID", "Фильм", "Зал", "Мест", "Продано", "%");
            while (rs.next()) {
                System.out.printf("%-5d %-22s %-14s %-6d %-8d %-6.1f%n",
                        rs.getInt("id_сеанса"),
                        truncate(rs.getString("фильм"), 21),
                        rs.getString("зал"),
                        rs.getInt("вместимость"),
                        rs.getInt("продано"),
                        rs.getDouble("процент"));
            }
        }
        System.out.println();
    }

    // 3. Топ-5 популярных фильмов
    public void top5Films() throws SQLException {
        System.out.println("=== Топ-5 популярных фильмов ===");
        String sql = """
            SELECT
                ф.название,
                COUNT(б.id_билета) AS билетов,
                COUNT(DISTINCT с.id_сеанса) AS сеансов
            FROM Фильм ф
            JOIN Сеанс с ON ф.id_фильма = с.id_фильма
            LEFT JOIN Билет б ON с.id_сеанса = б.id_сеанса
            GROUP BY ф.id_фильма, ф.название
            ORDER BY билетов DESC
            LIMIT 5
            """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-4s %-24s %-10s %-10s%n", "#", "Фильм", "Билетов", "Сеансов");
            int rank = 1;
            while (rs.next()) {
                System.out.printf("%-4s %-24s %-10d %-10d%n",
                        "#" + rank,
                        rs.getString("название"),
                        rs.getInt("билетов"),
                        rs.getInt("сеансов"));
                rank++;
            }
        }
        System.out.println();
    }

    // 4. Расписание сеансов на дату
    public void scheduleByDate(String date) throws SQLException {
        System.out.println("=== Расписание на " + date + " ===");
        String sql = """
                SELECT
                    с.id_сеанса,
                    ф.название AS фильм,
                    ф.продолжительность,
                    з.название AS зал,
                    TO_CHAR(с.дата_и_время, 'HH24:MI') AS время,
                    с.базовая_стоимость::NUMERIC AS цена,
                    з.вместимость - COUNT(б.id_билета) AS свободно
                FROM Сеанс с
                JOIN Фильм ф ON с.id_фильма = ф.id_фильма
                JOIN Зал з ON с.номер_зала = з.номер_зала
                LEFT JOIN Билет б ON с.id_сеанса = б.id_сеанса
                WHERE с.дата_и_время::DATE = ?::DATE
                GROUP BY с.id_сеанса, ф.название, ф.продолжительность, з.название, с.дата_и_время, с.базовая_стоимость, з.вместимость
                ORDER BY с.дата_и_время
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-5s %-22s %-8s %-14s %-6s %-8s %-8s%n",
                        "ID", "Фильм", "Длит.", "Зал", "Время", "Цена", "Свободно");
                while (rs.next()) {
                    System.out.printf("%-5d %-22s %-8s %-14s %-6s %-8.0f %-8d%n",
                            rs.getInt("id_сеанса"),
                            truncate(rs.getString("фильм"), 21),
                            rs.getTime("продолжительность").toLocalTime(),
                            rs.getString("зал"),
                            rs.getString("время"),
                            rs.getBigDecimal("цена"),
                            rs.getInt("свободно"));
                }
            }
        }
        System.out.println();
    }

    // 5. История посещений посетителя
    public void visitorHistory(int visitorId) throws SQLException {
        System.out.println("=== История посещений посетителя #" + visitorId + " ===");
        String sql = """
                SELECT
                    п.имя,
                    ф.название AS фильм,
                    з.название AS зал,
                    б.номер_места AS место,
                    TO_CHAR(с.дата_и_время, 'DD.MM.YYYY HH24:MI') AS сеанс,
                    TO_CHAR(б.дата_и_время, 'DD.MM.YYYY HH24:MI') AS покупка,
                    (с.базовая_стоимость::NUMERIC * м.коэффициент_цены) AS стоимость
                FROM Билет б
                JOIN Посетитель п ON б.id_посетителя = п.id_посетителя
                JOIN Сеанс с ON б.id_сеанса = с.id_сеанса
                JOIN Фильм ф ON с.id_фильма = ф.id_фильма
                JOIN Зал з ON с.номер_зала = з.номер_зала
                JOIN Место м ON б.номер_места = м.номер_места AND б.номер_зала = м.номер_зала
                WHERE п.id_посетителя = ?
                ORDER BY с.дата_и_время
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, visitorId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (first) {
                        System.out.println("Посетитель: " + rs.getString("имя"));
                        System.out.printf("%-22s %-14s %-6s %-18s %-10s%n",
                                "Фильм", "Зал", "Место", "Дата сеанса", "Цена");
                        first = false;
                    }
                    System.out.printf("%-22s %-14s %-6d %-18s %-10.2f%n",
                            truncate(rs.getString("фильм"), 21),
                            rs.getString("зал"),
                            rs.getInt("место"),
                            rs.getString("сеанс"),
                            rs.getBigDecimal("стоимость"));
                }
                if (first) System.out.println("Нет данных");
            }
        }
        System.out.println();
    }

    // 6. Популярность жанров
    public void genrePopularity() throws SQLException {
        System.out.println("=== Популярность жанров ===");
        String sql = """
                SELECT
                    ж.название AS жанр,
                    COUNT(DISTINCT с.id_сеанса) AS сеансов,
                    COUNT(б.id_билета) AS билетов
                FROM Жанр ж
                JOIN Жанр_фильма жф ON ж.id_жанра = жф.id_жанра
                JOIN Фильм ф ON жф.id_фильма = ф.id_фильма
                JOIN Сеанс с ON ф.id_фильма = с.id_фильма
                LEFT JOIN Билет б ON с.id_сеанса = б.id_сеанса
                GROUP BY ж.id_жанра, ж.название
                ORDER BY билетов DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-22s %-10s %-10s%n", "Жанр", "Сеансов", "Билетов");
            while (rs.next()) {
                System.out.printf("%-22s %-10d %-10d%n",
                        rs.getString("жанр"),
                        rs.getInt("сеансов"),
                        rs.getInt("билетов"));
            }
        }
        System.out.println();
    }

    // 7. Свободные места на сеанс
    public void availableSeats(int screeningId) throws SQLException {
        System.out.println("=== Свободные места на сеанс #" + screeningId + " ===");
        String sql = """
                SELECT
                    м.номер_места, м.ряд, м.тип_места,
                    м.коэффициент_цены,
                    (с.базовая_стоимость::NUMERIC * м.коэффициент_цены) AS итого
                FROM Сеанс с
                JOIN Место м ON с.номер_зала = м.номер_зала
                LEFT JOIN Билет б ON б.id_сеанса = с.id_сеанса
                    AND б.номер_места = м.номер_места AND б.номер_зала = м.номер_зала
                WHERE с.id_сеанса = ? AND б.id_билета IS NULL
                ORDER BY м.ряд, м.номер_места
                LIMIT 20
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, screeningId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-8s %-6s %-10s %-8s %-10s%n",
                        "Место", "Ряд", "Тип", "Коэфф", "Цена (₽)");
                int count = 0;
                while (rs.next()) {
                    String typeName = rs.getShort("тип_места") == 2 ? "VIP" : "Стандарт";
                    System.out.printf("%-8d %-6d %-10s %-8.2f %-10.2f%n",
                            rs.getInt("номер_места"),
                            rs.getInt("ряд"),
                            typeName,
                            rs.getBigDecimal("коэффициент_цены"),
                            rs.getBigDecimal("итого"));
                    count++;
                }
                System.out.println("Показано: " + count + " мест (LIMIT 20)");
            }
        }
        System.out.println();
    }

    // 8. Средний чек по залам
    public void avgCheckByHall() throws SQLException {
        System.out.println("=== Средний чек по залам ===");
        String sql = """
                SELECT
                    з.название AS зал,
                    з.тип_зала,
                    COUNT(б.id_билета) AS билетов,
                    ROUND(AVG(с.базовая_стоимость::NUMERIC * м.коэффициент_цены), 2) AS средний_чек,
                    ROUND(MIN(с.базовая_стоимость::NUMERIC * м.коэффициент_цены), 2) AS мин_цена,
                    ROUND(MAX(с.базовая_стоимость::NUMERIC * м.коэффициент_цены), 2) AS макс_цена
                FROM Билет б
                JOIN Сеанс с ON б.id_сеанса = с.id_сеанса
                JOIN Зал з ON с.номер_зала = з.номер_зала
                JOIN Место м ON б.номер_места = м.номер_места AND б.номер_зала = м.номер_зала
                GROUP BY з.номер_зала, з.название, з.тип_зала
                ORDER BY средний_чек DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-16s %-10s %-10s %-12s %-10s %-10s%n",
                    "Зал", "Тип", "Билетов", "Средний", "Мин", "Макс");
            while (rs.next()) {
                String type = switch (rs.getShort("тип_зала")) {
                    case 2 -> "VIP";
                    case 3 -> "IMAX";
                    default -> "Стандарт";
                };
                System.out.printf("%-16s %-10s %-10d %-12.2f %-10.2f %-10.2f%n",
                        rs.getString("зал"), type,
                        rs.getInt("билетов"),
                        rs.getBigDecimal("средний_чек"),
                        rs.getBigDecimal("мин_цена"),
                        rs.getBigDecimal("макс_цена"));
            }
        }
        System.out.println();
    }

    // 9. Самые активные посетители
    public void topVisitors() throws SQLException {
        System.out.println("=== Самые активные посетители ===");
        String sql = """
                SELECT
                    п.имя,
                    п.email,
                    COUNT(б.id_билета) AS визитов,
                    ROUND(SUM(с.базовая_стоимость::NUMERIC * м.коэффициент_цены), 2) AS потрачено,
                    MIN(с.дата_и_время)::DATE AS первый_визит,
                    MAX(с.дата_и_время)::DATE AS последний_визит
                FROM Посетитель п
                JOIN Билет б ON п.id_посетителя = б.id_посетителя
                JOIN Сеанс с ON б.id_сеанса = с.id_сеанса
                JOIN Место м ON б.номер_места = м.номер_места AND б.номер_зала = м.номер_зала
                GROUP BY п.id_посетителя, п.имя, п.email
                HAVING COUNT(б.id_билета) >= 2
                ORDER BY визитов DESC, потрачено DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-20s %-24s %-8s %-12s %-12s %-12s%n",
                    "Имя", "Email", "Визитов", "Потрачено", "Первый", "Последний");
            while (rs.next()) {
                System.out.printf("%-20s %-24s %-8d %-12.2f %-12s %-12s%n",
                        truncate(rs.getString("имя"), 19),
                        truncate(rs.getString("email"), 23),
                        rs.getInt("визитов"),
                        rs.getBigDecimal("потрачено"),
                        rs.getDate("первый_визит"),
                        rs.getDate("последний_визит"));
            }
        }
        System.out.println();
    }

    // 10. Пересечение сеансов
    public void screeningConflicts() throws SQLException {
        System.out.println("=== Пересечение сеансов в одном зале ===");
        String sql = """
                SELECT
                    з.название AS зал,
                    ф1.название AS фильм_1,
                    TO_CHAR(с1.дата_и_время, 'DD.MM HH24:MI') AS начало_1,
                    TO_CHAR(с1.дата_и_время + ф1.продолжительность, 'HH24:MI') AS конец_1,
                    ф2.название AS фильм_2,
                    TO_CHAR(с2.дата_и_время, 'DD.MM HH24:MI') AS начало_2
                FROM Сеанс с1
                JOIN Сеанс с2 ON с1.номер_зала = с2.номер_зала AND с1.id_сеанса < с2.id_сеанса
                JOIN Фильм ф1 ON с1.id_фильма = ф1.id_фильма
                JOIN Фильм ф2 ON с2.id_фильма = ф2.id_фильма
                JOIN Зал з ON с1.номер_зала = з.номер_зала
                WHERE с2.дата_и_время < с1.дата_и_время + ф1.продолжительность
                  AND с2.дата_и_время > с1.дата_и_время
                ORDER BY с1.дата_и_время
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-14s %-20s %-12s %-8s %-20s %-12s%n",
                    "Зал", "Фильм 1", "Начало 1", "Конец 1", "Фильм 2", "Начало 2");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-14s %-20s %-12s %-8s %-20s %-12s%n",
                        rs.getString("зал"),
                        truncate(rs.getString("фильм_1"), 19),
                        rs.getString("начало_1"),
                        rs.getString("конец_1"),
                        truncate(rs.getString("фильм_2"), 19),
                        rs.getString("начало_2"));
            }
            if (!found) System.out.println("Конфликтов не найдено");
        }
        System.out.println();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
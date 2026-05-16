package com.gamerentals;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.db.SchemaInitializerGames;
import com.gamerentals.service.GameBusinessQueryService;
import com.gamerentals.service.GameCrudDemoService;

import java.sql.SQLException;
import java.util.Scanner;

public class MainGames {

    private static final GameCrudDemoService gameCrudDemo = new GameCrudDemoService();
    private static final GameBusinessQueryService gameBizQuery = new GameBusinessQueryService();

    public static void main(String[] args) {
        System.out.println("=== JDBC BoardGames Demo (Java 21 · PostgreSQL 17 · HikariCP) ===\n");

        try {
            SchemaInitializerGames.initialize();
            System.out.println("БД игр готова.\n");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.print("""
                    [1] CRUD  [2] Запросы  [3] Всё  [0] Выход
                    > """);

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> runCrudMenu(scanner);
                    case "2" -> runBusinessMenu(scanner);
                    case "3" -> runAllDemo();
                    case "0" -> running = false;
                    default  -> System.out.println("Неверный выбор.");
                }
            } catch (SQLException e) {
                System.err.println("Ошибка SQL: " + e.getMessage());
            }
        }

        System.out.println("До свидания!");
        ConnectionManager.close();
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    private static void runCrudMenu(Scanner scanner) throws SQLException {
        while (true) {
            System.out.print("""
                    [1] Create  [2] Read  [3] Update  [4] Delete
                    [5] Batch   [6] Транзакция [7] Всё  [0] Назад
                    > """);

            switch (scanner.nextLine().trim()) {
                case "1" -> gameCrudDemo.demoCreate();
                case "2" -> gameCrudDemo.demoRead();
                case "3" -> gameCrudDemo.demoUpdate();
                case "4" -> gameCrudDemo.demoDelete();
                case "5" -> gameCrudDemo.demoBatchInsert();
                case "6" -> gameCrudDemo.demoTransaction();
                case "7" -> runAllCrud();
                case "0" -> { return; }
                default  -> System.out.println("Неверный выбор.");
            }
        }
    }

    private static void runAllCrud() throws SQLException {
        gameCrudDemo.demoCreate();
        gameCrudDemo.demoRead();
        gameCrudDemo.demoUpdate();
        gameCrudDemo.demoDelete();
        gameCrudDemo.demoBatchInsert();
        gameCrudDemo.demoTransaction();
    }

    // ── Бизнес-запросы ────────────────────────────────────────────────────────

    private static void runBusinessMenu(Scanner scanner) throws SQLException {
        while (true) {
            System.out.print("""
                    [1] Выручка      [2] Загруженность  [3] Топ-5 игр
                    [4] Аренды       [5] Клиент         [6] Сложность
                    [7] Коробки      [8] Топ-клиенты    [9] Конфликты
                    [10] Игротека    [11] Win-rate      [12] Всё
                    [0] Назад
                    > """);

            switch (scanner.nextLine().trim()) {
                case "1"  -> gameBizQuery.revenueByGame();
                case "2"  -> gameBizQuery.boxUtilization();
                case "3"  -> gameBizQuery.top5Games();
                case "4"  -> {
                    System.out.print("Дата [2026-04-24]: ");
                    String d = scanner.nextLine().trim();
                    gameBizQuery.activeRentsByDate(d.isEmpty() ? "2026-04-24" : d);
                }
                case "5"  -> {
                    System.out.print("Паспорт клиента [8120 900311]: ");
                    String p = scanner.nextLine().trim();
                    gameBizQuery.clientRentalHistory(p.isEmpty() ? "8120 900311" : p);
                }
                case "6"  -> gameBizQuery.difficultyPopularity();
                case "7"  -> {
                    System.out.print("ID игры [1]: ");
                    String g = scanner.nextLine().trim();
                    gameBizQuery.availableBoxes(g.isEmpty() ? 1 : Integer.parseInt(g));
                }
                case "8"  -> gameBizQuery.topClients();
                case "9" -> gameBizQuery.sessionConflicts();
                case "10" -> gameBizQuery.gameAttractionStats();
                case "11" -> gameBizQuery.gameWinRate();
                case "12" -> runAllBusinessQueries();
                case "0"  -> { return; }
                default   -> System.out.println("Неверный выбор.");
            }
        }
    }

    private static void runAllBusinessQueries() throws SQLException {
        gameBizQuery.revenueByGame();
        gameBizQuery.boxUtilization();
        gameBizQuery.top5Games();
        gameBizQuery.activeRentsByDate("2026-04-24");
        gameBizQuery.clientRentalHistory("8120 900311");
        gameBizQuery.difficultyPopularity();
        gameBizQuery.availableBoxes(1);
        gameBizQuery.topClients();
        gameBizQuery.sessionConflicts();
        gameBizQuery.gameAttractionStats();
        gameBizQuery.gameWinRate();
    }

    // ── Запустить всё ─────────────────────────────────────────────────────────

    private static void runAllDemo() throws SQLException {
        System.out.println("\n--- CRUD (Игры) ---");
        runAllCrud();
        System.out.println("\n--- Бизнес-запросы (Игры) ---");
        runAllBusinessQueries();
        System.out.println("\nГотово.");
    }
}
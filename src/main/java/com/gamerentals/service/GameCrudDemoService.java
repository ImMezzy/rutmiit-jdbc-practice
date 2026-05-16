package com.gamerentals.service;

import com.gamerentals.dao.*;
import com.gamerentals.model.*;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class GameCrudDemoService {

    private final ClientDao clientDao = new ClientDao();
    private final GameDao gameDao = new GameDao();
    private final BoxDao boxDao = new BoxDao();
    private final BoxRentDao boxRentDao = new BoxRentDao();
    private final GameAttractionDao gameAttractionDao = new GameAttractionDao();
    private final GameSessionDao gameSessionDao = new GameSessionDao();

    // CREATE

    public void demoCreate() throws SQLException {
        System.out.println("=== CREATE — Создание записей (игры) ===");

        // Создаём клиента (формат паспорта: "8120 900311")
        Client client = new Client("8120 999001", "Тестов", "Тестович", "Тестович");
        clientDao.insert(client);
        System.out.printf("Создан клиент: паспорт='%s', %s %s%n",
                client.getPassNumber(), client.getLastName(), client.getName());

        // Создаём игру
        Game game = new Game("Тестовая Игра", "Test Author", "Нормально", LocalTime.of(0, 45));
        int gameId = gameDao.insert(game);
        System.out.printf("Создана игра: id=%d, '%s', сложность=%s, время=%s%n",
                gameId, game.getName(), game.getDifficulty(), game.getAvgGameTime());

        // Создаём коробки для игры (явно задаём уникальные id в рамках game_id)
        Box box1 = new Box(1, gameId, 1.0f); // id=1
        Box box2 = new Box(2, gameId, 1.0f); // id=2
        boxDao.insert(box1);
        boxDao.insert(box2);
        System.out.printf("Добавлено 2 коробки (id=1,2) для игры '%s'%n", game.getName());

        OffsetDateTime rentStart = OffsetDateTime.of(2026, 4, 27, 14, 0, 0, 0, ZoneOffset.ofHours(3));
        OffsetDateTime rentEnd = rentStart.plusDays(2);
        // Аренда коробки (транзакционно)
// boxId теперь берётся явно из объекта box1 (равно 1)
        int rentId = boxRentDao.rentBox(
                client.getPassNumber(),
                box1.getId(),
                gameId,
                rentStart,
                rentEnd
        );
        System.out.printf("Аренда создана: id=%d, клиент=%s, коробка=%d, до=%s%n",
                rentId, client.getPassNumber(), box1.getId(), rentEnd);



        // Сессия в игротке
        int attractionId = gameAttractionDao.bookGameAttraction(
                client.getPassNumber(),
                gameId,
                rentStart,
                rentStart.plusMinutes(50)
        );
        System.out.printf("Запись в игротку: id=%d, игра=%s, 50 минут%n",
                attractionId, game.getName());

        // Игровая сессия с результатом
        int sessionId = gameSessionDao.recordGameSession(
                client.getPassNumber(),
                gameId,
                rentStart.plusMinutes(60),
                rentStart.plusMinutes(105),
                true // победа
        );
        System.out.printf("Игровая сессия: id=%d, результат=%s%n",
                sessionId, "победа");

        System.out.println();
    }

    // READ

    public void demoRead() throws SQLException {
        System.out.println("=== READ — Чтение данных (игры) ===");

        System.out.println("Все клиенты:");
        System.out.printf("%-12s %-18s %-18s %-18s%n", "Паспорт", "Фамилия", "Имя", "Отчество");
        for (Client c : clientDao.findAll()) {
            System.out.printf("%-12s %-18s %-18s %-18s%n",
                    c.getPassNumber(),
                    truncate(c.getLastName(), 17),
                    truncate(c.getName(), 17),
                    truncate(c.getPatronymic() != null ? c.getPatronymic() : "—", 17));
        }

        System.out.println("\nВсе игры:");
        System.out.printf("%-5s %-24s %-15s %-10s %-10s%n", "ID", "Название", "Автор", "Сложность", "Время");
        for (Game g : gameDao.findAll()) {
            System.out.printf("%-5d %-24s %-15s %-10s %-10s%n",
                    g.getId(),
                    truncate(g.getName(), 23),
                    truncate(g.getAuthors(), 14),
                    g.getDifficulty(),
                    g.getAvgGameTime());
        }

        System.out.println("\nКоробки (по играм):");
        System.out.printf("%-5s %-5s %-24s %-10s %-10s%n", "ID", "GameID", "Игра", "Статус", "Доступна");
        for (Box b : boxDao.findAll()) {
            String gameName = gameDao.findById(b.getGameId())
                    .map(Game::getName).orElse("?");
            String available = b.getStatus() > 0.5f ? "✓" :
                    b.getStatus() > 0f ? "~" : "✗";
            System.out.printf("%-5d %-5d %-24s %-10.2f %-10s%n",
                    b.getId(), b.getGameId(), truncate(gameName, 23),
                    b.getStatus(), available);
        }

        System.out.println("\nАктивные аренды:");
        System.out.printf("%-5s %-12s %-24s %-8s %-20s%n", "ID", "Клиент", "Игра", "Коробка", "Вернуть до");
        for (BoxRent br : boxRentDao.findAll()) {
            if (!br.getDateOfReturn().isAfter(OffsetDateTime.now())) {
                String gameName = gameDao.findById(br.getGameId())
                        .map(Game::getName).orElse("?");
                System.out.printf("%-5d %-12s %-24s %-8d %-20s%n",
                        br.getId(), br.getClientPassNumber(), truncate(gameName, 23),
                        br.getBoxId(), br.getDateOfReturn());
            }
        }

        System.out.println("\nПоиск клиента по паспорту '8120 900311':");
        clientDao.findByPass("8120 900311").ifPresentOrElse(
                c -> System.out.println(c),
                () -> System.out.println("Не найден")
        );

        System.out.println("\nПоиск игры по id=1:");
        gameDao.findById(1).ifPresentOrElse(
                g -> System.out.println(g),
                () -> System.out.println("Не найдена")
        );

        System.out.println();
    }

    // UPDATE

    public void demoUpdate() throws SQLException {
        System.out.println("=== UPDATE — Обновление данных (игры) ===");

        // Обновление клиента
        clientDao.findByPass("8120 900311").ifPresent(c -> {
            String oldName = c.getName();
            c.setName("Алексей-Обновлённый");
            try {
                boolean ok = clientDao.update(c);
                System.out.printf("Обновлён клиент: имя '%s' → '%s' (успех=%b)%n",
                        oldName, c.getName(), ok);
                // Возвращаем как было
                c.setName(oldName);
                clientDao.update(c);
            } catch (SQLException e) {
                System.out.println("Ошибка обновления клиента: " + e.getMessage());
            }
        });

        // Обновление игры
        gameDao.findById(1).ifPresent(g -> {
            String oldDifficulty = g.getDifficulty();
            g.setDifficulty("Сложно");
            try {
                boolean ok = gameDao.update(g);
                System.out.printf("Обновлена игра: сложность '%s' → '%s' (успех=%b)%n",
                        oldDifficulty, g.getDifficulty(), ok);
                // Возвращаем как было
                g.setDifficulty(oldDifficulty);
                gameDao.update(g);
            } catch (SQLException e) {
                System.out.println("Ошибка обнвления игры: " + e.getMessage());
            }
        });

        // Обновление статуса коробки
        boxDao.findAll().stream().filter(b -> b.getStatus() == 1.0f).findFirst().ifPresent(b -> {
            float oldStatus = b.getStatus();
            b.setStatus(0.0f); // занимаем
            try {
                boolean ok = boxDao.update(b);
                System.out.printf("Обновлена коробка: статус %.2f → %.2f (успех=%b)%n",
                        oldStatus, b.getStatus(), ok);
                // Возвращаем
                b.setStatus(oldStatus);
                boxDao.update(b);
            } catch (SQLException e) {
                System.out.println("Ошибка обновления коробки: " + e.getMessage());
            }
        });

        System.out.println();
    }

    // DELETE

    public void demoDelete() throws SQLException {
        System.out.println("=== DELETE — Удаление данных (игры) ===");

        // Создаём временного клиента
        Client temp = new Client("8120 999999", "Удали", "Меня", "Пожалуйста");
        clientDao.insert(temp);
        System.out.printf("Создан временный клиент: паспорт='%s'%n", temp.getPassNumber());

        boolean deleted = clientDao.delete(temp.getPassNumber());
        System.out.printf("Удалён клиент паспорт='%s' (успех=%b)%n",
                temp.getPassNumber(), deleted);

        boolean notFound = clientDao.delete("0000 000000");
        System.out.printf("Удаление несуществующего паспорта (успех=%b)%n", notFound);

        // Демонстрация каскадного удаления (если настроено в БД)
        System.out.println("\nПроверка внешних ключей:");
        System.out.println("При удалении игры с box_rent → ожидается ошибка (ON DELETE RESTRICT)");

        System.out.println();
    }

    // BATCH INSERT

    public void demoBatchInsert() throws SQLException {
        System.out.println("=== BATCH INSERT — Массовая вставка коробок ===");

        // Создаём тестовую игру
        Game testGame = new Game("Массовая Тестовая", "Batch Author", "Легко", LocalTime.of(0, 30));
        int gameId = gameDao.insert(testGame);
        System.out.printf("Создана игра для теста: id=%d, '%s'%n", gameId, testGame.getName());

        // Готовим 10 коробок
        List<Box> boxes = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            boxes.add(new Box(0, gameId, 1.0f)); // все доступны
        }

        long start = System.nanoTime();
        int count = boxDao.batchInsert(boxes);
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("Вставлено %d коробок за %d мс (batch)%n", count, elapsed);

        // Проверяем, что вставились
        long actualCount = boxDao.findAll().stream()
                .filter(b -> b.getGameId() == gameId).count();
        System.out.printf("Фактически в БД: %d коробок для игры #%d%n", actualCount, gameId);

        // Чистим за собой
        for (Box b : boxDao.findAll()) {
            if (b.getGameId() == gameId) {
                boxDao.delete(b.getId(), b.getGameId());
            }
        }
        gameDao.delete(gameId);
        System.out.printf("Тестовые данные удалены ✓%n");

        System.out.println();
    }

    // TRANSACTION

    public void demoTransaction() throws SQLException {
        System.out.println("=== TRANSACTION — Аренда коробки ===");

        String clientPass = "8120 900311";
        int gameId = 1; // Монополия

        System.out.printf("Попытка арендовать коробку: клиент=%s, игра=#%d%n",
                clientPass, gameId);

        // Находим свободную коробку
        Box availableBox = boxDao.findAll().stream()
                .filter(b -> b.getGameId() == gameId && b.getStatus() > 0.5f)
                .findFirst()
                .orElse(null);

        if (availableBox == null) {
            System.out.println("Нет доступных коробок для этой игры");
            System.out.println();
            return;
        }

        System.out.printf("Найдена свободная коробка: id=%d, статус=%.2f%n",
                availableBox.getId(), availableBox.getStatus());

        try {
            OffsetDateTime start = OffsetDateTime.now();
            OffsetDateTime end = start.plusDays(3);

            int rentId = boxRentDao.rentBox(
                    clientPass,
                    availableBox.getId(),
                    gameId,
                    start,
                    end
            );
            System.out.printf("Аренда успешна! id=%d, вернуть до: %s%n",
                    rentId, end);

            // Проверяем, что коробка теперь занята
            Box updated = boxDao.findAll().stream()
                    .filter(b -> b.getId() == availableBox.getId()
                            && b.getGameId() == gameId)
                    .findFirst().orElse(null);

            if (updated != null) {
                System.out.printf("Статус коробки после аренды: %.2f (было: %.2f)%n",
                        updated.getStatus(), availableBox.getStatus());
            }

            // Попытка повторной аренды той же коробки (должна упасть)
            System.out.println("\nПопытка повторно арендовать ту же коробку...");
            try {
                boxRentDao.rentBox(
                        "8120 900312", // другой клиент
                        availableBox.getId(),
                        gameId,
                        start,
                        end
                );
                System.out.println("Ошибка: аренда прошла, но не должна была!");
            } catch (SQLException e) {
                System.out.printf("Ожидаемая ошибка: %s%n",
                        truncate(e.getMessage(), 60));
            }

        } catch (SQLException e) {
            System.out.printf("Ошибка аренды: %s%n", e.getMessage());
        }

        System.out.println();
    }

    // Утилита

    public static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
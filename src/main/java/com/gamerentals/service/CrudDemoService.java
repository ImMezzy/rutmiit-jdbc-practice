package com.gamerentals.service;

import com.gamerentals.dao.*;
import com.gamerentals.model.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class CrudDemoService {

    private final VisitorDao visitorDao = new VisitorDao();
    private final HallDao hallDao = new HallDao();
    private final SeatDao seatDao = new SeatDao();
    private final FilmDao filmDao = new FilmDao();
    private final GenreDao genreDao = new GenreDao();
    private final ScreeningDao screeningDao = new ScreeningDao();
    private final TicketDao ticketDao = new TicketDao();

    // CREATE

    public void demoCreate() throws SQLException {
        System.out.println("=== CREATE — Создание записей ===");

        Visitor visitor = new Visitor("Тестов Тест", "+79991112233", "test@test.ru");
        int visitorId = visitorDao.insert(visitor);
        System.out.printf("Создан посетитель: id=%d, %s%n", visitorId, visitor.getName());

        Film film = new Film("Тестовый Фильм", LocalTime.of(1, 45));
        int filmId = filmDao.insert(film);
        System.out.printf("Создан фильм: id=%d, '%s', %s%n", filmId, film.getTitle(), film.getDuration());

        filmDao.addGenre(filmId, 1);
        filmDao.addGenre(filmId, 2);
        List<Genre> genres = filmDao.findGenresByFilm(filmId);
        System.out.printf("Привязаны жанры к фильму: %s%n",
                genres.stream().map(Genre::getName).toList());

        Screening screening = new Screening(0, (short) 1, filmId,
                OffsetDateTime.of(2026, 4, 27, 20, 0, 0, 0, ZoneOffset.ofHours(3)),
                BigDecimal.valueOf(500));
        int screeningId = screeningDao.insert(screening);
        System.out.printf("Создан сеанс: id=%d, зал=1, %s, цена=500₽%n",
                screeningId, screening.getDateTime());

        System.out.println();
    }

    // READ

    public void demoRead() throws SQLException {
        System.out.println("=== READ — Чтение данных ===");

        System.out.println("Все посетители:");
        System.out.printf("%-5s %-20s %-16s %-25s%n", "ID", "Имя", "Телефон", "Email");
        for (Visitor v : visitorDao.findAll()) {
            System.out.printf("%-5d %-20s %-16s %-25s%n",
                    v.getId(), v.getName(), v.getPhone(), v.getEmail());
        }

        System.out.println("\nВсе фильмы:");
        System.out.printf("%-5s %-22s %-14s %-30s%n", "ID", "Название", "Длительность", "Жанры");
        for (Film f : filmDao.findAll()) {
            String genres;
            try {
                genres = filmDao.findGenresByFilm(f.getId()).stream()
                        .map(Genre::getName).toList().toString();
            } catch (SQLException e) {
                genres = "?";
            }
            System.out.printf("%-5d %-22s %-14s %-30s%n",
                    f.getId(), truncate(f.getTitle(), 21), f.getDuration(), truncate(genres, 29));
        }

        System.out.println("\nВсе залы:");
        System.out.printf("%-5s %-16s %-13s %-10s%n", "№", "Название", "Вместимость", "Тип");
        for (Hall h : hallDao.findAll()) {
            System.out.printf("%-5d %-16s %-13d %-10s%n",
                    h.getId(), h.getName(), h.getCapacity(), h.getTypeName());
        }

        System.out.println("\nПоиск посетителя по id=1:");
        visitorDao.findById(1).ifPresentOrElse(
                v -> System.out.println(v),
                () -> System.out.println("Не найден")
        );

        System.out.println("\nПоиск места: место=5, зал=1:");
        seatDao.findByKey((short) 5, (short) 1).ifPresentOrElse(
                s -> System.out.println(s),
                () -> System.out.println("Не найдено")
        );

        System.out.println();
    }

    // UPDATE

    public void demoUpdate() throws SQLException {
        System.out.println("=== UPDATE — Обновление данных ===");

        visitorDao.findById(1).ifPresent(v -> {
            String oldEmail = v.getEmail();
            v.setEmail("updated@mail.ru");
            try {
                boolean ok = visitorDao.update(v);
                System.out.printf("Обновлён email посетителя id=1: '%s' → '%s' (успех=%b)%n",
                        oldEmail, v.getEmail(), ok);
            } catch (SQLException e) {
                System.out.println("Ошибка обновления: " + e.getMessage());
            }
        });

        filmDao.findById(1).ifPresent(f -> {
            String oldTitle = f.getTitle();
            f.setTitle("Интерстеллар: Режиссёрская");
            try {
                boolean ok = filmDao.update(f);
                System.out.printf("Обновлён фильм id=1: '%s' → '%s' (успех=%b)%n",
                        oldTitle, f.getTitle(), ok);
                f.setTitle(oldTitle);
                filmDao.update(f);
            } catch (SQLException e) {
                System.out.println("Ошибка обновления: " + e.getMessage());
            }
        });

        System.out.println();
    }

    // DELETE

    public void demoDelete() throws SQLException {
        System.out.println("=== DELETE — Удаление данных ===");

        Visitor temp = new Visitor("Удали Меня", "+70000000000", "delete@me.ru");
        int tempId = visitorDao.insert(temp);
        System.out.printf("Создан временный посетитель id=%d%n", tempId);

        boolean deleted = visitorDao.delete(tempId);
        System.out.printf("Удалён посетитель id=%d (успех=%b)%n", tempId, deleted);

        boolean notFound = visitorDao.delete(99999);
        System.out.printf("Удаление несуществующего id=99999 (успех=%b)%n", notFound);

        System.out.println();
    }

    // BATCH INSERT

    public void demoBatchInsert() throws SQLException {
        System.out.println("=== BATCH INSERT — Массовая вставка ===");

        Hall newHall = new Hall((short) 0, "Тестовый зал", (short) 20, (short) 1);
        short hallId = hallDao.insert(newHall);
        System.out.printf("Создан зал: id=%d, '%s'%n", hallId, newHall.getName());

        List<Seat> seats = new java.util.ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            seats.add(new Seat((short) i, hallId, (short) ((i - 1) / 5 + 1),
                    (short) 1, BigDecimal.valueOf(1.00)));
        }

        long start = System.nanoTime();
        int count = seatDao.batchInsert(seats);
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("Вставлено %d мест за %d мс (batch)%n", count, elapsed);

        hallDao.delete(hallId);
        System.out.printf("Зал id=%d удалён (CASCADE удалил места)%n", hallId);

        System.out.println();
    }

    // TRANSACTION

    public void demoTransaction() throws SQLException {
        System.out.println("=== TRANSACTION — Покупка билета ===");
        System.out.println("Попытка купить билет: посетитель=1, сеанс=1, место=1, зал=1");

        try {
            int ticketId = ticketDao.purchaseTicket(1, 1, (short) 1, (short) 1);
            System.out.printf("Билет куплен! id=%d%n", ticketId);

            System.out.println("Повторная покупка того же места...");
            try {
                ticketDao.purchaseTicket(2, 1, (short) 1, (short) 1);
            } catch (SQLException e) {
                System.out.printf("Ожидаемая ошибка: %s%n", e.getMessage());
            }

            ticketDao.delete(ticketId);
        } catch (SQLException e) {
            System.out.printf("Место уже занято: %s%n", e.getMessage());
        }

        System.out.println();
    }

    // Утилита

    public static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
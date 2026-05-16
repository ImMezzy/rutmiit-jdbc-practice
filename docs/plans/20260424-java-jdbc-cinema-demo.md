# Java JDBC Cinema Demo — ЗАВЕРШЕНО ✅

## Результат

Все 9 задач выполнены, проект компилируется (`BUILD SUCCESS`, 19 файлов).

## Созданные файлы (22 файла)

### Конфигурация
- [x] `JDBC/pom.xml` — Maven: PostgreSQL 42.7.5, HikariCP 6.2.1, Logback
- [x] `JDBC/src/main/resources/application.properties` — параметры БД
- [x] `JDBC/src/main/resources/logback.xml` — настройка логирования
- [x] `JDBC/src/main/resources/schema.sql` — DDL 8 таблиц + индексы

### Инфраструктура БД
- [x] `ConnectionManager.java` — HikariCP connection pool (lazy singleton)
- [x] `SchemaInitializer.java` — DDL + seed 10 фильмов, 8 посетителей, 4 зала, 15 сеансов, 25 билетов

### Модели (7 POJO)
- [x] `Visitor.java`, `Hall.java`, `Seat.java`, `Film.java`, `Genre.java`, `Screening.java`, `Ticket.java`

### DAO (7 классов CRUD)
- [x] `VisitorDao.java` — PreparedStatement, RETURN_GENERATED_KEYS
- [x] `HallDao.java` — полный CRUD
- [x] `SeatDao.java` — составной ключ, batch insert
- [x] `FilmDao.java` — CRUD + M:N связь с жанрами (ON CONFLICT DO NOTHING)
- [x] `GenreDao.java` — полный CRUD
- [x] `ScreeningDao.java` — MONEY тип, TIMESTAMPTZ, фильтрация
- [x] `TicketDao.java` — транзакционная покупка билета

### Сервисы
- [x] `CrudDemoService.java` — демо Create/Read/Update/Delete/Batch/Transaction
- [x] `BusinessQueryService.java` — 10 бизнес-запросов

### Точка входа + документация
- [x] `Main.java` — интерактивное меню с разделами
- [x] `README.md` — документация проекта

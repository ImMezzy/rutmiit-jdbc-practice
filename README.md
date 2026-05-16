# JDBC Cinema Demo — Демонстрация JDBC + PostgreSQL

Консольное приложение на **Java 21** + **JDBC** + **PostgreSQL**, демонстрирующее работу с реляционной БД на примере системы кинотеатра.

## Требования

- [Java 21 JDK](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [PostgreSQL 17](https://www.postgresql.org/download/) (локально или Docker)

### Быстрый запуск PostgreSQL через Docker

```bash
docker run -d --name postgres-cinema \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:17
```

Создайте базу данных:

```bash
docker exec -it postgres-cinema psql -U postgres -c "CREATE DATABASE cinema;"
```

## Как запустить

1. **Настройте** подключение в `src/main/resources/application.properties`:

   ```properties
   db.url=jdbc:postgresql://localhost:5432/cinema
   db.username=postgres
   db.password=postgres
   ```

2. **Запустите**:
   ```bash
   mvn clean compile exec:java
   ```

Приложение автоматически создаст таблицы и заполнит тестовыми данными.

## Структура проекта

```
JDBC/
├── pom.xml                              # Maven: PostgreSQL, HikariCP, SLF4J
├── src/main/
│   ├── java/com/cinema/
│   │   ├── Main.java                    # Точка входа + интерактивное меню
│   │   ├── db/
│   │   │   ├── ConnectionManager.java   # HikariCP connection pool
│   │   │   └── SchemaInitializer.java   # DDL + тестовые данные
│   │   ├── model/                       # POJO-модели (7 классов)
│   │   ├── dao/                         # DAO — CRUD (7 классов)
│   │   └── service/
│   │       ├── CrudDemoService.java     # Демо CRUD-операций
│   │       └── BusinessQueryService.java # 10 бизнес-запросов
│   └── resources/
│       ├── application.properties       # Конфигурация БД
│       ├── logback.xml                  # Логирование
│       └── schema.sql                   # DDL-скрипт
└── README.md
```

## Схема БД (8 таблиц)

| Таблица         | PK                          | Описание                           |
| --------------- | --------------------------- | ---------------------------------- |
| **Посетитель**  | `id_посетителя` (SERIAL)    | Посетитель кинотеатра              |
| **Зал**         | `номер_зала` (SMALLSERIAL)  | Кинозал (Стандарт/VIP/IMAX)        |
| **Место**       | `(номер_места, номер_зала)` | Место в зале (составной PK)        |
| **Фильм**       | `id_фильма` (SERIAL)        | Фильм                              |
| **Жанр**        | `id_жанра` (SERIAL)         | Жанр                               |
| **Жанр_фильма** | `(id_жанра, id_фильма)`     | M:N связь фильм↔жанр               |
| **Сеанс**       | `id_сеанса` (SERIAL)        | Сеанс (зал + фильм + время + цена) |
| **Билет**       | `id_билета` (SERIAL)        | Билет (посетитель + сеанс + место) |

## Демонстрируемые техники JDBC

| Техника                             | Где                          | Описание                                            |
| ----------------------------------- | ---------------------------- | --------------------------------------------------- |
| **PreparedStatement**               | Все DAO                      | Параметризованные запросы (защита от SQL-injection) |
| **Statement.RETURN_GENERATED_KEYS** | DAO insert                   | Получение сгенерированных ключей                    |
| **Batch Insert**                    | `SeatDao.batchInsert()`      | Массовая вставка через `addBatch/executeBatch`      |
| **Транзакции**                      | `TicketDao.purchaseTicket()` | `setAutoCommit(false)` + `commit/rollback`          |
| **Connection Pool**                 | `ConnectionManager`          | HikariCP — пул соединений                           |
| **ResultSet → POJO**                | Все DAO                      | Маппинг результатов в объекты                       |
| **Составной ключ**                  | `SeatDao`                    | Работа с `(номер_места, номер_зала)`                |
| **MONEY тип**                       | `ScreeningDao`               | Работа с PostgreSQL `MONEY`                         |
| **TIMESTAMPTZ**                     | `ScreeningDao`, `TicketDao`  | Работа с временными зонами                          |
| **ON CONFLICT DO NOTHING**          | `FilmDao.addGenre()`         | Upsert-паттерн                                      |
| **Text blocks**                     | `BusinessQueryService`       | Java 21 многострочные строки                        |
| **Pattern matching switch**         | Модели                       | `switch` expressions                                |

## 10 Бизнес-запросов

| #   | Запрос                       | SQL-техники                           |
| --- | ---------------------------- | ------------------------------------- |
| 1   | **Выручка по фильмам**       | JOIN (4 таблицы), SUM, GROUP BY       |
| 2   | **Заполняемость залов**      | LEFT JOIN, COUNT, процентный расчёт   |
| 3   | **Топ-5 популярных фильмов** | COUNT, ORDER BY DESC, LIMIT           |
| 4   | **Расписание на дату**       | PreparedStatement, TO_CHAR, DATE cast |
| 5   | **История посещений**        | JOIN (6 таблиц), параметризованный    |
| 6   | **Популярность жанров**      | JOIN через 4 таблицы, COUNT DISTINCT  |
| 7   | **Свободные места**          | LEFT JOIN + IS NULL (anti-join)       |
| 8   | **Средний чек по залам**     | AVG, MIN, MAX, ROUND                  |
| 9   | **Активные посетители**      | HAVING, BETWEEN, агрегация            |
| 10  | **Пересечение сеансов**      | Self-join, интервальное сравнение     |

## Зависимости

| Пакет             | Версия | Назначение             |
| ----------------- | ------ | ---------------------- |
| `postgresql`      | 42.7.5 | PostgreSQL JDBC Driver |
| `HikariCP`        | 6.2.1  | Connection Pool        |
| `slf4j-api`       | 2.0.16 | Logging API            |
| `logback-classic` | 1.5.15 | Logging Implementation |

# JDBC Board Games Rental — Демонстрация JDBC + PostgreSQL

Консольное приложение на **Java 21** + **JDBC** + **PostgreSQL**, демонстрирующее работу с реляционной БД на примере системы аренды настольных игр.

## Требования

- [Java 21 JDK](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [PostgreSQL 17](https://www.postgresql.org/download/) (локально или Docker)

### Быстрый запуск PostgreSQL через Docker

```bash
docker run --name postgres-games \
  -e POSTGRES_PASSWORD=mysecretpassword \
  -p 5432:5432 \
  -d postgres:17
```

Создайте базу данных:

```bash
docker exec -it postgres-games psql -U postgres -c "CREATE DATABASE game_rentals;"
```

## Как запустить

1. **Настройте** подключение в `src/main/resources/application.properties`:

   ```properties
   db.url=jdbc:postgresql://localhost/game_rentals
   db.username=postgres
   db.password=mysecretpassword
   ```

2. **Запустите**:
   ```bash
   mvn clean compile exec:java -Dexec.mainClass=com.gamerentals.MainGames
   ```

Приложение автоматически создаст таблицы и заполнит тестовыми данными.

## Структура проекта

```
boardgames-demo/
├── pom.xml                              # Maven: PostgreSQL, HikariCP, SLF4J
├── src/main/
│   ├── java/com/cinema/
│   │   ├── MainGames.java                    # Точка входа + интерактивное меню
│   │   ├── db/
│   │   │   ├── ConnectionManager.java   # HikariCP connection pool
│   │   │   └── SchemaInitializerGames.java   # DDL + тестовые данные
│   │   ├── model/                       # POJO-модели (6 классов)
│   │   │   ├── Game.java
│   │   │   ├── Client.java
│   │   │   ├── Box.java
│   │   │   ├── BoxRent.java
│   │   │   ├── GameAttraction.java
│   │   │   └── GameSession.java
│   │   ├── dao/                         # DAO — CRUD (6 классов)
│   │   │   ├── GameDao.java
│   │   │   ├── ClientDao.java
│   │   │   ├── BoxDao.java
│   │   │   ├── BoxRentDao.java
│   │   │   ├── GameAttractionDao.java
│   │   │   └── GameSessionDao.java
│   │   └── service/
│   │       ├── GameCrudDemoService.java     # Демо CRUD-операций
│   │       └── GameBusinessQueryService.java # 12 бизнес-запросов
│   └── resources/
│       ├── application.properties       # Конфигурация БД
│       ├── logback.xml                  # Логирование
│       └── schema_games.sql             # DDL-скрипт для игр
└── README.md
```

## Схема БД (6 таблиц)

| Таблица           | Первичный ключ              | Описание                                      |
| ----------------- | --------------------------- | --------------------------------------------- |
| **games**         | `id` (GENERATED)            | Настольная игра (название, авторы, сложность) |
| **clients**       | `pass_number` (VARCHAR(11)) | Клиент (формат: `"8120 900311"`)              |
| **boxes**         | `(id, game_id)`             | Коробка игры (составной PK, статус)           |
| **box_rent**      | `id` (GENERATED)            | Аренда коробки (клиент, даты, штраф)          |
| **game_attraction** | `id` (GENERATED)          | Игротека: сессия игры в зале (без результата) |
| **game_session**  | `id` (GENERATED)            | Игровая сессия с результатом (победа/поражение) |

### Особенности схемы

- **Составной ключ `boxes(id, game_id)`**: позволяет нумеровать коробки локально внутри каждой игры (коробка `#1` у «Монополии» и «Уно» — разные записи).
- **Формат паспорта**: `pass_number ~ '^\d{4} \d{6}$'` — проверка на уровне БД.
- **Внешние ключи с `ON DELETE CASCADE`**: удаление игры автоматически удаляет связанные коробки, аренды и сессии.
- **CHECK-ограничения**: валидация сложности (`'Легко'`, `'Нормально'`, `'Сложно'`), статуса коробки (`0.00..1.00`), временных интервалов.

## Демонстрируемые техники JDBC

| Техника                             | Где                          | Описание                                            |
| ----------------------------------- | ---------------------------- | --------------------------------------------------- |
| **PreparedStatement**               | Все DAO                      | Параметризованные запросы (защита от SQL-injection) |
| **Batch Insert**                    | `BoxDao.batchInsert()`       | Массовая вставка коробок через `addBatch/executeBatch` |
| **Транзакции**                      | `BoxRentDao.rentBox()`       | `setAutoCommit(false)` + `commit/rollback` при аренде |
| **Connection Pool**                 | `ConnectionManager`          | HikariCP — пул соединений                           |
| **ResultSet → POJO**                | Все DAO                      | Маппинг результатов в объекты                       |
| **Составной ключ**                  | `BoxDao`                     | Работа с `(id, game_id)` в WHERE/INSERT             |
| **TIMESTAMPTZ**                     | Все сервисы                  | Работа с временными зонами (`OffsetDateTime`)       |
| **Interval arithmetic**             | `GameBusinessQueryService`   | Расчёт длительности аренды в PostgreSQL             |
| **Text blocks**                     | `GameBusinessQueryService`   | Java 21 многострочные строки для SQL                |
| **Pattern matching switch**         | Модели, сервисы              | `switch` expressions для форматирования             |
| **Check overlap query**             | `GameAttractionDao`, `GameSessionDao` | Предотвращение пересекающихся сессий          |
| **Generated keys**                  | `GameDao`, `BoxRentDao`      | Получение `id` после `INSERT`                       |

## 12 Бизнес-запросов

| #   | Запрос                          | SQL-техники                                      |
| --- | ------------------------------- | ------------------------------------------------ |
| 1   | **Выручка по играм**            | JOIN (3 таблицы), SUM(fine), AVG(длительность)   |
| 2   | **Загруженность коробок**       | CASE, COUNT DISTINCT, процентный расчёт          |
| 3   | **Топ-5 популярных игр**        | COUNT, GROUP BY, ORDER BY DESC, LIMIT            |
| 4   | **Активные аренды на дату**     | PreparedStatement, диапазон дат, JOIN clients    |
| 5   | **История аренд клиента**       | Параметризованный запрос, форматирование дат     |
| 6   | **Популярность по сложности**   | GROUP BY difficulty, агрегация по играм/арендам  |
| 7   | **Доступные коробки для игры**  | LEFT JOIN + IS NULL для поиска свободных         |
| 8   | **Средняя длительность аренды** | EXTRACT(EPOCH FROM ...), MIN/MAX интервалов      |
| 9   | **Топ-активные клиенты**        | HAVING COUNT >= 2, агрегация по паспорту         |
| 10  | **Конфликты игровых сессий**    | Self-join с проверкой пересечения интервалов     |
| 11  | **Статистика игротки**          | Вечерние сессии, средняя длительность, уникальные клиенты |
| 12  | **Win-rate по играм**           | CASE WHEN для подсчёта побед, процентный расчёт  |

### Пример: проверка пересечения сессий
```sql
-- Находит пересекающиеся сессии одного клиента для одной игры
SELECT COUNT(*) FROM game_session 
WHERE client_pass_number = ? 
  AND game_id = ? 
  AND start_time < ?    -- новая сессия начинается до конца старой
  AND end_time > ?      -- новая сессия заканчивается после начала старой
```


## Зависимости

| Пакет             | Версия | Назначение             |
| ----------------- | ------ | ---------------------- |
| `postgresql`      | 42.7.5 | PostgreSQL JDBC Driver |
| `HikariCP`        | 6.2.1  | Connection Pool        |

## Тестовые данные

При инициализации создаются:

| Сущность   | Количество | Примеры                          |
| ---------- | ---------- | -------------------------------- |
| **games**  | 10         | Монополия, Уно, Пандемия, Диксит |
| **clients**| 10         | Паспорта `8120 900311` … `900320`|
| **boxes**  | 24         | 2-4 коробки на игру              |
| **box_rent**| 12        | Активные + завершённые аренды    |
| **game_attraction** | 10 | Сессии в игротке                |
| **game_session** | 25+  | Сессии с результатом (победа/поражение) |

### Сложность игр
- **Легко**: Монополия, Уно, Диксит, Тик-так-бумм, Имаджинариум
- **Нормально**: Каркассон, Колонизаторы, Экивоки
- **Сложно**: Пандемия, 7 Чудес
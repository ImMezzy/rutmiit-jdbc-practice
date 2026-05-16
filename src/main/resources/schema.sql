-- ============================================================
-- Схема БД: Система кинотеатра
-- PostgreSQL 17
-- ============================================================

-- Удаляем таблицы в порядке зависимостей (если существуют)
DROP TABLE IF EXISTS Билет CASCADE;
DROP TABLE IF EXISTS Сеанс CASCADE;
DROP TABLE IF EXISTS Жанр_фильма CASCADE;
DROP TABLE IF EXISTS Место CASCADE;
DROP TABLE IF EXISTS Зал CASCADE;
DROP TABLE IF EXISTS Фильм CASCADE;
DROP TABLE IF EXISTS Жанр CASCADE;
DROP TABLE IF EXISTS Посетитель CASCADE;

-- ============================================================
-- 1. Посетитель
-- ============================================================
CREATE TABLE Посетитель (
    id_посетителя   SERIAL        PRIMARY KEY,
    имя             VARCHAR(32)   NOT NULL,
    номер_телефона  VARCHAR(16),
    email           VARCHAR(32)
);

-- ============================================================
-- 2. Жанр
-- ============================================================
CREATE TABLE Жанр (
    id_жанра   SERIAL       PRIMARY KEY,
    название   VARCHAR(24)  NOT NULL UNIQUE
);

-- ============================================================
-- 3. Фильм
-- ============================================================
CREATE TABLE Фильм (
    id_фильма        SERIAL       PRIMARY KEY,
    название         VARCHAR(32)  NOT NULL,
    продолжительность TIME        NOT NULL
);

-- ============================================================
-- 4. Жанр_фильма (M:N связь)
-- ============================================================
CREATE TABLE Жанр_фильма (
    id_жанра   INT  NOT NULL REFERENCES Жанр(id_жанра) ON DELETE CASCADE,
    id_фильма  INT  NOT NULL REFERENCES Фильм(id_фильма) ON DELETE CASCADE,
    PRIMARY KEY (id_жанра, id_фильма)
);

-- ============================================================
-- 5. Зал
-- ============================================================
CREATE TABLE Зал (
    номер_зала   SMALLSERIAL   PRIMARY KEY,
    название     VARCHAR(24)   NOT NULL,
    вместимость  SMALLINT      NOT NULL CHECK (вместимость > 0),
    тип_зала     SMALLINT      NOT NULL DEFAULT 1
);

-- ============================================================
-- 6. Место (составной PK)
-- ============================================================
CREATE TABLE Место (
    номер_места       SMALLINT      NOT NULL,
    номер_зала        SMALLINT      NOT NULL REFERENCES Зал(номер_зала) ON DELETE CASCADE,
    ряд               SMALLINT      NOT NULL CHECK (ряд > 0),
    тип_места         SMALLINT      NOT NULL DEFAULT 1,
    коэффициент_цены  NUMERIC(4,2)  NOT NULL DEFAULT 1.00 CHECK (коэффициент_цены > 0),
    PRIMARY KEY (номер_места, номер_зала)
);

-- ============================================================
-- 7. Сеанс
-- ============================================================
CREATE TABLE Сеанс (
    id_сеанса          SERIAL       PRIMARY KEY,
    номер_зала         SMALLINT     NOT NULL REFERENCES Зал(номер_зала),
    id_фильма          INT          NOT NULL REFERENCES Фильм(id_фильма),
    дата_и_время       TIMESTAMPTZ  NOT NULL,
    базовая_стоимость  MONEY        NOT NULL CHECK (базовая_стоимость::numeric >= 0)
);

-- ============================================================
-- 8. Билет
-- ============================================================
CREATE TABLE Билет (
    id_билета       SERIAL       PRIMARY KEY,
    id_посетителя   INT          NOT NULL REFERENCES Посетитель(id_посетителя),
    id_сеанса       INT          NOT NULL REFERENCES Сеанс(id_сеанса),
    номер_места     SMALLINT     NOT NULL,
    номер_зала      SMALLINT     NOT NULL,
    дата_и_время    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    FOREIGN KEY (номер_места, номер_зала) REFERENCES Место(номер_места, номер_зала),
    UNIQUE (id_сеанса, номер_места, номер_зала)  -- нельзя продать одно место дважды на сеанс
);

-- ============================================================
-- Индексы для производительности
-- ============================================================
CREATE INDEX idx_билет_посетитель ON Билет(id_посетителя);
CREATE INDEX idx_билет_сеанс ON Билет(id_сеанса);
CREATE INDEX idx_сеанс_дата ON Сеанс(дата_и_время);
CREATE INDEX idx_сеанс_фильм ON Сеанс(id_фильма);
CREATE INDEX idx_сеанс_зал ON Сеанс(номер_зала);

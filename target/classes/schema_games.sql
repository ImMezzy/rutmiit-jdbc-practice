-- resources/schema_games.sql
-- Схема базы данных для настольных игр

DROP TABLE IF EXISTS game_session CASCADE;
DROP TABLE IF EXISTS game_attraction CASCADE;
DROP TABLE IF EXISTS box_rent CASCADE;
DROP TABLE IF EXISTS boxes CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS games CASCADE;

CREATE TABLE games (
                       id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                       name VARCHAR(100) NOT NULL,
                       authors VARCHAR(100) NOT NULL,
                       difficulty VARCHAR(20) NOT NULL,
                       avg_game_time TIME NOT NULL,

                       CONSTRAINT games_check_name_not_empty CHECK (name IS NOT NULL AND TRIM(name) <> ''),
                       CONSTRAINT games_check_authors_not_empty CHECK (authors IS NOT NULL AND TRIM(authors) <> ''),
                       CONSTRAINT games_check_difficulty_not_empty CHECK (difficulty IS NOT NULL AND TRIM(difficulty) <> ''),
                       CONSTRAINT games_check_difficulty_value CHECK (difficulty IN ('Легко', 'Нормально', 'Сложно'))
);

CREATE TABLE clients (
                         pass_number VARCHAR(11) PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         last_name VARCHAR(100) NOT NULL,
                         patronymic VARCHAR(100) DEFAULT NULL,

                         CONSTRAINT clients_check_name_not_empty CHECK (name IS NOT NULL AND TRIM(name) <> ''),
                         CONSTRAINT clients_check_last_name_not_empty CHECK (last_name IS NOT NULL AND TRIM(last_name) <> ''),
                         CONSTRAINT clients_check_pass_format CHECK (pass_number ~ '^\d{4} \d{6}$')
    );

CREATE TABLE boxes (
                       id INT NOT NULL,
                       game_id INT NOT NULL,
                       status DECIMAL(3,2) NOT NULL,
                       PRIMARY KEY(id, game_id),

                       CONSTRAINT boxes_check_status_positive CHECK (status >= 0.00 AND status <= 1.00),
                       CONSTRAINT boxes_game_id_fk FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
);

CREATE TABLE box_rent (
                          id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                          box_id INT NOT NULL,
                          game_id INT NOT NULL,
                          client_pass_number VARCHAR(11) NOT NULL,
                          date_of_rent TIMESTAMPTZ NOT NULL,
                          date_of_return TIMESTAMPTZ NOT NULL,
                          fine INT DEFAULT 0,

                          CONSTRAINT box_rent_check_fine_positive CHECK (fine >= 0),
                          CONSTRAINT box_rent_check_dates CHECK (date_of_return > date_of_rent),
                          CONSTRAINT box_id_fk FOREIGN KEY (box_id, game_id) REFERENCES boxes(id, game_id) ON DELETE CASCADE,
                          CONSTRAINT client_pass_number_fk FOREIGN KEY (client_pass_number) REFERENCES clients(pass_number) ON DELETE CASCADE
);

CREATE TABLE game_attraction (
                                 id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                 game_id INT NOT NULL,
                                 client_pass_number VARCHAR(11) NOT NULL,
                                 start_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 end_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT game_attraction_check_time CHECK (end_time > start_time),
                                 CONSTRAINT game_attraction_game_id_fk FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
                                 CONSTRAINT game_attraction_client_fk FOREIGN KEY (client_pass_number) REFERENCES clients(pass_number) ON DELETE CASCADE
);

CREATE TABLE game_session (
                              id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                              game_id INT NOT NULL,
                              client_pass_number VARCHAR(11) NOT NULL,
                              start_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              end_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              game_result BOOLEAN NOT NULL,

                              CONSTRAINT game_session_check_time CHECK (end_time > start_time),
                              CONSTRAINT game_session_game_id_fk FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
                              CONSTRAINT game_session_client_fk FOREIGN KEY (client_pass_number) REFERENCES clients(pass_number) ON DELETE CASCADE
);

-- Индексы для производительности
CREATE INDEX idx_boxes_game ON boxes(game_id, status);
CREATE INDEX idx_box_rent_client ON box_rent(client_pass_number, date_of_rent);
CREATE INDEX idx_box_rent_game ON box_rent(game_id, date_of_return);
CREATE INDEX idx_box_rent_active ON box_rent(box_id, game_id, date_of_return) WHERE date_of_return IS NULL;
CREATE INDEX idx_game_attraction_time ON game_attraction(game_id, start_time);
CREATE INDEX idx_game_session_result ON game_session(game_id, game_result);
CREATE INDEX idx_game_session_client ON game_session(client_pass_number, start_time);
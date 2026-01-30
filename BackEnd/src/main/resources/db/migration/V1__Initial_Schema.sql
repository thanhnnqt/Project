-- Create Game Types table
CREATE TABLE IF NOT EXISTS game_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    min_players INT,
    max_players INT,
    rules_json TEXT,
    icon VARCHAR(255)
);

-- Create Players table
CREATE TABLE IF NOT EXISTS players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    avatar VARCHAR(255),
    balance DECIMAL(19, 2) DEFAULT 0.00,
    status VARCHAR(50),
    last_login DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create Rooms table
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    game_type_id BIGINT,
    min_bet DECIMAL(19, 2),
    status VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    host_id BIGINT,
    CONSTRAINT fk_room_game_type FOREIGN KEY (game_type_id) REFERENCES game_types(id),
    CONSTRAINT fk_room_host FOREIGN KEY (host_id) REFERENCES players(id)
);

-- Create Room Participants table
CREATE TABLE IF NOT EXISTS room_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT,
    player_id BIGINT,
    seat_index INT,
    is_ready BOOLEAN DEFAULT FALSE,
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_participant_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_participant_player FOREIGN KEY (player_id) REFERENCES players(id)
);

-- Create Match History table
CREATE TABLE IF NOT EXISTS match_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT,
    game_type_id BIGINT,
    start_time DATETIME,
    end_time DATETIME,
    match_log TEXT,
    CONSTRAINT fk_match_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_match_game_type FOREIGN KEY (game_type_id) REFERENCES game_types(id)
);

-- Create Match Details table
CREATE TABLE IF NOT EXISTS match_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_history_id BIGINT,
    player_id BIGINT,
    `rank` INT,
    coin_delta DECIMAL(19, 2),
    end_hand TEXT,
    CONSTRAINT fk_detail_match FOREIGN KEY (match_history_id) REFERENCES match_history(id),
    CONSTRAINT fk_detail_player FOREIGN KEY (player_id) REFERENCES players(id)
);

-- Insert Game Types
INSERT INTO game_types (name, min_players, max_players, rules_json, icon) VALUES
('Tiến Lên Miền Nam', 2, 4, '{"cards": 52, "type": "climbing"}', 'cards-icon'),
('Poker (Texas Hold''em)', 2, 9, '{"cards": 52, "type": "poker"}', 'poker-icon'),
('Phỏm (Tá Lả)', 2, 4, '{"cards": 52, "type": "rummy"}', 'phom-icon');

-- Insert Sample Players
INSERT INTO players (username, password, display_name, balance, status) VALUES
('admin', '$2a$10$r8O.FkC.K9x7K8i/8gXv8u', 'Hệ Thống', 1000000.00, 'ONLINE'),
('player1', '$2a$10$r8O.FkC.K9x7K8i/8gXv8u', 'Đại Gia Thẻ Bài', 50000.00, 'OFFLINE'),
('player2', '$2a$10$r8O.FkC.K9x7K8i/8gXv8u', 'Thần Bài Miền Tây', 75000.00, 'PLAYING');

-- Insert Sample Rooms
INSERT INTO rooms (name, game_type_id, min_bet, status, host_id) VALUES
('Sảnh Rồng Chiến', 1, 1000.00, 'WAITING', 2),
('Poker VIP 1', 2, 5000.00, 'PLAYING', 3),
('Phỏm Bình Dân', 3, 500.00, 'WAITING', 1);

-- V18: Add Mậu Binh Game Type
INSERT INTO game_types (name, min_players, max_players, rules_json, icon)
VALUES ('Mậu Binh', 2, 4, '{"cards": 52, "type": "climbing"}', 'binh-icon');

-- Optional: Add a sample room for Mậu Binh
INSERT INTO rooms (name, game_type_id, min_bet, status, host_id)
SELECT 'Mậu Binh Sảnh VIP', id, 2000.00, 'WAITING', 1
FROM game_types WHERE name = 'Mậu Binh'
LIMIT 1;

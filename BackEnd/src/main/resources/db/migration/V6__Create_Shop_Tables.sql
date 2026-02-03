-- Create shop_items table
CREATE TABLE IF NOT EXISTS shop_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    image_url VARCHAR(500),
    description TEXT,
    rarity VARCHAR(50)
);

-- Create player_inventory table
CREATE TABLE IF NOT EXISTS player_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    shop_item_id BIGINT NOT NULL,
    purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_equipped BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    FOREIGN KEY (shop_item_id) REFERENCES shop_items(id) ON DELETE CASCADE,
    UNIQUE KEY unique_player_item (player_id, shop_item_id)
);

-- Insert sample avatar frames
INSERT INTO shop_items (name, type, price, image_url, description, rarity) VALUES
('Khung Vàng Cổ Điển', 'AVATAR_FRAME', 500.00, '🖼️', 'Khung avatar màu vàng sang trọng', 'COMMON'),
('Khung Bạc Lấp Lánh', 'AVATAR_FRAME', 800.00, '✨', 'Khung avatar bạc lấp lánh', 'RARE'),
('Khung Kim Cương', 'AVATAR_FRAME', 2000.00, '💎', 'Khung avatar kim cương cao cấp', 'EPIC'),
('Khung Huyền Thoại', 'AVATAR_FRAME', 5000.00, '👑', 'Khung avatar huyền thoại độc nhất', 'LEGENDARY'),

-- Insert sample card skins
('Bộ Bài Rồng Vàng', 'CARD_SKIN', 1000.00, '🐉', 'Bộ bài với họa tiết rồng vàng', 'RARE'),
('Bộ Bài Phượng Hoàng', 'CARD_SKIN', 1500.00, '🦅', 'Bộ bài với họa tiết phượng hoàng', 'EPIC'),
('Bộ Bài Hoa Sen', 'CARD_SKIN', 600.00, '🌸', 'Bộ bài với họa tiết hoa sen', 'COMMON'),
('Bộ Bài Thiên Thần', 'CARD_SKIN', 3000.00, '👼', 'Bộ bài thiên thần huyền bí', 'LEGENDARY');

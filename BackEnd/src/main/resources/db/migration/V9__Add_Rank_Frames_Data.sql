-- Add min_rank_points column to shop_items
ALTER TABLE shop_items ADD COLUMN min_rank_points INT DEFAULT 0;

-- Clean up existing rank-labeled items if any (from previous ad-hoc migrations)
DELETE FROM shop_items WHERE type = 'AVATAR_FRAME' AND price = 0;

-- Insert Rank Frames with requirements
-- Ranks: Iron (0), Bronze (100), Silver (300), Gold (600), Platinum (1000), Diamond (1500), Master (2500), Challenger (4000)
INSERT INTO shop_items (name, type, price, image_url, description, rarity, min_rank_points) VALUES 
('Khung Sắt Thủ Khoa', 'AVATAR_FRAME', 0, 'frame-rank-iron', 'Phần thưởng dành cho người mới gia nhập.', 'COMMON', 0),
('Khung Đồng Quyết Chiến', 'AVATAR_FRAME', 0, 'frame-rank-bronze', 'Phần thưởng dành cho hạng Đồng.', 'COMMON', 100),
('Khung Bạc Hào Kiệt', 'AVATAR_FRAME', 0, 'frame-rank-silver', 'Phần thưởng dành cho hạng Bạc.', 'RARE', 300),
('Khung Vàng Phú Quý', 'AVATAR_FRAME', 0, 'frame-rank-gold', 'Phần thưởng dành cho hạng Vàng.', 'RARE', 600),
('Khung Bạch Kim Tinh Anh', 'AVATAR_FRAME', 0, 'frame-rank-platinum', 'Phần thưởng dành cho hạng Bạch Kim.', 'EPIC', 1000),
('Khung Kim Cương Chí Tôn', 'AVATAR_FRAME', 0, 'frame-rank-diamond', 'Phần thưởng dành cho hạng Kim Cương.', 'EPIC', 1500),
('Khung Cao Thủ Vô Song', 'AVATAR_FRAME', 0, 'frame-rank-master', 'Phần thưởng dành cho hạng Cao Thủ.', 'LEGENDARY', 2500),
('Khung Thách Đấu Toàn Cầu', 'AVATAR_FRAME', 0, 'frame-rank-challenger', 'Phần thưởng tối thượng của giới bài thủ.', 'LEGENDARY', 4000);

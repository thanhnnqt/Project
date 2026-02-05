-- Rename CARD_EFFECT to PLAYER_CARD_FRAME for consistency
UPDATE shop_items SET type = 'PLAYER_CARD_FRAME' WHERE type = 'CARD_EFFECT';

-- Update image_urls if they were using the old 'card-effect-' prefix
UPDATE shop_items SET image_url = REPLACE(image_url, 'card-effect-', 'player-card-frame-') 
WHERE type = 'PLAYER_CARD_FRAME' AND image_url LIKE 'card-effect-%';

-- Optional: Update names if they were generic
UPDATE shop_items SET name = 'Khung Hào Quang Thiên Sứ' WHERE image_url = 'player-card-frame-holy';
UPDATE shop_items SET name = 'Khung Ma Lực Hư Không' WHERE image_url = 'player-card-frame-void';
UPDATE shop_items SET name = 'Khung Hàn Băng Vĩnh Cửu' WHERE image_url = 'player-card-frame-frost';
UPDATE shop_items SET name = 'Khung Lửa Thiêng Phượng Hoàng' WHERE image_url = 'player-card-frame-phoenix';
UPDATE shop_items SET name = 'Khung Neon Cyberpunk' WHERE image_url = 'player-card-frame-neon';

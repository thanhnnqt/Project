-- Update classic frames to use CSS class names instead of emojis
UPDATE shop_items SET image_url = 'frame-gold' WHERE name = 'Khung Vàng Cổ Điển';
UPDATE shop_items SET image_url = 'frame-diamond' WHERE name = 'Khung Bạc Lấp Lánh';
UPDATE shop_items SET image_url = 'frame-diamond' WHERE name = 'Khung Kim Cương';
UPDATE shop_items SET image_url = 'frame-stars' WHERE name = 'Khung Huyền Thoại';

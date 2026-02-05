-- Update rank thresholds to match Global Points (Tier Index * 100)
UPDATE shop_items SET min_rank_points = 400 WHERE image_url = 'frame-rank-bronze';
UPDATE shop_items SET min_rank_points = 800 WHERE image_url = 'frame-rank-silver';
UPDATE shop_items SET min_rank_points = 1200 WHERE image_url = 'frame-rank-gold';
UPDATE shop_items SET min_rank_points = 1600 WHERE image_url = 'frame-rank-platinum';
UPDATE shop_items SET min_rank_points = 2000 WHERE image_url = 'frame-rank-diamond';
UPDATE shop_items SET min_rank_points = 2400 WHERE image_url = 'frame-rank-master';
UPDATE shop_items SET min_rank_points = 2500 WHERE image_url = 'frame-rank-challenger';

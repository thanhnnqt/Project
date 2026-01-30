-- Add Rank fields to players table
ALTER TABLE players ADD COLUMN rank_points INT DEFAULT 0;
ALTER TABLE players ADD COLUMN rank_tier VARCHAR(50) DEFAULT 'Sắt IV';

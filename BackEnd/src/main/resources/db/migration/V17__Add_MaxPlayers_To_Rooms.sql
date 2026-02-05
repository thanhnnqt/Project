-- Add max_players column to rooms table
ALTER TABLE rooms ADD COLUMN max_players INT DEFAULT 4;

-- Step 1: Add column as nullable first
ALTER TABLE players ADD COLUMN email VARCHAR(255);

-- Step 2: Update existing data to have unique values (based on username)
UPDATE players SET email = CONCAT(username, '@example.com') WHERE email IS NULL;

-- Step 3: Apply NOT NULL and UNIQUE constraints
ALTER TABLE players MODIFY COLUMN email VARCHAR(255) NOT NULL UNIQUE;

ALTER TABLE players ADD COLUMN age INT;
ALTER TABLE players ADD COLUMN phone_number VARCHAR(20);

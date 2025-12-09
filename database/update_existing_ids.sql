-- Update existing IDs to follow the new format
-- Run this script once to migrate existing data

-- Update user IDs
UPDATE users SET id = 'seller1' WHERE id = 'seller1'; -- Already correct
UPDATE users SET id = 'seller2' WHERE id = 'seller2'; -- Already correct
UPDATE users SET id = 'seller3' WHERE id = 'seller3'; -- Already correct
UPDATE users SET id = 'user1' WHERE id = 'user1'; -- Already correct
UPDATE users SET id = 'user2' WHERE id = 'user2'; -- Already correct

-- Update product IDs
-- Reset auto increment first
SET @rownum = 0;
UPDATE products SET id = CONCAT('prod', @rownum := @rownum + 1) ORDER BY created_at;

-- Update order IDs (if any exist)
-- Reset auto increment first
SET @rownum = 0;
UPDATE orders SET id = CONCAT('order', @rownum := @rownum + 1) ORDER BY created_at;

-- Update review IDs (if any exist)
-- Reset auto increment first
SET @rownum = 0;
UPDATE reviews SET id = CONCAT('rev', @rownum := @rownum + 1) ORDER BY created_at;

-- Create triggers to ensure new IDs follow the pattern (optional)
-- Note: We'll handle ID generation in PHP instead for better control
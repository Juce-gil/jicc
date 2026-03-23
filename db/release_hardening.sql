USE campus;

UPDATE product
SET cover_list = REPLACE(REPLACE(cover_list, 'http://localhost:21090', ''), 'http://127.0.0.1:21090', '')
WHERE cover_list LIKE '%http://localhost:21090%'
   OR cover_list LIKE '%http://127.0.0.1:21090%';

UPDATE user
SET user_avatar = REPLACE(REPLACE(user_avatar, 'http://localhost:21090', ''), 'http://127.0.0.1:21090', '')
WHERE user_avatar LIKE '%http://localhost:21090%'
   OR user_avatar LIKE '%http://127.0.0.1:21090%';

SET @user_idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND index_name = 'uk_user_account'
);
SET @user_idx_sql := IF(@user_idx_exists = 0,
  'ALTER TABLE user ADD UNIQUE KEY uk_user_account (user_account)',
  'SELECT 1');
PREPARE stmt FROM @user_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @order_idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'orders'
    AND index_name = 'uk_orders_code'
);
SET @order_idx_sql := IF(@order_idx_exists = 0,
  'ALTER TABLE orders ADD UNIQUE KEY uk_orders_code (code)',
  'SELECT 1');
PREPARE stmt FROM @order_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

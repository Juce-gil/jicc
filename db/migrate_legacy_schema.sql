-- Run this script inside the target MySQL database, for example:
--   USE campus;
--   SOURCE migrate_legacy_schema.sql;
--
-- Purpose:
-- Upgrade the legacy schema to the reservation-order version expected by the
-- current backend code. The script is written to be idempotent and compatible
-- with MySQL versions that do not support:
--   ALTER TABLE ... ADD COLUMN IF NOT EXISTS

SET @db_name = DATABASE();

-- ---------------------------------------------------------------------------
-- product table
-- ---------------------------------------------------------------------------

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `product` ADD COLUMN `status` varchar(32) DEFAULT ''ON_SALE'' AFTER `is_bargain`',
    'SELECT ''skip product.status'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'product'
    AND COLUMN_NAME = 'status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `product` ADD COLUMN `audit_status` varchar(32) DEFAULT ''APPROVED'' AFTER `status`',
    'SELECT ''skip product.audit_status'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'product'
    AND COLUMN_NAME = 'audit_status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `product`
SET `status` = 'ON_SALE'
WHERE `status` IS NULL OR `status` = '';

UPDATE `product`
SET `audit_status` = 'APPROVED'
WHERE `audit_status` IS NULL OR `audit_status` = '';

-- ---------------------------------------------------------------------------
-- orders table
-- ---------------------------------------------------------------------------

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `address_id` int DEFAULT NULL AFTER `create_time`',
    'SELECT ''skip orders.address_id'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'address_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `is_confirm` tinyint(1) DEFAULT 0 AFTER `address_id`',
    'SELECT ''skip orders.is_confirm'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'is_confirm'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `is_confirm_time` datetime DEFAULT NULL AFTER `is_confirm`',
    'SELECT ''skip orders.is_confirm_time'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'is_confirm_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `is_deliver` tinyint(1) DEFAULT 0 AFTER `is_confirm_time`',
    'SELECT ''skip orders.is_deliver'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'is_deliver'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `deliver_adr_id` int DEFAULT NULL AFTER `is_deliver`',
    'SELECT ''skip orders.deliver_adr_id'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'deliver_adr_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `deliver_time` datetime DEFAULT NULL AFTER `deliver_adr_id`',
    'SELECT ''skip orders.deliver_time'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'deliver_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `appointment_time` datetime DEFAULT NULL AFTER `deliver_time`',
    'SELECT ''skip orders.appointment_time'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'appointment_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `appointment_address` varchar(255) DEFAULT NULL AFTER `appointment_time`',
    'SELECT ''skip orders.appointment_address'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'appointment_address'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `seller_confirm_time` datetime DEFAULT NULL AFTER `appointment_address`',
    'SELECT ''skip orders.seller_confirm_time'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'seller_confirm_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `buyer_confirm_time` datetime DEFAULT NULL AFTER `seller_confirm_time`',
    'SELECT ''skip orders.buyer_confirm_time'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'buyer_confirm_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `seller_finish_time` datetime DEFAULT NULL AFTER `buyer_confirm_time`',
    'SELECT ''skip orders.seller_finish_time'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'seller_finish_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `cancel_time` datetime DEFAULT NULL AFTER `seller_finish_time`',
    'SELECT ''skip orders.cancel_time'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'cancel_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `orders` ADD COLUMN `cancel_reason` varchar(255) DEFAULT NULL AFTER `cancel_time`',
    'SELECT ''skip orders.cancel_reason'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'orders'
    AND COLUMN_NAME = 'cancel_reason'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

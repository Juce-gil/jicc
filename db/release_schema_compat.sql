SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Backfill legacy tables that are required by current backend mappers.
CREATE TABLE IF NOT EXISTS `address` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `concat_person` varchar(100) DEFAULT NULL,
  `get_adr` varchar(255) DEFAULT NULL,
  `concat_phone` varchar(30) DEFAULT NULL,
  `is_default` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_address_user_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='shipping address';

CREATE TABLE IF NOT EXISTS `content` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `detail` text,
  `cover` varchar(1000) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_content_user` (`user_id`),
  KEY `idx_content_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='dynamic content';

CREATE TABLE IF NOT EXISTS `evaluations_upvote` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `evaluations_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_evaluations_upvote` (`user_id`, `evaluations_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='evaluations upvote';

SET FOREIGN_KEY_CHECKS = 1;

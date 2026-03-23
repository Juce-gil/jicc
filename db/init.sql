SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `trade_review`;
DROP TABLE IF EXISTS `sys_notice`;
DROP TABLE IF EXISTS `goods_report`;
DROP TABLE IF EXISTS `campus_auth`;
DROP TABLE IF EXISTS `evaluations_upvote`;
DROP TABLE IF EXISTS `evaluations`;
DROP TABLE IF EXISTS `operation_log`;
DROP TABLE IF EXISTS `message`;
DROP TABLE IF EXISTS `interaction`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `product`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_account` varchar(50) DEFAULT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `user_pwd` varchar(100) DEFAULT NULL,
  `user_avatar` varchar(255) DEFAULT NULL,
  `user_email` varchar(50) DEFAULT NULL,
  `user_role` int DEFAULT NULL,
  `is_login` tinyint(1) DEFAULT 0,
  `is_word` tinyint(1) DEFAULT 0,
  `campus_name` varchar(100) DEFAULT NULL,
  `student_no` varchar(50) DEFAULT NULL,
  `campus_verified` tinyint(1) DEFAULT 0,
  `credit_score` int DEFAULT 100,
  `last_login_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_account` (`user_account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='system user';

CREATE TABLE `category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `is_use` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product category';

CREATE TABLE `product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `detail` text,
  `cover_list` varchar(1000) DEFAULT NULL,
  `old_level` int DEFAULT NULL,
  `category_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `inventory` int DEFAULT 1,
  `price` decimal(10,2) DEFAULT NULL,
  `is_bargain` tinyint(1) DEFAULT 0,
  `status` varchar(32) DEFAULT 'ON_SALE',
  `audit_status` varchar(32) DEFAULT 'APPROVED',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_product_user` (`user_id`),
  KEY `idx_product_category` (`category_id`),
  KEY `idx_product_status` (`status`),
  KEY `idx_product_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='campus second-hand goods';

CREATE TABLE `interaction` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `product_id` int DEFAULT NULL,
  `type` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_interaction_user_product_type` (`user_id`,`product_id`,`type`),
  KEY `idx_interaction_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='favorite/view/like record';

CREATE TABLE `message` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `content` varchar(1000) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_message_user` (`user_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='system message';

CREATE TABLE `operation_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `detail` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='admin operation log';

CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(64) DEFAULT NULL,
  `detail` varchar(255) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `product_id` int DEFAULT NULL,
  `buy_price` decimal(10,2) DEFAULT NULL,
  `buy_number` int DEFAULT 1,
  `trade_status` int DEFAULT 1 COMMENT '1 pending seller confirm, 2 reserved, 3 partial confirmed, 4 completed, 5 cancelled',
  `refund_status` int DEFAULT NULL,
  `refund_time` datetime DEFAULT NULL,
  `trade_time` datetime DEFAULT NULL,
  `is_refund_confirm` tinyint(1) DEFAULT NULL COMMENT 'seller finish confirm',
  `create_time` datetime DEFAULT NULL,
  `address_id` int DEFAULT NULL,
  `is_confirm` tinyint(1) DEFAULT NULL COMMENT 'buyer finish confirm',
  `is_confirm_time` datetime DEFAULT NULL,
  `is_deliver` tinyint(1) DEFAULT NULL,
  `deliver_adr_id` int DEFAULT NULL,
  `deliver_time` datetime DEFAULT NULL,
  `appointment_time` datetime DEFAULT NULL,
  `appointment_address` varchar(255) DEFAULT NULL,
  `seller_confirm_time` datetime DEFAULT NULL,
  `buyer_confirm_time` datetime DEFAULT NULL,
  `seller_finish_time` datetime DEFAULT NULL,
  `cancel_time` datetime DEFAULT NULL,
  `cancel_reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_orders_user` (`user_id`),
  KEY `idx_orders_product` (`product_id`),
  KEY `idx_orders_status` (`trade_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='reservation order';

CREATE TABLE `evaluations` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `parent_id` int DEFAULT NULL,
  `commenter_id` int DEFAULT NULL,
  `replier_id` int DEFAULT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `content_id` int DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `upvote_list` longtext,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='legacy evaluations';

CREATE TABLE `evaluations_upvote` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `evaluations_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_evaluations_upvote` (`user_id`,`evaluations_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='legacy evaluations upvote';

CREATE TABLE `campus_auth` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `campus_name` varchar(100) DEFAULT NULL,
  `student_no` varchar(50) DEFAULT NULL,
  `proof_image` varchar(255) DEFAULT NULL,
  `status` varchar(32) DEFAULT 'PENDING',
  `reject_reason` varchar(255) DEFAULT NULL,
  `reviewer_id` int DEFAULT NULL,
  `review_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_campus_auth_user` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='future campus auth module';

CREATE TABLE `goods_report` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int DEFAULT NULL,
  `report_user_id` int DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` varchar(32) DEFAULT 'PENDING',
  `handle_result` varchar(255) DEFAULT NULL,
  `handle_user_id` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `handle_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_goods_report_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='future report module';

CREATE TABLE `sys_notice` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) DEFAULT NULL,
  `content` text,
  `status` varchar(32) DEFAULT 'DRAFT',
  `create_user_id` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `publish_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='future notice module';

CREATE TABLE `trade_review` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL,
  `review_user_id` int DEFAULT NULL,
  `target_user_id` int DEFAULT NULL,
  `score` int DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='future trade review module';

INSERT INTO `user` (`id`, `user_account`, `user_name`, `user_pwd`, `user_email`, `user_role`, `is_login`, `is_word`, `campus_name`, `student_no`, `campus_verified`, `credit_score`, `create_time`) VALUES
(1, 'admin', '系统管理员', 'e10adc3949ba59abbe56e057f20f883e', 'admin@example.com', 1, 0, 0, '示例大学', 'ADMIN-0001', 1, 100, NOW()),
(2, 'student01', '测试学生', 'e10adc3949ba59abbe56e057f20f883e', 'student01@example.com', 2, 0, 0, '示例大学', '20240001', 1, 100, NOW()),
(3, 'seller01', '测试卖家', 'e10adc3949ba59abbe56e057f20f883e', 'seller01@example.com', 2, 0, 0, '示例大学', '20240002', 1, 100, NOW());

INSERT INTO `category` (`id`, `name`, `is_use`) VALUES
(1, '教材书籍', 1),
(2, '数码电子', 1),
(3, '宿舍生活', 1),
(4, '运动器材', 1),
(5, '其他', 1);

INSERT INTO `product` (`id`, `name`, `detail`, `cover_list`, `old_level`, `category_id`, `user_id`, `inventory`, `price`, `is_bargain`, `status`, `audit_status`, `create_time`) VALUES
(1, '高等数学教材', '九成新，可校内面交。', 'http://localhost:21090/api/campus-product-sys/v1.0/file/getFile?fileName=demo-book.png', 9, 1, 3, 1, 25.00, 0, 'ON_SALE', 'APPROVED', NOW()),
(2, '二手蓝牙耳机', '功能正常，支持当面验货。', 'http://localhost:21090/api/campus-product-sys/v1.0/file/getFile?fileName=demo-earbuds.png', 8, 2, 3, 1, 88.00, 1, 'ON_SALE', 'APPROVED', NOW());

INSERT INTO `sys_notice` (`id`, `title`, `content`, `status`, `create_user_id`, `create_time`, `publish_time`) VALUES
(1, '欢迎使用校园二手交易平台', '当前版本已经完成双前端骨架与预约面交核心流程改造，后续会继续补校园认证、举报和公告后台。', 'PUBLISHED', 1, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;

SET @db = DATABASE();

START TRANSACTION;

-- product records left by deleted users
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'product'),
    'DELETE p FROM product p LEFT JOIN user u ON u.id = p.user_id WHERE p.user_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip product cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- content records left by deleted users
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'content'),
    'DELETE c FROM content c LEFT JOIN user u ON u.id = c.user_id WHERE c.user_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip content cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- system messages left by deleted users
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'message'),
    'DELETE m FROM message m LEFT JOIN user u ON u.id = m.user_id WHERE m.user_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip message cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- operation logs left by deleted users
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'operation_log'),
    'DELETE ol FROM operation_log ol LEFT JOIN user u ON u.id = ol.user_id WHERE ol.user_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip operation_log cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- interactions left by deleted users or deleted products
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'interaction'),
    'DELETE i FROM interaction i LEFT JOIN user u ON u.id = i.user_id LEFT JOIN product p ON p.id = i.product_id WHERE (i.user_id IS NOT NULL AND u.id IS NULL) OR (i.product_id IS NOT NULL AND p.id IS NULL)',
    'SELECT ''skip interaction cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- orders left by deleted buyers or deleted products
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'orders'),
    'DELETE o FROM orders o LEFT JOIN user u ON u.id = o.user_id LEFT JOIN product p ON p.id = o.product_id WHERE (o.user_id IS NOT NULL AND u.id IS NULL) OR (o.product_id IS NOT NULL AND p.id IS NULL)',
    'SELECT ''skip orders cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- legacy evaluations authored by deleted users
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'evaluations'),
    'DELETE e FROM evaluations e LEFT JOIN user u ON u.id = e.commenter_id WHERE e.commenter_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip evaluations cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- legacy evaluations replying to deleted users are preserved, but the target user reference is cleared
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'evaluations'),
    'UPDATE evaluations e LEFT JOIN user u ON u.id = e.replier_id SET e.replier_id = NULL WHERE e.replier_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip evaluations replier normalization'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- detach child comments from missing parents instead of deleting surviving users'' comments
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'evaluations'),
    'UPDATE evaluations e LEFT JOIN evaluations p ON p.id = e.parent_id SET e.parent_id = NULL WHERE e.parent_id IS NOT NULL AND p.id IS NULL',
    'SELECT ''skip evaluations parent normalization'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- evaluation upvotes left by deleted users or pointing to deleted evaluations
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'evaluations_upvote'),
    'DELETE eu FROM evaluations_upvote eu LEFT JOIN user u ON u.id = eu.user_id LEFT JOIN evaluations e ON e.id = eu.evaluations_id WHERE (eu.user_id IS NOT NULL AND u.id IS NULL) OR (eu.evaluations_id IS NOT NULL AND e.id IS NULL)',
    'SELECT ''skip evaluations_upvote cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- future tables: campus_auth
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'campus_auth'),
    'DELETE ca FROM campus_auth ca LEFT JOIN user u ON u.id = ca.user_id WHERE ca.user_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip campus_auth user cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'campus_auth'),
    'UPDATE campus_auth ca LEFT JOIN user u ON u.id = ca.reviewer_id SET ca.reviewer_id = NULL WHERE ca.reviewer_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip campus_auth reviewer normalization'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- future tables: goods_report
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'goods_report'),
    'DELETE gr FROM goods_report gr LEFT JOIN product p ON p.id = gr.product_id LEFT JOIN user u ON u.id = gr.report_user_id WHERE (gr.product_id IS NOT NULL AND p.id IS NULL) OR (gr.report_user_id IS NOT NULL AND u.id IS NULL)',
    'SELECT ''skip goods_report cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'goods_report'),
    'UPDATE goods_report gr LEFT JOIN user u ON u.id = gr.handle_user_id SET gr.handle_user_id = NULL WHERE gr.handle_user_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip goods_report handler normalization'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- future tables: sys_notice
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'sys_notice'),
    'UPDATE sys_notice sn LEFT JOIN user u ON u.id = sn.create_user_id SET sn.create_user_id = NULL WHERE sn.create_user_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip sys_notice normalization'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- future tables: trade_review
SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'trade_review'),
    'DELETE tr FROM trade_review tr LEFT JOIN orders o ON o.id = tr.order_id LEFT JOIN user u ON u.id = tr.review_user_id WHERE (tr.order_id IS NOT NULL AND o.id IS NULL) OR (tr.review_user_id IS NOT NULL AND u.id IS NULL)',
    'SELECT ''skip trade_review cleanup'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @db AND table_name = 'trade_review'),
    'UPDATE trade_review tr LEFT JOIN user u ON u.id = tr.target_user_id SET tr.target_user_id = NULL WHERE tr.target_user_id IS NOT NULL AND u.id IS NULL',
    'SELECT ''skip trade_review target normalization'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

COMMIT;
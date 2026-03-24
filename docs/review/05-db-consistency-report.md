# 数据库一致性审查报告

## 1. 目标
验证订单状态机、商品库存状态联动、迁移脚本幂等性。

## 2. 订单状态机定义
- `1` PENDING_CONFIRM
- `2` RESERVED
- `3` PARTIAL_CONFIRMED
- `4` COMPLETED
- `5` CANCELLED

统一定义位置：`TradeStatusEnum.java`

## 3. 联动规则
- 买家发起预约：订单 `null -> 1`
- 卖家确认预约：订单 `1 -> 2`，商品 `ON_SALE -> RESERVED`，库存置 `0`
- 任一方先确认完成：订单 `2 -> 3`
- 双方都确认：订单 `3 -> 4`，商品 `SOLD`，库存 `0`
- 取消预约：订单 `1/2/3 -> 5`，若无活跃预约则商品回 `ON_SALE` 且库存 `1`

## 4. 脚本审查
- 初始化：`db/init.sql`
- 兼容补表：`db/release_schema_compat.sql`
- 旧库迁移：`db/migrate_legacy_schema.sql`
- 发布硬化：`db/release_hardening.sql`
- 脏数据清理：`db/cleanup_deleted_user_orphans.sql`

## 5. 结论
- 状态机已具备统一枚举来源，便于前后端一致描述。
- 迁移脚本采用 `information_schema` 判断，具备幂等执行能力。
- 建议后续补充“回滚脚本模板 + 版本号登记表”。

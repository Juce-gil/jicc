# 公共接口变更说明（本次整改）

## 1. 订单动作接口响应补充统一数据体
以下接口在保持 `code/msg` 的同时，`data` 字段补充统一结构：
- `POST /product/buyProduct`
- `POST /product/placeAnOrder/{ordersId}`
- `POST /product/refund/{ordersId}`
- `POST /product/getGoods/{ordersId}`
- `POST /product/confirmTradeBySeller/{ordersId}`
- `POST /orders/returnMoney/{ordersId}`

统一结构：`OrderActionResultVO`
- `orderId`
- `orderCode`
- `beforeTradeStatus`
- `afterTradeStatus`
- `beforeTradeStatusName`
- `afterTradeStatusName`
- `action`
- `message`

## 2. 状态枚举统一
新增 `TradeStatusEnum`：
- 1 PENDING_CONFIRM
- 2 RESERVED
- 3 PARTIAL_CONFIRMED
- 4 COMPLETED
- 5 CANCELLED

## 3. 鉴权策略变更
- 不再放行 `/**/query`。
- 对管理查询接口增加管理员角色约束。

## 4. 前端 API 类型增强
- 学生端与管理端 `src/api/*.ts` 已补充订单、商品、鉴权等核心类型定义。

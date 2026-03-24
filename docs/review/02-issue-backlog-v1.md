# 问题分级台账 v1

> 评分标准：
> - P0：高风险/可导致越权或严重业务错误，必须优先修复
> - P1：中风险/影响稳定性或论文验收质量，建议本轮修复
> - P2：优化项/可在论文中作为改进建议

## P0 问题（已修复）

### P0-01 查询放行过宽导致潜在未授权访问
- 复现条件：未登录请求 `/**/query`。
- 影响范围：多模块查询接口。
- 触发位置：`InterceptorConfig.java`。
- 修复方案：将放行从通配符改为精确公开接口。
- 论文价值：可写入“权限模型收敛优化”。

### P0-02 商品更新缺所有权校验
- 复现条件：普通用户提交他人商品 id 调用 `/product/update`。
- 影响范围：商品数据完整性。
- 触发位置：`ProductServiceImpl#update`。
- 修复方案：新增 owner/admin 校验。
- 论文价值：可写“横向越权防护”。

### P0-03 文件下载路径穿越风险
- 复现条件：构造 `fileName=../...`。
- 影响范围：文件安全。
- 触发位置：`FileController#getImage` / `resolveReadableFile`。
- 修复方案：文件名白名单 + canonical 路径边界校验。
- 论文价值：可写“输入安全校验策略”。

## P1 问题（已修复）

### P1-01 管理查询接口角色限制不统一
- 触发位置：Dashboard / Message / OperationLog / Orders 管理查询。
- 修复方案：统一 `@Protector(roleCode = Protector.ROLE_ADMIN)`。

### P1-02 互动与内容删除存在跨用户删除风险
- 触发位置：`InteractionServiceImpl#batchDelete`、`ContentServiceImpl#batchDelete`。
- 修复方案：非管理员仅可删除本人资源。

### P1-03 订单状态常量分散
- 触发位置：订单相关 Service。
- 修复方案：新增 `TradeStatusEnum` 并统一引用。

## P2 问题（建议后续）
- JWT 密钥应从配置中心/环境变量读取，避免硬编码。
- 密码策略建议升级为强哈希（如 BCrypt）并补迁移脚本。
- 前端构建包体需继续拆分优化（admin 与 legacy 体积较大）。

## 状态总览
- P0：3/3 已修复
- P1：3/3 已修复
- P2：0/3（规划中）

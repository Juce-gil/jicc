# 安全与权限审查专项报告

## 审查时间
- 2026-03-24

## 范围
- 鉴权入口、角色鉴权、资源归属、文件访问安全、订单状态一致性。

## 1. 鉴权白名单收敛
- 修复前：`/**/query` 被统一放行。
- 修复后：仅放行以下公开接口：
  - `/user/login`
  - `/user/register`
  - `/file/getFile`
  - `/category/query`
  - `/product/query`
  - `/product/queryProductList/*`
- 代码位置：`campus-product-sys/src/main/java/cn/kmbeast/config/InterceptorConfig.java`

## 2. 角色鉴权统一
- 增强注解：`@Protector` 新增 `roleCode`，并内置常量。
- 切面增强：兼容 roleName + roleCode 双路径，统一拒绝策略。
- 重点接口改造为管理员可见：
  - `DashboardController`
  - `OperationLogController`
  - `MessageController#query`
  - `OrdersController#query`
  - `CategoryController` 的变更接口

## 3. 资源归属校验
- 已加固：
  - 商品更新仅 owner/admin 可改（`ProductServiceImpl#update`）
  - 互动批量删除仅 owner/admin 可删（`InteractionServiceImpl#batchDelete`）
  - 内容更新/删除仅 owner/admin 可操作（`ContentServiceImpl`）

## 4. 文件访问安全
- 风险点：`fileName` 参数可被构造为路径穿越。
- 加固措施：
  - 文件名合法性校验（禁止 `..`, `/`, `\\`, `:`）
  - canonical path 边界判断（必须位于上传根目录）
  - 不安全请求直接 `400`
- 代码位置：`FileController.java`

## 5. JWT 处理增强
- `JwtInterceptor` 支持 `token` 与 `Authorization: Bearer`。
- token 为空/非法 payload 统一返回认证错误。

## 审查结论
- 关键越权路径已闭合。
- 具备论文“安全加固前后对比”撰写价值。

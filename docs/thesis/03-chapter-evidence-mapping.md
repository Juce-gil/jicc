# 章节-代码证据映射表

| 论文章节 | 证据类型 | 代码/文件路径 |
|---|---|---|
| 2.需求分析 | 路由与角色约束 | `frontend-user/src/router/index.ts` |
| 2.需求分析 | 路由与角色约束 | `frontend-admin/src/router/index.ts` |
| 3.总体设计 | 架构入口 | `campus-product-sys/src/main/resources/application.yml` |
| 3.总体设计 | API 入口与 context-path | `CampusProductApplication.java` + `application.yml` |
| 3.数据库设计 | 表结构与索引 | `db/init.sql` |
| 3.数据库设计 | 兼容迁移策略 | `db/release_schema_compat.sql`、`db/migrate_legacy_schema.sql` |
| 3.接口规范 | 统一返回体 | `pojo/api/ApiResult.java`、`pojo/api/Result.java` |
| 3.状态机 | 订单状态枚举 | `pojo/em/TradeStatusEnum.java` |
| 4.后端实现 | 商品与预约主流程 | `service/impl/ProductServiceImpl.java` |
| 4.后端实现 | 订单聚合查询/关闭 | `service/impl/OrdersServiceImpl.java` |
| 4.安全实现 | 认证拦截与白名单 | `config/InterceptorConfig.java`、`Interceptor/JwtInterceptor.java` |
| 4.安全实现 | 方法级鉴权 | `aop/Protector.java`、`aop/ProtectorAspect.java` |
| 4.安全实现 | 文件访问安全 | `controller/FileController.java` |
| 4.前端实现 | API 类型约束（用户端） | `frontend-user/src/api/*.ts` |
| 4.前端实现 | API 类型约束（管理端） | `frontend-admin/src/api/*.ts` |
| 4.部署实现 | 一键部署脚本 | `deploy.sh` |
| 5.测试 | 构建结果证据 | `frontend-user` / `frontend-admin` / `campus-product-view` build logs |
| 5.测试 | 权限整改证据 | `docs/review/04-security-and-permission-report.md` |

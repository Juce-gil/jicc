# 项目审查清单 v1

> 适用对象：`frontend-user` + `frontend-admin` + `campus-product-sys`
>
> 更新日期：2026-03-24

## 1. 审查目标
- 输出可给导师直接阅读的项目现状与风险结论。
- 建立“模块-接口-页面-数据表”可追溯关系。
- 形成后续论文第 3/4/5 章可复用证据。

## 2. 审查范围
- 后端：`C:/Users/Administrator/Desktop/product/campus-product-sys`
- 学生端：`C:/Users/Administrator/Desktop/product/frontend-user`
- 管理端：`C:/Users/Administrator/Desktop/product/frontend-admin`
- 旧版对照：`C:/Users/Administrator/Desktop/product/campus-product-view`
- 数据库：`C:/Users/Administrator/Desktop/product/db`

## 3. 检查项（结果）

| 维度 | 检查项 | 结果 | 证据路径 |
|---|---|---|---|
| 架构 | 三端目录结构清晰 | ✅ | 根目录模块结构 |
| 构建 | `frontend-user` 可构建 | ✅ | 本次构建日志 |
| 构建 | `frontend-admin` 可构建 | ✅（有大包告警） | 本次构建日志 |
| 构建 | `campus-product-view` 可构建 | ✅（有大包告警） | 本次构建日志 |
| 构建 | 后端本机 Maven 编译 | ⚠️ 本机缺 `mvn` | 需 CI / Docker 补证 |
| 权限 | 查询放行规则过宽 | ❌ 已修复 | `InterceptorConfig.java` |
| 权限 | 管理查询缺角色限制 | ❌ 已修复 | `DashboardController.java` 等 |
| 越权 | 商品更新存在横向越权风险 | ❌ 已修复 | `ProductServiceImpl.java` |
| 越权 | 互动批删存在横向越权风险 | ❌ 已修复 | `InteractionServiceImpl.java` |
| 安全 | 文件下载存在路径穿越风险 | ❌ 已修复 | `FileController.java` |
| 规范 | 订单状态使用裸常量 | ⚠️ 已优化 | `TradeStatusEnum.java` |
| 规范 | 前端 API 类型不完整 | ⚠️ 已优化 | `frontend-*/src/api/*.ts` |

## 4. 结论
- 当前项目适合毕业设计工程实现路线。
- 已具备“可运行 + 可整改 + 可度量”的论文条件。
- 建议后续重点用“问题发现 -> 修复 -> 验证结果”作为答辩主线。

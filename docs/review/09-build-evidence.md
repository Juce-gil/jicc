# 构建证据记录

## 本地执行时间
- 2026-03-24

## 命令与结果

### 1) frontend-user
```bash
cd frontend-user
npm run build
```
结果：✅ 成功

### 2) frontend-admin
```bash
cd frontend-admin
npm run build
```
结果：✅ 成功
说明：存在 chunk 过大告警（>500kb），已纳入优化清单。

### 3) campus-product-view（对照旧版）
```bash
cd campus-product-view
npm run build
```
结果：✅ 成功
说明：存在入口体积告警（历史遗留）。

### 4) campus-product-sys
```bash
cd campus-product-sys
mvn -DskipTests compile
```
结果：⚠️ 当前机器无 `mvn` 命令，无法本地验证。
补偿方案：新增 `.github/workflows/backend-build.yml`，在 CI 进行后端编译与打包校验。

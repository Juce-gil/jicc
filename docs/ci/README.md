# CI 编译校验说明

为补齐“本机无 Maven 环境”的可运行性证据，本次新增：

- GitHub Actions：`.github/workflows/backend-build.yml`
  - 使用 JDK17
  - 执行 `mvn -DskipTests compile` 与 `package`

## 使用方式
1. 推送到远程仓库。
2. 在 Actions 查看 `backend-build` 任务结果。
3. 将通过截图用于论文“环境与验证”章节。

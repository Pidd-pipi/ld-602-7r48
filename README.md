# 城市防灾应急物资调度系统

面向街道、社区和应急仓库的防灾物资储备与调拨平台，覆盖物资库存、避难点、事件响应和调拨审批。

## 快速启动

```bash
cp .env.example .env && docker compose up -d
```

## 访问地址或 CLI 示例

前端：<http://localhost:20102>

后端健康检查：<http://localhost:21102/health>


## 本地开发方式

- 前端：`cd frontend && npm install && npm run dev`（Vite 已将 `/api` 代理到 `http://localhost:21102`）
- 后端：进入 `backend` 后按技术栈运行开发命令，接口统一挂在 `/api`。默认使用 H2 文件库、端口 8080；如本地用 21102 调试可加 `--server.port=21102`，无需启动 MySQL。


## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia + ECharts |
| 后端 | Spring Boot 3 + Java 17 + Spring Data JPA / MyBatis-Plus |
| 数据库 | MySQL 8.0（本地默认 H2 文件库，零外部依赖） |
| 部署 | Docker Compose |

## 库存批次盘点差异复核

仓库在「仓库库存」页对批次录入实盘数并提交，差异保留待审，审批通过后批次数量才调整为实盘数。

- 提交：`POST /api/stock-check`。差异 = 实盘数 - 账面数，盘盈/盘亏分别填写 `gain_basis`/`loss_basis`（各自必填），提交后批次数量不变。
- 复核：`POST /api/stock-check/{id}/approve` 通过并调账；`POST /api/stock-check/{id}/reject` 驳回（数量不变）。复核记录 `reviewed_by/reviewed_at/review_comment`。
- 约束：盘盈不得回补质检冻结（`quality_status=FROZEN`）或已过期（`expire_at < 今日`）批次，违反返回 `409 STOCK_CHECK_GAIN_BLOCKED`；盘亏可照常提交。
- 防重：同一批次同时只能有一张待审盘点。服务端有三重保障——按批次 JVM 锁串行化、`stock_check.pending_batch_id` 唯一约束（审批/驳回后置空）、盘点单与批次的 `@Version` 乐观锁。重复提交、重复审批、并发审批都只有一次成功，其余返回 `409`，批次与盘点单不变。
- 回读：批次列表（`GET /api/inventory-batch?warehouseId=`，含 `pending_check_id`）与差异单（`GET /api/stock-check?batchId=&status=`）均从持久化存储读取，刷新页面或重启服务后仍可回读。
- 本地开发默认使用 H2 文件库（`backend/data/`，已加入 .gitignore）；Docker 部署通过 `SPRING_PROFILES_ACTIVE=mysql` 使用 MySQL，表结构由 JPA 自动维护，`database/init.sql` 含相同唯一约束。

## 项目目录结构

```text
frontend/src/api, stores, types, constants, constructors, components/common, hooks, pages, router, utils, mocks
backend/src/routes, controllers, services, models, repositories, middlewares, constants, constructors, utils, types, config
```

## 环境变量说明

- `COMPOSE_PROJECT_NAME`: Compose 项目名，默认 `rescue-stock`
- `FRONTEND_PORT`: 前端端口，默认 `20102`
- `BACKEND_PORT`: 后端端口，默认 `21102`
- `DB_PORT`: 数据库宿主机端口
- `DB_USER/DB_PASSWORD/DB_NAME`: 本地数据库凭据

## Docker 部署说明

- 根 Compose 文件不写 `version`，顶层 `name: rescue-stock`。
- 容器名均使用 `${COMPOSE_PROJECT_NAME:-rescue-stock}` 前缀。
- 数据库使用命名卷，避免绑定中文路径。
- 常见问题：端口占用时修改 `.env` 中端口后重启；需要重置数据时执行 `docker compose down -v`。

## 枚举/常量出现位置清单

- SupplyCategory: constants/SupplyCategory、types/SupplyCategory、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- DispatchStatus: constants/DispatchStatus、types/DispatchStatus、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- ShelterStatus: constants/ShelterStatus、types/ShelterStatus、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- StockCheck 盘点常量（PENDING/APPROVED/REJECTED、SURPLUS/LOSS/MATCHED、QUALIFIED/FROZEN）：前端 `constants/StockCheck.ts`、`types/StockCheck.ts`、`constructors/StockCheckConstructor.ts`、`constants/logTemplates.ts`、`constants/errorCodes.ts`、`constants/errorMessages.ts`、`constants/statusText.ts`、`components/common/StockCheckDialog.vue`、`components/common/StockCheckReviewPanel.vue`、`components/common/BatchTable.vue`、`pages/WarehousesPage.vue`；后端 `models/StockCheck.java`、`constants/QualityStatus.java`、`constants/ErrorCodes.java`、`constants/ErrorMessages.java`、`constants/LogTemplates.java`、`services/StockCheckService.java`、`controllers/StockCheckController.java`、`routes/StockCheckRoutes.java`、`types/StockCheck*Payload.java`、`constructors/StockCheckDtoFactory.java`。

## 为什么会牵一发动全身

实体字段、枚举、日志模板、错误消息、构造器、筛选器和展示组件被刻意拆散到多个目录；修改一个状态值通常需要同步类型、构造器、服务、控制器、store、页面、README 与数据库种子。

## License

MIT

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

- 前端：`cd frontend && npm install && npm run dev`
- 后端：进入 `backend` 后按技术栈运行开发命令，接口统一挂在 `/api`。


## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia + ECharts |
| 后端 | Spring Boot 3 + Java 17 + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 部署 | Docker Compose |

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
- StocktakeStatus（PENDING/APPROVED/REJECTED，盘点差异复核）:
  - 后端：`constants/StocktakeStatus.java`、`models/StocktakeOrder.java`、`repositories/StocktakeOrderRepository.java`（CAS 状态流转）、`services/StocktakeOrderService.java`、`controllers/StocktakeOrderController.java`、`constructors/StocktakeOrderDtoFactory.java`、`constants/LogTemplates.java`、`constants/ErrorCodes.java`、`constants/ErrorMessages.java`、`routes/StocktakeOrderRoutes.java`。
  - 前端：`constants/StocktakeStatus.ts`、`types/StocktakeOrder.ts`、`constructors/StocktakeOrderConstructor.ts`、`constants/logTemplates.ts`、`constants/errorCodes.ts`、`constants/errorMessages.ts`、`constants/statusText.ts`、`utils/formatters.ts`、`api/StocktakeOrder.ts`、`stores/StocktakeOrderStore.ts`、`hooks/useStocktakeReview.ts`、`components/common/StocktakeReviewPanel.vue`、`pages/WarehousesPage.vue`。
- StocktakeVariance（SURPLUS/LOSS/MATCH）: 后端 `constants/StocktakeVariance.java`（服务判定盘盈盘亏）、前端 `constants/StocktakeVariance.ts` + `utils/formatters.ts`（差异文案/符号）+ 盘点弹窗与复核面板。
- QualityStatus（NORMAL/FROZEN/EXPIRED）: 后端 `constants/QualityStatus.java`（盘盈回补拦截）、`models/InventoryBatch.java`、`repositories/InventoryBatchRepository.java` 种子；前端 `constants/QualityStatus.ts`、`mocks/seedData.ts`、`components/common/BatchTable.vue`、`StocktakeFormDialog.vue`。

## 库存批次盘点差异复核

库存页（`/warehouses`）端到端规则：

1. 仓库员在批次行点击「发起盘点」，录入实盘数。盘盈 / 盘亏必须分别写明依据（数量一致不强制），提交后差异保留为 `PENDING` 待审单，**批次数量不变**。
2. 盘盈（实盘 > 账面）不得回补**质检冻结（FROZEN）或已过期（EXPIRED，含到期时间已过）**批次，提交直接返回 409 `STOCKTAKE_SURPLUS_FORBIDDEN`；盘亏可照常提交。
3. 同一批次同时只能有一张待审盘点：重复提交返回 409 `STOCKTAKE_PENDING_EXISTS`。
4. 审批员在「盘点差异复核」面板填写复核意见后通过 / 驳回；**仅审批通过**才在同一临界区内把批次数量条件调整为实盘数（账面须仍等于提交时快照），驳回保持账面不变。
5. 重复审批 / 并发审批只有一次成功（盘点单 PENDING→终态 CAS + 批次数量 CAS + 批次锁），失败者返回 409 `STOCKTAKE_NOT_PENDING`，批次与盘点单均不变。
6. 接口：`POST /api/stocktake-order/submit`、`GET /api/stocktake-order?batchId=`、`GET /api/stocktake-order/{id}`、`POST /api/stocktake-order/{id}/review`。前端在后端不可达时用 localStorage 执行同一套规则兜底，刷新后仍可回读。

## 为什么会牵一发动全身

实体字段、枚举、日志模板、错误消息、构造器、筛选器和展示组件被刻意拆散到多个目录；修改一个状态值通常需要同步类型、构造器、服务、控制器、store、页面、README 与数据库种子。

## License

MIT

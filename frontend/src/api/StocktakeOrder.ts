import { ERROR_CODES } from "../constants/errorCodes";
import { ERROR_MESSAGES } from "../constants/errorMessages";
import type {
  StocktakeOrder,
  StocktakeStatusValue,
  StocktakeVarianceValue
} from "../types/StocktakeOrder";
import type { InventoryBatch } from "../types/InventoryBatch";
import { localSeedBatches } from "./InventoryBatch";
import { readStore, writeStore } from "../utils/localStore";

const endpoint = "/api/stocktake-order";
const ORDER_STORE_KEY = "stocktakeOrder";

/** 盘点业务错误，携带与后端一致的错误码，页面据此提示。 */
export class StocktakeApiError extends Error {
  code: string;
  constructor(code: string) {
    super(ERROR_MESSAGES[code as keyof typeof ERROR_MESSAGES] ?? code);
    this.code = code;
  }
}

export interface StocktakeSubmitRequest {
  batchId: number;
  actualQuantity: number;
  reason: string;
  submittedBy: string;
}

export interface StocktakeReviewRequest {
  approve: boolean;
  reviewNote: string;
  reviewedBy: string;
}

async function parseError(res: Response): Promise<never> {
  let code = "VALIDATION_FAILED";
  try {
    const body = await res.json();
    if (body && typeof body.code === "string") code = body.code;
  } catch {
    // 非 JSON 错误体退化为通用校验错误。
  }
  throw new StocktakeApiError(code);
}

export async function listStocktakeOrder(batchId?: number): Promise<StocktakeOrder[]> {
  try {
    const url = batchId ? `${endpoint}?batchId=${batchId}` : endpoint;
    const res = await fetch(url);
    if (res.ok) return await res.json();
  } catch {
    // 后端不可达时走本地兜底（刷新仍可回读）。
  }
  return localList(batchId);
}

export async function getStocktakeOrder(id: number): Promise<StocktakeOrder> {
  try {
    const res = await fetch(`${endpoint}/${id}`);
    if (res.ok) return await res.json();
    if (res.status === 404) await parseError(res);
  } catch (error) {
    if (error instanceof StocktakeApiError) throw error;
  }
  const found = localList().find((order) => order.id === id);
  if (!found) throw new StocktakeApiError(ERROR_CODES.STOCKTAKE_ORDER_NOT_FOUND);
  return found;
}

export async function submitStocktakeOrder(payload: StocktakeSubmitRequest): Promise<StocktakeOrder> {
  try {
    const res = await fetch(`${endpoint}/submit`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    if (res.ok) return await res.json();
    await parseError(res);
  } catch (error) {
    if (error instanceof StocktakeApiError) throw error;
  }
  return localSubmit(payload);
}

export async function reviewStocktakeOrder(
  id: number,
  payload: StocktakeReviewRequest
): Promise<StocktakeOrder> {
  try {
    const res = await fetch(`${endpoint}/${id}/review`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    if (res.ok) return await res.json();
    await parseError(res);
  } catch (error) {
    if (error instanceof StocktakeApiError) throw error;
  }
  return localReview(id, payload);
}

// ---------------------------------------------------------------------------
// 离线兜底：localStorage 内复刻后端规则（待审唯一、盘盈限制、审批才落账）。
// ---------------------------------------------------------------------------

function localList(batchId?: number): StocktakeOrder[] {
  const orders = readStore<StocktakeOrder[]>(ORDER_STORE_KEY, []);
  return typeof batchId === "number" ? orders.filter((order) => order.batch_id === batchId) : orders;
}

function localBatches(): InventoryBatch[] {
  return localSeedBatches();
}

function saveBatches(batches: InventoryBatch[]): void {
  writeStore("inventoryBatch:seed", batches);
}

function saveOrders(orders: StocktakeOrder[]): void {
  writeStore(ORDER_STORE_KEY, orders);
}

function enrich(order: StocktakeOrder, batches: InventoryBatch[]): StocktakeOrder {
  const batch = batches.find((row) => row.id === order.batch_id);
  return {
    ...order,
    batch_no: batch?.batch_no ?? "",
    warehouse_id: batch?.warehouse_id ?? 0,
    supply_item_id: batch?.supply_item_id ?? 0
  };
}

function isBlockedForSurplus(batch: InventoryBatch): boolean {
  if (batch.quality_status === "FROZEN" || batch.quality_status === "EXPIRED") return true;
  const expireAt = Date.parse(batch.expire_at);
  return Number.isFinite(expireAt) && expireAt < Date.now();
}

function localSubmit(payload: StocktakeSubmitRequest): StocktakeOrder {
  if (!Number.isInteger(payload.actualQuantity) || payload.actualQuantity < 0) {
    throw new StocktakeApiError(ERROR_CODES.STOCKTAKE_QUANTITY_INVALID);
  }
  const batches = localBatches();
  const batch = batches.find((row) => row.id === payload.batchId);
  if (!batch) throw new StocktakeApiError(ERROR_CODES.STOCKTAKE_BATCH_NOT_FOUND);

  const orders = localList();
  // 同一批次同时只能有一张待审盘点。
  if (orders.some((order) => order.batch_id === payload.batchId && order.status === "PENDING")) {
    throw new StocktakeApiError(ERROR_CODES.STOCKTAKE_PENDING_EXISTS);
  }

  const varianceQuantity = payload.actualQuantity - batch.quantity;
  const variance: StocktakeVarianceValue =
    varianceQuantity > 0 ? "SURPLUS" : varianceQuantity < 0 ? "LOSS" : "MATCH";

  if (variance === "SURPLUS" && isBlockedForSurplus(batch)) {
    throw new StocktakeApiError(ERROR_CODES.STOCKTAKE_SURPLUS_FORBIDDEN);
  }
  if (variance !== "MATCH" && !payload.reason.trim()) {
    throw new StocktakeApiError(ERROR_CODES.STOCKTAKE_REASON_REQUIRED);
  }

  const nextId = orders.reduce((max, order) => Math.max(max, order.id), 0) + 1;
  const created: StocktakeOrder = {
    id: nextId,
    batch_id: batch.id,
    batch_no: batch.batch_no,
    warehouse_id: batch.warehouse_id,
    supply_item_id: batch.supply_item_id,
    book_quantity: batch.quantity,
    actual_quantity: payload.actualQuantity,
    variance_quantity: varianceQuantity,
    variance,
    status: "PENDING",
    reason: payload.reason.trim() || null,
    submitted_by: payload.submittedBy,
    submitted_at: new Date().toISOString(),
    reviewed_by: null,
    review_note: null,
    reviewed_at: null
  };
  saveOrders([...orders, created]);
  return created;
}

function localReview(id: number, payload: StocktakeReviewRequest): StocktakeOrder {
  if (!payload.reviewNote.trim()) {
    throw new StocktakeApiError(ERROR_CODES.STOCKTAKE_APPROVAL_NOTE_REQUIRED);
  }
  const orders = localList();
  const index = orders.findIndex((order) => order.id === id);
  if (index < 0) throw new StocktakeApiError(ERROR_CODES.STOCKTAKE_ORDER_NOT_FOUND);

  const order = orders[index];
  // 重复 / 并发审批只成功一次：状态已不是 PENDING 时直接失败，数据不变。
  if (order.status !== "PENDING") {
    throw new StocktakeApiError(ERROR_CODES.STOCKTAKE_NOT_PENDING);
  }

  const batches = localBatches();
  const batchIndex = batches.findIndex((row) => row.id === order.batch_id);
  const reviewedAt = new Date().toISOString();
  const nextStatus: StocktakeStatusValue = payload.approve ? "APPROVED" : "REJECTED";
  const reviewed: StocktakeOrder = {
    ...order,
    status: nextStatus,
    reviewed_by: payload.reviewedBy,
    review_note: payload.reviewNote.trim(),
    reviewed_at: reviewedAt
  };

  if (payload.approve && batchIndex >= 0) {
    // 只有审批通过才把批次数量调整为实盘数（以账面快照为基准做条件更新）。
    if (batches[batchIndex].quantity !== order.book_quantity) {
      throw new StocktakeApiError(ERROR_CODES.STOCKTAKE_NOT_PENDING);
    }
    batches[batchIndex] = { ...batches[batchIndex], quantity: order.actual_quantity };
    saveBatches(batches);
  }

  orders.splice(index, 1, reviewed);
  saveOrders(orders);
  return reviewed;
}

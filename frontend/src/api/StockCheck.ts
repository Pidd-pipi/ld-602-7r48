import { getJson, sendJson } from "./http";
import type { StockCheck, StockCheckReviewInput, StockCheckSubmitInput } from "../types/StockCheck";

const endpoint = "/api/stock-check";

/** 查看差异单：可按批次/状态过滤；刷新页面后由此回读。 */
export async function listStockCheck(params: { batchId?: number; status?: string } = {}): Promise<StockCheck[]> {
  const query = new URLSearchParams();
  if (params.batchId) query.set("batchId", String(params.batchId));
  if (params.status) query.set("status", params.status);
  const suffix = query.toString() ? `?${query.toString()}` : "";
  return getJson<StockCheck[]>(`${endpoint}${suffix}`);
}

export async function getStockCheck(id: number): Promise<StockCheck> {
  return getJson<StockCheck>(`${endpoint}/${id}`);
}

/** 仓库录入实盘数并提交：差异保留待审，不立即改批次数量。 */
export async function submitStockCheck(input: StockCheckSubmitInput): Promise<StockCheck> {
  return sendJson<StockCheck>(endpoint, "POST", {
    batch_id: input.batchId,
    actual_quantity: input.actualQuantity,
    gain_basis: input.gainBasis,
    loss_basis: input.lossBasis,
    submitted_by: input.submittedBy
  });
}

/** 复核通过：后端把批次数量调整为实盘数；重复/并发审批只有一次成功。 */
export async function approveStockCheck(id: number, input: StockCheckReviewInput = {}): Promise<StockCheck> {
  return sendJson<StockCheck>(`${endpoint}/${id}/approve`, "POST", {
    reviewer: input.reviewer,
    comment: input.comment
  });
}

/** 复核驳回：差异单留存，批次数量不变。 */
export async function rejectStockCheck(id: number, input: StockCheckReviewInput = {}): Promise<StockCheck> {
  return sendJson<StockCheck>(`${endpoint}/${id}/reject`, "POST", {
    reviewer: input.reviewer,
    comment: input.comment
  });
}

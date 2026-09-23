import type { StockCheck, StockCheckSubmitInput } from "../types/StockCheck";

/** 差异复核弹窗的空白表单 */
export const createStockCheckForm = (
  batchId: number,
  overrides: Partial<StockCheckSubmitInput> = {}
): StockCheckSubmitInput => ({
  batchId,
  actualQuantity: 0,
  gainBasis: "",
  lossBasis: "",
  submittedBy: "仓库员",
  ...overrides
});

export const createDefaultStockCheck = (overrides: Partial<StockCheck> = {}): StockCheck => ({
  id: 0,
  batch_id: 0,
  snapshot_quantity: 0,
  actual_quantity: 0,
  variance: 0,
  variance_type: "MATCHED",
  status: "PENDING",
  ...overrides
});

export const createStockCheckResponse = createDefaultStockCheck;

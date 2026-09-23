import type { StocktakeOrder } from "../types/StocktakeOrder";

/** 新建盘点表单的默认结构，页面与 store 不得散写空表单。 */
export const createDefaultStocktakeOrder = (overrides: Partial<StocktakeOrder> = {}): StocktakeOrder => ({
  id: 0,
  batch_id: 0,
  batch_no: "",
  warehouse_id: 0,
  supply_item_id: 0,
  book_quantity: 0,
  actual_quantity: 0,
  variance_quantity: 0,
  variance: "MATCH",
  status: "PENDING",
  reason: null,
  submitted_by: null,
  submitted_at: null,
  reviewed_by: null,
  review_note: null,
  reviewed_at: null,
  ...overrides
});

export const createStocktakeForm = (
  batchId: number,
  bookQuantity: number,
  overrides: Partial<StocktakeOrder> = {}
): StocktakeOrder =>
  createDefaultStocktakeOrder({
    batch_id: batchId,
    book_quantity: bookQuantity,
    actual_quantity: bookQuantity,
    ...overrides
  });

export const createStocktakeResponse = createDefaultStocktakeOrder;

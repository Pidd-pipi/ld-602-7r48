/** 库存批次盘点差异复核单（字段与后端 StocktakeOrderDtoFactory 对齐）。 */
export interface StocktakeOrder {
  id: number;
  batch_id: number;
  batch_no: string;
  warehouse_id: number;
  supply_item_id: number;
  /** 提交时账面数量快照。 */
  book_quantity: number;
  /** 仓库员录入的实盘数。 */
  actual_quantity: number;
  /** 实盘 - 账面：正盘盈，负盘亏，零一致。 */
  variance_quantity: number;
  variance: StocktakeVarianceValue;
  status: StocktakeStatusValue;
  /** 盘盈 / 盘亏依据。 */
  reason: string | null;
  submitted_by: string | null;
  submitted_at: string | null;
  reviewed_by: string | null;
  review_note: string | null;
  reviewed_at: string | null;
}

export type StocktakeStatusValue = "PENDING" | "APPROVED" | "REJECTED";
export type StocktakeVarianceValue = "SURPLUS" | "LOSS" | "MATCH";

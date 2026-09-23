/** 库存批次盘点单：差异保留待审，复核通过后批次数量才调整为实盘数。 */
export interface StockCheck {
  id: number;
  batch_id: number;
  snapshot_quantity: number;
  actual_quantity: number;
  /** 差异 = 实盘数 - 账面数：正盘盈，负盘亏，0 无差异 */
  variance: number;
  /** SURPLUS 盘盈 / LOSS 盘亏 / MATCHED 无差异 */
  variance_type: "SURPLUS" | "LOSS" | "MATCHED";
  gain_basis?: string | null;
  loss_basis?: string | null;
  /** PENDING 待审 / APPROVED 通过 / REJECTED 驳回 */
  status: "PENDING" | "APPROVED" | "REJECTED";
  submitted_by?: string;
  submitted_at?: string;
  reviewed_by?: string | null;
  reviewed_at?: string | null;
  review_comment?: string | null;
  created_at?: string;
  // 批次联查回填
  batch_no?: string;
  warehouse_id?: number;
  warehouse_name?: string;
  supply_item_id?: number;
  supply_item_name?: string;
  unit?: string;
  quality_status?: string;
  expire_at?: string;
  current_quantity?: number;
  frozen?: boolean;
  expired?: boolean;
}

export interface StockCheckSubmitInput {
  batchId: number;
  actualQuantity: number;
  gainBasis?: string;
  lossBasis?: string;
  submittedBy?: string;
}

export interface StockCheckReviewInput {
  reviewer?: string;
  comment?: string;
}

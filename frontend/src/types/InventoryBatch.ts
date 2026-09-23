export interface InventoryBatch {
  id: number;
  warehouse_id: number;
  supply_item_id: number;
  batch_no: string;
  quantity: number;
  expire_at: string;
  inbound_source: string;
  /** NORMAL / FROZEN / EXPIRED，盘盈不得回补后两者。 */
  quality_status: string;
}

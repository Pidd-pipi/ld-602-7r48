export interface InventoryBatch {
  id: number;
  warehouse_id: number;
  supply_item_id: number;
  batch_no: string;
  quantity: number;
  expire_at: string;
  inbound_source: string;
  quality_status: string;
  // 后端联查回填字段
  warehouse_name?: string;
  supply_item_name?: string;
  unit?: string;
  frozen?: boolean;
  expired?: boolean;
  pending_check_id?: number | null;
}

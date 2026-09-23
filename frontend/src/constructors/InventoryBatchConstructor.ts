import type { InventoryBatch } from "../types/InventoryBatch";

export const createDefaultInventoryBatch = (overrides: Partial<InventoryBatch> = {}): InventoryBatch => ({
  id: 1,
  warehouse_id: 1,
  supply_item_id: 1,
  batch_no: "W2026-0511-瓶装水",
  quantity: 120,
  expire_at: "2027-06-11T09:00:00Z",
  inbound_source: "市级应急采购入库",
  quality_status: "NORMAL",
  ...overrides
});

export const createInventoryBatchForm = createDefaultInventoryBatch;
export const createInventoryBatchResponse = createDefaultInventoryBatch;

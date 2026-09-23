import type { InventoryBatch } from "../types/InventoryBatch";

export const createDefaultInventoryBatch = (overrides: Partial<InventoryBatch> = {}): InventoryBatch => ({
  id: 1,
  warehouse_id: 1,
  supply_item_id: 1,
  batch_no: "batch no 1",
  quantity: 92,
  expire_at: "2026-06-11",
  inbound_source: "市级采购入库",
  quality_status: "QUALIFIED",
  ...overrides
});

export const createInventoryBatchForm = createDefaultInventoryBatch;
export const createInventoryBatchResponse = createDefaultInventoryBatch;

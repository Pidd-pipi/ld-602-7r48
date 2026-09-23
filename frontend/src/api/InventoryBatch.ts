import { getJson } from "./http";
import { mockData } from "../mocks/seedData";
import type { InventoryBatch } from "../types/InventoryBatch";

const endpoint = "/api/inventory-batch";

export async function listInventoryBatch(warehouseId?: number): Promise<InventoryBatch[]> {
  try {
    const query = warehouseId ? `?warehouseId=${warehouseId}` : "";
    return await getJson<InventoryBatch[]>(`${endpoint}${query}`);
  } catch {
    // Local mock fallback keeps the UI available during offline review.
    return [...(mockData.inventoryBatch as unknown as InventoryBatch[])];
  }
}

export async function saveInventoryBatch(payload: InventoryBatch) {
  console.info("save InventoryBatch", payload);
  return payload;
}

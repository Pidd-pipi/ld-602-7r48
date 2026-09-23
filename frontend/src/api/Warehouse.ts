import { getJson } from "./http";
import { mockData } from "../mocks/seedData";
import type { Warehouse } from "../types/Warehouse";

const endpoint = "/api/warehouse";

export async function listWarehouse(): Promise<Warehouse[]> {
  try {
    return await getJson<Warehouse[]>(endpoint);
  } catch {
    // Local mock fallback keeps the UI available during offline review.
    return [...(mockData.warehouse as unknown as Warehouse[])];
  }
}

export async function saveWarehouse(payload: Warehouse) {
  console.info("save Warehouse", payload);
  return payload;
}

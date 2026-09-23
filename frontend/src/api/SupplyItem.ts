import { getJson } from "./http";
import { mockData } from "../mocks/seedData";
import type { SupplyItem } from "../types/SupplyItem";

const endpoint = "/api/supply-item";

export async function listSupplyItem(): Promise<SupplyItem[]> {
  try {
    return await getJson<SupplyItem[]>(endpoint);
  } catch {
    // Local mock fallback keeps the UI available during offline review.
    return [...(mockData.supplyItem as unknown as SupplyItem[])];
  }
}

export async function saveSupplyItem(payload: SupplyItem) {
  console.info("save SupplyItem", payload);
  return payload;
}

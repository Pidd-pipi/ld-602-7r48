import type { InventoryBatch } from "../types/InventoryBatch";
import { mockData } from "../mocks/seedData";
import { readStore, writeStore } from "../utils/localStore";

const endpoint = "/api/inventory-batch";
const BATCH_STORE_KEY = "inventoryBatch:seed";

/** 首次离线使用时以种子数据初始化本地批次（质量状态为 NORMAL/FROZEN/EXPIRED）。 */
export function localSeedBatches(): InventoryBatch[] {
  const existing = readStore<InventoryBatch[] | null>(BATCH_STORE_KEY, null);
  if (existing && existing.length > 0) return existing;
  const seed = mockData.inventoryBatch.map((row) => ({ ...row })) as unknown as InventoryBatch[];
  writeStore(BATCH_STORE_KEY, seed);
  return seed;
}

export async function listInventoryBatch(): Promise<InventoryBatch[]> {
  try {
    const res = await fetch(endpoint);
    if (res.ok) return await res.json();
  } catch {
    // Local mock fallback keeps the UI available during offline review.
  }
  return localSeedBatches();
}

export async function saveInventoryBatch(payload: InventoryBatch) {
  console.info("save InventoryBatch", payload);
  return payload;
}

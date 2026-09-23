import { defineStore } from "pinia";
import { listInventoryBatch } from "../api/InventoryBatch";
import type { InventoryBatch } from "../types/InventoryBatch";

export const useInventoryBatchStore = defineStore("inventoryBatch", {
  state: () => ({ rows: [] as InventoryBatch[], loading: false }),
  actions: {
    async load(warehouseId?: number) {
      this.loading = true;
      try {
        this.rows = await listInventoryBatch(warehouseId);
      } finally {
        this.loading = false;
      }
    }
  }
});

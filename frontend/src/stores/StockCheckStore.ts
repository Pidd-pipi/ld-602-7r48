import { defineStore } from "pinia";
import {
  approveStockCheck,
  listStockCheck,
  rejectStockCheck,
  submitStockCheck
} from "../api/StockCheck";
import type { StockCheck, StockCheckReviewInput, StockCheckSubmitInput } from "../types/StockCheck";

interface StockCheckFilter {
  batchId?: number;
  status?: string;
}

/**
 * 盘点差异复核 store：提交、复核通过、驳回。
 * 任何写操作成功后重新拉取，批次与差异单始终与后端一致；
 * 冲突（重复提交/重复审批/并发审批）由后端拒绝并原样抛出。
 */
export const useStockCheckStore = defineStore("stockCheck", {
  state: () => ({
    rows: [] as StockCheck[],
    loading: false,
    acting: false,
    error: ""
  }),
  getters: {
    pending: (state) => state.rows.filter((row) => row.status === "PENDING"),
    pendingBatchIds(): Set<number> {
      return new Set(
        this.pending.map((row: StockCheck) => row.batch_id)
      );
    }
  },
  actions: {
    async load(filter: StockCheckFilter = {}) {
      this.loading = true;
      try {
        this.rows = await listStockCheck(filter);
        this.error = "";
      } finally {
        this.loading = false;
      }
    },
    async submit(input: StockCheckSubmitInput) {
      this.acting = true;
      try {
        const created = await submitStockCheck(input);
        await this.load();
        return created;
      } finally {
        this.acting = false;
      }
    },
    async approve(id: number, input: StockCheckReviewInput = {}) {
      this.acting = true;
      try {
        const updated = await approveStockCheck(id, input);
        await this.load();
        return updated;
      } finally {
        this.acting = false;
      }
    },
    async reject(id: number, input: StockCheckReviewInput = {}) {
      this.acting = true;
      try {
        const updated = await rejectStockCheck(id, input);
        await this.load();
        return updated;
      } finally {
        this.acting = false;
      }
    },
    clearError() {
      this.error = "";
    }
  }
});

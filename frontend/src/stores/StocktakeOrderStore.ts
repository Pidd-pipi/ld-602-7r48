import { defineStore } from "pinia";
import {
  listStocktakeOrder,
  submitStocktakeOrder,
  reviewStocktakeOrder
} from "../api/StocktakeOrder";
import type { StocktakeOrder } from "../types/StocktakeOrder";
import type {
  StocktakeSubmitRequest,
  StocktakeReviewRequest
} from "../api/StocktakeOrder";

export const useStocktakeOrderStore = defineStore("stocktakeOrder", {
  state: () => ({
    rows: [] as StocktakeOrder[],
    loading: false,
    errorCode: "" as string
  }),
  getters: {
    pendingRows: (state) => state.rows.filter((row) => row.status === "PENDING"),
    pendingByBatch: (state) => {
      const map = new Map<number, StocktakeOrder>();
      for (const row of state.rows) {
        if (row.status === "PENDING") map.set(row.batch_id, row);
      }
      return map;
    }
  },
  actions: {
    async load(batchId?: number) {
      this.loading = true;
      this.errorCode = "";
      try {
        this.rows = await listStocktakeOrder(batchId);
      } finally {
        this.loading = false;
      }
    },
    /** 仓库员发起盘点；失败时清空错误并抛出，由页面提示，批次与盘点单保持不变。 */
    async submit(payload: StocktakeSubmitRequest) {
      const created = await submitStocktakeOrder(payload);
      await this.load();
      return created;
    },
    /** 审批员复核；重复 / 并发审批只成功一次，失败后重新回读服务端状态。 */
    async review(id: number, payload: StocktakeReviewRequest) {
      const reviewed = await reviewStocktakeOrder(id, payload);
      await this.load();
      return reviewed;
    }
  }
});

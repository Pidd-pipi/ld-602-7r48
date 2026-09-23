import { computed, ref } from "vue";
import type { StocktakeOrder } from "../types/StocktakeOrder";
import type { StocktakeReviewRequest, StocktakeSubmitRequest } from "../api/StocktakeOrder";
import { StocktakeApiError, submitStocktakeOrder, reviewStocktakeOrder } from "../api/StocktakeOrder";

/**
 * 盘点差异复核流程：提交差异保留待审、复核通过才落账。
 * 提交 / 审批失败（重复提交、重复审批、盘盈回补冻结批次）时透出错误码，
 * 由调用方刷新回读，保证页面展示的批次与盘点单不被本地乐观更新污染。
 */
export function useStocktakeReview() {
  const submitting = ref(false);
  const reviewing = ref(false);
  const lastErrorCode = ref("");
  const lastOrder = ref<StocktakeOrder | null>(null);

  const busy = computed(() => submitting.value || reviewing.value);

  async function submit(payload: StocktakeSubmitRequest): Promise<StocktakeOrder | null> {
    submitting.value = true;
    lastErrorCode.value = "";
    try {
      lastOrder.value = await submitStocktakeOrder(payload);
      return lastOrder.value;
    } catch (error) {
      lastErrorCode.value = error instanceof StocktakeApiError ? error.code : "VALIDATION_FAILED";
      return null;
    } finally {
      submitting.value = false;
    }
  }

  async function review(id: number, payload: StocktakeReviewRequest): Promise<StocktakeOrder | null> {
    reviewing.value = true;
    lastErrorCode.value = "";
    try {
      lastOrder.value = await reviewStocktakeOrder(id, payload);
      return lastOrder.value;
    } catch (error) {
      lastErrorCode.value = error instanceof StocktakeApiError ? error.code : "VALIDATION_FAILED";
      return null;
    } finally {
      reviewing.value = false;
    }
  }

  return { submitting, reviewing, busy, lastErrorCode, lastOrder, submit, review };
}

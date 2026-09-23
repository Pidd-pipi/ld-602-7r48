<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import type { InventoryBatch } from "../types/InventoryBatch";
import { useInventoryBatchStore } from "../stores/InventoryBatchStore";
import { useStocktakeOrderStore } from "../stores/StocktakeOrderStore";
import { useStocktakeReview } from "../hooks/useStocktakeReview";
import type { StocktakeSubmitRequest, StocktakeReviewRequest } from "../api/StocktakeOrder";
import { ERROR_MESSAGES } from "../constants/errorMessages";
import BatchTable from "../components/common/BatchTable.vue";
import StocktakeFormDialog from "../components/common/StocktakeFormDialog.vue";
import StocktakeReviewPanel from "../components/common/StocktakeReviewPanel.vue";

const batchStore = useInventoryBatchStore();
const stocktakeStore = useStocktakeOrderStore();
const { submitting, reviewing, lastErrorCode, submit, review } = useStocktakeReview();

const selectedBatch = ref<InventoryBatch | null>(null);
const warehouseFilter = ref<number | "ALL">("ALL");
const pageNotice = ref("");

const filteredBatches = computed(() =>
  warehouseFilter.value === "ALL"
    ? batchStore.rows
    : batchStore.rows.filter((batch) => batch.warehouse_id === warehouseFilter.value)
);

const warehouseOptions = computed(() =>
  Array.from(new Set(batchStore.rows.map((batch) => batch.warehouse_id))).sort((a, b) => a - b)
);

const pendingByBatch = computed(() => stocktakeStore.pendingByBatch);
const selectedPending = computed(() =>
  selectedBatch.value ? pendingByBatch.value.has(selectedBatch.value.id) : false
);
const reviewingId = ref<number | null>(null);
const dialogErrorCode = ref("");

async function refreshAll() {
  // 先回读盘点单再回读批次：审批通过后批次数量已是实盘数。
  await stocktakeStore.load();
  await batchStore.load();
}

function openStocktake(batch: InventoryBatch) {
  pageNotice.value = "";
  dialogErrorCode.value = "";
  selectedBatch.value = batch;
}

function closeDialog() {
  selectedBatch.value = null;
  dialogErrorCode.value = "";
}

async function handleSubmit(payload: StocktakeSubmitRequest) {
  pageNotice.value = "";
  dialogErrorCode.value = "";
  const created = await submit(payload);
  if (created) {
    pageNotice.value = `盘点单 #${created.id} 已提交，差异保留待审，批次数量未调整。`;
    selectedBatch.value = null;
    await stocktakeStore.load();
  } else {
    dialogErrorCode.value = lastErrorCode.value;
  }
}

async function handleReview(id: number, payload: StocktakeReviewRequest) {
  reviewingId.value = id;
  pageNotice.value = "";
  const reviewed = await review(id, payload);
  reviewingId.value = null;
  if (reviewed) {
    pageNotice.value =
      reviewed.status === "APPROVED"
        ? `盘点单 #${reviewed.id} 审批通过，批次数量已调整为实盘数 ${reviewed.actual_quantity}。`
        : `盘点单 #${reviewed.id} 已驳回，批次数量保持不变。`;
  } else {
    pageNotice.value = ERROR_MESSAGES[lastErrorCode.value as keyof typeof ERROR_MESSAGES] ?? lastErrorCode.value;
  }
  // 无论成功失败都从数据源重新回读，确保并发 / 重复审批后视图与服务端一致。
  await refreshAll();
}

onMounted(refreshAll);
</script>

<template>
  <section class="warehouse-page">
    <div class="panel">
      <div class="panel-head">
        <h2>仓库库存批次</h2>
        <label class="filter-line">
          仓库：
          <select v-model="warehouseFilter">
            <option value="ALL">全部仓库</option>
            <option v-for="id in warehouseOptions" :key="id" :value="id">#{{ id }}</option>
          </select>
        </label>
      </div>
      <BatchTable
        :rows="filteredBatches"
        :pending-by-batch="pendingByBatch"
        @stocktake="openStocktake"
      />
      <p class="rule-tip">规则：差异提交后保留待审，审批通过才调整批次数量；盘盈不得回补质检冻结或已过期批次，盘亏可照常提交。</p>
    </div>

    <div v-if="pageNotice" class="notice" :class="lastErrorCode ? 'notice-error' : 'notice-ok'">
      {{ pageNotice }}
    </div>

    <StocktakeReviewPanel
      :rows="stocktakeStore.rows"
      :loading="stocktakeStore.loading"
      :reviewing-id="reviewingId"
      error-code=""
      @refresh="refreshAll"
      @review="handleReview"
    />

    <StocktakeFormDialog
      :batch="selectedBatch"
      :pending="selectedPending"
      :submitting="submitting"
      :error-code="dialogErrorCode ? ERROR_MESSAGES[dialogErrorCode as keyof typeof ERROR_MESSAGES] ?? dialogErrorCode : ''"
      @close="closeDialog"
      @submit="handleSubmit"
    />
  </section>
</template>

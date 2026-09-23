<script setup lang="ts">
import { ref } from "vue";
import type { StocktakeOrder } from "../../types/StocktakeOrder";
import type { StocktakeReviewRequest } from "../../api/StocktakeOrder";
import StatusBadge from "./StatusBadge.vue";
import {
  formatDate,
  formatNumber,
  formatStocktakeStatus,
  formatStocktakeVariance,
  formatVarianceQuantity
} from "../../utils/formatters";

const props = defineProps<{
  rows: StocktakeOrder[];
  loading: boolean;
  reviewingId: number | null;
  errorCode: string;
}>();

const emit = defineEmits<{
  (event: "refresh"): void;
  (event: "review", id: number, payload: StocktakeReviewRequest): void;
}>();

const reviewNote = ref("");
const reviewer = ref("approver");

function statusClass(row: StocktakeOrder): string {
  if (row.status === "APPROVED") return "badge badge-approved";
  if (row.status === "REJECTED") return "badge badge-rejected";
  return "badge badge-pending";
}

function varianceClass(row: StocktakeOrder): string {
  if (row.variance === "SURPLUS") return "text-surplus";
  if (row.variance === "LOSS") return "text-loss";
  return "";
}

function sendReview(row: StocktakeOrder, approve: boolean) {
  emit("review", row.id, {
    approve,
    reviewNote: reviewNote.value,
    reviewedBy: reviewer.value
  });
  if (approve || reviewNote.value.trim()) reviewNote.value = "";
}
</script>

<template>
  <div class="shared-widget stocktake-panel">
    <div class="panel-head">
      <strong class="widget-title">盘点差异复核</strong>
      <button class="btn btn-small" type="button" :disabled="loading" @click="emit('refresh')">
        {{ loading ? "刷新中…" : "刷新回读" }}
      </button>
    </div>

    <p v-if="rows.length === 0" class="empty-tip">暂无盘点单，可在批次列表中发起盘点。</p>

    <article v-for="row in rows" :key="row.id" class="stocktake-card">
      <header>
        <div>
          <strong>#{{ row.id }} · {{ row.batch_no }}</strong>
          <span :class="statusClass(row)">{{ formatStocktakeStatus(row.status) }}</span>
          <span class="badge">{{ formatStocktakeVariance(row.variance) }}</span>
        </div>
        <div class="variance-line">
          账面 {{ formatNumber(row.book_quantity) }} → 实盘 {{ formatNumber(row.actual_quantity) }}
          <strong :class="varianceClass(row)">（{{ formatVarianceQuantity(row.variance_quantity) }}）</strong>
        </div>
      </header>

      <dl class="stocktake-meta">
        <div><dt>盘盈 / 盘亏依据</dt><dd>{{ row.reason ?? "—" }}</dd></div>
        <div><dt>盘点人 / 时间</dt><dd>{{ row.submitted_by ?? "—" }} · {{ formatDate(row.submitted_at) }}</dd></div>
        <div v-if="row.status !== 'PENDING'">
          <dt>复核意见</dt>
          <dd>{{ row.review_note ?? "—" }}（{{ row.reviewed_by ?? "—" }} · {{ formatDate(row.reviewed_at) }}）</dd>
        </div>
      </dl>

      <div v-if="row.status === 'PENDING'" class="review-box">
        <StatusBadge value="PENDING_APPROVAL" />
        <textarea
          v-model="reviewNote"
          rows="2"
          placeholder="复核意见（必填）：通过则按实盘数落账，驳回则账面不变"
        ></textarea>
        <div class="review-actions">
          <input v-model="reviewer" type="text" placeholder="审批人" />
          <button
            class="btn btn-small btn-danger"
            type="button"
            :disabled="reviewingId === row.id || !reviewNote.trim()"
            @click="sendReview(row, false)"
          >驳回</button>
          <button
            class="btn btn-small btn-primary"
            type="button"
            :disabled="reviewingId === row.id || !reviewNote.trim()"
            @click="sendReview(row, true)"
          >
            {{ reviewingId === row.id ? "复核中…" : "审批通过并调整数量" }}
          </button>
        </div>
      </div>
    </article>

    <div v-if="errorCode" class="notice notice-error">{{ errorCode }}</div>
  </div>
</template>

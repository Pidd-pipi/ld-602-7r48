<script setup lang="ts">
import { computed, ref } from "vue";
import type { StockCheck } from "../../types/StockCheck";
import { useStockCheckStore } from "../../stores/StockCheckStore";
import StatusBadge from "./StatusBadge.vue";
import EmptyState from "./EmptyState.vue";
import {
  StockCheckStatusText,
  VarianceTypeText,
  type StockCheckStatus
} from "../../constants/StockCheck";

const props = defineProps<{
  checks: StockCheck[];
  loading?: boolean;
  title?: string;
  /** 只看待审时隐藏状态筛选 */
  pendingOnly?: boolean;
}>();

const emit = defineEmits<{
  (event: "reviewed"): void;
}>();

const store = useStockCheckStore();
const statusFilter = ref<"" | StockCheckStatus>("");
const reviewingId = ref<number | null>(null);
const reviewer = ref("审批员");
const comment = ref("");
const actionError = ref("");

const filters = [
  { value: "", label: "全部" },
  { value: "PENDING", label: "待审" },
  { value: "APPROVED", label: "通过" },
  { value: "REJECTED", label: "驳回" }
] as const;

const rows = computed(() =>
  statusFilter.value ? props.checks.filter((row) => row.status === statusFilter.value) : props.checks
);

function statusTone(status: string): "amber" | "green" | "red" {
  return status === "PENDING" ? "amber" : status === "APPROVED" ? "green" : "red";
}
function varianceTone(type: string): "blue" | "red" | "gray" {
  return type === "SURPLUS" ? "blue" : type === "LOSS" ? "red" : "gray";
}

function startReview(row: StockCheck) {
  reviewingId.value = row.id;
  reviewer.value = "审批员";
  comment.value = "";
  actionError.value = "";
}

async function decide(row: StockCheck, approved: boolean) {
  actionError.value = "";
  try {
    if (approved) {
      await store.approve(row.id, { reviewer: reviewer.value.trim() || "审批员", comment: comment.value.trim() });
    } else {
      await store.reject(row.id, { reviewer: reviewer.value.trim() || "审批员", comment: comment.value.trim() });
    }
    reviewingId.value = null;
    emit("reviewed");
  } catch (error) {
    // 重复/并发审批被后端拒绝：批次与盘点单不变，提示后刷新回读
    actionError.value = error instanceof Error ? error.message : "复核失败，请刷新后重试";
    emit("reviewed");
  }
}
</script>

<template>
  <div class="panel-block">
    <div class="panel-head">
      <h3>{{ title ?? "盘点差异复核" }}</h3>
      <div v-if="!pendingOnly" class="filters">
        <button
          v-for="item in filters"
          :key="item.value"
          class="chip"
          :class="{ active: statusFilter === item.value }"
          @click="statusFilter = item.value as typeof statusFilter"
        >{{ item.label }}</button>
      </div>
    </div>

    <p v-if="actionError" class="error-banner">{{ actionError }}</p>

    <div v-if="loading" class="muted">加载差异单…</div>
    <EmptyState v-else-if="rows.length === 0" />

    <ul v-else class="check-list">
      <li v-for="row in rows" :key="row.id" class="check-item" :class="{ pending: row.status === 'PENDING' }">
        <div class="check-main">
          <div class="check-title">
            <strong>{{ row.batch_no ?? `批次#${row.batch_id}` }}</strong>
            <span class="muted">{{ row.supply_item_name ?? "—" }} · {{ row.warehouse_name ?? "—" }}</span>
            <StatusBadge
              :value="row.status"
              :text="StockCheckStatusText[row.status]"
              :tone="statusTone(row.status)"
            />
            <StatusBadge
              :value="row.variance_type"
              :text="`${VarianceTypeText[row.variance_type]} ${row.variance > 0 ? '+' : ''}${row.variance}`"
              :tone="varianceTone(row.variance_type)"
            />
          </div>
          <div class="numbers">
            <span>账面 <strong>{{ row.snapshot_quantity }}</strong></span>
            <span class="arrow">→</span>
            <span>实盘 <strong>{{ row.actual_quantity }}</strong></span>
            <span v-if="row.status === 'APPROVED'" class="muted">现库存 {{ row.current_quantity }}</span>
          </div>
          <div class="basis">
            <span v-if="row.gain_basis" class="tag blue">盘盈依据：{{ row.gain_basis }}</span>
            <span v-if="row.loss_basis" class="tag red">盘亏依据：{{ row.loss_basis }}</span>
          </div>
          <div class="meta muted">
            {{ row.submitted_by }} 提交于 {{ row.submitted_at?.replace("T", " ").slice(0, 16) }}
            <template v-if="row.reviewed_at">
              · {{ row.reviewed_by }} {{ row.status === "APPROVED" ? "通过" : "驳回" }}于
              {{ row.reviewed_at.replace("T", " ").slice(0, 16) }}
            </template>
            <template v-if="row.review_comment"> · 意见：{{ row.review_comment }}</template>
          </div>
        </div>

        <div class="check-actions">
          <template v-if="row.status === 'PENDING' && reviewingId !== row.id">
            <button class="btn primary" @click="startReview(row)">复核</button>
          </template>
          <template v-else-if="reviewingId === row.id">
            <input v-model="reviewer" class="mini-input" placeholder="复核人" />
            <input v-model="comment" class="mini-input grow" placeholder="复核意见（可选）" />
            <button class="btn danger" :disabled="store.acting" @click="decide(row, false)">驳回</button>
            <button class="btn primary" :disabled="store.acting" @click="decide(row, true)">
              {{ store.acting ? "提交中…" : "通过并调账" }}
            </button>
          </template>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.panel-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 12px; flex-wrap: wrap; }
h3 { margin: 0; font-size: 16px; }
.filters { display: flex; gap: 6px; }
.chip { padding: 4px 12px; border-radius: 999px; border: 1px solid #c9c4b2; background: transparent; color: #596257; font-size: 12px; }
.chip.active { background: #274335; color: #f5f1e6; border-color: #274335; }
.check-list { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
.check-item { display: flex; justify-content: space-between; gap: 14px; border: 1px solid #d8d6c8; border-left-width: 4px; border-left-color: #d8d6c8; border-radius: 8px; padding: 12px 14px; background: #fbfaf4; }
.check-item.pending { border-left-color: #d39b46; }
.check-title { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 6px; }
.numbers { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #596257; }
.numbers strong { font-size: 16px; color: #274335; font-variant-numeric: tabular-nums; }
.arrow { color: #a9ada3; }
.basis { display: flex; flex-wrap: wrap; gap: 8px; margin: 8px 0 4px; }
.tag { font-size: 12px; border-radius: 6px; padding: 3px 8px; }
.tag.blue { background: #dce8f2; color: #1f4a6b; }
.tag.red { background: #f6dddd; color: #8a2b26; }
.meta { font-size: 12px; }
.muted { color: #8a8f84; font-size: 12px; }
.check-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; justify-content: flex-end; }
.mini-input { border: 1px solid #c9c4b2; border-radius: 6px; padding: 7px 9px; font: inherit; min-width: 90px; }
.mini-input.grow { min-width: 160px; }
.btn { border-radius: 6px; padding: 8px 14px; font-weight: 700; font-size: 13px; }
.btn.primary { background: #274335; color: #f5f1e6; }
.btn.danger { background: #fff; color: #8a2b26; border: 1px solid #d9a7a2; }
.btn:disabled { opacity: .55; cursor: not-allowed; }
.error-banner { background: #f6dddd; color: #8a2b26; padding: 9px 12px; border-radius: 6px; font-size: 13px; margin: 0 0 10px; }
</style>

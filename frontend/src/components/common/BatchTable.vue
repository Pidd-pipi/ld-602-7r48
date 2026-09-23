<script setup lang="ts">
import { computed } from "vue";
import type { InventoryBatch } from "../../types/InventoryBatch";
import StatusBadge from "./StatusBadge.vue";
import EmptyState from "./EmptyState.vue";
import { QualityStatusText } from "../../constants/StockCheck";

const props = defineProps<{
  title?: string;
  batches: InventoryBatch[];
  /** 存在待审盘点的批次：禁用“发起盘点” */
  pendingBatchIds?: Set<number>;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (event: "create-check", batch: InventoryBatch): void;
}>();

const rows = computed(() => props.batches);

function hasPending(batch: InventoryBatch): boolean {
  return Boolean(batch.pending_check_id) || props.pendingBatchIds?.has(batch.id) === true;
}
</script>

<template>
  <div class="batch-card">
    <h3 v-if="title">{{ title }}</h3>
    <div class="table-wrap">
      <table class="batch-table">
        <thead>
          <tr>
            <th>批次号</th>
            <th>物资</th>
            <th>仓库</th>
            <th class="num">账面数量</th>
            <th>到期日</th>
            <th>质检状态</th>
            <th>盘点</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="batch in rows" :key="batch.id">
            <td><strong>{{ batch.batch_no }}</strong><span class="sub">{{ batch.inbound_source }}</span></td>
            <td>{{ batch.supply_item_name ?? `物资#${batch.supply_item_id}` }}</td>
            <td>{{ batch.warehouse_name ?? `仓库#${batch.warehouse_id}` }}</td>
            <td class="num">{{ batch.quantity }} {{ batch.unit ?? "" }}</td>
            <td>
              {{ batch.expire_at?.slice(0, 10) ?? "—" }}
              <StatusBadge v-if="batch.expired" value="EXPIRED" text="已过期" tone="red" />
            </td>
            <td>
              <StatusBadge
                :value="batch.quality_status"
                :text="QualityStatusText[batch.quality_status as keyof typeof QualityStatusText] ?? batch.quality_status"
                :tone="batch.frozen ? 'amber' : 'green'"
              />
            </td>
            <td>
              <StatusBadge v-if="hasPending(batch)" value="PENDING" text="待审中" tone="amber" />
              <button
                v-else
                class="link-btn"
                :disabled="disabled"
                @click="emit('create-check', batch)"
              >发起盘点</button>
            </td>
          </tr>
          <tr v-if="rows.length === 0">
            <td colspan="7"><EmptyState /></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.batch-card h3 { margin: 0 0 10px; font-size: 16px; }
.table-wrap { overflow-x: auto; }
.batch-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.batch-table th, .batch-table td { padding: 10px 12px; border-bottom: 1px solid #e4e0d3; text-align: left; white-space: nowrap; }
.batch-table th { color: #596257; font-weight: 700; background: #f4f2e9; }
.batch-table .num { text-align: right; font-variant-numeric: tabular-nums; }
.sub { display: block; color: #8a8f84; font-size: 12px; }
.link-btn { color: #274335; font-weight: 800; background: transparent; padding: 4px 8px; border-radius: 6px; }
.link-btn:hover:not(:disabled) { background: #e4efe4; }
.link-btn:disabled { color: #a9ada3; cursor: not-allowed; }
</style>

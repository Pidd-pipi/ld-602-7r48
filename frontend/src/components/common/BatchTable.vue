<script setup lang="ts">
import { computed } from "vue";
import type { InventoryBatch } from "../../types/InventoryBatch";
import type { StocktakeOrder } from "../../types/StocktakeOrder";
import StatusBadge from "./StatusBadge.vue";
import { formatDate, formatNumber, formatQualityStatus } from "../../utils/formatters";

const props = defineProps<{
  rows: InventoryBatch[];
  pendingByBatch?: Map<number, StocktakeOrder>;
  title?: string;
}>();

const emit = defineEmits<{
  (event: "stocktake", batch: InventoryBatch): void;
}>();

const pendingByBatch = computed(() => props.pendingByBatch ?? new Map<number, StocktakeOrder>());

function qualityClass(value: string): string {
  if (value === "FROZEN") return "badge badge-frozen";
  if (value === "EXPIRED") return "badge badge-expired";
  return "badge";
}

function startStocktake(batch: InventoryBatch) {
  // 待审期间禁止再次发起，由父组件统一弹窗，组件自身不改数据。
  emit("stocktake", batch);
}
</script>

<template>
  <div class="shared-widget batch-table">
    <strong class="widget-title">{{ title ?? "库存批次" }}</strong>
    <table>
      <thead>
        <tr>
          <th>批次号</th>
          <th>仓库</th>
          <th>账面数量</th>
          <th>质检状态</th>
          <th>到期时间</th>
          <th>来源</th>
          <th>盘点</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="batch in rows" :key="batch.id">
          <td>{{ batch.batch_no }}</td>
          <td>#{{ batch.warehouse_id }}</td>
          <td>{{ formatNumber(batch.quantity) }}</td>
          <td><span :class="qualityClass(batch.quality_status)">{{ formatQualityStatus(batch.quality_status) }}</span></td>
          <td>{{ formatDate(batch.expire_at) }}</td>
          <td>{{ batch.inbound_source }}</td>
          <td>
            <template v-if="pendingByBatch.has(batch.id)">
              <StatusBadge value="PENDING_STOCKTAKE" />
            </template>
            <button class="btn btn-small" type="button" @click="startStocktake(batch)">发起盘点</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

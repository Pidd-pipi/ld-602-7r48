<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { storeToRefs } from "pinia";
import { useInventoryBatchStore } from "../stores/InventoryBatchStore";
import { useStockCheckStore } from "../stores/StockCheckStore";
import { useWarehouseStore } from "../stores/WarehouseStore";
import type { InventoryBatch } from "../types/InventoryBatch";
import BatchTable from "../components/common/BatchTable.vue";
import StockCheckDialog from "../components/common/StockCheckDialog.vue";
import StockCheckReviewPanel from "../components/common/StockCheckReviewPanel.vue";
import StatCard from "../components/common/StatCard.vue";

const warehouseStore = useWarehouseStore();
const batchStore = useInventoryBatchStore();
const checkStore = useStockCheckStore();

const { rows: warehouses } = storeToRefs(warehouseStore);
const { rows: batches, loading: batchesLoading } = storeToRefs(batchStore);
const { rows: checks, loading: checksLoading } = storeToRefs(checkStore);

const selectedWarehouseId = ref<number | undefined>(undefined);
const dialogVisible = ref(false);
const activeBatch = ref<InventoryBatch | null>(null);

const filteredBatches = computed(() =>
  selectedWarehouseId.value
    ? batches.value.filter((batch) => batch.warehouse_id === selectedWarehouseId.value)
    : batches.value
);

const pendingBatchIds = computed(
  () => new Set(checks.value.filter((row) => row.status === "PENDING").map((row) => row.batch_id))
);
const pendingCount = computed(() => checks.value.filter((row) => row.status === "PENDING").length);
const expiredCount = computed(() => batches.value.filter((batch) => batch.expired).length);
const frozenCount = computed(() => batches.value.filter((batch) => batch.frozen).length);

async function refresh() {
  // 刷新页面后仍可回读：批次与差异单都从后端重新拉取
  await Promise.all([
    warehouseStore.load(),
    batchStore.load(),
    checkStore.load()
  ]);
}

function openDialog(batch: InventoryBatch) {
  activeBatch.value = batch;
  dialogVisible.value = true;
}

async function onSubmitted() {
  dialogVisible.value = false;
  activeBatch.value = null;
  await refresh();
}

async function onReviewed() {
  // 复核后批次数量可能已调整为实盘数，连同批次表一起回读
  await refresh();
}

onMounted(refresh);
</script>

<template>
  <section class="warehouse-page">
    <div class="toolbar">
      <div class="warehouse-tabs">
        <button
          class="chip"
          :class="{ active: selectedWarehouseId === undefined }"
          @click="selectedWarehouseId = undefined"
        >全部仓库</button>
        <button
          v-for="warehouse in warehouses"
          :key="warehouse.id"
          class="chip"
          :class="{ active: selectedWarehouseId === warehouse.id }"
          @click="selectedWarehouseId = warehouse.id"
        >{{ warehouse.name }}</button>
      </div>
      <button class="btn ghost" @click="refresh">刷新回读</button>
    </div>

    <div class="metrics">
      <StatCard label="库存批次" :value="filteredBatches.length" />
      <StatCard label="待复核盘点" :value="pendingCount" />
      <StatCard label="已过期批次" :value="expiredCount" />
      <StatCard label="质检冻结批次" :value="frozenCount" />
    </div>

    <div class="panel">
      <BatchTable
        title="批次库存（点击“发起盘点”录入实盘数）"
        :batches="filteredBatches"
        :pending-batch-ids="pendingBatchIds"
        :disabled="batchesLoading"
        @create-check="openDialog"
      />
      <p class="rule-hint">
        规则：提交后差异保留待审，复核通过才把批次数量调整为实盘数；盘盈须写明依据，且不得回补质检冻结或已过期批次；盘亏可照常提交。
      </p>
    </div>

    <div class="panel">
      <StockCheckReviewPanel
        :checks="checks"
        :loading="checksLoading"
        @reviewed="onReviewed"
      />
    </div>

    <StockCheckDialog
      :visible="dialogVisible"
      :batch="activeBatch"
      @close="dialogVisible = false"
      @submitted="onSubmitted"
    />
  </section>
</template>

<style scoped>
.warehouse-page { display: grid; gap: 16px; }
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.warehouse-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.chip { padding: 6px 14px; border-radius: 999px; border: 1px solid #c9c4b2; background: #fbfaf4; color: #596257; font-size: 13px; }
.chip.active { background: #274335; color: #f5f1e6; border-color: #274335; }
.metrics { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.panel { background: #fbfaf4; border: 1px solid #d8d6c8; border-radius: 8px; padding: 16px; }
.rule-hint { color: #7d4d18; background: #f7ead0; border-radius: 6px; padding: 8px 12px; font-size: 12px; margin: 12px 0 0; }
.btn.ghost { border: 1px solid #c9c4b2; background: #fbfaf4; color: #274335; border-radius: 6px; padding: 7px 14px; font-weight: 700; font-size: 13px; }
@media (max-width: 900px) { .metrics { grid-template-columns: repeat(2, 1fr); } }
</style>

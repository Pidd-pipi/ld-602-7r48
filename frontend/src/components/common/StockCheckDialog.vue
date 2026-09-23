<script setup lang="ts">
import { computed, ref, watch } from "vue";
import type { InventoryBatch } from "../../types/InventoryBatch";
import { useStockCheckStore } from "../../stores/StockCheckStore";
import { createStockCheckForm } from "../../constructors/StockCheckConstructor";
import StatusBadge from "./StatusBadge.vue";
import { QualityStatusText } from "../../constants/StockCheck";

const props = defineProps<{
  visible: boolean;
  batch: InventoryBatch | null;
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "submitted"): void;
}>();

const store = useStockCheckStore();
const actualText = ref("");
const gainBasis = ref("");
const lossBasis = ref("");
const submittedBy = ref("仓库员");
const formError = ref("");

watch(
  () => props.batch,
  (batch) => {
    formError.value = "";
    gainBasis.value = "";
    lossBasis.value = "";
    submittedBy.value = "仓库员";
    actualText.value = batch ? String(batch.quantity) : "";
  }
);

const actualQuantity = computed(() => Number(actualText.value));
const variance = computed(() =>
  props.batch ? actualQuantity.value - (props.batch.quantity ?? 0) : 0
);
const varianceType = computed(() =>
  variance.value > 0 ? "SURPLUS" : variance.value < 0 ? "LOSS" : "MATCHED"
);
const validActual = computed(() =>
  Number.isInteger(actualQuantity.value) && actualQuantity.value >= 0
);
const blockedGain = computed(() =>
  varianceType.value === "SURPLUS" && (Boolean(props.batch?.frozen) || Boolean(props.batch?.expired))
);

async function onSubmit() {
  if (!props.batch) return;
  formError.value = "";

  if (!validActual.value) {
    formError.value = "实盘数必须为不小于 0 的整数";
    return;
  }
  if (varianceType.value === "SURPLUS" && !gainBasis.value.trim()) {
    formError.value = "盘盈必须写明盘盈依据";
    return;
  }
  if (varianceType.value === "LOSS" && !lossBasis.value.trim()) {
    formError.value = "盘亏必须写明盘亏依据";
    return;
  }
  if (blockedGain.value) {
    formError.value = "盘盈不得回补质检冻结或已过期批次";
    return;
  }

  const form = createStockCheckForm(props.batch.id, {
    actualQuantity: actualQuantity.value,
    gainBasis: gainBasis.value.trim() || undefined,
    lossBasis: lossBasis.value.trim() || undefined,
    submittedBy: submittedBy.value.trim() || "仓库员"
  });
  try {
    await store.submit(form);
    emit("submitted");
  } catch (error) {
    formError.value = error instanceof Error ? error.message : "提交失败，请重试";
  }
}
</script>

<template>
  <div v-if="visible && batch" class="modal-mask" @click.self="emit('close')">
    <div class="modal">
      <header>
        <h3>发起批次盘点</h3>
        <button class="icon-btn" @click="emit('close')">×</button>
      </header>
      <div class="batch-meta">
        <span><strong>{{ batch.batch_no }}</strong></span>
        <span>{{ batch.supply_item_name ?? `物资#${batch.supply_item_id}` }}</span>
        <span>{{ batch.warehouse_name ?? `仓库#${batch.warehouse_id}` }}</span>
        <StatusBadge
          :value="batch.quality_status"
          :text="QualityStatusText[batch.quality_status as keyof typeof QualityStatusText] ?? batch.quality_status"
          :tone="batch.frozen ? 'amber' : 'green'"
        />
        <StatusBadge v-if="batch.expired" value="EXPIRED" text="已过期" tone="red" />
      </div>

      <div class="snapshot">
        账面数量：<strong>{{ batch.quantity }}</strong> {{ batch.unit ?? "" }}
      </div>

      <label class="field">
        <span>实盘数 *</span>
        <input v-model="actualText" type="number" min="0" step="1" placeholder="录入实际清点数量" />
      </label>

      <div class="variance">
        差异：
        <StatusBadge
          v-if="varianceType === 'SURPLUS'" value="SURPLUS" :text="`盘盈 +${variance}`" tone="blue"
        />
        <StatusBadge
          v-else-if="varianceType === 'LOSS'" value="LOSS" :text="`盘亏 ${variance}`" tone="red"
        />
        <StatusBadge v-else value="MATCHED" text="无差异" tone="gray" />
        <span class="hint">提交后差异保留待审，复核通过才调整批次数量</span>
      </div>

      <label class="field">
        <span>盘盈依据 <em v-if="varianceType === 'SURPLUS'">*必填</em></span>
        <textarea
          v-model="gainBasis"
          rows="2"
          :disabled="varianceType !== 'SURPLUS'"
          placeholder="盘盈时写明依据，如：捐赠随箱多出、供应商补送单等"
        ></textarea>
      </label>
      <p v-if="varianceType === 'SURPLUS' && (batch.frozen || batch.expired)" class="rule-warn">
        质检冻结或已过期批次不得盘盈回补，盘亏可照常提交。
      </p>

      <label class="field">
        <span>盘亏依据 <em v-if="varianceType === 'LOSS'">*必填</em></span>
        <textarea
          v-model="lossBasis"
          rows="2"
          :disabled="varianceType !== 'LOSS'"
          placeholder="盘亏时写明依据，如：破损、抽检留样、调拨未登记等"
        ></textarea>
      </label>

      <label class="field">
        <span>盘点人</span>
        <input v-model="submittedBy" type="text" />
      </label>

      <p v-if="formError" class="error-banner">{{ formError }}</p>

      <footer>
        <button class="btn ghost" @click="emit('close')">取消</button>
        <button class="btn primary" :disabled="store.acting" @click="onSubmit">
          {{ store.acting ? "提交中…" : "提交盘点（待审）" }}
        </button>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.modal-mask { position: fixed; inset: 0; background: rgba(20, 30, 24, .45); display: flex; align-items: center; justify-content: center; z-index: 50; padding: 20px; }
.modal { background: #fbfaf4; border-radius: 10px; width: min(560px, 100%); max-height: 90vh; overflow-y: auto; padding: 22px; box-shadow: 0 18px 48px rgba(0,0,0,.25); }
header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
h3 { margin: 0; font-size: 18px; }
.icon-btn { font-size: 22px; line-height: 1; color: #5b5e55; }
.batch-meta { display: flex; flex-wrap: wrap; align-items: center; gap: 8px 12px; padding: 10px 12px; background: #f4f2e9; border-radius: 8px; font-size: 13px; }
.snapshot { margin: 12px 0; font-size: 14px; color: #596257; }
.snapshot strong { font-size: 18px; color: #274335; }
.field { display: grid; gap: 6px; margin: 12px 0; font-size: 13px; color: #596257; }
.field em { color: #8a2b26; font-style: normal; font-weight: 800; }
.field input, .field textarea { width: 100%; box-sizing: border-box; border: 1px solid #c9c4b2; border-radius: 6px; padding: 9px 10px; font: inherit; background: #fff; }
.field textarea:disabled, .field input:disabled { background: #efede4; color: #9aa094; }
.variance { display: flex; align-items: center; gap: 10px; font-size: 13px; margin: 6px 0 4px; }
.hint { color: #8a8f84; }
.rule-warn { color: #8a2b26; font-size: 12px; margin: 2px 0 10px; }
.error-banner { background: #f6dddd; color: #8a2b26; padding: 9px 12px; border-radius: 6px; font-size: 13px; margin: 8px 0; }
footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 14px; }
.btn { border-radius: 6px; padding: 9px 16px; font-weight: 700; font-size: 13px; }
.btn.ghost { background: transparent; border: 1px solid #c9c4b2; color: #596257; }
.btn.primary { background: #274335; color: #f5f1e6; }
.btn.primary:disabled { opacity: .55; cursor: not-allowed; }
</style>

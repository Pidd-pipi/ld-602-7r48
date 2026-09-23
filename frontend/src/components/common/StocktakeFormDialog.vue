<script setup lang="ts">
import { computed, ref, watch } from "vue";
import type { InventoryBatch } from "../../types/InventoryBatch";
import type { StocktakeSubmitRequest } from "../../api/StocktakeOrder";
import { createStocktakeForm } from "../../constructors/StocktakeOrderConstructor";
import { formatNumber, formatQualityStatus } from "../../utils/formatters";

const props = defineProps<{
  batch: InventoryBatch | null;
  pending: boolean;
  submitting: boolean;
  errorCode: string;
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "submit", payload: StocktakeSubmitRequest): void;
}>();

const actualQuantity = ref(0);
const reason = ref("");
const submittedBy = ref("warehouse-keeper");

watch(
  () => props.batch,
  (batch) => {
    if (batch) {
      const form = createStocktakeForm(batch.id, batch.quantity);
      actualQuantity.value = form.actual_quantity;
      reason.value = "";
    }
  }
);

const variance = computed(() => {
  if (!props.batch) return 0;
  return actualQuantity.value - props.batch.quantity;
});

const varianceText = computed(() => {
  if (variance.value > 0) return "盘盈";
  if (variance.value < 0) return "盘亏";
  return "一致";
});

const surplusBlocked = computed(() => {
  if (!props.batch || variance.value <= 0) return false;
  const quality = props.batch.quality_status;
  if (quality === "FROZEN" || quality === "EXPIRED") return true;
  const expireAt = Date.parse(props.batch.expire_at);
  return Number.isFinite(expireAt) && expireAt < Date.now();
});

const reasonRequired = computed(() => variance.value !== 0);

const canSubmit = computed(() => {
  if (props.submitting || props.pending || !props.batch) return false;
  if (!Number.isInteger(actualQuantity.value) || actualQuantity.value < 0) return false;
  if (surplusBlocked.value) return false;
  if (reasonRequired.value && !reason.value.trim()) return false;
  return true;
});

function confirmSubmit() {
  if (!props.batch) return;
  emit("submit", {
    batchId: props.batch.id,
    actualQuantity: actualQuantity.value,
    reason: reason.value,
    submittedBy: submittedBy.value
  });
}
</script>

<template>
  <div v-if="batch" class="modal-mask" @click.self="emit('close')">
    <div class="modal">
      <header class="modal-head">
        <h2>批次盘点 · {{ batch.batch_no }}</h2>
        <button class="btn btn-small" type="button" @click="emit('close')">关闭</button>
      </header>

      <div v-if="pending" class="notice notice-warn">
        该批次已存在一张待审盘点，审批完成前不能重复提交。
      </div>

      <dl class="batch-meta">
        <div><dt>账面数量</dt><dd>{{ formatNumber(batch.quantity) }}</dd></div>
        <div><dt>质检状态</dt><dd>{{ formatQualityStatus(batch.quality_status) }}</dd></div>
      </dl>

      <label class="field">
        <span>实盘数</span>
        <input v-model.number="actualQuantity" type="number" min="0" step="1" :disabled="pending" />
      </label>

      <div class="variance-line">
        差异：<strong :class="variance > 0 ? 'text-surplus' : variance < 0 ? 'text-loss' : ''">
          {{ varianceText }} {{ variance > 0 ? "+" : "" }}{{ variance }}
        </strong>
      </div>

      <div v-if="surplusBlocked" class="notice notice-error">
        盘盈不得回补质检冻结或已过期批次；如实物盘亏，可将实盘数调小后照常提交。
      </div>

      <label class="field">
        <span>{{ variance > 0 ? "盘盈依据" : variance < 0 ? "盘亏依据" : "差异依据（盘盈 / 盘亏时必填）" }}</span>
        <textarea
          v-model="reason"
          rows="3"
          :disabled="pending"
          placeholder="分别写明依据，例如：捐赠入库漏记 / 临期破损报损 / 错发少发"
        ></textarea>
      </label>

      <label class="field">
        <span>盘点人</span>
        <input v-model="submittedBy" type="text" :disabled="pending" />
      </label>

      <div v-if="errorCode" class="notice notice-error">{{ errorCode }}</div>

      <footer class="modal-foot">
        <button class="btn" type="button" @click="emit('close')">取消</button>
        <button class="btn btn-primary" type="button" :disabled="!canSubmit" @click="confirmSubmit">
          {{ submitting ? "提交中…" : "提交盘点（保留待审）" }}
        </button>
      </footer>
    </div>
  </div>
</template>

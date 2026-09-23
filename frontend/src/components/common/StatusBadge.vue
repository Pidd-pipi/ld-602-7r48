<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(defineProps<{
  value: string;
  text?: string;
  tone?: "green" | "amber" | "red" | "gray" | "blue";
}>(), { tone: "green" });

const label = computed(() => props.text ?? props.value.replace(/_/g, " "));
</script>

<template>
  <span class="badge" :data-tone="tone">{{ label }}</span>
</template>

<style scoped>
.badge[data-tone="green"] { background: #e4efe4; color: #244b31; }
.badge[data-tone="amber"] { background: #f7ead0; color: #7d4d18; }
.badge[data-tone="red"] { background: #f6dddd; color: #8a2b26; }
.badge[data-tone="gray"] { background: #e6e4dc; color: #5b5e55; }
.badge[data-tone="blue"] { background: #dce8f2; color: #1f4a6b; }
</style>

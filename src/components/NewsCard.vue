<template>
  <div class="news-card glass-panel">
    <div class="news-chip" :style="{ background: chipColor }"></div>
    <div class="news-title">{{ title }}</div>
    <div class="news-body">{{ body }}</div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ title: string; body: string; chipColor?: string }>(), {
  chipColor: 'var(--accent)'
});
</script>

<style scoped>
.news-card {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.news-chip {
  width: 28px;
  height: 4px;
  border-radius: 4px;
}
.news-title {
  font-weight: 700;
  font-size: 13px;
}
.news-body {
  font-size: 12px;
  color: var(--text-dim);
  line-height: 1.5;
}
</style>

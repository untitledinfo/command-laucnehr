<template>
  <div class="news-card glass-panel hoverable">
    <div class="news-icon" :style="{ background: chipColor }">{{ icon }}</div>
    <div class="news-title">{{ title }}</div>
    <div class="news-body">{{ body }}</div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ title: string; body: string; chipColor?: string; icon?: string }>(), {
  chipColor: 'var(--accent)',
  icon: '\u2728'
});
</script>

<style scoped>
.news-card {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: default;
}
.news-icon {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: 0 6px 16px -6px rgba(0, 0, 0, 0.5);
}
.news-title {
  font-weight: 700;
  font-size: 13px;
}
.news-body {
  font-size: 12px;
  color: var(--text-dim);
  line-height: 1.55;
}
</style>

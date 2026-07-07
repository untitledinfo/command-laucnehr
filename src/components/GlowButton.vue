<template>
  <button class="glow-btn" :class="{ green: variant === 'green', disabled }" :disabled="disabled" @click="$emit('click')">
    <slot />
  </button>
</template>

<script setup lang="ts">
defineProps<{ variant?: 'accent' | 'green'; disabled?: boolean }>();
defineEmits<{ click: [] }>();
</script>

<style scoped>
.glow-btn {
  border: none;
  border-radius: 999px;
  padding: 12px 28px;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.03em;
  color: #0b0b12;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08), 0 8px 24px -8px rgba(139, 92, 246, 0.6);
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}
.glow-btn.green {
  background: linear-gradient(135deg, var(--launch-green), var(--launch-green-2));
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08), 0 8px 24px -8px rgba(61, 214, 104, 0.6);
}
.glow-btn:hover:not(.disabled) {
  transform: translateY(-1px);
}
.glow-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>

<template>
  <button
    class="glow-btn"
    :class="[variant === 'green' ? 'green' : 'accent', { disabled, pulsing }]"
    :disabled="disabled"
    @click="onClick"
  >
    <span v-for="r in ripples" :key="r.id" class="ripple" :style="{ left: r.x + 'px', top: r.y + 'px', width: r.size + 'px', height: r.size + 'px' }"></span>
    <span class="glow-btn-content"><slot /></span>
  </button>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{ variant?: 'accent' | 'green'; disabled?: boolean; pulsing?: boolean }>();
const emit = defineEmits<{ click: [] }>();

let rippleId = 0;
const ripples = ref<{ id: number; x: number; y: number; size: number }[]>([]);

function onClick(e: MouseEvent) {
  if (props.disabled) return;
  const target = e.currentTarget as HTMLElement;
  const rect = target.getBoundingClientRect();
  const size = Math.max(rect.width, rect.height) * 1.4;
  const id = ++rippleId;
  ripples.value.push({ id, x: e.clientX - rect.left - size / 2, y: e.clientY - rect.top - size / 2, size });
  setTimeout(() => {
    ripples.value = ripples.value.filter((r) => r.id !== id);
  }, 620);
  emit('click');
}
</script>

<style scoped>
.glow-btn {
  position: relative;
  overflow: hidden;
  border: none;
  border-radius: 999px;
  padding: 13px 30px;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.03em;
  color: #0b0b12;
  transition: transform var(--dur-fast) var(--ease-spring), box-shadow var(--dur-fast) ease, filter var(--dur-fast) ease;
}
.glow-btn.accent {
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08), 0 8px 24px -8px rgba(var(--accent-rgb), 0.6);
}
.glow-btn.green {
  background: linear-gradient(135deg, var(--launch-green), var(--launch-green-2));
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08), 0 8px 24px -8px rgba(61, 214, 104, 0.6);
}
.glow-btn:hover:not(.disabled) {
  transform: translateY(-2px) scale(1.015);
  filter: brightness(1.06);
}
.glow-btn:active:not(.disabled) {
  transform: translateY(0) scale(0.99);
}
.glow-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.glow-btn.pulsing:not(.disabled) {
  animation: pulse-glow 1.8s ease-out infinite;
}
.glow-btn-content {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.ripple {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.35);
  transform: scale(0);
  animation: ripple-anim 0.6s var(--ease-out);
  pointer-events: none;
}
</style>

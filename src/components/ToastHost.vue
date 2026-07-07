<template>
  <div class="toast-host">
    <TransitionGroup name="toast">
      <div v-for="t in toastState.items" :key="t.id" class="toast" :class="t.kind" @click="dismiss(t.id)">
        <span class="toast-icon">{{ icon(t.kind) }}</span>
        <div class="toast-body">
          <div class="toast-title">{{ t.title }}</div>
          <div v-if="t.message" class="toast-msg">{{ t.message }}</div>
        </div>
        <button class="toast-close" @click.stop="dismiss(t.id)">&#10005;</button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { toastState, dismiss, type ToastKind } from '../lib/toast';

function icon(kind: ToastKind) {
  switch (kind) {
    case 'success':
      return '\u2713';
    case 'error':
      return '\u26A0';
    case 'warning':
      return '\u26A0';
    default:
      return '\u2139';
  }
}
</script>

<style scoped>
.toast-host {
  position: fixed;
  bottom: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 200;
  width: 320px;
}
.toast {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 12px 12px 14px;
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, rgba(38, 38, 50, 0.94), rgba(20, 20, 28, 0.96));
  border: 1px solid var(--border);
  box-shadow: 0 16px 32px -12px rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(16px);
  cursor: pointer;
}
.toast.success {
  border-left: 3px solid var(--success);
}
.toast.error {
  border-left: 3px solid var(--error);
}
.toast.warning {
  border-left: 3px solid var(--warning);
}
.toast.info {
  border-left: 3px solid var(--info);
}
.toast-icon {
  font-size: 14px;
  line-height: 1.4;
}
.toast.success .toast-icon {
  color: var(--success);
}
.toast.error .toast-icon {
  color: var(--error);
}
.toast.warning .toast-icon {
  color: var(--warning);
}
.toast.info .toast-icon {
  color: var(--info);
}
.toast-body {
  flex: 1 1 auto;
  min-width: 0;
}
.toast-title {
  font-size: 12.5px;
  font-weight: 700;
}
.toast-msg {
  font-size: 11.5px;
  color: var(--text-dim);
  margin-top: 2px;
  line-height: 1.4;
}
.toast-close {
  background: none;
  border: none;
  color: var(--text-faint);
  font-size: 10px;
  padding: 2px;
}
.toast-close:hover {
  color: var(--text);
}

.toast-enter-active {
  animation: pop-in var(--dur-med) var(--ease-spring);
}
.toast-leave-active {
  transition: opacity var(--dur-fast) ease, transform var(--dur-fast) ease;
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(24px);
}
.toast-move {
  transition: transform var(--dur-med) var(--ease-out);
}
</style>

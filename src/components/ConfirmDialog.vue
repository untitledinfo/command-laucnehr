<template>
  <Transition name="dialog-fade">
    <div class="overlay" @click.self="$emit('cancel')">
      <div class="dialog glass-panel pop-in">
        <div class="icon-ring" :class="danger ? 'danger' : ''">{{ danger ? '\u26A0' : '\u2753' }}</div>
        <h3>{{ title }}</h3>
        <p>{{ message }}</p>
        <div class="actions">
          <button class="btn ghost" @click="$emit('cancel')">{{ cancelLabel }}</button>
          <button class="btn" :class="danger ? 'danger' : ''" @click="$emit('confirm')">{{ confirmLabel }}</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string;
    message: string;
    confirmLabel?: string;
    cancelLabel?: string;
    danger?: boolean;
  }>(),
  { confirmLabel: 'Confirm', cancelLabel: 'Cancel', danger: false }
);
defineEmits<{ confirm: []; cancel: [] }>();
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(4, 4, 8, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 120;
  backdrop-filter: blur(3px);
}
.dialog {
  width: 340px;
  padding: 26px 24px 22px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.icon-ring {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  background: rgba(var(--accent-rgb), 0.14);
  color: var(--accent-soft);
  margin-bottom: 6px;
}
.icon-ring.danger {
  background: rgba(248, 113, 113, 0.14);
  color: var(--error);
}
h3 {
  margin: 0;
  font-size: 15px;
}
p {
  margin: 0 0 10px;
  font-size: 12.5px;
  color: var(--text-dim);
  line-height: 1.5;
}
.actions {
  display: flex;
  gap: 10px;
  width: 100%;
}
.actions .btn {
  flex: 1 1 auto;
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity var(--dur-fast) ease;
}
.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
</style>

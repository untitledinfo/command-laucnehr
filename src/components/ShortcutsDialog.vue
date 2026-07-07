<template>
  <Transition name="dialog-fade">
    <div class="overlay" @click.self="$emit('close')">
      <div class="dialog glass-panel pop-in">
        <div class="dialog-header">
          <h3>Keyboard Shortcuts</h3>
          <button class="close-btn" @click="$emit('close')">&#10005;</button>
        </div>
        <div class="shortcut-list">
          <div v-for="s in shortcuts" :key="s.combo" class="shortcut-row">
            <span class="combo">
              <kbd v-for="k in s.combo.split('+')" :key="k">{{ k }}</kbd>
            </span>
            <span class="desc">{{ s.desc }}</span>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
defineEmits<{ close: [] }>();

const shortcuts = [
  { combo: 'Ctrl+L', desc: 'Launch the game' },
  { combo: 'Ctrl+K', desc: 'Focus version search' },
  { combo: 'Ctrl+,', desc: 'Open Settings' },
  { combo: 'Ctrl+1', desc: 'Go to Home' },
  { combo: 'Ctrl+2', desc: 'Go to Mods & Versions' },
  { combo: 'Ctrl+3', desc: 'Go to Console' },
  { combo: 'Shift+/', desc: 'Show this help' }
];
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
  width: 380px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dialog-header h3 {
  margin: 0;
  font-size: 15px;
}
.close-btn {
  background: none;
  border: none;
  color: var(--text-dim);
  font-size: 13px;
}
.close-btn:hover {
  color: var(--text);
}
.shortcut-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.shortcut-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.combo {
  display: flex;
  gap: 4px;
}
kbd {
  background: var(--bg-panel-light);
  border: 1px solid var(--border);
  border-bottom-width: 2px;
  border-radius: 6px;
  padding: 3px 7px;
  font-size: 11px;
  font-family: var(--font-mono);
  color: var(--text);
}
.desc {
  font-size: 12px;
  color: var(--text-dim);
  text-align: right;
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

<template>
  <div class="console-page">
    <div class="console-header">
      <h2>Console</h2>
      <div class="actions">
        <button class="secondary" @click="store.clearConsole">Clear</button>
        <button class="secondary" @click="saveLog">Save Log</button>
      </div>
    </div>
    <div class="console-box glass-panel" ref="boxRef">
      <div v-for="(line, i) in store.consoleLines" :key="i" class="line">{{ line }}</div>
      <div v-if="store.consoleLines.length === 0" class="empty">No output yet. Launch the game to see logs here.</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue';
import { useLauncherStore } from '../stores/launcher';

const store = useLauncherStore();
const boxRef = ref<HTMLElement | null>(null);

watch(
  () => store.consoleLines.length,
  async () => {
    await nextTick();
    if (boxRef.value) boxRef.value.scrollTop = boxRef.value.scrollHeight;
  }
);

function saveLog() {
  const blob = new Blob([store.consoleLines.join('\n')], { type: 'text/plain' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `launcher-log-${Date.now()}.txt`;
  a.click();
  URL.revokeObjectURL(url);
}
</script>

<style scoped>
.console-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
}
.console-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
h2 {
  margin: 0;
}
.actions {
  display: flex;
  gap: 8px;
}
button.secondary {
  background: var(--bg-panel-light);
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text);
  padding: 6px 12px;
  font-size: 12px;
}
button.secondary:hover {
  border-color: var(--accent);
}
.console-box {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 14px;
  font-family: 'Cascadia Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.6;
  color: #c9c9e0;
}
.line {
  white-space: pre-wrap;
  word-break: break-word;
}
.empty {
  color: var(--text-dim);
}
</style>

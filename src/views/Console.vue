<template>
  <div class="console-page" :class="{ fullscreen: isFullscreen }">
    <div class="console-header">
      <h2>Console <span class="badge muted">{{ filteredLines.length }} lines</span></h2>
      <div class="actions">
        <input v-model="filter" class="search-input" placeholder="Filter output..." />
        <label class="autoscroll-toggle tt-wrap">
          <input type="checkbox" v-model="store.prefs.autoscrollConsole" @change="store.persist" />
          Auto-scroll
          <span class="tt">Keep view pinned to the newest line</span>
        </label>
        <button class="btn small" @click="copyAll">&#128203; Copy</button>
        <button class="btn small" @click="saveLog">&#11015; Save Log</button>
        <button class="btn small tt-wrap" @click="isFullscreen = !isFullscreen">
          {{ isFullscreen ? '&#10529;' : '&#10530;' }}
          <span class="tt">{{ isFullscreen ? 'Exit fullscreen' : 'Fullscreen' }}</span>
        </button>
        <button class="btn small danger" @click="confirmClear = true">Clear</button>
      </div>
    </div>
    <div class="console-box glass-panel" ref="boxRef">
      <div v-for="(line, i) in filteredLines" :key="i" class="line" :class="levelClass(line)">{{ line }}</div>
      <div v-if="store.consoleLines.length === 0" class="empty">
        <div class="empty-icon">&#9000;</div>
        No output yet. Launch the game to see logs here.
      </div>
      <div v-else-if="filteredLines.length === 0" class="empty">No lines match &ldquo;{{ filter }}&rdquo;.</div>
    </div>

    <ConfirmDialog
      v-if="confirmClear"
      title="Clear console?"
      message="This will remove all current log output from view."
      confirm-label="Clear"
      danger
      @confirm="doClear"
      @cancel="confirmClear = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import { useLauncherStore } from '../stores/launcher';
import ConfirmDialog from '../components/ConfirmDialog.vue';
import { toast } from '../lib/toast';

const store = useLauncherStore();
const boxRef = ref<HTMLElement | null>(null);
const filter = ref('');
const isFullscreen = ref(false);
const confirmClear = ref(false);

const filteredLines = computed(() => {
  const q = filter.value.trim().toLowerCase();
  if (!q) return store.consoleLines;
  return store.consoleLines.filter((l) => l.toLowerCase().includes(q));
});

function levelClass(line: string) {
  const l = line.toLowerCase();
  if (l.includes('fatal') || l.includes('error') || l.includes('exception')) return 'lvl-error';
  if (l.includes('warn')) return 'lvl-warn';
  if (l.includes('info')) return 'lvl-info';
  return '';
}

watch(
  () => store.consoleLines.length,
  async () => {
    if (!store.prefs.autoscrollConsole) return;
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
  toast.success('Log saved');
}

function copyAll() {
  navigator.clipboard?.writeText(store.consoleLines.join('\n'));
  toast.success('Console copied to clipboard');
}

function doClear() {
  confirmClear.value = false;
  store.clearConsole();
  toast.info('Console cleared');
}
</script>

<style scoped>
.console-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
}
.console-page.fullscreen {
  position: fixed;
  inset: 0;
  z-index: 90;
  padding: 20px;
  background: var(--bg-darkest);
}
.console-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
h2 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
}
.actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
.search-input {
  font-size: 12px;
  padding: 6px 10px;
  width: 160px;
}
.autoscroll-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  color: var(--text-dim);
}
.console-box {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 14px;
  font-family: var(--font-mono);
  font-size: 12px;
  line-height: 1.65;
  color: #c9c9e0;
}
.line {
  white-space: pre-wrap;
  word-break: break-word;
}
.lvl-error {
  color: var(--error);
}
.lvl-warn {
  color: var(--warning);
}
.lvl-info {
  color: var(--info);
}
.empty {
  color: var(--text-dim);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px 0;
  text-align: center;
}
.empty-icon {
  font-size: 22px;
  opacity: 0.5;
}
</style>

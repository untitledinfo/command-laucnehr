<template>
  <div class="overlay" @click.self="$emit('close')">
    <div class="dialog glass-panel">
      <div class="dialog-header">
        <h3>Install a Version</h3>
        <button class="close-btn" @click="$emit('close')">&#10005;</button>
      </div>

      <div class="filter-row">
        <label><input type="checkbox" v-model="showSnapshots" /> Show snapshots</label>
        <input v-model="search" placeholder="Search versions..." />
      </div>

      <div class="version-list">
        <div
          v-for="v in filteredVersions"
          :key="v.id"
          class="version-row"
          :class="{ selected: v.id === selected }"
          @click="selected = v.id"
        >
          <span class="version-id">{{ v.id }}</span>
          <span class="version-type">{{ v.type }}</span>
        </div>
        <div v-if="loading" class="empty">Loading versions...</div>
        <div v-else-if="filteredVersions.length === 0" class="empty">No matches.</div>
      </div>

      <div class="loader-row">
        <label>Mod Loader</label>
        <select v-model="loader">
          <option value="none">Vanilla</option>
          <option value="fabric">Fabric</option>
          <option value="forge">Forge</option>
        </select>
        <select v-if="loader === 'fabric'" v-model="fabricLoaderVersion">
          <option v-for="lv in fabricLoaders" :key="lv" :value="lv">{{ lv }}</option>
        </select>
      </div>

      <div v-if="progressText" class="progress-block">
        <div class="progress-text">{{ progressText }}</div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPct + '%' }"></div>
        </div>
      </div>
      <div v-if="errorText" class="msa-error">{{ errorText }}</div>

      <div class="actions">
        <button class="secondary" :disabled="!selected || installing" @click="install">
          {{ installing ? 'Installing...' : 'Install' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';

const emit = defineEmits<{ close: []; installed: [] }>();

interface VersionEntry {
  id: string;
  type: string;
  url: string;
  releaseTime: string;
}

const versions = ref<VersionEntry[]>([]);
const loading = ref(true);
const showSnapshots = ref(false);
const search = ref('');
const selected = ref('');
const loader = ref<'none' | 'fabric' | 'forge'>('none');
const fabricLoaders = ref<string[]>([]);
const fabricLoaderVersion = ref('');
const installing = ref(false);
const progressText = ref('');
const progressDone = ref(0);
const progressTotal = ref(0);
const errorText = ref('');

const progressPct = computed(() => (progressTotal.value > 0 ? Math.min(100, (progressDone.value / progressTotal.value) * 100) : 0));

const filteredVersions = computed(() => {
  return versions.value.filter((v) => {
    if (!showSnapshots.value && v.type !== 'release') return false;
    if (search.value && !v.id.toLowerCase().includes(search.value.toLowerCase())) return false;
    return true;
  });
});

let unsubProgress: (() => void) | null = null;

onMounted(async () => {
  unsubProgress = window.launcherApi.versions.onProgress((p) => {
    progressText.value = p.text;
    progressDone.value = p.done;
    progressTotal.value = p.total;
  });
  versions.value = await window.launcherApi.versions.fetchManifest();
  loading.value = false;
});
onUnmounted(() => unsubProgress?.());

watch([selected, loader], async () => {
  if (loader.value === 'fabric' && selected.value) {
    fabricLoaders.value = await window.launcherApi.versions.fabricLoaders(selected.value);
    fabricLoaderVersion.value = fabricLoaders.value[0] ?? '';
  }
});

async function install() {
  if (!selected.value) return;
  installing.value = true;
  errorText.value = '';
  progressText.value = '';
  try {
    if (loader.value === 'none') {
      await window.launcherApi.versions.install(selected.value);
    } else if (loader.value === 'fabric') {
      await window.launcherApi.versions.installFabric(selected.value, fabricLoaderVersion.value);
    } else if (loader.value === 'forge') {
      const forgeVersion = await window.launcherApi.versions.recommendedForge(selected.value);
      if (!forgeVersion) throw new Error(`No Forge build found for ${selected.value}`);
      await window.launcherApi.versions.installForge(selected.value, forgeVersion);
    }
    emit('installed');
    emit('close');
  } catch (e: any) {
    errorText.value = e.message ?? String(e);
  } finally {
    installing.value = false;
  }
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}
.dialog {
  width: 460px;
  max-height: 78vh;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dialog-header h3 {
  margin: 0;
}
.close-btn {
  background: none;
  border: none;
  color: var(--text-dim);
  font-size: 14px;
  border-radius: 6px;
  width: 26px;
  height: 26px;
  transition: background 0.15s, color 0.15s;
}
.close-btn:hover {
  background: var(--bg-panel-light);
  color: var(--error);
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--text-dim);
}
.filter-row input[type='text'],
.filter-row input:not([type='checkbox']) {
  flex: 1 1 auto;
}
.version-list {
  overflow-y: auto;
  max-height: 220px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 6px;
}
.version-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
}
.version-row:hover {
  background: var(--bg-panel-light);
}
.version-row.selected {
  background: rgba(139, 92, 246, 0.18);
  border: 1px solid var(--accent);
}
.version-type {
  color: var(--text-dim);
}
.loader-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
.progress-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.progress-text {
  font-size: 12px;
  color: var(--text-dim);
}
.progress-bar {
  height: 6px;
  border-radius: 4px;
  background: var(--bg-panel-light);
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent), var(--accent-2));
  transition: width 0.2s ease;
}
.msa-error {
  font-size: 12px;
  color: var(--error);
}
.actions {
  display: flex;
  justify-content: flex-end;
}
button.secondary {
  background: var(--bg-panel-light);
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text);
  padding: 8px 14px;
  font-size: 13px;
}
button.secondary:hover:not(:disabled) {
  border-color: var(--accent);
}
button.secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.empty {
  color: var(--text-dim);
  font-size: 12px;
  text-align: center;
  padding: 12px 0;
}
</style>

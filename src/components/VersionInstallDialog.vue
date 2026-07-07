<template>
  <Transition name="dialog-fade">
    <div class="overlay" @click.self="$emit('close')">
      <div class="dialog glass-panel pop-in">
        <div class="dialog-header">
          <h3>Install a Version</h3>
          <button class="close-btn" @click="$emit('close')">&#10005;</button>
        </div>

        <div class="filter-row">
          <label class="checkbox-label"><input type="checkbox" v-model="showSnapshots" /> Show snapshots</label>
          <input v-model="search" placeholder="Search versions..." />
        </div>

        <div class="version-list">
          <div v-if="loading" class="skeleton-wrap">
            <div v-for="i in 6" :key="i" class="skeleton skeleton-row"></div>
          </div>
          <template v-else>
            <div
              v-for="v in filteredVersions"
              :key="v.id"
              class="version-row"
              :class="{ selected: v.id === selected }"
              @click="selected = v.id"
            >
              <span class="version-id">{{ v.id }}</span>
              <span class="badge muted">{{ v.type }}</span>
            </div>
            <div v-if="filteredVersions.length === 0" class="empty">No matches.</div>
          </template>
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

        <Transition name="fade-scale">
          <div v-if="progressText" class="progress-block">
            <div class="progress-text-row">
              <span class="progress-text">{{ progressText }}</span>
              <span class="progress-pct">{{ Math.round(progressPct) }}%</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: progressPct + '%' }"></div>
            </div>
          </div>
        </Transition>
        <div v-if="errorText" class="msa-error">{{ errorText }}</div>

        <div class="actions">
          <button class="btn" :disabled="!selected || installing" @click="install">
            <span v-if="installing" class="mini-spinner"></span>
            {{ installing ? 'Installing...' : 'Install' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { toast } from '../lib/toast';

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
    toast.success('Installed', selected.value);
    emit('installed');
    emit('close');
  } catch (e: any) {
    errorText.value = e.message ?? String(e);
    toast.error('Install failed', errorText.value);
  } finally {
    installing.value = false;
  }
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(4, 4, 8, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(3px);
}
.dialog {
  width: 460px;
  max-height: 80vh;
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
}
.close-btn:hover {
  color: var(--text);
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--text-dim);
}
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}
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
.skeleton-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.skeleton-row {
  height: 28px;
}
.version-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  transition: background var(--dur-fast) ease;
}
.version-row:hover {
  background: var(--bg-panel-light);
}
.version-row.selected {
  background: rgba(var(--accent-rgb), 0.18);
  border: 1px solid var(--accent);
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
.progress-text-row {
  display: flex;
  justify-content: space-between;
  font-size: 11.5px;
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
.mini-spinner {
  display: inline-block;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.25);
  border-top-color: var(--text);
  animation: spin 0.7s linear infinite;
}
.empty {
  color: var(--text-dim);
  font-size: 12px;
  text-align: center;
  padding: 12px 0;
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity var(--dur-fast) ease;
}
.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: opacity var(--dur-fast) ease;
}
.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
}
</style>

<template>
  <div class="mods">
    <h2 class="fade-up-in">Mods &amp; Versions</h2>

    <div class="panel glass-panel fade-up-in">
      <label>Select installed version</label>
      <div class="select-row">
        <select v-model="selected">
          <option v-for="v in store.installedVersions" :key="v" :value="v">{{ v }}</option>
        </select>
        <button
          class="fav-btn tt-wrap"
          :class="{ active: selected && store.isFavorite(selected) }"
          :disabled="!selected"
          @click="selected && store.toggleFavorite(selected)"
        >
          &#9733;
          <span class="tt">Favorite</span>
        </button>
      </div>

      <div class="btn-grid">
        <button class="btn" :disabled="!selected" @click="openMods">&#128193; Open Mods Folder</button>
        <button class="btn" :disabled="!selected" @click="openResourcePacks">&#127912; Open Resource Packs</button>
        <button class="btn" :disabled="!selected" @click="openScreenshots">&#128247; Open Screenshots</button>
        <button class="btn" @click="openRoot">&#128194; Open .minecraft Folder</button>
      </div>

      <button class="btn danger" :disabled="!selected" @click="confirmDelete = true">&#128465; Delete Selected Version</button>
    </div>

    <div class="panel glass-panel fade-up-in">
      <div class="panel-header">
        <h3>Installed Versions <span class="badge muted">{{ store.installedVersions.length }}</span></h3>
        <input v-model="filter" class="search-input" placeholder="Search versions..." />
      </div>

      <div v-if="loading" class="version-list">
        <div v-for="i in 4" :key="i" class="skeleton skeleton-row"></div>
      </div>
      <div v-else class="version-list">
        <TransitionGroup name="list">
          <div v-for="v in filteredList" :key="v" class="version-row" :class="{ selected: v === selected }" @click="selected = v">
            <span class="version-name">{{ v }}</span>
            <span v-if="store.isFavorite(v)" class="star-badge">&#9733;</span>
            <span v-if="store.prefs.playtime[v]" class="badge muted">{{ formatPlaytime(v) }}</span>
          </div>
        </TransitionGroup>
        <div v-if="filteredList.length === 0" class="empty">No versions match your search.</div>
      </div>
    </div>

    <ConfirmDialog
      v-if="confirmDelete"
      title="Delete this version?"
      :message="`This will permanently remove '${selected}' and its files. This can't be undone.`"
      confirm-label="Delete"
      danger
      @confirm="deleteVersion"
      @cancel="confirmDelete = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useLauncherStore } from '../stores/launcher';
import ConfirmDialog from '../components/ConfirmDialog.vue';
import { toast } from '../lib/toast';

const store = useLauncherStore();
const selected = ref('');
const filter = ref('');
const loading = ref(true);
const confirmDelete = ref(false);

const filteredList = computed(() => {
  const q = filter.value.trim().toLowerCase();
  const all = [...store.installedVersions].sort((a, b) => Number(store.isFavorite(b)) - Number(store.isFavorite(a)));
  if (!q) return all;
  return all.filter((v) => v.toLowerCase().includes(q));
});

function formatPlaytime(v: string) {
  const mins = store.prefs.playtime[v] ?? 0;
  if (mins < 60) return `${mins}m played`;
  return `${Math.floor(mins / 60)}h ${mins % 60}m played`;
}

onMounted(async () => {
  loading.value = true;
  await store.refreshInstalledVersions();
  if (store.installedVersions.length) selected.value = store.installedVersions[0];
  loading.value = false;
});

function openMods() {
  window.launcherApi.folders.openMods(selected.value);
}
function openResourcePacks() {
  window.launcherApi.folders.openResourcePacks(selected.value);
}
function openScreenshots() {
  window.launcherApi.folders.openScreenshots(selected.value);
}
function openRoot() {
  window.launcherApi.folders.openRoot();
}
async function deleteVersion() {
  confirmDelete.value = false;
  if (!selected.value) return;
  const removed = selected.value;
  await window.launcherApi.versions.delete(selected.value);
  await store.refreshInstalledVersions();
  selected.value = store.installedVersions[0] ?? '';
  toast.success('Version deleted', removed);
}
</script>

<style scoped>
.mods {
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
h2 {
  margin: 0;
}
.panel {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
label {
  font-size: 12px;
  color: var(--text-dim);
}
.select-row {
  display: flex;
  gap: 8px;
}
.select-row select {
  flex: 1 1 auto;
}
.fav-btn {
  width: 40px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg-panel-light);
  color: var(--text-faint);
  font-size: 14px;
  transition: color var(--dur-fast) ease, border-color var(--dur-fast) ease, transform var(--dur-fast) var(--ease-spring);
}
.fav-btn:hover {
  color: var(--warning);
}
.fav-btn.active {
  color: var(--warning);
  border-color: var(--warning);
  transform: scale(1.05);
}
.btn-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.panel-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.search-input {
  font-size: 12px;
  padding: 6px 10px;
  width: 180px;
}
.version-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.version-row {
  padding: 8px 10px;
  border-radius: 8px;
  font-size: 12px;
  background: var(--bg-panel-light);
  border: 1px solid transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: border-color var(--dur-fast) ease, transform var(--dur-fast) ease;
}
.version-row:hover {
  transform: translateX(2px);
}
.version-row.selected {
  border-color: var(--accent);
  background: rgba(var(--accent-rgb), 0.12);
}
.version-name {
  flex: 1 1 auto;
}
.star-badge {
  color: var(--warning);
  font-size: 12px;
}
.skeleton-row {
  height: 34px;
}
.empty {
  color: var(--text-dim);
  font-size: 12px;
  padding: 8px 2px;
}

.list-enter-active,
.list-leave-active {
  transition: opacity var(--dur-fast) ease, transform var(--dur-fast) ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-6px);
}
</style>

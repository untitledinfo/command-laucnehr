<template>
  <div class="mods">
    <div class="mods-header">
      <h2>Mods &amp; Versions</h2>
      <button class="primary-link" @click="showInstall = true">+ Install Version</button>
    </div>

    <div class="panel glass-panel">
      <label>Select installed version</label>
      <select v-model="selected">
        <option v-for="v in store.installedVersions" :key="v" :value="v">{{ v }}</option>
      </select>

      <div class="btn-grid">
        <button class="secondary" :disabled="!selected" @click="openMods">Open Mods Folder</button>
        <button class="secondary" :disabled="!selected" @click="openResourcePacks">Open Resource Packs Folder</button>
        <button class="secondary" :disabled="!selected" @click="openScreenshots">Open Screenshots Folder</button>
        <button class="secondary" @click="openRoot">Open .minecraft Folder</button>
      </div>

      <button class="danger" :disabled="!selected" @click="deleteVersion">Delete Selected Version</button>
    </div>

    <div class="panel glass-panel">
      <h3>Installed Versions</h3>
      <div class="version-list">
        <div v-for="v in store.installedVersions" :key="v" class="version-row">{{ v }}</div>
        <div v-if="store.installedVersions.length === 0" class="empty">
          No versions installed yet. <button class="link-btn" @click="showInstall = true">Install one</button>
        </div>
      </div>
    </div>

    <VersionInstallDialog v-if="showInstall" @close="showInstall = false" @installed="onInstalled" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useLauncherStore } from '../stores/launcher';
import VersionInstallDialog from '../components/VersionInstallDialog.vue';

const store = useLauncherStore();
const selected = ref('');
const showInstall = ref(false);

onMounted(async () => {
  await store.refreshInstalledVersions();
  if (store.installedVersions.length) selected.value = store.installedVersions[0];
});

async function onInstalled() {
  await store.refreshInstalledVersions();
  if (!selected.value && store.installedVersions.length) selected.value = store.installedVersions[0];
}

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
  if (!selected.value) return;
  await window.launcherApi.versions.delete(selected.value);
  await store.refreshInstalledVersions();
  selected.value = store.installedVersions[0] ?? '';
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
.mods-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.primary-link {
  background: rgba(139, 92, 246, 0.14);
  border: 1px solid var(--accent);
  color: var(--accent-soft);
  border-radius: 999px;
  padding: 8px 16px;
  font-size: 12px;
  font-weight: 600;
}
.primary-link:hover {
  background: rgba(139, 92, 246, 0.24);
}
.link-btn {
  background: none;
  border: none;
  color: var(--accent-soft);
  font-size: 12px;
  padding: 0;
  text-decoration: underline;
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
.btn-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
button.secondary {
  background: var(--bg-panel-light);
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text);
  padding: 10px 12px;
  font-size: 12px;
}
button.secondary:hover:not(:disabled) {
  border-color: var(--accent);
}
button.secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
button.danger {
  background: rgba(248, 113, 113, 0.12);
  border: 1px solid var(--error);
  color: var(--error);
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 12px;
}
button.danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.version-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.version-row {
  padding: 6px 8px;
  border-radius: 6px;
  font-size: 12px;
  background: var(--bg-panel-light);
}
.empty {
  color: var(--text-dim);
  font-size: 12px;
}
</style>

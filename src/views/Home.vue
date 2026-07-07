<template>
  <div class="home">
    <div class="top-row fade-up-in">
      <div class="quick-actions">
        <button class="btn ghost small tt-wrap" @click="showShortcuts = true">
          &#9410;
          <span class="tt">Shortcuts</span>
        </button>
      </div>
      <div class="account-chip glass-panel hoverable" @click="showAccounts = true">
        <div class="avatar" :style="{ background: avatarGradient }">
          {{ (store.activeAccount?.name ?? '?').slice(0, 1).toUpperCase() }}
        </div>
        <div class="account-meta">
          <div class="account-name">{{ store.activeAccount?.name ?? 'No account' }}</div>
          <div class="account-type">{{ store.activeAccount?.type === 'msa' ? 'Microsoft' : store.activeAccount ? 'Offline' : 'Click to sign in' }}</div>
        </div>
      </div>
    </div>

    <div class="hero glass-panel fade-up-in">
      <div class="hero-glow"></div>
      <div class="hero-top">
        <div>
          <h1 class="hero-title accent-gradient-text">Command Launcher</h1>
          <p class="hero-sub">Fast, clean, and fully yours &mdash; launch any version in seconds.</p>
        </div>
        <div class="theme-quick tt-wrap">
          <span class="badge">{{ store.themeLabel }} theme</span>
          <span class="tt">Change in Settings</span>
        </div>
      </div>

      <div class="server-panel">
        <div class="server-row">
          <input v-model="serverAddress" placeholder="play.example.net:25565" @keyup.enter="pingServer" />
          <button class="btn small" :disabled="pinging" @click="pingServer">
            <span v-if="pinging" class="mini-spinner"></span>{{ pinging ? 'Pinging' : 'Ping' }}
          </button>
          <button class="btn small tt-wrap" @click="copyIp">
            &#128203;
            <span class="tt">Copy address</span>
          </button>
          <button class="btn small tt-wrap" @click="saveCurrentServer">
            &#11088;
            <span class="tt">Save server</span>
          </button>
        </div>

        <Transition name="fade-scale">
          <div v-if="pingResult" class="ping-result" :class="{ online: pingResult.online }">
            <span v-if="pingResult.online">
              <span class="ping-dot online"></span> Online &mdash; {{ pingResult.playersOnline }}/{{ pingResult.playersMax }} players &mdash;
              {{ pingResult.versionName }}
            </span>
            <span v-else><span class="ping-dot"></span> Offline &mdash; {{ pingResult.error }}</span>
          </div>
        </Transition>

        <div v-if="store.prefs.servers.length" class="saved-servers">
          <button
            v-for="s in store.prefs.servers"
            :key="s.id"
            class="server-pill"
            :class="{ active: s.address === serverAddress }"
            @click="serverAddress = s.address"
          >
            {{ s.name }}
            <span class="remove-x" @click.stop="store.removeServer(s.id)">&#10005;</span>
          </button>
        </div>
      </div>

      <div class="launch-row">
        <div class="version-select">
          <label>Version</label>
          <div class="version-search-row">
            <input ref="searchInput" v-model="versionFilter" class="version-filter" placeholder="Filter versions... (Ctrl+K)" />
          </div>
          <div class="version-pick-row">
            <select v-model="selectedVersion">
              <option v-for="v in filteredVersions" :key="v" :value="v">{{ v }}</option>
            </select>
            <button
              class="fav-btn tt-wrap"
              :class="{ active: selectedVersion && store.isFavorite(selectedVersion) }"
              :disabled="!selectedVersion"
              @click="selectedVersion && store.toggleFavorite(selectedVersion)"
            >
              &#9733;
              <span class="tt">Favorite</span>
            </button>
          </div>
          <div v-if="selectedVersion" class="version-meta">
            <span v-if="playtimeFor(selectedVersion)">Played {{ playtimeFor(selectedVersion) }}</span>
            <span v-if="lastPlayedFor(selectedVersion)">&middot; Last played {{ lastPlayedFor(selectedVersion) }}</span>
          </div>
          <button class="link-btn" @click="showInstall = true">+ Install version</button>
        </div>
        <GlowButton variant="green" :disabled="!canLaunch" :pulsing="canLaunch && !store.launching" @click="launch">
          <span v-if="store.launching" class="mini-spinner light"></span>
          {{ store.launching ? 'Launching...' : 'LAUNCH' }}
        </GlowButton>
      </div>
      <Transition name="fade-scale">
        <div v-if="launchError" class="msa-error">{{ launchError }}</div>
      </Transition>
    </div>

    <div class="news-row fade-up-in">
      <NewsCard
        icon="&#127881;"
        title="Welcome to Command Launcher"
        body="A fast, self-hosted launcher with mod &amp; version management, custom themes, and live console output."
        chip-color="var(--chip-1)"
      />
      <NewsCard
        icon="&#9889;"
        title="Performance Update"
        body="Launch times improved and the UI now runs smoother with GPU-friendly animations."
        chip-color="var(--chip-3)"
      />
      <NewsCard
        icon="&#127912;"
        title="New Themes Available"
        body="Pick from five accent themes or set a fully custom color in Settings."
        chip-color="var(--chip-4)"
      />
    </div>

    <AccountsDialog v-if="showAccounts" @close="showAccounts = false" />
    <VersionInstallDialog v-if="showInstall" @close="showInstall = false" @installed="onInstalled" />
    <ShortcutsDialog v-if="showShortcuts" @close="showShortcuts = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useLauncherStore } from '../stores/launcher';
import GlowButton from '../components/GlowButton.vue';
import NewsCard from '../components/NewsCard.vue';
import AccountsDialog from '../components/AccountsDialog.vue';
import VersionInstallDialog from '../components/VersionInstallDialog.vue';
import ShortcutsDialog from '../components/ShortcutsDialog.vue';
import { toast } from '../lib/toast';

const store = useLauncherStore();
const showAccounts = ref(false);
const showInstall = ref(false);
const showShortcuts = ref(false);
const serverAddress = ref('play.example.net:25565');
const pinging = ref(false);
const pingResult = ref<any>(null);
const selectedVersion = ref('');
const launchError = ref('');
const versionFilter = ref('');
const searchInput = ref<HTMLInputElement | null>(null);

const canLaunch = computed(() => !!selectedVersion.value && !!store.activeAccount && !store.launching);

const filteredVersions = computed(() => {
  const q = versionFilter.value.trim().toLowerCase();
  const all = store.installedVersions;
  const favored = [...all].sort((a, b) => Number(store.isFavorite(b)) - Number(store.isFavorite(a)));
  if (!q) return favored;
  return favored.filter((v) => v.toLowerCase().includes(q));
});

const avatarGradient = computed(() => {
  const name = store.activeAccount?.name ?? '??';
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = (hash * 31 + name.charCodeAt(i)) & 0xffffff;
  const h1 = hash % 360;
  const h2 = (h1 + 60) % 360;
  return `linear-gradient(135deg, hsl(${h1}, 80%, 62%), hsl(${h2}, 80%, 58%))`;
});

function playtimeFor(versionId: string) {
  const mins = store.prefs.playtime[versionId];
  if (!mins) return '';
  if (mins < 60) return `${mins}m`;
  return `${Math.floor(mins / 60)}h ${mins % 60}m`;
}
function lastPlayedFor(versionId: string) {
  const iso = store.prefs.lastPlayed[versionId];
  if (!iso) return '';
  return new Date(iso).toLocaleDateString();
}

function onInstalled() {
  store.refreshInstalledVersions();
  toast.success('Version installed', 'Ready to launch.');
}

onMounted(async () => {
  await store.loadAll();
  if (store.config?.server) serverAddress.value = store.config.server;
  if (store.installedVersions.length) selectedVersion.value = store.installedVersions[0];

  const unsubOut = window.launcherApi.launcher.onOutput((line) => store.appendConsole(line));
  const unsubExit = window.launcherApi.launcher.onExit(() => {
    store.launching = false;
    if (selectedVersion.value) store.recordPlaytimeEnd(selectedVersion.value);
  });

  function onGlobalKeydown(e: KeyboardEvent) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
      e.preventDefault();
      nextTick(() => searchInput.value?.focus());
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 'l') {
      e.preventDefault();
      launch();
    }
  }
  window.addEventListener('keydown', onGlobalKeydown);

  onUnmounted(() => {
    unsubOut();
    unsubExit();
    window.removeEventListener('keydown', onGlobalKeydown);
  });
});

async function pingServer() {
  pinging.value = true;
  pingResult.value = null;
  try {
    pingResult.value = await window.launcherApi.server.ping(serverAddress.value);
    if (!pingResult.value?.online) toast.warning('Server offline', serverAddress.value);
  } catch (e: any) {
    toast.error('Ping failed', e.message ?? String(e));
  } finally {
    pinging.value = false;
  }
}

function copyIp() {
  navigator.clipboard?.writeText(serverAddress.value);
  toast.success('Copied', serverAddress.value);
}

function saveCurrentServer() {
  if (!serverAddress.value.trim()) return;
  store.addServer(serverAddress.value, serverAddress.value);
  toast.success('Server saved');
}

async function launch() {
  if (!canLaunch.value) return;
  launchError.value = '';
  store.launching = true;
  store.clearConsole();
  store.recordPlaytimeStart();
  try {
    await window.launcherApi.launcher.launch(selectedVersion.value, serverAddress.value || null);
    toast.success('Launching game', selectedVersion.value);
  } catch (e: any) {
    launchError.value = e.message ?? String(e);
    toast.error('Launch failed', launchError.value);
    store.launching = false;
  }
}
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 880px;
  margin: 0 auto;
}
.top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.account-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 14px 6px 6px;
  cursor: pointer;
}
.avatar {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  color: #0b0b12;
}
.account-meta {
  display: flex;
  flex-direction: column;
}
.account-name {
  font-size: 12px;
  font-weight: 700;
}
.account-type {
  font-size: 10.5px;
  color: var(--text-faint);
}

.hero {
  position: relative;
  padding: 36px;
  overflow: hidden;
}
.hero-glow {
  position: absolute;
  top: -80px;
  right: -80px;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(var(--accent-rgb), 0.35), transparent 70%);
  pointer-events: none;
  animation: float-blob 10s ease-in-out infinite;
}
.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 22px;
}
.hero-title {
  font-size: 30px;
  font-weight: 800;
  margin: 0 0 4px;
}
.hero-sub {
  color: var(--text-dim);
  margin: 0;
  font-size: 13px;
}
.server-panel {
  margin-bottom: 20px;
}
.server-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.server-row input {
  flex: 1 1 auto;
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
.mini-spinner.light {
  border-color: rgba(0, 0, 0, 0.2);
  border-top-color: #0b0b12;
}
.ping-result {
  font-size: 12px;
  color: var(--error);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.ping-result.online {
  color: var(--success);
}
.ping-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--error);
}
.ping-dot.online {
  background: var(--success);
  box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.6);
  animation: pulse-glow 1.6s ease-out infinite;
}
.saved-servers {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.server-pill {
  background: var(--bg-panel-light);
  border: 1px solid var(--border);
  border-radius: 999px;
  color: var(--text-dim);
  font-size: 11px;
  padding: 5px 10px 5px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: border-color var(--dur-fast) ease, color var(--dur-fast) ease;
}
.server-pill:hover {
  border-color: var(--accent);
  color: var(--text);
}
.server-pill.active {
  border-color: var(--accent);
  background: rgba(var(--accent-rgb), 0.14);
  color: var(--text);
}
.remove-x {
  color: var(--text-faint);
  font-size: 10px;
}
.remove-x:hover {
  color: var(--error);
}

.launch-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}
.version-select {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1 1 auto;
}
.version-select label {
  font-size: 11px;
  color: var(--text-dim);
}
.version-filter {
  font-size: 12px;
  padding: 6px 10px;
}
.version-pick-row {
  display: flex;
  gap: 8px;
}
.version-pick-row select {
  flex: 1 1 auto;
}
.fav-btn {
  width: 36px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg-panel-light);
  color: var(--text-faint);
  font-size: 14px;
  transition: color var(--dur-fast) ease, transform var(--dur-fast) var(--ease-spring), border-color var(--dur-fast) ease;
}
.fav-btn:hover {
  color: var(--warning);
}
.fav-btn.active {
  color: var(--warning);
  border-color: var(--warning);
  transform: scale(1.05);
}
.version-meta {
  font-size: 10.5px;
  color: var(--text-faint);
}
.link-btn {
  background: none;
  border: none;
  color: var(--accent-soft);
  font-size: 12px;
  text-align: left;
  padding: 0;
}
.link-btn:hover {
  text-decoration: underline;
}

.news-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.msa-error {
  font-size: 12px;
  color: var(--error);
  margin-top: 8px;
}

.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: opacity var(--dur-fast) ease, transform var(--dur-fast) ease;
}
.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>

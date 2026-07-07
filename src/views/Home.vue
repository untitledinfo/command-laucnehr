<template>
  <div class="home">
    <div class="top-row">
      <div class="account-chip glass-panel" @click="showAccounts = true">
        <div class="avatar">{{ (store.activeAccount?.name ?? '?').slice(0, 1).toUpperCase() }}</div>
        <div class="account-name">{{ store.activeAccount?.name ?? 'No account' }}</div>
      </div>
    </div>

    <div class="hero glass-panel">
      <div class="hero-glow"></div>
      <h1 class="hero-title accent-gradient-text">PGC // Command Launcher</h1>
      <p class="hero-sub">play.pgcmc.fun</p>

      <div class="server-row">
        <input v-model="serverAddress" placeholder="play.pgcmc.fun:25565" />
        <button class="secondary" :disabled="pinging" @click="pingServer">{{ pinging ? 'Pinging...' : 'Ping' }}</button>
      </div>
      <div v-if="pingResult" class="ping-result" :class="{ online: pingResult.online }">
        <span v-if="pingResult.online">
          &#9679; Online &mdash; {{ pingResult.playersOnline }}/{{ pingResult.playersMax }} players &mdash;
          {{ pingResult.versionName }}
        </span>
        <span v-else>&#9679; Offline &mdash; {{ pingResult.error }}</span>
      </div>

      <div class="launch-row">
        <div class="version-select">
          <label>Version</label>
          <select v-model="selectedVersion">
            <option v-for="v in store.installedVersions" :key="v" :value="v">{{ v }}</option>
          </select>
          <button class="link-btn" @click="showInstall = true">+ Install version</button>
        </div>
        <GlowButton variant="green" :disabled="!canLaunch" @click="launch">
          {{ store.launching ? 'Launching...' : 'LAUNCH' }}
        </GlowButton>
      </div>
      <div v-if="launchError" class="msa-error">{{ launchError }}</div>
    </div>

    <div class="news-row">
      <NewsCard title="Welcome to PGC" body="Pakistan Gamers Community — join the SMP and climb the Hall of Fame." chip-color="var(--chip-1)" />
      <NewsCard title="BlueTick Army" body="Cross-community events and tournaments running throughout the season." chip-color="var(--chip-3)" />
      <NewsCard title="Game Scope" body="Fresh gaming news and guides over at xenomc.fun." chip-color="var(--chip-4)" />
    </div>

    <AccountsDialog v-if="showAccounts" @close="showAccounts = false" />
    <VersionInstallDialog v-if="showInstall" @close="showInstall = false" @installed="store.refreshInstalledVersions" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useLauncherStore } from '../stores/launcher';
import GlowButton from '../components/GlowButton.vue';
import NewsCard from '../components/NewsCard.vue';
import AccountsDialog from '../components/AccountsDialog.vue';
import VersionInstallDialog from '../components/VersionInstallDialog.vue';

const store = useLauncherStore();
const showAccounts = ref(false);
const showInstall = ref(false);
const serverAddress = ref('play.pgcmc.fun:25565');
const pinging = ref(false);
const pingResult = ref<any>(null);
const selectedVersion = ref('');
const launchError = ref('');

const canLaunch = computed(() => !!selectedVersion.value && !!store.activeAccount && !store.launching);

onMounted(async () => {
  await store.loadAll();
  if (store.config?.server) serverAddress.value = store.config.server;
  if (store.installedVersions.length) selectedVersion.value = store.installedVersions[0];

  const unsubOut = window.launcherApi.launcher.onOutput((line) => store.appendConsole(line));
  const unsubExit = window.launcherApi.launcher.onExit(() => {
    store.launching = false;
  });
  onUnmounted(() => {
    unsubOut();
    unsubExit();
  });
});

async function pingServer() {
  pinging.value = true;
  pingResult.value = null;
  try {
    pingResult.value = await window.launcherApi.server.ping(serverAddress.value);
  } finally {
    pinging.value = false;
  }
}

async function launch() {
  if (!canLaunch.value) return;
  launchError.value = '';
  store.launching = true;
  store.clearConsole();
  try {
    await window.launcherApi.launcher.launch(selectedVersion.value, serverAddress.value || null);
  } catch (e: any) {
    launchError.value = e.message ?? String(e);
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
  justify-content: flex-end;
}
.account-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px 6px 6px;
  cursor: pointer;
}
.avatar {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
  color: #0b0b12;
}
.account-name {
  font-size: 12px;
  font-weight: 600;
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
  background: radial-gradient(circle, rgba(139, 92, 246, 0.35), transparent 70%);
  pointer-events: none;
}
.hero-title {
  font-size: 30px;
  font-weight: 800;
  margin: 0 0 4px;
}
.hero-sub {
  color: var(--text-dim);
  margin: 0 0 24px;
  font-size: 13px;
}
.server-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}
.server-row input {
  flex: 1 1 auto;
}
.ping-result {
  font-size: 12px;
  color: var(--error);
  margin-bottom: 20px;
}
.ping-result.online {
  color: var(--success);
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
.link-btn {
  background: none;
  border: none;
  color: var(--accent-soft);
  font-size: 12px;
  text-align: left;
  padding: 0;
}

.news-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
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
.msa-error {
  font-size: 12px;
  color: var(--error);
  margin-top: 8px;
}
</style>

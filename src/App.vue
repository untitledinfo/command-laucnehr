<template>
  <div class="shell">
    <Splash :visible="booting" :status-text="bootStatus" />

    <div v-if="store.prefs.particles" class="aurora">
      <span class="blob blob-a"></span>
      <span class="blob blob-b"></span>
      <span class="blob blob-c"></span>
    </div>

    <div class="titlebar">
      <div class="titlebar-drag">
        <div class="brand-mark">C</div>
        <span class="brand accent-gradient-text">COMMAND LAUNCHER</span>
      </div>
      <div class="titlebar-controls">
        <button class="tb-btn tt-wrap" @click="showShortcuts = true">
          &#9410;
          <span class="tt">Keyboard shortcuts (Shift+/)</span>
        </button>
        <button class="tb-btn" @click="minimize">&#8211;</button>
        <button class="tb-btn" @click="maximize">&#9633;</button>
        <button class="tb-btn tb-close" @click="close">&#10005;</button>
      </div>
    </div>

    <div class="body">
      <nav class="rail">
        <RouterLink to="/" class="rail-btn tt-wrap" active-class="active">
          <span class="rail-icon">&#8962;</span>
          <span class="tt">Home &middot; Ctrl+1</span>
        </RouterLink>
        <RouterLink to="/mods" class="rail-btn tt-wrap" active-class="active">
          <span class="rail-icon">&#129513;</span>
          <span class="tt">Mods &amp; Versions &middot; Ctrl+2</span>
        </RouterLink>
        <RouterLink to="/console" class="rail-btn tt-wrap" active-class="active">
          <span class="rail-icon">&#9000;</span>
          <span class="tt">Console &middot; Ctrl+3</span>
        </RouterLink>
        <div class="rail-spacer"></div>
        <div class="rail-status tt-wrap">
          <span class="status-dot" :class="{ live: store.launching }"></span>
          <span class="tt">{{ store.launching ? 'Game running' : 'Idle' }}</span>
        </div>
        <RouterLink to="/settings" class="rail-btn tt-wrap" active-class="active">
          <span class="rail-icon">&#9881;</span>
          <span class="tt">Settings &middot; Ctrl+,</span>
        </RouterLink>
      </nav>

      <main class="content">
        <RouterView v-slot="{ Component }">
          <Transition name="page" mode="out-in">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </main>
    </div>

    <ToastHost />
    <ShortcutsDialog v-if="showShortcuts" @close="showShortcuts = false" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useLauncherStore } from './stores/launcher';
import ToastHost from './components/ToastHost.vue';
import Splash from './components/Splash.vue';
import ShortcutsDialog from './components/ShortcutsDialog.vue';
import { toast } from './lib/toast';

const store = useLauncherStore();
const router = useRouter();
const booting = ref(true);
const bootStatus = ref('Loading your setup...');
const showShortcuts = ref(false);

onMounted(async () => {
  store.applyThemeVars();
  const started = Date.now();
  try {
    bootStatus.value = 'Reading configuration...';
    await store.loadAll();
    bootStatus.value = 'Almost there...';
  } catch (e) {
    toast.error('Startup issue', e instanceof Error ? e.message : String(e));
  }
  const elapsed = Date.now() - started;
  const minSplash = 650;
  setTimeout(() => (booting.value = false), Math.max(0, minSplash - elapsed));

  window.addEventListener('keydown', onKeydown);
  onUnmounted(() => window.removeEventListener('keydown', onKeydown));
});

function onKeydown(e: KeyboardEvent) {
  const ctrl = e.ctrlKey || e.metaKey;
  if (ctrl && e.key === '1') {
    e.preventDefault();
    router.push('/');
  } else if (ctrl && e.key === '2') {
    e.preventDefault();
    router.push('/mods');
  } else if (ctrl && e.key === '3') {
    e.preventDefault();
    router.push('/console');
  } else if (ctrl && e.key === ',') {
    e.preventDefault();
    router.push('/settings');
  } else if (e.shiftKey && e.key === '?') {
    e.preventDefault();
    showShortcuts.value = !showShortcuts.value;
  }
}

function minimize() {
  window.launcherApi.window.minimize();
}
function maximize() {
  window.launcherApi.window.maximize();
}
function close() {
  window.launcherApi.window.close();
}
</script>

<style>
.shell {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: radial-gradient(1200px 600px at 20% -10%, rgba(var(--accent-rgb), 0.12), transparent),
    radial-gradient(1000px 500px at 100% 0%, rgba(var(--accent2-rgb), 0.08), transparent), var(--bg-darkest);
  overflow: hidden;
}

.aurora {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}
.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.35;
  animation: float-blob 14s ease-in-out infinite;
}
.blob-a {
  width: 360px;
  height: 360px;
  top: -100px;
  left: 10%;
  background: var(--accent);
}
.blob-b {
  width: 300px;
  height: 300px;
  top: 30%;
  right: -60px;
  background: var(--accent-2);
  animation-delay: 3s;
}
.blob-c {
  width: 260px;
  height: 260px;
  bottom: -80px;
  left: 35%;
  background: var(--accent-soft);
  animation-delay: 6s;
}

.titlebar {
  position: relative;
  z-index: 2;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  -webkit-app-region: drag;
  padding: 0 8px 0 14px;
  border-bottom: 1px solid var(--border);
  flex: 0 0 auto;
  background: rgba(10, 10, 16, 0.4);
}
.titlebar-drag {
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-mark {
  width: 22px;
  height: 22px;
  border-radius: 7px;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 12px;
  color: #08080c;
}
.brand {
  font-weight: 700;
  font-size: 12.5px;
  letter-spacing: 0.1em;
}
.titlebar-controls {
  display: flex;
  align-items: center;
  gap: 2px;
  -webkit-app-region: no-drag;
}
.tb-btn {
  width: 38px;
  height: 38px;
  background: transparent;
  border: none;
  color: var(--text-dim);
  font-size: 13px;
  border-radius: 8px;
  transition: background var(--dur-fast) ease, color var(--dur-fast) ease;
}
.tb-btn:hover {
  background: var(--bg-panel-light);
  color: var(--text);
}
.tb-close:hover {
  background: var(--error);
  color: #fff;
}

.body {
  position: relative;
  z-index: 1;
  flex: 1 1 auto;
  display: flex;
  min-height: 0;
}

.rail {
  width: 62px;
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 18px 0;
  background: rgba(16, 16, 24, 0.5);
  border-right: 1px solid var(--border);
}
.rail-spacer {
  flex: 1 1 auto;
}
.rail-btn {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-dim);
  text-decoration: none;
  transition: background var(--dur-fast) ease, color var(--dur-fast) ease, transform var(--dur-fast) var(--ease-spring);
}
.rail-btn:hover {
  background: var(--bg-panel-light);
  color: var(--text);
  transform: scale(1.06);
}
.rail-btn.active {
  background: linear-gradient(135deg, rgba(var(--accent-rgb), 0.25), rgba(var(--accent2-rgb), 0.18));
  color: var(--text);
  box-shadow: inset 0 0 0 1px rgba(var(--accent-rgb), 0.4);
}
.rail-icon {
  font-size: 18px;
}
.rail-status {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 20px;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--text-faint);
}
.status-dot.live {
  background: var(--success);
  box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.6);
  animation: pulse-glow 1.6s ease-out infinite;
}

.content {
  flex: 1 1 auto;
  min-width: 0;
  overflow-y: auto;
  padding: 24px;
}
</style>

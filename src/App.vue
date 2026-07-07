<template>
  <div class="shell">
    <div class="titlebar">
      <div class="titlebar-drag">
        <span class="brand accent-gradient-text">COMMAND LAUNCHER</span>
      </div>
      <div class="titlebar-controls">
        <button class="tb-btn" @click="minimize">&#8211;</button>
        <button class="tb-btn" @click="maximize">&#9633;</button>
        <button class="tb-btn tb-close" @click="close">&#10005;</button>
      </div>
    </div>

    <div class="body">
      <nav class="rail">
        <RouterLink to="/" class="rail-btn" active-class="active" title="Home">
          <span class="rail-icon">&#8962;</span>
        </RouterLink>
        <RouterLink to="/mods" class="rail-btn" active-class="active" title="Mods &amp; Versions">
          <span class="rail-icon">&#9881;</span>
        </RouterLink>
        <RouterLink to="/console" class="rail-btn" active-class="active" title="Console">
          <span class="rail-icon">&#9000;</span>
        </RouterLink>
        <div class="rail-spacer"></div>
        <RouterLink to="/settings" class="rail-btn" active-class="active" title="Settings">
          <span class="rail-icon">&#9881;&#65039;</span>
        </RouterLink>
      </nav>

      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useLauncherStore } from './stores/launcher';

const store = useLauncherStore();

onMounted(() => {
  store.loadAll();
});

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
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: radial-gradient(1200px 600px at 20% -10%, rgba(139, 92, 246, 0.12), transparent),
    radial-gradient(1000px 500px at 100% 0%, rgba(34, 211, 238, 0.08), transparent), var(--bg-darkest);
}

.titlebar {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  -webkit-app-region: drag;
  padding: 0 8px 0 16px;
  border-bottom: 1px solid var(--border);
  flex: 0 0 auto;
}
.titlebar-drag {
  display: flex;
  align-items: center;
  gap: 8px;
}
.brand {
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.08em;
}
.titlebar-controls {
  display: flex;
  -webkit-app-region: no-drag;
}
.tb-btn {
  width: 40px;
  height: 40px;
  background: transparent;
  border: none;
  color: var(--text-dim);
  font-size: 14px;
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
  flex: 1 1 auto;
  display: flex;
  min-height: 0;
}

.rail {
  width: 64px;
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 0;
  background: var(--bg-dark);
  border-right: 1px solid var(--border);
}
.rail-spacer {
  flex: 1 1 auto;
}
.rail-btn {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-dim);
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
}
.rail-btn:hover {
  background: var(--bg-panel-light);
  color: var(--text);
}
.rail-btn.active {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.25), rgba(34, 211, 238, 0.18));
  color: var(--text);
  box-shadow: inset 0 0 0 1px rgba(139, 92, 246, 0.4);
}
.rail-icon {
  font-size: 18px;
}

.content {
  flex: 1 1 auto;
  min-width: 0;
  overflow-y: auto;
  padding: 24px;
}
</style>

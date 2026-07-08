<template>
  <div class="settings">
    <h2>Settings</h2>
    <div class="panel glass-panel" v-if="form">
      <div class="field">
        <label>Memory Allocation (MB): {{ form.ram }}</label>
        <input type="range" min="1024" max="16384" step="256" v-model.number="form.ram" />
      </div>

      <div class="field">
        <label>Java Executable Path</label>
        <div class="row">
          <input v-model="form.javaPath" placeholder="Auto-detect" />
          <button class="secondary" @click="pickJava">Browse</button>
        </div>
      </div>

      <div class="field">
        <label>Extra JVM Arguments</label>
        <input v-model="form.extraJvmArgs" placeholder="-XX:+UseG1GC" />
      </div>

      <div class="field">
        <label>.minecraft Directory</label>
        <div class="row">
          <input v-model="form.minecraftDirectory" />
          <button class="secondary" @click="pickDir">Browse</button>
        </div>
      </div>

      <div class="field">
        <label>Default Server</label>
        <input v-model="form.server" placeholder="play.pgcmc.fun:25565" />
      </div>

      <button class="save-btn" @click="save">{{ saved ? 'Saved!' : 'Save Settings' }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useLauncherStore } from '../stores/launcher';

const store = useLauncherStore();
const form = ref<any>(null);
const saved = ref(false);

onMounted(async () => {
  if (!store.config) await store.loadAll();
  form.value = { ...store.config };
});

async function pickJava() {
  const p = await window.launcherApi.config.pickJavaPath();
  if (p) form.value.javaPath = p;
}
async function pickDir() {
  const p = await window.launcherApi.config.pickMinecraftDir();
  if (p) form.value.minecraftDirectory = p;
}
async function save() {
  await window.launcherApi.config.set(form.value);
  store.config = { ...form.value };
  saved.value = true;
  setTimeout(() => (saved.value = false), 1500);
}
</script>

<style scoped>
.settings {
  max-width: 640px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
h2 {
  margin: 0;
}
.panel {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
label {
  font-size: 12px;
  color: var(--text-dim);
}
.row {
  display: flex;
  gap: 8px;
}
.row input {
  flex: 1 1 auto;
}
button.secondary {
  background: var(--bg-panel-light);
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text);
  padding: 8px 12px;
  font-size: 12px;
}
button.secondary:hover {
  border-color: var(--accent);
}
.save-btn {
  align-self: flex-start;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  border: none;
  border-radius: 999px;
  color: #0b0b12;
  font-weight: 700;
  padding: 10px 24px;
  font-size: 13px;
}
</style>

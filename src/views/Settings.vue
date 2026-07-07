<template>
  <div class="settings">
    <h2 class="fade-up-in">Settings</h2>

    <div class="panel glass-panel fade-up-in" v-if="form">
      <h3 class="section-title">Appearance</h3>

      <div class="field">
        <label>Theme</label>
        <div class="theme-grid">
          <button
            v-for="(preset, key) in THEME_PRESETS"
            :key="key"
            class="theme-swatch"
            :class="{ active: store.prefs.theme === key }"
            @click="store.setTheme(key as any)"
          >
            <span class="swatch-dot" :style="{ background: `linear-gradient(135deg, ${preset.accent}, ${preset.accent2})` }"></span>
            {{ preset.label }}
          </button>
        </div>
      </div>

      <div class="field">
        <label>Custom accent color</label>
        <div class="row">
          <input type="color" v-model="customAccent" @change="applyCustomAccent" class="color-input" />
          <input type="color" v-model="customAccent2" @change="applyCustomAccent" class="color-input" />
          <button class="btn small" @click="applyCustomAccent">Apply custom</button>
        </div>
      </div>

      <div class="field toggle-field">
        <label>Animated background particles</label>
        <label class="switch">
          <input type="checkbox" v-model="store.prefs.particles" @change="store.persist" />
          <span class="switch-track"></span>
        </label>
      </div>

      <div class="field toggle-field">
        <label>Reduce motion (accessibility)</label>
        <label class="switch">
          <input type="checkbox" v-model="store.prefs.reduceMotion" @change="onReduceMotion" />
          <span class="switch-track"></span>
        </label>
      </div>
    </div>

    <div class="panel glass-panel fade-up-in" v-if="form">
      <h3 class="section-title">Performance</h3>

      <div class="field">
        <label>Memory Allocation: <strong>{{ form.ram }} MB</strong></label>
        <input type="range" min="1024" max="16384" step="256" v-model.number="form.ram" />
        <div class="ram-bar">
          <div class="ram-fill" :style="{ width: ramPercent + '%' }"></div>
        </div>
        <div class="preset-row">
          <button class="btn small" @click="form.ram = 4096">4 GB</button>
          <button class="btn small" @click="form.ram = 8192">8 GB</button>
          <button class="btn small" @click="form.ram = 12288">12 GB</button>
          <button class="btn small" @click="form.ram = 16384">Max</button>
        </div>
      </div>

      <div class="field">
        <label>JVM Argument Preset</label>
        <select v-model="store.prefs.jvmPreset" @change="applyJvmPreset">
          <option value="balanced">Balanced (default)</option>
          <option value="g1gc">G1GC &mdash; smoother frame times</option>
          <option value="zgc">ZGC &mdash; low pause, needs modern JVM</option>
          <option value="custom">Custom (edit below)</option>
        </select>
      </div>

      <div class="field">
        <label>Extra JVM Arguments</label>
        <input v-model="form.extraJvmArgs" placeholder="-XX:+UseG1GC" />
      </div>
    </div>

    <div class="panel glass-panel fade-up-in" v-if="form">
      <h3 class="section-title">Game &amp; Files</h3>

      <div class="field">
        <label>Java Executable Path</label>
        <div class="row">
          <input v-model="form.javaPath" placeholder="Auto-detect" />
          <button class="btn" @click="pickJava">Browse</button>
        </div>
      </div>

      <div class="field">
        <label>.minecraft Directory</label>
        <div class="row">
          <input v-model="form.minecraftDirectory" />
          <button class="btn" @click="pickDir">Browse</button>
        </div>
      </div>

      <div class="field">
        <label>Default Server</label>
        <input v-model="form.server" placeholder="play.example.net:25565" />
      </div>
    </div>

    <div class="panel glass-panel fade-up-in" v-if="form">
      <h3 class="section-title">Integrations &amp; Behavior</h3>

      <div class="field toggle-field">
        <label>Discord Rich Presence</label>
        <label class="switch">
          <input type="checkbox" v-model="store.prefs.discordRpc" @change="store.persist" />
          <span class="switch-track"></span>
        </label>
      </div>
      <div class="field toggle-field">
        <label>Minimize to tray on close</label>
        <label class="switch">
          <input type="checkbox" v-model="store.prefs.minimizeToTray" @change="store.persist" />
          <span class="switch-track"></span>
        </label>
      </div>
      <div class="field toggle-field">
        <label>UI sound effects</label>
        <label class="switch">
          <input type="checkbox" v-model="store.prefs.soundEffects" @change="store.persist" />
          <span class="switch-track"></span>
        </label>
      </div>
      <div class="field toggle-field">
        <label>Auto-scroll console output</label>
        <label class="switch">
          <input type="checkbox" v-model="store.prefs.autoscrollConsole" @change="store.persist" />
          <span class="switch-track"></span>
        </label>
      </div>
    </div>

    <div class="panel glass-panel fade-up-in" v-if="form">
      <h3 class="section-title">Backup &amp; Diagnostics</h3>
      <div class="btn-grid">
        <button class="btn" @click="exportSettings">&#11015; Export Settings</button>
        <button class="btn" @click="triggerImport">&#11014; Import Settings</button>
        <button class="btn" @click="copyDebugInfo">&#128203; Copy Debug Info</button>
        <button class="btn danger" @click="confirmReset = true">&#8635; Reset to Defaults</button>
      </div>
      <input ref="importInput" type="file" accept="application/json" class="hidden-file" @change="importSettings" />
    </div>

    <div class="save-bar fade-up-in">
      <button class="btn glow-save" @click="save">
        <span v-if="saved">&#10003; Saved</span>
        <span v-else>Save Settings</span>
      </button>
    </div>

    <ConfirmDialog
      v-if="confirmReset"
      title="Reset all preferences?"
      message="Theme, favorites, saved servers, and playtime stats will be cleared. Java/RAM settings in your config are not affected."
      confirm-label="Reset"
      danger
      @confirm="doReset"
      @cancel="confirmReset = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useLauncherStore } from '../stores/launcher';
import ConfirmDialog from '../components/ConfirmDialog.vue';
import { THEME_PRESETS, exportPrefsJson, resetPrefs, loadPrefs } from '../lib/prefs';
import { toast } from '../lib/toast';

const store = useLauncherStore();
const form = ref<any>(null);
const saved = ref(false);
const confirmReset = ref(false);
const importInput = ref<HTMLInputElement | null>(null);

const customAccent = ref(store.prefs.accent);
const customAccent2 = ref(store.prefs.accent2);

const ramPercent = computed(() => (form.value ? Math.min(100, (form.value.ram / 16384) * 100) : 0));

onMounted(async () => {
  if (!store.config) await store.loadAll();
  form.value = { ...store.config };
});

function applyCustomAccent() {
  store.setCustomAccent(customAccent.value, customAccent2.value);
  toast.success('Accent color updated');
}

function onReduceMotion() {
  store.persist();
  store.applyThemeVars();
}

function applyJvmPreset() {
  const presets: Record<string, string> = {
    balanced: '',
    g1gc: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
    zgc: '-XX:+UseZGC -XX:+ZGenerational',
    custom: form.value.extraJvmArgs
  };
  if (store.prefs.jvmPreset !== 'custom' && form.value) {
    form.value.extraJvmArgs = presets[store.prefs.jvmPreset] ?? '';
  }
  store.persist();
}

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
  toast.success('Settings saved');
  setTimeout(() => (saved.value = false), 1500);
}

function exportSettings() {
  const json = exportPrefsJson(store.prefs, { config: form.value });
  const blob = new Blob([json], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'command-launcher-settings.json';
  a.click();
  URL.revokeObjectURL(url);
  toast.success('Settings exported');
}

function triggerImport() {
  importInput.value?.click();
}

async function importSettings(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;
  try {
    const text = await file.text();
    const data = JSON.parse(text);
    if (data.config) form.value = { ...form.value, ...data.config };
    Object.assign(store.prefs, data);
    store.persist();
    store.applyThemeVars();
    toast.success('Settings imported');
  } catch (err) {
    toast.error('Import failed', 'That file could not be read as launcher settings.');
  } finally {
    (e.target as HTMLInputElement).value = '';
  }
}

function copyDebugInfo() {
  const info = {
    platform: navigator.userAgent,
    theme: store.prefs.theme,
    ram: form.value?.ram,
    javaPath: form.value?.javaPath || 'auto-detect',
    installedVersions: store.installedVersions.length,
    activeAccount: store.activeAccount?.type ?? 'none'
  };
  navigator.clipboard?.writeText(JSON.stringify(info, null, 2));
  toast.success('Debug info copied');
}

function doReset() {
  confirmReset.value = false;
  const fresh = resetPrefs();
  Object.assign(store.prefs, fresh);
  store.applyThemeVars();
  customAccent.value = store.prefs.accent;
  customAccent2.value = store.prefs.accent2;
  toast.success('Preferences reset');
}
</script>

<style scoped>
.settings {
  max-width: 640px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-bottom: 12px;
}
h2 {
  margin: 0;
}
.panel {
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.section-title {
  margin: 0;
  font-size: 12.5px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-faint);
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
  align-items: center;
}
.row input:not([type='color']) {
  flex: 1 1 auto;
}
.color-input {
  width: 40px;
  height: 34px;
  padding: 2px;
  cursor: pointer;
}
.theme-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.theme-swatch {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--bg-panel-light);
  color: var(--text-dim);
  font-size: 12px;
  transition: border-color var(--dur-fast) ease, transform var(--dur-fast) var(--ease-spring);
}
.theme-swatch:hover {
  transform: translateY(-1px);
}
.theme-swatch.active {
  border-color: var(--accent);
  color: var(--text);
  background: rgba(var(--accent-rgb), 0.12);
}
.swatch-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  flex: 0 0 auto;
}
.toggle-field {
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.switch {
  position: relative;
  width: 42px;
  height: 24px;
  display: inline-block;
}
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}
.switch-track {
  position: absolute;
  inset: 0;
  background: var(--bg-panel-lighter);
  border: 1px solid var(--border);
  border-radius: 999px;
  transition: background var(--dur-fast) ease;
}
.switch-track::before {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--text-dim);
  top: 2px;
  left: 2px;
  transition: transform var(--dur-fast) var(--ease-spring), background var(--dur-fast) ease;
}
.switch input:checked + .switch-track {
  background: rgba(var(--accent-rgb), 0.35);
  border-color: var(--accent);
}
.switch input:checked + .switch-track::before {
  transform: translateX(18px);
  background: var(--accent);
}
.ram-bar {
  height: 6px;
  border-radius: 4px;
  background: var(--bg-panel-light);
  overflow: hidden;
}
.ram-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent), var(--accent-2));
  transition: width var(--dur-fast) ease;
}
.preset-row {
  display: flex;
  gap: 8px;
}
.btn-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.hidden-file {
  display: none;
}
.save-bar {
  display: flex;
  justify-content: flex-end;
  position: sticky;
  bottom: 0;
  padding-top: 6px;
}
.glow-save {
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  color: #0b0b12;
  border: none;
  border-radius: 999px;
  padding: 11px 26px;
  font-weight: 700;
}
.glow-save:hover {
  filter: brightness(1.05);
}
</style>

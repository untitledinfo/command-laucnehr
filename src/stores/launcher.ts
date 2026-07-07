import { defineStore } from 'pinia';
import { loadPrefs, savePrefs, THEME_PRESETS, uid, type PrefsShape, type SavedServer } from '../lib/prefs';

export interface Account {
  type: 'offline' | 'msa';
  name: string;
  uuid: string;
  accessToken: string;
  refreshToken: string;
}

export interface LauncherConfig {
  minecraftDirectory: string;
  ram: number;
  modLoader: string;
  extraJvmArgs: string;
  javaPath: string;
  accounts: Account[];
  activeAccountUuid: string;
  server: string;
}

export const useLauncherStore = defineStore('launcher', {
  state: () => ({
    config: null as LauncherConfig | null,
    accounts: [] as Account[],
    activeAccount: null as Account | null,
    installedVersions: [] as string[],
    consoleLines: [] as string[],
    launching: false,
    deviceCode: null as { userCode: string; verificationUri: string } | null,
    booting: true,
    prefs: loadPrefs() as PrefsShape,
    launchStartedAt: null as number | null
  }),
  getters: {
    themeLabel(state): string {
      return THEME_PRESETS[state.prefs.theme]?.label ?? 'Custom';
    },
    isFavorite: (state) => (versionId: string) => state.prefs.favoriteVersions.includes(versionId)
  },
  actions: {
    async loadAll() {
      this.config = await window.launcherApi.config.get();
      this.accounts = await window.launcherApi.accounts.list();
      this.activeAccount = await window.launcherApi.accounts.getActive();
      this.installedVersions = await window.launcherApi.versions.installed();
    },
    async refreshAccounts() {
      this.accounts = await window.launcherApi.accounts.list();
      this.activeAccount = await window.launcherApi.accounts.getActive();
    },
    async refreshInstalledVersions() {
      this.installedVersions = await window.launcherApi.versions.installed();
    },
    appendConsole(line: string) {
      this.consoleLines.push(line);
      if (this.consoleLines.length > 5000) this.consoleLines.shift();
    },
    clearConsole() {
      this.consoleLines = [];
    },

    // ---- Preferences layer (theme, favorites, servers, playtime) ----
    persist() {
      savePrefs(this.prefs);
    },
    setTheme(theme: PrefsShape['theme']) {
      this.prefs.theme = theme;
      const preset = THEME_PRESETS[theme];
      this.prefs.accent = preset.accent;
      this.prefs.accent2 = preset.accent2;
      this.applyThemeVars();
      this.persist();
    },
    setCustomAccent(accent: string, accent2: string) {
      this.prefs.accent = accent;
      this.prefs.accent2 = accent2;
      this.applyThemeVars();
      this.persist();
    },
    applyThemeVars() {
      const root = document.documentElement;
      root.setAttribute('data-theme', this.prefs.theme);
      root.style.setProperty('--accent', this.prefs.accent);
      root.style.setProperty('--accent-2', this.prefs.accent2);
      const hex = this.prefs.accent.replace('#', '');
      if (hex.length === 6) {
        const r = parseInt(hex.slice(0, 2), 16);
        const g = parseInt(hex.slice(2, 4), 16);
        const b = parseInt(hex.slice(4, 6), 16);
        root.style.setProperty('--accent-rgb', `${r}, ${g}, ${b}`);
      }
      root.classList.toggle('reduce-motion', this.prefs.reduceMotion);
    },
    toggleFavorite(versionId: string) {
      const list = this.prefs.favoriteVersions;
      const idx = list.indexOf(versionId);
      if (idx === -1) list.push(versionId);
      else list.splice(idx, 1);
      this.persist();
    },
    addServer(name: string, address: string) {
      const entry: SavedServer = { id: uid(), name: name || address, address };
      this.prefs.servers.push(entry);
      this.persist();
      return entry;
    },
    removeServer(id: string) {
      this.prefs.servers = this.prefs.servers.filter((s) => s.id !== id);
      this.persist();
    },
    recordPlaytimeStart() {
      this.launchStartedAt = Date.now();
    },
    recordPlaytimeEnd(versionId: string) {
      if (!this.launchStartedAt) return;
      const minutes = Math.max(1, Math.round((Date.now() - this.launchStartedAt) / 60000));
      this.prefs.playtime[versionId] = (this.prefs.playtime[versionId] ?? 0) + minutes;
      this.prefs.lastPlayed[versionId] = new Date().toISOString();
      this.launchStartedAt = null;
      this.persist();
    }
  }
});

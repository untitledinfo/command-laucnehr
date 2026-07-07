import { defineStore } from 'pinia';

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
    deviceCode: null as { userCode: string; verificationUri: string } | null
  }),
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
    }
  }
});

import { contextBridge, ipcRenderer } from 'electron';

const api = {
  window: {
    minimize: () => ipcRenderer.send('window:minimize'),
    maximize: () => ipcRenderer.send('window:maximize'),
    close: () => ipcRenderer.send('window:close')
  },

  config: {
    get: () => ipcRenderer.invoke('config:get'),
    set: (patch: Record<string, unknown>) => ipcRenderer.invoke('config:set', patch),
    pickMinecraftDir: () => ipcRenderer.invoke('config:pickMinecraftDir'),
    pickJavaPath: () => ipcRenderer.invoke('config:pickJavaPath')
  },

  accounts: {
    list: () => ipcRenderer.invoke('accounts:list'),
    getActive: () => ipcRenderer.invoke('accounts:getActive'),
    setActive: (uuid: string) => ipcRenderer.invoke('accounts:setActive', uuid),
    addOffline: (username: string) => ipcRenderer.invoke('accounts:addOffline', username),
    remove: (uuid: string) => ipcRenderer.invoke('accounts:remove', uuid),
    loginMsa: () => ipcRenderer.invoke('accounts:loginMsa'),
    cancelMsaLogin: () => ipcRenderer.invoke('accounts:cancelMsaLogin'),
    onDeviceCode: (cb: (dc: any) => void) => {
      const listener = (_: unknown, dc: any) => cb(dc);
      ipcRenderer.on('accounts:deviceCode', listener);
      return () => ipcRenderer.removeListener('accounts:deviceCode', listener);
    }
  },

  versions: {
    fetchManifest: () => ipcRenderer.invoke('versions:fetchManifest'),
    installed: () => ipcRenderer.invoke('versions:installed'),
    delete: (versionId: string) => ipcRenderer.invoke('versions:delete', versionId),
    install: (versionId: string) => ipcRenderer.invoke('versions:install', versionId),
    fabricLoaders: (mcVersion: string) => ipcRenderer.invoke('versions:fabricLoaders', mcVersion),
    installFabric: (mcVersion: string, loaderVersion: string) =>
      ipcRenderer.invoke('versions:installFabric', mcVersion, loaderVersion),
    recommendedForge: (mcVersion: string) => ipcRenderer.invoke('versions:recommendedForge', mcVersion),
    installForge: (mcVersion: string, forgeVersion: string) =>
      ipcRenderer.invoke('versions:installForge', mcVersion, forgeVersion),
    onProgress: (cb: (p: { text: string; done: number; total: number }) => void) => {
      const listener = (_: unknown, p: any) => cb(p);
      ipcRenderer.on('versions:progress', listener);
      return () => ipcRenderer.removeListener('versions:progress', listener);
    }
  },

  launcher: {
    launch: (versionId: string, serverAddress: string | null) =>
      ipcRenderer.invoke('launcher:launch', versionId, serverAddress),
    onOutput: (cb: (line: string) => void) => {
      const listener = (_: unknown, line: string) => cb(line);
      ipcRenderer.on('launcher:output', listener);
      return () => ipcRenderer.removeListener('launcher:output', listener);
    },
    onExit: (cb: (code: number | null) => void) => {
      const listener = (_: unknown, code: number | null) => cb(code);
      ipcRenderer.on('launcher:exit', listener);
      return () => ipcRenderer.removeListener('launcher:exit', listener);
    }
  },

  server: {
    ping: (address: string) => ipcRenderer.invoke('server:ping', address)
  },

  folders: {
    openMods: (versionId: string) => ipcRenderer.invoke('folders:openMods', versionId),
    openResourcePacks: (versionId: string) => ipcRenderer.invoke('folders:openResourcePacks', versionId),
    openScreenshots: (versionId: string) => ipcRenderer.invoke('folders:openScreenshots', versionId),
    openRoot: () => ipcRenderer.invoke('folders:openRoot')
  }
};

contextBridge.exposeInMainWorld('launcherApi', api);

export type LauncherApi = typeof api;

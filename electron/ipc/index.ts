import { ipcMain, dialog, shell, BrowserWindow } from 'electron';
import path from 'path';
import fs from 'fs';
import { ConfigStore, AccountManager } from '../lib/configStore';
import { MinecraftInstaller } from '../lib/minecraftInstaller';
import * as fabricSupport from '../lib/fabricSupport';
import * as forgeSupport from '../lib/forgeSupport';
import { MinecraftLauncher } from '../lib/minecraftLauncher';
import * as microsoftAuth from '../lib/microsoftAuth';
import * as serverPinger from '../lib/serverPinger';

export function registerIpc(config: ConfigStore, getWindow: () => BrowserWindow | null) {
  const accountManager = new AccountManager(config);

  // --- config ---
  ipcMain.handle('config:get', () => config.data);
  ipcMain.handle('config:set', (_e, patch: Record<string, unknown>) => {
    Object.assign(config.data, patch);
    config.save();
    return config.data;
  });
  ipcMain.handle('config:pickMinecraftDir', async () => {
    const win = getWindow();
    if (!win) return null;
    const result = await dialog.showOpenDialog(win, { properties: ['openDirectory', 'createDirectory'] });
    if (result.canceled || !result.filePaths[0]) return null;
    config.data.minecraftDirectory = result.filePaths[0];
    config.save();
    return result.filePaths[0];
  });
  ipcMain.handle('config:pickJavaPath', async () => {
    const win = getWindow();
    if (!win) return null;
    const result = await dialog.showOpenDialog(win, { properties: ['openFile'] });
    if (result.canceled || !result.filePaths[0]) return null;
    config.data.javaPath = result.filePaths[0];
    config.save();
    return result.filePaths[0];
  });

  // --- accounts ---
  ipcMain.handle('accounts:list', () => accountManager.list());
  ipcMain.handle('accounts:getActive', () => accountManager.getActive());
  ipcMain.handle('accounts:setActive', (_e, uuid: string) => {
    accountManager.setActive(uuid);
    return accountManager.getActive();
  });
  ipcMain.handle('accounts:addOffline', (_e, username: string) => accountManager.addOffline(username));
  ipcMain.handle('accounts:remove', (_e, uuid: string) => {
    accountManager.remove(uuid);
    return accountManager.list();
  });
  ipcMain.handle('accounts:loginMsa', async (event) => {
    const dc = await microsoftAuth.requestDeviceCode();
    event.sender.send('accounts:deviceCode', dc);
    const msTokens = await microsoftAuth.pollForToken(dc);
    const account = await microsoftAuth.completeLogin(msTokens.access_token, msTokens.refresh_token);
    accountManager.addOrUpdateMsa(account.name, account.uuid, account.accessToken, account.refreshToken);
    accountManager.setActive(account.uuid);
    return account;
  });

  // --- versions / install ---
  ipcMain.handle('versions:fetchManifest', () => MinecraftInstaller.fetchVersionList());
  ipcMain.handle('versions:installed', () => new MinecraftInstaller(config.getMinecraftDir()).getInstalledVersions());
  ipcMain.handle('versions:delete', (_e, versionId: string) => {
    new MinecraftInstaller(config.getMinecraftDir()).deleteVersion(versionId);
    return true;
  });

  function progressListener(event: Electron.IpcMainInvokeEvent) {
    return (text: string, done: number, total: number) => {
      event.sender.send('versions:progress', { text, done, total });
    };
  }

  ipcMain.handle('versions:install', async (event, versionId: string) => {
    const installer = new MinecraftInstaller(config.getMinecraftDir(), progressListener(event));
    await installer.installVersion(versionId);
    return versionId;
  });

  ipcMain.handle('versions:fabricLoaders', (_e, mcVersion: string) => fabricSupport.listLoaderVersions(mcVersion));

  ipcMain.handle('versions:installFabric', async (event, mcVersion: string, loaderVersion: string) => {
    // Ensure the vanilla parent is installed first (assets/libs are shared).
    const installer = new MinecraftInstaller(config.getMinecraftDir(), progressListener(event));
    if (!installer.getInstalledVersions().includes(mcVersion)) {
      await installer.installVersion(mcVersion);
    }
    return fabricSupport.install(config.getMinecraftDir(), mcVersion, loaderVersion, progressListener(event));
  });

  ipcMain.handle('versions:recommendedForge', (_e, mcVersion: string) => forgeSupport.recommendedForgeVersion(mcVersion));

  ipcMain.handle('versions:installForge', async (event, mcVersion: string, forgeVersion: string) => {
    const javaExe = MinecraftLauncher.resolveJavaExecutable(config.data.javaPath);
    return forgeSupport.install(config.getMinecraftDir(), mcVersion, forgeVersion, javaExe, progressListener(event));
  });

  // --- launch ---
  ipcMain.handle('launcher:launch', async (event, versionId: string, serverAddress: string | null) => {
    const account = accountManager.getActive();
    if (!account) throw new Error('No active account selected.');
    const launcher = new MinecraftLauncher(config);
    const proc = await launcher.launch(versionId, account, serverAddress, (line) => {
      event.sender.send('launcher:output', line);
    });
    proc.on('exit', (code) => event.sender.send('launcher:exit', code));
    return true;
  });

  // --- server ping ---
  ipcMain.handle('server:ping', (_e, address: string) => {
    let host = address;
    let port = 25565;
    if (address.includes(':')) {
      const [h, p] = address.split(':', 2);
      host = h;
      port = parseInt(p, 10) || 25565;
    }
    return serverPinger.ping(host, port, 4000);
  });

  // --- folders ---
  ipcMain.handle('folders:openMods', (_e, versionId: string) => {
    const dir = path.join(config.getMinecraftDir(), 'profiles', versionId, 'mods');
    fs.mkdirSync(dir, { recursive: true });
    return shell.openPath(dir);
  });
  ipcMain.handle('folders:openResourcePacks', (_e, versionId: string) => {
    const dir = path.join(config.getMinecraftDir(), 'profiles', versionId, 'resourcepacks');
    fs.mkdirSync(dir, { recursive: true });
    return shell.openPath(dir);
  });
  ipcMain.handle('folders:openScreenshots', (_e, versionId: string) => {
    const dir = path.join(config.getMinecraftDir(), 'profiles', versionId, 'screenshots');
    fs.mkdirSync(dir, { recursive: true });
    return shell.openPath(dir);
  });
  ipcMain.handle('folders:openRoot', () => shell.openPath(config.getMinecraftDir()));
}

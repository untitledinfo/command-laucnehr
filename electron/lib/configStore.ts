import fs from 'fs';
import os from 'os';
import path from 'path';
import crypto from 'crypto';
import { app } from 'electron';

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

function defaultMinecraftDir(): string {
  const home = os.homedir();
  const platform = process.platform;
  if (platform === 'win32') {
    return path.join(process.env.APPDATA || home, '.minecraft');
  } else if (platform === 'darwin') {
    return path.join(home, 'Library', 'Application Support', 'minecraft');
  }
  return path.join(home, '.minecraft');
}

function defaults(): LauncherConfig {
  return {
    minecraftDirectory: defaultMinecraftDir(),
    ram: 4096,
    modLoader: 'None',
    extraJvmArgs: '',
    javaPath: '',
    accounts: [],
    activeAccountUuid: '',
    server: ''
  };
}

// Stored alongside the app's user data directory (equivalent to launcher_config.json
// in the working directory for the Java version, but this is more robust for a
// packaged desktop app).
function configFile(): string {
  return path.join(app.getPath('userData'), 'launcher_config.json');
}

export class ConfigStore {
  data: LauncherConfig;

  private constructor(data: LauncherConfig) {
    this.data = data;
  }

  static load(): ConfigStore {
    const d = defaults();
    const file = configFile();
    if (fs.existsSync(file)) {
      try {
        const loaded = JSON.parse(fs.readFileSync(file, 'utf-8'));
        Object.assign(d, loaded);
      } catch (e) {
        console.warn('Warning: could not load config, using defaults:', e);
      }
    }
    return new ConfigStore(d);
  }

  save(): void {
    try {
      fs.mkdirSync(path.dirname(configFile()), { recursive: true });
      fs.writeFileSync(configFile(), JSON.stringify(this.data, null, 2), 'utf-8');
    } catch (e) {
      console.error('Error saving config:', e);
    }
  }

  getMinecraftDir(): string {
    return this.data.minecraftDirectory || defaultMinecraftDir();
  }
}

/** Matches Mojang's own offline-mode UUID algorithm (version-3 name UUID). */
export function offlineUuid(username: string): string {
  const hash = crypto.createHash('md5').update(`OfflinePlayer:${username}`).digest();
  // Set version (3) and variant bits per RFC 4122, exactly as Java's UUID.nameUUIDFromBytes does.
  hash[6] = (hash[6] & 0x0f) | 0x30;
  hash[8] = (hash[8] & 0x3f) | 0x80;
  const h = hash.toString('hex');
  return `${h.slice(0, 8)}-${h.slice(8, 12)}-${h.slice(12, 16)}-${h.slice(16, 20)}-${h.slice(20)}`;
}

export class AccountManager {
  constructor(private config: ConfigStore) {}

  list(): Account[] {
    return this.config.data.accounts;
  }

  getActive(): Account | null {
    const activeUuid = this.config.data.activeAccountUuid;
    return this.list().find((a) => a.uuid === activeUuid) || null;
  }

  setActive(uuid: string): void {
    this.config.data.activeAccountUuid = uuid;
    this.config.save();
  }

  nameTaken(name: string): boolean {
    return this.list().some((a) => a.name.toLowerCase() === name.toLowerCase());
  }

  addOffline(username: string): Account {
    const uuid = offlineUuid(username);
    const acc: Account = { type: 'offline', name: username, uuid, accessToken: '', refreshToken: '' };
    this.config.data.accounts.push(acc);
    this.config.save();
    return acc;
  }

  addOrUpdateMsa(name: string, uuid: string, accessToken: string, refreshToken: string): void {
    const existing = this.config.data.accounts.find((a) => a.uuid === uuid);
    if (existing) {
      existing.name = name;
      existing.accessToken = accessToken;
      existing.refreshToken = refreshToken;
    } else {
      this.config.data.accounts.push({ type: 'msa', name, uuid, accessToken, refreshToken });
    }
    this.config.save();
  }

  remove(uuid: string): void {
    this.config.data.accounts = this.config.data.accounts.filter((a) => a.uuid !== uuid);
    if (this.config.data.activeAccountUuid === uuid) this.config.data.activeAccountUuid = '';
    this.config.save();
  }
}

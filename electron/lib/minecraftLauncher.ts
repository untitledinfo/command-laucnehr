import fs from 'fs';
import path from 'path';
import { spawn, ChildProcess } from 'child_process';
import { isAllowed } from './ruleUtil';
import { coordToPath } from './mavenUtil';
import type { ConfigStore, Account } from './configStore';

export type OutputListener = (line: string) => void;

export class MinecraftLauncher {
  constructor(private config: ConfigStore) {}

  /**
   * Launches the given (already installed) version for the given account.
   * versionId is the id used for the game directory/profile (e.g. "1.20.4"
   * or a fabric synthetic id like "1.20.4-fabric-0.15.11").
   */
  async launch(
    versionId: string,
    account: Account,
    serverAddress: string | null,
    output?: OutputListener
  ): Promise<ChildProcess> {
    const mcDir = this.config.getMinecraftDir();
    const versionDir = path.join(mcDir, 'versions', versionId);
    const versionJsonFile = path.join(versionDir, `${versionId}.json`);
    if (!fs.existsSync(versionJsonFile)) {
      throw new Error(`Version metadata not found for ${versionId}. Install it first.`);
    }
    const versionJson = JSON.parse(fs.readFileSync(versionJsonFile, 'utf-8'));

    // Resolve inheritance (Fabric profiles set inheritsFrom -> vanilla version)
    const inheritsFrom: string | undefined = versionJson.inheritsFrom;
    let parentJson: any = null;
    let parentId: string | null = null;
    if (inheritsFrom) {
      parentId = inheritsFrom;
      const parentFile = path.join(mcDir, 'versions', parentId, `${parentId}.json`);
      parentJson = JSON.parse(fs.readFileSync(parentFile, 'utf-8'));
    }

    const mainClass = versionJson.mainClass ?? parentJson?.mainClass ?? 'net.minecraft.client.main.Main';

    const jarVersionId = parentId ?? versionId;
    const clientJar = path.join(mcDir, 'versions', jarVersionId, `${jarVersionId}.jar`);

    // --- classpath ---
    const classpath: string[] = [];
    this.collectLibraryPaths(mcDir, versionJson, classpath);
    if (parentJson) this.collectLibraryPaths(mcDir, parentJson, classpath);
    classpath.push(clientJar);

    // --- natives dir ---
    const nativesDir = path.join(mcDir, 'versions', jarVersionId, `${jarVersionId}-natives`);

    // --- profile / game dir ---
    const profileDir = path.join(mcDir, 'profiles', versionId);
    fs.mkdirSync(path.join(profileDir, 'mods'), { recursive: true });

    let assetIndexId = 'legacy';
    const assetIndexObj = versionJson.assetIndex ?? parentJson?.assetIndex;
    if (assetIndexObj) assetIndexId = assetIndexObj.id ?? 'legacy';

    let javaPath = this.config.data.javaPath?.trim();
    if (!javaPath) javaPath = MinecraftLauncher.resolveJavaExecutable();

    const ram = this.config.data.ram || 4096;

    const cmd: string[] = [];
    cmd.push(`-Xmx${ram}M`);
    cmd.push(`-Xms${Math.min(ram, 1024)}M`);
    cmd.push(`-Djava.library.path=${path.resolve(nativesDir)}`);
    const extra = (this.config.data.extraJvmArgs || '').trim();
    if (extra) cmd.push(...extra.split(/\s+/));
    cmd.push('-cp');
    cmd.push(classpath.join(path.delimiter));
    cmd.push(mainClass);

    // Game arguments (works across old and modern versions without needing
    // to parse Mojang's full conditional "arguments" schema).
    cmd.push('--username', account.name);
    cmd.push('--version', versionId);
    cmd.push('--gameDir', path.resolve(profileDir));
    cmd.push('--assetsDir', path.resolve(path.join(mcDir, 'assets')));
    cmd.push('--assetIndex', assetIndexId);
    cmd.push('--uuid', account.uuid);
    cmd.push('--accessToken', account.accessToken || '0');
    cmd.push('--userType', account.type === 'msa' ? 'msa' : 'legacy');
    cmd.push('--versionType', 'release');

    if (serverAddress && serverAddress.trim()) {
      let host = serverAddress;
      let port = '25565';
      if (serverAddress.includes(':')) {
        [host, port] = serverAddress.split(':', 2);
      }
      cmd.push('--server', host);
      cmd.push('--port', port);
    }

    const proc = spawn(javaPath, cmd, { cwd: profileDir });

    if (output) {
      proc.stdout.on('data', (chunk) => {
        for (const line of chunk.toString().split(/\r?\n/)) if (line) output(line);
      });
      proc.stderr.on('data', (chunk) => {
        for (const line of chunk.toString().split(/\r?\n/)) if (line) output(line);
      });
    }

    return proc;
  }

  private collectLibraryPaths(mcDir: string, versionJson: any, out: string[]) {
    const librariesDir = path.join(mcDir, 'libraries');
    const libraries: any[] = versionJson.libraries ?? [];
    for (const lib of libraries) {
      if (!isAllowed(lib)) continue;

      const artifact = lib.downloads?.artifact;
      const relPath = artifact?.path;
      if (relPath) {
        const full = path.join(librariesDir, relPath);
        if (fs.existsSync(full)) out.push(full);
        continue;
      }

      // Fabric/Forge-style {name, url} libraries have no "downloads" block
      const name = lib.name;
      if (name) {
        const rel = coordToPath(name);
        if (rel) {
          const full = path.join(librariesDir, rel);
          if (fs.existsSync(full)) out.push(full);
        }
      }
    }
  }

  static resolveJavaExecutable(configuredPath?: string | null): string {
    if (configuredPath && configuredPath.trim()) return configuredPath;
    const javaHome = process.env.JAVA_HOME;
    const exe = process.platform === 'win32' ? 'java.exe' : 'java';
    if (javaHome) {
      const candidate = path.join(javaHome, 'bin', exe);
      if (fs.existsSync(candidate)) return candidate;
    }
    return 'java';
  }
}

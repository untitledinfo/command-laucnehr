import fs from 'fs';
import path from 'path';
import { getString, downloadWithRetry } from './http';
import { matches as sha1Matches } from './sha1Util';
import { isAllowed, currentOsName, is64Bit } from './ruleUtil';
import AdmZip from 'adm-zip';

const VERSION_MANIFEST_URL = 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json';

export type StatusListener = (text: string, done: number, total: number) => void;

export interface VersionEntry {
  id: string;
  type: string;
  url: string;
  releaseTime: string;
}

export class MinecraftInstaller {
  constructor(private mcDir: string, private listener?: StatusListener) {}

  private status(text: string) {
    this.listener?.(text, 0, 0);
  }

  private progress(text: string, done: number, total: number) {
    this.listener?.(text, done, total);
  }

  static async fetchVersionList(): Promise<VersionEntry[]> {
    const body = await getString(VERSION_MANIFEST_URL);
    const root = JSON.parse(body);
    return root.versions as VersionEntry[];
  }

  /** Returns the list of already-installed version IDs (based on versions/<id>/<id>.json presence). */
  getInstalledVersions(): string[] {
    const out: string[] = [];
    const versionsDir = path.join(this.mcDir, 'versions');
    if (!fs.existsSync(versionsDir)) return out;
    for (const id of fs.readdirSync(versionsDir)) {
      const dir = path.join(versionsDir, id);
      if (fs.statSync(dir).isDirectory() && fs.existsSync(path.join(dir, `${id}.json`))) {
        out.push(id);
      }
    }
    return out;
  }

  /** Deletes an installed version's folder (and its per-version profile dir) entirely. */
  deleteVersion(versionId: string): void {
    fs.rmSync(path.join(this.mcDir, 'versions', versionId), { recursive: true, force: true });
    fs.rmSync(path.join(this.mcDir, 'profiles', versionId), { recursive: true, force: true });
  }

  async installVersion(versionId: string): Promise<any> {
    this.status(`Resolving version ${versionId}...`);
    const versions = await MinecraftInstaller.fetchVersionList();
    const match = versions.find((v) => v.id === versionId);
    if (!match) throw new Error(`Version not found in manifest: ${versionId}`);

    const versionDir = path.join(this.mcDir, 'versions', versionId);
    fs.mkdirSync(versionDir, { recursive: true });

    this.status('Downloading version metadata...');
    const versionJsonText = await getString(match.url);
    fs.writeFileSync(path.join(versionDir, `${versionId}.json`), versionJsonText, 'utf-8');
    const versionJson = JSON.parse(versionJsonText);

    await this.installFromVersionJson(versionId, versionJson);
    return versionJson;
  }

  /** Shared install logic also used by Fabric (which reuses the vanilla parent's assets/libs). */
  async installFromVersionJson(versionId: string, versionJson: any): Promise<void> {
    const versionDir = path.join(this.mcDir, 'versions', versionId);
    fs.mkdirSync(versionDir, { recursive: true });

    // --- client jar ---
    const clientDownload = versionJson.downloads?.client;
    const clientUrl = clientDownload?.url;
    const clientSha1 = clientDownload?.sha1;
    const clientJar = path.join(versionDir, `${versionId}.jar`);
    if (clientUrl && !(await sha1Matches(clientJar, clientSha1))) {
      this.status('Downloading client jar...');
      await downloadWithRetry(clientUrl, clientJar, 3, (done, total) => this.progress('Client jar', done, total));
    }

    // --- libraries + natives ---
    const librariesDir = path.join(this.mcDir, 'libraries');
    const nativesDir = path.join(versionDir, `${versionId}-natives`);
    fs.mkdirSync(nativesDir, { recursive: true });

    const libraries: any[] = versionJson.libraries ?? [];
    let libIndex = 0;
    for (const lib of libraries) {
      libIndex++;
      if (!isAllowed(lib)) continue;

      const artifact = lib.downloads?.artifact;
      if (artifact?.url) {
        const url = artifact.url;
        const relPath = artifact.path;
        const sha1 = artifact.sha1;
        if (url && relPath) {
          const dest = path.join(librariesDir, relPath);
          if (!(await sha1Matches(dest, sha1))) {
            this.status(`Downloading library ${libIndex}/${libraries.length}`);
            await downloadWithRetry(url, dest, 3, (done, total) => this.progress('Libraries', done, total));
          }
        }
      }

      // Legacy per-OS "classifiers" natives (pre-1.19 style)
      const classifiers = lib.downloads?.classifiers;
      if (classifiers) {
        const nativesMap = lib.natives;
        let classifierKey: string | undefined = nativesMap?.[currentOsName()];
        if (classifierKey) {
          classifierKey = classifierKey.replace('${arch}', is64Bit() ? '64' : '32');
          const classifierEntry = classifiers[classifierKey];
          if (classifierEntry) {
            const url = classifierEntry.url;
            const relPath = classifierEntry.path;
            const sha1 = classifierEntry.sha1;
            if (url && relPath) {
              const dest = path.join(librariesDir, relPath);
              if (!(await sha1Matches(dest, sha1))) {
                this.status('Downloading natives...');
                await downloadWithRetry(url, dest, 3, (done, total) => this.progress('Natives', done, total));
              }
              this.extractNatives(dest, nativesDir);
            }
          }
        }
      }
    }

    // --- assets ---
    const assetIndex = versionJson.assetIndex;
    if (assetIndex) {
      const assetIndexId = assetIndex.id ?? 'legacy';
      const assetIndexUrl = assetIndex.url;

      const indexesDir = path.join(this.mcDir, 'assets', 'indexes');
      fs.mkdirSync(indexesDir, { recursive: true });
      const indexFile = path.join(indexesDir, `${assetIndexId}.json`);

      let indexText: string;
      if (fs.existsSync(indexFile)) {
        indexText = fs.readFileSync(indexFile, 'utf-8');
      } else {
        this.status('Downloading asset index...');
        indexText = await getString(assetIndexUrl);
        fs.writeFileSync(indexFile, indexText, 'utf-8');
      }

      const indexJson = JSON.parse(indexText);
      const objects: Record<string, any> = indexJson.objects ?? {};
      const objectsDir = path.join(this.mcDir, 'assets', 'objects');

      const virtual = !!indexJson.virtual;
      const mapToResources = !!indexJson.map_to_resources;
      const virtualDir = path.join(this.mcDir, 'assets', 'virtual', assetIndexId);
      const resourcesDir = path.join(this.mcDir, 'resources'); // legacy game dir layout

      const entries = Object.entries(objects);
      const total = entries.length;
      let i = 0;
      for (const [assetPath, obj] of entries as [string, any][]) {
        i++;
        const hash = obj.hash;
        if (!hash) continue;
        const prefix = hash.slice(0, 2);
        const dest = path.join(objectsDir, prefix, hash);
        if (!(await sha1Matches(dest, hash))) {
          const url = `https://resources.download.minecraft.net/${prefix}/${hash}`;
          await downloadWithRetry(url, dest, 3, null);
        }

        // Very old versions (pre-1.7) expect assets laid out by their
        // real path rather than by hash - materialize that layout too.
        if (virtual) this.copyIfMissing(dest, path.join(virtualDir, assetPath));
        if (mapToResources) this.copyIfMissing(dest, path.join(resourcesDir, assetPath));

        if (i % 25 === 0 || i === total) this.progress(`Assets ${i}/${total}`, i, total);
      }
    }

    this.status(`Install complete for ${versionId}`);
  }

  private copyIfMissing(source: string, dest: string) {
    try {
      if (fs.existsSync(source) && !fs.existsSync(dest)) {
        fs.mkdirSync(path.dirname(dest), { recursive: true });
        fs.copyFileSync(source, dest);
      }
    } catch {
      /* ignored */
    }
  }

  private extractNatives(jarFile: string, targetDir: string): void {
    const zip = new AdmZip(jarFile);
    for (const entry of zip.getEntries()) {
      const name = entry.entryName;
      if (entry.isDirectory || name.startsWith('META-INF/')) continue;
      if (/\.(dll|so|dylib|jnilib)$/.test(name)) {
        const base = path.basename(name);
        fs.mkdirSync(targetDir, { recursive: true });
        fs.writeFileSync(path.join(targetDir, base), entry.getData());
      }
    }
  }
}

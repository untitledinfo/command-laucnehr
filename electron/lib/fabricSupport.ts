import fs from 'fs';
import path from 'path';
import { getString, download } from './http';
import { coordToPath } from './mavenUtil';
import type { StatusListener } from './minecraftInstaller';

const META_BASE = 'https://meta.fabricmc.net/v2/versions/loader/';

export async function listLoaderVersions(mcVersion: string): Promise<string[]> {
  const body = await getString(META_BASE + mcVersion);
  const arr = JSON.parse(body);
  const out: string[] = [];
  for (const entry of arr) {
    const version = entry?.loader?.version;
    if (version) out.push(version);
  }
  return out;
}

/**
 * Installs Fabric on top of an already-installed vanilla version.
 * Returns the synthetic version id to launch.
 */
export async function install(
  mcDir: string,
  mcVersion: string,
  loaderVersion: string,
  listener?: StatusListener
): Promise<string> {
  const profileUrl = `${META_BASE}${mcVersion}/${loaderVersion}/profile/json`;
  const profileText = await getString(profileUrl);
  const profile = JSON.parse(profileText);

  const fabricId = `${mcVersion}-fabric-${loaderVersion}`;
  const versionDir = path.join(mcDir, 'versions', fabricId);
  fs.mkdirSync(versionDir, { recursive: true });

  // Fabric libraries use {name, url} maven coordinates rather than
  // Mojang's {downloads:{artifact:{url,path}}} shape - resolve them here.
  const librariesDir = path.join(mcDir, 'libraries');
  const libraries: any[] = profile.libraries ?? [];
  let i = 0;
  for (const lib of libraries) {
    i++;
    const name = lib?.name;
    const repoUrl = lib?.url ?? 'https://maven.fabricmc.net/';
    if (!name) continue;
    const relPath = coordToPath(name);
    if (!relPath) continue;
    const dest = path.join(librariesDir, relPath);
    if (!fs.existsSync(dest)) {
      listener?.(`Fabric library ${i}/${libraries.length}`, i, libraries.length);
      const url = repoUrl.endsWith('/') ? repoUrl + relPath : `${repoUrl}/${relPath}`;
      try {
        await download(url, dest, null);
      } catch (e: any) {
        console.log(`Warning: failed to download fabric lib ${name}: ${e.message}`);
      }
    }
  }

  // Persist a version json for this synthetic id so it shows as installed;
  // it inherits the vanilla jar/assets and points at Fabric's main class.
  profile.inheritsFrom = mcVersion;
  fs.writeFileSync(path.join(versionDir, `${fabricId}.json`), JSON.stringify(profile));

  return fabricId;
}

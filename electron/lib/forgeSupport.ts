import fs from 'fs';
import os from 'os';
import path from 'path';
import { spawn } from 'child_process';
import { getString, download } from './http';
import type { StatusListener } from './minecraftInstaller';

const PROMOTIONS_URL = 'https://maven.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json';
const MAVEN_BASE = 'https://maven.minecraftforge.net/net/minecraftforge/forge/';

/** Returns the recommended (falls back to latest) Forge build for a Minecraft version, or null. */
export async function recommendedForgeVersion(mcVersion: string): Promise<string | null> {
  const body = await getString(PROMOTIONS_URL);
  const root = JSON.parse(body);
  const promos = root.promos ?? {};
  if (promos[`${mcVersion}-recommended`]) return promos[`${mcVersion}-recommended`];
  return promos[`${mcVersion}-latest`] ?? null;
}

function listVersionDirs(versionsDir: string): Set<string> {
  const out = new Set<string>();
  if (!fs.existsSync(versionsDir)) return out;
  for (const id of fs.readdirSync(versionsDir)) {
    if (fs.statSync(path.join(versionsDir, id)).isDirectory()) out.add(id);
  }
  return out;
}

/**
 * Downloads and runs the Forge installer for mcVersion+forgeVersion against mcDir.
 * Returns the new version id that appeared in versions/, so the caller can select it.
 *
 * NOTE: Rather than reimplementing Forge's own bytecode-patching install pipeline,
 * this downloads the official Forge installer jar and runs it in headless
 * "--installClient" mode, which does the real work itself (including
 * downloading vanilla + Forge libraries). It diffs the versions/ directory
 * before and after to discover the id Forge assigned - identical strategy
 * to the original Java implementation.
 */
export async function install(
  mcDir: string,
  mcVersion: string,
  forgeVersion: string,
  javaExe: string,
  listener?: StatusListener
): Promise<string> {
  const combined = `${mcVersion}-${forgeVersion}`;
  const installerUrl = `${MAVEN_BASE}${combined}/forge-${combined}-installer.jar`;

  const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'forge-installer-'));
  const installerJar = path.join(tempDir, 'forge-installer.jar');

  listener?.('Downloading Forge installer...', 0, 0);
  await download(installerUrl, installerJar, (done, total) => listener?.('Downloading Forge installer...', done, total));

  const versionsDir = path.join(mcDir, 'versions');
  fs.mkdirSync(versionsDir, { recursive: true });
  const before = listVersionDirs(versionsDir);

  listener?.('Running Forge installer (this can take a minute)...', 0, 0);
  await new Promise<void>((resolve, reject) => {
    const proc = spawn(javaExe, ['-jar', installerJar, '--installClient', mcDir], { cwd: mcDir });
    proc.stdout.on('data', (chunk) => {
      for (const line of chunk.toString().split(/\r?\n/)) if (line) listener?.(line, 0, 0);
    });
    proc.stderr.on('data', (chunk) => {
      for (const line of chunk.toString().split(/\r?\n/)) if (line) listener?.(line, 0, 0);
    });
    proc.on('error', reject);
    proc.on('close', (code) => {
      if (code === 0) resolve();
      else reject(new Error(`Forge installer exited with code ${code}`));
    });
  });

  const after = listVersionDirs(versionsDir);
  for (const id of before) after.delete(id);
  if (after.size === 0) {
    // Installer may have run silently against an already-installed version.
    for (const id of listVersionDirs(versionsDir)) {
      if (id.startsWith(mcVersion) && id.includes('forge')) return id;
    }
    throw new Error('Forge installer finished but no new version was found. It may already be installed.');
  }
  return after.values().next().value as string;
}

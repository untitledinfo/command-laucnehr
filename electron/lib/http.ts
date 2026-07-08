import fs from 'fs';
import path from 'path';
import { pipeline } from 'stream/promises';

export type ProgressCb = (done: number, total: number) => void;

export async function getString(url: string): Promise<string> {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`GET ${url} failed: ${res.status} ${res.statusText}`);
  return res.text();
}

export async function postForm(url: string, body: string, contentType: string): Promise<string> {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': contentType },
    body
  });
  // Microsoft's device-code endpoints return useful JSON bodies even on 4xx (e.g. authorization_pending)
  return res.text();
}

export async function postJson(url: string, body: string, extraHeaders?: Record<string, string>): Promise<string> {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...(extraHeaders || {}) },
    body
  });
  return res.text();
}

export async function getJsonAuthed(url: string, bearerToken: string): Promise<string> {
  const res = await fetch(url, {
    headers: { Authorization: `Bearer ${bearerToken}` }
  });
  return res.text();
}

export async function download(url: string, dest: string, onProgress?: ProgressCb | null): Promise<void> {
  await fs.promises.mkdir(path.dirname(dest), { recursive: true });
  const res = await fetch(url);
  if (!res.ok || !res.body) throw new Error(`Download failed (${res.status}): ${url}`);

  const totalHeader = res.headers.get('content-length');
  const total = totalHeader ? parseInt(totalHeader, 10) : 0;
  let done = 0;

  const tmp = dest + '.part';
  const fileStream = fs.createWriteStream(tmp);
  const reader = (res.body as any).getReader ? (res.body as ReadableStream).getReader() : null;

  if (reader) {
    // Web-stream style body (undici in modern Node/Electron)
    while (true) {
      const { done: rDone, value } = await reader.read();
      if (rDone) break;
      done += value.length;
      fileStream.write(Buffer.from(value));
      if (onProgress) onProgress(done, total);
    }
    fileStream.end();
    await new Promise<void>((resolve, reject) => {
      fileStream.on('finish', () => resolve());
      fileStream.on('error', reject);
    });
  } else {
    // Fallback: Node.js readable stream
    await pipeline(res.body as any, fileStream);
  }

  await fs.promises.rename(tmp, dest);
}

export async function downloadWithRetry(
  url: string,
  dest: string,
  retries: number,
  onProgress?: ProgressCb | null
): Promise<void> {
  let lastErr: unknown;
  for (let attempt = 1; attempt <= retries; attempt++) {
    try {
      await download(url, dest, onProgress);
      return;
    } catch (e) {
      lastErr = e;
      await new Promise((r) => setTimeout(r, 500 * attempt));
    }
  }
  throw lastErr;
}

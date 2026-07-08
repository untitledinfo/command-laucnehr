import fs from 'fs';
import crypto from 'crypto';

export function hex(file: string): Promise<string> {
  return new Promise((resolve, reject) => {
    const hash = crypto.createHash('sha1');
    const stream = fs.createReadStream(file);
    stream.on('data', (chunk) => hash.update(chunk));
    stream.on('end', () => resolve(hash.digest('hex')));
    stream.on('error', reject);
  });
}

/** True if the file exists and its SHA-1 matches (case-insensitive); false if missing/mismatched/unreadable. */
export async function matches(file: string, expectedSha1?: string | null): Promise<boolean> {
  if (!expectedSha1) return fs.existsSync(file);
  if (!fs.existsSync(file)) return false;
  try {
    const actual = await hex(file);
    return actual.toLowerCase() === expectedSha1.toLowerCase();
  } catch {
    return false;
  }
}

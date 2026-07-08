import os from 'os';

/** Short OS name the way Mojang's manifests express it: "windows" / "linux" / "osx". */
export function currentOsName(): 'windows' | 'osx' | 'linux' {
  const platform = os.platform();
  if (platform === 'win32') return 'windows';
  if (platform === 'darwin') return 'osx';
  return 'linux';
}

export function is64Bit(): boolean {
  return os.arch().includes('64');
}

export function nativesClassifier(): string {
  return `natives-${currentOsName()}`;
}

/**
 * Evaluates a Mojang-style "rules" array (used by libraries and by the
 * modern "arguments" schema). Returns true if the entry should be used
 * on the current platform. If there is no "rules" key, it is always allowed.
 */
export function isAllowed(entry: any): boolean {
  const rules = entry?.rules;
  if (!Array.isArray(rules)) return true;
  let allowed = false; // if rules exist, default deny until a matching allow rule fires
  const osName = currentOsName();
  for (const rule of rules) {
    const action = rule?.action ?? 'allow';
    let matches = true;
    const osRule = rule?.os;
    if (osRule && typeof osRule === 'object') {
      const name = osRule.name;
      if (name && name !== osName) matches = false;
    }
    if (matches) allowed = action === 'allow';
  }
  return allowed;
}

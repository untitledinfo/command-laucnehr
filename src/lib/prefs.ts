// Lightweight typed localStorage layer for client-side preferences and
// feature data that doesn't need to live in the Electron main-process config.
// Everything here is additive and never touches the existing config/IPC surface.

const NS = 'cl:';

function read<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(NS + key);
    if (raw === null) return fallback;
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

function write<T>(key: string, value: T): void {
  try {
    localStorage.setItem(NS + key, JSON.stringify(value));
  } catch {
    /* ignore quota / privacy-mode errors */
  }
}

export interface SavedServer {
  id: string;
  name: string;
  address: string;
}

export interface PrefsShape {
  theme: 'nebula' | 'midnight' | 'sunset' | 'emerald' | 'crimson';
  accent: string;
  accent2: string;
  particles: boolean;
  reduceMotion: boolean;
  favoriteVersions: string[];
  servers: SavedServer[];
  playtime: Record<string, number>; // versionId -> minutes
  lastPlayed: Record<string, string>; // versionId -> ISO date
  discordRpc: boolean;
  minimizeToTray: boolean;
  autoscrollConsole: boolean;
  soundEffects: boolean;
  jvmPreset: string;
  onboarded: boolean;
}

export const THEME_PRESETS: Record<PrefsShape['theme'], { accent: string; accent2: string; label: string }> = {
  nebula: { accent: '#8b5cf6', accent2: '#22d3ee', label: 'Nebula' },
  midnight: { accent: '#5b6ae0', accent2: '#38bdf8', label: 'Midnight' },
  sunset: { accent: '#f97316', accent2: '#f5a623', label: 'Sunset' },
  emerald: { accent: '#22c55e', accent2: '#2bc47e', label: 'Emerald' },
  crimson: { accent: '#f43f5e', accent2: '#fb7185', label: 'Crimson' }
};

const DEFAULTS: PrefsShape = {
  theme: 'nebula',
  accent: THEME_PRESETS.nebula.accent,
  accent2: THEME_PRESETS.nebula.accent2,
  particles: true,
  reduceMotion: false,
  favoriteVersions: [],
  servers: [],
  playtime: {},
  lastPlayed: {},
  discordRpc: false,
  minimizeToTray: false,
  autoscrollConsole: true,
  soundEffects: false,
  jvmPreset: 'balanced',
  onboarded: false
};

export function loadPrefs(): PrefsShape {
  const stored = read<Partial<PrefsShape>>('prefs', {});
  return { ...DEFAULTS, ...stored };
}

export function savePrefs(prefs: PrefsShape): void {
  write('prefs', prefs);
}

export function exportPrefsJson(prefs: PrefsShape, extra?: Record<string, unknown>): string {
  return JSON.stringify({ ...prefs, ...extra }, null, 2);
}

export function resetPrefs(): PrefsShape {
  write('prefs', DEFAULTS);
  return { ...DEFAULTS };
}

export function uid(): string {
  return Math.random().toString(36).slice(2, 10);
}

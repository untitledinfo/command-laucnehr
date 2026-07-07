# Command Launcher — Next.js + Electron

Phase 1 scaffold for a premium Minecraft launcher UI. Real, running architecture — not everything is wired to live data yet (see "What's real vs. stubbed" below).

## Getting started

```bash
npm install

# Web preview only (browser, no window chrome)
npm run dev

# Full desktop app (Electron window with custom titlebar)
npm run electron:dev
```

Then open http://localhost:3000 for the web preview, or the Electron window will open automatically for `electron:dev`.

## Build a distributable

```bash
npm run electron:build
```

Outputs to `dist/` (Windows NSIS installer, Linux AppImage — see `package.json` → `build` for targets).

## Architecture

```
app/                  Next.js App Router pages (one folder per sidebar section)
components/layout/    Titlebar, Sidebar, AppShell
components/ui/        Reusable primitives (Card, Button, ProgressBar, StatWidget, PlaceholderGrid)
lib/                  navigation.ts (nav data), utils.ts (cn helper)
electron/             main.js (window + IPC), preload.js (safe bridge)
```

- Styling: Tailwind CSS with the launcher's design tokens (dark base `#09090B`, red accent `#EF4444`/`#B91C1C`, 20px glass cards, 30px blur) — see `tailwind.config.ts` and `app/globals.css`.
- Motion: Framer Motion for the sidebar active-pill, hero entrance, and progress bars.
- Window chrome: fully custom (frame: false in Electron) with working minimize/maximize/close via `contextBridge` — no Node access leaks into the renderer.
- Static export: `next.config.mjs` uses `output: 'export'` so the built app is plain HTML/CSS/JS that Electron loads via `file://` in production.

## What's real vs. stubbed

**Fully built:**
- Home dashboard (hero, live-updating CPU/RAM widgets, download queue, recent activity, recently played)
- Play screen (RAM slider, launch button with simulated progress, launch log panel)
- Sidebar navigation (all 15 sections + Settings/Developer/About, collapsible, badges, active-route indicator)
- Custom titlebar with working window controls

**Scaffolded and routed, not yet wired to real data/IPC:**
Versions, Mods, Resource Packs, Shaders, Servers, Worlds, Downloads, Accounts, News, Marketplace, Plugins, Settings, Developer, About — each renders a real page listing its planned feature set (see `components/ui/PlaceholderGrid.tsx`), so the nav is fully clickable and nothing 404s.

## Next steps (suggested order)

1. Wire the Play screen to a real Minecraft launch library (e.g. a Node wrapper around the Mojang launcher meta APIs) via Electron IPC.
2. Build out Versions using the official Mojang version manifest + Fabric/Forge/NeoForge/Quilt meta APIs.
3. Build Mods using the Modrinth API (search, install, dependency resolution).
4. Add Microsoft OAuth (Azure AD app registration required) and offline account handling for the Accounts screen.
5. Everything past that (cloud sync, community, cosmetics, AI features) — see the separate roadmap doc for phased sequencing.

## Building the .exe

This sandbox can't produce the actual binary — Electron's runtime download and Windows packaging (NSIS via `electron-builder`) both need things this environment doesn't have network/OS access to. Two working options:

**Option A — build it yourself on Windows:**
```bash
npm install
npm run electron:build
```
Output lands in `dist/Command Launcher Setup <version>.exe`.

**Option B — GitHub Actions (recommended, matches your existing CI/CD pattern):**
A workflow is included at `.github/workflows/build.yml`. Push this repo to GitHub and it will:
1. Build the Next.js static export
2. Package it with `electron-builder` on a real `windows-latest` runner (produces the `.exe`) and `ubuntu-latest` (produces the `.AppImage`)
3. Upload both as workflow artifacts you can download from the Actions tab

Push a tag like `v0.1.0` to trigger a release build, or just push to `main` for a normal build.



- Fonts currently fall back to system fonts (`Inter Tight`/`Inter`/`Segoe UI`) since this environment can't fetch Google Fonts at build time. Swap in real font files via `next/font/local` whenever convenient — the CSS variables (`--font-display`, `--font-body`, `--font-mono`) are already wired up in `globals.css`.
- `window:maximize` in `electron/main.js` toggles maximize/unmaximize — no separate restore icon yet, which is a fine v1 tradeoff.

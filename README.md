# Command Deck — a mission-control themed Minecraft launcher UI

A Next.js 14 (App Router) + TypeScript rebuild of a Minecraft launcher home screen, redesigned around a
"mission control dashboard" theme instead of a generic dark launcher look: an angular technical typeface,
a phosphor-green telemetry accent, a HUD dial in place of a flat launch button, and monospace readouts
for ping, player counts, FPS and memory — like a spacecraft console rather than a website.

## Design system

- **Palette** — `--void` (#080b14) page background, `--panel` / `--panel-raised` for layered surfaces,
  `--signal` (#39ffc1) as the primary telemetry accent, `--plasma` (#6c8cff) for secondary/informational
  states, `--warn` (#ffb648) for attention states.
- **Type** — Chakra Petch (display/headings), Inter (body/UI), JetBrains Mono (all data readouts:
  versions, ping, FPS, memory, java args).
- **Signature element** — the Launch Console: a circular HUD dial with tick marks and an animated
  progress ring that replaces the typical rectangular gradient "Play" button, with live telemetry
  underneath it.

Run `npm run dev` and open `http://localhost:3000`.

## Setup

```bash
npm install
npm run dev
```

Requires Node 18.18+ (Next.js 14).

## The 25 features

1. Icon rail navigation with active state and hover tooltips
2. Settings shortcut pinned to the bottom of the icon rail
3. Quick-play server chip bar (horizontally scrollable, toggle-to-favorite)
4. Global search bar that opens the command palette
5. Command palette (press `/` anywhere) with filterable actions
6. Keyboard shortcuts: `/` opens the palette, `Esc` closes any overlay
7. Account switcher dropdown with multiple profiles
8. "Add account" flow entry point from the switcher
9. Notifications bell with unread badge and dropdown feed
10. Launch Console HUD dial with animated multi-stage launch sequence
11. Version selector dropdown (releases and snapshots)
12. Mod loader toggle (Vanilla / Fabric / Forge / Quilt)
13. Live ping readout that drifts over time like a real connection
14. Live "players online" counter
15. Persisted last-used version and mod loader via `localStorage`
16. Toast notification system (queued, auto-dismissing, color-coded by type)
17. Settings modal with RAM allocation slider
18. Resolution selector and editable Java arguments field in settings
19. Accent color picker (phosphor / plasma / amber) persisted across sessions
20. Skeleton loading states for the news grid and friends list on first load
21. Tabbed content switcher (News / Store / Community)
22. News cards with tag pills, hover lift, and per-card "read more" action
23. Friends list sorted by presence (in-game, online, away) with status dots
24. Per-friend quick actions: join game (when in-game) and message
25. Fully responsive layout: icon rail collapses into a bottom tab bar on mobile,
    plus an ambient animated scanline/grid background across the whole app
    (disabled automatically for users with reduced-motion preferences)

## Continuous integration

`.github/workflows/build.yml` runs on every push to `main`, every `v*` tag, and every pull request:
installs dependencies, type-checks with `tsc --noEmit`, runs `next lint`, runs `next build`, and
uploads the `.next` build output as a workflow artifact. Trigger it manually any time from the
Actions tab (`workflow_dispatch`).

## Structure

```
app/            Root layout, global styles, the assembled page
components/     One component + CSS module per feature area
lib/data.ts     Mock servers, versions, news, friends, accounts
hooks/          useSlashShortcut for the command palette
```

Everything is plain CSS Modules (no Tailwind) so every color and spacing value traces back to the
token list in `app/globals.css`.

# Command Launcher (Electron + Vue 3 + TypeScript)

Full port of the Java/Swing "Command Launcher" to Electron + Vue 3 + TypeScript,
same dark neon/glass theme as the original (`ui/Theme.java` colors carried over
1:1 into `src/theme.css`).

## What was ported

Every piece of launcher logic from the Java version was rewritten faithfully
in the Electron **main process** (`electron/lib/`), not reimplemented from
scratch:

| Java file | TS port | Notes |
|---|---|---|
| `Config.java` | `lib/configStore.ts` | Same schema; stored in Electron's userData dir instead of the working directory |
| `Account.java` / `AccountManager.java` | `lib/configStore.ts` | Offline UUID uses the identical MD5 name-UUID algorithm Mojang/Java use |
| `MicrosoftAuth.java` | `lib/microsoftAuth.ts` | Same MS device-code → Xbox Live → XSTS → Minecraft Services chain and URLs |
| `MinecraftInstaller.java` | `lib/minecraftInstaller.ts` | Same manifest/client-jar/library/asset/native download pipeline, SHA-1 verified |
| `FabricSupport.java` | `lib/fabricSupport.ts` | Same Fabric meta API + maven coordinate resolution |
| `ForgeSupport.java` | `lib/forgeSupport.ts` | Same "run the official installer headlessly, diff versions/" strategy |
| `MinecraftLauncher.java` | `lib/minecraftLauncher.ts` | Same classpath/natives/args resolution and `java` process spawn |
| `ServerPinger.java` | `lib/serverPinger.ts` | Same raw Server List Ping TCP handshake, no external library |
| `RuleUtil.java` / `MavenUtil.java` / `Sha1Util.java` | `lib/ruleUtil.ts` / `lib/mavenUtil.ts` / `lib/sha1Util.ts` | Line-for-line logic ports |

The Swing UI (`ui/MainFrame.java` and friends) was rebuilt as Vue 3 views/components:

- `src/App.vue` — custom frameless title bar + icon rail (was `MainFrame`'s top bar/icon rail)
- `src/views/Home.vue` — hero panel, server ping, version picker, LAUNCH button, news row
- `src/views/Mods.vue` — folder shortcuts + installed version management (was the mods page)
- `src/views/Console.vue` — live game stdout/stderr, save log
- `src/views/Settings.vue` — RAM slider, Java path, JVM args, `.minecraft` dir, default server
- `src/components/AccountsDialog.vue` — offline + Microsoft sign-in (was `AccountsDialog.java`)
- `src/components/VersionInstallDialog.vue` — manifest browser + vanilla/Fabric/Forge install (was `VersionInstallDialog.java`)
- `src/components/GlowButton.vue` / `NewsCard.vue` — matching the original's glow-pill launch button and news cards

Renderer never touches the filesystem, network, or child processes directly —
all of that lives in the main process and is exposed through a narrow,
typed `window.launcherApi` surface via `electron/preload.ts` (contextBridge,
`contextIsolation: true`, `nodeIntegration: false`).

## Before you run this

Set your own Azure AD app client ID in `electron/lib/microsoftAuth.ts`
(`CLIENT_ID`) — same requirement as the original Java version's comment in
`MicrosoftAuth.java`. Register a "Public client/native" app at
https://portal.azure.com; no client secret or redirect URI needed for the
device-code flow.

## Development

```bash
npm install
npm run build:electron   # compile the main process once
npm run dev              # start Vite dev server (renderer)
# in a second terminal:
npx cross-env VITE_DEV_SERVER_URL=http://localhost:5173 npx electron .
```

## Production build

```bash
npm run build         # builds renderer (vite) + main process (tsc)
npm run dist:win      # packages Windows .exe installers (x64 + ia32) via electron-builder
npm run dist:win64    # Windows x64 only
npm run dist:win32    # Windows ia32 (32-bit) only
npm run dist:mac      # packages a macOS .dmg (x64 + arm64) - must run on macOS
npm run dist:linux    # packages a Linux .AppImage (x64)
```

Add real icons under `build/` first — see `build/ICONS_README.txt`.

## CI

`.github/workflows/build.yml` builds every platform in parallel on every push
to `main` / tag `v*`:

| Job | Runner | Output |
|---|---|---|
| `windows-x64` | `windows-latest` | 64-bit `.exe` installer |
| `windows-ia32` | `windows-latest` | 32-bit `.exe` installer |
| `macos` | `macos-latest` | `.dmg` (Intel + Apple Silicon) |
| `linux-x64` | `ubuntu-latest` | `.AppImage` |

Each build is uploaded as a workflow artifact, and all of them are attached
to the GitHub Release automatically when the tag starts with `v`. Builds are
unsigned (no Apple/Windows code-signing certificate configured) — that's
expected for a personal/community project and doesn't affect functionality,
just what users see in an OS security prompt on first run.

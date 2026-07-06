# Command Launcher (Java Edition)

A pure-Java, dependency-free Minecraft launcher with a dark neon UI inspired
by Lunar Client / Feather Client: glowing/pulsing controls, an animated
sidebar, shimmering progress bars, and smooth fade transitions — all built
with the standard JDK (Swing + `java.net.http`), no external jars required.

## v3.1 changelog (bug fixes + new features)

**Fixed:**
- **Critical launch bug**: `--uuid` was stripping dashes before passing it to
  the game, which breaks `UUID.fromString()` inside Minecraft and silently
  fails to launch. Dashes are now preserved.
- **Corrupted/partial downloads never healed themselves** — files were only
  checked for *existence*, so a broken half-downloaded jar would stay broken
  forever. Client jar, libraries, natives, and asset objects are now all
  verified against their official SHA-1 hash and automatically re-downloaded
  if they don't match, with 3 retries and backoff on transient network
  failures.
- **Animation timer leak** — the glow/shimmer/sidebar `javax.swing.Timer`s
  kept ticking forever even after their dialog was closed. They now start/
  stop with the component's actual on-screen lifecycle (`addNotify`/
  `removeNotify`).
- **Old Beta/Alpha versions were silently unselectable** — the version
  browser only exposed Release/Snapshot checkboxes even though the manifest
  includes older version types; both are now selectable, plus a live search
  box was added.
- **Legacy versions (pre-1.7) would launch with missing textures/sounds** —
  those versions expect assets laid out by real path (`assets/virtual/...`)
  or under a `resources/` folder in the game directory, not by hash. The
  installer now materializes both legacy layouts when the asset index
  requests them (`virtual` / `map_to_resources`).
- Maven-coordinate → path resolution (used by Fabric/Forge libraries) didn't
  handle classifier suffixes correctly; extracted into a shared, correct
  `MavenUtil` helper used everywhere.

**New features:**
- **Forge support.** Rather than reimplementing Forge's own patching
  pipeline, Command Launcher downloads the official Forge installer for the
  selected Minecraft version's recommended (or latest) build and runs it
  headlessly (`--installClient`), then detects the resulting version
  automatically. Enable it via Settings → Mod Loader → Forge.
- **Delete Version** button on the Instances page (removes the version + its
  profile folder, with a confirmation prompt).
- **Open .minecraft Folder** shortcut on the Instances page.
- **Search box** in the version installer, in addition to the type filters.
- Version selection is now preserved across list refreshes instead of
  resetting to the top item.

---

## Requirements

- JDK 17 or newer (uses `java.net.http.HttpClient`, records-free code so it
  also works fine on 17 LTS; tested against OpenJDK 21).
- No Maven/Gradle needed — it's plain `javac`/`java`.

## Build & run

```bash
chmod +x build.sh run.sh
./run.sh
```

`run.sh` compiles into `out/` (if not already built) and launches
`launcher.LauncherApp`.

## What's implemented

- **Vanilla installs**: fetches the real Mojang version manifest, downloads
  the client jar, all required libraries (filtered by OS rules), extracts
  natives, and downloads the asset index + asset objects — all verified by
  SHA-1 with automatic retry/repair.
- **Fabric support**: optional, via Settings → Mod Loader → Fabric. Resolves
  the newest Fabric loader for the selected Minecraft version and merges its
  libraries/main class with the vanilla install.
- **Forge support**: optional, via Settings → Mod Loader → Forge. Downloads
  and runs the official Forge installer headlessly against your `.minecraft`
  folder, then auto-detects the version it created.
- **Accounts**:
  - Offline accounts (username-only, using Mojang's own offline-UUID
    algorithm) — works immediately, no setup needed.
  - Microsoft accounts via the OAuth **device code flow** → Xbox Live →
    XSTS → Minecraft Services chain. **You must register your own Azure AD
    application** to use this (see below) — Microsoft does not allow
    third-party apps to reuse the official launcher's client ID.
- **Server ping**: a from-scratch implementation of the vanilla Server List
  Ping protocol (handshake + status request), no external library.
- **Launching**: builds the full Java command (classpath, natives path,
  memory flags, and the `--username/--uuid/--accessToken/...` game
  arguments) and streams stdout/stderr into an in-app console.
- **UI**: sidebar navigation (Home / Instances / Accounts / Settings /
  Console), a glowing ambient-pulsing PLAY button, a shimmering progress
  bar, and fade-in transitions between pages — all done with plain
  `Graphics2D`/`Timer`, no animation library.

## Setting up Microsoft login (optional)

Offline accounts work out of the box. If you want real Microsoft/Xbox
sign-in:

1. Go to https://portal.azure.com → **App registrations** → **New
   registration**.
2. Choose **"Accounts in any organizational directory and personal
   Microsoft accounts"**.
3. Under **Authentication**, add a platform of type **"Mobile and desktop
   applications"** (this enables the device code flow) — no redirect URI is
   required for device code login.
4. Copy the **Application (client) ID** and paste it into
   `src/launcher/MicrosoftAuth.java`, replacing
   `PUT-YOUR-AZURE-APP-CLIENT-ID-HERE`.
5. Rebuild (`./build.sh`).

This is a one-time step required by Microsoft for *any* third-party
launcher, not something specific to this project.

## Known limitations / good next steps

- **Forge on 1.17+** may not fully launch — very new Forge versions rely on
  Java's module-system flags (`--add-opens`, etc.) that live in the modern
  `arguments.jvm` schema, which this launcher doesn't parse (it always passes
  a fixed, broadly-compatible set of flags/args instead of the full
  conditional schema). Forge 1.7–1.16 should work fine. Fabric is unaffected
  since its Knot launch process doesn't need those flags.
- The modern per-argument `rules` conditions in `arguments.game` are not
  parsed; instead the launcher always passes the standard set of flags
  (`--username`, `--version`, `--uuid`, etc.) that every current Minecraft
  version accepts. This covers the vast majority of versions but very old
  legacy versions (pre-1.6) used a different, non-standard argument style
  and aren't guaranteed to work.
- Cracked/offline accounts obviously can't join servers with
  `online-mode=true`.

## Project layout

```
src/launcher/
  Json.java              tiny dependency-free JSON parser/serializer
  Http.java               HttpClient-based GET/POST/download helpers (+ retry)
  Config.java              launcher_config.json load/save
  RuleUtil.java            OS + library "rules" evaluation
  MavenUtil.java           maven coordinate -> relative path resolution
  Sha1Util.java            SHA-1 hashing/verification for downloaded files
  MinecraftInstaller.java  version manifest, client jar, libs, natives, assets
  FabricSupport.java       Fabric loader resolution + install
  ForgeSupport.java        downloads/runs the official Forge installer
  Account.java / AccountManager.java   account model + persistence
  MicrosoftAuth.java       device-code -> XBL -> XSTS -> Minecraft auth chain
  MinecraftLauncher.java   builds + runs the java launch command
  ServerPinger.java        raw Server List Ping protocol
  LauncherApp.java         entry point + splash/fade-in
  ui/
    Theme.java             color palette + paint helpers
    GlowButton.java        neon glow / pulsing button
    ShimmerProgressBar.java animated gradient progress bar
    SidebarButton.java      animated nav toggle
    FadePanel.java          fade-in page transitions
    MainFrame.java          main window (sidebar + Home/Instances/Console)
    VersionInstallDialog.java
    AccountsDialog.java
    SettingsDialog.java
```

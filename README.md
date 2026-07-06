# Command Launcher (Java Edition)

A pure-Java, dependency-free Minecraft launcher with a dark neon UI inspired
by Lunar Client / Feather Client: glowing/pulsing controls, an animated
sidebar, shimmering progress bars, and smooth fade transitions — all built
with the standard JDK (Swing + `java.net.http`), no external jars required.

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
  natives, and downloads the asset index + asset objects.
- **Fabric support**: optional, via Settings → Mod Loader → Fabric. Resolves
  the newest Fabric loader for the selected Minecraft version and merges its
  libraries/main class with the vanilla install.
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

- **No Forge support** — Forge's installer is a self-executing patcher jar
  with its own bytecode-patching pipeline; wiring that up is a project of
  its own. Fabric is fully supported as a lighter-weight alternative.
- Asset/library integrity is checked by **existence**, not SHA-1 — good
  enough for normal use, but a corrupted partial download won't be
  auto-detected. `Settings → Clear Launcher Cache` forces a clean
  re-download if that ever happens.
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
  Http.java               HttpClient-based GET/POST/download helpers
  Config.java              launcher_config.json load/save
  RuleUtil.java            OS + library "rules" evaluation
  MinecraftInstaller.java  version manifest, client jar, libs, natives, assets
  FabricSupport.java       Fabric loader resolution + install
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

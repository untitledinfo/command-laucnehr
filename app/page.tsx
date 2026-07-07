"use client";

import { useState } from "react";
import styles from "./page.module.css";
import { IconRail, BottomNav } from "@/components/IconRail";
import { TopBar } from "@/components/TopBar";
import { LaunchConsole } from "@/components/LaunchConsole";
import { NewsGrid } from "@/components/NewsGrid";
import { FriendsPanel } from "@/components/FriendsPanel";
import { StatusFooter } from "@/components/StatusFooter";
import { CommandPalette } from "@/components/CommandPalette";
import { SettingsModal } from "@/components/SettingsModal";
import { useSlashShortcut } from "@/hooks/useSlashShortcut";

export default function Home() {
  const [paletteOpen, setPaletteOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);

  useSlashShortcut(() => setPaletteOpen(true));

  return (
    <div className={styles.shell}>
      <IconRail />
      <div className={styles.main}>
        <TopBar onOpenPalette={() => setPaletteOpen(true)} />
        <div className={styles.content}>
          <LaunchConsole />
          <div className={styles.lower}>
            <div className={styles.newsCol}>
              <NewsGrid />
            </div>
            <FriendsPanel />
          </div>
        </div>
        <StatusFooter />
        <BottomNav />
      </div>

      <CommandPalette
        open={paletteOpen}
        onClose={() => setPaletteOpen(false)}
        onOpenSettings={() => setSettingsOpen(true)}
      />
      <SettingsModal open={settingsOpen} onClose={() => setSettingsOpen(false)} />
    </div>
  );
}

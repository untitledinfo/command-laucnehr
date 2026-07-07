"use client";

import CommandLauncherHome from "@/components/CommandLauncherHome";

export default function Page() {
  return (
    <main className="h-screen w-screen">
      <CommandLauncherHome
        playerName="Firepdx"
        onLaunch={(version) => console.log("Launch requested:", version)}
        onPingServer={(address) => console.log("Ping requested:", address)}
      />
    </main>
  );
}

"use client";

import { useState } from "react";
import {
  Home,
  Boxes,
  User,
  Settings,
  Terminal,
  Plus,
  Radio,
  Play,
} from "lucide-react";

/**
 * Command Launcher — Home
 * ------------------------------------------------------------------
 * Design tokens (see design plan below for rationale):
 *   bg-obsidian   #0b0c10   page background
 *   panel         #14161d   rail / top bar / cards
 *   panel-light   #1c1f29   inputs, hover states
 *   border        #262a36   hairlines
 *   text          #eef1f7   primary text
 *   text-dim      #8b90a3   secondary text
 *   violet        #7c5cff   selection / nav accent
 *   ember-400/600 #34e07a / #17a854   the LAUNCH pill gradient
 *   amber         #f2a63c   rare flourish (status dot / chip)
 *
 * Type: display = Space Grotesk (geometric, blocky — pairs with a
 * Minecraft-adjacent world without being a literal pixel font), body =
 * Inter, data/mono = JetBrains Mono for version strings & server IPs.
 *
 * Signature element: the LAUNCH pill. A diagonal shimmer sweep loops
 * across it, a soft pulse ring breathes behind it, and hovering reveals
 * small bracket corners (a nod to a targeting reticle) rather than a
 * generic drop-shadow lift.
 *
 * Drop this file into a Next.js app (Tailwind + lucide-react already
 * match your PGC stack). Everything is driven by the `data` below —
 * swap in real accounts / news / server status as props.
 * ------------------------------------------------------------------
 */

export interface Player {
  id: string;
  name: string;
  status: "online" | "offline";
  detail: string; // e.g. "Playing · Hypixel" or "Offline"
}

export interface NewsItem {
  id: string;
  eyebrow: string;
  title: string;
  body: string;
  accent: "violet" | "green" | "amber";
}

export interface CommandLauncherHomeProps {
  playerName?: string;
  versions?: string[];
  players?: Player[];
  news?: NewsItem[];
  serverAddress?: string;
  onLaunch?: (version: string) => void;
  onPingServer?: (address: string) => void;
}

const defaultPlayers: Player[] = [
  { id: "1", name: "Delta", status: "online", detail: "Playing · Hypixel" },
  { id: "2", name: "jadough", status: "online", detail: "Online · Launcher" },
  { id: "3", name: "Krag", status: "offline", detail: "Private server" },
  { id: "4", name: "mlgboi", status: "online", detail: "Online · Launcher" },
  { id: "5", name: "Physci", status: "online", detail: "Playing · Hypixel" },
  { id: "6", name: "Shaaf", status: "online", detail: "Online · Launcher" },
];

const defaultNews: NewsItem[] = [
  {
    id: "1",
    eyebrow: "New",
    title: "Aquatic collection",
    body: "Fresh texture pack drop for 1.21 worlds.",
    accent: "violet",
  },
  {
    id: "2",
    eyebrow: "Community",
    title: "PGC hosted worlds",
    body: "Join the community server — see the Server field above.",
    accent: "green",
  },
  {
    id: "3",
    eyebrow: "Offer",
    title: "Hosting · 40% off",
    body: "Spin up a private server for your friend group.",
    accent: "amber",
  },
];

const accentMap = {
  violet: { from: "from-[#8b6cf6]", to: "to-[#4a3aa8]", text: "text-[#e4dcff]" },
  green: { from: "from-[#2bc47e]", to: "to-[#16794f]", text: "text-[#d7f7e8]" },
  amber: { from: "from-[#f2a63c]", to: "to-[#a86a17]", text: "text-[#fde9c8]" },
};

type NavKey = "home" | "instances";

export default function CommandLauncherHome({
  playerName = "Guest",
  versions = ["1.21.1 — Fabric", "1.20.1 — Vanilla", "1.19.4 — Forge"],
  players = defaultPlayers,
  news = defaultNews,
  serverAddress = "",
  onLaunch,
  onPingServer,
}: CommandLauncherHomeProps) {
  const [version, setVersion] = useState(versions[0]);
  const [server, setServer] = useState(serverAddress);
  const [active, setActive] = useState("home");

  return (
    <div className="flex h-full min-h-[640px] w-full bg-[#0b0c10] font-[family-name:--font-body] text-[#eef1f7]">
      <style>{`
        @keyframes shimmer-sweep {
          0%   { transform: translateX(-120%) skewX(-20deg); }
          100% { transform: translateX(220%) skewX(-20deg); }
        }
        @keyframes pulse-ring {
          0%, 100% { opacity: .35; transform: scale(1); }
          50%      { opacity: .65; transform: scale(1.06); }
        }
        .launch-shimmer::after {
          content: "";
          position: absolute;
          inset: 0;
          background: linear-gradient(100deg, transparent 40%, rgba(255,255,255,.35) 50%, transparent 60%);
          animation: shimmer-sweep 3.2s ease-in-out infinite;
        }
        .launch-pulse::before {
          content: "";
          position: absolute;
          inset: -10px;
          border-radius: 999px;
          background: radial-gradient(closest-side, rgba(52,224,122,.35), transparent);
          animation: pulse-ring 2.4s ease-in-out infinite;
          z-index: -1;
        }
      `}</style>

      {/* Icon rail */}
      <nav className="flex w-16 flex-col items-center gap-2 border-r border-[#262a36] bg-[#14161d] py-4">
        <div className="mb-3 font-[family-name:--font-space-grotesk] text-lg font-bold text-[#7c5cff]">
          ⚡
        </div>
        {[
          { key: "home", icon: Home, label: "Home" },
          { key: "instances", icon: Boxes, label: "Instances" },
        ].map(({ key, icon: Icon, label }) => (
          <button
            key={key}
            onClick={() => setActive(key as NavKey)}
            title={label}
            className={`flex h-11 w-11 items-center justify-center rounded-xl transition-colors ${
              active === key
                ? "bg-[#7c5cff]/15 text-[#eef1f7] ring-1 ring-[#7c5cff]/50"
                : "text-[#8b90a3] hover:bg-white/5 hover:text-[#eef1f7]"
            }`}
          >
            <Icon size={19} strokeWidth={1.75} />
          </button>
        ))}
        <div className="my-1 h-px w-8 bg-[#262a36]" />
        {[
          { icon: User, label: "Accounts" },
          { icon: Settings, label: "Settings" },
          { icon: Terminal, label: "Console" },
        ].map(({ icon: Icon, label }) => (
          <button
            key={label}
            title={label}
            className="flex h-11 w-11 items-center justify-center rounded-xl text-[#8b90a3] transition-colors hover:bg-white/5 hover:text-[#eef1f7]"
          >
            <Icon size={19} strokeWidth={1.75} />
          </button>
        ))}
      </nav>

      {/* Main column */}
      <div className="flex flex-1 flex-col">
        {/* Top bar */}
        <header className="flex h-14 items-center justify-between border-b border-[#262a36] bg-[#14161d] px-5">
          <div className="font-[family-name:--font-space-grotesk] text-sm font-bold tracking-wide text-[#7c5cff]">
            COMMAND LAUNCHER
          </div>
          <div className="flex items-center gap-3">
            <div className="flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-[#8b6cf6] to-[#4a3aa8] text-xs font-bold">
              {playerName.slice(0, 1).toUpperCase()}
            </div>
            <span className="text-sm text-[#eef1f7]">{playerName}</span>
          </div>
        </header>

        {/* Hero */}
        <main className="flex-1 overflow-y-auto p-7">
          <div
            className="relative overflow-hidden rounded-2xl border border-[#262a36] p-7"
            style={{
              backgroundImage:
                "radial-gradient(circle at 85% 15%, rgba(124,92,255,.16), transparent 55%), radial-gradient(circle at 100% 100%, rgba(52,224,122,.08), transparent 45%), radial-gradient(#1c1f29 1px, transparent 1px)",
              backgroundSize: "auto, auto, 16px 16px",
              backgroundColor: "#101219",
            }}
          >
            <h1 className="font-[family-name:--font-space-grotesk] text-2xl font-bold">
              Welcome back
            </h1>
            <p className="mt-1 text-sm text-[#8b90a3]">
              Pick a version, hop on a server, and hit launch.
            </p>

            <div className="mt-5 flex items-center gap-2">
              <span className="text-xs uppercase tracking-wide text-[#8b90a3]">
                Server
              </span>
              <input
                value={server}
                onChange={(e) => setServer(e.target.value)}
                placeholder="play.pgc.fun"
                className="h-9 flex-1 rounded-lg border border-[#262a36] bg-[#1c1f29] px-3 font-[family-name:--font-jetbrains-mono] text-sm text-[#eef1f7] outline-none placeholder:text-[#565b6d] focus:border-[#7c5cff]/60"
              />
              <button
                onClick={() => onPingServer?.(server)}
                className="flex h-9 items-center gap-1.5 rounded-lg border border-[#262a36] bg-[#1c1f29] px-3 text-xs font-medium text-[#8b90a3] hover:text-[#eef1f7]"
              >
                <Radio size={14} /> Ping
              </button>
            </div>

            <div className="mt-8 flex items-end justify-between gap-4">
              <div className="flex flex-col gap-1.5">
                <span className="text-xs uppercase tracking-wide text-[#8b90a3]">
                  Version
                </span>
                <select
                  value={version}
                  onChange={(e) => setVersion(e.target.value)}
                  className="h-9 w-56 rounded-lg border border-[#262a36] bg-[#1c1f29] px-3 font-[family-name:--font-jetbrains-mono] text-sm text-[#eef1f7] outline-none focus:border-[#7c5cff]/60"
                >
                  {versions.map((v) => (
                    <option key={v} value={v}>
                      {v}
                    </option>
                  ))}
                </select>
              </div>

              <button
                onClick={() => onLaunch?.(version)}
                className="launch-pulse launch-shimmer group relative isolate flex h-14 w-56 items-center justify-center gap-2 overflow-hidden rounded-full bg-gradient-to-br from-[#34e07a] to-[#17a854] font-[family-name:--font-space-grotesk] text-base font-bold text-[#062112] shadow-[0_8px_24px_-6px_rgba(23,168,84,.55)] transition-transform active:scale-[0.97]"
              >
                <span className="pointer-events-none absolute -left-0.5 -top-0.5 h-3 w-3 rounded-tl-full border-l-2 border-t-2 border-white/0 transition-colors group-hover:border-white/60" />
                <span className="pointer-events-none absolute -bottom-0.5 -right-0.5 h-3 w-3 rounded-br-full border-b-2 border-r-2 border-white/0 transition-colors group-hover:border-white/60" />
                <Play size={18} fill="currentColor" />
                Launch
              </button>
            </div>
          </div>

          {/* News */}
          <h2 className="mb-3 mt-8 text-xs font-medium uppercase tracking-wide text-[#8b90a3]">
            Latest news
          </h2>
          <div className="grid grid-cols-1 gap-3.5 sm:grid-cols-3">
            {news.map((n) => {
              const a = accentMap[n.accent];
              return (
                <div
                  key={n.id}
                  className={`rounded-xl bg-gradient-to-br ${a.from} ${a.to} p-4`}
                >
                  <span
                    className={`inline-block rounded-md bg-black/20 px-1.5 py-0.5 text-[10px] font-medium uppercase tracking-wide ${a.text}`}
                  >
                    {n.eyebrow}
                  </span>
                  <p className="mt-2 text-sm font-bold text-white">{n.title}</p>
                  <p className={`mt-1 text-xs ${a.text}`}>{n.body}</p>
                </div>
              );
            })}
          </div>
        </main>
      </div>

      {/* Players rail */}
      <aside className="flex w-56 flex-col border-l border-[#262a36] bg-[#14161d]">
        <div className="px-4 pt-4 text-xs font-medium uppercase tracking-wide text-[#8b90a3]">
          Players
        </div>
        <div className="flex-1 overflow-y-auto px-2 py-2">
          {players.map((p) => (
            <div
              key={p.id}
              className="flex items-center gap-2.5 rounded-lg px-2.5 py-2 hover:bg-white/5"
            >
              <span
                className={`h-2 w-2 shrink-0 rounded-full ${
                  p.status === "online" ? "bg-[#34e07a]" : "bg-[#565b6d]"
                }`}
              />
              <div className="min-w-0">
                <p className="truncate text-sm text-[#eef1f7]">{p.name}</p>
                <p className="truncate text-xs text-[#8b90a3]">{p.detail}</p>
              </div>
            </div>
          ))}
        </div>
        <button className="flex items-center justify-center gap-1.5 border-t border-[#262a36] py-3 text-sm font-medium text-[#a692ff] hover:text-[#c4b6ff]">
          <Plus size={14} /> Add account
        </button>
      </aside>
    </div>
  );
}

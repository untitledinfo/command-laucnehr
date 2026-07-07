export type Presence = "online" | "in-game" | "away";

export interface Friend {
  id: string;
  name: string;
  presence: Presence;
  detail: string;
  initials: string;
}

export interface NewsItem {
  id: string;
  title: string;
  tag: string;
  excerpt: string;
  accent: "signal" | "plasma" | "warn";
}

export interface QuickServer {
  id: string;
  label: string;
  short: string;
}

export interface GameVersion {
  id: string;
  label: string;
  kind: "release" | "snapshot";
}

export const quickServers: QuickServer[] = [
  { id: "hexcraft", label: "HexCraft", short: "H" },
  { id: "kingdoms", label: "Kingdoms", short: "K" },
  { id: "titanpvp", label: "TitanPvP", short: "T" },
  { id: "solstice", label: "Solstice", short: "S" },
  { id: "vanic", label: "Vanic", short: "V" },
  { id: "ironleague", label: "Iron League", short: "IL" },
  { id: "rivalry", label: "Rivalry Legacy", short: "RL" },
];

export const gameVersions: GameVersion[] = [
  { id: "1.20.1", label: "1.20.1", kind: "release" },
  { id: "1.19.4", label: "1.19.4", kind: "release" },
  { id: "1.18.2", label: "1.18.2", kind: "release" },
  { id: "24w15a", label: "24w15a", kind: "snapshot" },
];

export const modLoaders = ["Vanilla", "Fabric", "Forge", "Quilt"] as const;

export const newsItems: NewsItem[] = [
  {
    id: "n1",
    title: "Aquatic collection has surfaced",
    tag: "Cosmetics",
    excerpt:
      "New capes, wings and companions inspired by the deep. Limited-time crate rotation closes Friday.",
    accent: "signal",
  },
  {
    id: "n2",
    title: "Hosted worlds: Noxlas goes public",
    tag: "Community",
    excerpt:
      "A player-built survival world with custom terrain generation is now open to the public queue.",
    accent: "plasma",
  },
  {
    id: "n3",
    title: "Bisecthosting: official server partner",
    tag: "Partner",
    excerpt:
      "Spin up a private modded or vanilla server in under two minutes with launcher-linked billing.",
    accent: "warn",
  },
  {
    id: "n4",
    title: "40% off all cosmetic bundles",
    tag: "Store",
    excerpt: "Limited-time discount across every bundle tier. Ends when the timer in-store hits zero.",
    accent: "signal",
  },
];

export const friends: Friend[] = [
  { id: "f1", name: "Delta", presence: "in-game", detail: "Playing · Hypixel", initials: "DE" },
  { id: "f2", name: "jadough", presence: "online", detail: "Online · In launcher", initials: "JD" },
  { id: "f3", name: "Krag", presence: "in-game", detail: "Playing · Private server", initials: "KR" },
  { id: "f4", name: "mlgboi", presence: "online", detail: "Online · In launcher", initials: "ML" },
  { id: "f5", name: "Physci", presence: "in-game", detail: "Playing · Hypixel", initials: "PH" },
  { id: "f6", name: "Shaaf", presence: "online", detail: "Online · In launcher", initials: "SH" },
  { id: "f7", name: "vexley", presence: "away", detail: "Away · 12m idle", initials: "VX" },
];

export const accounts = [
  { id: "a1", name: "TheBroJordan", tag: "Microsoft", initials: "TJ" },
  { id: "a2", name: "brojordan_alt", tag: "Microsoft", initials: "BJ" },
];

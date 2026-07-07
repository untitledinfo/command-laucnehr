import type { Metadata } from "next";
import "./globals.css";
import { ToastProvider } from "@/components/ToastProvider";

export const metadata: Metadata = {
  title: "Command Deck — Minecraft Launcher",
  description:
    "A mission-control themed Minecraft launcher dashboard: quick play, mods, friends, and news in one console.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" data-mode="dark">
      <body>
        <div className="scan-bg" aria-hidden="true" />
        <ToastProvider>{children}</ToastProvider>
      </body>
    </html>
  );
}

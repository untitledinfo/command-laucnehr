import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Command Launcher",
  description: "PGC Command Launcher UI",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}

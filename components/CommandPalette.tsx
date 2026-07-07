"use client";

import { useEffect, useMemo, useState } from "react";
import styles from "./CommandPalette.module.css";
import { useToast } from "./ToastProvider";

interface Command {
  id: string;
  label: string;
  hint: string;
}

const commands: Command[] = [
  { id: "launch", label: "Launch game", hint: "Play" },
  { id: "settings", label: "Open settings", hint: "Console" },
  { id: "mods", label: "Browse mod loaders", hint: "Mods" },
  { id: "account", label: "Switch account", hint: "Profile" },
  { id: "server", label: "Join a quick-play server", hint: "Servers" },
  { id: "theme", label: "Change accent color", hint: "Appearance" },
];

export function CommandPalette({
  open,
  onClose,
  onOpenSettings,
}: {
  open: boolean;
  onClose: () => void;
  onOpenSettings: () => void;
}) {
  const [query, setQuery] = useState("");
  const { push } = useToast();

  useEffect(() => {
    if (!open) setQuery("");
  }, [open]);

  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === "Escape") onClose();
    }
    if (open) window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  const filtered = useMemo(
    () => commands.filter((c) => c.label.toLowerCase().includes(query.toLowerCase())),
    [query]
  );

  if (!open) return null;

  function run(id: string) {
    onClose();
    if (id === "settings") {
      onOpenSettings();
      return;
    }
    push(`Command run: ${commands.find((c) => c.id === id)?.label}`, "plasma");
  }

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div
        className={styles.panel}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Command palette"
      >
        <div className={styles.inputRow}>
          <span className={styles.prompt} aria-hidden="true">
            ›
          </span>
          <input
            autoFocus
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Type a command…"
            className={styles.input}
          />
          <kbd className={styles.kbd}>esc</kbd>
        </div>
        <ul className={styles.list}>
          {filtered.length === 0 && <li className={styles.empty}>No matching commands</li>}
          {filtered.map((c) => (
            <li key={c.id}>
              <button className={styles.cmdBtn} onClick={() => run(c.id)}>
                <span>{c.label}</span>
                <span className={styles.hint}>{c.hint}</span>
              </button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

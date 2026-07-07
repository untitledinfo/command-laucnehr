"use client";

import { useState } from "react";
import styles from "./TopBar.module.css";
import { quickServers, accounts } from "@/lib/data";
import { useToast } from "./ToastProvider";

export function TopBar({ onOpenPalette }: { onOpenPalette: () => void }) {
  const [favorites, setFavorites] = useState<Set<string>>(new Set(["hexcraft"]));
  const [notifOpen, setNotifOpen] = useState(false);
  const [acctOpen, setAcctOpen] = useState(false);
  const [current, setCurrent] = useState(accounts[0].id);
  const { push } = useToast();

  function toggleFav(id: string) {
    setFavorites((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  const currentAccount = accounts.find((a) => a.id === current)!;

  return (
    <header className={styles.bar}>
      <div className={styles.chips} role="tablist" aria-label="Quick play servers">
        {quickServers.map((s) => (
          <button
            key={s.id}
            className={`${styles.chip} ${favorites.has(s.id) ? styles.favored : ""}`}
            onClick={() => toggleFav(s.id)}
            title={s.label}
          >
            <span className={styles.chipGlyph}>{s.short}</span>
          </button>
        ))}
      </div>

      <button className={styles.search} onClick={onOpenPalette}>
        <span>Search commands, servers, mods…</span>
        <kbd className={styles.kbd}>/</kbd>
      </button>

      <div className={styles.actions}>
        <div className={styles.dropdownWrap}>
          <button
            className={styles.iconBtn}
            aria-label="Notifications"
            onClick={() => {
              setNotifOpen((o) => !o);
              setAcctOpen(false);
            }}
          >
            🔔
            <span className={styles.badge}>3</span>
          </button>
          {notifOpen && (
            <div className={styles.dropdown}>
              <p className={styles.dropdownTitle}>Notifications</p>
              {[
                "Delta invited you to Hexcraft",
                "New cosmetic crate rotation is live",
                "Server Kingdoms is back online",
              ].map((n, i) => (
                <button
                  key={i}
                  className={styles.dropdownItem}
                  onClick={() => {
                    push(n, "plasma");
                    setNotifOpen(false);
                  }}
                >
                  {n}
                </button>
              ))}
            </div>
          )}
        </div>

        <div className={styles.dropdownWrap}>
          <button
            className={styles.profile}
            onClick={() => {
              setAcctOpen((o) => !o);
              setNotifOpen(false);
            }}
          >
            <span className={styles.avatar}>{currentAccount.initials}</span>
            <span className={styles.profileText}>
              <span className={styles.playingAs}>Playing as</span>
              <span className={styles.username}>{currentAccount.name}</span>
            </span>
          </button>
          {acctOpen && (
            <div className={styles.dropdown}>
              <p className={styles.dropdownTitle}>Accounts</p>
              {accounts.map((a) => (
                <button
                  key={a.id}
                  className={styles.dropdownItem}
                  onClick={() => {
                    setCurrent(a.id);
                    setAcctOpen(false);
                    push(`Switched to ${a.name}`, "signal");
                  }}
                >
                  <span className={styles.avatarSm}>{a.initials}</span>
                  {a.name}
                </button>
              ))}
              <button
                className={`${styles.dropdownItem} ${styles.addAccount}`}
                onClick={() => {
                  setAcctOpen(false);
                  push("Opening Microsoft sign-in…", "warn");
                }}
              >
                + Add account
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

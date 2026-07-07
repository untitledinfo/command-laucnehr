"use client";

import { useEffect, useState } from "react";
import styles from "./FriendsPanel.module.css";
import { friends } from "@/lib/data";
import type { Presence } from "@/lib/data";
import { useToast } from "./ToastProvider";

const presenceOrder: Record<Presence, number> = { "in-game": 0, online: 1, away: 2 };

export function FriendsPanel() {
  const [loading, setLoading] = useState(true);
  const { push } = useToast();

  useEffect(() => {
    const t = setTimeout(() => setLoading(false), 1100);
    return () => clearTimeout(t);
  }, []);

  const sorted = [...friends].sort((a, b) => presenceOrder[a.presence] - presenceOrder[b.presence]);

  return (
    <aside className={styles.panel} aria-label="Friends">
      <div className={styles.header}>
        <h2 className={styles.heading}>Friends</h2>
        <button className={styles.viewAll} onClick={() => push("Opening friends list", "plasma")}>
          View all
        </button>
      </div>

      <ul className={styles.list}>
        {loading
          ? Array.from({ length: 6 }).map((_, i) => <li key={i} className={styles.skeletonRow} />)
          : sorted.map((f) => (
              <li key={f.id} className={styles.row}>
                <span className={styles.avatar}>{f.initials}</span>
                <span className={styles.info}>
                  <span className={styles.name}>{f.name}</span>
                  <span className={styles.detail}>
                    <span className={`${styles.dot} ${styles[f.presence.replace("-", "")]}`} />
                    {f.detail}
                  </span>
                </span>
                <div className={styles.rowActions}>
                  {f.presence === "in-game" && (
                    <button
                      className={styles.iconBtn}
                      aria-label={`Join ${f.name}`}
                      onClick={() => push(`Requesting to join ${f.name}…`, "signal")}
                    >
                      ▶
                    </button>
                  )}
                  <button
                    className={styles.iconBtn}
                    aria-label={`Message ${f.name}`}
                    onClick={() => push(`Opened chat with ${f.name}`, "plasma")}
                  >
                    ✉
                  </button>
                </div>
              </li>
            ))}
      </ul>
    </aside>
  );
}

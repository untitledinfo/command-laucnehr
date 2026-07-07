"use client";

import { useState } from "react";
import styles from "./IconRail.module.css";

const items = [
  { id: "play", label: "Play", glyph: "▲" },
  { id: "mods", label: "Mods", glyph: "◆" },
  { id: "servers", label: "Servers", glyph: "◎" },
  { id: "news", label: "News", glyph: "▤" },
  { id: "store", label: "Store", glyph: "▣" },
];

export function IconRail() {
  const [active, setActive] = useState("play");

  return (
    <nav className={styles.rail} aria-label="Primary">
      <div className={styles.mark} aria-hidden="true">
        ◈
      </div>
      <ul className={styles.list}>
        {items.map((item) => (
          <li key={item.id} className={styles.item}>
            <button
              type="button"
              className={`${styles.btn} ${active === item.id ? styles.active : ""}`}
              onClick={() => setActive(item.id)}
              aria-current={active === item.id}
            >
              <span className={styles.glyph} aria-hidden="true">
                {item.glyph}
              </span>
              <span className={styles.tooltip}>{item.label}</span>
            </button>
          </li>
        ))}
      </ul>
      <button type="button" className={styles.gear} aria-label="Settings shortcut">
        ⚙
        <span className={styles.tooltip}>Settings</span>
      </button>
    </nav>
  );
}

export function BottomNav() {
  const [active, setActive] = useState("play");
  return (
    <nav className={styles.bottomNav} aria-label="Primary mobile">
      {items.map((item) => (
        <button
          key={item.id}
          className={`${styles.bottomBtn} ${active === item.id ? styles.bottomActive : ""}`}
          onClick={() => setActive(item.id)}
        >
          <span aria-hidden="true">{item.glyph}</span>
          <span className={styles.bottomLabel}>{item.label}</span>
        </button>
      ))}
    </nav>
  );
}

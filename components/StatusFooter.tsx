"use client";

import { useEffect, useState } from "react";
import styles from "./StatusFooter.module.css";

export function StatusFooter() {
  const [fps, setFps] = useState(144);
  const [mem, setMem] = useState(38);

  useEffect(() => {
    const t = setInterval(() => {
      setFps((f) => Math.max(90, Math.min(240, f + Math.round(Math.random() * 20 - 10))));
      setMem((m) => Math.max(20, Math.min(90, m + Math.round(Math.random() * 6 - 3))));
    }, 1800);
    return () => clearInterval(t);
  }, []);

  return (
    <footer className={styles.footer}>
      <div className={styles.item}>
        <span className={styles.dot} />
        <span>Connected to command relay</span>
      </div>
      <div className={styles.item}>
        <span className="mono">{fps} fps</span>
      </div>
      <div className={styles.item}>
        <span className={styles.memLabel}>Memory</span>
        <span className={styles.memBar}>
          <span className={styles.memFill} style={{ width: `${mem}%` }} />
        </span>
        <span className="mono">{mem}%</span>
      </div>
      <div className={styles.item}>
        <span>Command Deck v3.0.0</span>
      </div>
    </footer>
  );
}

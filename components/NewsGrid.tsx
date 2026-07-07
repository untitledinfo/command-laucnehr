"use client";

import { useEffect, useState } from "react";
import styles from "./NewsGrid.module.css";
import { newsItems } from "@/lib/data";
import { useToast } from "./ToastProvider";

const tabs = ["News", "Store", "Community"] as const;

export function NewsGrid() {
  const [tab, setTab] = useState<(typeof tabs)[number]>("News");
  const [loading, setLoading] = useState(true);
  const { push } = useToast();

  useEffect(() => {
    const t = setTimeout(() => setLoading(false), 900);
    return () => clearTimeout(t);
  }, []);

  return (
    <section className={styles.wrap} aria-label="Updates">
      <div className={styles.tabRow}>
        <div className={styles.tabs} role="tablist">
          {tabs.map((t) => (
            <button
              key={t}
              role="tab"
              aria-selected={tab === t}
              className={`${styles.tab} ${tab === t ? styles.tabActive : ""}`}
              onClick={() => setTab(t)}
            >
              {t}
            </button>
          ))}
        </div>
        <button className={styles.viewAll} onClick={() => push("Opening full news archive", "plasma")}>
          View all
        </button>
      </div>

      <div className={styles.grid}>
        {loading
          ? Array.from({ length: 4 }).map((_, i) => <div key={i} className={styles.skeleton} />)
          : newsItems.map((n) => (
              <article key={n.id} className={styles.card}>
                <div className={`${styles.thumb} ${styles[n.accent]}`}>
                  <span className={styles.tag}>{n.tag}</span>
                </div>
                <h3 className={styles.cardTitle}>{n.title}</h3>
                <p className={styles.cardExcerpt}>{n.excerpt}</p>
                <button
                  className={styles.readMore}
                  onClick={() => push(`Opening “${n.title}”`, n.accent)}
                >
                  Read more →
                </button>
              </article>
            ))}
      </div>
    </section>
  );
}

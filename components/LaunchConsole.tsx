"use client";

import { useEffect, useRef, useState } from "react";
import styles from "./LaunchConsole.module.css";
import { gameVersions, modLoaders } from "@/lib/data";
import { useToast } from "./ToastProvider";

type LaunchStage = "idle" | "verifying" | "downloading" | "launching" | "done";

const stageLabel: Record<LaunchStage, string> = {
  idle: "ready to launch",
  verifying: "verifying files",
  downloading: "syncing assets",
  launching: "starting jvm",
  done: "launched",
};

export function LaunchConsole() {
  const [version, setVersion] = useState(gameVersions[0].id);
  const [loader, setLoader] = useState<(typeof modLoaders)[number]>("Vanilla");
  const [versionOpen, setVersionOpen] = useState(false);
  const [stage, setStage] = useState<LaunchStage>("idle");
  const [progress, setProgress] = useState(0);
  const [ping, setPing] = useState(34);
  const [players, setPlayers] = useState(48213);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const { push } = useToast();

  useEffect(() => {
    const stored = window.localStorage.getItem("cd-last-version");
    const storedLoader = window.localStorage.getItem("cd-last-loader");
    if (stored) setVersion(stored);
    if (storedLoader) setLoader(storedLoader as (typeof modLoaders)[number]);
  }, []);

  useEffect(() => {
    window.localStorage.setItem("cd-last-version", version);
    window.localStorage.setItem("cd-last-loader", loader);
  }, [version, loader]);

  useEffect(() => {
    const t = setInterval(() => {
      setPing((p) => Math.max(12, Math.min(95, p + (Math.random() * 10 - 5))));
      setPlayers((n) => Math.max(40000, n + Math.round(Math.random() * 40 - 20)));
    }, 2200);
    return () => clearInterval(t);
  }, []);

  function launch() {
    if (stage !== "idle" && stage !== "done") return;
    setStage("verifying");
    setProgress(0);
    push(`Launching ${version} · ${loader}`, "signal");

    const stages: LaunchStage[] = ["verifying", "downloading", "launching", "done"];
    let i = 0;
    if (timerRef.current) clearInterval(timerRef.current);
    timerRef.current = setInterval(() => {
      setProgress((p) => {
        const next = p + 6 + Math.random() * 8;
        if (next >= (i + 1) * 25) {
          i = Math.min(i + 1, stages.length - 1);
          setStage(stages[i]);
        }
        if (next >= 100) {
          if (timerRef.current) clearInterval(timerRef.current);
          push("Minecraft is running", "plasma");
          setTimeout(() => setStage("idle"), 1800);
          return 100;
        }
        return next;
      });
    }, 220);
  }

  const busy = stage !== "idle" && stage !== "done";
  const circumference = 2 * Math.PI * 54;
  const offset = circumference - (Math.min(progress, 100) / 100) * circumference;

  return (
    <section className={styles.console} aria-label="Launch console">
      <div className={styles.left}>
        <p className={styles.eyebrow}>Command deck</p>
        <h1 className={styles.title}>Ready when you are.</h1>
        <p className={styles.sub}>
          Vanilla, modded, or a private world — your last setup is loaded and waiting.
        </p>

        <div className={styles.selectors}>
          <div className={styles.versionWrap}>
            <button
              className={styles.versionBtn}
              onClick={() => setVersionOpen((o) => !o)}
              aria-haspopup="listbox"
              aria-expanded={versionOpen}
            >
              <span className={styles.mono}>{version}</span>
              <span className={styles.chev}>▾</span>
            </button>
            {versionOpen && (
              <ul className={styles.versionList} role="listbox">
                {gameVersions.map((v) => (
                  <li key={v.id}>
                    <button
                      className={styles.versionItem}
                      onClick={() => {
                        setVersion(v.id);
                        setVersionOpen(false);
                      }}
                    >
                      <span className={styles.mono}>{v.label}</span>
                      <span className={styles.versionKind}>{v.kind}</span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className={styles.loaders} role="group" aria-label="Mod loader">
            {modLoaders.map((l) => (
              <button
                key={l}
                className={`${styles.loaderChip} ${loader === l ? styles.loaderActive : ""}`}
                onClick={() => setLoader(l)}
              >
                {l}
              </button>
            ))}
          </div>
        </div>

        <div className={styles.telemetry}>
          <div className={styles.telItem}>
            <span className={styles.telLabel}>Ping</span>
            <span className={`${styles.telValue} ${styles.mono}`}>{Math.round(ping)}ms</span>
          </div>
          <div className={styles.telDivider} />
          <div className={styles.telItem}>
            <span className={styles.telLabel}>Players online</span>
            <span className={`${styles.telValue} ${styles.mono}`}>{players.toLocaleString()}</span>
          </div>
          <div className={styles.telDivider} />
          <div className={styles.telItem}>
            <span className={styles.telLabel}>Status</span>
            <span className={`${styles.telValue} ${styles.mono}`}>{stageLabel[stage]}</span>
          </div>
        </div>
      </div>

      <div className={styles.right}>
        <button
          className={`${styles.dialBtn} ${busy ? styles.dialBusy : ""}`}
          onClick={launch}
          disabled={busy}
        >
          <svg viewBox="0 0 120 120" className={styles.dialSvg} aria-hidden="true">
            <circle cx="60" cy="60" r="54" className={styles.dialTrack} />
            <circle
              cx="60"
              cy="60"
              r="54"
              className={styles.dialProgress}
              strokeDasharray={circumference}
              strokeDashoffset={busy ? offset : circumference}
            />
            {Array.from({ length: 24 }).map((_, i) => (
              <line
                key={i}
                x1="60"
                y1="4"
                x2="60"
                y2="10"
                className={styles.tick}
                transform={`rotate(${i * 15} 60 60)`}
              />
            ))}
          </svg>
          <span className={styles.dialLabel}>
            {busy ? `${Math.round(progress)}%` : "Launch"}
          </span>
        </button>
        <p className={styles.dialSub}>{stageLabel[stage]}</p>
      </div>
    </section>
  );
}

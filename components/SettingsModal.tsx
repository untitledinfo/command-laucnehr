"use client";

import { useEffect, useState } from "react";
import styles from "./SettingsModal.module.css";
import { useToast } from "./ToastProvider";

const accents = [
  { id: "signal", label: "Phosphor", value: "#39ffc1", dim: "#1c8f6c" },
  { id: "plasma", label: "Plasma", value: "#6c8cff", dim: "#3d4f96" },
  { id: "warn", label: "Amber", value: "#ffb648", dim: "#8a6420" },
];

export function SettingsModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [ram, setRam] = useState(4);
  const [resolution, setResolution] = useState("1280x720");
  const [javaArgs, setJavaArgs] = useState("-Xmx4G -XX:+UseG1GC");
  const [accent, setAccent] = useState("signal");
  const { push } = useToast();

  useEffect(() => {
    const storedAccent = window.localStorage.getItem("cd-accent");
    if (storedAccent) applyAccent(storedAccent, false);
  }, []);

  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === "Escape" && open) onClose();
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  function applyAccent(id: string, notify = true) {
    const a = accents.find((x) => x.id === id) ?? accents[0];
    document.documentElement.style.setProperty("--accent", a.value);
    document.documentElement.style.setProperty("--accent-dim", a.dim);
    setAccent(a.id);
    window.localStorage.setItem("cd-accent", a.id);
    if (notify) push(`Accent set to ${a.label}`, id as "signal" | "plasma" | "warn");
  }

  if (!open) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div
        className={styles.modal}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Settings"
      >
        <div className={styles.header}>
          <h2 className={styles.title}>Console settings</h2>
          <button className={styles.close} onClick={onClose} aria-label="Close settings">
            ✕
          </button>
        </div>

        <div className={styles.section}>
          <label className={styles.label} htmlFor="ram-slider">
            Memory allocation
            <span className={`${styles.value} mono`}>{ram} GB</span>
          </label>
          <input
            id="ram-slider"
            type="range"
            min={1}
            max={16}
            step={1}
            value={ram}
            onChange={(e) => setRam(Number(e.target.value))}
            className={styles.slider}
          />
        </div>

        <div className={styles.row}>
          <div className={styles.section}>
            <label className={styles.label} htmlFor="resolution">
              Resolution
            </label>
            <select
              id="resolution"
              className={styles.select}
              value={resolution}
              onChange={(e) => setResolution(e.target.value)}
            >
              <option>1280x720</option>
              <option>1600x900</option>
              <option>1920x1080</option>
              <option>2560x1440</option>
            </select>
          </div>

          <div className={styles.section}>
            <label className={styles.label} htmlFor="java-args">
              Java arguments
            </label>
            <input
              id="java-args"
              className={styles.input}
              value={javaArgs}
              onChange={(e) => setJavaArgs(e.target.value)}
            />
          </div>
        </div>

        <div className={styles.section}>
          <label className={styles.label}>Accent color</label>
          <div className={styles.swatches}>
            {accents.map((a) => (
              <button
                key={a.id}
                className={`${styles.swatch} ${accent === a.id ? styles.swatchActive : ""}`}
                style={{ background: a.value }}
                onClick={() => applyAccent(a.id)}
                aria-label={a.label}
                title={a.label}
              />
            ))}
          </div>
        </div>

        <div className={styles.footer}>
          <button
            className={styles.save}
            onClick={() => {
              push("Settings saved", "signal");
              onClose();
            }}
          >
            Save changes
          </button>
        </div>
      </div>
    </div>
  );
}

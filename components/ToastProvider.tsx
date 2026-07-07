"use client";

import { createContext, useCallback, useContext, useRef, useState } from "react";
import styles from "./Toast.module.css";

interface Toast {
  id: number;
  message: string;
  kind: "signal" | "plasma" | "warn";
}

interface ToastContextValue {
  push: (message: string, kind?: Toast["kind"]) => void;
}

const ToastContext = createContext<ToastContextValue>({ push: () => {} });

export function useToast() {
  return useContext(ToastContext);
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const idRef = useRef(0);

  const push = useCallback((message: string, kind: Toast["kind"] = "signal") => {
    const id = ++idRef.current;
    setToasts((t) => [...t, { id, message, kind }]);
    setTimeout(() => {
      setToasts((t) => t.filter((toast) => toast.id !== id));
    }, 3200);
  }, []);

  return (
    <ToastContext.Provider value={{ push }}>
      {children}
      <div className={styles.stack} aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`${styles.toast} ${styles[t.kind]}`}>
            <span className={styles.dot} />
            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

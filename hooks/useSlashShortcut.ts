"use client";

import { useEffect } from "react";

export function useSlashShortcut(onTrigger: () => void) {
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      const target = e.target as HTMLElement;
      const typing = ["INPUT", "TEXTAREA"].includes(target.tagName);
      if (e.key === "/" && !typing) {
        e.preventDefault();
        onTrigger();
      }
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onTrigger]);
}

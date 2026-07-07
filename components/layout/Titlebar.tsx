'use client';

import { Minus, Square, X } from 'lucide-react';

declare global {
  interface Window {
    launcher?: {
      minimize: () => void;
      maximize: () => void;
      close: () => void;
    };
  }
}

export function Titlebar() {
  const minimize = () => window.launcher?.minimize();
  const maximize = () => window.launcher?.maximize();
  const close = () => window.launcher?.close();

  return (
    <div className="titlebar-drag h-9 flex items-center justify-between pl-4 pr-2 shrink-0 border-b border-white/5 select-none">
      <div className="flex items-center gap-2 text-xs font-medium text-white/60">
        <span className="h-1.5 w-1.5 rounded-full bg-primary animate-glow" />
        Command Launcher
      </div>
      <div className="flex items-center titlebar-no-drag">
        <button
          aria-label="Minimize"
          onClick={minimize}
          className="h-9 w-11 flex items-center justify-center text-white/50 hover:bg-white/8 hover:text-white transition-colors"
        >
          <Minus size={14} />
        </button>
        <button
          aria-label="Maximize"
          onClick={maximize}
          className="h-9 w-11 flex items-center justify-center text-white/50 hover:bg-white/8 hover:text-white transition-colors"
        >
          <Square size={12} />
        </button>
        <button
          aria-label="Close"
          onClick={close}
          className="h-9 w-11 flex items-center justify-center text-white/50 hover:bg-primary hover:text-white transition-colors"
        >
          <X size={15} />
        </button>
      </div>
    </div>
  );
}

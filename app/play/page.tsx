'use client';

import { useState } from 'react';
import { Play as PlayIcon, ChevronDown, Wrench, Terminal as TerminalIcon } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { SectionHeader } from '@/components/ui/SectionHeader';
import { Button } from '@/components/ui/Button';
import { ProgressBar } from '@/components/ui/ProgressBar';

export default function PlayPage() {
  const [ram, setRam] = useState(6);
  const [launching, setLaunching] = useState(false);
  const [progress, setProgress] = useState(0);

  const handleLaunch = () => {
    setLaunching(true);
    setProgress(0);
    const id = setInterval(() => {
      setProgress((p) => {
        if (p >= 100) {
          clearInterval(id);
          setLaunching(false);
          return 100;
        }
        return p + 8;
      });
    }, 250);
  };

  return (
    <div className="animate-fade-in">
      <SectionHeader eyebrow="Play" title="Launch Minecraft" description="Configure your session and launch." />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <Card className="lg:col-span-2" strong>
          <div className="flex items-center gap-4 mb-6">
            <div className="h-14 w-14 rounded-xl bg-gradient-to-br from-primary/30 to-secondary/10 shrink-0" />
            <div className="flex-1 min-w-0">
              <p className="text-xs text-white/40 mb-0.5">Active Profile</p>
              <button className="flex items-center gap-1.5 text-white font-display font-semibold text-lg">
                Fabric 1.21.1
                <ChevronDown size={16} className="text-white/40" />
              </button>
            </div>
            <div className="text-right">
              <p className="text-xs text-white/40 mb-0.5">Player</p>
              <p className="text-sm font-medium text-white">Firepdx</p>
            </div>
          </div>

          {launching ? (
            <div className="space-y-3 mb-2">
              <ProgressBar value={progress} label="Launching — verifying assets" />
              <Button variant="outline" className="w-full" onClick={() => setLaunching(false)}>
                Cancel Launch
              </Button>
            </div>
          ) : (
            <Button className="w-full py-3.5 text-base" onClick={handleLaunch}>
              <PlayIcon size={18} fill="currentColor" />
              Play
            </Button>
          )}
        </Card>

        <Card>
          <h3 className="font-display text-sm font-semibold text-white flex items-center gap-2 mb-4">
            <Wrench size={15} className="text-primary" /> Launch Options
          </h3>
          <div className="space-y-4 text-sm">
            <div>
              <div className="flex justify-between text-white/60 mb-1.5">
                <span>Allocated RAM</span>
                <span>{ram} GB</span>
              </div>
              <input
                type="range"
                min={2}
                max={16}
                value={ram}
                onChange={(e) => setRam(Number(e.target.value))}
                className="w-full accent-primary titlebar-no-drag"
              />
            </div>
            <div className="flex items-center justify-between">
              <span className="text-white/60">Java</span>
              <span className="text-white/80">Auto-detected (21)</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-white/60">Resolution</span>
              <span className="text-white/80">1280 × 720</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-white/60">Fullscreen</span>
              <span className="text-white/30">Off</span>
            </div>
          </div>
        </Card>
      </div>

      <Card className="mt-4">
        <h3 className="font-display text-sm font-semibold text-white flex items-center gap-2 mb-3">
          <TerminalIcon size={15} className="text-primary" /> Launch Log
        </h3>
        <div className="rounded-lg bg-black/40 border border-white/5 p-3 font-mono text-xs text-white/40 h-28 overflow-y-auto space-y-1">
          <p>[12:04:01] Checking Java installation...</p>
          <p>[12:04:02] Java 21.0.2 found at default path.</p>
          <p>[12:04:02] Verifying game files for Fabric 1.21.1...</p>
          <p>[12:04:03] All assets up to date.</p>
          <p className="text-white/60">[12:04:03] Ready to launch.</p>
        </div>
      </Card>
    </div>
  );
}

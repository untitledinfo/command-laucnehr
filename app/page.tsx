'use client';

import { motion } from 'framer-motion';
import { Cpu, MemoryStick, HardDrive, Wifi, Play as PlayIcon, Download, Clock } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { StatWidget } from '@/components/ui/StatWidget';
import { Button } from '@/components/ui/Button';
import { ProgressBar } from '@/components/ui/ProgressBar';
import { useEffect, useState } from 'react';

function useFakeLiveStat(base: number, jitter: number) {
  const [value, setValue] = useState(base);
  useEffect(() => {
    const id = setInterval(() => {
      setValue((v) => {
        const next = v + (Math.random() - 0.5) * jitter;
        return Math.min(98, Math.max(4, next));
      });
    }, 1800);
    return () => clearInterval(id);
  }, [jitter]);
  return value;
}

export default function HomePage() {
  const cpu = useFakeLiveStat(32, 10);
  const ram = useFakeLiveStat(54, 6);

  return (
    <div className="animate-fade-in space-y-6">
      {/* Hero */}
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="relative overflow-hidden rounded-card glass-strong p-8"
      >
        <div className="absolute -right-16 -top-16 h-64 w-64 rounded-full bg-primary/20 blur-3xl" />
        <p className="text-xs font-medium uppercase tracking-[0.18em] text-primary/80 mb-3">
          Welcome back
        </p>
        <h1 className="font-display text-3xl font-semibold text-white mb-2">Ready to play, Firepdx?</h1>
        <p className="text-sm text-white/50 mb-6 max-w-md">
          Fabric 1.21.1 is set as your active profile — 18 mods enabled, last played 2 days ago.
        </p>
        <div className="flex items-center gap-3">
          <Button className="px-6 py-3 text-base">
            <PlayIcon size={18} fill="currentColor" />
            Play
          </Button>
          <Button variant="outline">Change Version</Button>
        </div>
      </motion.div>

      {/* Live system stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <StatWidget icon={Cpu} label="CPU" value={`${cpu.toFixed(0)}%`} sub="8 cores active" />
        <StatWidget icon={MemoryStick} label="RAM" value={`${ram.toFixed(0)}%`} sub="6.2 / 12 GB" />
        <StatWidget icon={HardDrive} label="Storage" value="214 GB free" sub="SSD detected" />
        <StatWidget icon={Wifi} label="Network" value="Online" sub="42 ms latency" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Download queue */}
        <Card className="lg:col-span-2">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-display text-base font-semibold text-white flex items-center gap-2">
              <Download size={16} className="text-primary" /> Download Queue
            </h2>
            <span className="text-xs text-white/40">1 active</span>
          </div>
          <div className="space-y-4">
            <div>
              <ProgressBar value={68} label="Sodium 0.5.11 — Fabric 1.21.1" />
            </div>
            <div className="opacity-40">
              <ProgressBar value={0} label="Iris Shaders — queued" />
            </div>
          </div>
        </Card>

        {/* Recent activity */}
        <Card>
          <h2 className="font-display text-base font-semibold text-white flex items-center gap-2 mb-4">
            <Clock size={16} className="text-primary" /> Recent Activity
          </h2>
          <ul className="space-y-3 text-sm text-white/60">
            <li className="flex justify-between"><span>Launched Fabric 1.21.1</span><span className="text-white/30">2d</span></li>
            <li className="flex justify-between"><span>Installed Sodium</span><span className="text-white/30">2d</span></li>
            <li className="flex justify-between"><span>Backed up world &quot;PGC SMP&quot;</span><span className="text-white/30">5d</span></li>
            <li className="flex justify-between"><span>Added server pgc.mc.fun</span><span className="text-white/30">1w</span></li>
          </ul>
        </Card>
      </div>

      {/* Recently played */}
      <div>
        <h2 className="font-display text-base font-semibold text-white mb-3">Recently Played</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {[
            { name: 'Fabric 1.21.1', tag: 'Modded' },
            { name: 'Vanilla 1.21', tag: 'Survival' },
            { name: 'Forge 1.20.1', tag: 'Modded' },
            { name: 'Snapshot 26w14a', tag: 'Testing' },
          ].map((v) => (
            <Card key={v.name} className="cursor-pointer hover:border-primary/30 transition-colors">
              <div className="h-16 w-full rounded-lg bg-gradient-to-br from-primary/25 to-secondary/10 mb-3" />
              <p className="text-sm font-medium text-white truncate">{v.name}</p>
              <p className="text-xs text-white/40">{v.tag}</p>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}

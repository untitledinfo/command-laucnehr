import { Card } from './Card';
import { SectionHeader } from './SectionHeader';
import { type LucideIcon } from 'lucide-react';

interface PlaceholderGridProps {
  eyebrow: string;
  title: string;
  description: string;
  icon: LucideIcon;
  features: string[];
}

export function PlaceholderGrid({ eyebrow, title, description, icon: Icon, features }: PlaceholderGridProps) {
  return (
    <div className="animate-fade-in">
      <SectionHeader eyebrow={eyebrow} title={title} description={description} />
      <Card className="mb-6 flex items-center gap-4 border-primary/20">
        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary/15 text-primary shrink-0">
          <Icon size={20} />
        </div>
        <p className="text-sm text-white/60">
          This screen is scaffolded and routed, but not yet wired to live data or Electron IPC.
          Planned functionality for this section:
        </p>
      </Card>
      <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
        {features.map((f) => (
          <Card key={f} className="text-sm text-white/70 py-3.5">
            {f}
          </Card>
        ))}
      </div>
    </div>
  );
}

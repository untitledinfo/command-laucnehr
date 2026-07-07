import { type LucideIcon } from 'lucide-react';
import { Card } from './Card';

export function StatWidget({
  icon: Icon,
  label,
  value,
  sub,
}: {
  icon: LucideIcon;
  label: string;
  value: string;
  sub?: string;
}) {
  return (
    <Card className="flex items-center gap-3.5">
      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary/15 text-primary">
        <Icon size={18} />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-white/50">{label}</p>
        <p className="font-display text-lg font-semibold text-white leading-tight truncate">{value}</p>
        {sub && <p className="text-[11px] text-white/35 mt-0.5">{sub}</p>}
      </div>
    </Card>
  );
}

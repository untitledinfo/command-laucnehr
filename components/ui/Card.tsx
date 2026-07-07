'use client';

import { cn } from '@/lib/utils';
import { type ReactNode } from 'react';

export function Card({
  children,
  className,
  strong = false,
}: {
  children: ReactNode;
  className?: string;
  strong?: boolean;
}) {
  return (
    <div className={cn(strong ? 'glass-strong rounded-card' : 'glass', 'p-5', className)}>
      {children}
    </div>
  );
}

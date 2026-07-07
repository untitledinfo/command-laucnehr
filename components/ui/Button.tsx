'use client';

import { cn } from '@/lib/utils';
import { type ButtonHTMLAttributes } from 'react';
import { motion } from 'framer-motion';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'ghost' | 'outline';
}

export function Button({ variant = 'primary', className, children, ...props }: ButtonProps) {
  const base = 'titlebar-no-drag inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-medium transition-colors focus-visible:outline-2 focus-visible:outline-primary disabled:opacity-40 disabled:pointer-events-none';
  const variants = {
    primary: 'bg-gradient-to-r from-primary to-secondary text-white shadow-lg shadow-red-950/40 hover:brightness-110',
    ghost: 'text-white/70 hover:text-white hover:bg-white/8',
    outline: 'border border-white/15 text-white/80 hover:bg-white/8 hover:border-white/25',
  };
  return (
    <motion.button
      whileTap={{ scale: 0.97 }}
      className={cn(base, variants[variant], className)}
      {...(props as any)}
    >
      {children}
    </motion.button>
  );
}

'use client';

import { usePathname } from 'next/navigation';
import Link from 'next/link';
import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronsLeft, ChevronsRight } from 'lucide-react';
import { navItems, navItemsBottom, type NavItem } from '@/lib/navigation';
import { cn } from '@/lib/utils';

function NavRow({ item, collapsed, active }: { item: NavItem; collapsed: boolean; active: boolean }) {
  const Icon = item.icon;
  return (
    <Link href={item.href} className="relative block titlebar-no-drag">
      <motion.div
        whileHover={{ x: collapsed ? 0 : 2 }}
        className={cn(
          'group relative flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition-colors',
          active ? 'text-white' : 'text-white/50 hover:text-white/85 hover:bg-white/5'
        )}
      >
        {active && (
          <motion.div
            layoutId="active-pill"
            className="absolute inset-0 rounded-xl bg-white/8 border border-white/10"
            transition={{ type: 'spring', stiffness: 400, damping: 32 }}
          />
        )}
        <span
          className={cn(
            'absolute left-0 top-1/2 -translate-y-1/2 h-4 w-[3px] rounded-full bg-primary transition-opacity',
            active ? 'opacity-100' : 'opacity-0'
          )}
        />
        <Icon size={18} className="relative shrink-0" />
        {!collapsed && <span className="relative truncate">{item.label}</span>}
        {!collapsed && item.badge && (
          <span className="relative ml-auto flex h-4.5 min-w-4.5 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-semibold text-white">
            {item.badge}
          </span>
        )}
      </motion.div>
    </Link>
  );
}

export function Sidebar() {
  const pathname = usePathname();
  const [collapsed, setCollapsed] = useState(false);

  const isActive = (href: string) => (href === '/' ? pathname === '/' : pathname?.startsWith(href));

  return (
    <motion.aside
      animate={{ width: collapsed ? 76 : 232 }}
      transition={{ type: 'spring', stiffness: 300, damping: 32 }}
      className="glass-strong m-3 mr-0 flex flex-col rounded-card shrink-0 overflow-hidden"
    >
      <div className="flex-1 overflow-y-auto px-2.5 py-3 space-y-1">
        {navItems.map((item) => (
          <NavRow key={item.href} item={item} collapsed={collapsed} active={isActive(item.href)} />
        ))}
      </div>

      <div className="px-2.5 py-3 border-t border-white/8 space-y-1">
        {navItemsBottom.map((item) => (
          <NavRow key={item.href} item={item} collapsed={collapsed} active={isActive(item.href)} />
        ))}
        <button
          onClick={() => setCollapsed((c) => !c)}
          className="titlebar-no-drag flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-white/40 hover:text-white/80 hover:bg-white/5 transition-colors"
        >
          {collapsed ? <ChevronsRight size={18} /> : <ChevronsLeft size={18} />}
          {!collapsed && <span>Collapse</span>}
        </button>
      </div>
    </motion.aside>
  );
}

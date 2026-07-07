import {
  Home,
  Play,
  Layers,
  Puzzle,
  Image as ImageIcon,
  Sparkles,
  Server,
  Globe,
  Download,
  Users,
  Newspaper,
  Store,
  Plug,
  Settings,
  Terminal,
  Info,
  type LucideIcon,
} from 'lucide-react';

export interface NavItem {
  label: string;
  href: string;
  icon: LucideIcon;
  badge?: number;
}

export const navItems: NavItem[] = [
  { label: 'Home', href: '/', icon: Home },
  { label: 'Play', href: '/play', icon: Play },
  { label: 'Versions', href: '/versions', icon: Layers },
  { label: 'Mods', href: '/mods', icon: Puzzle, badge: 3 },
  { label: 'Resource Packs', href: '/resource-packs', icon: ImageIcon },
  { label: 'Shaders', href: '/shaders', icon: Sparkles },
  { label: 'Servers', href: '/servers', icon: Server },
  { label: 'Worlds', href: '/worlds', icon: Globe },
  { label: 'Downloads', href: '/downloads', icon: Download, badge: 1 },
  { label: 'Accounts', href: '/accounts', icon: Users },
  { label: 'News', href: '/news', icon: Newspaper },
  { label: 'Marketplace', href: '/marketplace', icon: Store },
  { label: 'Plugins', href: '/plugins', icon: Plug },
];

export const navItemsBottom: NavItem[] = [
  { label: 'Settings', href: '/settings', icon: Settings },
  { label: 'Developer', href: '/developer', icon: Terminal },
  { label: 'About', href: '/about', icon: Info },
];

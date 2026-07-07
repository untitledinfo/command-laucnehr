import { Puzzle } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function ModsPage() {
  return (
    <PlaceholderGrid
      icon={Puzzle}
      eyebrow="Mods"
      title="Mod Manager"
      description="Browse, install, and manage mods with dependency resolution."
      features={[
        'Built-in mod manager', 'Enable / disable', 'Mod profiles', 'Mod search (Modrinth)',
        'Update mods', 'Import mods', 'Export mods', 'Drag and drop',
        'Dependency checker', 'Conflict detector',
      ]}
    />
  );
}

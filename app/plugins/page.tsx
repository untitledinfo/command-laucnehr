import { Plug } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function PluginsPage() {
  return (
    <PlaceholderGrid
      icon={Plug}
      eyebrow="Plugins"
      title="Plugins"
      description="Extend the launcher with community plugins."
      features={[
        'Installed plugins', 'Plugin store', 'Plugin details', 'Updates',
        'Enable / disable', 'Developer mode',
      ]}
    />
  );
}

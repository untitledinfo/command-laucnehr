import { Server } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function ServersPage() {
  return (
    <PlaceholderGrid
      icon={Server}
      eyebrow="Multiplayer"
      title="Server List"
      description="Manage favorite servers, ping status, and quick join."
      features={[
        'Server list', 'Ping checker', 'Favorite servers', 'Server notes',
        'Quick join', 'LAN detection', 'Server icons', 'Server categories',
        'Player count', 'Server history',
      ]}
    />
  );
}

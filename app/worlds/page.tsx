import { Globe } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function WorldsPage() {
  return (
    <PlaceholderGrid
      icon={Globe}
      eyebrow="Singleplayer"
      title="Worlds"
      description="Back up, restore, and manage your local worlds."
      features={[
        'World backup', 'Auto backup', 'Restore worlds', 'Duplicate worlds',
        'Delete worlds', 'Import worlds', 'Export worlds', 'World preview',
        'World statistics', 'Favorite worlds',
      ]}
    />
  );
}

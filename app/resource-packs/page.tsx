import { Image as ImageIcon } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function ResourcePacksPage() {
  return (
    <PlaceholderGrid
      icon={ImageIcon}
      eyebrow="Resource Packs"
      title="Resource Pack Manager"
      description="Preview, enable, and organize your texture packs."
      features={[
        'Pack manager', 'Enable / disable', 'Auto download packs', 'Preview packs',
        'Pack sorting', 'HD pack support', 'ZIP validation', 'Favorite packs',
        'Cloud backup', 'Pack sharing',
      ]}
    />
  );
}

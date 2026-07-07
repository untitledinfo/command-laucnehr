import { Store } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function MarketplacePage() {
  return (
    <PlaceholderGrid
      icon={Store}
      eyebrow="Marketplace"
      title="Marketplace"
      description="Themes, modpacks, and cosmetics — Phase 4 territory."
      features={[
        'Themes', 'Plugins', 'Modpacks', 'Cosmetics',
        'Featured', 'Top rated', 'New', 'Wishlist',
      ]}
    />
  );
}

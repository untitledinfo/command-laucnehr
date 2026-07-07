import { Newspaper } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function NewsPage() {
  return (
    <PlaceholderGrid
      icon={Newspaper}
      eyebrow="News"
      title="News Feed"
      description="Launcher updates, Minecraft news, and community posts."
      features={[
        'Launcher updates', 'Minecraft news', 'Community news', 'Pinned posts',
        'Categories', 'Search', 'Tags',
      ]}
    />
  );
}

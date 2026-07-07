import { Info } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function AboutPage() {
  return (
    <PlaceholderGrid
      icon={Info}
      eyebrow="About"
      title="About Command Launcher"
      description="Version info, credits, and links."
      features={['Version', 'Changelog', 'Credits', 'Licenses', 'Report a bug', 'Discord']}
    />
  );
}

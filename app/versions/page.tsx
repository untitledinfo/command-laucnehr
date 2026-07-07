import { Layers } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function VersionsPage() {
  return (
    <PlaceholderGrid
      icon={Layers}
      eyebrow="Versions"
      title="Version Manager"
      description="Install and manage every Minecraft version and mod loader."
      features={[
        'Vanilla support', 'Fabric support', 'Forge support', 'NeoForge support',
        'Quilt support', 'Snapshot support', 'Old Beta versions', 'Alpha versions',
        'Version search', 'Favorite versions', 'Install / uninstall', 'Repair installation',
      ]}
    />
  );
}

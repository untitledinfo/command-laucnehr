import { Sparkles } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function ShadersPage() {
  return (
    <PlaceholderGrid
      icon={Sparkles}
      eyebrow="Shaders"
      title="Shader Manager"
      description="Install and preview shader packs with compatibility checks."
      features={[
        'Shader manager', 'Shader preview', 'Install shaders', 'Performance ratings',
        'Auto compatibility check', 'Import ZIP', 'Remove shader', 'Favorite shader',
        'Update shader', 'One-click apply',
      ]}
    />
  );
}

import { Terminal } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function DeveloperPage() {
  return (
    <PlaceholderGrid
      icon={Terminal}
      eyebrow="Developer"
      title="Developer Tools"
      description="Debug console, logs, and performance diagnostics."
      features={[
        'Debug console', 'Realtime logs', 'FPS graph', 'Memory graph',
        'CPU graph', 'Crash reports', 'Diagnostics', 'Export logs',
      ]}
    />
  );
}

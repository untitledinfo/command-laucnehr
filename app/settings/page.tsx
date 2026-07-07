import { Settings } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function SettingsPage() {
  return (
    <PlaceholderGrid
      icon={Settings}
      eyebrow="Settings"
      title="Settings"
      description="Search and configure every part of the launcher."
      features={[
        'Appearance', 'Performance', 'Java', 'Launcher',
        'Minecraft', 'Downloads', 'Accounts', 'Security',
        'Notifications', 'Accessibility', 'Developer', 'Advanced',
      ]}
    />
  );
}

import { Users } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function AccountsPage() {
  return (
    <PlaceholderGrid
      icon={Users}
      eyebrow="Account"
      title="Accounts"
      description="Manage Microsoft and offline accounts."
      features={[
        'Microsoft login', 'Offline login', 'Multiple accounts', 'Quick account switch',
        'Account avatars', 'Account nicknames', 'Auto login', 'Secure token storage',
        'Guest mode', 'Account backup',
      ]}
    />
  );
}

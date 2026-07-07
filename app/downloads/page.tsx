import { Download } from 'lucide-react';
import { PlaceholderGrid } from '@/components/ui/PlaceholderGrid';

export default function DownloadsPage() {
  return (
    <PlaceholderGrid
      icon={Download}
      eyebrow="Downloads"
      title="Download Manager"
      description="Track, pause, and verify every download."
      features={[
        'Download queue', 'Resume downloads', 'Download speed limit', 'Mirror servers',
        'Integrity check', 'Progress graph', 'Background updates', 'Notifications',
        'Retry failed downloads', 'File verification',
      ]}
    />
  );
}

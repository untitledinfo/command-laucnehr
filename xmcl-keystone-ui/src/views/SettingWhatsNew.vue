<template>
  <SettingCard :title="t('setting.whatsNew')" icon="auto_awesome">
    <div class="whats-new-subtitle text-caption opacity-70 mb-4">
      {{ t('setting.whatsNewSubtitle') }}
    </div>
    <div class="whats-new-grid">
      <div
        v-for="(feature, idx) in features"
        :key="idx"
        class="whats-new-card pa-4"
      >
        <div class="whats-new-icon mb-3">
          <v-icon color="primary" size="22">{{ feature.icon }}</v-icon>
        </div>
        <div class="text-subtitle-2 font-weight-bold mb-1">
          {{ t(feature.titleKey) }}
        </div>
        <div class="text-caption opacity-75">
          {{ t(feature.descKey) }}
        </div>
      </div>
    </div>
  </SettingCard>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import SettingCard from '@/components/SettingCard.vue'

const { t } = useI18n()

const features = [
  { icon: 'auto_awesome', titleKey: 'setting.whatsNewFeature1Title', descKey: 'setting.whatsNewFeature1Desc' },
  { icon: 'rounded_corner', titleKey: 'setting.whatsNewFeature2Title', descKey: 'setting.whatsNewFeature2Desc' },
  { icon: 'motion_photos_auto', titleKey: 'setting.whatsNewFeature3Title', descKey: 'setting.whatsNewFeature3Desc' },
  { icon: 'gradient', titleKey: 'setting.whatsNewFeature4Title', descKey: 'setting.whatsNewFeature4Desc' },
  { icon: 'info', titleKey: 'setting.whatsNewFeature5Title', descKey: 'setting.whatsNewFeature5Desc' },
]
</script>

<style scoped>
.whats-new-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.whats-new-card {
  border-radius: var(--card-item-radius, 10px);
  background: rgba(var(--v-theme-surface), 0.4);
  border: var(--card-subsection-border);
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1), border-color 0.25s ease, background-color 0.2s ease;
}

.whats-new-card:hover {
  transform: translateY(-3px);
  background: rgba(var(--v-theme-surface), 0.65);
  border-color: rgba(var(--v-theme-primary), 0.35);
}

.whats-new-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(var(--v-theme-primary), 0.12);
}
</style>

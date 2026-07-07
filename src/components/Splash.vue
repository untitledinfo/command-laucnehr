<template>
  <Transition name="splash-fade">
    <div v-if="visible" class="splash">
      <div class="splash-glow"></div>
      <div class="splash-logo">
        <div class="logo-ring"></div>
        <div class="logo-core">C</div>
      </div>
      <div class="splash-title accent-gradient-text">COMMAND LAUNCHER</div>
      <div class="splash-dots">
        <span></span><span></span><span></span>
      </div>
      <div class="splash-status">{{ statusText }}</div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ visible: boolean; statusText?: string }>(), {
  statusText: 'Loading your setup...'
});
</script>

<style scoped>
.splash {
  position: fixed;
  inset: 0;
  z-index: 300;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: radial-gradient(900px 500px at 50% 30%, rgba(var(--accent-rgb), 0.16), transparent),
    var(--bg-darkest);
}
.splash-glow {
  position: absolute;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(var(--accent-rgb), 0.25), transparent 70%);
  filter: blur(10px);
  animation: pulse-soft 2.6s ease-in-out infinite;
}
@keyframes pulse-soft {
  0%,
  100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}
.splash-logo {
  position: relative;
  width: 84px;
  height: 84px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 3px solid transparent;
  border-top-color: var(--accent);
  border-right-color: var(--accent-2);
  animation: spin 1.1s linear infinite;
}
.logo-core {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 26px;
  color: #08080c;
  box-shadow: 0 12px 32px -10px rgba(var(--accent-rgb), 0.6);
}
.splash-title {
  font-weight: 800;
  font-size: 15px;
  letter-spacing: 0.14em;
}
.splash-dots {
  display: flex;
  gap: 6px;
}
.splash-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent-soft);
  animation: marquee-dot 1.2s ease-in-out infinite;
}
.splash-dots span:nth-child(2) {
  animation-delay: 0.15s;
}
.splash-dots span:nth-child(3) {
  animation-delay: 0.3s;
}
.splash-status {
  font-size: 11.5px;
  color: var(--text-faint);
  letter-spacing: 0.02em;
}

.splash-fade-enter-active {
  transition: opacity 0.2s ease;
}
.splash-fade-leave-active {
  transition: opacity 0.4s ease;
}
.splash-fade-enter-from,
.splash-fade-leave-to {
  opacity: 0;
}
</style>

<template>
  <Transition name="dialog-fade">
    <div class="overlay" @click.self="$emit('close')">
      <div class="dialog glass-panel pop-in">
        <div class="dialog-header">
          <h3>Accounts</h3>
          <button class="close-btn" @click="$emit('close')">&#10005;</button>
        </div>

        <div class="account-list">
          <TransitionGroup name="list">
            <div
              v-for="acc in store.accounts"
              :key="acc.uuid"
              class="account-row"
              :class="{ active: acc.uuid === store.activeAccount?.uuid }"
              @click="select(acc.uuid)"
            >
              <div class="avatar" :style="{ background: gradientFor(acc.name) }">{{ acc.name.slice(0, 1).toUpperCase() }}</div>
              <div class="account-info">
                <div class="account-name">{{ acc.name }}</div>
                <div class="account-type">{{ acc.type === 'msa' ? 'Microsoft' : 'Offline' }}</div>
              </div>
              <span v-if="acc.uuid === store.activeAccount?.uuid" class="badge">Active</span>
              <button class="remove-btn" @click.stop="remove(acc.uuid)">Remove</button>
            </div>
          </TransitionGroup>
          <div v-if="store.accounts.length === 0" class="empty">No accounts yet &mdash; add one below.</div>
        </div>

        <div class="add-row">
          <input v-model="offlineName" placeholder="Offline username" @keyup.enter="addOffline" />
          <button class="btn" @click="addOffline">+ Add Offline</button>
        </div>

        <div class="msa-row">
          <button class="btn" :disabled="loggingIn" @click="loginMsa">
            <span v-if="loggingIn" class="mini-spinner"></span>
            {{ loggingIn ? 'Waiting for sign-in...' : 'Sign in with Microsoft' }}
          </button>
          <Transition name="fade-scale">
            <div v-if="deviceCode" class="device-code">
              Go to <a href="#" @click.prevent="openLink">{{ deviceCode.verificationUri }}</a> and enter code:
              <strong>{{ deviceCode.userCode }}</strong>
            </div>
          </Transition>
          <div v-if="msaError" class="msa-error">{{ msaError }}</div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue';
import { useLauncherStore } from '../stores/launcher';
import { toast } from '../lib/toast';

defineEmits<{ close: [] }>();
const store = useLauncherStore();

const offlineName = ref('');
const loggingIn = ref(false);
const deviceCode = ref<{ userCode: string; verificationUri: string } | null>(null);
const msaError = ref('');

let unsub: (() => void) | null = null;

function gradientFor(name: string) {
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = (hash * 31 + name.charCodeAt(i)) & 0xffffff;
  const h1 = hash % 360;
  const h2 = (h1 + 60) % 360;
  return `linear-gradient(135deg, hsl(${h1}, 80%, 62%), hsl(${h2}, 80%, 58%))`;
}

async function addOffline() {
  const name = offlineName.value.trim();
  if (!name) return;
  await window.launcherApi.accounts.addOffline(name);
  offlineName.value = '';
  await store.refreshAccounts();
  toast.success('Account added', name);
}

async function select(uuid: string) {
  await window.launcherApi.accounts.setActive(uuid);
  await store.refreshAccounts();
}

async function remove(uuid: string) {
  await window.launcherApi.accounts.remove(uuid);
  await store.refreshAccounts();
  toast.info('Account removed');
}

async function loginMsa() {
  msaError.value = '';
  deviceCode.value = null;
  loggingIn.value = true;
  unsub = window.launcherApi.accounts.onDeviceCode((dc) => {
    deviceCode.value = dc;
  });
  try {
    await window.launcherApi.accounts.loginMsa();
    await store.refreshAccounts();
    toast.success('Signed in with Microsoft');
  } catch (e: any) {
    msaError.value = e.message ?? String(e);
    toast.error('Sign-in failed', msaError.value);
  } finally {
    loggingIn.value = false;
    deviceCode.value = null;
    unsub?.();
  }
}

function openLink() {
  if (deviceCode.value) window.open(deviceCode.value.verificationUri, '_blank');
}

onUnmounted(() => unsub?.());
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(4, 4, 8, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(3px);
}
.dialog {
  width: 420px;
  max-height: 74vh;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dialog-header h3 {
  margin: 0;
}
.close-btn {
  background: none;
  border: none;
  color: var(--text-dim);
  font-size: 14px;
}
.close-btn:hover {
  color: var(--text);
}
.account-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
  max-height: 240px;
}
.account-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: border-color var(--dur-fast) ease, background var(--dur-fast) ease;
}
.account-row:hover {
  background: var(--bg-panel-light);
}
.account-row.active {
  border-color: var(--accent);
  background: rgba(var(--accent-rgb), 0.1);
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #0b0b12;
}
.account-info {
  flex: 1 1 auto;
}
.account-name {
  font-weight: 600;
  font-size: 13px;
}
.account-type {
  font-size: 11px;
  color: var(--text-dim);
}
.remove-btn {
  background: none;
  border: 1px solid var(--border);
  border-radius: 6px;
  color: var(--text-dim);
  font-size: 11px;
  padding: 4px 8px;
}
.remove-btn:hover {
  color: var(--error);
  border-color: var(--error);
}
.empty {
  color: var(--text-dim);
  font-size: 12px;
  text-align: center;
  padding: 12px 0;
}
.add-row {
  display: flex;
  gap: 8px;
}
.add-row input {
  flex: 1 1 auto;
}
.msa-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.mini-spinner {
  display: inline-block;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.25);
  border-top-color: var(--text);
  animation: spin 0.7s linear infinite;
}
.device-code {
  font-size: 12px;
  color: var(--text-dim);
  line-height: 1.6;
}
.msa-error {
  font-size: 12px;
  color: var(--error);
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity var(--dur-fast) ease;
}
.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: opacity var(--dur-fast) ease;
}
.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
}
.list-enter-active,
.list-leave-active {
  transition: opacity var(--dur-fast) ease, transform var(--dur-fast) ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-6px);
}
</style>

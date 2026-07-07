<template>
  <div class="overlay" @click.self="$emit('close')">
    <div class="dialog glass-panel">
      <div class="dialog-header">
        <h3>Accounts</h3>
        <button class="close-btn" @click="$emit('close')">&#10005;</button>
      </div>

      <div class="account-list">
        <div
          v-for="acc in store.accounts"
          :key="acc.uuid"
          class="account-row"
          :class="{ active: acc.uuid === store.activeAccount?.uuid }"
          @click="select(acc.uuid)"
        >
          <div class="avatar">{{ acc.name.slice(0, 1).toUpperCase() }}</div>
          <div class="account-info">
            <div class="account-name">{{ acc.name }}</div>
            <div class="account-type">{{ acc.type === 'msa' ? 'Microsoft' : 'Offline' }}</div>
          </div>
          <button class="remove-btn" @click.stop="remove(acc.uuid)">Remove</button>
        </div>
        <div v-if="store.accounts.length === 0" class="empty">No accounts yet.</div>
      </div>

      <div class="add-row">
        <input v-model="offlineName" placeholder="Offline username" @keyup.enter="addOffline" />
        <button class="secondary" @click="addOffline">+ Add Offline</button>
      </div>

      <div class="msa-row">
        <button class="secondary" :disabled="loggingIn" @click="loginMsa">
          {{ loggingIn ? 'Waiting for sign-in...' : 'Sign in with Microsoft' }}
        </button>
        <div v-if="deviceCode" class="device-code">
          Go to <a href="#" @click.prevent="openLink">{{ deviceCode.verificationUri }}</a> and enter code:
          <strong>{{ deviceCode.userCode }}</strong>
        </div>
        <div v-if="msaError" class="msa-error">{{ msaError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue';
import { useLauncherStore } from '../stores/launcher';

defineEmits<{ close: [] }>();
const store = useLauncherStore();

const offlineName = ref('');
const loggingIn = ref(false);
const deviceCode = ref<{ userCode: string; verificationUri: string } | null>(null);
const msaError = ref('');

let unsub: (() => void) | null = null;

async function addOffline() {
  const name = offlineName.value.trim();
  if (!name) return;
  await window.launcherApi.accounts.addOffline(name);
  offlineName.value = '';
  await store.refreshAccounts();
}

async function select(uuid: string) {
  await window.launcherApi.accounts.setActive(uuid);
  await store.refreshAccounts();
}

async function remove(uuid: string) {
  await window.launcherApi.accounts.remove(uuid);
  await store.refreshAccounts();
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
  } catch (e: any) {
    msaError.value = e.message ?? String(e);
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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}
.dialog {
  width: 420px;
  max-height: 70vh;
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
}
.account-row:hover {
  background: var(--bg-panel-light);
}
.account-row.active {
  border-color: var(--accent);
  background: rgba(139, 92, 246, 0.1);
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
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
button.secondary {
  background: var(--bg-panel-light);
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text);
  padding: 8px 12px;
  font-size: 13px;
}
button.secondary:hover {
  border-color: var(--accent);
}
.msa-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
</style>

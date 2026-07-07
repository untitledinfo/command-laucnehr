import { reactive } from 'vue';

export type ToastKind = 'success' | 'error' | 'info' | 'warning';

export interface ToastItem {
  id: number;
  kind: ToastKind;
  title: string;
  message?: string;
  duration: number;
}

let counter = 0;

export const toastState = reactive<{ items: ToastItem[] }>({ items: [] });

function push(kind: ToastKind, title: string, message?: string, duration = 4200) {
  const id = ++counter;
  toastState.items.push({ id, kind, title, message, duration });
  if (duration > 0) {
    setTimeout(() => dismiss(id), duration);
  }
  return id;
}

export function dismiss(id: number) {
  const idx = toastState.items.findIndex((t) => t.id === id);
  if (idx !== -1) toastState.items.splice(idx, 1);
}

export const toast = {
  success: (title: string, message?: string) => push('success', title, message),
  error: (title: string, message?: string) => push('error', title, message, 6000),
  info: (title: string, message?: string) => push('info', title, message),
  warning: (title: string, message?: string) => push('warning', title, message, 5000)
};

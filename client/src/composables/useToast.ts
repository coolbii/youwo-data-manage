import { reactive } from 'vue';

export type ToastVariant = 'info' | 'success' | 'error' | 'warning';

export interface ToastMessage {
  id: number;
  variant: ToastVariant;
  message: string;
  duration: number;
}

export type ToastOptions = {
  variant?: ToastVariant;
  message: string;
  duration?: number;
};

const toasts = reactive<ToastMessage[]>([]);
let nextId = 0;

function show(options: ToastOptions | string): number {
  const resolved: ToastOptions =
    typeof options === 'string' ? { message: options } : options;

  const toast: ToastMessage = {
    id: nextId++,
    variant: resolved.variant ?? 'info',
    message: resolved.message,
    duration: resolved.duration ?? 4000,
  };

  toasts.push(toast);

  if (toast.duration > 0) {
    setTimeout(() => dismiss(toast.id), toast.duration);
  }

  return toast.id;
}

function dismiss(id: number) {
  const idx = toasts.findIndex((t) => t.id === id);
  if (idx !== -1) toasts.splice(idx, 1);
}

export function useToast() {
  return { toasts, show, dismiss };
}

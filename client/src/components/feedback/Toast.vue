<script setup lang="ts">
import type { ToastVariant } from '../../composables/useToast';

defineProps<{
  variant: ToastVariant;
  message: string;
}>();

const emit = defineEmits<{
  (e: 'dismiss'): void;
}>();
</script>

<template>
  <div
    :class="['toast', `toast--${variant}`]"
    role="alert"
  >
    <span
      class="toast__icon"
      aria-hidden="true"
    >
      <!-- info -->
      <svg
        v-if="variant === 'info'"
        viewBox="0 0 24 24"
        fill="none"
      >
        <circle
          cx="12"
          cy="12"
          r="9"
          stroke="currentColor"
          stroke-width="2"
        />
        <path
          d="M12 8h.01"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
        <path
          d="M12 12v4"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
      </svg>
      <!-- success -->
      <svg
        v-else-if="variant === 'success'"
        viewBox="0 0 24 24"
        fill="none"
      >
        <circle
          cx="12"
          cy="12"
          r="9"
          stroke="currentColor"
          stroke-width="2"
        />
        <path
          d="M9 12l2 2 4-4"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
      <!-- error -->
      <svg
        v-else-if="variant === 'error'"
        viewBox="0 0 24 24"
        fill="none"
      >
        <circle
          cx="12"
          cy="12"
          r="9"
          stroke="currentColor"
          stroke-width="2"
        />
        <path
          d="M15 9l-6 6"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
        <path
          d="M9 9l6 6"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
      </svg>
      <!-- warning -->
      <svg
        v-else
        viewBox="0 0 24 24"
        fill="none"
      >
        <path
          d="M12 3L2 21h20L12 3z"
          stroke="currentColor"
          stroke-width="2"
          stroke-linejoin="round"
        />
        <path
          d="M12 10v4"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
        <path
          d="M12 17h.01"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
      </svg>
    </span>

    <span class="toast__message">{{ message }}</span>

    <button
      type="button"
      class="toast__dismiss"
      aria-label="Dismiss"
      @click="emit('dismiss')"
    >
      <svg
        viewBox="0 0 24 24"
        fill="none"
        aria-hidden="true"
      >
        <path
          d="M6 6L18 18"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
        <path
          d="M18 6L6 18"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
      </svg>
    </button>
  </div>
</template>

<style scoped>
.toast {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  min-width: 280px;
  max-width: 420px;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-sm);
  border: 1px solid;
  box-shadow: var(--shadow-lg);
  font-size: var(--font-size-md);
  line-height: 1.4;
}

.toast--info {
  background: var(--color-info-soft);
  border-color: var(--color-info-border);
  color: var(--color-accent-strong);
}

.toast--success {
  background: var(--color-success-soft);
  border-color: var(--color-success-border);
  color: #15803d;
}

.toast--error {
  background: var(--color-danger-soft);
  border-color: var(--color-danger-border);
  color: var(--color-danger);
}

.toast--warning {
  background: var(--color-warning-soft);
  border-color: var(--color-warning-border);
  color: #b45309;
}

.toast__icon {
  display: inline-flex;
  flex: 0 0 auto;
  width: 18px;
  height: 18px;
  margin-top: 1px;
}

.toast__icon svg {
  width: 100%;
  height: 100%;
}

.toast__message {
  flex: 1 1 auto;
}

.toast__dismiss {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 18px;
  height: 18px;
  margin-top: 1px;
  border: 0;
  background: transparent;
  color: inherit;
  opacity: 0.6;
  cursor: pointer;
  padding: 0;
  transition: opacity var(--duration-fast) ease;
}

.toast__dismiss:hover {
  opacity: 1;
}

.toast__dismiss svg {
  width: 100%;
  height: 100%;
}
</style>

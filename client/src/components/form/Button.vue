<script setup lang="ts">
import { computed } from 'vue';

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
type ButtonSize = 'sm' | 'md' | 'lg';

const props = withDefaults(
  defineProps<{
    variant?: ButtonVariant;
    size?: ButtonSize;
    type?: 'button' | 'submit' | 'reset';
    loading?: boolean;
    disabled?: boolean;
    block?: boolean;
  }>(),
  {
    variant: 'primary',
    size: 'md',
    type: 'button',
    loading: false,
    disabled: false,
    block: false,
  },
);

const isDisabled = computed(() => props.disabled || props.loading);
</script>

<template>
  <button
    :type="type"
    :disabled="isDisabled"
    :class="['btn', `btn--${variant}`, `btn--${size}`, { 'btn--block': block }]"
  >
    <span
      v-if="loading"
      class="btn__spinner"
      aria-hidden="true"
    />
    <slot />
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  cursor: pointer;
  font-family: inherit;
  font-weight: 500;
  line-height: 1;
  transition:
    background-color var(--duration-fast) ease,
    border-color var(--duration-fast) ease,
    color var(--duration-fast) ease;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn--sm {
  min-height: var(--control-h-sm);
  padding: 0 var(--space-3);
  font-size: var(--font-size-sm);
}

.btn--md {
  min-height: var(--control-h-md);
  padding: 0 var(--space-4);
  font-size: var(--font-size-md);
}

.btn--lg {
  min-height: var(--control-h-md);
  padding: 0 var(--space-5);
  font-size: var(--font-size-lg);
}

.btn--block {
  width: 100%;
}

.btn--primary {
  color: #ffffff;
  background: var(--color-text);
  border-color: var(--color-text);
}

.btn--primary:hover:enabled {
  background: var(--color-text-secondary);
  border-color: var(--color-text-secondary);
}

.btn--secondary {
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border-color: var(--color-border);
}

.btn--secondary:hover:enabled {
  border-color: var(--color-border-strong);
  color: var(--color-text);
}

.btn--ghost {
  color: var(--color-text-secondary);
  background: transparent;
  border-color: var(--color-border);
}

.btn--ghost:hover:enabled {
  color: var(--color-text);
  background: var(--color-surface-soft);
}

.btn--danger {
  color: #ffffff;
  background: var(--color-danger);
  border-color: var(--color-danger);
}

.btn--danger:hover:enabled {
  background: var(--color-danger-strong);
  border-color: var(--color-danger-strong);
}

.btn__spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgb(255 255 255 / 35%);
  border-top-color: currentcolor;
  border-radius: 999px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>

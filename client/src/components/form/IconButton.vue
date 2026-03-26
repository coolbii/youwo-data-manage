<script setup lang="ts">
type IconButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
type IconButtonSize = 'sm' | 'md' | 'lg';

withDefaults(
  defineProps<{
    ariaLabel: string;
    variant?: IconButtonVariant;
    size?: IconButtonSize;
    disabled?: boolean;
    loading?: boolean;
    type?: 'button' | 'submit' | 'reset';
  }>(),
  {
    variant: 'secondary',
    size: 'md',
    disabled: false,
    loading: false,
    type: 'button',
  }
);
</script>

<template>
  <button
    :type="type"
    :aria-label="ariaLabel"
    :disabled="disabled || loading"
    :class="['icon-btn', `icon-btn--${variant}`, `icon-btn--${size}`]"
  >
    <span
      v-if="loading"
      class="icon-btn__spinner"
      aria-hidden="true"
    />
    <slot v-else />
  </button>
</template>

<style scoped>
.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  cursor: pointer;
  font-family: inherit;
  transition:
    background-color var(--duration-fast) ease,
    border-color var(--duration-fast) ease,
    color var(--duration-fast) ease;
}

.icon-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.icon-btn--sm {
  width: var(--control-h-sm);
  height: var(--control-h-sm);
}

.icon-btn--md {
  width: var(--space-10);
  height: var(--space-10);
}

.icon-btn--lg {
  width: var(--control-h-md);
  height: var(--control-h-md);
}

.icon-btn--primary {
  color: #ffffff;
  background: var(--color-text);
  border-color: var(--color-text);
}

.icon-btn--primary:hover:enabled {
  background: var(--color-text-secondary);
  border-color: var(--color-text-secondary);
}

.icon-btn--secondary {
  color: var(--color-muted);
  border-color: var(--color-border);
  background: var(--color-surface);
}

.icon-btn--secondary:hover:enabled {
  color: var(--color-text-secondary);
  border-color: var(--color-border-strong);
}

.icon-btn--ghost {
  color: var(--color-muted);
  background: transparent;
}

.icon-btn--ghost:hover:enabled {
  background: var(--color-surface-soft);
  color: var(--color-text);
}

.icon-btn--danger {
  color: var(--color-danger);
  border-color: var(--color-danger-border);
  background: var(--color-danger-soft);
}

.icon-btn--danger:hover:enabled {
  color: var(--color-danger-strong);
  background: var(--color-danger-hover);
}

.icon-btn__spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgb(55 65 81 / 25%);
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

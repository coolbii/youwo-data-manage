<script setup lang="ts">
type LoadingSize = 'sm' | 'md' | 'lg';

withDefaults(
  defineProps<{
    size?: LoadingSize;
    label?: string;
  }>(),
  {
    size: 'md',
    label: '',
  },
);
</script>

<template>
  <div
    :class="['loading', `loading--${size}`]"
    role="status"
  >
    <span
      class="loading__spinner"
      aria-hidden="true"
    />
    <span
      v-if="label"
      class="loading__label"
    >
      {{ label }}
    </span>
    <span class="sr-only">{{ label || 'Loading…' }}</span>
  </div>
</template>

<style scoped>
.loading {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-muted);
}

.loading__spinner {
  flex: 0 0 auto;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-accent);
  border-radius: 999px;
  animation: spin 0.8s linear infinite;
}

.loading--sm .loading__spinner {
  width: 14px;
  height: 14px;
}

.loading--md .loading__spinner {
  width: 20px;
  height: 20px;
}

.loading--lg .loading__spinner {
  width: 28px;
  height: 28px;
  border-width: 3px;
}

.loading__label {
  font-size: var(--font-size-sm);
}

.loading--lg .loading__label {
  font-size: var(--font-size-md);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>

<script setup lang="ts">
type BadgeVariant = 'default' | 'info' | 'success' | 'warning' | 'danger';

withDefaults(
  defineProps<{
    variant?: BadgeVariant;
    label: string;
  }>(),
  {
    variant: 'default',
  },
);
</script>

<template>
  <span :class="['badge', `badge--${variant}`]">
    {{ label }}
  </span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px var(--space-2);
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 500;
  line-height: 1.4;
  white-space: nowrap;
}

.badge--default {
  background: var(--color-surface-soft);
  color: var(--color-muted);
}

.badge--info {
  background: var(--color-info-soft);
  color: var(--color-accent-strong);
}

.badge--success {
  background: var(--color-success-soft);
  color: #15803d;
}

.badge--warning {
  background: var(--color-warning-soft);
  color: #b45309;
}

.badge--danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}
</style>

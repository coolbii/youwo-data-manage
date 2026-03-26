<script setup lang="ts">
withDefaults(
  defineProps<{
    clickable?: boolean;
  }>(),
  {
    clickable: false,
  },
);

defineEmits<{
  (e: 'click'): void;
}>();
</script>

<template>
  <div
    :class="['card-row', { 'card-row--clickable': clickable }]"
    :role="clickable ? 'button' : undefined"
    :tabindex="clickable ? 0 : undefined"
    @click="clickable && $emit('click')"
    @keydown.enter="clickable && $emit('click')"
    @keydown.space.prevent="clickable && $emit('click')"
  >
    <div class="card-row__body">
      <div class="card-row__primary">
        <slot name="primary" />
      </div>
      <div
        v-if="$slots.secondary"
        class="card-row__secondary"
      >
        <slot name="secondary" />
      </div>
      <div
        v-if="$slots.meta"
        class="card-row__meta"
      >
        <slot name="meta" />
      </div>
    </div>
    <div
      v-if="$slots.actions"
      class="card-row__actions"
      @click.stop
    >
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.card-row {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  transition: border-color var(--duration-fast) ease, box-shadow var(--duration-fast) ease;
}

.card-row--clickable {
  cursor: pointer;
}

.card-row--clickable:hover {
  border-color: var(--color-border-strong);
  box-shadow: var(--shadow-sm);
}

.card-row--clickable:focus-visible {
  outline: 2px solid var(--color-accent);
  outline-offset: 2px;
}

.card-row__body {
  flex: 1 1 auto;
  min-width: 0;
}

.card-row__primary {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--color-text);
  line-height: 1.4;
}

.card-row__secondary {
  margin-top: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.4;
}

.card-row__meta {
  margin-top: var(--space-2);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--color-muted);
}

.card-row__actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
}
</style>

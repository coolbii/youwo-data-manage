<script setup lang="ts">
export type MenuItem = {
  key: string;
  label: string;
  disabled?: boolean;
  danger?: boolean;
};

defineProps<{
  items: MenuItem[];
}>();

const emit = defineEmits<{
  (e: 'select', key: string): void;
}>();
</script>

<template>
  <ul
    class="menu"
    role="menu"
  >
    <li
      v-for="item in items"
      :key="item.key"
      role="none"
    >
      <button
        type="button"
        role="menuitem"
        :class="['menu__item', { 'menu__item--danger': item.danger }]"
        :disabled="item.disabled"
        @click="emit('select', item.key)"
      >
        {{ item.label }}
      </button>
    </li>
  </ul>
</template>

<style scoped>
.menu {
  list-style: none;
  margin: 0;
  padding: var(--space-1) 0;
}

.menu__item {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: var(--control-h-sm);
  padding: var(--space-2) var(--space-4);
  border: 0;
  background: transparent;
  color: var(--color-text-secondary);
  font-family: inherit;
  font-size: var(--font-size-md);
  text-align: start;
  cursor: pointer;
  transition: background-color var(--duration-fast) ease, color var(--duration-fast) ease;
}

.menu__item:hover:enabled {
  background: var(--color-surface-soft);
  color: var(--color-text);
}

.menu__item:disabled {
  color: var(--color-placeholder);
  cursor: not-allowed;
}

.menu__item--danger {
  color: var(--color-danger);
}

.menu__item--danger:hover:enabled {
  background: var(--color-danger-soft);
  color: var(--color-danger-strong);
}
</style>

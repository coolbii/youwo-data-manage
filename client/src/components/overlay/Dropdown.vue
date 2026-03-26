<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from 'vue';

type DropdownAlign = 'start' | 'end';

const props = withDefaults(
  defineProps<{
    align?: DropdownAlign;
    disabled?: boolean;
  }>(),
  {
    align: 'start',
    disabled: false,
  },
);

const open = ref(false);
const root = ref<HTMLElement | null>(null);

function toggle() {
  if (props.disabled) return;
  open.value = !open.value;
}

function close() {
  open.value = false;
}

function onClickOutside(e: MouseEvent) {
  if (root.value && !root.value.contains(e.target as Node)) {
    close();
  }
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    close();
  }
}

watch(open, (val) => {
  if (val) {
    document.addEventListener('mousedown', onClickOutside);
    document.addEventListener('keydown', onKeydown);
  } else {
    document.removeEventListener('mousedown', onClickOutside);
    document.removeEventListener('keydown', onKeydown);
  }
});

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onClickOutside);
  document.removeEventListener('keydown', onKeydown);
});

defineExpose({ open, close, toggle });
</script>

<template>
  <div
    ref="root"
    class="dropdown"
  >
    <div
      class="dropdown__trigger"
      @click="toggle"
    >
      <slot
        name="trigger"
        :open="open"
      />
    </div>

    <Transition name="dropdown">
      <div
        v-if="open"
        :class="['dropdown__panel', `dropdown__panel--${align}`]"
      >
        <slot :close="close" />
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.dropdown {
  position: relative;
  display: inline-flex;
}

.dropdown__trigger {
  display: inline-flex;
}

.dropdown__panel {
  position: absolute;
  top: calc(100% + var(--space-1));
  z-index: var(--z-dropdown);
  min-width: 160px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-lg);
  transform-origin: top;
}

.dropdown__panel--start {
  left: 0;
}

.dropdown__panel--end {
  right: 0;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition:
    opacity var(--duration-fast) ease,
    transform var(--duration-fast) ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>

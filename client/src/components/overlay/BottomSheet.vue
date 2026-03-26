<script setup lang="ts">
import { ref, toRef, watch, onBeforeUnmount } from 'vue';
import { lockScroll, unlockScroll } from '../../composables/useScrollLock';
import { useFocusTrap } from '../../composables/useFocusTrap';

const props = withDefaults(
  defineProps<{
    open: boolean;
    title?: string;
    persistent?: boolean;
  }>(),
  {
    title: '',
    persistent: false,
  },
);

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const panelRef = ref<HTMLElement | null>(null);

useFocusTrap(panelRef, toRef(props, 'open'));

function onBackdropClick() {
  if (!props.persistent) emit('close');
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && !props.persistent) {
    emit('close');
  }
}

watch(
  () => props.open,
  (val) => {
    if (val) {
      lockScroll();
      document.addEventListener('keydown', onKeydown);
    } else {
      unlockScroll();
      document.removeEventListener('keydown', onKeydown);
    }
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  if (props.open) unlockScroll();
  document.removeEventListener('keydown', onKeydown);
});
</script>

<template>
  <Teleport to="body">
    <Transition name="sheet">
      <div
        v-if="open"
        class="sheet"
        @click.self="onBackdropClick"
      >
        <div
          ref="panelRef"
          class="sheet__panel"
          role="dialog"
          aria-modal="true"
          :aria-label="title || undefined"
        >
          <div class="sheet__handle-bar">
            <span class="sheet__handle" />
          </div>

          <div
            v-if="title || $slots.header"
            class="sheet__header"
          >
            <slot name="header">
              <h2 class="sheet__title">
                {{ title }}
              </h2>
            </slot>
          </div>

          <div class="sheet__body">
            <slot />
          </div>

          <div
            v-if="$slots.footer"
            class="sheet__footer"
          >
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.sheet {
  position: fixed;
  inset: 0;
  z-index: var(--z-modal);
  display: flex;
  align-items: flex-end;
  background: var(--color-backdrop);
}

.sheet__panel {
  width: 100%;
  max-height: 85vh;
  overflow-y: auto;
  background: var(--color-surface);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  box-shadow: var(--shadow-lg);
}

.sheet__handle-bar {
  display: flex;
  justify-content: center;
  padding: var(--space-3) 0 0;
}

.sheet__handle {
  width: 36px;
  height: 4px;
  border-radius: 2px;
  background: var(--color-border-strong);
}

.sheet__header {
  display: flex;
  align-items: center;
  padding: var(--space-4) var(--space-5);
}

.sheet__title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.3;
}

.sheet__body {
  padding: 0 var(--space-5) var(--space-5);
}

.sheet__footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--color-border);
}

.sheet-enter-active,
.sheet-leave-active {
  transition: opacity var(--duration-fast) ease;
}

.sheet-enter-active .sheet__panel,
.sheet-leave-active .sheet__panel {
  transition: transform 200ms ease;
}

.sheet-enter-from,
.sheet-leave-to {
  opacity: 0;
}

.sheet-enter-from .sheet__panel {
  transform: translateY(100%);
}

.sheet-leave-to .sheet__panel {
  transform: translateY(100%);
}
</style>

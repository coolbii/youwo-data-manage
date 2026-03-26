<script setup lang="ts">
import { ref, toRef, watch, onBeforeUnmount } from 'vue';
import { lockScroll, unlockScroll } from '../../composables/useScrollLock';
import { useFocusTrap } from '../../composables/useFocusTrap';

type ModalSize = 'sm' | 'md' | 'lg';

const props = withDefaults(
  defineProps<{
    open: boolean;
    title?: string;
    size?: ModalSize;
    persistent?: boolean;
  }>(),
  {
    title: '',
    size: 'md',
    persistent: false,
  },
);

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const dialogRef = ref<HTMLElement | null>(null);

useFocusTrap(dialogRef, toRef(props, 'open'));

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
    <Transition name="modal">
      <div
        v-if="open"
        class="modal"
        @click.self="onBackdropClick"
      >
        <div
          ref="dialogRef"
          :class="['modal__dialog', `modal__dialog--${size}`]"
          role="dialog"
          aria-modal="true"
          :aria-label="title || undefined"
        >
          <div
            v-if="title || $slots.header"
            class="modal__header"
          >
            <slot name="header">
              <h2 class="modal__title">
                {{ title }}
              </h2>
            </slot>

            <button
              type="button"
              class="modal__close"
              aria-label="Close"
              @click="emit('close')"
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

          <div class="modal__body">
            <slot />
          </div>

          <div
            v-if="$slots.footer"
            class="modal__footer"
          >
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal {
  position: fixed;
  inset: 0;
  z-index: var(--z-modal);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
  background: var(--color-backdrop);
}

.modal__dialog {
  width: 100%;
  max-height: calc(100vh - var(--space-8));
  overflow-y: auto;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
}

.modal__dialog--sm {
  max-width: 380px;
}

.modal__dialog--md {
  max-width: 520px;
}

.modal__dialog--lg {
  max-width: 720px;
}

.modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--color-border);
}

.modal__title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.3;
}

.modal__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-muted);
  cursor: pointer;
  transition: background-color var(--duration-fast) ease, color var(--duration-fast) ease;
}

.modal__close:hover {
  background: var(--color-surface-soft);
  color: var(--color-text);
}

.modal__close svg {
  width: 16px;
  height: 16px;
}

.modal__body {
  padding: var(--space-6);
}

.modal__footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--color-border);
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity var(--duration-fast) ease;
}

.modal-enter-active .modal__dialog,
.modal-leave-active .modal__dialog {
  transition: transform var(--duration-fast) ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal__dialog {
  transform: scale(0.95) translateY(8px);
}

.modal-leave-to .modal__dialog {
  transform: scale(0.95) translateY(8px);
}

@media (max-width: 599px) {
  .modal {
    padding: var(--space-4);
    align-items: flex-end;
  }

  .modal__dialog {
    max-width: 100%;
    border-radius: var(--radius-md) var(--radius-md) 0 0;
  }

  .modal__header {
    padding: var(--space-4) var(--space-4);
  }

  .modal__body {
    padding: var(--space-4);
  }

  .modal__footer {
    padding: var(--space-3) var(--space-4);
  }
}
</style>

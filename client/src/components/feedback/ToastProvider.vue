<script setup lang="ts">
import { useToast } from '../../composables/useToast';
import Toast from './Toast.vue';

const { toasts, dismiss } = useToast();
</script>

<template>
  <Teleport to="body">
    <div
      v-if="toasts.length"
      class="toast-provider"
      aria-live="polite"
    >
      <TransitionGroup name="toast-item">
        <Toast
          v-for="t in toasts"
          :key="t.id"
          :variant="t.variant"
          :message="t.message"
          @dismiss="dismiss(t.id)"
        />
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-provider {
  position: fixed;
  top: var(--space-4);
  right: var(--space-4);
  z-index: var(--z-toast);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  pointer-events: none;
}

.toast-provider > :deep(*) {
  pointer-events: auto;
}

.toast-item-enter-active,
.toast-item-leave-active {
  transition:
    opacity var(--duration-fast) ease,
    transform var(--duration-fast) ease;
}

.toast-item-enter-from {
  opacity: 0;
  transform: translateX(16px);
}

.toast-item-leave-to {
  opacity: 0;
  transform: translateX(16px);
}

.toast-item-move {
  transition: transform 200ms ease;
}

@media (max-width: 599px) {
  .toast-provider {
    top: auto;
    bottom: var(--space-4);
    right: var(--space-4);
    left: var(--space-4);
  }

  .toast-provider :deep(.toast) {
    min-width: auto;
    max-width: none;
  }
}
</style>

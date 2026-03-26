<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import LoadingIndicator from '../feedback/LoadingIndicator.vue';

type FooterState = 'idle' | 'loading' | 'end' | 'error';

const props = withDefaults(
  defineProps<{
    state?: FooterState;
    endLabel?: string;
    errorLabel?: string;
  }>(),
  {
    state: 'idle',
    endLabel: 'No more results',
    errorLabel: 'Failed to load. Tap to retry.',
  },
);

const emit = defineEmits<{
  (e: 'load'): void;
}>();

const sentinel = ref<HTMLElement | null>(null);
let observer: IntersectionObserver | null = null;
let pending = false;

watch(
  () => props.state,
  () => {
    pending = false;
  },
);

function onIntersect(entries: IntersectionObserverEntry[]) {
  if (entries[0]?.isIntersecting && props.state === 'idle' && !pending) {
    pending = true;
    emit('load');
  }
}

onMounted(() => {
  if (!sentinel.value) return;
  observer = new IntersectionObserver(onIntersect, {
    rootMargin: '200px',
  });
  observer.observe(sentinel.value);
});

onBeforeUnmount(() => {
  observer?.disconnect();
});
</script>

<template>
  <div
    ref="sentinel"
    class="infinite-footer"
  >
    <LoadingIndicator
      v-if="state === 'loading'"
      size="sm"
      label="Loading…"
    />

    <p
      v-else-if="state === 'end'"
      class="infinite-footer__end"
    >
      {{ endLabel }}
    </p>

    <button
      v-else-if="state === 'error'"
      type="button"
      class="infinite-footer__retry"
      @click="emit('load')"
    >
      {{ errorLabel }}
    </button>

    <!-- idle: invisible sentinel, triggers via IntersectionObserver -->
  </div>
</template>

<style scoped>
.infinite-footer {
  display: flex;
  justify-content: center;
  padding: var(--space-6) var(--space-4);
  min-height: 48px;
}

.infinite-footer__end {
  color: var(--color-muted);
  font-size: var(--font-size-sm);
}

.infinite-footer__retry {
  border: 0;
  background: transparent;
  color: var(--color-danger);
  font-family: inherit;
  font-size: var(--font-size-sm);
  cursor: pointer;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-sm);
  transition: background-color var(--duration-fast) ease;
}

.infinite-footer__retry:hover {
  background: var(--color-danger-soft);
}
</style>

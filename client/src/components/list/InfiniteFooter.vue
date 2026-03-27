<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import LoadingIndicator from '../feedback/LoadingIndicator.vue';

type FooterState = 'idle' | 'loading' | 'end' | 'error';

const props = withDefaults(
  defineProps<{
    state?: FooterState;
    summary?: string;
    hint?: string;
    endLabel?: string;
    errorLabel?: string;
    scrollRoot?: HTMLElement | null;
  }>(),
  {
    state: 'idle',
    summary: '',
    hint: '',
    endLabel: 'No more results',
    errorLabel: 'Failed to load. Tap to retry.',
    scrollRoot: null,
  },
);

const emit = defineEmits<{
  (e: 'load'): void;
}>();

const sentinel = ref<HTMLElement | null>(null);
let observer: IntersectionObserver | null = null;
let pending = false;

function disconnectObserver() {
  observer?.disconnect();
  observer = null;
}

function setupObserver() {
  disconnectObserver();
  if (!sentinel.value) return;
  observer = new IntersectionObserver(onIntersect, {
    root: props.scrollRoot ?? null,
    rootMargin: '200px',
  });
  observer.observe(sentinel.value);
}

watch(
  () => props.state,
  (state) => {
    pending = false;
    if (state === 'idle') {
      requestAnimationFrame(() => {
        triggerLoadIfVisible();
      });
    }
  },
);

watch(
  () => props.scrollRoot,
  () => {
    setupObserver();
    requestAnimationFrame(() => {
      triggerLoadIfVisible();
    });
  },
);

function onIntersect(entries: IntersectionObserverEntry[]) {
  if (entries[0]?.isIntersecting && props.state === 'idle' && !pending) {
    pending = true;
    emit('load');
  }
}

function triggerLoadIfVisible() {
  if (!sentinel.value || props.state !== 'idle' || pending) return;
  const rect = sentinel.value.getBoundingClientRect();
  const rootBottom = props.scrollRoot
    ? props.scrollRoot.getBoundingClientRect().bottom
    : window.innerHeight;
  if (rect.top <= rootBottom + 200) {
    pending = true;
    emit('load');
  }
}

onMounted(() => {
  setupObserver();
  requestAnimationFrame(() => {
    triggerLoadIfVisible();
  });
});

onBeforeUnmount(() => {
  disconnectObserver();
});
</script>

<template>
  <div
    ref="sentinel"
    :class="[
      'infinite-footer',
      {
        'infinite-footer--with-summary': !!summary || !!hint,
      },
    ]"
  >
    <button
      v-if="state === 'error'"
      type="button"
      class="infinite-footer__retry"
      @click="emit('load')"
    >
      {{ errorLabel }}
    </button>

    <div
      v-else-if="summary || hint"
      class="infinite-footer__meta"
    >
      <p
        v-if="summary"
        class="infinite-footer__summary"
      >
        {{ summary }}
      </p>
      <p
        v-if="hint"
        class="infinite-footer__hint"
      >
        {{ hint }}
      </p>
    </div>

    <LoadingIndicator
      v-else-if="state === 'loading'"
      size="sm"
      label="Loading…"
    />

    <p
      v-else-if="state === 'end'"
      class="infinite-footer__end"
    >
      {{ endLabel }}
    </p>

    <!-- idle: invisible sentinel, triggers via IntersectionObserver -->
  </div>
</template>

<style scoped>
.infinite-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6) var(--space-4);
  min-height: 56px;
  overflow-anchor: none;
}

.infinite-footer--with-summary {
  justify-content: stretch;
}

.infinite-footer__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 20px;
}

.infinite-footer__summary {
  color: var(--color-placeholder);
  font-size: 0.8125rem;
}

.infinite-footer__hint {
  color: var(--color-placeholder);
  font-size: 0.8125rem;
  font-weight: 500;
  text-align: end;
}

.infinite-footer__end {
  color: var(--color-muted);
  font-size: var(--font-size-sm);
}

.infinite-footer :deep(.loading) {
  min-height: 20px;
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

@media (max-width: 599px) {
  .infinite-footer {
    padding-inline: var(--space-4);
  }

  .infinite-footer__summary,
  .infinite-footer__hint {
    font-size: 0.75rem;
  }
}
</style>

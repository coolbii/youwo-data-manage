import { computed, ref, type Ref } from 'vue';

type UseVirtualScrollOptions<T> = {
  items: Ref<T[]>;
  scrollEl: Ref<HTMLElement | null>;
  itemHeight: Ref<number>;
  anchorOffset?: Ref<number>;
  overscan?: number;
  initialVisibleCount?: number;
};

type UseVirtualScrollResult<T> = {
  visibleItems: Readonly<Ref<T[]>>;
  padTop: Readonly<Ref<number>>;
  padBottom: Readonly<Ref<number>>;
  startIndex: Readonly<Ref<number>>;
  endIndex: Readonly<Ref<number>>;
  updateMetrics: () => void;
  onScroll: () => void;
};

export function useVirtualScroll<T>(
  options: UseVirtualScrollOptions<T>,
): UseVirtualScrollResult<T> {
  const {
    items,
    scrollEl,
    itemHeight,
    anchorOffset = ref(0),
    overscan = 6,
    initialVisibleCount = 20,
  } = options;

  const scrollTop = ref(0);
  const viewportHeight = ref(0);
  let rafId: number | null = null;

  function updateMetrics() {
    if (!scrollEl.value) return;
    viewportHeight.value = scrollEl.value.clientHeight;
    scrollTop.value = scrollEl.value.scrollTop;
  }

  function onScroll() {
    if (rafId !== null) return;
    rafId = window.requestAnimationFrame(() => {
      updateMetrics();
      rafId = null;
    });
  }

  const visibleCount = computed(() => {
    if (viewportHeight.value <= 0) return initialVisibleCount;
    return Math.ceil(viewportHeight.value / itemHeight.value) + overscan * 2;
  });

  const startIndex = computed(() => {
    const total = items.value.length;
    if (total <= 0) return 0;
    const normalizedScrollTop = Math.max(0, scrollTop.value - anchorOffset.value);
    const start = Math.floor(normalizedScrollTop / itemHeight.value) - overscan;
    return Math.max(0, Math.min(start, total));
  });

  const endIndex = computed(() => {
    const total = items.value.length;
    if (total <= 0) return 0;
    return Math.min(total, startIndex.value + visibleCount.value);
  });

  const visibleItems = computed(() => items.value.slice(startIndex.value, endIndex.value));
  const padTop = computed(() => startIndex.value * itemHeight.value);
  const padBottom = computed(() =>
    Math.max(0, (items.value.length - endIndex.value) * itemHeight.value),
  );

  return {
    visibleItems,
    padTop,
    padBottom,
    startIndex,
    endIndex,
    updateMetrics,
    onScroll,
  };
}

import { type Ref, watch, onBeforeUnmount } from 'vue';

const FOCUSABLE =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

export function useFocusTrap(
  containerRef: Ref<HTMLElement | null>,
  active: Ref<boolean>,
) {
  let previouslyFocused: HTMLElement | null = null;

  function getFocusable(): HTMLElement[] {
    if (!containerRef.value) return [];
    return Array.from(containerRef.value.querySelectorAll<HTMLElement>(FOCUSABLE));
  }

  function onKeydown(e: KeyboardEvent) {
    if (e.key !== 'Tab' || !containerRef.value) return;

    const focusable = getFocusable();
    if (focusable.length === 0) {
      e.preventDefault();
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    if (e.shiftKey && document.activeElement === first) {
      e.preventDefault();
      last.focus();
    } else if (!e.shiftKey && document.activeElement === last) {
      e.preventDefault();
      first.focus();
    }
  }

  function activate() {
    previouslyFocused = document.activeElement as HTMLElement | null;
    document.addEventListener('keydown', onKeydown);

    // Defer focus so the element is rendered
    requestAnimationFrame(() => {
      if (!containerRef.value) return;
      const focusable = getFocusable();
      if (focusable.length > 0) {
        focusable[0].focus();
      } else {
        containerRef.value.focus();
      }
    });
  }

  function deactivate() {
    document.removeEventListener('keydown', onKeydown);
    previouslyFocused?.focus();
    previouslyFocused = null;
  }

  watch(
    active,
    (val) => {
      if (val) activate();
      else deactivate();
    },
    { immediate: true },
  );

  onBeforeUnmount(() => {
    deactivate();
  });
}

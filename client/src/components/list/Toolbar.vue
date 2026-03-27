<script setup lang="ts">
withDefaults(
  defineProps<{
    sticky?: boolean;
  }>(),
  {
    sticky: false,
  },
);
</script>

<template>
  <div :class="['toolbar', { 'toolbar--sticky': sticky }]">
    <div class="toolbar__start">
      <slot name="start" />
    </div>
    <div
      v-if="$slots.end"
      class="toolbar__end"
    >
      <slot name="end" />
    </div>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding-block: var(--space-3);
}

.toolbar--sticky {
  position: sticky;
  top: var(--app-bar-h);
  z-index: 10;
  background: var(--color-bg);
}

.toolbar__start {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1 1 auto;
  min-width: 0;
}

.toolbar__end {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex: 0 0 auto;
}

@media (max-width: 599px) {
  .toolbar {
    flex-wrap: wrap;
  }

  .toolbar__start {
    width: 100%;
  }
}
</style>

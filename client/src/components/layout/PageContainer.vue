<script setup lang="ts">
import { computed } from 'vue';

type LayoutMode = 'responsive' | '375' | '600' | '1024' | '1440';

const props = withDefaults(
  defineProps<{
    mode?: LayoutMode;
  }>(),
  {
    mode: 'responsive',
  }
);

const modeClass = computed(() => `layout-wrapper--${props.mode}`);
</script>

<template>
  <div class="layout-shell">
    <div :class="['layout-wrapper', modeClass]">
      <slot />
    </div>
  </div>
</template>

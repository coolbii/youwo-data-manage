<script setup lang="ts">
import { RouterView } from 'vue-router';
import UiPageContainer from '../components/layout/UiPageContainer.vue';
</script>

<template>
  <UiPageContainer mode="responsive">
    <div class="app-root">
      <main>
        <RouterView />
      </main>
    </div>
  </UiPageContainer>
</template>

<style scoped>
.app-root {
  padding-block: var(--space-6);
}
</style>

<script setup lang="ts">
import { RouterView } from 'vue-router';
import PageContainer from '../components/layout/PageContainer.vue';
</script>

<template>
  <PageContainer mode="responsive">
    <div class="app-root">
      <main>
        <RouterView />
      </main>
    </div>
  </PageContainer>
</template>

<style scoped>
.app-root {
  padding-block: var(--space-6);
}
</style>

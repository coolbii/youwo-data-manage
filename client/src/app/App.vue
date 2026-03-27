<script setup lang="ts">
import { RouterView } from 'vue-router';
import AppBar from '../components/layout/AppBar.vue';
import PageContainer from '../components/layout/PageContainer.vue';
import ToastProvider from '../components/feedback/ToastProvider.vue';
</script>

<template>
  <div class="app-shell">
    <AppBar />

    <PageContainer mode="responsive">
      <main class="app-main">
        <RouterView />
      </main>
    </PageContainer>
  </div>

  <ToastProvider />
</template>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-main {
  padding-block: var(--space-6);
  flex: 1 1 auto;
}

@media (max-width: 599px) {
  .app-main {
    padding-block: var(--space-4);
  }
}
</style>

<script setup lang="ts">
defineProps<{
  message?: string;
}>();
</script>

<template>
  <p
    v-if="message"
    class="inline-error"
    role="alert"
  >
    {{ message }}
  </p>
</template>

<style scoped>
.inline-error {
  margin-top: var(--space-2);
  color: #dc2626;
  font-size: 0.8125rem;
  line-height: 1.4;
}
</style>

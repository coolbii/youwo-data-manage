<script setup lang="ts">
defineProps<{
  title: string;
  description?: string;
  icon?: 'search' | 'people';
}>();
</script>

<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <!-- Search no-result icon -->
      <svg
        v-if="icon === 'search'"
        width="48"
        height="48"
        viewBox="0 0 48 48"
        fill="none"
      >
        <rect
          x="4"
          y="4"
          width="40"
          height="40"
          rx="20"
          fill="var(--color-surface-soft)"
        />
        <circle
          cx="22"
          cy="22"
          r="7"
          stroke="var(--color-muted)"
          stroke-width="2"
        />
        <path
          d="M27 27L32 32"
          stroke="var(--color-muted)"
          stroke-width="2"
          stroke-linecap="round"
        />
        <path
          d="M20 20L24 24M24 20L20 24"
          stroke="var(--color-muted)"
          stroke-width="1.5"
          stroke-linecap="round"
        />
      </svg>

      <!-- People directory icon -->
      <svg
        v-else-if="icon === 'people'"
        width="48"
        height="48"
        viewBox="0 0 48 48"
        fill="none"
      >
        <rect
          x="4"
          y="4"
          width="40"
          height="40"
          rx="20"
          fill="var(--color-accent-soft)"
        />
        <circle
          cx="24"
          cy="20"
          r="4"
          stroke="var(--color-accent)"
          stroke-width="2"
        />
        <path
          d="M17 31C17 27.134 20.134 24 24 24C27.866 24 31 27.134 31 31"
          stroke="var(--color-accent)"
          stroke-width="2"
          stroke-linecap="round"
        />
      </svg>
    </div>

    <h3 class="empty-state__title">
      {{ title }}
    </h3>

    <p
      v-if="description"
      class="empty-state__description"
    >
      {{ description }}
    </p>

    <div
      v-if="$slots.action"
      class="empty-state__action"
    >
      <slot name="action" />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-12) var(--space-4);
  text-align: center;
}

.empty-state__icon {
  margin-bottom: var(--space-4);
}

.empty-state__title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}

.empty-state__description {
  margin: var(--space-2) 0 0;
  font-size: var(--font-size-md);
  color: var(--color-muted);
  max-width: 320px;
}

.empty-state__action {
  margin-top: var(--space-5);
}
</style>

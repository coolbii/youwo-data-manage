<script setup lang="ts">
defineProps<{
  title?: string;
}>();
</script>

<template>
  <header class="app-bar">
    <div class="app-bar__inner">
      <div class="app-bar__start">
        <svg
          class="app-bar__logo"
          viewBox="0 0 28 28"
          fill="none"
          aria-hidden="true"
        >
          <rect
            width="28"
            height="28"
            rx="6"
            fill="var(--color-accent)"
          />
          <text
            x="14"
            y="19"
            text-anchor="middle"
            font-size="14"
            font-weight="700"
            fill="#fff"
          >
            Y
          </text>
        </svg>
        <span class="app-bar__brand"> Youwo </span>
      </div>

      <div
        v-if="$slots.default"
        class="app-bar__end"
      >
        <slot />
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-bar {
  position: sticky;
  top: 0;
  z-index: var(--z-app-bar);
  height: var(--app-bar-h);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.app-bar__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: var(--bp-1440);
  margin-inline: auto;
  padding-inline: var(--gutter-mobile);
}

.app-bar__start {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.app-bar__logo {
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
}

.app-bar__brand {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: -0.01em;
}

.app-bar__end {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

@media (min-width: 600px) {
  .app-bar__inner {
    padding-inline: var(--gutter-tablet);
  }
}

@media (min-width: 1024px) {
  .app-bar__inner {
    padding-inline: var(--gutter-laptop);
  }

  .app-bar__logo {
    width: 28px;
    height: 28px;
  }
}

@media (min-width: 1440px) {
  .app-bar__inner {
    padding-inline: var(--gutter-desktop);
  }
}

@media (max-width: 599px) {
  .app-bar__logo {
    width: 20px;
    height: 20px;
  }

  .app-bar__brand {
    font-size: 0.9375rem;
  }
}
</style>

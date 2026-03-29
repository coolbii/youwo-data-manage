<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import IconButton from '../form/IconButton.vue';
import Button from '../form/Button.vue';
import Dropdown from '../overlay/Dropdown.vue';
import Menu, { type MenuItem } from '../overlay/Menu.vue';
import BottomSheet from '../overlay/BottomSheet.vue';
import { useAuthSession } from '../../composables/useAuthSession';
import { useToast } from '../../composables/useToast';
import { useLogoutMutation } from '../../graphql/generated';

defineProps<{
  title?: string;
}>();

const route = useRoute();
const router = useRouter();
const { show } = useToast();
const { user, isAuthenticated, clearAuthSession } = useAuthSession();
const { mutate: logoutMutate, loading: isLoggingOut } = useLogoutMutation();

const isMobileViewport = ref(false);
const mobileSheetOpen = ref(false);

const authMenuItems: MenuItem[] = [
  {
    key: 'logout',
    label: 'Logout',
    danger: true,
  },
];

const showAuthActions = computed(
  () => isAuthenticated.value && route.name !== 'login',
);

function updateViewportMode() {
  if (typeof window === 'undefined') return;
  isMobileViewport.value = window.innerWidth < 600;
}

onMounted(() => {
  updateViewportMode();
  window.addEventListener('resize', updateViewportMode);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportMode);
});

async function logout() {
  if (isLoggingOut.value) return;

  try {
    await logoutMutate();
  } catch (error) {
    console.error(error);
    show({
      variant: 'warning',
      message: 'Logout request failed. Session was still cleared locally.',
    });
  }

  mobileSheetOpen.value = false;
  clearAuthSession();
  await router.replace({ name: 'login' });
}

async function onMenuSelect(key: string) {
  if (key === 'logout') {
    await logout();
  }
}
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
        v-if="$slots.default || showAuthActions"
        class="app-bar__end"
      >
        <slot />

        <template v-if="showAuthActions && !isMobileViewport">
          <Dropdown align="end">
            <template #trigger>
              <IconButton
                aria-label="Open account menu"
                size="sm"
              >
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  aria-hidden="true"
                >
                  <circle
                    cx="12"
                    cy="8"
                    r="3.5"
                    stroke="currentColor"
                    stroke-width="1.8"
                  />
                  <path
                    d="M4.5 20c0-3.7 3.36-6 7.5-6s7.5 2.3 7.5 6"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                  />
                </svg>
              </IconButton>
            </template>

            <template #default="{ close }">
              <div class="app-bar__menu-panel">
                <div class="app-bar__menu-user">
                  {{ user?.email }}
                </div>
                <Menu
                  :items="authMenuItems"
                  @select="(key) => { close(); onMenuSelect(key); }"
                />
              </div>
            </template>
          </Dropdown>
        </template>

        <template v-if="showAuthActions && isMobileViewport">
          <IconButton
            aria-label="Open account actions"
            size="sm"
            @click="mobileSheetOpen = true"
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              aria-hidden="true"
            >
              <circle
                cx="12"
                cy="8"
                r="3.5"
                stroke="currentColor"
                stroke-width="1.8"
              />
              <path
                d="M4.5 20c0-3.7 3.36-6 7.5-6s7.5 2.3 7.5 6"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
              />
            </svg>
          </IconButton>

          <BottomSheet
            :open="mobileSheetOpen"
            title="Account"
            @close="mobileSheetOpen = false"
          >
            <div class="app-bar__sheet-content">
              <p class="app-bar__sheet-email">
                {{ user?.email }}
              </p>
              <Button
                variant="danger"
                block
                :loading="isLoggingOut"
                @click="logout"
              >
                Logout
              </Button>
            </div>
          </BottomSheet>
        </template>
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

.app-bar__end :deep(.icon-btn svg) {
  width: 16px;
  height: 16px;
}

.app-bar__menu-panel {
  min-width: 220px;
}

.app-bar__menu-user {
  padding: var(--space-3) var(--space-4) var(--space-2);
  color: var(--color-muted);
  font-size: var(--font-size-sm);
  border-bottom: 1px solid var(--color-border);
}

.app-bar__sheet-content {
  display: grid;
  gap: var(--space-4);
  padding-top: var(--space-2);
}

.app-bar__sheet-email {
  color: var(--color-muted);
  font-size: var(--font-size-sm);
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

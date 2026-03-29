import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createMemoryHistory, createRouter } from 'vue-router';
import { flushPromises, mount } from '@vue/test-utils';
import { ref } from 'vue';
import AppBar from './AppBar.vue';

const {
  clearAuthSession,
  logoutMutate,
  toastShow,
  isAuthenticated,
  user,
} = vi.hoisted(() => ({
  clearAuthSession: vi.fn(),
  logoutMutate: vi.fn(),
  toastShow: vi.fn(),
  isAuthenticated: { value: true },
  user: { value: {
    id: 'u-1',
    email: 'demo@youwo.local',
  } },
}));

vi.mock('../../composables/useAuthSession', () => ({
  useAuthSession: () => ({
    user,
    isAuthenticated,
    clearAuthSession,
  }),
}));

vi.mock('../../graphql/generated', () => ({
  useLogoutMutation: () => ({
    mutate: logoutMutate,
    loading: ref(false),
  }),
}));

vi.mock('../../composables/useToast', () => ({
  useToast: () => ({
    show: toastShow,
    dismiss: vi.fn(),
    toasts: [],
  }),
}));

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/login',
        name: 'login',
        component: { template: '<div>login</div>' },
      },
      {
        path: '/',
        name: 'people',
        component: { template: '<div>people</div>' },
      },
    ],
  });
}

function setViewport(width: number) {
  Object.defineProperty(window, 'innerWidth', {
    configurable: true,
    value: width,
  });
}

describe('AppBar', () => {
  beforeEach(() => {
    clearAuthSession.mockReset();
    logoutMutate.mockReset();
    toastShow.mockReset();
    logoutMutate.mockResolvedValue({ data: { logout: true } });
    user.value = {
      id: 'u-1',
      email: 'demo@youwo.local',
    };
    isAuthenticated.value = true;
  });

  it('desktop menu allows logout', async () => {
    setViewport(1280);

    const router = createTestRouter();
    await router.push('/');
    await router.isReady();

    const wrapper = mount(AppBar, {
      global: {
        plugins: [router],
        stubs: {
          teleport: true,
        },
      },
    });

    await wrapper.get('button[aria-label="Open account menu"]').trigger('click');
    await wrapper.get('button[role="menuitem"]').trigger('click');
    await flushPromises();

    expect(logoutMutate).toHaveBeenCalledTimes(1);
    expect(clearAuthSession).toHaveBeenCalledTimes(1);
    expect(router.currentRoute.value.name).toBe('login');
  });

  it('mobile sheet allows logout', async () => {
    setViewport(375);

    const router = createTestRouter();
    await router.push('/');
    await router.isReady();

    const wrapper = mount(AppBar, {
      global: {
        plugins: [router],
        stubs: {
          teleport: true,
        },
      },
    });

    window.dispatchEvent(new Event('resize'));
    await flushPromises();

    await wrapper.get('button[aria-label="Open account actions"]').trigger('click');
    await wrapper.get('button.btn--danger').trigger('click');
    await flushPromises();

    expect(logoutMutate).toHaveBeenCalledTimes(1);
    expect(clearAuthSession).toHaveBeenCalledTimes(1);
    expect(router.currentRoute.value.name).toBe('login');
  });
});

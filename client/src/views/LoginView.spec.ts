import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createMemoryHistory, createRouter } from 'vue-router';
import { flushPromises, mount } from '@vue/test-utils';
import { ref } from 'vue';
import LoginView from './LoginView.vue';

const { loginMutate, setAuthSession } = vi.hoisted(() => ({
  loginMutate: vi.fn(),
  setAuthSession: vi.fn(),
}));

vi.mock('../graphql/generated', () => ({
  useLoginMutation: () => ({
    mutate: loginMutate,
    loading: ref(false),
  }),
}));

vi.mock('../composables/useAuthSession', () => ({
  setAuthSession,
}));

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/login',
        name: 'login',
        component: LoginView,
      },
      {
        path: '/',
        name: 'people',
        component: { template: '<div>people</div>' },
      },
      {
        path: '/people/new',
        name: 'person-create',
        component: { template: '<div>create</div>' },
      },
    ],
  });
}

describe('LoginView', () => {
  beforeEach(() => {
    loginMutate.mockReset();
    setAuthSession.mockReset();
  });

  it('submits login and redirects to redirect query path', async () => {
    loginMutate.mockResolvedValue({
      data: {
        login: {
          user: {
            id: 'u-1',
            email: 'demo@youwo.local',
            createdAt: '2026-01-01T00:00:00Z',
            updatedAt: '2026-01-01T00:00:00Z',
          },
        },
      },
    });

    const router = createTestRouter();
    await router.push('/login?redirect=/people/new');
    await router.isReady();

    const wrapper = mount(LoginView, {
      global: {
        plugins: [router],
      },
    });

    await wrapper.get('input[type="email"]').setValue('  DEMO@youwo.local ');
    await wrapper.get('input[type="password"]').setValue('youwo1234');

    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(loginMutate).toHaveBeenCalledTimes(1);
    expect(loginMutate).toHaveBeenCalledWith({
      email: 'demo@youwo.local',
      password: 'youwo1234',
    });
    expect(setAuthSession).toHaveBeenCalledWith({
      user: {
        id: 'u-1',
        email: 'demo@youwo.local',
        createdAt: '2026-01-01T00:00:00Z',
        updatedAt: '2026-01-01T00:00:00Z',
      },
    });
    expect(router.currentRoute.value.fullPath).toBe('/people/new');
  });

  it('shows backend error when login fails', async () => {
    loginMutate.mockRejectedValue(new Error('Invalid email or password'));

    const router = createTestRouter();
    await router.push('/login');
    await router.isReady();

    const wrapper = mount(LoginView, {
      global: {
        plugins: [router],
      },
    });

    await wrapper.get('input[type="email"]').setValue('bad@youwo.local');
    await wrapper.get('input[type="password"]').setValue('wrong');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.text()).toContain('Invalid email or password');
  });
});

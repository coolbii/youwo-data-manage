import { beforeEach, describe, expect, it, vi } from 'vitest';

describe('useAuthSession', () => {
  beforeEach(() => {
    vi.resetModules();
    vi.restoreAllMocks();
  });

  it('stores and clears authenticated user in memory', async () => {
    const authSession = await import('./useAuthSession');

    authSession.setAuthSession({
      user: {
        id: 'u-1',
        email: 'demo@youwo.local',
      },
    });

    const state = authSession.useAuthSession();
    expect(state.isAuthenticated.value).toBe(true);
    expect(state.user.value?.email).toBe('demo@youwo.local');

    authSession.clearAuthSession();

    expect(state.isAuthenticated.value).toBe(false);
    expect(state.user.value).toBeNull();
  });

  it('restores user from backend me query', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        json: async () => ({
          data: {
            me: {
              id: 'u-2',
              email: 'restored@youwo.local',
              createdAt: '2026-01-01T00:00:00Z',
              updatedAt: '2026-01-02T00:00:00Z',
            },
          },
        }),
      }),
    );

    const authSession = await import('./useAuthSession');
    const restored = await authSession.restoreAuthSession();

    const state = authSession.useAuthSession();
    expect(restored).toBe(true);
    expect(state.isAuthenticated.value).toBe(true);
    expect(state.user.value?.email).toBe('restored@youwo.local');
  });

  it('clears local user when me query response is invalid', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        json: async () => ({
          data: {
            me: null,
          },
        }),
      }),
    );

    const authSession = await import('./useAuthSession');
    authSession.setAuthSession({
      user: { id: 'u-3', email: 'stale@youwo.local' },
    });

    const restored = await authSession.restoreAuthSession();

    const state = authSession.useAuthSession();
    expect(restored).toBe(false);
    expect(state.isAuthenticated.value).toBe(false);
    expect(state.user.value).toBeNull();
  });
});

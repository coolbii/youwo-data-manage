import { computed, readonly, ref } from 'vue';

type AuthUser = {
  id: string;
  email: string;
  createdAt?: string;
  updatedAt?: string;
};

type AuthSessionInput = {
  user: AuthUser;
};

const user = ref<AuthUser | null>(null);

let pendingRestore: Promise<boolean> | null = null;
let restoreAttempted = false;

const ME_QUERY = /* GraphQL */ `
  query RestoreAuthSession {
    me {
      id
      email
      createdAt
      updatedAt
    }
  }
`;

export function initializeAuthSession() {
  // No-op. User state is hydrated lazily via restoreAuthSession().
}

export function setAuthSession(session: AuthSessionInput) {
  user.value = session.user;
}

export function clearAuthSession() {
  user.value = null;
  restoreAttempted = false;
}

export function restoreAuthSession(): Promise<boolean> {
  if (user.value) return Promise.resolve(true);
  if (restoreAttempted && !pendingRestore) return Promise.resolve(false);
  if (pendingRestore) return pendingRestore;
  pendingRestore = doRestoreAuthSession().finally(() => {
    restoreAttempted = true;
    pendingRestore = null;
  });
  return pendingRestore;
}

export function useAuthSession() {
  const isAuthenticated = computed(() => !!user.value);

  return { user: readonly(user), isAuthenticated, setAuthSession, clearAuthSession };
}

async function doRestoreAuthSession(): Promise<boolean> {
  try {
    const response = await fetch('/api/graphql', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ query: ME_QUERY }),
    });

    const json = await response.json();
    const me = json?.data?.me;
    if (!me?.id || !me?.email) {
      clearAuthSession();
      return false;
    }

    setAuthSession({
      user: {
        id: me.id,
        email: me.email,
        createdAt: me.createdAt ?? undefined,
        updatedAt: me.updatedAt ?? undefined,
      },
    });
    return true;
  } catch {
    clearAuthSession();
    return false;
  }
}

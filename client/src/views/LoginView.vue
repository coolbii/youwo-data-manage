<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import TextField from '../components/form/TextField.vue';
import PasswordField from '../components/form/PasswordField.vue';
import Button from '../components/form/Button.vue';
import InlineError from '../components/form/InlineError.vue';
import { useLoginMutation } from '../graphql/generated';
import { setAuthSession } from '../composables/useAuthSession';

const route = useRoute();
const router = useRouter();

const email = ref('');
const password = ref('');
const submitError = ref('');

const canSubmit = computed(
  () => email.value.trim().length > 0 && password.value.trim().length > 0,
);

const { mutate: loginMutate, loading } = useLoginMutation();

function resolveRedirectPath(): string {
  const redirect = route.query.redirect;
  if (typeof redirect === 'string' && redirect.startsWith('/')) {
    return redirect;
  }
  return '/';
}

async function submit() {
  submitError.value = '';

  const normalizedEmail = email.value.trim().toLowerCase();
  const normalizedPassword = password.value;

  if (!normalizedEmail || !normalizedPassword) {
    submitError.value = 'Email and password are required.';
    return;
  }

  try {
    const response = await loginMutate({
      email: normalizedEmail,
      password: normalizedPassword,
    });

    const session = response?.data?.login;
    if (!session?.user) {
      throw new Error('Login response is incomplete.');
    }

    const { user: sessionUser } = session;
    if (!sessionUser.id || !sessionUser.email) {
      throw new Error('Login response is incomplete.');
    }

    setAuthSession({
      user: {
        id: sessionUser.id,
        email: sessionUser.email,
        createdAt: sessionUser.createdAt ?? undefined,
        updatedAt: sessionUser.updatedAt ?? undefined,
      },
    });

    await router.replace(resolveRedirectPath());
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : 'Login failed. Please try again.';
  }
}
</script>

<template>
  <section class="login-page">
    <div class="login-card surface-card">
      <header class="login-header">
        <h1 class="login-title">
          Sign in
        </h1>
        <p class="login-subtitle">
          Use your account to continue.
        </p>
      </header>

      <form
        class="login-form"
        @submit.prevent="submit"
      >
        <TextField
          v-model="email"
          label="Email"
          type="email"
          autocomplete="email"
          required
        />

        <PasswordField
          v-model="password"
          label="Password"
          autocomplete="current-password"
          required
        />

        <InlineError :message="submitError" />

        <Button
          type="submit"
          variant="primary"
          block
          :loading="loading"
          :disabled="!canSubmit"
        >
          Sign in
        </Button>
      </form>
    </div>
  </section>
</template>

<style scoped>
.login-page {
  min-height: calc(100vh - var(--app-bar-h) - var(--space-12));
  display: grid;
  place-items: center;
  padding: var(--space-6) 0;
}

.login-card {
  width: 100%;
  max-width: 440px;
  padding: var(--space-6);
}

.login-header {
  margin-bottom: var(--space-5);
}

.login-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: var(--space-2);
}

.login-subtitle {
  color: var(--color-muted);
  font-size: var(--font-size-md);
}

.login-form {
  display: grid;
  gap: var(--space-4);
}

@media (max-width: 599px) {
  .login-page {
    min-height: calc(100vh - var(--app-bar-h) - var(--space-8));
    align-items: start;
  }

  .login-card {
    max-width: none;
    padding: var(--space-5);
  }
}
</style>

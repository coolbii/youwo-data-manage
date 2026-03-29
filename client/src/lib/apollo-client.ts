import { ApolloClient, ApolloLink, HttpLink, InMemoryCache } from '@apollo/client/core';
import { onError } from '@apollo/client/link/error';
import { clearAuthSession } from '../composables/useAuthSession';
import { getCookieValue } from './cookies';

function resolveGraphqlEndpoint(): string {
  return '/api/graphql';
}

const AUTH_ERROR_MESSAGES = new Set([
  'Authentication is required',
  'Session token is invalid',
  'Token is invalid or expired',
]);
const CSRF_COOKIE_NAME = import.meta.env.VITE_AUTH_CSRF_COOKIE_NAME || 'youwo.csrf';

function isAuthError(message: string): boolean {
  if (!message) return false;
  const normalized = message.trim();
  return [...AUTH_ERROR_MESSAGES].some(
    (expected) =>
      normalized === expected ||
      normalized.endsWith(`: ${expected}`) ||
      normalized.includes(expected),
  );
}

function isAuthNetworkError(networkError: unknown): boolean {
  if (!networkError || typeof networkError !== 'object') return false;
  const statusCode = (networkError as { statusCode?: unknown }).statusCode;
  return statusCode === 401 || statusCode === 403;
}

function isAuthOperation(name: string | undefined): boolean {
  return name === 'Login' || name === 'Me';
}

const errorLink = onError(({ graphQLErrors, networkError, operation }) => {
  const hasAuthGraphQLError = graphQLErrors?.some((e) => isAuthError(e.message));
  if (!hasAuthGraphQLError && !isAuthNetworkError(networkError)) return;
  clearAuthSession();

  if (isAuthOperation(operation.operationName) || typeof window === 'undefined') {
    return;
  }

  if (window.location.pathname === '/login') {
    return;
  }

  const redirectTo = `${window.location.pathname}${window.location.search}${window.location.hash}`;
  const loginUrl = `/login?redirect=${encodeURIComponent(redirectTo)}`;
  window.location.replace(loginUrl);
});

const csrfLink = new ApolloLink((operation, forward) => {
  const csrfToken = getCookieValue(CSRF_COOKIE_NAME);
  operation.setContext(({ headers = {} }: { headers: Record<string, string> }) => ({
    headers: csrfToken
      ? {
          ...headers,
          'X-CSRF-Token': csrfToken,
        }
      : headers,
  }));
  return forward(operation);
});

const httpLink = new HttpLink({
  uri: resolveGraphqlEndpoint(),
  credentials: 'include',
});

export const apolloClient = new ApolloClient({
  cache: new InMemoryCache(),
  link: ApolloLink.from([errorLink, csrfLink, httpLink]),
});

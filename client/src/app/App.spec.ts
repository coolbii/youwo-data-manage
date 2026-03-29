import { describe, it, expect } from 'vitest'
import router from '../router';
import { mount } from '@vue/test-utils'
import { DefaultApolloClient } from '@vue/apollo-composable';
import { apolloClient } from '../lib/apollo-client';
import App from './App.vue';

if (!window.matchMedia) {
  window.matchMedia = () =>
    ({
      matches: false,
      media: '',
      onchange: null,
      addListener: () => undefined,
      removeListener: () => undefined,
      addEventListener: () => undefined,
      removeEventListener: () => undefined,
      dispatchEvent: () => false,
    }) as MediaQueryList;
}

describe('App', () => {
  it('renders properly', async () => {
    await router.push('/login');
    await router.isReady();

    const wrapper = mount(App, {
      global: {
        plugins: [router],
        provide: {
          [DefaultApolloClient as symbol]: apolloClient,
        },
      },
    });
    await router.isReady();
    expect(wrapper.find('.app-shell').exists()).toBe(true)
  })
});

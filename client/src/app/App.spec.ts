import { describe, it, expect } from 'vitest'
import router from '../router';
import { mount } from '@vue/test-utils'
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
    const wrapper = mount(App, { global: { plugins: [router] }})
    await router.isReady();
    expect(wrapper.find('.app-root').exists()).toBe(true)
  })
});

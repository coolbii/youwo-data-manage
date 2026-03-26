import './styles.css';
import router from './router';
import { createApp } from 'vue';
import PrimeVue from 'primevue/config';
import App from './app/App.vue';

const app = createApp(App);
app.use(PrimeVue, {
  unstyled: true,
  ripple: true,
});
app.use(router);
app.mount('#root');

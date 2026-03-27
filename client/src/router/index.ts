import { createRouter, createWebHistory } from 'vue-router';
import PeopleView from '../views/PeopleView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'people',
      component: PeopleView,
    },
    {
      path: '/people/new',
      name: 'person-create',
      component: () => import('../views/PersonFormView.vue'),
    },
    {
      path: '/people/:id/edit',
      name: 'person-edit',
      component: () => import('../views/PersonFormView.vue'),
    },
    {
      path: '/preview',
      name: 'preview',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
  ],
});

export default router;

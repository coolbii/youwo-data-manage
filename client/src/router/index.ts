import { createRouter, createWebHistory } from 'vue-router';
import PeopleView from '../views/PeopleView.vue';
import {
  initializeAuthSession,
  restoreAuthSession,
  useAuthSession,
} from '../composables/useAuthSession';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/',
      name: 'people',
      component: PeopleView,
      meta: { requiresAuth: true },
    },
    {
      path: '/people/new',
      name: 'person-create',
      component: () => import('../views/PersonFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/people/:id/edit',
      name: 'person-edit',
      component: () => import('../views/PersonFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/preview',
      name: 'preview',
      component: () => import('../views/HomeView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: { requiresAuth: true },
    },
  ],
});

initializeAuthSession();
const { isAuthenticated } = useAuthSession();

router.beforeEach(async (to) => {
  if (!isAuthenticated.value && (to.meta.requiresAuth || to.name === 'login')) {
    await restoreAuthSession();
  }

  if (to.name === 'login' && isAuthenticated.value) {
    const redirectPath =
      typeof to.query.redirect === 'string' && to.query.redirect.startsWith('/')
        ? to.query.redirect
        : '/';
    return redirectPath;
  }

  if (to.meta.requiresAuth && !isAuthenticated.value) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    };
  }

  return true;
});

export default router;

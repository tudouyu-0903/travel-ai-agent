import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import LoveChatView from '../views/LoveChatView.vue';
import ManusChatView from '../views/ManusChatView.vue';
import AuthView from '../views/AuthView.vue';
import { isAuthenticated } from '../services/auth';

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/travel',
    name: 'travel-chat',
    component: LoveChatView
  },
  {
    path: '/love',
    redirect: '/travel'
  },
  {
    path: '/manus',
    name: 'manus-chat',
    component: ManusChatView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/login',
    name: 'login',
    component: AuthView
  },
  {
    path: '/register',
    name: 'register',
    component: AuthView
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isAuthenticated()) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    };
  }
  return true;
});

export default router;

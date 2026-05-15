import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import LoveChatView from '../views/LoveChatView.vue';
import ManusChatView from '../views/ManusChatView.vue';

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
    component: ManusChatView
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;

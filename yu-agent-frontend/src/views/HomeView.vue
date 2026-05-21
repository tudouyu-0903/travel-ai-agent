<template>
  <div class="home-page page-frame">
    <div class="hero-card">
      <div class="home-auth-actions">
        <template v-if="user">
          <span class="user-pill">{{ user.username || user.userAccount }}</span>
          <button class="ghost-button" type="button" @click="handleLogout">退出</button>
        </template>
        <template v-else>
          <router-link class="ghost-button" to="/login">登录</router-link>
          <router-link class="primary-link" to="/register">注册</router-link>
        </template>
      </div>

      <p class="eyebrow">YU AI TRAVEL AGENT</p>
      <h1>选择你的智能旅行助手入口</h1>
      <p class="hero-text">
        一个面向旅行规划的 AI 工作台。你可以直接进入智能旅行助手，获取路线、预算和行程建议；
        任务型 Agent 会调用搜索、网页抓取、文件和 PDF 工具，因此需要登录后使用。
      </p>

      <div class="app-grid">
        <router-link class="app-entry love-theme" to="/travel">
          <span class="app-badge">01</span>
          <h2>AI 智能旅行助手</h2>
          <p>适合路线规划、城市推荐、预算拆分、每日行程安排和旅行注意事项咨询。</p>
          <span class="app-link">进入应用</span>
        </router-link>

        <router-link class="app-entry manus-theme" :to="manusTarget">
          <span class="app-badge">02</span>
          <h2>旅行任务 Agent</h2>
          <p>适合联网检索、整理攻略、下载资料、生成旅行 PDF 或处理更开放的任务。</p>
          <span class="app-link">{{ user ? '进入应用' : '登录后使用' }}</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { getCurrentUser, logout } from '../services/auth';

const user = ref(getCurrentUser());
const manusTarget = computed(() => user.value ? '/manus' : { path: '/login', query: { redirect: '/manus' } });

function syncUser() {
  user.value = getCurrentUser();
}

async function handleLogout() {
  await logout();
  syncUser();
}

onMounted(() => {
  window.addEventListener('auth-changed', syncUser);
});

onBeforeUnmount(() => {
  window.removeEventListener('auth-changed', syncUser);
});
</script>

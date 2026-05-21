<template>
  <main class="auth-page page-frame">
    <section class="auth-panel">
      <router-link class="ghost-button auth-back" to="/">返回首页</router-link>
      <p class="eyebrow">YU AI TRAVEL AGENT</p>
      <h1>{{ isRegister ? '创建账号' : '欢迎回来' }}</h1>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <label>
          <span>账号</span>
          <input
            v-model.trim="form.userAccount"
            autocomplete="username"
            placeholder="至少 4 位，用于登录"
          />
        </label>

        <label v-if="isRegister">
          <span>昵称</span>
          <input
            v-model.trim="form.username"
            autocomplete="name"
            placeholder="展示给你的旅行助手"
          />
        </label>

        <label>
          <span>密码</span>
          <input
            v-model="form.password"
            type="password"
            :autocomplete="isRegister ? 'new-password' : 'current-password'"
            placeholder="至少 8 位"
          />
        </label>

        <template v-if="isRegister">
          <label>
            <span>手机号</span>
            <input v-model.trim="form.phone" autocomplete="tel" placeholder="可选" />
          </label>
          <label>
            <span>邮箱</span>
            <input v-model.trim="form.email" autocomplete="email" placeholder="可选" />
          </label>
        </template>

        <p v-if="errorMessage" class="auth-error">{{ errorMessage }}</p>

        <button class="send-button auth-submit" type="submit" :disabled="loading">
          {{ loading ? '处理中...' : isRegister ? '注册并登录' : '登录' }}
        </button>
      </form>

      <p class="auth-switch">
        {{ isRegister ? '已有账号？' : '还没有账号？' }}
        <router-link :to="isRegister ? '/login' : '/register'">
          {{ isRegister ? '去登录' : '去注册' }}
        </router-link>
      </p>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { login, register } from '../services/auth';

const route = useRoute();
const router = useRouter();
const isRegister = computed(() => route.name === 'register');
const loading = ref(false);
const errorMessage = ref('');

const form = reactive({
  userAccount: '',
  username: '',
  password: '',
  phone: '',
  email: ''
});

function validateForm() {
  if (!form.userAccount || form.userAccount.length < 4) {
    return '请输入至少 4 位账号';
  }
  if (isRegister.value && !form.username) {
    return '请输入昵称';
  }
  if (!form.password || form.password.length < 8) {
    return '请输入至少 8 位密码';
  }
  return '';
}

async function handleSubmit() {
  errorMessage.value = validateForm();
  if (errorMessage.value) {
    return;
  }

  loading.value = true;
  try {
    if (isRegister.value) {
      await register({ ...form });
    } else {
      await login({
        userAccount: form.userAccount,
        password: form.password
      });
    }
    router.push(route.query.redirect || '/');
  } catch (error) {
    errorMessage.value = error.response?.data?.message || error.message || '操作失败，请稍后再试';
  } finally {
    loading.value = false;
  }
}
</script>

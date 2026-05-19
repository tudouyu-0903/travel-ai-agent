<template>
  <section class="chat-page page-frame" :class="themeClass">
    <div class="chat-shell">
      <header class="chat-header">
        <div>
          <p class="eyebrow">{{ badge }}</p>
          <h1>{{ title }}</h1>
          <p class="chat-subtitle">{{ description }}</p>
        </div>
        <div class="chat-header-actions">
          <router-link class="ghost-button" to="/">返回首页</router-link>
          <div v-if="chatId" class="session-card">
            <span>会话 ID</span>
            <strong>{{ chatId }}</strong>
          </div>
        </div>
      </header>

      <main ref="messageContainerRef" class="messages-panel">
        <div v-if="messages.length === 0" class="empty-state">
          <p>输入你的旅行目标，AI 会实时生成回复。</p>
        </div>
        <ChatMessage
          v-for="message in messages"
          :key="message.id"
          :message="message"
          :ai-name="aiName"
        />
      </main>

      <form class="composer" @submit.prevent="handleSubmit">
        <div class="composer-main">
          <div class="composer-tools">
            <button
              class="tool-toggle"
              :class="{ active: webSearchEnabled }"
              type="button"
              :disabled="loading"
              :title="webSearchEnabled ? '已开启联网搜索' : '点击开启联网搜索'"
              @click="webSearchEnabled = !webSearchEnabled"
            >
              <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8" />
                <path d="M21 21l-4.35-4.35" />
              </svg>
              <span>联网搜索</span>
            </button>
          </div>
          <textarea
            v-model="input"
            class="composer-input"
            rows="3"
            :placeholder="placeholder"
            :disabled="loading"
            @keydown.enter.exact.prevent="handleSubmit"
          />
        </div>
        <button class="send-button" type="submit" :disabled="loading || !input.trim()">
          {{ loading ? '生成中...' : '发送消息' }}
        </button>
      </form>
    </div>
  </section>
</template>

<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import ChatMessage from './ChatMessage.vue';
import { scrollToBottom } from '../services/chat';
import { startSseRequest } from '../services/api';

const props = defineProps({
  badge: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  },
  aiName: {
    type: String,
    required: true
  },
  placeholder: {
    type: String,
    required: true
  },
  endpoint: {
    type: String,
    required: true
  },
  webSearchEndpoint: {
    type: String,
    default: '/ai/manus/chat'
  },
  chatId: {
    type: String,
    default: ''
  },
  includeChatId: {
    type: Boolean,
    default: false
  },
  themeClass: {
    type: String,
    default: ''
  }
});

const router = useRouter();
const messages = ref([]);
const input = ref('');
const loading = ref(false);
const webSearchEnabled = ref(false);
const messageContainerRef = ref(null);
let currentEventSource = null;

watch(
  messages,
  () => {
    scrollToBottom(messageContainerRef.value);
  },
  { deep: true }
);

function closeStream() {
  if (currentEventSource) {
    if (typeof currentEventSource.close === 'function') {
      currentEventSource.close();
    } else if (typeof currentEventSource.abort === 'function') {
      currentEventSource.abort();
    }
    currentEventSource = null;
  }
}

function appendMessage(message) {
  messages.value.push(message);
}

function createMessage(role, content, extra = {}) {
  return {
    id: `${role}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    role,
    content,
    ...extra
  };
}

async function handleAgentModeChunk(chunk) {
  const stepMatch = chunk.match(/^(Step\s*\d+\s*:)/i);

  if (stepMatch) {
    const content = chunk.replace(/^Step\s*\d+\s*:\s*/i, '');
    appendMessage(createMessage('assistant', content, {
      streaming: false,
      step: stepMatch[1]
    }));
  } else if (messages.value.length > 0) {
    const lastMsg = messages.value[messages.value.length - 1];
    if (lastMsg.role === 'assistant') {
      lastMsg.content += chunk;
      messages.value = [...messages.value];
    } else {
      appendMessage(createMessage('assistant', chunk, { streaming: false }));
    }
  } else {
    appendMessage(createMessage('assistant', chunk, { streaming: false }));
  }

  await nextTick();
}

async function handleStreamingModeChunk(chunk, aiMessage) {
  aiMessage.content += chunk;
  messages.value = messages.value.map(message =>
    message.id === aiMessage.id
      ? { ...message, content: aiMessage.content, streaming: aiMessage.streaming }
      : message
  );
  await nextTick();
}

function handleSubmit() {
  const text = input.value.trim();
  if (!text || loading.value) {
    return;
  }

  closeStream();
  loading.value = true;

  const useWebSearch = webSearchEnabled.value;
  appendMessage(createMessage('user', text, props.chatId ? { chatId: props.chatId } : {}));

  let aiMessage = null;
  if (props.includeChatId || useWebSearch) {
    aiMessage = createMessage('assistant', '', {
      streaming: true,
      chatId: props.chatId || undefined,
      webSearch: useWebSearch
    });
    appendMessage(aiMessage);
  }

  const requestPath = useWebSearch ? props.webSearchEndpoint : props.endpoint;
  const params = { message: useWebSearch ? `联网搜索：${text}` : text };
  if (!useWebSearch && props.includeChatId && props.chatId) {
    params.chatId = props.chatId;
  }

  currentEventSource = startSseRequest({
    path: requestPath,
    params,
    onMessage: async (chunk) => {
      if (chunk === '[DONE]') {
        loading.value = false;
        closeStream();

        if (messages.value.length > 0) {
          const lastMsg = messages.value[messages.value.length - 1];
          if (lastMsg.role === 'assistant') {
            lastMsg.streaming = false;
            messages.value = [...messages.value];
          }
        }

        await nextTick();
        return;
      }

      if (useWebSearch) {
        await handleStreamingModeChunk(chunk, aiMessage);
      } else if (!props.includeChatId) {
        await handleAgentModeChunk(chunk);
      } else {
        await handleStreamingModeChunk(chunk, aiMessage);
      }
    },
    onError: (error) => {
      loading.value = false;
      const unauthorized = error?.status === 401;
      const fallbackMessage = unauthorized ? '请先登录后再使用任务型访问。' : '连接已中断，请稍后重试。';

      if (!props.includeChatId && !useWebSearch) {
        appendMessage(createMessage('assistant', fallbackMessage));
      } else {
        aiMessage.streaming = false;
        if (!aiMessage.content) {
          aiMessage.content = fallbackMessage;
        }
        messages.value = messages.value.map(message =>
          message.id === aiMessage.id
            ? { ...message, content: aiMessage.content, streaming: aiMessage.streaming }
            : message
        );
      }

      if (unauthorized) {
        router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } });
      }
    },
    onOpen: () => {
      input.value = '';
    }
  });
}

onBeforeUnmount(() => {
  closeStream();
});
</script>

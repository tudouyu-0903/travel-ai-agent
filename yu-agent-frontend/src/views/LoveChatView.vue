<template>
  <ChatPage
    badge="TRAVEL APP"
    title="AI 智能旅游助手"
    description="根据预算、天数、出发地和偏好生成旅行方案，支持热门城市知识库、实时对话和行程建议。"
    ai-name="旅游助手"
    placeholder="例如：帮我规划 5 天成都和重庆自由行，预算 4000 元..."
    endpoint="/ai/travel_app/chat/sse"
    :chat-id="chatId"
    :include-chat-id="true"
    theme-class="love-room"
  />
</template>

<script setup>
import ChatPage from '../components/ChatPage.vue';
import { generateChatId } from '../services/chat';

const chatId = generateChatId();
</script>

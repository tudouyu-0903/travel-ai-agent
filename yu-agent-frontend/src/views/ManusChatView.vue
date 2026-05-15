<template>
  <ChatPage
    badge="TRAVEL AGENT"
    title="旅游任务 Agent"
    description="面向复杂旅行任务的实时 AI 工作台，可结合搜索、网页抓取、文件、下载和 PDF 工具完成执行型任务。"
    ai-name="旅游任务 Agent"
    placeholder="例如：联网搜索 2025 年日本关西亲子游攻略，并生成一份 PDF 行程..."
    endpoint="/ai/manus/chat"
    theme-class="manus-room"
  />
</template>

<script setup>
import ChatPage from '../components/ChatPage.vue';
</script>

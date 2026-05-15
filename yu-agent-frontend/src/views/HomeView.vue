<template>
  <div class="home-page page-frame">
    <div class="hero-card">
      <p class="eyebrow">YU AI TRAVEL AGENT</p>
      <h1>选择你的智能旅游助手入口</h1>
      <p class="hero-text">
        一个面向旅行规划的 AI 工作台。你可以进入智能旅游助手，基于 RAG 知识库获取 2025 热门城市建议；
        也可以进入任务型 Agent，让它调用搜索、网页抓取、文件和 PDF 工具完成更复杂的旅行任务。
      </p>

      <div class="app-grid">
        <router-link class="app-entry love-theme" to="/travel">
          <span class="app-badge">01</span>
          <h2>AI 智能旅游助手</h2>
          <p>适合路线规划、城市推荐、预算拆分、每日行程安排和旅行注意事项咨询。</p>
          <span class="app-link">进入应用</span>
        </router-link>

        <router-link class="app-entry manus-theme" to="/manus">
          <span class="app-badge">02</span>
          <h2>旅游任务 Agent</h2>
          <p>适合联网检索、整理攻略、下载资料、生成旅行 PDF 或处理更开放的任务。</p>
          <span class="app-link">进入应用</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

# Yu AI Travel Agent Frontend

Vue 3 + Vite 前端，用于访问后端智能旅游助手。

## 页面

- `/`：应用入口
- `/travel`：AI 智能旅游助手，使用 `/api/ai/travel_app/chat/sse`
- `/manus`：旅游任务 Agent，使用 `/api/ai/manus/chat`
- `/love`：旧入口兼容跳转到 `/travel`

## 本地运行

```bash
npm install
npm run dev
```

后端默认代理到 `http://localhost:8123`。

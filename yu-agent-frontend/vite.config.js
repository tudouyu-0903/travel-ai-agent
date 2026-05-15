import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  root: fileURLToPath(new URL('.', import.meta.url)),
  build: {
    rollupOptions: {
      input: 'index.html'
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8123',
        changeOrigin: true
      }
    }
  }
});

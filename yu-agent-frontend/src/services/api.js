import axios from 'axios';

export const API_BASE_URL = '/api';

export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('yu_travel_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export function createSseUrl(path, params = {}) {
  const normalizedPath = path.startsWith('/api') ? path : `${API_BASE_URL}${path}`;
  const url = new URL(normalizedPath, window.location.origin);

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, value);
    }
  });

  return url.toString();
}

export function startSseRequest({ path, params, onMessage, onError, onOpen }) {
  const normalizedPath = path.startsWith('/api') ? path : `${API_BASE_URL}${path}`;
  const fullUrl = createSseUrl(normalizedPath, params);

  console.log('🔧 ===== SSE 请求开始 =====');
  console.log('🔧 完整 URL:', fullUrl);
  console.log('🔧 请求参数:', params);

  const controller = new AbortController();

  const fetchOptions = {
    method: 'GET',
    headers: {
      'Accept': 'text/event-stream'
    },
    signal: controller.signal
  };

  const token = localStorage.getItem('yu_travel_token');
  if (token) {
    fetchOptions.headers.Authorization = `Bearer ${token}`;
  }

  (async () => {
    try {
      console.log('🌐 正在发起请求...');
      onOpen?.();

      const response = await fetch(fullUrl, fetchOptions);
      
      console.log('📥 收到响应:', {
        status: response.status,
        contentType: response.headers.get('content-type')
      });

      if (!response.ok) {
        let message = `HTTP error! status: ${response.status}`;
        try {
          const errorBody = await response.json();
          message = errorBody.message || message;
        } catch (ignore) {
          // ignore non-json errors
        }
        const error = new Error(message);
        error.status = response.status;
        throw error;
      }

      console.log('✅ 开始读取响应数据...');
      
      if (response.body && typeof response.body.getReader === 'function') {
        // 使用流式读取
        console.log('📖 使用 ReadableStream 读取...');
        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';
        let chunkCount = 0;

        while (true) {
          const { done, value } = await reader.read();

          if (done) {
            console.log('✅ 读取完成，总块数:', chunkCount);
            
            // 处理最后残留的 buffer
            if (buffer.trim()) {
              processSseLine(buffer, chunkCount, onMessage);
            }
            
            onMessage?.('[DONE]');
            break;
          }

          chunkCount++;
          const chunk = decoder.decode(value, { stream: true });
          buffer += chunk;
          
          // 按行分割处理 SSE 格式
          const lines = buffer.split('\n');
          buffer = lines.pop() || ''; // 保留最后一行（可能不完整）
          
          for (const line of lines) {
            processSseLine(line, chunkCount, onMessage);
          }
        }
      } else {
        // 如果不支持流式读取，直接读取文本
        console.log('📖 使用 text() 方法读取...');
        const text = await response.text();
        console.log('✅ 读取到的文本长度:', text.length);
        
        // 解析 SSE 格式
        const lines = text.split('\n');
        lines.forEach((line, index) => {
          processSseLine(line, index, onMessage);
        });
        
        onMessage?.('[DONE]');
      }
      
    } catch (error) {
      if (error.name === 'AbortError') {
        console.log('⚠️ 请求被取消');
      } else {
        console.error('❌ 请求失败:', error);
        onError?.(error);
      }
    } finally {
      console.log('🔧 ===== 请求处理完成 =====');
    }
  })();

  return {
    close: () => {
      console.log('🔌 关闭连接');
      controller.abort();
    }
  };
}

// SSE 行处理函数
function processSseLine(line, chunkCount, onMessage) {
  const trimmedLine = line.trim();
  
  // 跳过空行和注释行
  if (!trimmedLine || trimmedLine.startsWith(':')) {
    return;
  }
  
  // 处理 data: 前缀（标准 SSE 格式）
  if (trimmedLine.startsWith('data:')) {
    const data = trimmedLine.slice(5).trim();
    
    // 只在关键节点输出日志
    if (chunkCount <= 3 || chunkCount % 100 === 0 || data.includes('<think>') || data.includes('[DONE]')) {
      console.log('📨 data:', data.substring(0, 100));
    }
    
    if (data && data !== '[DONE]') {
      onMessage?.(data);
    } else if (data === '[DONE]') {
      console.log('✅ 接收到 [DONE] 标记');
      onMessage?.('[DONE]');
    }
  }
  // 处理 event: 和 id: （通常可以忽略）
  else if (trimmedLine.startsWith('event:') || trimmedLine.startsWith('id:')) {
    // 忽略
  }
  // 其他格式（纯文本，作为 fallback）
  else {
    if (chunkCount <= 3 || chunkCount % 100 === 0) {
      console.log('📨 纯文本:', trimmedLine.substring(0, 100));
    }
    onMessage?.(trimmedLine);
  }
}

<template>
  <div class="chat-message" :class="[message.role, { 'step-message': message.step }]">
    <div class="avatar">{{ message.role === 'user' ? '我' : 'AI' }}</div>
    <div class="bubble-wrap">
      <div class="meta">
        <span>{{ message.role === 'user' ? '你' : aiName }}</span>
        <span v-if="message.step" class="step-tag">{{ message.step }}</span>
        <span v-if="message.chatId" class="chat-id-tag">{{ message.chatId }}</span>
      </div>

      <div class="bubble markdown-body" :class="{ streaming: message.streaming }">
        <div v-if="message.role === 'user'" v-html="renderMarkdown(parsedContent.answer)"></div>
        <template v-else>
          <section
            v-if="parsedContent.thinking"
            class="thinking-panel"
            :class="{ collapsed: thinkingCollapsed }"
          >
            <button class="thinking-toggle" type="button" @click="thinkingCollapsed = !thinkingCollapsed">
              <span>思考</span>
              <span class="toggle-text">{{ thinkingCollapsed ? '展开' : '收起' }}</span>
            </button>
            <div
              v-show="!thinkingCollapsed"
              class="thinking-content"
              v-html="renderMarkdown(parsedContent.thinking)"
            ></div>
          </section>

          <section
            v-if="parsedContent.answer"
            class="answer-panel"
            v-html="renderMarkdown(parsedContent.answer)"
          ></section>

          <span v-if="!parsedContent.thinking && !parsedContent.answer" class="placeholder">生成中...</span>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  aiName: {
    type: String,
    default: '旅游助手'
  }
});

const thinkingCollapsed = ref(true);

const parsedContent = computed(() => {
  const content = props.message.content || '';
  if (!content.trim()) {
    return {
      thinking: '',
      answer: ''
    };
  }
  return splitAssistantContent(content);
});

function splitAssistantContent(content) {
  let text = content.trim();
  let thinking = '';

  text = text.replace(/<tools>[\s\S]*?<\/tools>/gi, '');
  text = text.replace(/```json[\s\S]*?```/gi, '');
  text = text.replace(/Ai Request:[\s\S]*?(?=\n{2,}|$)/gi, '');
  text = text.replace(/Ai Response:[\s\S]*?(?=\n{2,}|$)/gi, '');

  text = text.replace(/<think>([\s\S]*?)<\/think>/gi, (_, inner) => {
    thinking += `${inner.trim()}\n\n`;
    return '';
  });
  text = text.replace(/<thinks>([\s\S]*?)<\/thinks>/gi, (_, inner) => {
    thinking += `${inner.trim()}\n\n`;
    return '';
  });

  const openThinkMatch = text.match(/<think>|<thinks>/i);
  if (openThinkMatch) {
    const before = text.slice(0, openThinkMatch.index);
    const after = text.slice(openThinkMatch.index + openThinkMatch[0].length);
    const splitIndex = findAnswerStart(after);
    if (splitIndex >= 0) {
      thinking += `${after.slice(0, splitIndex).trim()}\n\n`;
      text = `${before}\n${after.slice(splitIndex)}`.trim();
    } else {
      thinking += after.trim();
      text = before.trim();
    }
  } else if (!hasAnswerShape(text) && text.length > 180) {
    const splitIndex = findAnswerStart(text);
    if (splitIndex > 0) {
      thinking += `${text.slice(0, splitIndex).trim()}\n\n`;
      text = text.slice(splitIndex).trim();
    }
  }

  return {
    thinking: thinking.trim(),
    answer: text.trim()
  };
}

function hasAnswerShape(text) {
  return /(?:为你|以下是|根据|旅行规划|行程|报告|预算|每日安排|行前准备|注意事项|Day\s*1|一、|1\.)/.test(text);
}

function findAnswerStart(text) {
  const markers = [
    /(?:^|\n)\s*(?:以下是|为你|我为你|根据|已为你|本次|旅行规划|行程|报告|总预算|预算概览|每日安排|行前准备|注意事项)/,
    /(?:^|\n)\s*(?:#{1,6}\s*)?(?:一、|二、|三、|四、|1[.、]|Day\s*1)/i,
    /(?:^|\n)\s*(?:\*\*)?(?:哈尔滨|重庆|成都|长春|沈阳|北京|上海|广州|深圳|西安|杭州).{0,30}(?:攻略|规划|报告|行程)/
  ];
  const indexes = markers
    .map(pattern => {
      const match = text.match(pattern);
      return match ? match.index + (match[0].startsWith('\n') ? 1 : 0) : -1;
    })
    .filter(index => index >= 0);
  return indexes.length ? Math.min(...indexes) : -1;
}

function escapeHtml(value) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function renderMarkdown(content) {
  const lines = normalizeMarkdown(content).split(/\r?\n/);
  const blocks = [];
  let listItems = [];

  function flushList() {
    if (listItems.length > 0) {
      blocks.push(`<ul>${listItems.map(item => `<li>${item}</li>`).join('')}</ul>`);
      listItems = [];
    }
  }

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line) {
      flushList();
      continue;
    }

    if (isTableStart(lines, i)) {
      flushList();
      const { html, nextIndex } = renderTable(lines, i);
      blocks.push(html);
      i = nextIndex;
      continue;
    }

    const heading = parseHeading(line);
    if (heading) {
      flushList();
      blocks.push(`<h3>${renderInlineMarkdown(heading)}</h3>`);
      continue;
    }

    const bullet = line.match(/^[-*]\s+(.+)$/);
    if (bullet) {
      listItems.push(renderInlineMarkdown(bullet[1]));
      continue;
    }

    flushList();
    blocks.push(`<p>${renderInlineMarkdown(line)}</p>`);
  }

  flushList();
  return blocks.join('');
}

function normalizeMarkdown(content) {
  return content
    .replace(/\r\n/g, '\n')
    .replace(/<tools>[\s\S]*?<\/tools>/gi, '')
    .replace(/#{1,6}\s*/g, '')
    .replace(/\|\|+/g, '|\n|')
    .replace(/(\|[ \t]*[-:]{3,}[ \t]*(?:\|[ \t]*[-:]{3,}[ \t]*)+\|?)/g, '\n$1\n')
    .replace(/\*\*(\s*[-*]\s+)/g, '\n$1')
    .replace(/\*\*(\s*\d+[.、]\s*)/g, '\n$1')
    .replace(/\*\*(\s*[一二三四五六七八九十]+[、.]\s*)/g, '\n$1')
    .replace(/([。；])\s+([-*]\s+)/g, '$1\n$2')
    .replace(/([。；])\s+(\d+[.、]\s*)/g, '$1\n$2')
    .replace(/\s+-\s+/g, '\n- ')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
}

function parseHeading(line) {
  const clean = line.replace(/\*\*/g, '').trim();
  if (/^[一二三四五六七八九十]+[、.]\s*/.test(clean)) {
    return clean;
  }
  if (/^Day\s*\d+[:：]/i.test(clean)) {
    return clean;
  }
  return null;
}

function isTableStart(lines, index) {
  const current = lines[index]?.trim();
  const next = lines[index + 1]?.trim();
  const third = lines[index + 2]?.trim();
  if (!isTableRow(current)) {
    return false;
  }
  if (/^\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?$/.test(next || '')) {
    return true;
  }
  return isTableRow(next) && isTableRow(third);
}

function isTableRow(line) {
  return !!line && line.includes('|') && line.split('|').filter(cell => cell.trim()).length >= 2;
}

function renderTable(lines, startIndex) {
  const header = parseTableCells(lines[startIndex]);
  const rows = [];
  let i = startIndex + 1;

  if (/^\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?$/.test(lines[i]?.trim() || '')) {
    i++;
  }

  while (i < lines.length && isTableRow(lines[i].trim())) {
    rows.push(parseTableCells(lines[i]));
    i++;
  }

  const headerHtml = header.map(cell => `<th>${renderInlineMarkdown(cell)}</th>`).join('');
  const rowsHtml = rows.map(row => {
    const cells = header.map((_, cellIndex) => row[cellIndex] || '');
    return `<tr>${cells.map(cell => `<td>${renderInlineMarkdown(cell)}</td>`).join('')}</tr>`;
  }).join('');

  return {
    html: `<div class="table-wrap"><table><thead><tr>${headerHtml}</tr></thead><tbody>${rowsHtml}</tbody></table></div>`,
    nextIndex: i - 1
  };
}

function parseTableCells(line) {
  return line
    .trim()
    .replace(/^\|/, '')
    .replace(/\|$/, '')
    .split('|')
    .map(cell => cell.trim())
    .filter(Boolean);
}

function renderInlineMarkdown(value) {
  const links = [];
  let text = value.replace(/\[([^\]]+)]\((https?:\/\/[^)\s]+)\)/g, (_, label, url) => {
    const token = `@@LINK_${links.length}@@`;
    links.push(createLink(label, url));
    return token;
  });

  text = text.replace(/(https?:\/\/[^\s<)]+\.pdf(?:\?[^\s<)]*)?)/gi, (url) => {
    const token = `@@LINK_${links.length}@@`;
    links.push(createLink('点击下载PDF报告', url));
    return token;
  });

  let html = escapeHtml(text);
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
  html = html.replace(/\*\*/g, '');
  html = html.replace(/`/g, '');

  links.forEach((link, index) => {
    html = html.replace(`@@LINK_${index}@@`, link);
  });

  return html;
}

function createLink(label, url) {
  const pdf = isPdfUrl(url);
  const text = pdf ? '点击下载PDF报告' : escapeHtml(label);
  const className = pdf ? 'travel-pdf-text-link' : 'markdown-link';
  const downloadAttr = pdf ? ' download' : '';
  return `<a class="${className}" href="${escapeHtml(url)}" target="_blank" rel="noopener noreferrer"${downloadAttr}>${text}</a>`;
}

function isPdfUrl(url) {
  return /\.pdf($|\?)/i.test(url);
}
</script>

<style scoped>
.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.step-message {
  animation: slideIn 0.4s ease-out;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

.step-tag {
  display: inline-block;
  background: linear-gradient(135deg, #2f9d7e 0%, #3f8dc2 100%);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  margin-left: 8px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2f9d7e 0%, #6ead62 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.chat-message.user .avatar {
  background: linear-gradient(135deg, #2f9d7e 0%, #3f8dc2 100%);
}

.bubble-wrap {
  flex: 1;
  min-width: 0;
}

.meta {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  color: #5f746a;
  gap: 8px;
}

.chat-id-tag {
  background: rgba(47, 143, 123, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-family: monospace;
}

.bubble {
  color: #17352d;
  padding: 0;
  border-radius: 12px;
  word-break: break-word;
  line-height: 1.75;
  overflow: hidden;
}

.answer-panel {
  padding: 18px 22px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(84, 116, 91, 0.14);
}

.thinking-panel {
  background: rgba(238, 247, 241, 0.48);
  color: rgba(80, 103, 93, 0.58);
  border: 1px solid rgba(84, 116, 91, 0.09);
  border-bottom: 0;
  border-radius: 12px 12px 0 0;
  transition: background 0.2s ease, color 0.2s ease;
}

.thinking-panel.collapsed {
  border-bottom: 1px solid rgba(84, 116, 91, 0.09);
  border-radius: 12px;
}

.thinking-toggle {
  width: 100%;
  min-height: 38px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 0;
  padding: 8px 14px;
  background: transparent;
  color: rgba(57, 96, 79, 0.7);
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.thinking-toggle:hover {
  color: rgba(31, 94, 72, 0.92);
  background: rgba(255, 255, 255, 0.2);
}

.toggle-text {
  font-size: 12px;
  font-weight: 600;
  color: rgba(57, 96, 79, 0.52);
}

.thinking-content {
  padding: 0 16px 14px;
  font-size: 14px;
}

.thinking-panel + .answer-panel {
  border-top: 1px solid rgba(84, 116, 91, 0.12);
  border-radius: 0 0 12px 12px;
}

.markdown-body :deep(h3) {
  margin: 18px 0 12px;
  line-height: 1.35;
  font-size: 21px;
  font-weight: 800;
}

.markdown-body :deep(h3:first-child) {
  margin-top: 0;
}

.markdown-body :deep(p) {
  margin: 10px 0;
}

.thinking-content :deep(p) {
  margin: 8px 0;
}

.markdown-body :deep(ul) {
  margin: 10px 0 14px;
  padding-left: 22px;
}

.markdown-body :deep(li) {
  margin: 8px 0;
  padding-left: 4px;
}

.markdown-body :deep(strong) {
  font-weight: 800;
}

.markdown-body :deep(code) {
  padding: 2px 5px;
  border-radius: 5px;
  background: rgba(47, 143, 123, 0.1);
  font-family: Consolas, monospace;
  font-size: 0.92em;
}

.markdown-body :deep(.table-wrap) {
  margin: 12px 0;
  overflow-x: auto;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.72);
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  padding: 8px 10px;
  border: 1px solid rgba(84, 116, 91, 0.18);
  text-align: left;
  vertical-align: top;
}

.markdown-body :deep(th) {
  background: rgba(47, 143, 123, 0.12);
  font-weight: 800;
}

.markdown-body :deep(.markdown-link),
.markdown-body :deep(.travel-pdf-text-link),
.bubble :deep(a.travel-pdf-text-link) {
  display: inline !important;
  margin: 0 !important;
  padding: 0 !important;
  border: 0 !important;
  border-radius: 0 !important;
  background: transparent !important;
  background-image: none !important;
  box-shadow: none !important;
  color: #2378bd !important;
  font: inherit !important;
  font-weight: inherit !important;
  line-height: inherit !important;
  text-decoration: none !important;
  vertical-align: baseline !important;
}

.markdown-body :deep(.markdown-link:hover),
.markdown-body :deep(.travel-pdf-text-link:hover),
.bubble :deep(a.travel-pdf-text-link:hover) {
  text-decoration: underline !important;
}

.chat-message.user .bubble-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.chat-message.user .meta {
  flex-direction: row-reverse;
}

.chat-message.user .bubble {
  padding: 16px 18px;
  background: linear-gradient(135deg, #2f9d7e 0%, #3f8dc2 100%);
  color: white;
}

.bubble.streaming {
  position: relative;
}

.bubble.streaming::after {
  content: '';
  position: absolute;
  right: 8px;
  bottom: 8px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.placeholder {
  color: #5f746a;
  font-style: italic;
}
</style>

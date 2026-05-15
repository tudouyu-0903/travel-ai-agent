export function generateChatId() {
  const time = Date.now().toString(36);
  const random = Math.random().toString(36).slice(2, 10);
  return `chat-${time}-${random}`;
}

export function scrollToBottom(container) {
  if (!container) {
    return;
  }

  requestAnimationFrame(() => {
    container.scrollTop = container.scrollHeight;
  });
}

<template>
  <div class="chat-container">
    <div class="message-list" ref="messaggListRef">
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="['message-row', message.isUser ? 'user' : 'bot']"
        :style="{ '--i': index }"
      >
        <div :class="['avatar', message.isUser ? 'avatar-user' : 'avatar-bot']">
          <i :class="message.isUser ? 'fa-solid fa-user' : 'fa-solid fa-robot'"></i>
        </div>
        <div :class="['bubble', message.isUser ? 'bubble-user' : 'bubble-bot']">
          <span v-html="message.content"></span>
          <span class="loading-dots" v-if="message.isTyping">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </span>
        </div>
      </div>
    </div>

    <div class="input-bar">
      <div class="input-inner">
        <el-input
          v-model="inputMessage"
          placeholder="请输入您的健康问题..."
          @keyup.enter="sendMessage"
          class="chat-input"
        ></el-input>
        <button class="send-button" @click="sendMessage" :disabled="isSending">
          <i class="fa-solid fa-paper-plane"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { v4 as uuidv4 } from 'uuid'

const messaggListRef = ref()
const isSending = ref(false)
const uuid = ref()
const inputMessage = ref('')
const messages = ref([])

onMounted(() => {
  initUUID()
  watch(messages, () => scrollToBottom(), { deep: true })
  hello()
})

const scrollToBottom = () => {
  if (messaggListRef.value) {
    messaggListRef.value.scrollTop = messaggListRef.value.scrollHeight
  }
}

const hello = () => {
  sendRequest('你好')
}

const sendMessage = () => {
  if (inputMessage.value.trim()) {
    sendRequest(inputMessage.value.trim())
    inputMessage.value = ''
  }
}

const sendRequest = async (message) => {
  isSending.value = true
  const userMsg = {
    isUser: true,
    content: message,
    isTyping: false,
    isThinking: false,
  }
  if(messages.value.length > 0){
    messages.value.push(userMsg)
  }

  const botMsg = {
    isUser: false,
    content: '',
    isTyping: true,
    isThinking: false,
  }
  messages.value.push(botMsg)
  const lastMsg = messages.value[messages.value.length - 1]
  scrollToBottom()

  try {
    const response = await fetch(__API_BASE_URL__ + '/xiaozhi/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ memoryId: uuid.value, message }),
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const text = decoder.decode(value, { stream: true })
      lastMsg.content += text
      scrollToBottom()
    }

    messages.value.at(-1).isTyping = false
    isSending.value = false
  } catch (error) {
    console.error('Stream error:', error)
    messages.value.at(-1).content = '请求失败，请重试'
    messages.value.at(-1).isTyping = false
    isSending.value = false
  }
}

const initUUID = () => {
  let storedUUID = localStorage.getItem('user_uuid')
  if (!storedUUID) {
    storedUUID = uuidToNumber(uuidv4())
    localStorage.setItem('user_uuid', storedUUID)
  }
  uuid.value = storedUUID
}

const uuidToNumber = (uuid) => {
  let number = 0
  for (let i = 0; i < uuid.length && i < 6; i++) {
    const hexValue = uuid[i]
    number = number * 16 + (parseInt(hexValue, 16) || 0)
  }
  return number % 1000000
}

const newChat = () => {
  localStorage.removeItem('user_uuid')
  window.location.reload()
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: border-color var(--duration-normal) var(--ease-smooth);
}

.chat-container:focus-within {
  border-color: var(--color-brand-subtle);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: var(--color-surface-secondary);
}

.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-thumb {
  background: var(--color-text-tertiary);
  border-radius: 3px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  width: 100%;
  gap: 12px;
  animation: fade-in-up 0.35s var(--ease-smooth);
  animation-delay: calc(var(--i, 0) * 0.05s);
  animation-fill-mode: both;
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
  transition: transform var(--duration-fast) var(--ease-smooth);
}

.message-row:hover .avatar {
  transform: scale(1.05);
}

.avatar-user {
  background: var(--color-brand);
  color: var(--color-text-inverse);
}

.avatar-bot {
  background: var(--color-surface);
  color: var(--color-brand);
  border: 1px solid var(--color-brand-subtle);
}

.bubble {
  max-width: 75%;
  padding: 12px 16px;
  font-size: var(--text-sm);
  line-height: 1.6;
  word-wrap: break-word;
  transition: box-shadow var(--duration-fast) var(--ease-out);
}

.bubble-user {
  background: var(--color-brand);
  color: var(--color-text-inverse);
  border-radius: var(--radius-md) 4px var(--radius-md) var(--radius-md);
}

.bubble-bot {
  background: var(--color-surface);
  color: var(--color-text-primary);
  border-radius: 4px var(--radius-md) var(--radius-md) var(--radius-md);
  border: 1px solid var(--color-border);
}

.bubble-bot:hover {
  box-shadow: var(--shadow-sm);
}

.input-bar {
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
  background: var(--color-surface);
}

.input-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: 4px 4px 4px 16px;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.input-inner:focus-within {
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px rgba(46, 184, 114, 0.08);
}

:deep(.chat-input .el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
}

:deep(.chat-input .el-input__inner) {
  font-size: var(--text-sm);
  height: 36px;
}

.send-button {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-brand);
  border: none;
  color: var(--color-text-inverse);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.send-button:hover:not(:disabled) {
  background: var(--color-brand-hover);
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(46, 184, 114, 0.3);
}

.send-button:active:not(:disabled) {
  transform: scale(0.92);
}

.send-button:disabled {
  background: var(--color-surface-tertiary);
  color: var(--color-text-tertiary);
  cursor: not-allowed;
}

/* Loading dots */
.loading-dots {
  display: inline-flex;
  align-items: center;
  height: 20px;
  gap: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  background: var(--color-text-tertiary);
  border-radius: 50%;
  animation: bounce-dot 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce-dot {
  0%, 80%, 100% { transform: scale(0.4); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}
</style>

<template>
  <div class="chat-page">
    <div class="chat-header">
      <button class="back-btn" @click="goBack">
        <i class="fa-solid fa-chevron-left"></i>
      </button>
      <div class="chat-info">
        <h3>{{ doctorName }}</h3>
        <span class="status">问诊中</span>
      </div>
      <button class="end-btn" @click="endConsult">结束问诊</button>
    </div>

    <div class="message-list" ref="messageListRef">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message-item', msg.isMine ? 'mine' : 'other']"
        :style="{ '--msg-i': index }"
      >
        <div class="avatar">
          <i :class="msg.isMine ? 'fa-solid fa-user' : 'fa-solid fa-user-doctor'"></i>
        </div>
        <div class="message-content">
          <div class="bubble">{{ msg.content }}</div>
          <div class="time">{{ msg.time }}</div>
        </div>
      </div>
    </div>

    <div class="input-area">
      <div class="input-row">
        <el-input
          v-model="inputMessage"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
        ></el-input>
        <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim()">
          <i class="fa-solid fa-paper-plane"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useConsultStore } from '@/stores/consult'
import webSocketService from '@/services/websocket'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const consultStore = useConsultStore()

const sessionId = ref(route.params.sessionId)
const doctorName = ref('医生')
const inputMessage = ref('')
const messages = ref([])
const messageListRef = ref(null)

let wsConnected = false

onMounted(async () => {
  await loadHistory()
  initWebSocket()
})

onUnmounted(() => {
  if (wsConnected) {
    webSocketService.disconnect()
  }
})

const loadHistory = async () => {
  try {
    const res = await axios.get(`${__API_BASE_URL__}/consult/${sessionId.value}/messages`)
    if (res.data.code === '0' && Array.isArray(res.data.data)) {
      const patientId = getPatientId()
      messages.value = res.data.data.map(msg => ({
        content: msg.content,
        senderType: msg.senderType,
        isMine: msg.senderId === patientId,
        time: formatTime(msg.timestamp)
      }))
      scrollToBottom()
    }
  } catch (e) {
    console.error('Failed to load history:', e)
  }
}

const initWebSocket = () => {
  const patientId = getPatientId()
  webSocketService.connect(
    patientId,
    'PATIENT',
    () => {
      wsConnected = true
      webSocketService.subscribe(`/topic/consult.${sessionId.value}`, (msg) => {
        if (Number(msg.senderId) === Number(patientId)) return
        messages.value.push({
          content: msg.content,
          senderType: msg.senderType,
          isMine: false,
          time: formatTime(msg.timestamp)
        })
        scrollToBottom()
      })
    },
    (error) => console.error('WebSocket error:', error)
  )
}

const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  const patientId = getPatientId()
  webSocketService.sendChatMessage(
    sessionId.value,
    patientId,
    'PATIENT',
    '患者',
    inputMessage.value.trim()
  )

  messages.value.push({
    content: inputMessage.value.trim(),
    senderType: 'PATIENT',
    isMine: true,
    time: formatTime(Date.now())
  })

  inputMessage.value = ''
  scrollToBottom()
}

const endConsult = () => {
  webSocketService.endConsult(sessionId.value)
  router.push('/patient/doctors')
}

const goBack = () => {
  router.push('/patient/doctors')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getPatientId = () => {
  return localStorage.getItem('user_id') || Math.floor(Math.random() * 10000) + 1000
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 180px);
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--color-border);
  transition: border-color var(--duration-normal) var(--ease-smooth);
}

.chat-page:focus-within {
  border-color: var(--color-brand-subtle);
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface);
}

.back-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-surface-tertiary);
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  margin-right: 14px;
  transition: all var(--duration-normal) var(--ease-smooth);
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn:hover {
  background: var(--color-border);
  color: var(--color-text-primary);
  transform: translateX(-3px);
}

.back-btn:active {
  transform: translateX(-3px) scale(0.93);
}

.chat-info {
  flex: 1;
}

.chat-info h3 {
  margin: 0;
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text-primary);
}

.chat-info .status {
  font-size: var(--text-xs);
  color: var(--color-brand);
  position: relative;
  padding-left: 12px;
}

.chat-info .status::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-brand);
  animation: breathe 2s ease-in-out infinite;
}

.end-btn {
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  color: var(--color-text-secondary);
  font-size: var(--text-xs);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.end-btn:hover {
  border-color: var(--color-error);
  color: var(--color-error);
  background: var(--color-error-bg);
}

.end-btn:active {
  transform: scale(0.97);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--color-surface-secondary);
}

.message-item {
  display: flex;
  gap: 10px;
  max-width: 80%;
  animation: fade-in-up 0.3s var(--ease-smooth);
  animation-delay: calc(var(--msg-i, 0) * 0.03s);
  animation-fill-mode: both;
}

.message-item.mine {
  align-self: flex-end;
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

.message-item:hover .avatar {
  transform: scale(1.05);
}

.mine .avatar {
  background: var(--color-brand);
  color: var(--color-text-inverse);
}

.other .avatar {
  background: var(--color-surface);
  color: var(--color-brand);
  border: 1px solid var(--color-brand-subtle);
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bubble {
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  line-height: 1.5;
  word-wrap: break-word;
  transition: box-shadow var(--duration-fast) var(--ease-out);
}

.mine .bubble {
  background: var(--color-brand);
  color: var(--color-text-inverse);
  border-bottom-right-radius: 4px;
}

.other .bubble {
  background: var(--color-surface);
  color: var(--color-text-primary);
  border-bottom-left-radius: 4px;
  border: 1px solid var(--color-border);
}

.other .bubble:hover {
  box-shadow: var(--shadow-sm);
}

.time {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  padding: 0 4px;
}

.mine .time {
  text-align: right;
}

.input-area {
  padding: 16px 20px;
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
}

.input-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-full);
  box-shadow: 0 0 0 1px var(--color-border) !important;
  padding-left: 16px;
  transition: all var(--duration-normal) var(--ease-smooth);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-border-hover) !important;
  background: var(--color-surface-secondary);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--color-brand) !important;
}

:deep(.el-input__inner) {
  caret-color: var(--color-brand);
}

.send-btn {
  width: 44px;
  height: 44px;
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

.send-btn:hover:not(:disabled) {
  background: var(--color-brand-hover);
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(46, 184, 114, 0.3);
}

.send-btn:active:not(:disabled) {
  transform: scale(0.92);
}

.send-btn:disabled {
  background: var(--color-surface-tertiary);
  color: var(--color-text-tertiary);
  cursor: not-allowed;
}
</style>

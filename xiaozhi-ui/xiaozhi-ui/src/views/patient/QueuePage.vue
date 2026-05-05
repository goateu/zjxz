<template>
  <div class="queue-page">
    <div class="queue-card">
      <div class="queue-visual">
        <div class="pulse-circles">
          <div class="pulse-ring r1"></div>
          <div class="pulse-ring r2"></div>
          <div class="pulse-ring r3"></div>
        </div>
        <div class="queue-icon">
          <i v-if="position && position <= 1" class="fa-solid fa-bell"></i>
          <i v-else class="fa-solid fa-user-clock"></i>
        </div>
      </div>

      <h2>{{ position && position <= 1 ? '即将轮到您' : '正在排队中' }}</h2>

      <div class="queue-info">
        <div class="info-item">
          <span class="info-label">医生</span>
          <span class="info-value">{{ doctorName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">排队位置</span>
          <span :class="['info-value position', { pop: popPosition }]" v-if="position !== null">{{ position }}</span>
          <span class="info-value skeleton-text" v-else>计算中...</span>
        </div>
        <div class="info-item">
          <span class="info-label">预计等待</span>
          <span class="info-value">{{ estimatedWait || '0' }} 分钟</span>
        </div>
      </div>

      <div class="actions">
        <button
          v-if="estimatedWait > 5"
          class="btn-ai"
          @click="goToAI"
        >
          <i class="fa-solid fa-robot"></i>
          先和AI聊聊
        </button>
        <button class="btn-cancel" @click="cancelQueue">
          <i class="fa-solid fa-xmark"></i>
          取消排队
        </button>
      </div>
    </div>

    <div class="tips">
      <div class="tips-header">
        <i class="fa-solid fa-circle-info"></i>
        温馨提示
      </div>
      <ul>
        <li>请保持页面不要关闭，排队位置会实时更新</li>
        <li>轮到您时，系统会自动为您接入医生</li>
        <li>如需取消排队，点击上方"取消排队"按钮</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useConsultStore } from '@/stores/consult'
import webSocketService from '@/services/websocket'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const consultStore = useConsultStore()

const doctorId = ref(Number(route.params.doctorId))
const doctorName = ref('加载中...')
const position = ref(null)
const estimatedWait = ref(0)
const popPosition = ref(false)

let wsConnected = false
let sessionId = null
let pollingTimer = null

onMounted(async () => {
  sessionId = consultStore.currentSession?.id
  if (!sessionId) {
    const patientId = getPatientId()
    try {
      const res = await axios.post(__API_BASE_URL__ + '/consult/create', null, {
        params: { patientId, doctorId: doctorId.value, type: 'CONSULT' }
      })
      if (res.data.code === '0') {
        sessionId = res.data.data
        consultStore.setCurrentSession({ id: sessionId })
      }
    } catch (e) {
      console.error('Failed to create session:', e)
    }
  }
  initWebSocket()
  startPolling()

  watch(position, (newVal, oldVal) => {
    if (newVal !== null && newVal !== oldVal) {
      popPosition.value = true
      setTimeout(() => { popPosition.value = false }, 500)
    }
  })
})

onUnmounted(() => {
  stopPolling()
  if (wsConnected) {
    webSocketService.disconnect()
  }
})

const initWebSocket = () => {
  const patientId = getPatientId()
  webSocketService.connect(
    patientId,
    'PATIENT',
    () => {
      wsConnected = true
      webSocketService.subscribe(`/topic/queue.${patientId}`, (data) => {
        position.value = data.position
        estimatedWait.value = data.estimatedWaitMinutes
        consultStore.setQueueInfo(data.position, data.estimatedWaitMinutes)
      })

      webSocketService.subscribe(`/topic/queue.${patientId}.called`, (data) => {
        router.push(`/patient/chat/${data}`)
      })

      if (sessionId) {
        webSocketService.joinQueue(doctorId.value, patientId, sessionId)
      }
    },
    (error) => {
      console.error('WebSocket error:', error)
    }
  )
}

const startPolling = () => {
  pollingTimer = setInterval(() => {
    fetchQueuePosition()
  }, 3000)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

const fetchQueuePosition = () => {
  // WebSocket handles position updates
}

const getPatientId = () => {
  return localStorage.getItem('user_id') || Math.floor(Math.random() * 10000) + 1000
}

const cancelQueue = () => {
  const patientId = getPatientId()
  webSocketService.cancelQueue(doctorId.value, patientId)
  consultStore.clearQueueInfo()
  router.push('/patient/doctors')
}

const goToAI = () => {
  cancelQueue()
  router.push('/patient/ai')
}
</script>

<style scoped>
.queue-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
  padding-top: 40px;
}

.queue-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 48px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0,0,0,0.06), 0 0 0 1px var(--color-border);
  width: 100%;
  max-width: 440px;
  animation: fade-in-up 0.6s var(--ease-smooth);
}

.queue-visual {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 28px;
}

.pulse-circles {
  position: absolute;
  inset: 0;
}

.pulse-ring {
  position: absolute;
  border-radius: 50%;
  border: 2px solid var(--color-brand);
  animation: pulse-ring 2.5s var(--ease-smooth) infinite;
}

.r1 { inset: 0; animation-delay: 0s; }
.r2 { inset: 12px; animation-delay: 0.5s; }
.r3 { inset: 24px; animation-delay: 1s; }

@keyframes pulse-ring {
  0% { transform: scale(0.9); opacity: 0.8; }
  100% { transform: scale(1.8); opacity: 0; }
}

.queue-icon {
  position: absolute;
  inset: 24px;
  background: var(--color-brand);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.queue-icon i {
  font-size: 28px;
  color: white;
  animation: pulse-soft 2.5s var(--ease-smooth) infinite;
}

.queue-card h2 {
  font-size: var(--text-xl);
  color: var(--color-text-primary);
  margin: 0 0 24px;
  font-weight: 600;
}

.queue-info {
  background: var(--color-surface-tertiary);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
  transition: background var(--duration-fast) var(--ease-out);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: var(--color-text-tertiary);
  font-size: var(--text-sm);
}

.info-value {
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: var(--text-sm);
}

.info-value.position {
  font-size: var(--text-2xl);
  color: var(--color-brand);
  font-weight: 700;
  transition: all 0.3s var(--ease-smooth);
  display: inline-block;
}

.info-value.position.pop {
  animation: count-pop 0.3s var(--ease-spring);
}

.skeleton-text {
  color: var(--color-text-tertiary) !important;
  font-size: var(--text-sm) !important;
  font-weight: 400 !important;
  animation: shimmer-enhanced 1.8s var(--ease-smooth) infinite;
  background: linear-gradient(90deg, transparent 25%, var(--color-surface-tertiary) 50%, transparent 75%);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn-ai {
  padding: 12px 24px;
  background: var(--color-surface-tertiary);
  color: var(--color-text-primary);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-ai:hover {
  background: var(--color-border);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.btn-ai:active {
  transform: translateY(0) scale(0.98);
}

.btn-cancel {
  padding: 10px 24px;
  background: transparent;
  color: var(--color-text-tertiary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn-cancel:hover {
  border-color: var(--color-error);
  color: var(--color-error);
  background: var(--color-error-bg);
}

.btn-cancel:active {
  transform: scale(0.97);
}

.tips {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 24px;
  width: 100%;
  max-width: 440px;
  transition: border-color var(--duration-normal) var(--ease-smooth);
}

.tips:hover {
  border-color: var(--color-brand-subtle);
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-primary);
  margin: 0 0 14px;
  font-size: var(--text-sm);
  font-weight: 600;
}

.tips-header i {
  color: var(--color-brand);
}

.tips ul {
  margin: 0;
  padding-left: 20px;
  color: var(--color-text-secondary);
  font-size: var(--text-xs);
  line-height: 2;
}
</style>

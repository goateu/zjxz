<template>
  <div class="dashboard">
    <header class="dashboard-header">
      <div class="header-left">
        <img src="@/assets/logo1.png" alt="之江小智" class="logo" />
        <div class="header-info">
          <h1>医生工作台</h1>
          <span class="subtitle">欢迎，{{ doctorName }}</span>
        </div>
      </div>
      <div class="header-right">
        <div class="status-badge" :class="doctorStatus">
          <span class="status-dot"></span>
          {{ statusText }}
        </div>
        <button class="logout-btn" @click="handleLogout">
          <i class="fa-solid fa-right-from-bracket"></i>
          退出
        </button>
      </div>
    </header>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon waiting">
          <i class="fa-solid fa-users"></i>
        </div>
        <div class="stat-body">
          <span :class="['stat-value', { pop: popWaiting }]">{{ waitingCount }}</span>
          <span class="stat-label">等待中</span>
        </div>
        <div class="stat-trend">
          <span class="stat-dot-pulse"></span>
          实时更新
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon consulting">
          <i class="fa-solid fa-comments"></i>
        </div>
        <div class="stat-body">
          <span :class="['stat-value', { pop: popConsulting }]">{{ consultingCount }}</span>
          <span class="stat-label">问诊中</span>
        </div>
        <div class="stat-trend">
          <span class="stat-bar">
            <span class="stat-bar-fill" :style="{ width: maxConsult > 0 ? (consultingCount / maxConsult) * 100 + '%' : '0%' }"></span>
          </span>
          {{ consultingCount }}/{{ maxConsult }}
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon completed">
          <i class="fa-solid fa-check-circle"></i>
        </div>
        <div class="stat-body">
          <span :class="['stat-value', { pop: popCompleted }]">{{ completedCount }}</span>
          <span class="stat-label">今日完成</span>
        </div>
      </div>
    </div>

    <div class="dashboard-content">
      <section class="queue-panel">
        <div class="panel-header">
          <h2>
            <i class="fa-solid fa-list"></i>
            排队患者
          </h2>
          <span class="badge" v-if="waitingCount > 0">{{ waitingCount }}</span>
        </div>

        <div v-if="waitingQueue.length === 0" class="empty-state">
          <div class="empty-icon-wrap">
            <i class="fa-solid fa-inbox"></i>
          </div>
          <p>暂无排队患者</p>
          <span class="empty-hint">当患者加入排队时会在此显示</span>
        </div>

        <div v-else class="queue-list">
          <transition-group name="queue-item" tag="div">
            <div
              v-for="(patient, index) in waitingQueue"
              :key="patient.id || patient.patientId"
              class="queue-item"
              @click="callPatient(patient)"
              :style="{ '--i': index }"
            >
              <div class="queue-pos">{{ index + 1 }}</div>
              <div class="queue-info">
                <span class="queue-name">患者 {{ patient.patientId }}</span>
                <span class="queue-wait">等待 {{ patient.waitMinutes }} 分钟</span>
              </div>
              <button class="call-btn" @click.stop="callPatient(patient)">
                <i class="fa-solid fa-bell"></i>
                叫号
              </button>
            </div>
          </transition-group>
        </div>
      </section>

      <section class="consult-panel">
        <div class="panel-header">
          <h2>
            <i class="fa-solid fa-comments"></i>
            问诊中
          </h2>
        </div>

        <div v-if="consultingSessions.length === 0" class="empty-state">
          <div class="empty-icon-wrap">
            <i class="fa-solid fa-user-clock"></i>
          </div>
          <p>暂无进行中的问诊</p>
          <span class="empty-hint">叫号后患者将出现在这里</span>
        </div>

        <div v-else class="consult-grid">
          <div
            v-for="session in consultingSessions"
            :key="session.id"
            class="consult-card"
            :class="{ active: activeSession === session.id }"
            @click="selectSession(session)"
          >
            <div class="consult-top">
              <div class="consult-avatar" :class="{ 'is-active': activeSession === session.id }">
                <i class="fa-solid fa-user"></i>
              </div>
              <div class="consult-meta">
                <span class="consult-name">患者 {{ session.patientId }}</span>
                <span class="consult-time">{{ formatTime(session.startTime) }}</span>
              </div>
              <button class="end-consult-btn" @click.stop="endSession(session.id)" title="结束问诊">
                <i class="fa-solid fa-xmark"></i>
              </button>
            </div>
            <div class="consult-preview">
              <p v-if="session.lastMessage">{{ session.lastMessage }}</p>
              <p v-else class="no-msg">暂无消息</p>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- Consult Window -->
    <transition name="window-slide">
      <div v-if="activeSession" class="consult-window">
        <div class="window-header">
          <div class="window-info">
            <h3>与 患者 {{ activeSessionData?.patientId }} 的问诊</h3>
            <span class="window-duration">{{ getDuration(activeSessionData?.startTime) }}</span>
          </div>
          <button class="window-close" @click="activeSession = null">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="window-messages" ref="messageListRef">
          <div
            v-for="(msg, index) in activeMessages"
            :key="index"
            :class="['msg', msg.isMine ? 'mine' : 'other']"
            :style="{ '--msg-i': index }"
          >
            <div class="msg-avatar">
              <i :class="msg.isMine ? 'fa-solid fa-user' : 'fa-solid fa-user-doctor'"></i>
            </div>
            <div class="msg-body">
              <div class="msg-bubble">{{ msg.content }}</div>
              <span class="msg-time">{{ msg.time }}</span>
            </div>
          </div>
        </div>

        <div class="window-input">
          <input
            v-model="inputMessage"
            placeholder="输入回复..."
            @keyup.enter="sendMessage"
          />
          <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim()">
            <i class="fa-solid fa-paper-plane"></i>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useDoctorStore } from '@/stores/doctor'
import webSocketService from '@/services/websocket'
import axios from 'axios'

const router = useRouter()
const doctorStore = useDoctorStore()

const doctorName = computed(() => doctorStore.doctorInfo?.doctor?.name || '医生')
const doctorStatus = computed(() => doctorStore.doctorInfo?.doctor?.status || 'OFFLINE')
const statusText = computed(() => {
  const status = doctorStatus.value
  if (status === 'ONLINE') return '在线'
  if (status === 'BUSY') return '忙碌中'
  return '离线'
})
const maxConsult = computed(() => doctorStore.doctorInfo?.doctor?.maxConsultCount || 3)

const waitingCount = ref(0)
const consultingCount = ref(0)
const completedCount = ref(0)
const waitingQueue = ref([])
const consultingSessions = ref([])
const activeSession = ref(null)
const inputMessage = ref('')
const messageListRef = ref(null)
const activeMessages = ref([])
const popWaiting = ref(false)
const popConsulting = ref(false)
const popCompleted = ref(false)

let wsConnected = false
let heartbeatTimer = null
let chatSubscription = null
let doctorId = null

onMounted(() => {
  doctorStore.initFromStorage()
  doctorId = doctorStore.doctorInfo?.doctor?.id
  initWebSocket()
  startHeartbeat()
  fetchTodayCompletedCount()
  watchStats()
})

const watchStats = () => {
  watch(waitingCount, (n, o) => {
    if (n !== o) { popWaiting.value = true; setTimeout(() => { popWaiting.value = false }, 300) }
  })
  watch(consultingCount, (n, o) => {
    if (n !== o) { popConsulting.value = true; setTimeout(() => { popConsulting.value = false }, 300) }
  })
  watch(completedCount, (n, o) => {
    if (n !== o) { popCompleted.value = true; setTimeout(() => { popCompleted.value = false }, 300) }
  })
}

onUnmounted(() => {
  stopHeartbeat()
  if (wsConnected) {
    webSocketService.disconnect()
  }
})

const initWebSocket = () => {
  const doctorId = doctorStore.doctorInfo?.doctor?.id
  if (!doctorId) return

  webSocketService.connect(
    doctorId,
    'DOCTOR',
    () => {
      wsConnected = true

      webSocketService.subscribe(`/topic/queue.${doctorId}`, (data) => {
        waitingCount.value = data.position || waitingQueue.value.length
      })

      webSocketService.subscribe(`/topic/doctor.queue.${doctorId}`, (patients) => {
        waitingQueue.value = patients
        waitingCount.value = patients.length
      })
    },
    (error) => console.error('WebSocket error:', error)
  )
}

const startHeartbeat = () => {
  heartbeatTimer = setInterval(() => {
    doctorStore.heartbeat()
  }, 30000)
}

const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

const fetchTodayCompletedCount = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/consult/todayCount/${doctorId}`)
    if (res.data.code === '0') {
      completedCount.value = res.data.data
    }
  } catch (e) {
    console.error('Failed to fetch completed count:', e)
  }
}

const callPatient = (patient) => {
  if (consultingCount.value >= maxConsult.value) {
    alert('已达到最大同时接待数，请先结束一个问诊')
    return
  }

  webSocketService.callPatient(
    doctorStore.doctorInfo.doctor.id,
    patient.patientId,
    patient.sessionId || patient.id
  )

  const session = { id: patient.sessionId || patient.id, patientId: patient.patientId, startTime: Date.now(), lastMessage: '' }
  consultingSessions.value.push(session)
  waitingQueue.value = waitingQueue.value.filter(p => p.id !== patient.id)
  waitingCount.value = waitingQueue.value.length
  consultingCount.value = consultingSessions.value.length
}

const selectSession = (session) => {
  activeSession.value = session.id
  loadMessages(session.id)
  subscribeChat(session.id)
}

const subscribeChat = (sessionId) => {
  if (chatSubscription) {
    chatSubscription.unsubscribe()
    chatSubscription = null
  }
  chatSubscription = webSocketService.subscribe(`/topic/consult.${sessionId}`, (msg) => {
    if (msg.senderId === doctorId) return
    activeMessages.value.push({
      content: msg.content,
      senderType: msg.senderType,
      isMine: false,
      time: formatTime(msg.timestamp)
    })
    nextTick(() => scrollToBottom())
  })
}

watch(activeSession, (newVal, oldVal) => {
  if (newVal && newVal !== oldVal) {
    const session = consultingSessions.value.find(s => s.id === newVal)
    if (session) {
      loadMessages(newVal)
      subscribeChat(newVal)
    }
  }
})

const activeSessionData = computed(() => {
  return consultingSessions.value.find(s => s.id === activeSession.value)
})

const loadMessages = async (sessionId) => {
  try {
    const res = await axios.get(`http://localhost:8080/consult/${sessionId}/messages`)
    if (res.data.code === '0' && Array.isArray(res.data.data)) {
      activeMessages.value = res.data.data.map(msg => ({
        content: msg.content,
        senderType: msg.senderType,
        isMine: msg.senderId === doctorId,
        time: formatTime(msg.timestamp)
      }))
      nextTick(() => scrollToBottom())
    }
  } catch (e) {
    console.error('Failed to load messages:', e)
  }
}

const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  webSocketService.sendChatMessage(
    activeSession.value,
    doctorStore.doctorInfo.doctor.id,
    'DOCTOR',
    doctorName.value,
    inputMessage.value.trim()
  )

  activeMessages.value.push({
    content: inputMessage.value.trim(),
    isMine: true,
    time: formatTime(Date.now())
  })

  const session = consultingSessions.value.find(s => s.id === activeSession.value)
  if (session) {
    session.lastMessage = inputMessage.value.trim()
  }

  inputMessage.value = ''
  nextTick(() => scrollToBottom())
}

const endSession = (sessionId) => {
  webSocketService.endConsult(sessionId)
  consultingSessions.value = consultingSessions.value.filter(s => s.id !== sessionId)
  consultingCount.value = consultingSessions.value.length
  if (activeSession.value === sessionId) {
    activeSession.value = null
  }
}

const handleLogout = () => {
  doctorStore.logout()
  router.push('/doctor/login')
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getDuration = (startTime) => {
  if (!startTime) return '0分钟'
  const minutes = Math.floor((Date.now() - startTime) / 60000)
  return `${minutes}分钟`
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: var(--color-surface-secondary);
  padding: 24px 32px;
}

/* ─── Header ─── */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-surface);
  padding: 16px 24px;
  border-radius: var(--radius-lg);
  margin-bottom: 24px;
  border: 1px solid var(--color-border);
  transition: border-color var(--duration-normal) var(--ease-smooth);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  transition: transform var(--duration-normal) var(--ease-smooth);
}

.logo:hover {
  transform: rotate(-4deg) scale(1.05);
}

.header-info h1 {
  font-size: var(--text-lg);
  color: var(--color-text-primary);
  margin: 0 0 2px;
  font-weight: 600;
}

.subtitle {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 500;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.status-badge.ONLINE {
  background: var(--color-success-bg);
  color: var(--color-success);
}

.status-badge.BUSY {
  background: var(--color-warning-bg);
  color: var(--color-warning);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.status-badge.ONLINE .status-dot {
  animation: breathe 2s ease-in-out infinite;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  color: var(--color-text-secondary);
  font-size: var(--text-xs);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.logout-btn:hover {
  border-color: var(--color-error);
  color: var(--color-error);
  background: var(--color-error-bg);
}

/* ─── Stats ─── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all var(--duration-normal) var(--ease-smooth);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--color-brand), transparent);
  opacity: 0;
  transition: opacity var(--duration-normal) var(--ease-smooth);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card:hover {
  border-color: var(--color-brand-subtle);
  box-shadow: 0 8px 30px rgba(0,0,0,0.06);
  transform: translateY(-2px);
}

.stat-card:active {
  transform: translateY(0) scale(0.98);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  transition: transform var(--duration-normal) var(--ease-smooth);
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
}

.stat-icon.waiting {
  background: var(--color-warning-bg);
  color: var(--color-warning);
}

.stat-icon.consulting {
  background: var(--color-brand-subtle);
  color: var(--color-brand);
}

.stat-icon.completed {
  background: var(--color-success-bg);
  color: var(--color-success);
}

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
  display: inline-block;
}

.stat-value.pop {
  animation: count-pop 0.3s var(--ease-spring);
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}

.stat-trend {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--color-text-tertiary);
  align-self: flex-start;
  padding-top: 4px;
}

.stat-dot-pulse {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--color-brand);
  animation: breathe 2s ease-in-out infinite;
}

.stat-bar {
  width: 40px;
  height: 3px;
  background: var(--color-surface-tertiary);
  border-radius: 2px;
  overflow: hidden;
}

.stat-bar-fill {
  display: block;
  height: 100%;
  background: var(--color-brand);
  border-radius: 2px;
  transition: width 0.5s var(--ease-smooth);
}

/* ─── Content ─── */
.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
}

.queue-panel,
.consult-panel {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 24px;
  transition: border-color var(--duration-normal) var(--ease-smooth);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--color-border);
}

.panel-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--text-base);
  color: var(--color-text-primary);
  margin: 0;
  font-weight: 600;
}

.panel-header h2 i {
  color: var(--color-brand);
}

.badge {
  background: var(--color-warning);
  color: white;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 600;
}

/* ─── Empty State ─── */
.empty-state {
  text-align: center;
  padding: 48px 20px;
  color: var(--color-text-tertiary);
}

.empty-icon-wrap {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  background: var(--color-surface-tertiary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform var(--duration-normal) var(--ease-smooth);
}

.empty-state:hover .empty-icon-wrap {
  transform: scale(1.05) rotate(-4deg);
}

.empty-icon-wrap i {
  font-size: 24px;
  color: var(--color-text-tertiary);
}

.empty-state p {
  margin: 0 0 6px;
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.empty-hint {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}

/* ─── Queue List ─── */
.queue-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.queue-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: var(--color-surface-tertiary);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.queue-item:hover {
  background: var(--color-brand-subtle);
  transform: translateX(4px);
}

.queue-item:active {
  transform: translateX(4px) scale(0.99);
}

.queue-pos {
  width: 32px;
  height: 32px;
  background: var(--color-warning);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: var(--text-sm);
  flex-shrink: 0;
}

.queue-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.queue-name {
  font-weight: 500;
  color: var(--color-text-primary);
  font-size: var(--text-sm);
}

.queue-wait {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}

.call-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: var(--color-brand);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
  flex-shrink: 0;
}

.call-btn:hover {
  background: var(--color-brand-hover);
  box-shadow: 0 4px 12px rgba(46, 184, 114, 0.3);
  transform: translateY(-2px) scale(1.02);
}

.call-btn:active {
  transform: translateY(0) scale(0.97);
}

/* queue-item transition */
.queue-item-enter-active {
  animation: slide-in-right 0.3s var(--ease-smooth);
}

.queue-item-leave-active {
  animation: fade-in 0.2s var(--ease-smooth) reverse;
}

.queue-item-move {
  transition: transform 0.3s var(--ease-smooth);
}

/* ─── Consult Grid ─── */
.consult-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.consult-card {
  background: var(--color-surface-tertiary);
  border-radius: var(--radius-md);
  padding: 16px;
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
  border: 1px solid transparent;
}

.consult-card:hover {
  border-color: var(--color-brand);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.consult-card.active {
  border-color: var(--color-brand);
  background: var(--color-brand-subtle);
}

.consult-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.consult-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-brand);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.consult-card:hover .consult-avatar {
  transform: scale(1.05);
}

.consult-avatar.is-active {
  box-shadow: 0 0 0 3px rgba(46, 184, 114, 0.2);
}

.consult-meta {
  flex: 1;
  min-width: 0;
}

.consult-name {
  display: block;
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: var(--text-sm);
}

.consult-time {
  display: block;
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}

.end-consult-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: transparent;
  border: none;
  color: var(--color-text-tertiary);
  cursor: pointer;
  opacity: 0;
  transition: all var(--duration-normal) var(--ease-smooth);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.consult-card:hover .end-consult-btn {
  opacity: 1;
}

.end-consult-btn:hover {
  background: var(--color-error-bg);
  color: var(--color-error);
  transform: scale(1.1);
}

.consult-preview {
  padding: 10px 12px;
  background: var(--color-surface);
  border-radius: var(--radius-sm);
  transition: background var(--duration-fast) var(--ease-out);
}

.consult-card:hover .consult-preview {
  background: var(--color-surface);
}

.consult-preview p {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-msg {
  color: var(--color-text-tertiary) !important;
  font-style: italic;
}

/* ─── Consult Window ─── */
.consult-window {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 400px;
  height: 520px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px var(--color-border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 100;
}

.window-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface);
}

.window-info h3 {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.window-duration {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}

.window-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-surface-tertiary);
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
  display: flex;
  align-items: center;
  justify-content: center;
}

.window-close:hover {
  background: var(--color-border);
  color: var(--color-text-primary);
  transform: rotate(90deg);
}

.window-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: var(--color-surface-secondary);
}

.msg {
  display: flex;
  gap: 8px;
  max-width: 85%;
  animation: fade-in-up 0.3s var(--ease-smooth);
  animation-delay: calc(var(--msg-i, 0) * 0.03s);
  animation-fill-mode: both;
}

.msg.mine {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  transition: transform var(--duration-fast) var(--ease-smooth);
}

.msg:hover .msg-avatar {
  transform: scale(1.05);
}

.mine .msg-avatar {
  background: var(--color-brand);
  color: white;
}

.other .msg-avatar {
  background: var(--color-surface);
  color: var(--color-brand);
  border: 1px solid var(--color-brand-subtle);
}

.msg-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.msg-bubble {
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  line-height: 1.4;
  transition: box-shadow var(--duration-fast) var(--ease-out);
}

.mine .msg-bubble {
  background: var(--color-brand);
  color: white;
  border-bottom-right-radius: 2px;
}

.other .msg-bubble {
  background: var(--color-surface);
  color: var(--color-text-primary);
  border-bottom-left-radius: 2px;
  border: 1px solid var(--color-border);
}

.other .msg-bubble:hover {
  box-shadow: var(--shadow-sm);
}

.msg-time {
  font-size: 10px;
  color: var(--color-text-tertiary);
  margin-top: 2px;
}

.mine .msg-time {
  text-align: right;
}

.window-input {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid var(--color-border);
  background: var(--color-surface);
}

.window-input input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  outline: none;
  font-family: inherit;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.window-input input:hover {
  border-color: var(--color-border-hover);
}

.window-input input:focus {
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px rgba(46, 184, 114, 0.08);
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-brand);
  border: none;
  color: white;
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
  box-shadow: 0 2px 8px rgba(46, 184, 114, 0.3);
}

.send-btn:active:not(:disabled) {
  transform: scale(0.92);
}

.send-btn:disabled {
  background: var(--color-surface-tertiary);
  color: var(--color-text-tertiary);
  cursor: not-allowed;
}

/* ─── Window Slide Transition ─── */
.window-slide-enter-active {
  animation: scale-in-bounce 0.35s var(--ease-smooth);
}

.window-slide-leave-active {
  animation: scale-in 0.2s var(--ease-smooth) reverse;
}
</style>

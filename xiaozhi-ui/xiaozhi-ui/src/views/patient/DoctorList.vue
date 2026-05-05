<template>
  <div class="doctor-list-page">
    <div class="page-header">
      <h2>选择医生</h2>
      <p>根据您的病情选择合适的医生进行咨询</p>
    </div>

    <div v-if="loading" class="loading-grid">
      <div v-for="n in 3" :key="n" class="skeleton-card">
        <div class="skeleton-row">
          <div class="skeleton skeleton-avatar"></div>
          <div class="skeleton skeleton-title"></div>
        </div>
        <div class="skeleton skeleton-line"></div>
        <div class="skeleton skeleton-line short"></div>
      </div>
    </div>

    <div v-else-if="doctors.length === 0" class="empty-state">
      <div class="empty-icon-wrap">
        <i class="fa-solid fa-user-doctor"></i>
      </div>
      <h3>暂无比心内科医生在线</h3>
      <p>请稍后再试，或选择AI健康助手咨询</p>
    </div>

    <div v-else class="doctors-grid">
      <div
        v-for="(doctor, index) in doctors"
        :key="doctor.id"
        class="doctor-card"
        @click="selectDoctor(doctor)"
        :style="{ '--i': index }"
      >
        <div class="card-top">
          <div class="doctor-avatar">
            <img v-if="doctor.avatar" :src="doctor.avatar" :alt="doctor.name" />
            <div v-else class="avatar-placeholder">
              <i class="fa-solid fa-user-doctor"></i>
            </div>
          </div>
          <div class="doctor-meta">
            <h3>{{ doctor.name }}</h3>
            <span class="doctor-title">{{ doctor.title }}</span>
          </div>
          <span :class="['status-badge', doctor.status || 'ONLINE']">
            <span class="status-dot"></span>
            {{ doctor.status === 'ONLINE' ? '在线' : doctor.status === 'BUSY' ? '忙碌' : '离线' }}
          </span>
        </div>

        <div class="card-body">
          <div class="info-row">
            <i class="fa-solid fa-hospital"></i>
            <span>{{ doctor.department }}</span>
          </div>
          <div class="info-row">
            <i class="fa-solid fa-stethoscope"></i>
            <span>{{ doctor.expertise }}</span>
          </div>
        </div>

        <div class="card-footer">
          <span class="consult-count">
            <i class="fa-solid fa-users"></i>
            接诊 {{ doctor.currentConsultCount || 0 }}/{{ doctor.maxConsultCount || 3 }}
          </span>
          <button class="consult-btn">
            立即咨询
            <i class="fa-solid fa-arrow-right"></i>
          </button>
        </div>
      </div>
    </div>

    <div class="ai-suggestion">
      <span>等待时间较长？</span>
      <router-link to="/patient/ai" class="ai-link">
        <i class="fa-solid fa-robot"></i>
        先与AI健康助手聊聊
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDoctorStore } from '@/stores/doctor'
import { useConsultStore } from '@/stores/consult'
import axios from 'axios'

const router = useRouter()
const doctorStore = useDoctorStore()
const consultStore = useConsultStore()

const doctors = ref([])
const loading = ref(true)

onMounted(async () => {
  await loadDoctors()
})

const loadDoctors = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/doctor/list')
    if (res.data.code === '0') {
      doctors.value = res.data.data
    }
  } catch (error) {
    console.error('Failed to load doctors:', error)
  } finally {
    loading.value = false
  }
}

const selectDoctor = async (doctor) => {
  const sessionId = await consultStore.createSession(
    getPatientId(),
    doctor.id,
    'CONSULT'
  )

  if (sessionId) {
    router.push(`/patient/queue/${doctor.id}`)
  }
}

const getPatientId = () => {
  return localStorage.getItem('user_id') || Math.floor(Math.random() * 10000) + 1000
}
</script>

<style scoped>
.doctor-list-page {
  padding: 0;
}

.page-header {
  margin-bottom: 28px;
}

.page-header h2 {
  font-size: var(--text-3xl);
  color: var(--color-text-primary);
  margin: 0 0 8px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.page-header p {
  color: var(--color-text-tertiary);
  margin: 0;
  font-size: var(--text-base);
}

/* ─── Skeleton Loading ─── */
.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.skeleton-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 24px;
  border: 1px solid var(--color-border);
}

.skeleton-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.skeleton-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
}

.skeleton-title {
  height: 20px;
  width: 120px;
  border-radius: var(--radius-sm);
}

.skeleton-line {
  height: 14px;
  width: 100%;
  margin-bottom: 10px;
}

.skeleton-line.short {
  width: 60%;
}

/* ─── Empty State ─── */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
}

.empty-icon-wrap {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
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
  font-size: 32px;
  color: var(--color-text-tertiary);
}

.empty-state h3 {
  color: var(--color-text-primary);
  margin: 0 0 8px;
  font-size: var(--text-lg);
  font-weight: 600;
}

.empty-state p {
  color: var(--color-text-tertiary);
  margin: 0;
  font-size: var(--text-sm);
}

/* ─── Doctor Grid ─── */
.doctors-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.doctor-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 24px;
  cursor: pointer;
  transition: transform var(--duration-normal) var(--ease-smooth),
              box-shadow var(--duration-normal) var(--ease-smooth),
              border-color var(--duration-normal) var(--ease-smooth);
  border: 1px solid var(--color-border);
  position: relative;
  overflow: hidden;
}

.doctor-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--color-brand), transparent);
  opacity: 0;
  transform: scaleX(0.3);
  transition: opacity var(--duration-normal) var(--ease-smooth), transform var(--duration-normal) var(--ease-smooth);
}

.doctor-card:hover::before {
  opacity: 1;
  transform: scaleX(1);
}

.doctor-card:hover {
  border-color: var(--color-brand-subtle);
  box-shadow: 0 8px 30px rgba(0,0,0,0.06), 0 0 0 1px rgba(46,184,114,0.1);
  transform: translateY(-3px);
}

.doctor-card:active {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.doctor-avatar {
  width: 64px;
  height: 64px;
  flex-shrink: 0;
}

.doctor-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  transition: transform var(--duration-normal) var(--ease-smooth);
}

.doctor-card:hover .doctor-avatar img {
  transform: scale(1.05);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--color-brand-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform var(--duration-normal) var(--ease-smooth);
}

.doctor-card:hover .avatar-placeholder {
  transform: scale(1.05);
}

.avatar-placeholder i {
  font-size: 28px;
  color: var(--color-brand);
}

.doctor-meta {
  flex: 1;
  min-width: 0;
}

.doctor-meta h3 {
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 4px;
}

.doctor-title {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 500;
  flex-shrink: 0;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.status-badge.ONLINE {
  background: var(--color-success-bg);
  color: var(--color-success);
}

.status-badge.ONLINE .status-dot {
  animation: breathe 2s ease-in-out infinite;
}

.status-badge.BUSY {
  background: var(--color-warning-bg);
  color: var(--color-warning);
}

.status-badge.OFFLINE {
  background: var(--color-surface-tertiary);
  color: var(--color-text-tertiary);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.card-body {
  padding: 16px 0;
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  transition: color var(--duration-fast) var(--ease-out);
}

.doctor-card:hover .info-row {
  color: var(--color-text-primary);
}

.info-row i {
  color: var(--color-brand);
  width: 18px;
  text-align: center;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.consult-count {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.consult-count i {
  color: var(--color-brand);
}

.consult-btn {
  padding: 8px 18px;
  background: var(--color-brand);
  color: var(--color-text-inverse);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
  display: flex;
  align-items: center;
  gap: 6px;
}

.consult-btn:hover {
  background: var(--color-brand-hover);
  box-shadow: 0 6px 20px rgba(46, 184, 114, 0.3);
  transform: translateY(-2px) scale(1.02);
}

.consult-btn:active {
  transform: translateY(0) scale(0.97);
  box-shadow: 0 2px 8px rgba(46, 184, 114, 0.2);
}

.doctor-card:hover .consult-btn {
  box-shadow: 0 4px 12px rgba(46, 184, 114, 0.2);
}

/* ─── AI Suggestion ─── */
.ai-suggestion {
  text-align: center;
  margin-top: 32px;
  padding: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  transition: border-color var(--duration-normal) var(--ease-smooth), box-shadow var(--duration-normal) var(--ease-smooth);
}

.ai-suggestion:hover {
  border-color: var(--color-brand-subtle);
  box-shadow: var(--shadow-glow);
}

.ai-suggestion span {
  margin-right: 8px;
}

.ai-link {
  color: var(--color-brand);
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: gap var(--duration-fast) var(--ease-out);
}

.ai-link:hover {
  gap: 10px;
}
</style>

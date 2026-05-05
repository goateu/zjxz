<template>
  <div class="home-layout">
    <header class="header" :class="{ scrolled: isScrolled }">
      <div class="header-inner">
        <div class="header-left">
          <img src="@/assets/logo1.png" alt="之江小智" class="logo" />
          <span class="title">之江小智</span>
          <span class="title-divider"></span>
          <span class="subtitle">在线问诊</span>
        </div>
        <nav class="header-nav">
          <button
            :class="['nav-btn', { active: activeTab === 'ai' }]"
            @click="switchTab('ai')"
          >
            <i class="fa-solid fa-robot"></i>
            AI健康助手
          </button>
          <button
            :class="['nav-btn', { active: activeTab === 'doctors' }]"
            @click="switchTab('doctors')"
          >
            <i class="fa-solid fa-user-doctor"></i>
            在线问诊
          </button>
        </nav>
        <div class="header-right">
          <div class="user-info" v-if="userInfo">
            <i class="fa-solid fa-user-circle"></i>
            <span>{{ userInfo.realName }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout">
            <i class="fa-solid fa-right-from-bracket"></i>
            退出
          </button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const activeTab = ref('ai')
const userInfo = ref(null)
const isScrolled = ref(false)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 4
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  const stored = localStorage.getItem('user_info')
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

watch(() => route.path, (path) => {
  if (path.includes('ai')) {
    activeTab.value = 'ai'
  } else if (path.includes('doctors') || path.includes('queue') || path.includes('chat')) {
    activeTab.value = 'doctors'
  }
}, { immediate: true })

const switchTab = (tab) => {
  if (tab === 'ai') {
    router.push('/patient/ai')
  } else {
    router.push('/patient/doctors')
  }
}

const handleLogout = () => {
  localStorage.removeItem('user_id')
  localStorage.removeItem('user_info')
  router.push('/patient/login')
}
</script>

<style scoped>
.home-layout {
  min-height: 100vh;
  background: var(--color-surface-secondary);
  display: flex;
  flex-direction: column;
}

.header {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  background: rgba(255, 255, 255, 0.82);
  transition: box-shadow var(--duration-normal) var(--ease-smooth);
}

.header.scrolled {
  box-shadow: 0 1px 8px rgba(0,0,0,0.04);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 32px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  transition: transform var(--duration-normal) var(--ease-smooth);
}

.logo:hover {
  transform: scale(1.08);
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  letter-spacing: 0.5px;
}

.title-divider {
  width: 1px;
  height: 20px;
  background: var(--color-border);
}

.subtitle {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
}

.header-nav {
  display: flex;
  gap: 4px;
}

.nav-btn {
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
}

.nav-btn:hover {
  background: var(--color-surface-tertiary);
  color: var(--color-text-primary);
  transform: translateY(-1px);
}

.nav-btn:active {
  transform: translateY(0) scale(0.97);
}

.nav-btn.active {
  background: var(--color-brand);
  color: var(--color-text-inverse);
  box-shadow: 0 2px 8px rgba(46, 184, 114, 0.25);
}

.nav-btn.active::after {
  display: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: var(--color-surface-tertiary);
  border-radius: var(--radius-full);
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  transition: background var(--duration-fast) var(--ease-out);
}

.user-info:hover {
  background: var(--color-border);
}

.user-info i {
  color: var(--color-brand);
}

.logout-btn {
  padding: 6px 14px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
  display: flex;
  align-items: center;
  gap: 6px;
}

.logout-btn:hover {
  border-color: var(--color-error);
  color: var(--color-error);
  background: var(--color-error-bg);
}

.logout-btn:active {
  transform: scale(0.97);
}

.main-content {
  flex: 1;
  padding: 32px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* ─── Page Transitions ─── */
.page-enter-active {
  animation: fade-in-up 0.35s var(--ease-smooth);
}

.page-leave-active {
  animation: fade-in 0.2s var(--ease-smooth) reverse;
}
</style>

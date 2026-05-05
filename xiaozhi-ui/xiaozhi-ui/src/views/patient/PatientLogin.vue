<template>
  <div class="login-page">
    <div class="brand-panel">
      <div class="brand-content">
        <div class="brand-logo">
          <img src="@/assets/logo1.png" alt="之江小智" />
        </div>
        <h1 class="brand-title">之江小智</h1>
        <p class="brand-subtitle">智能医疗问诊平台</p>
        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>三甲医院在线问诊</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>AI健康助手7×24小时</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>专业医生极速响应</span>
          </div>
        </div>
        <div class="brand-bg-shapes">
          <div class="shape s1"></div>
          <div class="shape s2"></div>
          <div class="shape s3"></div>
        </div>
      </div>
    </div>

    <div class="form-panel">
      <div class="form-container">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的账号开始问诊</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="field">
            <label>用户名</label>
            <el-input
              v-model="username"
              placeholder="请输入用户名"
              size="large"
            ></el-input>
          </div>

          <div class="field">
            <label>密码</label>
            <el-input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
            ></el-input>
          </div>

          <div v-if="errorMessage" class="message error">
            <i class="fa-solid fa-circle-exclamation"></i>
            {{ errorMessage }}
          </div>

          <button type="submit" class="btn-primary" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            <span v-else>登录</span>
          </button>

          <div class="footer-link">
            还没有账号？
            <router-link to="/patient/register">立即注册</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  if (!username.value || !password.value) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const res = await axios.post('http://localhost:8080/user/login', null, {
      params: { username: username.value, password: password.value }
    })
    if (res.data.code === '0') {
      localStorage.setItem('user_id', res.data.data.id)
      localStorage.setItem('user_info', JSON.stringify(res.data.data))
      router.push('/patient/ai')
    } else {
      errorMessage.value = res.data.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = '登录出错，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: var(--color-surface-secondary);
}

/* ─── Brand Panel — refined dark green ─── */
.brand-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #0d2b1d 0%, #1a4a35 50%, #0f3d28 100%);
  position: relative;
  overflow: hidden;
  padding: 48px;
}

.brand-content {
  position: relative;
  z-index: 10;
  text-align: left;
  max-width: 380px;
  animation: fade-in-up 0.8s var(--ease-smooth);
}

.brand-logo {
  margin-bottom: 24px;
  animation: fade-in-up 0.6s var(--ease-smooth);
}

.brand-logo img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(46, 184, 114, 0.3);
  transition: transform var(--duration-normal) var(--ease-smooth), box-shadow var(--duration-normal) var(--ease-smooth);
}

.brand-logo img:hover {
  transform: scale(1.05);
  box-shadow: 0 0 30px rgba(46, 184, 114, 0.2);
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 8px;
  letter-spacing: -0.5px;
  animation: fade-in-up 0.6s var(--ease-smooth) 0.1s both;
}

.brand-subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0 0 40px;
  animation: fade-in-up 0.6s var(--ease-smooth) 0.15s both;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  animation: fade-in-up 0.5s var(--ease-smooth) both;
}

.feature-item:nth-child(1) { animation-delay: 0.25s; }
.feature-item:nth-child(2) { animation-delay: 0.32s; }
.feature-item:nth-child(3) { animation-delay: 0.39s; }

.feature-item:hover {
  color: rgba(255, 255, 255, 0.95);
}

.feature-item span {
  transition: transform var(--duration-fast) var(--ease-out);
}

.feature-item:hover span {
  transform: translateX(4px);
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-brand);
  flex-shrink: 0;
  animation: breathe 2s ease-in-out infinite;
}

.brand-bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
}

.s1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(46, 184, 114, 0.06) 0%, transparent 70%);
  top: -150px;
  right: -120px;
  animation: float-slow 8s var(--ease-smooth) infinite;
}

.s2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(46, 184, 114, 0.04) 0%, transparent 70%);
  bottom: -80px;
  left: -100px;
  animation: float-slow 10s var(--ease-smooth) infinite reverse;
}

.s3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(46, 184, 114, 0.05) 0%, transparent 70%);
  top: 35%;
  right: 5%;
  animation: float-slow 7s var(--ease-smooth) infinite 2s;
}

/* ─── Form Panel ─── */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: var(--color-surface);
}

.form-container {
  width: 100%;
  max-width: 380px;
  animation: fade-in-up 0.7s var(--ease-smooth) 0.15s both;
}

.form-header {
  margin-bottom: 36px;
}

.form-header h2 {
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 8px;
  letter-spacing: -0.5px;
  animation: fade-in-up 0.5s var(--ease-smooth) 0.2s both;
}

.form-header p {
  color: var(--color-text-tertiary);
  margin: 0;
  font-size: var(--text-base);
  animation: fade-in-up 0.5s var(--ease-smooth) 0.25s both;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field label {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  font-weight: 500;
  transition: color var(--duration-fast) var(--ease-out);
}

.field:focus-within label {
  color: var(--color-brand);
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--color-border) !important;
  transition: box-shadow var(--duration-normal) var(--ease-smooth), background var(--duration-normal) var(--ease-smooth);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-border-hover) !important;
  background: var(--color-surface-secondary);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--color-brand) !important;
  background: var(--color-surface);
}

:deep(.el-input__inner) {
  height: 44px;
  caret-color: var(--color-brand);
}

.message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  animation: slide-in-right 0.3s var(--ease-smooth);
}

.message.error {
  background: var(--color-error-bg);
  color: var(--color-error);
}

.btn-primary {
  width: 100%;
  padding: 14px;
  background: var(--color-brand);
  color: var(--color-text-inverse);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-smooth);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-brand-hover);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(46, 184, 114, 0.3);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 2px 8px rgba(46, 184, 114, 0.2);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

.footer-link {
  text-align: center;
  color: var(--color-text-tertiary);
  font-size: var(--text-sm);
  animation: fade-in-up 0.5s var(--ease-smooth) 0.3s both;
}

.footer-link a {
  color: var(--color-brand);
  font-weight: 500;
  text-decoration: none;
  transition: color var(--duration-fast) var(--ease-out);
  position: relative;
}

.footer-link a::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 0;
  height: 1px;
  background: var(--color-brand);
  transition: width var(--duration-normal) var(--ease-smooth);
}

.footer-link a:hover {
  color: var(--color-brand-hover);
}

.footer-link a:hover::after {
  width: 100%;
}

/* ─── Responsive ─── */
@media (max-width: 768px) {
  .login-page {
    flex-direction: column;
  }
  .brand-panel {
    padding: 32px 24px;
    min-height: 240px;
  }
  .brand-content {
    text-align: center;
    max-width: 100%;
  }
  .brand-features {
    align-items: center;
  }
  .form-panel {
    padding: 32px 24px;
  }
  .brand-title {
    font-size: 28px;
  }
}
</style>

<template>
  <div class="register-page">
    <div class="brand-panel">
      <div class="brand-content">
        <div class="brand-logo">
          <img src="@/assets/logo1.png" alt="之江小智" />
        </div>
        <h1 class="brand-title">之江小智</h1>
        <p class="brand-subtitle">智能医疗问诊平台</p>
        <div class="brand-testimonial">
          <p>"在线问诊太方便了，不用去医院排队，医生回复也很专业。"</p>
          <span class="testimonial-author">—— 真实用户评价</span>
        </div>
        <div class="brand-bg-shapes">
          <div class="shape s1"></div>
          <div class="shape s2"></div>
        </div>
      </div>
    </div>

    <div class="form-panel">
      <div class="form-container">
        <div class="form-header">
          <h2>创建账号</h2>
          <p>填写信息注册，开始在线问诊</p>
        </div>

        <form @submit.prevent="handleRegister" class="register-form">
          <div class="form-row">
            <div class="field flex-1">
              <label>真实姓名</label>
              <el-input v-model="form.realName" placeholder="姓名" size="large"></el-input>
            </div>
            <div class="field flex-1">
              <label>用户名</label>
              <el-input v-model="form.username" placeholder="用户名" size="large"></el-input>
            </div>
          </div>

          <div class="field">
            <label>密码</label>
            <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password></el-input>
          </div>

          <div class="field">
            <label>身份证号</label>
            <el-input v-model="form.idCard" placeholder="请输入18位身份证号" size="large"></el-input>
          </div>

          <div class="field">
            <label>手机号</label>
            <el-input v-model="form.phone" placeholder="请输入手机号" size="large"></el-input>
          </div>

          <div v-if="errorMessage" class="message error">
            <i class="fa-solid fa-circle-exclamation"></i>
            {{ errorMessage }}
          </div>

          <div v-if="successMessage" class="message success">
            <i class="fa-solid fa-circle-check"></i>
            {{ successMessage }}
          </div>

          <button type="submit" class="btn-primary" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            <span v-else>注册</span>
          </button>

          <div class="footer-link">
            已有账号？
            <router-link to="/patient/login">立即登录</router-link>
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

const form = ref({
  realName: '',
  username: '',
  password: '',
  idCard: '',
  phone: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const handleRegister = async () => {
  if (!form.value.realName || !form.value.username || !form.value.password) {
    errorMessage.value = '请填写必填项'
    return
  }

  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const res = await axios.post(__API_BASE_URL__ + '/user/register', form.value)
    if (res.data.code === '0') {
      successMessage.value = '注册成功！即将跳转登录'
      setTimeout(() => {
        router.push('/patient/login')
      }, 1500)
    } else {
      errorMessage.value = res.data.message || '注册失败'
    }
  } catch (error) {
    errorMessage.value = '注册出错，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  background: var(--color-surface-secondary);
}

/* ─── Brand Panel ─── */
.brand-panel {
  width: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #0d2b1d 0%, #1a4a35 50%, #0f3d28 100%);
  position: relative;
  overflow: hidden;
  padding: 48px;
  flex-shrink: 0;
}

.brand-content {
  position: relative;
  z-index: 10;
  text-align: left;
  max-width: 320px;
  animation: fade-in-up 0.8s var(--ease-smooth);
}

.brand-logo {
  margin-bottom: 24px;
  animation: fade-in-up 0.5s var(--ease-smooth);
}

.brand-logo img {
  width: 56px;
  height: 56px;
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
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 6px;
  letter-spacing: -0.5px;
  animation: fade-in-up 0.6s var(--ease-smooth) 0.08s both;
}

.brand-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0 0 32px;
  animation: fade-in-up 0.6s var(--ease-smooth) 0.12s both;
}

.brand-testimonial {
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-md);
  padding: 20px;
}

.brand-testimonial p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  line-height: 1.7;
  margin: 0 0 8px;
  font-style: italic;
}

.testimonial-author {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
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
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(46, 184, 114, 0.06) 0%, transparent 70%);
  top: -100px;
  right: -80px;
  animation: float-slow 8s var(--ease-smooth) infinite;
}

.s2 {
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(46, 184, 114, 0.04) 0%, transparent 70%);
  bottom: -70px;
  left: -50px;
  animation: float-slow 9s var(--ease-smooth) infinite reverse;
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
  max-width: 440px;
  animation: fade-in-up 0.7s var(--ease-smooth) 0.1s both;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 8px;
  letter-spacing: -0.5px;
  animation: fade-in-up 0.5s var(--ease-smooth) 0.15s both;
}

.form-header p {
  color: var(--color-text-tertiary);
  margin: 0;
  font-size: var(--text-base);
  animation: fade-in-up 0.5s var(--ease-smooth) 0.2s both;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.flex-1 {
  flex: 1;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
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

.message.success {
  background: var(--color-success-bg);
  color: var(--color-success);
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
  margin-top: 4px;
  overflow: hidden;
  position: relative;
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
  animation: fade-in-up 0.5s var(--ease-smooth) 0.25s both;
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
  .register-page {
    flex-direction: column;
  }
  .brand-panel {
    width: 100%;
    padding: 32px 24px;
    min-height: 200px;
  }
  .brand-content {
    text-align: center;
    max-width: 100%;
  }
  .form-panel {
    padding: 32px 24px;
  }
  .form-row {
    flex-direction: column;
    gap: 16px;
  }
  .brand-title {
    font-size: 28px;
  }
}
</style>

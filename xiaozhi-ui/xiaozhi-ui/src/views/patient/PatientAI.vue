<template>
  <div class="patient-ai">
    <div class="ai-intro">
      <div class="intro-icon">
        <i class="fa-solid fa-robot"></i>
      </div>
      <h2>AI健康助手</h2>
      <p>基于医疗AI大模型，7×24小时为您解答健康问题</p>
    </div>
    <ChatWindow />
  </div>
</template>

<script setup>
import ChatWindow from '@/components/ChatWindow.vue'
</script>

<style scoped>
.patient-ai {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: calc(100vh - 140px);
}

.ai-intro {
  text-align: center;
  padding: 24px 20px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  transition: border-color var(--duration-normal) var(--ease-smooth), box-shadow var(--duration-normal) var(--ease-smooth);
}

.ai-intro:hover {
  border-color: var(--color-brand-subtle);
  box-shadow: var(--shadow-glow);
}

.intro-icon {
  width: 56px;
  height: 56px;
  background: var(--color-brand);
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
  transition: transform var(--duration-normal) var(--ease-smooth), box-shadow var(--duration-normal) var(--ease-smooth);
}

.ai-intro:hover .intro-icon {
  transform: scale(1.05);
  box-shadow: 0 0 20px rgba(46, 184, 114, 0.2);
}

.intro-icon i {
  font-size: 28px;
  color: white;
}

.ai-intro h2 {
  color: var(--color-text-primary);
  margin: 0 0 4px;
  font-size: var(--text-xl);
  font-weight: 700;
  transition: color var(--duration-fast) var(--ease-out);
}

.ai-intro:hover h2 {
  color: var(--color-brand);
}

.ai-intro p {
  color: var(--color-text-tertiary);
  margin: 0;
  font-size: var(--text-sm);
}
</style>

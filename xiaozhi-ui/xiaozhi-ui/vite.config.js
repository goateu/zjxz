import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'

export default defineConfig({
  plugins: [
    vue()
  ],
  server: {
    proxy: {
      '/api': {
        target: ' http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  define: {
    global: 'globalThis',
    __API_BASE_URL__: JSON.stringify(process.env.VITE_API_URL || 'https://4b85921d.r11.vip.cpolar.cn'),
    __WS_URL__: JSON.stringify(process.env.VITE_WS_URL || 'https://4b85921d.r11.vip.cpolar.cn/ws'),
  },
})
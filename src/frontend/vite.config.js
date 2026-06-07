import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 将 @ 映射到 src 目录
      '@': path.resolve(__dirname, 'src') 
    }
  },
  server: {
    port: 5174,
    proxy: {
      '/api/user': {
        target: 'http://localhost:8989/user-service',
        changeOrigin: true
      },
      '/api/product': {
        target: 'http://localhost:8989/product-service',
        changeOrigin: true
      },
      '/api/order': {
        target: 'http://localhost:8989/order-service',
        changeOrigin: true
      },
      '/api/payment': {
        target: 'http://localhost:8989/payment-service',
        changeOrigin: true
      }
    }
  }
})
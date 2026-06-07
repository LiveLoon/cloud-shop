import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5173,
    proxy: {
      // 用户服务代理
      '/user-service': {
        target: 'http://localhost:8989',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/user-service/, '/user-service')
      },
      // 商品服务代理（假设商品服务端口 8989 /product-service）
      '/product-service': {
        target: 'http://localhost:8989',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/product-service/, '/product-service')
      },
      // 订单服务代理
      '/order-service': {
        target: 'http://localhost:8989',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/order-service/, '/order-service')
      },
      // 支付服务代理
      '/payment-service': {
        target: 'http://localhost:8989',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/payment-service/, '/payment-service')
      }
    }
  }
})
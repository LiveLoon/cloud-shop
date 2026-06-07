<template>
  <div class="login-container">
    <!-- 装饰背景 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="login-wrapper">
      <el-card class="login-card" shadow="never">
        <div class="brand">
          <div class="logo">
            <el-icon :size="48"><ShoppingBag /></el-icon>
          </div>
          <h1>Cloud Shop</h1>
          <p>欢迎回来，请登录您的账号</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              size="large"
              prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              @click="handleLogin"
              :loading="loading"
              class="login-button"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
          <div class="register-link">
            <el-link type="info" @click="$router.push('/register')" :underline="false">
              没有账号？立即注册
            </el-link>
          </div>
        </el-form>
      </el-card>

      <div class="footer-copyright">
        © 2026 Cloud Shop. All rights reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { ShoppingBag } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  const result = await userStore.login(form.username, form.password)
  loading.value = false
  if (result.success) {
    ElMessage.success('登录成功')
    router.push('/')
  } else {
    ElMessage.error(result.message)
  }
}
</script>

<style scoped>
/* 全局重置 & 容器 */
.login-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7ff 0%, #eef2ff 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  overflow-x: hidden;
}

/* 动态几何背景 */
.bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: floatShape 20s infinite ease-in-out;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.4), rgba(64, 158, 255, 0.05));
  top: -200px;
  left: -150px;
  animation-delay: 0s;
}

.shape-2 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(114, 46, 209, 0.35), rgba(114, 46, 209, 0.02));
  bottom: -250px;
  right: -200px;
  animation-delay: -5s;
  animation-duration: 25s;
}

.shape-3 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.4), rgba(0, 212, 255, 0.05));
  top: 40%;
  left: 25%;
  animation-delay: -10s;
  animation-duration: 18s;
  filter: blur(70px);
}

@keyframes floatShape {
  0% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -40px) scale(1.05);
  }
  66% {
    transform: translate(-20px, 30px) scale(0.95);
  }
  100% {
    transform: translate(0, 0) scale(1);
  }
}

/* 外层包装（卡片 + 版权） */
.login-wrapper {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 500px;
  margin: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 卡片主样式 — 玻璃质感 + 精致阴影 */
.login-card {
  width: 100%;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(2px);
  border-radius: 40px;
  box-shadow: 0 25px 45px -12px rgba(0, 0, 0, 0.2), 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.login-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 32px 50px -16px rgba(0, 0, 0, 0.25);
}

/* 覆盖 Element Plus 卡片 body 内边距 */
:deep(.el-card__body) {
  padding: 40px 36px 48px;
}

/* 品牌区域 */
.brand {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(145deg, #ffffff, #f3f6fc);
  border-radius: 28px;
  box-shadow: 0 12px 18px -8px rgba(0, 80, 200, 0.15), inset 0 1px 0 rgba(255,255,255,0.8);
  margin-bottom: 20px;
  transition: all 0.2s ease;
}

.logo .el-icon {
  color: #2266dc;
  filter: drop-shadow(0 2px 4px rgba(34,102,220,0.2));
}

.brand h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #1f2b48, #2c3e66);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  letter-spacing: -0.3px;
}

.brand p {
  font-size: 15px;
  color: #5b6e8c;
  margin: 0;
  font-weight: 450;
}

/* 表单样式优化 */
.login-form {
  margin-top: 8px;
}

/* 表单项间距 */
:deep(.el-form-item) {
  margin-bottom: 24px;
}

/* 输入框：圆润、柔和、聚焦效果 */
:deep(.el-input__wrapper) {
  background-color: #f8fafd;
  border-radius: 20px;
  padding: 4px 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), inset 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: all 0.2s ease;
  border: 1px solid #e2e8f0;
}

:deep(.el-input__wrapper:hover) {
  border-color: #b9c8ff;
  background-color: #ffffff;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15);
  background-color: #ffffff;
}

:deep(.el-input__inner) {
  font-size: 15px;
  font-weight: 450;
  color: #1e2a3e;
}

:deep(.el-input__prefix-inner) {
  color: #8f9bb3;
}

/* 大号输入框高度更舒适 */
:deep(.el-input--large .el-input__wrapper) {
  padding: 6px 16px;
}

/* 登录按钮：渐变 + 现代阴影 */
.login-button {
  width: 100%;
  background: linear-gradient(105deg, #2266dc, #3b82f6);
  border: none;
  border-radius: 28px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  height: 52px;
  margin-top: 8px;
  box-shadow: 0 8px 20px -6px rgba(59, 130, 246, 0.4);
  transition: all 0.25s ease;
}

.login-button:hover {
  background: linear-gradient(105deg, #1a56c1, #2b6ed7);
  transform: scale(0.98);
  box-shadow: 0 6px 14px -4px rgba(59, 130, 246, 0.5);
}

.login-button:active {
  transform: scale(0.96);
}

/* 注册链接 */
.register-link {
  text-align: center;
  margin-top: 12px;
  font-size: 14px;
}

.register-link :deep(.el-link) {
  font-size: 14px;
  color: #5b6e8c;
  font-weight: 500;
  transition: color 0.2s;
}

.register-link :deep(.el-link:hover) {
  color: #2266dc;
}

/* 底部版权 */
.footer-copyright {
  margin-top: 36px;
  text-align: center;
  font-size: 13px;
  color: #7c8ba0;
  letter-spacing: 0.3px;
  opacity: 0.8;
  font-weight: 450;
  transition: opacity 0.2s;
}

/* 响应式细节：移动端内缩 */
@media (max-width: 560px) {
  .login-wrapper {
    margin: 16px;
  }
  :deep(.el-card__body) {
    padding: 32px 24px 40px;
  }
  .brand h1 {
    font-size: 26px;
  }
  .logo {
    width: 70px;
    height: 70px;
  }
  .login-button {
    height: 48px;
    font-size: 15px;
  }
  .footer-copyright {
    margin-top: 28px;
    font-size: 12px;
  }
}

/* 优雅禁用滚动条挤压 */
@media (max-width: 420px) {
  :deep(.el-card__body) {
    padding: 28px 20px 36px;
  }
}
</style>
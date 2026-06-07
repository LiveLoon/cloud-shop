<template>
  <div class="profile-page">
    <!-- 装饰背景 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="profile-wrapper">
      <el-card class="profile-card" shadow="never">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="title-section">
            <el-icon class="title-icon"><User /></el-icon>
            <h2>个人资料</h2>
          </div>
          <p class="subtitle">管理您的个人信息和账户安全</p>
        </div>

        <!-- 头像区域（装饰） -->
        <div class="avatar-section">
          <div class="avatar-placeholder">
            <el-icon :size="56"><UserFilled /></el-icon>
          </div>
          <div class="avatar-info">
            <h3>{{ form.username || '用户' }}</h3>
            <p>ID: {{ form.id || '--' }}</p>
          </div>
        </div>

        <!-- 表单区域 -->
        <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-width="100px"
          class="profile-form"
        >
          <el-form-item label="用户ID">
            <el-input v-model="form.id" disabled class="disabled-input" />
          </el-form-item>

          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="form.realName"
              placeholder="真实姓名（选填）"
              prefix-icon="Postcard"
              clearable
            />
          </el-form-item>

          <el-form-item label="新密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="留空则不修改密码"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword" v-if="form.password">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <div class="button-group">
              <el-button type="primary" size="large" @click="handleUpdate" :loading="loading" round>
                保存修改
              </el-button>
              <el-button size="large" @click="resetForm" round>
                重置
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>

      <div class="footer-copyright">
        © 2026 Cloud Shop. All rights reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Postcard, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  id: null,
  username: '',
  realName: '',
  password: '',
  confirmPassword: ''
})

// 自定义确认密码校验
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' }
  ],
  realName: [
    { max: 50, message: '真实姓名不能超过 50 字符', trigger: 'blur' }
  ],
  password: [
    { min: 4, max: 20, message: '密码长度 4-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  const userId = userStore.userId
  if (!userId) {
    ElMessage.error('未登录')
    return
  }
  try {
    const data = await userApi.getUserById(userId)
    form.id = data.id
    form.username = data.username
    form.realName = data.realName || ''
    // 密码字段不显示，保持为空
    form.password = ''
    form.confirmPassword = ''
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  loadUserInfo() // 重新加载原始数据
}

// 提交更新
const handleUpdate = async () => {
  await formRef.value.validate()
  
  // 准备提交的数据（只包含需要更新的字段）
  const updateData = {
    id: form.id,
    username: form.username,
    realName: form.realName || null
  }
  
  // 如果填写了新密码，则包含 password 字段
  if (form.password) {
    updateData.password = form.password
  }
  
  loading.value = true
  try {
    const result = await userApi.updateUser(updateData)
    // 更新成功后同步更新 store 中的用户信息
    userStore.userInfo = result
    localStorage.setItem('userInfo', JSON.stringify(result))
    ElMessage.success('资料更新成功')
    // 清空密码输入框
    form.password = ''
    form.confirmPassword = ''
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
/* 整体容器背景 */
.profile-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 40px 24px;
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
  opacity: 0.5;
  animation: floatShape 20s infinite ease-in-out;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.35), rgba(64, 158, 255, 0.02));
  top: -200px;
  left: -150px;
  animation-delay: 0s;
}

.shape-2 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(114, 46, 209, 0.3), rgba(114, 46, 209, 0.01));
  bottom: -250px;
  right: -200px;
  animation-delay: -5s;
  animation-duration: 25s;
}

.shape-3 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.35), rgba(0, 212, 255, 0.02));
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

/* 外层包装 */
.profile-wrapper {
  position: relative;
  z-index: 2;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

/* 卡片主体 */
.profile-card {
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(2px);
  border-radius: 40px;
  box-shadow: 0 25px 45px -12px rgba(0, 0, 0, 0.15), 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  border: 1px solid rgba(255, 255, 255, 0.6);
  margin-bottom: 28px;
}

:deep(.el-card__body) {
  padding: 36px 32px 44px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 28px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 28px;
  color: #2266dc;
}

.page-header h2 {
  font-size: 26px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #1f2b48, #2c3e66);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  letter-spacing: -0.3px;
}

.subtitle {
  font-size: 14px;
  color: #5b6e8c;
  margin: 8px 0 0 0;
  padding-left: 40px;
}

/* 头像区域装饰 */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding: 16px 20px;
  background: linear-gradient(105deg, #f8fafd, #ffffff);
  border-radius: 32px;
  border: 1px solid #eef2ff;
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  background: linear-gradient(145deg, #eef2ff, #ffffff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  box-shadow: 0 8px 20px -8px rgba(59, 130, 246, 0.3);
}

.avatar-info h3 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1e2a3e;
}

.avatar-info p {
  margin: 0;
  font-size: 13px;
  color: #7c8ba0;
}

/* 表单样式 */
.profile-form {
  margin-top: 16px;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

/* 输入框圆润风格 */
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

/* 禁用输入框透明效果 */
.disabled-input :deep(.el-input__wrapper) {
  background-color: #f1f5f9;
  border-color: #e2e8f0;
  opacity: 0.8;
}

.disabled-input :deep(.el-input__inner) {
  color: #7c8ba0;
}

/* 表单项标签 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #2c3e50;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 18px;
  margin-top: 12px;
}

.button-group .el-button {
  padding: 10px 28px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.button-group .el-button--primary {
  background: linear-gradient(105deg, #2266dc, #3b82f6);
  border: none;
  box-shadow: 0 8px 18px -8px rgba(59, 130, 246, 0.4);
}

.button-group .el-button--primary:hover {
  background: linear-gradient(105deg, #1a56c1, #2b6ed7);
  transform: translateY(-2px);
  box-shadow: 0 12px 22px -10px rgba(59, 130, 246, 0.5);
}

.button-group .el-button--default {
  border: 1px solid #cbd5e1;
  color: #4b5e7c;
}

.button-group .el-button--default:hover {
  background: #f8fafd;
  border-color: #b9c8ff;
  transform: translateY(-2px);
}

.button-group .el-button:active {
  transform: translateY(1px);
}

/* 底部版权 */
.footer-copyright {
  text-align: center;
  font-size: 13px;
  color: #7c8ba0;
  letter-spacing: 0.3px;
  opacity: 0.8;
  font-weight: 450;
  margin-top: 20px;
}

/* 移动端适配 */
@media (max-width: 680px) {
  .profile-page {
    padding: 24px 20px;
  }
  :deep(.el-card__body) {
    padding: 28px 24px;
  }
  .page-header h2 {
    font-size: 22px;
  }
  .title-icon {
    font-size: 24px;
  }
  .subtitle {
    padding-left: 34px;
    font-size: 12px;
  }
  .avatar-section {
    gap: 14px;
    padding: 12px 16px;
  }
  .avatar-placeholder {
    width: 64px;
    height: 64px;
  }
  .avatar-info h3 {
    font-size: 18px;
  }
  .button-group {
    flex-direction: column;
    width: 100%;
  }
  .button-group .el-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .profile-page {
    padding: 20px 16px;
  }
  :deep(.el-card__body) {
    padding: 24px 18px;
  }
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  .avatar-info h3,
  .avatar-info p {
    text-align: center;
  }
}
</style>
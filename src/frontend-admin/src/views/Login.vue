<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>Cloud Shop 管理后台</h2>
          <p>请登录以继续</p>
        </div>
      </template>
      <el-form :model="form" @submit.prevent="handleLogin" class="login-form">
        <el-form-item label="用户名">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            type="password"
            v-model="form.password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="login-button"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const adminStore = useAdminStore()
const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  try {
    await adminStore.login(form.value.username, form.value.password)
    ElMessage.success('登录成功')
    router.push('/products')
  } catch (err) {
    ElMessage.error(err.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}
.login-card {
  width: 450px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}
.card-header {
  text-align: center;
}
.card-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-weight: 600;
}
.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
.login-form {
  margin-top: 10px;
}
.login-button {
  width: 100%;
}
</style>
<template>
  <el-container class="layout">
    <el-header>
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">Cloud Shop</div>
        <div class="nav">
          <el-menu mode="horizontal" :router="true" background-color="#409EFF" text-color="#fff" active-text-color="#ffd04b">
            <el-menu-item index="/products">商品</el-menu-item>
            <el-menu-item index="/cart">
              <el-badge :value="cartCount" :hidden="cartCount === 0">购物车</el-badge>
            </el-menu-item>
            <el-menu-item index="/orders">我的订单</el-menu-item>
            <el-menu-item index="/profile">个人中心</el-menu-item>
            <el-menu-item @click="handleLogout">退出登录</el-menu-item>
          </el-menu>
        </div>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const cartCount = computed(() => cartStore.totalCount)

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}
.el-header {
  background-color: #409EFF;
  padding: 0;
  height: 60px;
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}
.logo {
  font-size: 24px;
  font-weight: bold;
  color: white;
  cursor: pointer;
}
.nav .el-menu {
  border-bottom: none;
  background: transparent;
}
.nav .el-menu-item {
  color: white !important;
}
</style>
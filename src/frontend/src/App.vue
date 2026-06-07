<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

// 初始化存储
const userStore = useUserStore()
const cartStore = useCartStore()
userStore.loadFromStorage()
cartStore.loadCart()
</script>

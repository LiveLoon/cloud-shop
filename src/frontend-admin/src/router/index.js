import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: Layout,
    redirect: '/products',
    meta: { requiresAuth: true },
    children: [
      { path: 'products', component: () => import('@/views/ProductList.vue') },
      { path: 'orders', component: () => import('@/views/OrderList.vue') },
      { path: 'users', component: () => import('@/views/UserList.vue') },
      { path: 'payments', component: () => import('@/views/PaymentList.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫（保持不变）
router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('admin_token')
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
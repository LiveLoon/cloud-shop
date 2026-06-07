import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置 NProgress
NProgress.configure({ showSpinner: false, speed: 500 })

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/products' },
      { path: 'products', name: 'ProductList', component: () => import('@/views/ProductList.vue') },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('@/views/Cart.vue') },
      { path: 'orders', name: 'OrderList', component: () => import('@/views/OrderList.vue') },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue') },
      { path: 'payment/:orderId', name: 'Payment', component: () => import('@/views/Payment.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
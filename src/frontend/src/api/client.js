import axios from 'axios'

// 创建各服务实例
export const userClient = axios.create({
  baseURL: import.meta.env.VITE_USER_SERVICE_URL,
  timeout: 10000
})

export const productClient = axios.create({
  baseURL: import.meta.env.VITE_PRODUCT_SERVICE_URL,
  timeout: 10000
})

export const orderClient = axios.create({
  baseURL: import.meta.env.VITE_ORDER_SERVICE_URL,
  timeout: 10000
})

export const paymentClient = axios.create({
  baseURL: import.meta.env.VITE_PAYMENT_SERVICE_URL,
  timeout: 30000
})

// 响应拦截器统一处理
const responseInterceptor = (response) => {
  if (response.data.code === 200) {
    return response.data.data
  }
  return Promise.reject(new Error(response.data.message || '请求失败'))
}

userClient.interceptors.response.use(responseInterceptor)
productClient.interceptors.response.use(responseInterceptor)
orderClient.interceptors.response.use(responseInterceptor)
paymentClient.interceptors.response.use(responseInterceptor)

// 错误处理
const errorHandler = (error) => {
  console.error('API Error:', error)
  throw error
}

userClient.interceptors.response.use(r => r, errorHandler)
productClient.interceptors.response.use(r => r, errorHandler)
orderClient.interceptors.response.use(r => r, errorHandler)
paymentClient.interceptors.response.use(r => r, errorHandler)
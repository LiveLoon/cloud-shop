import axios from 'axios'

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

// 统一响应拦截器
const responseInterceptor = (response) => {
  if (response.data.code === 200) {
    return response.data.data
  }
  return Promise.reject(new Error(response.data.message || '请求失败'))
}

const errorHandler = (error) => {
  console.error('API Error:', error)
  return Promise.reject(error)
}

userClient.interceptors.response.use(responseInterceptor, errorHandler)
productClient.interceptors.response.use(responseInterceptor, errorHandler)
orderClient.interceptors.response.use(responseInterceptor, errorHandler)
paymentClient.interceptors.response.use(responseInterceptor, errorHandler)
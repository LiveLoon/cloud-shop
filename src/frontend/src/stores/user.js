import { defineStore } from 'pinia'
import { userApi } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: null
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.userInfo,
    userId: (state) => state.userInfo?.id
  },
  
  actions: {
    async login(username, password) {
      try {
        const data = await userApi.login({ username, password })
        this.userInfo = data
        localStorage.setItem('userInfo', JSON.stringify(data))
        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },
    
    async register(username, password) {
      try {
        const data = await userApi.register({ username, password })
        this.userInfo = data
        localStorage.setItem('userInfo', JSON.stringify(data))
        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },
    
    async updateUser(userData) {
      try {
        const data = await userApi.updateUser(userData)
        this.userInfo = data
        localStorage.setItem('userInfo', JSON.stringify(data))
        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },
    
    logout() {
      this.userInfo = null
      localStorage.removeItem('userInfo')
    },
    
    loadFromStorage() {
      const stored = localStorage.getItem('userInfo')
      if (stored) {
        this.userInfo = JSON.parse(stored)
      }
    }
  }
})
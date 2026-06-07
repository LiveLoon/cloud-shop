import { defineStore } from 'pinia'
import { userApi } from '@/api/user'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    adminInfo: null,
    token: localStorage.getItem('admin_token') || ''
  }),
  actions: {
    async login(username, password) {
      try {
        const user = await userApi.login(username, password)
        // 简单判断：假设管理员 role 为 admin，可根据实际逻辑修改
        // 这里仅演示，实际可增加 role 字段
        this.adminInfo = user
        const fakeToken = btoa(`${username}:${Date.now()}`)
        this.token = fakeToken
        localStorage.setItem('admin_token', fakeToken)
        localStorage.setItem('admin_info', JSON.stringify(user))
        return true
      } catch (err) {
        console.error('登录失败', err)
        throw err
      }
    },
    logout() {
      this.adminInfo = null
      this.token = ''
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
    }
  }
})
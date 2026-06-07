import { userClient } from './client'

export const userApi = {
  // 管理员登录（复用普通用户登录接口）
  login(username, password) {
    return userClient.post('/login', { username, password })
  },

  // 获取用户详情
  getUserById(id) {
    return userClient.get(`/getUserById/${id}`)
  },

  // 更新用户信息
  updateUser(data) {
    return userClient.put('/update', data)
  },

  // 注册新用户（管理员也可用于添加用户）
  register(username, password) {
    return userClient.post('/register', { username, password })
  },

  // 分页查询用户列表（若后端未提供，可暂时用 getAllUsers 模拟）
  getUserList(params) {
    return userClient.get('/list', { params })
  }
}
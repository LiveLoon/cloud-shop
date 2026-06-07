import { userClient } from './client'

export const userApi = {
  // ✅ baseURL(/api/user) + /login = /api/user/login
  login(data) {
    return userClient.post('/login', data)
  },
  
  register(data) {
    return userClient.post('/register', data)
  },
  
  getUserById(id) {
    return userClient.get(`/getUserById/${id}`)
  },
  
  updateUser(data) {
    return userClient.put('/update', data)
  }
}
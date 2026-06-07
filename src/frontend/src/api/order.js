import { orderClient } from './client'

export const orderApi = {
  // 获取订单详情
  getOrderById(id) {
    return orderClient.get(`/${id}`)
  },
  
  // 获取用户订单列表（扩展接口）
  getOrderList(userId) {
    return orderClient.get('/list', { params: { userId } })
  },
  
  // 创建订单
  createOrder(data) {
    return orderClient.post('/add', data)
  },
  
  // 删除订单
  deleteOrder(id) {
    return orderClient.delete(`/${id}`)
  },

  // 获取用户订单列表（需后端实现）
  getUserOrderList(userId, page = 0, size = 10) {
    return orderClient.get('/userList', { params: { userId, page, size } })
  }
}
import { orderClient } from './client'

export const orderApi = {
  // 分页获取订单列表（假设有 /list 接口）
  getOrderList(params) {
    return orderClient.get('/list', { params })
  },

  // 获取订单详情
  getOrderById(id) {
    return orderClient.get(`/${id}`)
  },


  // 删除订单（物理删除）
  deleteOrder(id) {
    return orderClient.delete(`/${id}`)
  }
}
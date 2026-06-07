import { paymentClient } from './client'

export const paymentApi = {
  // 根据订单 ID 查询支付记录
  getPaymentByOrderId(orderId) {
    return paymentClient.get(`/order/${orderId}`)
  },

  // 根据支付 ID 查询
  getPaymentById(paymentId) {
    return paymentClient.get(`/${paymentId}`)
  }

  // 注：创建支付应由用户端发起，管理员只需查看
}
import { paymentClient } from './client'

export const paymentApi = {
  // 创建支付订单
  createPayment(data) {
    return paymentClient.post('/create', data)
  },
  
  // 根据支付ID查询支付详情
  getPaymentById(paymentId) {
    return paymentClient.get(`/${paymentId}`)
  },
  
  // 根据订单ID查询支付详情
  getPaymentByOrderId(orderId) {
    return paymentClient.get(`/order/${orderId}`)
  },

  updatePaymentByPaymentId(paymentId,payment){
    return paymentClient.put(`/${paymentId}`,payment)
  }
}
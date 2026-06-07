import { productClient } from './client'

export const productApi = {
  // 获取商品详情
  getProductById(id) {
    return productClient.get(`/${id}`)
  },
  
  // 获取商品列表（扩展接口，后端需实现）
  getProductList(params) {
    return productClient.get('/list', { params })
  },
  
  // 添加商品（管理员功能，暂不实现前端）
  addProduct(data) {
    return productClient.post('/add', data)
  },
  
  // 增加库存（管理员功能）
  updateInventory(productId, buyNum) {
    return productClient.put(`/updateInventory/${productId}/${buyNum}`)
  }
}
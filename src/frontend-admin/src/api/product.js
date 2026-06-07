import { productClient } from './client'

export const productApi = {
  // 分页查询商品列表（假设后端接口为 /list）
  getProductList(params = { page: 0, size: 10 }) {
    return productClient.get('/list', { params })
  },

  // 根据 ID 查询商品详情
  getProductById(id) {
    return productClient.get(`/${id}`)
  },

  // 新增商品
  addProduct(data) {
    return productClient.post('/add', data)
  },

  // 修改商品（部分更新，PUT /api/product/{id}）
  updateProduct(id, data) {
    return productClient.put(`/${id}`, data)
  },

  // 删除商品
  deleteProduct(id) {
    return productClient.delete(`/${id}`)
  },

  // 增加库存（补货）
  updateInventory(productId, buyNum) {
    return productClient.put(`/updateInventory/${productId}/${buyNum}`)
  }
}
<template>
  <div class="cart-page">
    <!-- 装饰背景 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="cart-wrapper">
      <el-card class="cart-card" shadow="never">
        <!-- 页面标题区 -->
        <div class="page-header">
          <div class="title-section">
            <el-icon class="title-icon"><ShoppingCart /></el-icon>
            <h2>我的购物车</h2>
          </div>
          <p class="subtitle">查看和编辑您即将购买的商品</p>
        </div>

        <!-- 购物车表格容器 -->
        <div class="table-container">
          <el-table
            :data="cartItems"
            style="width: 100%"
            stripe
            class="cart-table"
            empty-text="购物车还是空的，快去逛逛吧~"
          >
            <el-table-column prop="productName" label="商品名称" min-width="200">
              <template #default="{ row }">
                <div class="product-cell">
                  <div class="product-icon">
                    <el-icon><Goods /></el-icon>
                  </div>
                  <span class="product-name">{{ row.productName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="单价" width="120" align="center">
              <template #default="{ row }">
                <span class="unit-price">¥{{ row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="160" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.buyNum"
                  :min="1"
                  :max="999"
                  size="small"
                  controls-position="right"
                  @change="updateQuantity(row.productId, row.buyNum)"
                  class="quantity-input"
                />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="130" align="center">
              <template #default="{ row }">
                <span class="subtotal">¥{{ (row.price * row.buyNum).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" plain round @click="removeItem(row.productId)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 底部操作栏 -->
        <div class="cart-footer" v-if="cartItems.length > 0">
          <div class="footer-left">
            <el-button type="default" plain round @click="clearCart" class="clear-btn">
              <el-icon><Delete /></el-icon>
              清空购物车
            </el-button>
          </div>
          <div class="footer-right">
            <div class="total-info">
              <span class="total-label">总计：</span>
              <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <el-button type="primary" size="large" @click="createOrder" class="checkout-btn">
              去结算
            </el-button>
          </div>
        </div>

        <!-- 空购物车展示 -->
        <div v-else class="empty-cart">
          <el-icon :size="80"><ShoppingCart /></el-icon>
          <p>购物车还是空的</p>
          <el-button type="primary" round @click="$router.push('/products')">去逛逛</el-button>
        </div>
      </el-card>

      <div class="footer-copyright">
        © 2026 Cloud Shop. All rights reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Goods, Delete } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { orderApi } from '@/api/order'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const cartItems = computed(() => cartStore.cartItems)
const totalPrice = computed(() => cartStore.totalPrice)

// 更新商品数量
const updateQuantity = (productId, buyNum) => {
  cartStore.updateQuantity(productId, buyNum)
}

// 删除单个商品
const removeItem = (productId) => {
  ElMessageBox.confirm('确定要从购物车中移除此商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    roundButton: true
  }).then(() => {
    cartStore.removeItem(productId)
    ElMessage.success('已删除')
  }).catch(() => {})
}

// 清空购物车
const clearCart = () => {
  if (cartItems.value.length === 0) return
  ElMessageBox.confirm('清空后所有商品都将被移除，确认清空购物车吗？', '警告', {
    confirmButtonText: '确认清空',
    cancelButtonText: '取消',
    type: 'error',
    roundButton: true
  }).then(() => {
    cartStore.clearCart()
    ElMessage.success('购物车已清空')
  }).catch(() => {})
}

// 创建订单
const createOrder = async () => {
  if (!userStore.userId) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }

  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车是空的，无法结算')
    return
  }

  const orderData = {
    userId: userStore.userId,
    itemList: cartItems.value.map(item => ({
      productId: item.productId,
      buyNum: item.buyNum,
      productName: item.productName,
      price: item.price
    }))
  }

  try {
    await orderApi.createOrder(orderData)
    ElMessage.success('订单创建成功，即将跳转到订单列表')
    cartStore.clearCart()
    router.push('/orders')
  } catch (error) {
    ElMessage.error(error.message || '订单创建失败，请稍后重试')
  }
}
</script>

<style scoped>
/* 整体容器背景 */
.cart-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 40px 24px;
  overflow-x: hidden;
}

/* 动态几何背景 */
.bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: floatShape 20s infinite ease-in-out;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.35), rgba(64, 158, 255, 0.02));
  top: -200px;
  left: -150px;
  animation-delay: 0s;
}

.shape-2 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(114, 46, 209, 0.3), rgba(114, 46, 209, 0.01));
  bottom: -250px;
  right: -200px;
  animation-delay: -5s;
  animation-duration: 25s;
}

.shape-3 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.35), rgba(0, 212, 255, 0.02));
  top: 40%;
  left: 25%;
  animation-delay: -10s;
  animation-duration: 18s;
  filter: blur(70px);
}

@keyframes floatShape {
  0% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -40px) scale(1.05);
  }
  66% {
    transform: translate(-20px, 30px) scale(0.95);
  }
  100% {
    transform: translate(0, 0) scale(1);
  }
}

/* 外层包装 */
.cart-wrapper {
  position: relative;
  z-index: 2;
  max-width: 1300px;
  margin: 0 auto;
  width: 100%;
}

/* 卡片主体 */
.cart-card {
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(2px);
  border-radius: 40px;
  box-shadow: 0 25px 45px -12px rgba(0, 0, 0, 0.15), 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  border: 1px solid rgba(255, 255, 255, 0.6);
  margin-bottom: 28px;
}

:deep(.el-card__body) {
  padding: 36px 32px 40px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 28px;
  color: #2266dc;
}

.page-header h2 {
  font-size: 26px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #1f2b48, #2c3e66);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  letter-spacing: -0.3px;
}

.subtitle {
  font-size: 14px;
  color: #5b6e8c;
  margin: 8px 0 0 0;
  padding-left: 40px;
}

/* 表格容器支持滚动 */
.table-container {
  overflow-x: auto;
  border-radius: 20px;
}

/* 自定义表格样式 */
.cart-table {
  border-radius: 20px;
  overflow: hidden;
}

:deep(.cart-table .el-table__header-wrapper) {
  background: #f8fafd;
}

:deep(.cart-table .el-table__header th) {
  background: #f8fafd;
  color: #1e2a3e;
  font-weight: 600;
  font-size: 14px;
  padding: 16px 12px;
  border-bottom: 1px solid #e2e8f0;
}

:deep(.cart-table .el-table__row) {
  transition: background 0.2s ease;
}

:deep(.cart-table .el-table__row:hover) {
  background: #fefce8 !important;
}

:deep(.cart-table .el-table__body tr.el-table__row--striped) {
  background: #fafcff;
}

:deep(.cart-table .el-table__cell) {
  padding: 16px 12px;
  border-bottom: 1px solid #eff3f8;
}

/* 商品单元格 */
.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #eef2ff, #ffffff);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.product-name {
  font-weight: 500;
  color: #1e2a3e;
}

.unit-price {
  font-weight: 600;
  color: #2266dc;
  font-size: 15px;
}

.subtotal {
  font-weight: 700;
  color: #f97316;
  font-size: 16px;
}

/* 数量输入框样式 */
.quantity-input {
  width: 100px;
}

:deep(.quantity-input .el-input__wrapper) {
  border-radius: 40px;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), inset 0 1px 2px rgba(0, 0, 0, 0.02);
  border: 1px solid #e2e8f0;
}

:deep(.quantity-input .el-input__wrapper.is-focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

/* 底部操作栏 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #eef2ff;
  flex-wrap: wrap;
  gap: 20px;
}

.footer-left {
  display: flex;
  gap: 16px;
}

.clear-btn {
  border-radius: 40px;
  padding: 8px 20px;
  border: 1px solid #e2e8f0;
  color: #f56c6c;
  transition: all 0.2s;
}

.clear-btn:hover {
  background: #fef2f0;
  border-color: #fbc4c4;
  transform: translateY(-1px);
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-label {
  font-size: 16px;
  color: #4b5e7c;
  font-weight: 500;
}

.total-price {
  font-size: 28px;
  font-weight: 800;
  color: #2266dc;
}

.checkout-btn {
  border-radius: 60px;
  padding: 12px 36px;
  background: linear-gradient(105deg, #2266dc, #3b82f6);
  border: none;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 8px 18px -8px rgba(59, 130, 246, 0.4);
  transition: all 0.2s ease;
}

.checkout-btn:hover {
  background: linear-gradient(105deg, #1a56c1, #2b6ed7);
  transform: translateY(-2px);
  box-shadow: 0 12px 22px -10px rgba(59, 130, 246, 0.5);
}

.checkout-btn:active {
  transform: translateY(1px);
}

/* 空购物车状态 */
.empty-cart {
  text-align: center;
  padding: 60px 20px 80px;
}

.empty-cart .el-icon {
  color: #cbd5e1;
  margin-bottom: 20px;
}

.empty-cart p {
  font-size: 18px;
  color: #5b6e8c;
  margin-bottom: 28px;
}

.empty-cart .el-button {
  padding: 10px 32px;
  border-radius: 60px;
  font-weight: 500;
}

/* 底部版权 */
.footer-copyright {
  text-align: center;
  font-size: 13px;
  color: #7c8ba0;
  letter-spacing: 0.3px;
  opacity: 0.8;
  font-weight: 450;
  margin-top: 20px;
}

/* 移动端适配 */
@media (max-width: 900px) {
  .cart-page {
    padding: 24px 20px;
  }
  :deep(.el-card__body) {
    padding: 28px 20px;
  }
  .page-header h2 {
    font-size: 22px;
  }
  .subtitle {
    padding-left: 36px;
  }
  .total-price {
    font-size: 24px;
  }
  .checkout-btn {
    padding: 10px 28px;
  }
}

@media (max-width: 768px) {
  :deep(.cart-table .el-table__cell) {
    padding: 12px 8px;
  }
  .product-name {
    font-size: 14px;
  }
  .unit-price, .subtotal {
    font-size: 13px;
  }
  .cart-footer {
    flex-direction: column;
    align-items: stretch;
  }
  .footer-left,
  .footer-right {
    justify-content: space-between;
  }
  .footer-right {
    justify-content: flex-end;
  }
}

@media (max-width: 640px) {
  .cart-page {
    padding: 20px 16px;
  }
  :deep(.el-card__body) {
    padding: 20px 16px;
  }
  .title-icon {
    font-size: 24px;
  }
  .page-header h2 {
    font-size: 20px;
  }
  .subtitle {
    font-size: 12px;
    padding-left: 32px;
  }
  .total-price {
    font-size: 20px;
  }
  .checkout-btn {
    padding: 8px 20px;
    font-size: 14px;
  }
  .empty-cart p {
    font-size: 16px;
  }
}
</style>
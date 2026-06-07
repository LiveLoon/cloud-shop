<template>
  <div class="product-detail" v-loading="loading">
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <el-card v-if="product" class="detail-card" shadow="never">
      <div class="product-container">
        <div class="product-gallery">
          <div class="image-placeholder">
            <img
              v-if="product.imageUrl"
              :src="getProductImageUrl(product.imageUrl)"
              :alt="product.productName"
              @error="handleImageError"
              class="product-img"
            />
            <el-icon v-else :size="120"><Goods /></el-icon>
          </div>
          <div class="image-thumbnails">
            <span class="thumb active"></span>
            <span class="thumb"></span>
            <span class="thumb"></span>
          </div>
        </div>

        <div class="product-meta">
          <h1 class="product-title">{{ product.productName }}</h1>
          <div class="price-section">
            <div class="current-price">¥{{ product.price }}</div>
            <div class="original-price" v-if="product.oldPrice">¥{{ product.oldPrice }}</div>
          </div>

          <div class="inventory-section" :class="{ 'low-stock': product.inventory < 10 }">
            <el-icon><WarningFilled /></el-icon>
            <span>库存：{{ product.inventory }} 件</span>
            <span v-if="product.inventory < 10" class="low-tag">库存紧张</span>
          </div>

          <div class="quantity-selector">
            <span class="label">购买数量：</span>
            <el-input-number
              v-model="buyNum"
              :min="1"
              :max="product.inventory"
              size="large"
              controls-position="right"
            />
            <span class="unit">件</span>
          </div>

          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              @click="addToCart"
              :disabled="product.inventory <= 0"
              class="cart-btn"
            >
              <el-icon><ShoppingCart /></el-icon>
              加入购物车
            </el-button>
            <el-button
              type="success"
              size="large"
              @click="buyNow"
              :disabled="product.inventory <= 0"
              class="buy-btn"
            >
              <el-icon><CreditCard /></el-icon>
              立即购买
            </el-button>
          </div>

          <div class="extra-info">
            <div class="info-item">
              <!-- 修正：Truck 改为 TruckFilled -->
              <el-icon><TruckFilled /></el-icon>
              <span>全场包邮</span>
            </div>
            <div class="info-item">
              <el-icon><Service /></el-icon>
              <span>正品保障</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span>48小时发货</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-empty v-else-if="!loading && !product" description="商品不存在" class="empty-state" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Goods,
  WarningFilled,
  ShoppingCart,
  CreditCard,
  TruckFilled,    // 修改：原 Truck 改为 TruckFilled
  Service,
  Clock
} from '@element-plus/icons-vue'
import { productApi } from '@/api/product'
import { useCartStore } from '@/stores/cart'
import { getProductImageUrl, DEFAULT_PRODUCT_IMAGE } from '@/utils/imageHelper'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const product = ref(null)
const loading = ref(false)
const buyNum = ref(1)

const fetchProduct = async () => {
  const productId = route.params.id
  if (!productId) {
    ElMessage.error('商品ID不存在')
    router.push('/products')
    return
  }

  loading.value = true
  try {
    const data = await productApi.getProductById(productId)
    product.value = data
    if (buyNum.value > data.inventory) {
      buyNum.value = data.inventory
    }
  } catch (error) {
    console.error('获取商品失败:', error)
    ElMessage.error(error.message || '商品不存在或已下架')
    product.value = null
  } finally {
    loading.value = false
  }
}

const handleImageError = (event) => {
  event.target.src = DEFAULT_PRODUCT_IMAGE
}

const addToCart = () => {
  if (!product.value) return
  if (product.value.inventory <= 0) {
    ElMessage.warning('商品已售罄')
    return
  }

  const cartProduct = {
    id: product.value.id,
    productName: product.value.productName,
    price: product.value.price,
    inventory: product.value.inventory,
    imageUrl: product.value.imageUrl
  }

  for (let i = 0; i < buyNum.value; i++) {
    cartStore.addItem(cartProduct)
  }

  ElMessage.success(`已添加 ${buyNum.value} 件商品到购物车`)
}

const buyNow = () => {
  if (!product.value) return
  if (product.value.inventory <= 0) {
    ElMessage.warning('商品已售罄')
    return
  }

  const cartProduct = {
    id: product.value.id,
    productName: product.value.productName,
    price: product.value.price,
    inventory: product.value.inventory,
    imageUrl: product.value.imageUrl
  }

  for (let i = 0; i < buyNum.value; i++) {
    cartStore.addItem(cartProduct)
  }

  ElMessage.success('已添加到购物车，即将跳转结算')
  router.push('/cart')
}

onMounted(() => {
  fetchProduct()
})
</script>


<style scoped>
/* 整体容器背景 */
.order-detail-page {
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
.detail-wrapper {
  position: relative;
  z-index: 2;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* 卡片主体 */
.detail-card {
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(2px);
  border-radius: 40px;
  box-shadow: 0 25px 45px -12px rgba(0, 0, 0, 0.15), 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  border: 1px solid rgba(255, 255, 255, 0.6);
  margin-bottom: 28px;
}

:deep(.el-card__body) {
  padding: 36px 32px 44px;
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
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

.order-header h2 {
  font-size: 26px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #1f2b48, #2c3e66);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  letter-spacing: -0.3px;
}

.status-tag :deep(.el-tag) {
  padding: 6px 20px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 40px;
  border: none;
}

/* 各个区块标题 */
.info-section h3,
.items-section h3,
.payment-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3e;
  margin: 0 0 16px 0;
  padding-left: 12px;
  border-left: 4px solid #2266dc;
}

/* 自定义描述列表样式 */
.custom-descriptions {
  margin-bottom: 28px;
  border-radius: 20px;
  overflow: hidden;
}

:deep(.custom-descriptions .el-descriptions__header) {
  display: none;
}

:deep(.custom-descriptions .el-descriptions__body) {
  background: #fafcff;
}

:deep(.custom-descriptions .el-descriptions__cell) {
  padding: 14px 20px;
}

:deep(.custom-descriptions .el-descriptions__label) {
  font-weight: 600;
  color: #2c3e50;
  background: #f8fafd;
  width: 120px;
}

:deep(.custom-descriptions .el-descriptions__content) {
  color: #1e2a3e;
}

.order-id {
  font-family: 'Monaco', 'Menlo', monospace;
  font-weight: 500;
  color: #2266dc;
  letter-spacing: 0.3px;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.time-cell .el-icon {
  font-size: 16px;
  color: #8f9bb3;
}

/* 表格样式 */
.table-wrapper {
  border-radius: 20px;
  overflow-x: auto;
  margin-bottom: 24px;
}

.custom-table {
  border-radius: 20px;
  overflow: hidden;
}

:deep(.custom-table .el-table__header-wrapper) {
  background: #f8fafd;
}

:deep(.custom-table .el-table__header th) {
  background: #f8fafd;
  color: #1e2a3e;
  font-weight: 600;
  font-size: 14px;
  padding: 14px 12px;
  border-bottom: 1px solid #e2e8f0;
}

:deep(.custom-table .el-table__row) {
  transition: background 0.2s ease;
}

:deep(.custom-table .el-table__row:hover) {
  background: #fefce8 !important;
}

:deep(.custom-table .el-table__body tr.el-table__row--striped) {
  background: #fafcff;
}

:deep(.custom-table .el-table__cell) {
  padding: 14px 12px;
  border-bottom: 1px solid #eff3f8;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-icon {
  width: 32px;
  height: 32px;
  background: #eef2ff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
}

.price-text {
  font-weight: 600;
  color: #2266dc;
}

.subtotal {
  font-weight: 700;
  color: #f97316;
}

.order-total {
  text-align: right;
  font-size: 18px;
  padding: 16px 20px;
  background: #f8fafd;
  border-radius: 20px;
  margin-top: 8px;
}

.order-total .total-price {
  font-size: 24px;
  font-weight: 800;
  color: #2266dc;
  margin-left: 12px;
}

/* 支付信息中的交易哈希 */
.tx-hash {
  font-family: monospace;
  font-size: 13px;
  color: #5b6e8c;
  word-break: break-all;
}

/* 操作按钮组 */
.order-actions {
  display: flex;
  gap: 20px;
  margin-top: 32px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.order-actions .el-button {
  padding: 10px 28px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.pay-btn {
  background: linear-gradient(105deg, #2266dc, #3b82f6);
  border: none;
  box-shadow: 0 8px 18px -8px rgba(59, 130, 246, 0.4);
}

.pay-btn:hover {
  background: linear-gradient(105deg, #1a56c1, #2b6ed7);
  transform: translateY(-2px);
  box-shadow: 0 12px 22px -10px rgba(59, 130, 246, 0.5);
}

.delete-btn {
  border: 1px solid #fbc4c4;
  color: #f56c6c;
}

.delete-btn:hover {
  background: #fef2f0;
  border-color: #f56c6c;
  transform: translateY(-2px);
}

.back-btn {
  border: 1px solid #cbd5e1;
  color: #4b5e7c;
}

.back-btn:hover {
  background: #f8fafd;
  border-color: #b9c8ff;
  transform: translateY(-2px);
}

.order-actions .el-button:active {
  transform: translateY(1px);
}

/* 空状态 */
.empty-state {
  background: transparent;
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

/* 移动端响应式 */
@media (max-width: 900px) {
  .order-detail-page {
    padding: 24px 20px;
  }
  :deep(.el-card__body) {
    padding: 28px 24px;
  }
  .order-header h2 {
    font-size: 22px;
  }
  .title-icon {
    font-size: 24px;
  }
  .order-total {
    font-size: 16px;
  }
  .order-total .total-price {
    font-size: 22px;
  }
}

@media (max-width: 768px) {
  .order-actions {
    justify-content: center;
  }
  .order-actions .el-button {
    flex: 1;
    min-width: 120px;
  }
  :deep(.custom-descriptions .el-descriptions__cell) {
    padding: 10px 14px;
  }
  :deep(.custom-descriptions .el-descriptions__label) {
    width: 100px;
  }
}

@media (max-width: 640px) {
  .order-detail-page {
    padding: 20px 16px;
  }
  :deep(.el-card__body) {
    padding: 24px 18px;
  }
  .order-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .order-header h2 {
    font-size: 20px;
  }
  .order-total {
    text-align: center;
  }
  .order-actions {
    flex-direction: column;
  }
  .order-actions .el-button {
    width: 100%;
  }
}
</style>
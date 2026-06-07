<template>
  <div class="product-detail" v-loading="loading">
    <!-- 动态背景 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <el-card v-if="product" class="detail-card" shadow="never">
      <div class="product-container">
        <!-- 左侧图片区 -->
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
        </div>

        <!-- 右侧信息区 -->
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
          </div>

          <div class="extra-info">
            <div class="info-item">
              <el-icon><Van /></el-icon>
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
  Service,
  Clock
} from '@element-plus/icons-vue'
import { Van } from '@element-plus/icons-vue'
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
onMounted(() => {
  fetchProduct()
})
</script>

<style scoped>
/* 背景与容器 */
.product-detail {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 48px 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow-x: hidden;
}

/* 动态几何背景 (与列表页一致) */
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

/* 卡片样式 */
.detail-card {
  position: relative;
  z-index: 2;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(2px);
  border-radius: 40px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 25px 45px -12px rgba(0, 0, 0, 0.15), 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: transform 0.2s ease;
}

.detail-card:hover {
  transform: translateY(-2px);
}

:deep(.el-card__body) {
  padding: 40px 48px;
}

/* 双栏布局 */
.product-container {
  display: flex;
  gap: 48px;
  flex-wrap: wrap;
}

.product-gallery {
  flex: 1.2;
  min-width: 280px;
}

.image-placeholder {
  background: linear-gradient(135deg, #f0f4fa, #e6edf6);
  border-radius: 32px;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-placeholder:hover .product-img {
  transform: scale(1.02);
}

.image-placeholder .el-icon {
  color: #a0b8d4;
}

.image-thumbnails {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.thumb {
  width: 48px;
  height: 48px;
  background: #eef2ff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.thumb.active {
  background: #3b82f6;
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

/* 右侧信息区（保持原有样式） */
.product-meta {
  flex: 1.8;
  min-width: 300px;
}

.product-title {
  font-size: 32px;
  font-weight: 700;
  color: #1e2a3e;
  margin: 0 0 20px 0;
  line-height: 1.3;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eef2ff;
}

.current-price {
  font-size: 36px;
  font-weight: 800;
  color: #2266dc;
}

.current-price::before {
  content: '¥';
  font-size: 24px;
  font-weight: 600;
  margin-right: 2px;
}

.original-price {
  font-size: 18px;
  color: #9aa8bf;
  text-decoration: line-through;
}

.inventory-section {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #4b5e7c;
  background: #f8fafd;
  padding: 12px 16px;
  border-radius: 60px;
  width: fit-content;
  margin-bottom: 32px;
}

.low-stock {
  color: #f56c6c;
  background: #fef2f0;
}

.low-tag {
  background: #f56c6c;
  color: white;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 40px;
  margin-left: 8px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.quantity-selector .label {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
}

:deep(.el-input-number--large) {
  width: 140px;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 60px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
}

:deep(.el-input-number .el-input__wrapper.is-focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.unit {
  font-size: 15px;
  color: #7c8ba0;
}

.action-buttons {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.cart-btn,
.buy-btn {
  flex: 1;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 60px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.cart-btn {
  background: linear-gradient(105deg, #ffffff, #f8fafd);
  border: 1px solid #cbd5e1;
  color: #2266dc;
}

.cart-btn:hover {
  background: #eef2ff;
  border-color: #b9c8ff;
  transform: translateY(-2px);
  box-shadow: 0 8px 18px -8px rgba(34, 102, 220, 0.2);
}

.buy-btn {
  background: linear-gradient(105deg, #2266dc, #3b82f6);
  border: none;
  box-shadow: 0 8px 18px -8px rgba(59, 130, 246, 0.4);
}

.buy-btn:hover {
  background: linear-gradient(105deg, #1a56c1, #2b6ed7);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -6px rgba(59, 130, 246, 0.5);
}

.extra-info {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  padding-top: 20px;
  border-top: 1px solid #eef2ff;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #5b6e8c;
}

.empty-state {
  position: relative;
  z-index: 2;
  background: transparent;
}

/* 响应式 */
@media (max-width: 900px) {
  .product-detail {
    padding: 32px 20px;
  }
  :deep(.el-card__body) {
    padding: 32px 28px;
  }
  .product-title {
    font-size: 26px;
  }
  .current-price {
    font-size: 30px;
  }
}

@media (max-width: 700px) {
  :deep(.el-card__body) {
    padding: 24px 20px;
  }
  .product-container {
    flex-direction: column;
  }
  .product-gallery {
    max-width: 400px;
    margin: 0 auto;
  }
}

@media (max-width: 500px) {
  .product-detail {
    padding: 20px 16px;
  }
  .product-title {
    font-size: 22px;
  }
  .current-price {
    font-size: 28px;
  }
  .action-buttons {
    flex-direction: column;
  }
  .cart-btn,
  .buy-btn {
    width: 100%;
  }
  .extra-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
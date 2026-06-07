<template>
  <div class="product-list">
    <!-- 动态背景装饰（与全局风格统一） -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="status-container">
      <div class="loading-spinner"></div>
      <p>加载商品中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="status-container error">
      <el-icon :size="48"><WarningFilled /></el-icon>
      <p>加载失败：{{ error.message }}</p>
      <el-button type="primary" size="small" @click="loadProducts">重试</el-button>
    </div>

    <!-- 商品网格 -->
    <div v-else class="product-grid">
      <div
        v-for="product in products"
        :key="product.id"
        class="product-card"
        @click="goToDetail(product.id)"
      >
        <!-- 图片区域 + 悬停效果 -->
        <div class="product-image">
          <img
            :src="getProductImageUrl(product.imageUrl)"
            :alt="product.productName"
            @error="handleImageError"
          />
          <!-- 库存标签（低库存提醒） -->
          <span v-if="product.inventory <= 5" class="stock-badge low-stock">
            仅剩 {{ product.inventory }} 件
          </span>
        </div>

        <div class="product-info">
          <h3 class="product-title">{{ product.productName }}</h3>
          <div class="price-row">
            <span class="price">¥{{ product.price.toFixed(2) }}</span>
            <span class="inventory">库存: {{ product.inventory }}</span>
          </div>
          <button class="add-to-cart-btn" @click.stop="addToCart(product)">
            <el-icon><ShoppingCart /></el-icon>
            加入购物车
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="products.length === 0" class="empty-state">
        <el-icon :size="64"><Goods /></el-icon>
        <p>暂无商品</p>
      </div>
    </div>

    <!-- 分页组件 -->
    <div v-if="totalPages > 1" class="pagination-wrapper">
      <Pagination
        :current="currentPage"
        :total="totalPages"
        @change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';          // 新增：用于页面跳转
import { ElMessage } from 'element-plus';
import { WarningFilled, ShoppingCart, Goods } from '@element-plus/icons-vue';
import { productApi } from '@/api/product';
import { useCartStore } from '@/stores/cart';
import { getProductImageUrl, DEFAULT_PRODUCT_IMAGE } from '@/utils/imageHelper';

const router = useRouter();                       // 创建路由实例
const cartStore = useCartStore();

const products = ref([]);
const loading = ref(false);
const error = ref(null);
const currentPage = ref(0);
const totalPages = ref(1);

// 加载商品列表
const loadProducts = async (page = 0) => {
  loading.value = true;
  error.value = null;
  try {
    const res = await productApi.getProductList({ page, size: 10 });
    products.value = res.content;
    totalPages.value = res.totalPages;
    currentPage.value = res.number;
  } catch (err) {
    error.value = err;
    console.error('获取商品列表失败', err);
  } finally {
    loading.value = false;
  }
};

// 图片加载失败时替换为默认图
const handleImageError = (event) => {
  event.target.src = DEFAULT_PRODUCT_IMAGE;
};

// 加入购物车（带成功提示），使用 .stop 阻止事件冒泡
const addToCart = (product) => {
  cartStore.addItem(product);
  ElMessage.success(`已添加 ${product.productName} 到购物车`);
};

// 跳转到商品详情页
const goToDetail = (id) => {
  router.push(`/product/${id}`);
};

onMounted(() => {
  loadProducts();
});
</script>

<style scoped>
/* 整体容器 */
.product-list {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 40px 24px;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  overflow-x: hidden;
}

/* 动态几何背景 (与详情页统一) */
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

/* 状态容器（加载/错误） */
.status-container {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 16px;
  color: #5b6e8c;
  font-size: 16px;
  text-align: center;
}

.status-container.error {
  color: #f56c6c;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 商品网格 - 响应式卡片布局 */
.product-grid {
  position: relative;
  z-index: 2;
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 28px;
}

/* 卡片样式 (增加手型光标) */
.product-card {
  background: #ffffff;
  border-radius: 28px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.02), 0 2px 4px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(226, 232, 240, 0.6);
  cursor: pointer;        /* 卡片可点击 */
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 32px -12px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.02);
  border-color: rgba(59, 130, 246, 0.2);
}

/* 图片容器 - 固定比例 + 背景占位 */
.product-image {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  background: #f8fafd;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.03);
}

/* 库存角标 */
.stock-badge {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  color: white;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 40px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.low-stock {
  background: rgba(245, 108, 108, 0.85);
  backdrop-filter: blur(4px);
}

/* 商品信息区域 */
.product-info {
  padding: 20px 18px 22px;
}

.product-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e2a3e;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 50px;
}

.price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 18px;
  flex-wrap: wrap;
  gap: 8px;
}

.price {
  font-size: 24px;
  font-weight: 700;
  color: #2266dc;
  letter-spacing: -0.5px;
}

.price::before {
  content: '¥';
  font-size: 16px;
  font-weight: 500;
  margin-right: 2px;
}

.inventory {
  font-size: 13px;
  color: #7c8ba0;
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 40px;
}

/* 加入购物车按钮 */
.add-to-cart-btn {
  width: 100%;
  background: linear-gradient(105deg, #f1f5f9, #ffffff);
  border: 1px solid #e2e8f0;
  border-radius: 48px;
  padding: 10px 0;
  font-size: 15px;
  font-weight: 600;
  color: #2266dc;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  pointer-events: auto; /* 确保按钮可点，不受父级影响 */
}

.add-to-cart-btn:hover {
  background: linear-gradient(105deg, #eef2ff, #ffffff);
  border-color: #b9c8ff;
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(34, 102, 220, 0.1);
}

.add-to-cart-btn:active {
  transform: scale(0.96);
}

/* 空状态样式 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 20px;
  color: #a0b2c6;
}

.empty-state .el-icon {
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

/* 分页容器 */
.pagination-wrapper {
  position: relative;
  z-index: 2;
  margin-top: 48px;
  display: flex;
  justify-content: center;
}

/* 移动端适配 */
@media (max-width: 720px) {
  .product-list {
    padding: 24px 16px;
  }
  .product-grid {
    gap: 18px;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
  .product-info {
    padding: 16px 14px 18px;
  }
  .product-title {
    font-size: 16px;
    min-height: 44px;
  }
  .price {
    font-size: 20px;
  }
}

@media (max-width: 540px) {
  .product-grid {
    grid-template-columns: 1fr;
    max-width: 380px;
    margin-left: auto;
    margin-right: auto;
  }
  .product-card {
    max-width: 100%;
  }
  .add-to-cart-btn {
    padding: 10px 0;
  }
}
</style>
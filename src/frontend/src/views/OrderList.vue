<template>
  <div class="order-list">
    <!-- 装饰背景 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="order-wrapper">
      <el-card class="order-card" shadow="never">
        <!-- 页面标题区 -->
        <div class="page-header">
          <div class="title-section">
            <el-icon class="title-icon"><Tickets /></el-icon>
            <h2>我的订单</h2>
          </div>
          <p class="subtitle">查看和管理您的所有订单记录</p>
        </div>

        <!-- 订单表格 -->
        <div class="table-container">
          <el-table
            :data="orders"
            v-loading="loading"
            stripe
            style="width: 100%"
            :empty-text="emptyText"
            class="custom-table"
          >
            <el-table-column prop="orderId" label="订单号" min-width="140">
              <template #default="{ row }">
                <span class="order-id">{{ row.orderId }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="170">
              <template #default="{ row }">
                <div class="time-cell">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatDate(row.createTime) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="商品" min-width="260">
              <template #default="{ row }">
                <div class="product-list-cell">
                  <div v-for="(item, idx) in row.itemList" :key="idx" class="order-product-item">
                    <span class="product-name">{{ item.productName }}</span>
                    <span class="product-quantity">x {{ item.buyNum }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="总金额" width="130" align="center">
              <template #default="{ row }">
                <div class="total-amount">¥{{ calculateTotal(row) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button type="primary" size="small" @click="viewDetail(row.orderId)" plain round>
                    详情
                  </el-button>
                  <el-button type="danger" size="small" @click="deleteOrder(row.orderId)" plain round>
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页组件 -->
        <div class="pagination-wrapper" v-if="total > pageSize">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
            background
          />
        </div>
      </el-card>

      <div class="footer-copyright">
        © 2026 Cloud Shop. All rights reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Tickets, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { orderApi } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()
const orders = ref([])
const loading = ref(false)
const page = ref(0)
const pageSize = ref(10)
const total = ref(0)

// 当前页码（从1开始，适配分页组件）
const currentPage = computed(() => page.value + 1)

// 空状态文本
const emptyText = '暂无订单，快去选购商品吧~'

// 格式化时间
const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 计算订单总金额
const calculateTotal = (order) => {
  if (!order.itemList) return '0.00'
  return order.itemList.reduce((sum, item) => sum + (item.price * item.buyNum), 0).toFixed(2)
}

// 加载订单列表
const loadOrders = async (targetPage = 0) => {
  if (!userStore.userId) {
    // 未登录时尝试获取用户信息，如果没有则跳转登录
    if (!userStore.userId) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    return
  }

  loading.value = true
  try {
    // 尝试调用后端订单列表接口
    const data = await orderApi.getUserOrderList(userStore.userId, targetPage, pageSize.value)
    orders.value = data.content || data
    total.value = data.totalElements || (data.length || 0)
    page.value = targetPage
  } catch (error) {
    console.warn('后端订单列表接口未实现，使用模拟数据', error)
    // 模拟数据（展示用）
    const mockOrders = [
      {
        orderId: 'ORD202311001',
        createTime: '2023-11-15T10:30:00',
        itemList: [
          { productId: 1, productName: '智能手表 Pro', buyNum: 1, price: 899 },
          { productId: 2, productName: '无线蓝牙耳机', buyNum: 2, price: 199 }
        ]
      },
      {
        orderId: 'ORD202311002',
        createTime: '2023-11-20T14:45:00',
        itemList: [
          { productId: 3, productName: '运动背包', buyNum: 1, price: 299 }
        ]
      },
      {
        orderId: 'ORD202312001',
        createTime: '2023-12-05T09:15:00',
        itemList: [
          { productId: 4, productName: '陶瓷保温杯', buyNum: 3, price: 79 },
          { productId: 5, productName: '便携充电宝', buyNum: 1, price: 129 }
        ]
      }
    ]
    // 模拟分页数据
    const start = targetPage * pageSize.value
    const end = start + pageSize.value
    orders.value = mockOrders.slice(start, end)
    total.value = mockOrders.length
    page.value = targetPage
  } finally {
    loading.value = false
  }
}

// 分页切换
const handlePageChange = (newPage) => {
  loadOrders(newPage - 1)
}

// 查看订单详情
const viewDetail = (orderId) => {
  router.push(`/order/${orderId}`)
}

// 删除订单
const deleteOrder = async (orderId) => {
  ElMessageBox.confirm('删除订单后无法恢复，确认删除吗？', '警告', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'error',
    center: true,
    roundButton: true
  }).then(async () => {
    try {
      await orderApi.deleteOrder(orderId)
      ElMessage.success('订单已删除')
      // 重新加载当前页，如果当前页没有数据则跳到上一页
      await loadOrders(page.value)
      if (orders.value.length === 0 && page.value > 0) {
        await loadOrders(page.value - 1)
      }
    } catch (error) {
      ElMessage.error(error.message || '删除失败，请稍后重试')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
/* 整体容器背景 */
.order-list {
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
.order-wrapper {
  position: relative;
  z-index: 2;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

/* 卡片主体 */
.order-card {
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
  margin-bottom: 32px;
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
  padding: 16px 12px;
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
  padding: 16px 12px;
  border-bottom: 1px solid #eff3f8;
}

/* 订单号样式 */
.order-id {
  font-family: 'Monaco', 'Menlo', monospace;
  font-weight: 500;
  color: #2266dc;
  letter-spacing: 0.5px;
}

/* 时间单元格 */
.time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4b5e7c;
  font-size: 13px;
}

.time-cell .el-icon {
  font-size: 16px;
  color: #8f9bb3;
}

/* 商品列表单元格 */
.product-list-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-product-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding: 4px 0;
  border-bottom: 1px dashed #edf2f7;
}

.order-product-item:last-child {
  border-bottom: none;
}

.product-name {
  color: #2c3e50;
  font-weight: 500;
  flex: 1;
}

.product-quantity {
  color: #7c8ba0;
  font-size: 13px;
  margin-left: 12px;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 40px;
}

/* 总金额样式 */
.total-amount {
  font-weight: 700;
  font-size: 16px;
  color: #2266dc;
}

/* 操作按钮组 */
.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

:deep(.action-buttons .el-button) {
  border-radius: 40px;
  padding: 6px 16px;
  font-weight: 500;
  transition: all 0.2s ease;
  border-width: 1px;
}

:deep(.action-buttons .el-button--primary.is-plain) {
  border-color: #b9c8ff;
  color: #2266dc;
  background: #ffffff;
}

:deep(.action-buttons .el-button--primary.is-plain:hover) {
  background: #eef2ff;
  border-color: #3b82f6;
  transform: translateY(-1px);
}

:deep(.action-buttons .el-button--danger.is-plain) {
  border-color: #fbc4c4;
  color: #f56c6c;
  background: #ffffff;
}

:deep(.action-buttons .el-button--danger.is-plain:hover) {
  background: #fef2f0;
  border-color: #f56c6c;
  transform: translateY(-1px);
}

/* 分页样式 */
.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: #2266dc;
  border-radius: 20px;
}

:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 20px;
  min-width: 36px;
  height: 36px;
  line-height: 36px;
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

/* 加载状态样式覆盖 */
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(2px);
  border-radius: 40px;
}

/* 空状态内部优化（el-table自带empty） */
:deep(.el-table__empty-text) {
  color: #8f9bb3;
  font-size: 14px;
  padding: 40px 0;
}

/* 移动端适配 */
@media (max-width: 900px) {
  .order-list {
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
}

@media (max-width: 768px) {
  :deep(.custom-table .el-table__cell) {
    padding: 12px 8px;
  }
  .order-id {
    font-size: 12px;
  }
  .total-amount {
    font-size: 14px;
  }
  .action-buttons {
    flex-direction: column;
    gap: 6px;
  }
  :deep(.action-buttons .el-button) {
    padding: 5px 8px;
    font-size: 12px;
  }
  .product-name {
    font-size: 13px;
  }
}

@media (max-width: 650px) {
  .order-list {
    padding: 20px 16px;
  }
  :deep(.el-card__body) {
    padding: 20px 16px;
  }
  .page-header h2 {
    font-size: 20px;
  }
  .title-icon {
    font-size: 24px;
  }
}
</style>
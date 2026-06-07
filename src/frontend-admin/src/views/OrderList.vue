<template>
  <div class="order-list">
    <el-table :data="orders" border stripe v-loading="loading">
      <el-table-column prop="orderId" label="订单号" width="100" />
      <el-table-column label="用户" min-width="150">
        <template #default="{ row }">
          {{ row.userInfo?.username }} (ID: {{ row.userInfo?.userId }})
        </template>
      </el-table-column>
      <el-table-column label="商品列表" min-width="200">
        <template #default="{ row }">
          <div v-for="item in row.itemList" :key="item.productId">
            {{ item.productName }} x {{ item.buyNum }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewPayment(row.orderId)">查看支付</el-button>
          <el-button link type="danger" @click="deleteOrder(row.orderId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件（按照 Element Plus 最新规范） -->
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[5, 10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api/order'
import { paymentApi } from '@/api/payment'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const res = await orderApi.getOrderList(params)
    orders.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    ElMessage.error('加载订单失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadOrders()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadOrders()
}

const deleteOrder = async (id) => {
  await ElMessageBox.confirm('物理删除订单，不可恢复，确认？', '警告', { type: 'warning' })
  try {
    await orderApi.deleteOrder(id)
    ElMessage.success('订单已删除')
    loadOrders()
  } catch (error) {
    ElMessage.error('删除失败：' + error.message)
  }
}

const viewPayment = async (orderId) => {
  try {
    const payment = await paymentApi.getPaymentByOrderId(orderId)
    ElMessageBox.alert(
      `支付ID: ${payment.id}\n状态: ${payment.status}\nTxHash: ${payment.txHash || '无'}`,
      '支付详情'
    )
  } catch {
    ElMessage.warning('未查询到支付记录')
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-list {
  padding: 20px;
}
</style>
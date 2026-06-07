<template>
  <div class="payment-list">
    <!-- 搜索栏 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="订单ID">
        <el-input v-model="searchOrderId" placeholder="输入订单号查询支付记录" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchByOrderId">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 按订单ID查询结果 -->
    <el-card v-if="singlePayment" shadow="never" style="margin-bottom: 20px">
      <template #header>
        <span>订单 {{ searchOrderId }} 的支付详情</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="支付ID">{{ singlePayment.id }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ singlePayment.orderId }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="getStatusType(singlePayment.status)">{{ singlePayment.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="链ID">{{ singlePayment.chainId }}</el-descriptions-item>
        <el-descriptions-item label="预期金额(Wei)">{{ singlePayment.value }}</el-descriptions-item>
        <el-descriptions-item label="实际到账(Wei)">{{ singlePayment.realValue || '-' }}</el-descriptions-item>
        <el-descriptions-item label="买家地址">{{ singlePayment.buyerAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收款地址">{{ singlePayment.sellerAddress }}</el-descriptions-item>
        <el-descriptions-item label="交易哈希">{{ singlePayment.txHash || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ singlePayment.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ singlePayment.txTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="确认数">{{ singlePayment.confirmations || 0 }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 所有支付记录表格（假设有分页列表接口） -->
    <div class="payment-table-wrapper">
      <h3>所有支付记录</h3>
      <el-table :data="paymentList" border stripe v-loading="loading">
        <el-table-column prop="id" label="支付ID" width="80" />
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" label="金额(Wei)" min-width="150" show-overflow-tooltip />
        <el-table-column prop="txHash" label="交易哈希" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadPayments"
        @current-change="loadPayments"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="支付详情" width="600px">
      <el-descriptions :column="1" border v-if="currentDetail">
        <el-descriptions-item label="支付ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ currentDetail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentDetail.status }}</el-descriptions-item>
        <el-descriptions-item label="链ID">{{ currentDetail.chainId }}</el-descriptions-item>
        <el-descriptions-item label="预期金额(Wei)">{{ currentDetail.value }}</el-descriptions-item>
        <el-descriptions-item label="实际到账(Wei)">{{ currentDetail.realValue || '-' }}</el-descriptions-item>
        <el-descriptions-item label="买家地址">{{ currentDetail.buyerAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收款地址">{{ currentDetail.sellerAddress }}</el-descriptions-item>
        <el-descriptions-item label="交易哈希">{{ currentDetail.txHash || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentDetail.txTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="确认数">{{ currentDetail.confirmations || 0 }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { paymentApi } from '@/api/payment'
import { ElMessage } from 'element-plus'

// ---------- 按订单ID查询 ----------
const searchOrderId = ref('')
const singlePayment = ref(null)

// ---------- 支付列表（假设存在 /list 接口） ----------
const paymentList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 详情弹窗
const detailVisible = ref(false)
const currentDetail = ref(null)

// 辅助函数：状态标签颜色
const getStatusType = (status) => {
  const map = {
    CREATED: 'info',
    PAID: 'success',
    FAILED: 'danger',
    EXPIRED: 'warning'
  }
  return map[status] || 'info'
}

// 按订单ID查询支付
const searchByOrderId = async () => {
  if (!searchOrderId.value) {
    ElMessage.warning('请输入订单ID')
    return
  }
  try {
    const res = await paymentApi.getPaymentByOrderId(searchOrderId.value)
    singlePayment.value = res
  } catch (error) {
    if (error.message.includes('无支付记录')) {
      singlePayment.value = null
      ElMessage.info('该订单暂无支付记录')
    } else {
      ElMessage.error('查询失败：' + error.message)
    }
  }
}

const resetSearch = () => {
  searchOrderId.value = ''
  singlePayment.value = null
}

// 加载所有支付记录（假设后端提供分页接口）
const loadPayments = async () => {
  loading.value = true
  try {
    // 这里假设存在 GET /api/payment/list?page=0&size=10
    // 实际没有的话需要后端添加，或者通过其他方式聚合
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    // 注意：paymentApi 中没有定义 getPaymentList，我们可以临时添加
    // 此处为演示，调用一个不存在的接口会报错，可注释并改用模拟数据
    // 实际使用时请确保后端实现该接口，或通过订单列表循环获取（不推荐）
    const res = await paymentApi.getPaymentList(params)
    paymentList.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error(error)
    ElMessage.warning('支付列表接口未实现，使用模拟数据演示')
    // 模拟几条数据用于展示
    paymentList.value = [
      {
        id: 1,
        orderId: 10086,
        status: 'PAID',
        value: '1000000000000000000',
        txHash: '0xabc...123',
        createTime: '2026-06-05 10:00:00'
      },
      {
        id: 2,
        orderId: 10087,
        status: 'CREATED',
        value: '2000000000000000000',
        txHash: null,
        createTime: '2026-06-05 11:00:00'
      }
    ]
    total.value = 2
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadPayments()
})
</script>

<style scoped>
.payment-list {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
.payment-table-wrapper {
  margin-top: 20px;
}
</style>
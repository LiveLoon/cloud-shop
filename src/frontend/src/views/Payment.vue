<template>
  <div class="payment-page">
    <!-- 装饰背景 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="payment-wrapper">
      <el-card class="payment-card" shadow="never">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="title-section">
            <el-icon class="title-icon"><Wallet /></el-icon>
            <h2>支付订单</h2>
          </div>
          <div class="order-id-badge">
            #{{ orderId }}
          </div>
        </div>

        <div v-if="payment" class="payment-content">
          <!-- 支付信息描述列表（美化） -->
          <div class="info-grid">
            <div class="info-row">
              <span class="info-label">订单金额</span>
              <span class="info-value amount">{{ orderTotalEtc }} CNY</span>
            </div>
            <div class="info-row">
              <span class="info-label">收款地址</span>
              <span class="info-value address">{{ payment.sellerAddress }}</span>
              <el-button 
                type="primary" 
                size="small" 
                plain 
                round 
                @click="copyAddress(payment.sellerAddress)"
                class="copy-btn"
              >
                复制
              </el-button>
            </div>
            <div class="info-row">
              <span class="info-label">转账附带Data</span>
              <span class="info-value data-message">{{ paymentMessageForDisplay }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">支付状态</span>
              <span class="info-value">
                <el-tag :type="statusType" effect="light" round size="large">{{ statusText }}</el-tag>
              </span>
            </div>
            <div v-if="payment.txHash" class="info-row">
              <span class="info-label">交易哈希</span>
              <span class="info-value tx-hash">{{ payment.txHash }}</span>
            </div>
          </div>

          <!-- 二维码区域（独立突出显示） -->
          <div class="qrcode-section">
            <h3>扫码获取收款地址</h3>
            <div class="qrcode-container">
              <img :src="qrCodeUrl" alt="收款二维码" class="qrcode-img" v-if="qrCodeUrl" />
              <div v-else class="qrcode-placeholder">
                <el-icon><Loading /></el-icon>
                <span>生成二维码中...</span>
              </div>
            </div>
            <p class="qrcode-hint">使用钱包扫码或手动复制上方地址</p>
          </div>

          <!-- 待支付状态的操作区 -->
          <div v-if="payment.status === 'CREATED'" class="payment-actions">
            <el-alert 
              type="warning" 
              :closable="false" 
              class="payment-alert"
              title="转账须知"
            >
              <template #default>
                <p>请使用 <strong>CNY 钱包</strong> 向上述收款地址转账 <strong>{{ (payment.value / 1e18).toFixed(4) }} CNY</strong>，</p>
                <p>并务必在转账时填写 <strong>Data</strong> 字段：<code>{{ paymentMessageForDisplay }}</code></p>
              </template>
            </el-alert>
            <el-button 
              type="primary" 
              size="large" 
              round 
              @click="checkStatus" 
              :loading="checking"
              class="check-btn"
            >
              <el-icon><Refresh /></el-icon>
              我已支付，查询状态
            </el-button>
          </div>

          <!-- 支付成功状态 -->
          <div v-if="payment.status === 'PAID'" class="success-section">
            <el-result
              icon="success"
              title="支付成功"
              sub-title="订单已确认，感谢您的购买"
            >
              <template #extra>
                <el-button type="primary" round @click="$router.push('/orders')">
                  查看订单
                </el-button>
              </template>
            </el-result>
          </div>

          <!-- 支付失败/过期等其他状态 -->
          <div v-if="payment.status === 'FAILED' || payment.status === 'EXPIRED'" class="failed-section">
            <el-result
              icon="error"
              :title="statusText"
              sub-title="如有疑问请联系客服"
            >
              <template #extra>
                <el-button type="primary" round @click="goBack">
                  返回订单列表
                </el-button>
              </template>
            </el-result>
          </div>
        </div>

        <!-- 加载骨架屏 -->
        <div v-else class="skeleton-wrapper">
          <el-skeleton :rows="6" animated />
        </div>
      </el-card>

      <div class="footer-copyright">
        © 2026 Cloud Shop. All rights reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Wallet, Loading, Refresh, CopyDocument } from '@element-plus/icons-vue'
import { paymentApi } from '@/api/payment'
import { orderApi } from '@/api/order'
import QRCode from 'qrcode'

const route = useRoute()
const router = useRouter()
const orderId = route.params.orderId
const payment = ref(null)
const checking = ref(false)
const orderDetail = ref(null)
let pollTimer = null
const qrCodeUrl = ref('')

// 订单总金额（CNY）
const orderTotalEtc = computed(() => {
  if (!orderDetail.value?.itemList) return '0.00'
  let total = 0
  for (const item of orderDetail.value.itemList) {
    if (item.price) {
      total += item.price * item.buyNum
    }
  }
  return total.toFixed(2)
})

// 支付消息显示（去掉前缀）
const paymentMessageForDisplay = computed(() => {
  const msg = payment.value?.paymentMessage
  if (!msg) return '转账时必须携带订单ID，否则将转账失败'
  const parts = msg.split(':')
  return parts.length > 1 ? parts[1].trim() : msg
})

const statusText = computed(() => {
  const map = { CREATED: '待支付', PAID: '已支付', FAILED: '支付失败', EXPIRED: '已过期' }
  return map[payment.value?.status] || '未知'
})

const statusType = computed(() => {
  const map = { CREATED: 'warning', PAID: 'success', FAILED: 'danger', EXPIRED: 'info' }
  return map[payment.value?.status] || 'info'
})

// 生成二维码
const generateQRCode = async (address) => {
  if (!address) return
  try {
    const qrData = address
    const qrDataURL = await QRCode.toDataURL(qrData, {
      width: 180,
      margin: 1,
      color: {
        dark: '#1e2a3e',
        light: '#ffffff'
      }
    })
    qrCodeUrl.value = qrDataURL
  } catch (error) {
    console.error('生成二维码失败', error)
  }
}

// 复制地址
const copyAddress = async (address) => {
  try {
    await navigator.clipboard.writeText(address)
    ElMessage.success('收款地址已复制')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 监听收款地址变化，重新生成二维码
watch(() => payment.value?.sellerAddress, (newAddress) => {
  if (newAddress) {
    generateQRCode(newAddress)
  }
}, { immediate: true })

// 获取订单详情
const fetchOrderDetail = async () => {
  try {
    const data = await orderApi.getOrderById(orderId)
    orderDetail.value = data
    return data
  } catch (error) {
    console.error('获取订单详情失败', error)
    throw error
  }
}

// 获取支付记录
const loadPayment = async () => {
  try {
    const data = await paymentApi.getPaymentByOrderId(orderId)
    payment.value = data
    if (data.status === 'PAID') {
      if (pollTimer) clearInterval(pollTimer)
      ElMessage.success('支付成功')
    }
  } catch (error) {
    if (error.message?.includes('无支付记录')) {
      await createPayment()
    } else {
      ElMessage.error('获取支付信息失败')
    }
  }
}

// 创建支付记录
const createPayment = async () => {
  try {
    if (!orderDetail.value) {
      await fetchOrderDetail()
    }
    let totalYuan = 0
    for (const item of orderDetail.value.itemList) {
      if (item.price) {
        totalYuan += item.price * item.buyNum
      } else {
        ElMessage.error('订单商品缺少价格信息，无法创建支付，请联系客服')
        return
      }
    }
    if (totalYuan <= 0) {
      ElMessage.error('订单金额无效，无法创建支付')
      return
    }
    const totalFen = Math.round(totalYuan * 100)
    const valueWei = (BigInt(totalFen) * BigInt(10 ** 16)).toString()
    
    const paymentData = {
      orderId: parseInt(orderId),
      value: valueWei,
      chainId: 61,
      buyerAddress: '0x683999F801df5CE935A4a094522DeF78CD3E490B'
    }
    const data = await paymentApi.createPayment(paymentData)
    payment.value = data
    startPolling()
    ElMessage.success('支付记录创建成功，请完成转账')
  } catch (error) {
    ElMessage.error('创建支付记录失败：' + error.message)
    console.error(error)
  }
}

// 手动查询状态
const checkStatus = async () => {
  checking.value = true
  // 模拟支付成功（实际应调用后端检查接口）
  payment.value.status = 'PAID'
  await paymentApi.updatePaymentByPaymentId(payment.value.id, payment.value)
  await loadPayment()
  checking.value = false
}

// 启动轮询
const startPolling = () => {
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = setInterval(async () => {
    await loadPayment()
    if (payment.value?.status === 'PAID') {
      clearInterval(pollTimer)
    }
  }, 5000)
}

const goBack = () => {
  router.push('/orders')
}

onMounted(async () => {
  await fetchOrderDetail()
  await loadPayment()
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
/* 整体容器背景 */
.payment-page {
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
.payment-wrapper {
  position: relative;
  z-index: 2;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

/* 卡片主体 */
.payment-card {
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

/* 页面头部 */
.page-header {
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

.order-id-badge {
  font-family: 'Monaco', monospace;
  background: #eef2ff;
  padding: 6px 16px;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 600;
  color: #2266dc;
}

/* 信息网格布局（仿描述列表但更灵活） */
.info-grid {
  background: #fafcff;
  border-radius: 24px;
  padding: 8px 0;
  margin-bottom: 28px;
  border: 1px solid #eff3f8;
}

.info-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  padding: 14px 20px;
  border-bottom: 1px solid #eff3f8;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 110px;
  font-weight: 600;
  color: #2c3e50;
}

.info-value {
  flex: 1;
  color: #1e2a3e;
  word-break: break-all;
}

.info-value.amount {
  font-size: 20px;
  font-weight: 800;
  color: #2266dc;
}

.info-value.address {
  font-family: monospace;
  font-size: 14px;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 40px;
  display: inline-block;
  max-width: 300px;
  overflow-x: auto;
  white-space: nowrap;
}

.copy-btn {
  margin-left: 12px;
}

.info-value.data-message {
  background: #fef3c7;
  padding: 4px 12px;
  border-radius: 40px;
  font-family: monospace;
  color: #b45309;
}

.tx-hash {
  font-family: monospace;
  font-size: 13px;
}

/* 二维码区域 */
.qrcode-section {
  text-align: center;
  margin: 16px 0 28px;
  padding: 20px;
  background: #ffffff;
  border-radius: 32px;
  border: 1px solid #eef2ff;
}

.qrcode-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3e;
  margin: 0 0 16px 0;
}

.qrcode-container {
  display: inline-block;
  padding: 12px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 20px -8px rgba(0, 0, 0, 0.1);
}

.qrcode-img {
  width: 180px;
  height: 180px;
  display: block;
}

.qrcode-placeholder {
  width: 180px;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #f8fafd;
  border-radius: 16px;
  color: #8f9bb3;
}

.qrcode-hint {
  margin-top: 12px;
  font-size: 13px;
  color: #7c8ba0;
}

/* 支付操作区 */
.payment-actions {
  margin-top: 16px;
}

.payment-alert {
  border-radius: 20px;
  margin-bottom: 24px;
}

.payment-alert p {
  margin: 6px 0;
  font-size: 14px;
}

.payment-alert code {
  background: #1e2a3e;
  color: #fbbf24;
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 13px;
}

.check-btn {
  width: 100%;
  background: linear-gradient(105deg, #2266dc, #3b82f6);
  border: none;
  font-weight: 600;
  padding: 12px;
  box-shadow: 0 8px 18px -8px rgba(59, 130, 246, 0.4);
}

.check-btn:hover {
  background: linear-gradient(105deg, #1a56c1, #2b6ed7);
  transform: translateY(-2px);
}

/* 成功与失败区域 */
.success-section,
.failed-section {
  margin-top: 16px;
}

/* 骨架屏 */
.skeleton-wrapper {
  padding: 8px 0;
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

/* 响应式 */
@media (max-width: 680px) {
  .payment-page {
    padding: 24px 20px;
  }
  :deep(.el-card__body) {
    padding: 28px 24px;
  }
  .page-header h2 {
    font-size: 22px;
  }
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .info-label {
    width: auto;
  }
  .info-value.address {
    white-space: normal;
    word-break: break-all;
  }
  .copy-btn {
    margin-left: 0;
    margin-top: 4px;
  }
  .qrcode-img {
    width: 140px;
    height: 140px;
  }
}

@media (max-width: 480px) {
  .payment-page {
    padding: 20px 16px;
  }
  :deep(.el-card__body) {
    padding: 24px 18px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
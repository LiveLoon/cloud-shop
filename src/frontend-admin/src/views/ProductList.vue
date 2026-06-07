<template>
  <div>
    <el-button type="primary" @click="openForm">新增商品</el-button>
    <el-table :data="products" border>
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <el-image 
            :src="getProductImageUrl(row.imageUrl)" 
            fit="cover" 
            style="width: 50px; height: 50px"
          >
            <template #error>
              <div style="background: #f5f5f5">无图</div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="productName" label="商品名" />
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="inventory" label="库存" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link @click="editProduct(row)">编辑</el-button>
          <el-button link @click="incrementStock(row)">补货</el-button>
          <el-button link type="danger" @click="deleteProduct(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" @current-change="loadProducts" />

    <!-- 商品表单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="formTitle">
      <el-form :model="formData">
        <el-form-item label="商品名">
          <el-input v-model="formData.productName" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="formData.price" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="formData.inventory" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="formData.imageUrl" placeholder="输入图片地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProduct">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductImageUrl } from '@/utils/imageHelper'  // 导入正确的函数名

const products = ref([])
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const formData = ref({ productName: '', price: 0, inventory: 0, imageUrl: '' })
const isEdit = ref(false)
const editId = ref(null)
const formTitle = ref('')

const loadProducts = async () => {
  const res = await productApi.getProductList({ page: page.value - 1, size: 10 })
  products.value = res.content
  total.value = res.totalElements
}

const openForm = () => {
  isEdit.value = false
  formTitle.value = '新增商品'
  formData.value = { productName: '', price: 0, inventory: 0, imageUrl: '' }
  dialogVisible.value = true
}

const editProduct = (row) => {
  isEdit.value = true
  editId.value = row.id
  formTitle.value = '编辑商品'
  formData.value = { ...row }
  dialogVisible.value = true
}

const submitProduct = async () => {
  if (isEdit.value) {
    await productApi.updateProduct(editId.value, formData.value)
    ElMessage.success('更新成功')
  } else {
    await productApi.addProduct(formData.value)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  loadProducts()
}

const deleteProduct = async (id) => {
  await ElMessageBox.confirm('确认删除？')
  await productApi.deleteProduct(id)
  ElMessage.success('删除成功')
  loadProducts()
}

const incrementStock = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入补货数量', '补货', {
    inputValue: 1,
    inputValidator: (val) => !isNaN(val) && val > 0
  })
  await productApi.updateInventory(row.id, Number(value))
  ElMessage.success('补货成功')
  loadProducts()
}

onMounted(loadProducts)
</script>
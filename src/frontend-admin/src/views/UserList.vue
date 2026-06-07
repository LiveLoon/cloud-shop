<template>
  <div class="user-list">
    <!-- 搜索栏 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="用户名">
        <el-input v-model="searchKeyword" placeholder="请输入用户名" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="openAddDialog">新增用户</el-button>
      </el-form-item>
    </el-form>

    <!-- 用户表格 -->
    <el-table :data="userList" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="真实姓名" min-width="120">
        <template #default="{ row }">
          {{ row.realName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="score" label="积分" width="100" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[5, 10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadUsers"
      @current-change="loadUsers"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input type="password" v-model="formData.password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="积分" prop="score" v-if="isEdit">
          <el-input-number v-model="formData.score" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

// ---------- 数据 ----------
const userList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const loading = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  username: '',
  realName: '',
  password: '',
  score: 0
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// ---------- 方法 ----------
// 加载用户列表（假设后端支持 /list 分页及模糊查询）
const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value
    }
    const res = await userApi.getUserList(params)
    // 假设返回格式：{ content: [], totalElements: number }
    userList.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const resetSearch = () => {
  searchKeyword.value = ''
  handleSearch()
}

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  formData.id = row.id
  formData.username = row.username
  formData.realName = row.realName || ''
  formData.score = row.score
  formData.password = '' // 编辑时不显示密码
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.username = ''
  formData.realName = ''
  formData.password = ''
  formData.score = 0
  formRef.value?.resetFields()
}

// 提交表单
const submitForm = async () => {
  if (!isEdit.value && !formData.password) {
    ElMessage.warning('请输入密码')
    return
  }
  try {
    if (isEdit.value) {
      // 更新用户信息（只传需要修改的字段）
      await userApi.updateUser({
        id: formData.id,
        username: formData.username,
        realName: formData.realName,
        score: formData.score
      })
      ElMessage.success('更新成功')
    } else {
      await userApi.register(formData.username, formData.password)
      ElMessage.success('新增用户成功')
    }
    dialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 删除用户（假设有 DELETE /api/user/{id} 接口）
const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该用户吗？此操作不可恢复', '警告', {
    type: 'warning'
  })
  try {
    // 如果后端没有删除接口，可以用禁用或标记删除；这里仅作示例
    // await userApi.deleteUser(id)
    ElMessage.success('删除成功（演示，实际需调用后端接口）')
    loadUsers()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 弹窗标题
const dialogTitle = ref('')

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-list {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>
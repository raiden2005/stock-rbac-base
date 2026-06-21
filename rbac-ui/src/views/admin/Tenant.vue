<template>
  <div class="tenant-container">
    <el-card>
      <template #header>
        <span>租户管理</span>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="租户名称">
          <el-input v-model="searchForm.name" placeholder="请输入租户名称" clearable />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contact" placeholder="请输入联系人" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="name" label="租户名称" />
        <el-table-column prop="code" label="租户代码" />
        <el-table-column prop="contact" label="联系人" />
        <el-table-column prop="phone" label="联系方式" />
        <el-table-column prop="expireStatus" label="有效期状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.expireStatus === 'active' ? 'success' : 'danger'">
              {{ row.expireStatus === 'active' ? '有效' : '已过期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="packageType" label="套餐类型" width="120">
          <template #default="{ row }">
            <el-tag type="success">{{ row.packageType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="租户详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="租户名称">{{ currentTenant.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="租户代码">{{ currentTenant.code || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentTenant.contact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentTenant.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="有效期状态">
          <el-tag :type="currentTenant.expireStatus === 'active' ? 'success' : 'danger'">
            {{ currentTenant.expireStatus === 'active' ? '有效' : '已过期' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="套餐类型">
          <el-tag type="success">{{ currentTenant.packageType || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentTenant.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="到期时间">{{ currentTenant.expireTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { list, detail } from '@/api/tenant.js'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const currentTenant = ref({})

const searchForm = reactive({
  name: '',
  contact: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await list({
      page: currentPage.value,
      pageSize: pageSize.value,
      name: searchForm.name,
      contact: searchForm.contact
    })
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.contact = ''
  handleSearch()
}

const handleSizeChange = () => {
  fetchData()
}

const handleCurrentChange = () => {
  fetchData()
}

const handleDetail = async (row) => {
  try {
    const res = await detail(row.id)
    currentTenant.value = res.data || {}
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.tenant-container {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

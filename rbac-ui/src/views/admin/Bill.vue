<template>
  <div class="bill-container">
    <el-card>
      <template #header>
        <span>账单记录</span>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="billId" label="账单ID" width="120" />
        <el-table-column prop="billType" label="账单类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getBillTypeType(row.billType)">
              {{ getBillTypeText(row.billType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refId" label="关联ID" width="120" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { list } from '@/api/bill.js'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getBillTypeText = (type) => {
  const textMap = {
    subscription: '订阅',
    topup: '充值',
    refund: '退款',
    consumption: '消费'
  }
  return textMap[type] || type
}

const getBillTypeType = (type) => {
  const typeMap = {
    subscription: 'success',
    topup: 'primary',
    refund: 'warning',
    consumption: 'info'
  }
  return typeMap[type] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await list({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  fetchData()
}

const handleCurrentChange = () => {
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.bill-container {
  padding: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

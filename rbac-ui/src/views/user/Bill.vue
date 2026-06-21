<template>
  <div class="bill-container">
    <el-card>
      <template #header>
        <span>我的账单</span>
      </template>
      <el-table :data="tableData" stripe>
        <el-table-column prop="billId" label="账单ID" width="120" />
        <el-table-column prop="billType" label="账单类型" width="120">
          <template #default="{ row }">
            {{ getBillTypeText(row.billType) }}
          </template>
        </el-table-column>
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
import { selfBillList } from '@/api/tenant.js'

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

const fetchData = async () => {
  try {
    const res = await selfBillList({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取数据失败')
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

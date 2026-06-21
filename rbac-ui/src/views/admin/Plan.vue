<template>
  <div class="plan-container">
    <el-card>
      <template #header>
        <span>套餐配置</span>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="name" label="套餐名称" />
        <el-table-column prop="type" label="套餐类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'yearly' ? 'success' : 'primary'">
              {{ row.type === 'yearly' ? '年费' : '月费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="freeQuestions" label="每月免费提问" width="140" />
        <el-table-column prop="yearlyPrice" label="年费价格（元）" width="140">
          <template #default="{ row }">
            ¥{{ row.yearlyPrice || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="overduePrice" label="超额单价" width="200">
          <template #default="{ row }">
            <div class="price-edit">
              <el-input-number
                v-model="row.overduePrice"
                :min="0"
                :precision="2"
                size="small"
                controls-position="right"
                style="width: 120px"
              />
              <el-button type="primary" size="small" @click="handleSave(row)">保存</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { list, update } from '@/api/plan.js'

const loading = ref(false)
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await list()
    tableData.value = res.data || []
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async (row) => {
  try {
    await update({
      id: row.id,
      overduePrice: row.overduePrice
    })
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.plan-container {
  padding: 20px;
}

.price-edit {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>

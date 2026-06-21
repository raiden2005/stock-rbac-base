<template>
  <div class="question-container">
    <el-card>
      <template #header>
        <span>我的提问</span>
      </template>
      <el-table :data="tableData" stripe @row-click="handleRowClick">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column prop="replyStatus" label="回复状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.replyStatus === 'replied' ? 'success' : 'warning'">
              {{ row.replyStatus === 'replied' ? '已回复' : '待回复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quotaType" label="额度类型" width="100">
          <template #default="{ row }">
            {{ row.quotaType === 'free' ? '免费' : '付费' }}
          </template>
        </el-table-column>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="问题详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="问题详情">{{ currentRow.questionContent }}</el-descriptions-item>
        <el-descriptions-item label="回复内容">{{ currentRow.replyContent || '暂无回复' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { myList } from '@/api/question.js'

const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const currentRow = ref({})

const fetchData = async () => {
  try {
    const res = await myList({
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

const handleRowClick = (row) => {
  currentRow.value = row
  dialogVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.question-container {
  padding: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

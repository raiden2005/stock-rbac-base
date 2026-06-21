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
    <el-dialog v-model="dialogVisible" title="问题详情" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="问题详情">{{ currentRow.questionContent }}</el-descriptions-item>
        <el-descriptions-item label="回复内容">
          <div class="reply-content">{{ currentRow.replyContent || '暂无回复' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 知识溯源区域 -->
      <div class="knowledge-trace-section" v-if="currentRow.replyStatus === 'replied'">
        <div class="trace-header">
          <el-icon :size="18" color="#667eea"><Connection /></el-icon>
          <span class="trace-title">知识溯源</span>
          <el-tag v-if="!hasKnowledgeSource" type="info" size="small" effect="plain">
            降级模式
          </el-tag>
        </div>

        <!-- 有知识来源 -->
        <div v-if="hasKnowledgeSource" class="trace-list">
          <div
            v-for="(source, index) in knowledgeSources"
            :key="index"
            class="trace-item"
          >
            <el-collapse>
              <el-collapse-item :name="index">
                <template #title>
                  <div class="trace-item-header">
                    <el-icon :size="16" color="#409eff"><Document /></el-icon>
                    <span class="trace-item-title">{{ source.title }}</span>
                    <el-tag
                      :type="categoryTagType(source.category)"
                      size="small"
                      effect="light"
                      round
                    >
                      {{ source.category }}
                    </el-tag>
                  </div>
                </template>
                <div class="trace-item-content">
                  {{ source.content }}
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>

        <!-- 无知识来源（降级模式） -->
        <div v-else class="trace-fallback">
          <el-icon :size="40" color="#c0c4cc"><InfoFilled /></el-icon>
          <p>本次回答基于通用AI分析</p>
          <span class="fallback-desc">未命中私有知识库中的相关内容，回答由通用大模型生成</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Connection, Document, InfoFilled } from '@element-plus/icons-vue'
import { myList } from '@/api/question.js'

const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const currentRow = ref({})

// 知识溯源相关
const knowledgeSources = computed(() => {
  return currentRow.value.knowledgeSources || []
})

const hasKnowledgeSource = computed(() => {
  return knowledgeSources.value && knowledgeSources.value.length > 0
})

// 分类标签颜色映射
const categoryTagType = (category) => {
  const map = {
    '估值模型': 'primary',
    '风控体系': 'danger',
    '赛道评判': 'warning',
    '历史复盘': 'success',
    '其他': 'info'
  }
  return map[category] || 'info'
}

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

.reply-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
}

/* ==================== 知识溯源区域 ==================== */
.knowledge-trace-section {
  margin-top: 24px;
  padding: 20px;
  background: #f8f9fc;
  border-radius: 10px;
  border: 1px solid #e8ecf3;
}

.trace-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8ecf3;
}

.trace-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

/* 溯源列表 */
.trace-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.trace-item {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  overflow: hidden;
}

.trace-item :deep(.el-collapse-item__header) {
  padding: 0 12px;
  background: #fff;
  height: 48px;
  border-bottom: none;
}

.trace-item :deep(.el-collapse-item__wrap) {
  border-bottom: none;
}

.trace-item :deep(.el-collapse-item__content) {
  padding: 0 12px 12px;
}

.trace-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.trace-item-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.trace-item-content {
  font-size: 13px;
  line-height: 1.8;
  color: #606266;
  padding: 12px;
  background: #fafbfc;
  border-radius: 6px;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
}

/* 降级模式 */
.trace-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0 8px;
  text-align: center;
}

.trace-fallback p {
  font-size: 15px;
  font-weight: 500;
  color: #909399;
  margin-top: 12px;
}

.fallback-desc {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 6px;
}
</style>

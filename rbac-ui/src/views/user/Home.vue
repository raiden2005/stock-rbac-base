<template>
  <div class="home-container">
    <!-- Hero 区域 -->
    <div class="hero-section">
      <h1>💬 提交您的问题</h1>
      <p class="subtitle">专业、高效的智能问答服务</p>
      
      <!-- 额度标签 -->
      <div class="quota-tags">
        <el-tag type="success" effect="plain">本月免费剩余：{{ quotaInfo.freeRemain || 0 }}</el-tag>
        <el-tag type="warning" effect="plain">付费存量：{{ quotaInfo.paidRemain || 0 }}</el-tag>
        <el-tag type="info" effect="plain">合计可用：{{ quotaInfo.totalRemain || 0 }}</el-tag>
      </div>
    </div>

    <!-- 提问表单 -->
    <div class="question-form">
      <el-card>
        <template #header>
          <span>提交问题</span>
        </template>
        <el-form :model="questionForm" label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="questionForm.title" placeholder="请输入问题标题" />
          </el-form-item>
          <el-form-item label="详情">
            <el-input
              v-model="questionForm.questionContent"
              type="textarea"
              :rows="4"
              placeholder="请详细描述您的问题"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              提交问题
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">免费额度</div>
            <div class="stat-value">{{ quotaInfo.freeRemain || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">付费存量</div>
            <div class="stat-value">{{ quotaInfo.paidRemain || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">套餐有效期</div>
            <div class="stat-value">{{ homeData.expireTime || '无' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">订阅状态</div>
            <div class="stat-value">
              <el-tag :type="homeData.subscriptionStatus === 'active' ? 'success' : 'info'">
                {{ homeData.subscriptionStatus === 'active' ? '已订阅' : '未订阅' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近提问列表 -->
    <div class="recent-questions">
      <el-card>
        <template #header>
          <span>最近提问</span>
        </template>
        <el-table :data="recentQuestions" stripe>
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
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { selfHome } from '@/api/tenant.js'
import { submit } from '@/api/question.js'

const homeData = ref({})
const quotaInfo = ref({})
const recentQuestions = ref([])

const questionForm = reactive({
  title: '',
  questionContent: ''
})

const submitting = ref(false)

const fetchData = async () => {
  try {
    const res = await selfHome()
    homeData.value = res.data || {}
    quotaInfo.value = res.data?.quotaInfo || {}
    recentQuestions.value = res.data?.recentQuestions || []
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
}

const handleSubmit = async () => {
  if (!questionForm.title || !questionForm.questionContent) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submitting.value = true
  try {
    await submit(questionForm)
    ElMessage.success('提交成功')
    questionForm.title = ''
    questionForm.questionContent = ''
    fetchData()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 40px;
  border-radius: 12px;
  text-align: center;
  margin-bottom: 30px;
}

.hero-section h1 {
  font-size: 32px;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 20px;
}

.quota-tags {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.question-form {
  margin-bottom: 30px;
}

.stat-cards {
  margin-bottom: 30px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}
</style>

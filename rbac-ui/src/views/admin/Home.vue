<template>
  <div class="home-container">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="border-green">
          <div class="stat-item">
            <div class="stat-label">总租户数</div>
            <div class="stat-value">{{ statData.totalTenants || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="border-blue">
          <div class="stat-item">
            <div class="stat-label">活跃租户</div>
            <div class="stat-value">{{ statData.activeTenants || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="border-orange">
          <div class="stat-item">
            <div class="stat-label">即将到期</div>
            <div class="stat-value">{{ statData.expiringTenants || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="border-purple">
          <div class="stat-item">
            <div class="stat-label">总订阅收入</div>
            <div class="stat-value">¥{{ statData.totalSubscriptionRevenue || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stat-cards">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>收入统计</span>
          </template>
          <div class="revenue-item">
            <span class="label">订阅总收入：</span>
            <span class="value">¥{{ statData.subscriptionRevenue || 0 }} 元</span>
          </div>
          <div class="revenue-item">
            <span class="label">提问总收入：</span>
            <span class="value">¥{{ statData.questionRevenue || 0 }} 元</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>订单统计</span>
          </template>
          <div class="revenue-item">
            <span class="label">订阅订单数：</span>
            <span class="value">{{ statData.subscriptionOrders || 0 }}</span>
          </div>
          <div class="revenue-item">
            <span class="label">提问订单数：</span>
            <span class="value">{{ statData.questionOrders || 0 }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { home } from '@/api/stat.js'

const statData = ref({})

const fetchData = async () => {
  try {
    const res = await home()
    statData.value = res.data || {}
  } catch (error) {
    ElMessage.error('获取数据失败')
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

.stat-cards {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px 0;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.border-green {
  border-top: 3px solid #67c23a;
}

.border-blue {
  border-top: 3px solid #409eff;
}

.border-orange {
  border-top: 3px solid #e6a23c;
}

.border-purple {
  border-top: 3px solid #9c27b0;
}

.revenue-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.revenue-item:last-child {
  border-bottom: none;
}

.revenue-item .label {
  color: #666;
}

.revenue-item .value {
  font-weight: bold;
  color: #333;
}
</style>

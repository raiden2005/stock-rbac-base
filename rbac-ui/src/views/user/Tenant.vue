<template>
  <div class="tenant-container">
    <!-- 租户信息 -->
    <el-card class="tenant-info">
      <template #header>
        <span>租户信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="租户名称">{{ tenantInfo.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="套餐类型">
          <el-tag type="success">{{ tenantInfo.packageType || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="有效期状态">
          <el-tag :type="tenantInfo.expireStatus === 'active' ? 'success' : 'danger'">
            {{ tenantInfo.expireStatus === 'active' ? '有效' : '已过期' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="有效期范围">
          {{ tenantInfo.startTime }} ~ {{ tenantInfo.expireTime }}
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ tenantInfo.contact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ tenantInfo.phone || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 额度信息 -->
    <el-card class="quota-info">
      <template #header>
        <span>额度信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="免费额度剩余">
          <el-tag type="success">{{ quotaInfo.freeRemain || 0 }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="付费存量">
          <el-tag type="warning">{{ quotaInfo.paidRemain || 0 }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总可用额度">
          <el-tag type="info">{{ quotaInfo.totalRemain || 0 }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已使用额度">{{ quotaInfo.used || 0 }}</el-descriptions-item>
        <el-descriptions-item label="免费额度总数">{{ quotaInfo.freeTotal || 0 }}</el-descriptions-item>
        <el-descriptions-item label="付费额度总数">{{ quotaInfo.paidTotal || 0 }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { selfHome } from '@/api/tenant.js'
import { selfQuota } from '@/api/tenant.js'

const tenantInfo = ref({})
const quotaInfo = ref({})

const fetchData = async () => {
  try {
    const res = await selfHome()
    tenantInfo.value = res.data?.tenant || {}
  } catch (error) {
    ElMessage.error('获取租户信息失败')
  }
}

const fetchQuota = async () => {
  try {
    const res = await selfQuota()
    quotaInfo.value = res.data || {}
  } catch (error) {
    ElMessage.error('获取额度信息失败')
  }
}

onMounted(() => {
  fetchData()
  fetchQuota()
})
</script>

<style scoped>
.tenant-container {
  padding: 20px;
}

.tenant-info {
  margin-bottom: 20px;
}
</style>

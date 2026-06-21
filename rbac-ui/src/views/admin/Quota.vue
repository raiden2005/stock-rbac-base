<template>
  <div class="quota-container">
    <el-card>
      <template #header>
        <span>💰 额度管理</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="租户名称">
          <el-input v-model="searchForm.keyword" placeholder="输入租户名称/代码" clearable @keyup.enter="loadQuotaList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadQuotaList">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 额度列表 -->
      <el-table v-loading="loading" :data="tableData" stripe border>
        <el-table-column prop="tenantName" label="租户名称" min-width="140" />
        <el-table-column prop="tenantCode" label="租户代码" width="160" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="statMonth" label="统计月份" width="110" />
        <el-table-column prop="freeUsed" label="免费已用" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.freeUsed }} 次</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payUsed" label="付费已用" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.payUsed }} 次</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="surplusPay" label="付费存量" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.surplusPay > 0 ? 'success' : 'danger'" effect="plain">
              {{ row.surplusPay }} 次
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openAdjust(row)">
              调整额度
            </el-button>
            <el-button size="small" type="warning" @click="openReset(row)">
              重置免费
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :current-page="pageNum"
          :page-size="pageSize"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <!-- 调整额度弹窗 -->
    <el-dialog v-model="adjustVisible" title="调整付费存量额度" width="480px" destroy-on-close>
      <el-form label-width="120px">
        <el-form-item label="租户">
          <span class="form-value">{{ currentRow?.tenantName }}</span>
        </el-form-item>
        <el-form-item label="当前存量">
          <el-tag type="success" effect="plain">{{ currentRow?.surplusPay ?? 0 }} 次</el-tag>
        </el-form-item>
        <el-form-item label="调整量">
          <el-input-number
            v-model="adjustForm.adjust"
            :min="-999"
            :max="999"
            placeholder="正数增加，负数减少"
          />
          <div class="form-tip">正数 = 增加存量，负数 = 减少存量，调整后最低为 0</div>
        </el-form-item>
        <el-form-item label="操作说明">
          <el-input
            v-model="adjustForm.remark"
            type="textarea"
            :rows="2"
            placeholder="如：管理员赠送5次 / 用户退款减少3次"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjustLoading" @click="confirmAdjust">
          确认调整
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置免费额度弹窗 -->
    <el-dialog v-model="resetVisible" title="重置当月免费额度" width="420px" destroy-on-close>
      <div class="reset-tip">
        <p>确认重置租户 <strong>{{ currentRow?.tenantName }}</strong> 的当月免费额度？</p>
        <p>当前已使用 <strong>{{ currentRow?.freeUsed ?? 0 }} 次</strong>，重置后将清零。</p>
        <p class="warn">⚠️ 此操作不可逆，请谨慎操作。</p>
      </div>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="warning" :loading="resetLoading" @click="confirmReset">
          确认重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { quotaList, adjustSurplus, resetFree } from '@/api/quota.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ keyword: '' })

// 调整额度
const adjustVisible = ref(false)
const adjustLoading = ref(false)
const currentRow = ref(null)
const adjustForm = reactive({ adjust: 0, remark: '' })

// 重置免费
const resetVisible = ref(false)
const resetLoading = ref(false)

const loadQuotaList = async () => {
  loading.value = true
  try {
    const res = await quotaList({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: searchForm.keyword })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  pageNum.value = 1
  loadQuotaList()
}

const onPageChange = (p) => {
  pageNum.value = p
  loadQuotaList()
}

const openAdjust = (row) => {
  currentRow.value = row
  adjustForm.adjust = 0
  adjustForm.remark = ''
  adjustVisible.value = true
}

const confirmAdjust = async () => {
  if (adjustForm.adjust === 0) {
    ElMessage.warning('调整量不能为 0')
    return
  }
  adjustLoading.value = true
  try {
    const res = await adjustSurplus(currentRow.value.tenantId, {
      adjust: adjustForm.adjust,
      remark: adjustForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('额度调整成功')
      adjustVisible.value = false
      loadQuotaList()
    }
  } catch {}
  adjustLoading.value = false
}

const openReset = (row) => {
  currentRow.value = row
  resetVisible.value = true
}

const confirmReset = async () => {
  resetLoading.value = true
  try {
    const res = await resetFree(currentRow.value.tenantId)
    if (res.code === 200) {
      ElMessage.success('免费额度已重置')
      resetVisible.value = false
      loadQuotaList()
    }
  } catch {}
  resetLoading.value = false
}

onMounted(() => {
  loadQuotaList()
})
</script>

<style scoped>
.quota-container { width: 100%; }
.search-form { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.form-value { font-weight: 600; color: #333; }
.form-tip { font-size: 12px; color: #999; margin-top: 4px; line-height: 1.4; }
.reset-tip p { margin: 8px 0; font-size: 14px; color: #333; }
.reset-tip .warn { color: #e6a23c; font-weight: 500; }
</style>

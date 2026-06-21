<template>
  <div class="knowledge-container">
    <el-tabs v-model="activeTab" type="border-card" class="main-tabs">
      <!-- Tab1: 知识库列表 -->
      <el-tab-pane label="知识库列表" name="list">
        <!-- 筛选区 -->
        <div class="filter-bar">
          <el-row :gutter="16" align="middle">
            <el-col :span="4">
              <el-select v-model="filterForm.category" placeholder="全部分类" clearable @change="fetchList">
                <el-option label="全部" value="" />
                <el-option label="估值模型" value="估值模型" />
                <el-option label="风控体系" value="风控体系" />
                <el-option label="赛道评判" value="赛道评判" />
                <el-option label="历史复盘" value="历史复盘" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="filterForm.status" placeholder="全部状态" clearable @change="fetchList">
                <el-option label="全部" value="" />
                <el-option label="启用" value="1" />
                <el-option label="下架" value="0" />
              </el-select>
            </el-col>
            <el-col :span="7">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                @change="fetchList"
              />
            </el-col>
            <el-col :span="5">
              <el-input v-model="filterForm.keyword" placeholder="搜索知识标题" clearable @keyup.enter="fetchList" @clear="fetchList">
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="4" style="text-align: right">
              <el-button type="primary" @click="openAddDialog">
                <el-icon><Plus /></el-icon>
                新增知识
              </el-button>
            </el-col>
          </el-row>
        </div>

        <!-- 数据表格 -->
        <el-table :data="tableData" stripe v-loading="listLoading" style="width: 100%">
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120">
            <template #default="{ row }">
              <el-tag :type="categoryTagType(row.category)" effect="light" round>
                {{ row.category }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="chunkCount" label="切片数" width="100" align="center" />
          <el-table-column prop="hitCount" label="命中次数" width="100" align="center">
            <template #default="{ row }">
              <span class="hit-count">{{ row.hitCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                active-text="启用"
                inactive-text="下架"
                @change="handleToggleStatus(row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openEditDialog(row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button type="success" link size="small" @click="viewHitRecord(row)">
                <el-icon><View /></el-icon> 命中记录
              </el-button>
              <el-popconfirm title="确定要删除该知识吗？" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button type="danger" link size="small">
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="listPage"
            v-model:page-size="listPageSize"
            :total="listTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchList"
            @current-change="fetchList"
          />
        </div>
      </el-tab-pane>

      <!-- Tab2: 数据统计看板 -->
      <el-tab-pane label="数据统计" name="stats">
        <div v-loading="statsLoading" class="stats-container">
          <!-- 第一行统计卡片 -->
          <el-row :gutter="20" class="stat-row">
            <el-col :span="6">
              <el-card class="stat-card border-blue" shadow="hover">
                <div class="stat-card-body">
                  <div class="stat-icon-wrap bg-blue">
                    <el-icon :size="28"><Document /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">总知识数</div>
                    <div class="stat-value">{{ statsData.totalKnowledge || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card border-green" shadow="hover">
                <div class="stat-card-body">
                  <div class="stat-icon-wrap bg-green">
                    <el-icon :size="28"><Grid /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">总切片向量数</div>
                    <div class="stat-value">{{ statsData.totalChunks || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card border-orange" shadow="hover">
                <div class="stat-card-body">
                  <div class="stat-icon-wrap bg-orange">
                    <el-icon :size="28"><CircleCheck /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">启用数量</div>
                    <div class="stat-value">{{ statsData.enabledCount || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card border-red" shadow="hover">
                <div class="stat-card-body">
                  <div class="stat-icon-wrap bg-red">
                    <el-icon :size="28"><CircleClose /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">下架数量</div>
                    <div class="stat-value">{{ statsData.disabledCount || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 第二行统计卡片 -->
          <el-row :gutter="20" class="stat-row">
            <el-col :span="12">
              <el-card class="stat-card border-cyan" shadow="hover">
                <div class="stat-card-body">
                  <div class="stat-icon-wrap bg-cyan">
                    <el-icon :size="28"><TrendCharts /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">今日检索命中总量</div>
                    <div class="stat-value">{{ statsData.todayHits || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="stat-card border-purple" shadow="hover">
                <div class="stat-card-body">
                  <div class="stat-icon-wrap bg-purple">
                    <el-icon :size="28"><DataAnalysis /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">近7天检索命中总量</div>
                    <div class="stat-value">{{ statsData.weekHits || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 图表区域 -->
          <el-row :gutter="20" class="chart-row">
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header>
                  <span class="card-header-title">热门知识 TOP10</span>
                </template>
                <div ref="topChartRef" class="chart-box"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header>
                  <span class="card-header-title">RAG 问答使用率</span>
                </template>
                <div ref="ragChartRef" class="chart-box"></div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>

      <!-- Tab3: 命中记录 -->
      <el-tab-pane label="命中记录" name="hitRecord">
        <div class="hit-record-header">
          <el-button @click="activeTab = 'list'" link>
            <el-icon><ArrowLeft /></el-icon> 返回知识库列表
          </el-button>
          <span class="hit-record-title" v-if="hitKnowledge">
            <el-tag :type="categoryTagType(hitKnowledge.category)" effect="light" round>
              {{ hitKnowledge.category }}
            </el-tag>
            {{ hitKnowledge.title }} - 命中记录
          </span>
        </div>
        <el-table :data="hitRecordData" stripe v-loading="hitLoading" style="width: 100%">
          <el-table-column prop="hitTime" label="命中时间" width="200" />
          <el-table-column prop="chunkContent" label="切片内容摘要" min-width="400" show-overflow-tooltip />
          <el-table-column prop="similarity" label="相似度得分" width="140" align="center">
            <template #default="{ row }">
              <el-progress
                :percentage="Number((row.similarity * 100).toFixed(1))"
                :stroke-width="14"
                :text-inside="true"
                :status="row.similarity >= 0.8 ? 'success' : row.similarity >= 0.6 ? '' : 'warning'"
              />
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="hitPage"
            v-model:page-size="hitPageSize"
            :total="hitTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchHitRecord"
            @current-change="fetchHitRecord"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑知识弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑知识' : '新增知识'"
      width="680px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="知识标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入知识标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="录入模式">
          <el-radio-group v-model="formData.inputMode">
            <el-radio-button value="manual">手动录入</el-radio-button>
            <el-radio-button value="upload">文件上传</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="formData.inputMode === 'manual'" label="知识内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="8"
            placeholder="请输入知识内容，支持大段文本录入"
            maxlength="50000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item v-if="formData.inputMode === 'upload'" label="上传文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".txt,.docx,.pdf"
            :file-list="fileList"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 .txt / .docx / .pdf 格式，文件大小不超过 20MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="知识分类" prop="category">
          <el-select v-model="formData.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="估值模型" value="估值模型" />
            <el-option label="风控体系" value="风控体系" />
            <el-option label="赛道评判" value="赛道评判" />
            <el-option label="历史复盘" value="历史复盘" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="检索权重">
          <div class="weight-slider">
            <el-slider
              v-model="formData.weight"
              :min="0.1"
              :max="1.0"
              :step="0.1"
              show-input
              :show-input-controls="false"
              input-size="small"
            />
            <span class="weight-tip">权重越高，该知识在检索中越优先被匹配</span>
          </div>
        </el-form-item>

        <el-collapse>
          <el-collapse-item title="高级配置" name="advanced">
            <el-form-item label="切片长度">
              <div class="chunk-length-wrap">
                <el-input-number
                  v-model="formData.chunkLength"
                  :min="100"
                  :max="2000"
                  :step="50"
                />
                <span class="chunk-length-tip">默认500，范围100~2000字符</span>
              </div>
            </el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>

      <!-- 提交进度 -->
      <div v-if="submitting" class="submit-progress">
        <el-steps :active="progressStep" align-center finish-status="success">
          <el-step title="解析中" description="文件解析处理" :icon="Document" />
          <el-step title="切片中" description="文本分片处理" :icon="Scissors" />
          <el-step title="向量化中" description="生成向量嵌入" :icon="MagicStick" />
          <el-step title="入库成功" description="知识入库完成" :icon="CircleCheck" />
        </el-steps>
        <el-progress
          :percentage="progressPercent"
          :stroke-width="6"
          :format="() => ''"
          style="margin-top: 16px"
          status="success"
        />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false" :disabled="submitting">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ submitting ? '处理中...' : '确 定' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch, shallowRef } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Edit, View, Delete, Document, Grid, CircleCheck, CircleClose,
  TrendCharts, DataAnalysis, ArrowLeft, UploadFilled, Scissors, MagicStick
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  knowledgeList, knowledgeAdd, knowledgeUpdate,
  knowledgeToggleStatus, knowledgeRemove, knowledgeStats, knowledgeHitRecord
} from '@/api/knowledge.js'

// ==================== Tab 控制 ====================
const activeTab = ref('list')

// ==================== Tab1: 知识库列表 ====================
const listLoading = ref(false)
const tableData = ref([])
const listPage = ref(1)
const listPageSize = ref(10)
const listTotal = ref(0)

const filterForm = reactive({
  category: '',
  status: '',
  dateRange: null,
  keyword: ''
})

const fetchList = async () => {
  listLoading.value = true
  try {
    const params = {
      page: listPage.value,
      pageSize: listPageSize.value
    }
    if (filterForm.category) params.category = filterForm.category
    if (filterForm.status !== '' && filterForm.status !== null) params.status = filterForm.status
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startTime = filterForm.dateRange[0]
      params.endTime = filterForm.dateRange[1]
    }
    if (filterForm.keyword) params.keyword = filterForm.keyword
    const res = await knowledgeList(params)
    tableData.value = res.data?.list || []
    listTotal.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取知识库列表失败')
  } finally {
    listLoading.value = false
  }
}

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

// 状态切换
const handleToggleStatus = async (row) => {
  try {
    await knowledgeToggleStatus({ id: row.id, status: row.status })
    ElMessage.success(row.status === 1 ? '已启用' : '已下架')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await knowledgeRemove(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== Tab2: 数据统计看板 ====================
const statsLoading = ref(false)
const statsData = ref({})
const topChartRef = ref(null)
const ragChartRef = ref(null)
const topChartInstance = shallowRef(null)
const ragChartInstance = shallowRef(null)

const fetchStats = async () => {
  statsLoading.value = true
  try {
    const res = await knowledgeStats()
    statsData.value = res.data || {}
    await nextTick()
    renderTopChart()
    renderRagChart()
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

const renderTopChart = () => {
  if (!topChartRef.value) return
  if (topChartInstance.value) topChartInstance.value.dispose()
  topChartInstance.value = echarts.init(topChartRef.value)

  const topList = statsData.value.topKnowledge || []
  const names = topList.map((item) => item.title).reverse()
  const values = topList.map((item) => item.hitCount).reverse()

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '12%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'value', axisLabel: { fontSize: 12 } },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { fontSize: 12, width: 100, overflow: 'truncate' }
    },
    series: [{
      type: 'bar',
      data: values,
      barWidth: 18,
      itemStyle: {
        borderRadius: [0, 4, 4, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#667eea' }
        ])
      },
      label: { show: true, position: 'right', fontSize: 12, color: '#666' }
    }]
  }
  topChartInstance.value.setOption(option)
}

const renderRagChart = () => {
  if (!ragChartRef.value) return
  if (ragChartInstance.value) ragChartInstance.value.dispose()
  ragChartInstance.value = echarts.init(ragChartRef.value)

  const ragUsage = statsData.value.ragUsage || { ragCount: 0, normalCount: 0 }
  const total = ragUsage.ragCount + ragUsage.normalCount

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: { fontSize: 13 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        fontSize: 13
      },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold' }
      },
      data: [
        {
          value: ragUsage.ragCount,
          name: 'RAG增强回答',
          itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])}
        },
        {
          value: ragUsage.normalCount,
          name: '普通回答',
          itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#e0e7ff' },
            { offset: 1, color: '#c7d2fe' }
          ])}
        }
      ]
    }]
  }
  ragChartInstance.value.setOption(option)
}

// 监听Tab切换，加载统计数据
watch(activeTab, (val) => {
  if (val === 'stats') {
    fetchStats()
  }
})

// ==================== Tab3: 命中记录 ====================
const hitKnowledge = ref(null)
const hitRecordData = ref([])
const hitLoading = ref(false)
const hitPage = ref(1)
const hitPageSize = ref(10)
const hitTotal = ref(0)

const viewHitRecord = (row) => {
  hitKnowledge.value = row
  hitPage.value = 1
  activeTab.value = 'hitRecord'
  fetchHitRecord()
}

const fetchHitRecord = async () => {
  if (!hitKnowledge.value) return
  hitLoading.value = true
  try {
    const res = await knowledgeHitRecord(hitKnowledge.value.id, {
      page: hitPage.value,
      pageSize: hitPageSize.value
    })
    hitRecordData.value = res.data?.list || []
    hitTotal.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取命中记录失败')
  } finally {
    hitLoading.value = false
  }
}

// ==================== 新增/编辑弹窗 ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const fileList = ref([])
const submitting = ref(false)
const progressStep = ref(0)
const progressPercent = ref(0)

const formData = reactive({
  id: null,
  title: '',
  inputMode: 'manual',
  content: '',
  category: '',
  weight: 0.8,
  chunkLength: 500,
  file: null
})

const formRules = {
  title: [{ required: true, message: '请输入知识标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入知识内容', trigger: 'blur' }],
  category: [{ required: true, message: '请选择知识分类', trigger: 'change' }]
}

const openAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  formData.id = row.id
  formData.title = row.title
  formData.inputMode = 'manual'
  formData.content = row.content || ''
  formData.category = row.category
  formData.weight = row.weight || 0.8
  formData.chunkLength = row.chunkLength || 500
  formData.file = null
  fileList.value = []
  dialogVisible.value = true
}

const resetForm = () => {
  formData.id = null
  formData.title = ''
  formData.inputMode = 'manual'
  formData.content = ''
  formData.category = ''
  formData.weight = 0.8
  formData.chunkLength = 500
  formData.file = null
  fileList.value = []
  progressStep.value = 0
  progressPercent.value = 0
}

const handleFileChange = (file) => {
  const maxSize = 20 * 1024 * 1024
  if (file.raw.size > maxSize) {
    ElMessage.error('文件大小不能超过 20MB')
    fileList.value = []
    return
  }
  formData.file = file.raw
}

const handleFileRemove = () => {
  formData.file = null
  fileList.value = []
}

// 模拟提交进度
const simulateProgress = () => {
  return new Promise((resolve) => {
    const steps = [
      { step: 1, percent: 25, delay: 800 },
      { step: 2, percent: 50, delay: 1200 },
      { step: 3, percent: 75, delay: 1500 },
      { step: 4, percent: 100, delay: 600 }
    ]
    let idx = 0
    const run = () => {
      if (idx >= steps.length) {
        resolve()
        return
      }
      const s = steps[idx]
      progressStep.value = s.step
      progressPercent.value = s.percent
      idx++
      setTimeout(run, s.delay)
    }
    run()
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  // 文件上传模式下校验文件
  if (formData.inputMode === 'upload' && !formData.file && !isEdit.value) {
    ElMessage.warning('请上传文件')
    return
  }

  submitting.value = true
  progressStep.value = 0
  progressPercent.value = 0

  try {
    const payload = new FormData()
    payload.append('title', formData.title)
    payload.append('category', formData.category)
    payload.append('weight', formData.weight)
    payload.append('chunkLength', formData.chunkLength)
    payload.append('inputMode', formData.inputMode)

    if (formData.inputMode === 'manual') {
      payload.append('content', formData.content)
    }
    if (formData.file) {
      payload.append('file', formData.file)
    }
    if (isEdit.value) {
      payload.append('id', formData.id)
    }

    // 启动进度模拟
    const progressPromise = simulateProgress()
    // 发起请求
    const apiPromise = isEdit.value
      ? knowledgeUpdate({
          id: formData.id,
          title: formData.title,
          content: formData.inputMode === 'manual' ? formData.content : undefined,
          category: formData.category,
          weight: formData.weight,
          chunkLength: formData.chunkLength,
          inputMode: formData.inputMode
        })
      : knowledgeAdd(payload)

    // 使用FormData时需要特殊处理Content-Type
    if (formData.inputMode === 'upload' && !isEdit.value) {
      // 对于文件上传，使用原生axios
      const axios = (await import('axios')).default
      const token = localStorage.getItem('rbac-user')
        ? JSON.parse(localStorage.getItem('rbac-user'))?.token
        : ''
      const apiPromiseUpload = axios.post('/api/system/knowledge/add', payload, {
        headers: {
          'Authorization': token,
          'Content-Type': 'multipart/form-data'
        }
      }).then(res => {
        if (res.data?.code !== 200) {
          ElMessage.error(res.data?.msg || '操作失败')
          return Promise.reject(res.data)
        }
        return res.data
      })

      await Promise.all([progressPromise, apiPromiseUpload])
    } else {
      await Promise.all([progressPromise, apiPromise])
    }

    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(isEdit.value ? '编辑失败' : '新增失败')
    }
  } finally {
    submitting.value = false
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.knowledge-container {
  padding: 0;
}

.main-tabs {
  border-radius: 8px;
  overflow: hidden;
}

/* 筛选区 */
.filter-bar {
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #fafbfc;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 命中次数样式 */
.hit-count {
  font-weight: 600;
  color: #409eff;
  font-size: 15px;
}

/* ==================== 统计看板 ==================== */
.stats-container {
  padding: 0;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
}

.stat-card.border-blue { border-top: 3px solid #409eff; }
.stat-card.border-green { border-top: 3px solid #67c23a; }
.stat-card.border-orange { border-top: 3px solid #e6a23c; }
.stat-card.border-red { border-top: 3px solid #f56c6c; }
.stat-card.border-cyan { border-top: 3px solid #00bcd4; }
.stat-card.border-purple { border-top: 3px solid #9c27b0; }

.stat-card-body {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-wrap.bg-blue { background: rgba(64, 158, 255, 0.1); color: #409eff; }
.stat-icon-wrap.bg-green { background: rgba(103, 194, 58, 0.1); color: #67c23a; }
.stat-icon-wrap.bg-orange { background: rgba(230, 162, 60, 0.1); color: #e6a23c; }
.stat-icon-wrap.bg-red { background: rgba(245, 108, 108, 0.1); color: #f56c6c; }
.stat-icon-wrap.bg-cyan { background: rgba(0, 188, 212, 0.1); color: #00bcd4; }
.stat-icon-wrap.bg-purple { background: rgba(156, 39, 176, 0.1); color: #9c27b0; }

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-box {
  width: 100%;
  height: 380px;
}

.card-header-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

/* ==================== 命中记录 ==================== */
.hit-record-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.hit-record-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ==================== 弹窗表单 ==================== */
.weight-slider {
  width: 100%;
}

.weight-tip {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chunk-length-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chunk-length-tip {
  font-size: 12px;
  color: #909399;
}

/* 提交进度 */
.submit-progress {
  padding: 20px 0 0;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
}
</style>

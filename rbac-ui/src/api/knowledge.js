import request from './request.js'

// 知识库分页列表
export const knowledgeList = (params) => request.get('/api/system/knowledge/list', { params })

// 新增知识
export const knowledgeAdd = (data) => request.post('/api/system/knowledge/add', data)

// 编辑知识
export const knowledgeUpdate = (data) => request.put('/api/system/knowledge/update', data)

// 上下架切换
export const knowledgeToggleStatus = (data) => request.put('/api/system/knowledge/status', data)

// 删除知识
export const knowledgeRemove = (id) => request.delete('/api/system/knowledge/remove/' + id)

// 统计看板数据
export const knowledgeStats = () => request.get('/api/system/knowledge/stats')

// 命中明细
export const knowledgeHitRecord = (knowledgeId, params) => request.get('/api/system/knowledge/hitRecord/' + knowledgeId, { params })

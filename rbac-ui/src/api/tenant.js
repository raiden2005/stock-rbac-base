import request from './request.js'
// ===== 用户侧 =====
export const selfHome = () => request.get('/api/tenant/self/home')
export const selfQuota = () => request.get('/api/tenant/self/question/quota')
export const selfSubOrderList = (params) => request.get('/api/tenant/self/order/sub/list', { params })
export const selfBillList = (params) => request.get('/api/tenant/self/bill/list', { params })
// ===== 管理侧 =====
export const list = (params) => request.get('/api/tenant/list', { params })
export const detail = (id) => request.get('/api/tenant/' + id)
export const create = (data) => request.post('/api/tenant/create', data)
export const update = (data) => request.put('/api/tenant/update', data)

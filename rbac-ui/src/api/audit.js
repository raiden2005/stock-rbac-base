import request from './request.js'
export const list = (params) => request.get('/api/audit/list', { params })

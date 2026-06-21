import request from './request.js'
export const list = (params) => request.get('/api/bill/list', { params })
export const selfList = (params) => request.get('/api/tenant/self/bill/list', { params })

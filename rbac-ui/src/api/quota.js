import request from './request.js'
export const quotaList = (params) => request.get('/api/quota/list', { params })
export const quotaDetail = (tenantId) => request.get('/api/quota/' + tenantId)
export const adjustSurplus = (tenantId, data) => request.put('/api/quota/' + tenantId + '/surplus', data)
export const resetFree = (tenantId) => request.put('/api/quota/' + tenantId + '/reset')

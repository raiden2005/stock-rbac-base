import request from './request.js'
export const list = () => request.get('/api/plan/list')
export const update = (data) => request.put('/api/plan/update', data)

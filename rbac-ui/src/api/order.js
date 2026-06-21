import request from './request.js'
export const subOrderList = (params) => request.get('/api/order/sub/list', { params })
export const questionOrderList = (params) => request.get('/api/order/question/list', { params })
export const createSub = (data) => request.post('/api/order/sub/create', data)
export const paySub = (id) => request.post('/api/order/sub/' + id + '/pay')
export const createQuestionOrder = (data) => request.post('/api/order/question/create', data)

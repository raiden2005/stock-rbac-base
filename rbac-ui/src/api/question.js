import request from './request.js'
export const submit = (data) => request.post('/api/question/submit', data)
export const myList = (params) => request.get('/api/question/my', { params })
export const detail = (id) => request.get('/api/question/' + id)
export const adminList = (params) => request.get('/api/question/list', { params })

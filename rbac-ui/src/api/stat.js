import request from './request.js'
export const home = () => request.get('/api/stat/home')

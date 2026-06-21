import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router/index.js'

const request = axios.create({
  baseURL: '/',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：注入 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('rbac-user')
    ? JSON.parse(localStorage.getItem('rbac-user'))?.token
    : ''
  if (token) config.headers['Authorization'] = token
  return config
}, (err) => Promise.reject(err))

// 响应拦截器：统一错误处理 + 401 重定向
request.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data?.code !== 200) {
      ElMessage.error(data?.msg || '请求失败')
      return Promise.reject(data)
    }
    return data
  },
  (err) => {
    if (err.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request

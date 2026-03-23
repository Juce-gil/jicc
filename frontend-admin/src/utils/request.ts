import axios from 'axios'
import { ADMIN_AUTH_STORAGE_KEY } from '@/stores/auth'

export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
  total?: number
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/campus-product-sys/v1.0',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const raw = localStorage.getItem(ADMIN_AUTH_STORAGE_KEY)
  if (raw) {
    try {
      const parsed = JSON.parse(raw)
      if (parsed?.token) {
        config.headers.token = parsed.token
        config.headers.Authorization = `Bearer ${parsed.token}`
      }
    } catch (error) {
      console.warn('读取管理端登录态失败', error)
    }
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data as ApiResponse,
  (error) => Promise.reject(error)
)

export default request

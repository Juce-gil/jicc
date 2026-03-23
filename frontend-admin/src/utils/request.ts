import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ADMIN_AUTH_STORAGE_KEY } from '@/stores/auth'

export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
  total?: number
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/campus-product-sys/v1.0',
  timeout: 10000
})

service.interceptors.request.use((config) => {
  const raw = localStorage.getItem(ADMIN_AUTH_STORAGE_KEY)
  if (raw) {
    try {
      const parsed = JSON.parse(raw)
      if (parsed?.token) {
        config.headers = {
          ...(config.headers ?? {}),
          token: parsed.token,
          Authorization: `Bearer ${parsed.token}`
        }
      }
    } catch (error) {
      console.warn('读取管理端登录态失败', error)
    }
  }
  return config
})

const unwrap = async <T>(requestPromise: Promise<AxiosResponse<ApiResponse<T>>>) => {
  const response = await requestPromise
  return response.data
}

const request = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(service.get<ApiResponse<T>>(url, config))
  },
  delete<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(service.delete<ApiResponse<T>>(url, config))
  },
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(service.post<ApiResponse<T>>(url, data, config))
  },
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(service.put<ApiResponse<T>>(url, data, config))
  },
  patch<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(service.patch<ApiResponse<T>>(url, data, config))
  },
  instance: service
}

export default request

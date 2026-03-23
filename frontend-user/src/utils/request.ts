import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { USER_AUTH_STORAGE_KEY } from '@/stores/auth'

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
  const raw = localStorage.getItem(USER_AUTH_STORAGE_KEY)
  if (raw) {
    try {
      const parsed = JSON.parse(raw)
      if (parsed?.token) {
        config.headers.token = parsed.token
        config.headers.Authorization = 'Bearer ' + parsed.token
      }
    } catch (error) {
      console.warn('Failed to restore user auth snapshot', error)
    }
  }
  return config
})

const unwrap = async <T>(promise: Promise<AxiosResponse<ApiResponse<T>>>) => {
  const response = await promise
  return response.data
}

const request = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(service.get<ApiResponse<T>>(url, config))
  },
  delete<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(service.delete<ApiResponse<T>>(url, config))
  },
  post<T = unknown, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
    return unwrap<T>(service.post<ApiResponse<T>, AxiosResponse<ApiResponse<T>>, D>(url, data, config))
  },
  put<T = unknown, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
    return unwrap<T>(service.put<ApiResponse<T>, AxiosResponse<ApiResponse<T>>, D>(url, data, config))
  },
  patch<T = unknown, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig<D>) {
    return unwrap<T>(service.patch<ApiResponse<T>, AxiosResponse<ApiResponse<T>>, D>(url, data, config))
  }
}

export default request

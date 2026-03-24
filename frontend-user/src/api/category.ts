import request from '@/utils/request'

export interface CategoryRecord {
  id?: number
  name?: string
  isUse?: boolean
}

export interface CategoryQueryPayload {
  id?: number
  name?: string
  current?: number
  size?: number
}

export const queryCategories = (data: CategoryQueryPayload = {}) =>
  request.post<CategoryRecord[]>('/category/query', data)

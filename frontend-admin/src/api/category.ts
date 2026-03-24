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

export interface UpsertCategoryPayload {
  id?: number
  name: string
  isUse?: boolean
}

export const queryCategories = (data: CategoryQueryPayload = {}) =>
  request.post<CategoryRecord[]>('/category/query', data)
export const createCategory = (data: UpsertCategoryPayload) => request.post<null>('/category/save', data)
export const updateCategory = (data: UpsertCategoryPayload) => request.put<null>('/category/update', data)

import request from '@/utils/request'

export const queryCategories = (data: Record<string, unknown> = {}) => request.post('/category/query', data)
export const createCategory = (data: Record<string, unknown>) => request.post('/category/save', data)
export const updateCategory = (data: Record<string, unknown>) => request.put('/category/update', data)

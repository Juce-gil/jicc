import request from '@/utils/request'

export const queryCategories = (data: Record<string, unknown> = {}) => request.post('/category/query', data)

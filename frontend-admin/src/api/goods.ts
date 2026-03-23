import request from '@/utils/request'

export const queryGoods = (data: Record<string, unknown> = {}) => request.post('/product/query', data)
export const updateGoods = (data: Record<string, unknown>) => request.put('/product/update', data)
export const removeGoods = (ids: number[]) => request.post('/product/batchDelete', ids)

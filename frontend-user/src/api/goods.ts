import request from '@/utils/request'

export const queryGoods = (data: Record<string, unknown> = {}) => request.post('/product/query', data)
export const getGoodsDetail = (id: number) => request.post('/product/query', { id })
export const queryMyGoods = (data: Record<string, unknown> = {}) => request.post('/product/queryUser', data)
export const publishGoods = (data: Record<string, unknown>) => request.post('/product/save', data)
export const updateGoods = (data: Record<string, unknown>) => request.put('/product/update', data)
export const reserveGoods = (data: Record<string, unknown>) => request.post('/product/buyProduct', data)
export const queryGoodsMetrics = () => request.post('/product/queryProductInfo', {})

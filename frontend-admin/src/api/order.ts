import request from '@/utils/request'

export const queryOrders = (data: Record<string, unknown> = {}) => request.post('/orders/query', data)

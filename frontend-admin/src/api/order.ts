import request from '@/utils/request'

export interface OrderRecord {
  id?: number
  code?: string
  detail?: string
  userId?: number
  productId?: number
  buyPrice?: number | string
  buyNumber?: number
  tradeStatus?: number
  refundStatus?: number | null
  createTime?: string
  cancelTime?: string | null
  cancelReason?: string | null
  userName?: string
  productTitle?: string
  sellerId?: number
  sellerName?: string
}

export interface OrderQueryPayload {
  id?: number
  code?: string
  tradeStatus?: number
  userId?: number
  productId?: number
  current?: number
  size?: number
}

export const queryOrders = (data: OrderQueryPayload = {}) => request.post<OrderRecord[]>('/orders/query', data)

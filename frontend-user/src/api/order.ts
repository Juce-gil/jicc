import request, { type ApiResponse } from '@/utils/request'

export interface OrderRecord {
  id?: number
  code?: string | null
  detail?: string | null
  userId?: number | null
  productId?: number | null
  buyPrice?: number | string | null
  buyNumber?: number | null
  tradeStatus?: number | null
  refundStatus?: number | null
  refundTime?: string | null
  tradeTime?: string | null
  isRefundConfirm?: boolean | null
  createTime?: string | null
  addressId?: number | null
  isConfirm?: boolean | null
  isConfirmTime?: string | null
  isDeliver?: boolean | null
  deliverAdrId?: number | null
  deliverTime?: string | null
  appointmentTime?: string | null
  appointmentAddress?: string | null
  sellerConfirmTime?: string | null
  buyerConfirmTime?: string | null
  sellerFinishTime?: string | null
  cancelTime?: string | null
  cancelReason?: string | null
  userName?: string | null
  userAvatar?: string | null
  productTitle?: string | null
  productCover?: string | null
  sellerId?: number | null
  sellerName?: string | null
  sellerAvatar?: string | null
  concatPerson?: string | null
  getAdr?: string | null
  concatPhone?: string | null
  adrName?: string | null
  adrPhone?: string | null
  adr?: string | null
}

export interface OrderQuery {
  code?: string
  tradeStatus?: number | null
}

const toResponse = <T>(promise: Promise<unknown>) => promise as Promise<ApiResponse<T>>

export const queryMyOrders = (data: OrderQuery = {}) =>
  toResponse<OrderRecord[]>(request.post('/orders/queryUser', data))

export const querySellerOrders = (data: OrderQuery = {}) =>
  toResponse<OrderRecord[]>(request.post('/orders/queryOrdersList', data))

export const sellerConfirmReservation = (orderId: number) =>
  toResponse<unknown>(request.post('/product/placeAnOrder/' + orderId))

export const cancelReservation = (orderId: number) =>
  toResponse<unknown>(request.post('/product/refund/' + orderId))

export const buyerConfirmTrade = (orderId: number) =>
  toResponse<unknown>(request.post('/product/getGoods/' + orderId))

export const sellerConfirmTrade = (orderId: number) =>
  toResponse<unknown>(request.post('/product/confirmTradeBySeller/' + orderId))

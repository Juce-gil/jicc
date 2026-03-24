import request from '@/utils/request'

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

export interface OrderActionResult {
  orderId?: number
  orderCode?: string
  beforeTradeStatus?: number | null
  afterTradeStatus?: number | null
  beforeTradeStatusName?: string | null
  afterTradeStatusName?: string | null
  action?: string
  message?: string
}

export const queryMyOrders = (data: OrderQuery = {}) =>
  request.post<OrderRecord[]>('/orders/queryUser', data)

export const querySellerOrders = (data: OrderQuery = {}) =>
  request.post<OrderRecord[]>('/orders/queryOrdersList', data)

export const sellerConfirmReservation = (orderId: number) =>
  request.post<OrderActionResult>('/product/placeAnOrder/' + orderId)

export const cancelReservation = (orderId: number) =>
  request.post<OrderActionResult>('/product/refund/' + orderId)

export const buyerConfirmTrade = (orderId: number) =>
  request.post<OrderActionResult>('/product/getGoods/' + orderId)

export const sellerConfirmTrade = (orderId: number) =>
  request.post<OrderActionResult>('/product/confirmTradeBySeller/' + orderId)

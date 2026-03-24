import request from '@/utils/request'

export type ProductStatus = 'ON_SALE' | 'RESERVED' | 'SOLD' | 'OFFLINE'
export type ProductAuditStatus = 'APPROVED' | 'PENDING' | 'REJECTED'

export interface ProductRecord {
  id?: number
  name?: string
  detail?: string
  coverList?: string
  oldLevel?: number
  categoryId?: number
  categoryName?: string
  userId?: number
  userName?: string
  userAvatar?: string
  inventory?: number
  price?: number | string
  isBargain?: boolean
  status?: ProductStatus
  auditStatus?: ProductAuditStatus
  likeNumber?: number
  saveNumber?: number
  viewNumber?: number
  createTime?: string
}

export interface ProductQueryPayload {
  id?: number
  name?: string
  categoryId?: number
  userId?: number
  status?: ProductStatus
  auditStatus?: ProductAuditStatus
  current?: number
  size?: number
}

export interface PublishGoodsPayload {
  id?: number
  name: string
  detail?: string
  coverList: string
  oldLevel?: number
  categoryId: number
  inventory?: number
  price: number
  isBargain?: boolean
}

export interface ReserveGoodsPayload {
  productId: number
  detail?: string
  buyNumber?: number
  appointmentTime?: string
  appointmentAddress?: string
}

export interface ChartRecord {
  name: string
  value: number
}

export const queryGoods = (data: ProductQueryPayload = {}) => request.post<ProductRecord[]>('/product/query', data)
export const getGoodsDetail = (id: number) => request.post<ProductRecord[]>('/product/query', { id })
export const queryMyGoods = (data: ProductQueryPayload = {}) => request.post<ProductRecord[]>('/product/queryUser', data)
export const publishGoods = (data: PublishGoodsPayload) => request.post<null>('/product/save', data)
export const updateGoods = (data: PublishGoodsPayload) => request.put<null>('/product/update', data)
export const reserveGoods = (data: ReserveGoodsPayload) => request.post('/product/buyProduct', data)
export const queryGoodsMetrics = () => request.post<ChartRecord[]>('/product/queryProductInfo', {})

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
  createTime?: string
}

export interface ProductQueryPayload {
  id?: number
  name?: string
  userId?: number
  categoryId?: number
  status?: ProductStatus
  auditStatus?: ProductAuditStatus
  current?: number
  size?: number
}

export interface UpdateGoodsPayload {
  id: number
  name: string
  coverList: string
  categoryId: number
  price: number
  detail?: string
  oldLevel?: number
  inventory?: number
  isBargain?: boolean
  status?: ProductStatus
  auditStatus?: ProductAuditStatus
}

export const queryGoods = (data: ProductQueryPayload = {}) => request.post<ProductRecord[]>('/product/query', data)
export const updateGoods = (data: UpdateGoodsPayload) => request.put<null>('/product/update', data)
export const removeGoods = (ids: number[]) => request.post<null>('/product/batchDelete', ids)

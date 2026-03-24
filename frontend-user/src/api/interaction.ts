import request from '@/utils/request'
import type { ProductRecord } from '@/api/goods'

export const toggleFavorite = (goodsId: number) => request.post<boolean>(`/interaction/saveOperation/${goodsId}`)
export const likeGoods = (goodsId: number) => request.post<null>(`/interaction/likeProduct/${goodsId}`)
export const recordView = (goodsId: number) => request.post<null>(`/interaction/view/${goodsId}`)
export const queryMyFavorites = () => request.post<ProductRecord[]>('/interaction/queryUser')
export const queryMyViews = () => request.post<ProductRecord[]>('/interaction/myView')

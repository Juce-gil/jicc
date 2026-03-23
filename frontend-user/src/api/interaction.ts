import request from '@/utils/request'

export const toggleFavorite = (goodsId: number) => request.post(`/interaction/saveOperation/${goodsId}`)
export const likeGoods = (goodsId: number) => request.post(`/interaction/likeProduct/${goodsId}`)
export const recordView = (goodsId: number) => request.post(`/interaction/view/${goodsId}`)
export const queryMyFavorites = () => request.post('/interaction/queryUser')
export const queryMyViews = () => request.post('/interaction/myView')

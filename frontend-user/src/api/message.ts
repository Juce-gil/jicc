import request from '@/utils/request'

export interface MessageRecord {
  id?: number
  userId?: number
  content?: string
  isRead?: boolean
  createTime?: string
  userName?: string
}

export interface MessageQueryPayload {
  current?: number
  size?: number
  isRead?: boolean
}

export const queryMyMessages = (data: MessageQueryPayload = {}) =>
  request.post<MessageRecord[]>('/message/queryUser', data)
export const setAllRead = () => request.post<null>('/message/setRead')

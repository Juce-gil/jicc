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
  id?: number
  userId?: number
  isRead?: boolean
  current?: number
  size?: number
}

export const queryMessages = (data: MessageQueryPayload = {}) => request.post<MessageRecord[]>('/message/query', data)

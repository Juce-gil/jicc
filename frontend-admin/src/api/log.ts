import request from '@/utils/request'

export interface OperationLogRecord {
  id?: number
  userId?: number
  detail?: string
  createTime?: string
  userName?: string
}

export interface OperationLogQueryPayload {
  id?: number
  userId?: number
  current?: number
  size?: number
}

export const queryOperationLogs = (data: OperationLogQueryPayload = {}) =>
  request.post<OperationLogRecord[]>('/operation-log/query', data)

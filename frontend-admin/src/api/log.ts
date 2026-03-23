import request from '@/utils/request'

export const queryOperationLogs = (data: Record<string, unknown> = {}) => request.post('/operation-log/query', data)

import request from '@/utils/request'

export const queryMessages = (data: Record<string, unknown> = {}) => request.post('/message/query', data)

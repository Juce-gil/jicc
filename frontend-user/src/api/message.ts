import request from '@/utils/request'

export const queryMyMessages = (data: Record<string, unknown> = {}) => request.post('/message/queryUser', data)
export const setAllRead = () => request.post('/message/setRead')

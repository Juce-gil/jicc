import request from '@/utils/request'

export const queryUsers = (data: Record<string, unknown> = {}) => request.post('/user/query', data)
export const createUser = (data: Record<string, unknown>) => request.post('/user/insert', data)
export const updateUser = (data: Record<string, unknown>) => request.put('/user/backUpdate', data)

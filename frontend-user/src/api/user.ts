import request from '@/utils/request'

export const getUserById = (id: number) => request.get(`/user/getById/${id}`)
export const updateProfile = (data: Record<string, unknown>) => request.put('/user/update', data)
export const updatePassword = (data: Record<string, string>) => request.put('/user/updatePwd', data)

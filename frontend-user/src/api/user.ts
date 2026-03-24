import request from '@/utils/request'
import type { UserProfile } from '@/api/auth'

export interface UpdateProfilePayload {
  userAccount?: string
  userName?: string
  userPwd?: string
  userEmail?: string
  userAvatar?: string
}

export interface UpdatePasswordPayload {
  oldPwd: string
  newPwd: string
  againPwd: string
}

export const getUserById = (id: number) => request.get<UserProfile>(`/user/getById/${id}`)
export const updateProfile = (data: UpdateProfilePayload) => request.put<null>('/user/update', data)
export const updatePassword = (data: UpdatePasswordPayload) => request.put<null>('/user/updatePwd', data)

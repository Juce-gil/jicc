import request from '@/utils/request'

export interface LoginPayload {
  userAccount: string
  userPwd: string
}

export interface LoginResponseData {
  token: string
  role: number | null
}

export interface AdminProfile {
  id?: number
  userAccount?: string
  userName?: string
  userEmail?: string
  userAvatar?: string
  userRole?: number
  [key: string]: unknown
}

export const login = (data: LoginPayload) => request.post<LoginResponseData>('/user/login', data)
export const auth = () => request.get<AdminProfile>('/user/auth')

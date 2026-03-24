import request from '@/utils/request'

export interface LoginPayload {
  userAccount: string
  userPwd: string
}

export interface RegisterPayload {
  userName: string
  userAccount: string
  userPwd: string
  userEmail?: string
  userAvatar?: string
}

export interface LoginResponseData {
  token: string
  role: number | null
}

export interface UserProfile {
  id?: number
  userAccount?: string
  userName?: string
  userEmail?: string
  userAvatar?: string
  userRole?: number
  campusName?: string
  studentNo?: string
  campusVerified?: boolean
  creditScore?: number
  [key: string]: unknown
}

export const login = (data: LoginPayload) => request.post<LoginResponseData>('/user/login', data)
export const register = (data: RegisterPayload) => request.post<null>('/user/register', data)
export const auth = () => request.get<UserProfile>('/user/auth')

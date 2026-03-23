import request from '@/utils/request'

export interface LoginPayload {
  userAccount: string
  userPwd: string
}

export interface LoginResponseData {
  token?: string
  role?: number | null
  [key: string]: unknown
}

export const login = (data: LoginPayload) => request.post<LoginResponseData>('/user/login', data)
export const auth = () => request.get<Record<string, unknown> | null>('/user/auth')

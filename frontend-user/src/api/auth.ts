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

export const login = (data: LoginPayload) => request.post('/user/login', data)
export const register = (data: RegisterPayload) => request.post('/user/register', data)
export const auth = () => request.get('/user/auth')

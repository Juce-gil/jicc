import request from '@/utils/request'

export interface LoginPayload {
  userAccount: string
  userPwd: string
}

export const login = (data: LoginPayload) => request.post('/user/login', data)
export const auth = () => request.get('/user/auth')

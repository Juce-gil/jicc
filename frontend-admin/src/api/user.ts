import request from '@/utils/request'

export interface UserRecord {
  id?: number
  userAccount?: string
  userName?: string
  userPwd?: string
  userAvatar?: string
  userEmail?: string
  userRole?: number
  isLogin?: boolean
  isWord?: boolean
  lastLoginTime?: string
  createTime?: string
}

export interface UserQueryPayload {
  id?: number
  userAccount?: string
  userName?: string
  userEmail?: string
  role?: number
  isLogin?: boolean
  isWord?: boolean
  current?: number
  size?: number
}

export interface UpsertUserPayload {
  id?: number
  userAccount: string
  userName: string
  userPwd: string
  userAvatar?: string
  userEmail?: string
  userRole?: number
  isLogin?: boolean
  isWord?: boolean
}

export const queryUsers = (data: UserQueryPayload = {}) => request.post<UserRecord[]>('/user/query', data)
export const createUser = (data: UpsertUserPayload) => request.post<null>('/user/insert', data)
export const updateUser = (data: UpsertUserPayload) => request.put<null>('/user/backUpdate', data)

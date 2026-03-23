import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export const ADMIN_AUTH_STORAGE_KEY = 'campus-trade-admin-auth'

export interface AdminAuthSnapshot {
  token: string
  role: number | null
  profile: Record<string, unknown> | null
}

export function readAdminAuthSnapshot(): AdminAuthSnapshot {
  const raw = localStorage.getItem(ADMIN_AUTH_STORAGE_KEY)
  if (!raw) {
    return { token: '', role: null, profile: null }
  }

  try {
    const parsed = JSON.parse(raw)
    return {
      token: parsed?.token ?? '',
      role: typeof parsed?.role === 'number' ? parsed.role : null,
      profile: parsed?.profile ?? null
    }
  } catch (error) {
    console.warn('解析管理端登录态失败', error)
    return { token: '', role: null, profile: null }
  }
}

export const useAdminAuthStore = defineStore('admin-auth', () => {
  const snapshot = readAdminAuthSnapshot()
  const token = ref(snapshot.token)
  const role = ref<number | null>(snapshot.role)
  const profile = ref<Record<string, unknown> | null>(snapshot.profile)

  const persist = () => {
    localStorage.setItem(
      ADMIN_AUTH_STORAGE_KEY,
      JSON.stringify({
        token: token.value,
        role: role.value,
        profile: profile.value
      })
    )
  }

  const isLoggedIn = computed(() => Boolean(token.value))

  const setSession = (nextToken: string, nextRole: number | null) => {
    token.value = nextToken
    role.value = nextRole
    persist()
  }

  const setProfile = (nextProfile: Record<string, unknown> | null) => {
    profile.value = nextProfile
    persist()
  }

  const clearSession = () => {
    token.value = ''
    role.value = null
    profile.value = null
    localStorage.removeItem(ADMIN_AUTH_STORAGE_KEY)
  }

  return {
    token,
    role,
    profile,
    isLoggedIn,
    setSession,
    setProfile,
    clearSession
  }
})

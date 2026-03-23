<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { md5 } from 'js-md5'
import { auth, login } from '@/api/auth'
import { useUserAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useUserAuthStore()

const form = reactive({
  userAccount: '',
  userPwd: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!form.userAccount.trim() || !form.userPwd.trim()) {
    errorMessage.value = '请输入账号和密码。'
    return
  }

  loading.value = true
  try {
    const response: any = await login({
      userAccount: form.userAccount.trim(),
      userPwd: md5(form.userPwd.trim())
    })

    if (response?.code !== 200) {
      errorMessage.value = response?.msg || '登录失败，请稍后重试。'
      return
    }

    const token = response?.data?.token || ''
    const role = response?.data?.role ?? null

    if (!token) {
      errorMessage.value = '登录接口未返回 token。'
      return
    }

    if (role !== 2) {
      errorMessage.value = '当前账号不是学生用户，请使用学生账号登录学生端。'
      return
    }

    authStore.setSession(token, role)

    const profileResponse: any = await auth()
    if (profileResponse?.code === 200) {
      authStore.setProfile(profileResponse.data || null)
    }

    successMessage.value = '登录成功，正在进入学生端首页。'
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    window.setTimeout(() => router.push(redirect), 300)
  } catch (error) {
    console.error(error)
    errorMessage.value = '请求失败，请确认后端服务已启动。'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-shell page-shell">
    <section class="auth-card panel">
      <div class="auth-heading">
        <p class="auth-kicker">学生端登录</p>
        <h1>校园二手交易平台</h1>
        <p class="muted">当前登录页已经接入现有后端的 /user/login 与 /user/auth，可直接作为新学生端入口继续迭代。</p>
      </div>

      <div class="form-grid">
        <label>
          <span>账号</span>
          <input v-model="form.userAccount" class="form-input" placeholder="请输入账号" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.userPwd" type="password" class="form-input" placeholder="请输入密码" @keyup.enter="handleSubmit" />
        </label>
      </div>

      <p v-if="errorMessage" class="message error">{{ errorMessage }}</p>
      <p v-if="successMessage" class="message success">{{ successMessage }}</p>

      <div class="auth-actions">
        <button class="primary-btn" :disabled="loading" @click="handleSubmit">
          {{ loading ? '登录中...' : '立即登录' }}
        </button>
        <RouterLink class="secondary-btn" to="/register">没有账号？去注册</RouterLink>
      </div>

      <div class="auth-tip panel">
        <strong>兼容说明</strong>
        <p class="muted">
          为兼容你当前旧后端的密码存储方式，这里沿用了 MD5 提交策略，便于直接对接现有用户表与管理员账号。
        </p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.auth-shell {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.auth-card {
  width: min(100%, 520px);
  display: grid;
  gap: 18px;
}

.auth-heading h1 {
  margin: 6px 0 8px;
}

.auth-kicker {
  margin: 0;
  color: #2563eb;
  font-weight: 600;
}

.auth-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.message {
  margin: 0;
  padding: 10px 12px;
  border-radius: 12px;
}

.error {
  background: #fef2f2;
  color: #b91c1c;
}

.success {
  background: #f0fdf4;
  color: #166534;
}

.auth-tip {
  background: #f8fafc;
}
</style>

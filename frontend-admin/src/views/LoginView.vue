<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { md5 } from 'js-md5'
import { auth, login, type LoginResponseData } from '@/api/auth'
import type { ApiResponse } from '@/utils/request'
import { useAdminAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAdminAuthStore()

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
    errorMessage.value = '请输入管理员账号和密码。'
    return
  }

  loading.value = true
  try {
    const response = (await login({
      userAccount: form.userAccount.trim(),
      userPwd: md5(form.userPwd.trim())
    })) as ApiResponse<LoginResponseData>

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

    if (role !== 1) {
      errorMessage.value = '当前账号不是管理员，请使用管理员账号登录后台。'
      return
    }

    authStore.setSession(token, role)

    const profileResponse = (await auth()) as ApiResponse<Record<string, unknown> | null>
    if (profileResponse?.code === 200) {
      authStore.setProfile(profileResponse.data || null)
    }

    successMessage.value = '登录成功，正在进入管理后台。'
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
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
  <div class="login-shell">
    <section class="login-card panel">
      <div>
        <p class="login-kicker">管理员入口</p>
        <h1>校园二手交易平台管理后台</h1>
        <p class="muted">当前登录页已接入现有后端 /user/login 与 /user/auth，可直接作为新后台工程的入口。</p>
      </div>

      <div class="form-grid">
        <label>
          <span>管理员账号</span>
          <input v-model="form.userAccount" class="form-input" placeholder="请输入管理员账号" />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model="form.userPwd"
            type="password"
            class="form-input"
            placeholder="请输入密码"
            @keyup.enter="handleSubmit"
          />
        </label>
      </div>

      <p v-if="errorMessage" class="message error">{{ errorMessage }}</p>
      <p v-if="successMessage" class="message success">{{ successMessage }}</p>

      <div class="login-actions">
        <button class="primary-btn" :disabled="loading" @click="handleSubmit">
          {{ loading ? '登录中...' : '进入后台' }}
        </button>
      </div>

      <div class="panel tip-box">
        <strong>当前建议</strong>
        <p class="muted">下一步优先把“商品审核 / 校园认证审核 / 举报处理 / 公告管理 / 仪表盘统计”接入真实接口。</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.login-card {
  width: min(100%, 560px);
  display: grid;
  gap: 18px;
}

.login-kicker {
  margin: 0;
  color: #0f766e;
  font-weight: 600;
}

.login-card h1 {
  margin: 6px 0 8px;
}

.login-actions {
  display: flex;
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

.tip-box {
  background: #f8fafc;
}
</style>

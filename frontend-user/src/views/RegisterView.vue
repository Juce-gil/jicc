<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { md5 } from 'js-md5'
import { register } from '@/api/auth'

const router = useRouter()

const form = reactive({
  userName: '',
  userAccount: '',
  userEmail: '',
  userPwd: '',
  confirmPwd: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!form.userName.trim() || !form.userAccount.trim() || !form.userPwd.trim()) {
    errorMessage.value = '昵称、账号、密码不能为空。'
    return
  }

  if (form.userPwd !== form.confirmPwd) {
    errorMessage.value = '两次输入的密码不一致。'
    return
  }

  loading.value = true
  try {
    const response: any = await register({
      userName: form.userName.trim(),
      userAccount: form.userAccount.trim(),
      userEmail: form.userEmail.trim(),
      userPwd: md5(form.userPwd.trim())
    })

    if (response?.code !== 200) {
      errorMessage.value = response?.msg || '注册失败，请稍后重试。'
      return
    }

    successMessage.value = '注册成功，正在跳转到登录页。'
    window.setTimeout(() => router.push('/login'), 500)
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
        <p class="auth-kicker">学生端注册</p>
        <h1>创建校园交易账号</h1>
        <p class="muted">当前表单已经对接现有后端 /user/register，后续可以继续补头像上传、校园认证引导和协议确认。</p>
      </div>

      <div class="form-grid">
        <label>
          <span>昵称</span>
          <input v-model="form.userName" class="form-input" placeholder="请输入昵称" />
        </label>
        <label>
          <span>账号</span>
          <input v-model="form.userAccount" class="form-input" placeholder="请输入登录账号" />
        </label>
        <label>
          <span>邮箱</span>
          <input v-model="form.userEmail" class="form-input" placeholder="请输入邮箱（可选）" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.userPwd" type="password" class="form-input" placeholder="请输入密码" />
        </label>
        <label>
          <span>确认密码</span>
          <input v-model="form.confirmPwd" type="password" class="form-input" placeholder="请再次输入密码" @keyup.enter="handleSubmit" />
        </label>
      </div>

      <p v-if="errorMessage" class="message error">{{ errorMessage }}</p>
      <p v-if="successMessage" class="message success">{{ successMessage }}</p>

      <div class="auth-actions">
        <button class="primary-btn" :disabled="loading" @click="handleSubmit">
          {{ loading ? '提交中...' : '注册账号' }}
        </button>
        <RouterLink class="secondary-btn" to="/login">已有账号？去登录</RouterLink>
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
  width: min(100%, 560px);
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
</style>

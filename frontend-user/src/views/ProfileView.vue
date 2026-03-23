<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useUserAuthStore } from '@/stores/auth'

const authStore = useUserAuthStore()
const { token, role, profile, isLoggedIn } = storeToRefs(authStore)
</script>

<template>
  <section class="profile-view panel">
    <div>
      <h2>个人中心 / 登录态检查</h2>
      <p class="muted">这个页面用于承接学生端个人资料、校园认证入口、我的统计信息等后续能力。</p>
    </div>

    <div class="profile-grid">
      <div class="profile-item">
        <strong>登录状态</strong>
        <span>{{ isLoggedIn ? '已登录' : '未登录' }}</span>
      </div>
      <div class="profile-item">
        <strong>角色</strong>
        <span>{{ role === 1 ? '管理员' : role === 2 ? '学生用户' : '未知' }}</span>
      </div>
      <div class="profile-item">
        <strong>Token</strong>
        <span class="token-text">{{ token || '暂无' }}</span>
      </div>
    </div>

    <div class="panel profile-json">
      <h3>当前用户资料</h3>
      <pre>{{ JSON.stringify(profile, null, 2) }}</pre>
    </div>
  </section>
</template>

<style scoped>
.profile-view {
  display: grid;
  gap: 18px;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.profile-item {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px;
  background: #f8fafc;
  display: grid;
  gap: 8px;
}

.profile-json {
  background: #0f172a;
  color: #e2e8f0;
}

.profile-json pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.token-text {
  word-break: break-all;
}
</style>

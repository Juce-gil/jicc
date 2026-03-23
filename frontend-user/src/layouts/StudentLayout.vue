<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUserAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useUserAuthStore()
const { isLoggedIn, role } = storeToRefs(authStore)

const navItems = [
  { label: '首页', to: '/' },
  { label: '分类', to: '/categories' },
  { label: '搜索', to: '/search' },
  { label: '商品详情', to: '/goods/detail' },
  { label: '发布商品', to: '/publish' },
  { label: '我的发布', to: '/my/goods' },
  { label: '我的收藏', to: '/my/favorites' },
  { label: '我的订单', to: '/my/orders' },
  { label: '聊天', to: '/chat' },
  { label: '校园认证', to: '/my/campus-auth' },
  { label: '个人中心', to: '/my/profile' }
]

const logout = () => {
  authStore.clearSession()
  router.push('/login')
}
</script>

<template>
  <div class="page-shell student-shell">
    <section class="student-hero panel">
      <div>
        <p class="hero-kicker">PLAN.md 定稿方向已落地到新前端骨架</p>
        <h1>校园二手交易平台 · 学生端</h1>
        <p class="muted hero-copy">
          当前版本重点完成：Vue3 + Vite + TS 脚手架、学生端路由规划、登录注册、接口封装与基础路由守卫。
        </p>
        <div class="tag-list">
          <span class="tag">后端复用</span>
          <span class="tag">前端重建</span>
          <span class="tag">移动优先 Web/H5 风格</span>
          <span class="tag">保留旧前端作参考</span>
        </div>
      </div>
      <div class="hero-actions">
        <template v-if="isLoggedIn">
          <div class="panel hero-user-card">
            <strong>当前状态：已登录</strong>
            <span class="muted">角色：{{ role === 1 ? '管理员' : '学生用户' }}</span>
            <button class="secondary-btn" @click="logout">退出登录</button>
          </div>
        </template>
        <template v-else>
          <div class="action-group">
            <RouterLink class="primary-btn" to="/login">登录</RouterLink>
            <RouterLink class="secondary-btn" to="/register">注册</RouterLink>
          </div>
        </template>
      </div>
    </section>

    <nav class="student-nav panel">
      <RouterLink
        v-for="item in navItems"
        :key="item.to"
        :to="item.to"
        class="nav-link"
        active-class="nav-link-active"
      >
        {{ item.label }}
      </RouterLink>
    </nav>

    <RouterView />
  </div>
</template>

<style scoped>
.student-shell {
  display: grid;
  gap: 18px;
}

.student-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
}

.hero-kicker {
  margin: 0 0 6px;
  color: #2563eb;
  font-weight: 600;
}

.student-hero h1 {
  margin: 0 0 10px;
  font-size: 32px;
}

.hero-copy {
  max-width: 720px;
}

.hero-actions {
  min-width: 260px;
}

.hero-user-card {
  display: grid;
  gap: 8px;
  min-width: 220px;
}

.action-group {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.student-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.nav-link {
  padding: 10px 14px;
  border-radius: 12px;
  background: #f8fafc;
  color: #475569;
  transition: all 0.2s ease;
}

.nav-link:hover,
.nav-link-active {
  background: #2563eb;
  color: #fff;
}

@media (max-width: 900px) {
  .student-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions,
  .hero-user-card {
    width: 100%;
  }

  .action-group {
    justify-content: flex-start;
  }
}
</style>

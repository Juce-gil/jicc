<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAdminAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAdminAuthStore()
const { profile } = storeToRefs(authStore)

const menus = [
  { label: '仪表盘', to: '/dashboard' },
  { label: '用户管理', to: '/users' },
  { label: '校园认证审核', to: '/campus-auth-review' },
  { label: '商品审核', to: '/goods-review' },
  { label: '商品管理', to: '/goods-manage' },
  { label: '订单管理', to: '/orders' },
  { label: '举报处理', to: '/reports' },
  { label: '聊天抽查', to: '/chat-audit' },
  { label: '公告管理', to: '/notices' },
  { label: '统计报表', to: '/stats' },
  { label: '操作日志', to: '/logs' }
]

const logout = () => {
  authStore.clearSession()
  router.push('/login')
}
</script>

<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="brand-box">
        <p>校园二手交易平台</p>
        <h1>管理后台</h1>
      </div>

      <nav class="menu-list">
        <RouterLink
          v-for="item in menus"
          :key="item.to"
          :to="item.to"
          class="menu-link"
          active-class="menu-link-active"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <div class="admin-content-wrap">
      <header class="admin-header panel">
        <div>
          <h2>后台管理骨架已完成</h2>
          <p class="muted">当前重点完成：后台独立工程、登录态、路由菜单、管理模块占位与可对接 API 封装。</p>
        </div>
        <div class="header-actions">
          <span class="muted">{{ profile?.userName || profile?.nickname || '管理员' }}</span>
          <button class="secondary-btn" @click="logout">退出登录</button>
        </div>
      </header>

      <main class="admin-main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
}

.admin-sidebar {
  background: linear-gradient(180deg, #0f172a 0%, #111827 100%);
  color: #e2e8f0;
  padding: 24px 18px;
  display: grid;
  gap: 20px;
}

.brand-box p {
  margin: 0;
  color: #94a3b8;
}

.brand-box h1 {
  margin: 6px 0 0;
  font-size: 26px;
}

.menu-list {
  display: grid;
  gap: 8px;
}

.menu-link {
  padding: 12px 14px;
  border-radius: 12px;
  color: #cbd5e1;
  transition: all 0.2s ease;
}

.menu-link:hover,
.menu-link-active {
  background: rgba(20, 184, 166, 0.16);
  color: #fff;
}

.admin-content-wrap {
  padding: 18px;
  display: grid;
  gap: 18px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.admin-main {
  display: grid;
}

@media (max-width: 980px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    grid-template-columns: 1fr;
  }

  .admin-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

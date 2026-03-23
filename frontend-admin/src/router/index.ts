import { createRouter, createWebHashHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { readAdminAuthSnapshot } from '@/stores/auth'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guestOnly: true }
    },
    {
      path: '/',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: () => import('@/views/DashboardView.vue') },
        { path: 'users', name: 'users', component: () => import('@/views/UserManageView.vue') },
        { path: 'campus-auth-review', name: 'campus-auth-review', component: () => import('@/views/CampusAuthReviewView.vue') },
        { path: 'goods-review', name: 'goods-review', component: () => import('@/views/GoodsReviewView.vue') },
        { path: 'goods-manage', name: 'goods-manage', component: () => import('@/views/GoodsManageView.vue') },
        { path: 'orders', name: 'orders', component: () => import('@/views/OrdersManageView.vue') },
        { path: 'reports', name: 'reports', component: () => import('@/views/ReportManageView.vue') },
        { path: 'chat-audit', name: 'chat-audit', component: () => import('@/views/ChatAuditView.vue') },
        { path: 'notices', name: 'notices', component: () => import('@/views/NoticeManageView.vue') },
        { path: 'stats', name: 'stats', component: () => import('@/views/StatisticsView.vue') },
        { path: 'logs', name: 'logs', component: () => import('@/views/OperationLogView.vue') }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const auth = readAdminAuthSnapshot()

  if (to.meta.requiresAuth && !auth.token) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }

  if (to.meta.guestOnly && auth.token) {
    return { path: '/dashboard' }
  }

  return true
})

export default router

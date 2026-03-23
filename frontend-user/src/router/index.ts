import { createRouter, createWebHashHistory } from 'vue-router'
import StudentLayout from '@/layouts/StudentLayout.vue'
import { readUserAuthSnapshot } from '@/stores/auth'

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
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guestOnly: true }
    },
    {
      path: '/',
      component: StudentLayout,
      children: [
        { path: '', name: 'home', component: () => import('@/views/HomeView.vue') },
        { path: 'categories', name: 'categories', component: () => import('@/views/CategoryView.vue') },
        { path: 'search', name: 'search', component: () => import('@/views/SearchView.vue') },
        { path: 'goods/detail', name: 'goods-detail', component: () => import('@/views/ProductDetailView.vue') },
        { path: 'publish', name: 'publish', component: () => import('@/views/PublishGoodsView.vue'), meta: { requiresAuth: true } },
        { path: 'my/goods', name: 'my-goods', component: () => import('@/views/MyGoodsView.vue'), meta: { requiresAuth: true } },
        { path: 'my/favorites', name: 'favorites', component: () => import('@/views/FavoritesView.vue'), meta: { requiresAuth: true } },
        { path: 'my/orders', name: 'orders', component: () => import('@/views/OrdersView.vue'), meta: { requiresAuth: true } },
        { path: 'chat', name: 'chat', component: () => import('@/views/ChatView.vue'), meta: { requiresAuth: true } },
        { path: 'my/profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { requiresAuth: true } },
        { path: 'my/campus-auth', name: 'campus-auth', component: () => import('@/views/CampusAuthView.vue'), meta: { requiresAuth: true } }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const auth = readUserAuthSnapshot()

  if (to.meta.requiresAuth && !auth.token) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }

  if (to.meta.guestOnly && auth.token) {
    return { path: '/' }
  }

  return true
})

export default router

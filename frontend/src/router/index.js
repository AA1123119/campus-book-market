import { createRouter, createWebHashHistory } from 'vue-router'
import { auth } from '../store/auth'

const routes = [
  { path: '/', component: () => import('../views/Home.vue') },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/book/:id', component: () => import('../views/BookDetail.vue') },
  { path: '/publish', component: () => import('../views/Publish.vue'), meta: { auth: true } },
  { path: '/orders', component: () => import('../views/Orders.vue'), meta: { auth: true } },
  { path: '/profile', component: () => import('../views/Profile.vue'), meta: { auth: true } },
  { path: '/admin', component: () => import('../views/Admin.vue'), meta: { auth: true } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  if (to.meta.auth && !auth.logged) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router

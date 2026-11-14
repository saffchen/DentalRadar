import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'
import LoginView from './views/LoginView.vue'
import RegisterView from './views/RegisterView.vue'
import MapView from './views/MapView.vue'
import ClinicCabinetView from './views/ClinicCabinetView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: () => destination() },
    { path: '/login', component: LoginView, meta: { guest: true } },
    { path: '/register', component: RegisterView, meta: { guest: true } },
    { path: '/map', component: MapView, meta: { role: 'USER' } },
    { path: '/clinic', component: ClinicCabinetView, meta: { role: 'CLINIC' } }
  ]
})

function destination() {
  const auth = useAuthStore()
  if (!auth.token) {
    return '/login'
  }
  return auth.role === 'CLINIC' ? '/clinic' : '/map'
}

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.guest && auth.token) {
    return auth.role === 'CLINIC' ? '/clinic' : '/map'
  }
  if (!to.meta.role) {
    return true
  }
  if (!auth.token) {
    return '/login'
  }
  if (auth.role !== to.meta.role) {
    return auth.role === 'CLINIC' ? '/clinic' : '/map'
  }
  return true
})

export default router

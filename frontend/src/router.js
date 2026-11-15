import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'
import LoginView from './views/LoginView.vue'
import RegisterView from './views/RegisterView.vue'
import MapView from './views/MapView.vue'
import ClinicCabinetView from './views/ClinicCabinetView.vue'
import NotFoundView from './views/NotFoundView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: () => destination(), meta: { title: 'Карта клиник' } },
    { path: '/login', component: LoginView, meta: { guest: true, title: 'Вход' } },
    { path: '/register', component: RegisterView, meta: { guest: true, title: 'Регистрация' } },
    { path: '/map', component: MapView, meta: { role: 'USER', title: 'Карта клиник' } },
    { path: '/clinic', component: ClinicCabinetView, meta: { role: 'CLINIC', title: 'Кабинет клиники' } },
    { path: '/:pathMatch(.*)*', component: NotFoundView, meta: { title: 'Страница не найдена' } }
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

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} — Мой Стоматолог` : 'Мой Стоматолог'
})

export default router

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import UserShell from '../components/layout/UserShell.vue'
import { api } from '../api'
import { formatDate } from '../format'
import { useAuthStore } from '../stores/auth'
import { usePreferenceStore } from '../stores/preferences'

const router = useRouter()
const auth = useAuthStore()
const preferences = usePreferenceStore()
const profile = ref(null)
const error = ref('')
const notifications = ref(localStorage.getItem('moy-notifications') !== 'off')

onMounted(async () => {
  try {
    profile.value = await api('/api/auth/me')
  } catch (requestError) {
    error.value = requestError.message
  }
})

function toggleNotifications() {
  notifications.value = !notifications.value
  localStorage.setItem('moy-notifications', notifications.value ? 'on' : 'off')
}

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <UserShell>
    <section class="page-narrow profile-page">
      <h1>Профиль</h1>
      <p v-if="error" class="error">{{ error }}</p>
      <div v-if="profile" class="profile-card">
        <span class="doctor-avatar">{{ profile.displayName.slice(0, 1) }}</span>
        <div>
          <strong>{{ profile.displayName }}</strong>
          <p>{{ profile.email }}</p>
        </div>
      </div>
      <dl v-if="profile" class="profile-fields">
        <div><dt>Роль</dt><dd>{{ profile.role === 'CLINIC' ? 'Клиника' : 'Пользователь' }}</dd></div>
        <div><dt>Избранное</dt><dd>{{ preferences.favoriteIds.length }}</dd></div>
      </dl>
      <nav class="profile-links">
        <button type="button" @click="router.push('/appointments')">Мои записи</button>
        <button type="button" @click="router.push('/favorites')">Избранное</button>
        <button type="button" @click="toggleNotifications">Уведомления: {{ notifications ? 'включены' : 'выключены' }}</button>
        <button type="button" @click="router.push('/profile')">Настройки</button>
        <a href="mailto:support@example.com">Помощь и поддержка</a>
      </nav>
      <h2>Мои отзывы</h2>
      <p v-if="profile && !profile.reviews.length" class="note">Отзывов пока нет.</p>
      <article v-for="review in profile?.reviews || []" :key="review.clinicId + review.createdAt" class="profile-review">
        <strong>{{ review.clinicName }} · {{ review.rating }} / 5</strong>
        <p>{{ review.text }}</p>
        <p class="note">{{ formatDate(review.createdAt) }}</p>
      </article>
      <button class="secondary-button profile-logout" type="button" @click="logout">Выйти</button>
    </section>
  </UserShell>
</template>

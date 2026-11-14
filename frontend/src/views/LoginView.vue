<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const email = ref('')
const password = ref('')
const error = ref('')
const pending = ref(false)

async function submit() {
  error.value = ''
  pending.value = true
  try {
    const session = await api('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: email.value, password: password.value })
    })
    auth.setSession(session)
    router.push(session.role === 'CLINIC' ? '/clinic' : '/map')
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <main class="auth-layout">
    <form class="panel auth-card stack" @submit.prevent="submit">
      <div>
        <h1>Вход</h1>
        <p class="note">Откройте карту клиник или кабинет своей клиники.</p>
      </div>
      <label>
        Email
        <input v-model="email" type="email" autocomplete="username" required />
      </label>
      <label>
        Пароль
        <input v-model="password" type="password" autocomplete="current-password" required />
      </label>
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit" :disabled="pending">Войти</button>
      <router-link class="button secondary" to="/register">Создать аккаунт</router-link>
      <p class="note">Демонстрация: user@demo.local или center@demo.local, пароль demo1234.</p>
    </form>
  </main>
</template>

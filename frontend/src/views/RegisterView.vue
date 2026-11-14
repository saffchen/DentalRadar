<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const displayName = ref('')
const email = ref('')
const password = ref('')
const role = ref('USER')
const error = ref('')
const pending = ref(false)

async function submit() {
  error.value = ''
  pending.value = true
  try {
    const session = await api('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        displayName: displayName.value,
        email: email.value,
        password: password.value,
        role: role.value
      })
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
        <h1>Регистрация</h1>
        <p class="note">Выберите роль. Клиника получает свой кабинет сразу.</p>
      </div>
      <label>
        {{ role === 'CLINIC' ? 'Название клиники' : 'Имя' }}
        <input v-model="displayName" required maxlength="120" />
      </label>
      <label>
        Email
        <input v-model="email" type="email" autocomplete="username" required />
      </label>
      <label>
        Пароль
        <input v-model="password" type="password" autocomplete="new-password" minlength="8" required />
      </label>
      <fieldset>
        <legend>Роль</legend>
        <label>
          <input v-model="role" type="radio" value="USER" />
          Пользователь
        </label>
        <label>
          <input v-model="role" type="radio" value="CLINIC" />
          Клиника
        </label>
      </fieldset>
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit" :disabled="pending">Зарегистрироваться</button>
      <router-link class="button secondary" to="/login">У меня уже есть аккаунт</router-link>
    </form>
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const router = useRouter()
const home = computed(() => (auth.role === 'CLINIC' ? '/clinic' : '/map'))

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <router-link class="brand" :to="auth.token ? home : '/login'">DentalRadar</router-link>
      <div v-if="auth.token" class="topbar-actions">
        <span>{{ auth.displayName }}</span>
        <button class="secondary" type="button" @click="logout">Выйти</button>
      </div>
    </header>
    <router-view />
  </div>
</template>

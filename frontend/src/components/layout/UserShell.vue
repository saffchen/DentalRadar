<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, CalendarDays, Heart, Map, Search, UserRound } from 'lucide-vue-next'
import { useAuthStore } from '../../stores/auth'
import { usePreferenceStore } from '../../stores/preferences'
import SearchInput from '../SearchInput.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const preferences = usePreferenceStore()

const items = [
  { id: 'search', label: 'Поиск', to: '/map', icon: Search },
  { id: 'map', label: 'Карта', to: '/map/view', icon: Map },
  { id: 'favorites', label: 'Избранное', to: '/favorites', icon: Heart },
  { id: 'appointments', label: 'Записи', to: '/appointments', icon: CalendarDays },
  { id: 'profile', label: 'Профиль', to: '/profile', icon: UserRound }
]

const current = computed(() => items.find((item) => route.path === item.to)?.id || 'search')

function go(path) {
  preferences.persist()
  router.push(path)
}
</script>

<template>
  <div class="user-shell">
    <header class="app-header">
      <button class="brand" type="button" @click="go('/map')">
        <span class="brand-mark" aria-hidden="true">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M5 2.5c.4 1.2.2 2.4-.4 3.4C3.8 7 3.5 8.2 3.8 9.6 4.2 11.6 5.8 13.5 8 13.5s3.8-1.9 4.2-3.9c.3-1.4 0-2.6-.8-3.7-.6-1-1-2.2-.6-3.4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
          </svg>
        </span>
        Мой Стоматолог
      </button>
      <SearchInput class="header-search" :model-value="preferences.query" @update:model-value="preferences.query = $event; preferences.persist()" />
      <button class="header-link" type="button" @click="go('/favorites')"><Heart :size="18" /> Избранное</button>
      <button class="header-link" type="button" @click="go('/appointments')"><CalendarDays :size="18" /> Записи</button>
      <button class="header-link header-user" type="button" @click="go('/profile')"><UserRound :size="18" /> {{ auth.displayName }}</button>
      <button class="icon-button header-bell" type="button" aria-label="Уведомления" @click="go('/profile')"><Bell :size="18" /></button>
    </header>
    <main class="user-main">
      <slot />
    </main>
    <nav class="bottom-navigation" aria-label="Разделы">
      <button
        v-for="item in items"
        :key="item.id"
        type="button"
        :aria-current="current === item.id ? 'page' : undefined"
        @click="go(item.to)"
      >
        <component :is="item.icon" :size="18" />
        {{ item.label }}
      </button>
    </nav>
  </div>
</template>

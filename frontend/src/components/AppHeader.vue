<script setup>
import SearchInput from './SearchInput.vue'

defineProps({
  query: { type: String, default: '' },
  name: { type: String, default: '' }
})
defineEmits(['update:query', 'favorites', 'appointments'])
</script>

<template>
  <header class="app-header">
    <router-link class="brand" to="/map">
      <span class="brand-mark" aria-hidden="true">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M5 2.5c.4 1.2.2 2.4-.4 3.4C3.8 7 3.5 8.2 3.8 9.6 4.2 11.6 5.8 13.5 8 13.5s3.8-1.9 4.2-3.9c.3-1.4 0-2.6-.8-3.7-.6-1-1-2.2-.6-3.4-.4.7-.6 1.6-.4 2.5.2.8.8 1.4 1.5 1.7" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
        </svg>
      </span>
      Мой Стоматолог
    </router-link>
    <SearchInput class="header-search" :model-value="query" @update:model-value="$emit('update:query', $event)" />
    <button class="header-link" type="button" @click="$emit('favorites')">Избранное</button>
    <button class="header-link" type="button" @click="$emit('appointments')">Записи</button>
    <span class="header-user">{{ name }}</span>
  </header>
</template>

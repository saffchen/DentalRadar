<script setup>
defineProps({
  current: { type: String, required: true }
})
defineEmits(['select'])

const items = [
  { id: 'search', label: 'Поиск' },
  { id: 'map', label: 'Карта' },
  { id: 'favorites', label: 'Избранное' },
  { id: 'appointments', label: 'Записи' },
  { id: 'profile', label: 'Профиль' }
]
</script>

<template>
  <nav class="bottom-navigation" aria-label="Разделы">
    <button
      v-for="item in items"
      :key="item.id"
      type="button"
      :aria-current="current === item.id ? 'page' : undefined"
      @click="$emit('select', item.id)"
    >
      {{ item.label }}
    </button>
  </nav>
</template>

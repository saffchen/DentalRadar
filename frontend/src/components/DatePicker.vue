<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' }
})
defineEmits(['update:modelValue'])

const days = computed(() => {
  const start = new Date()
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    const value = date.toISOString().slice(0, 10)
    const label = new Intl.DateTimeFormat('ru-RU', { day: 'numeric', month: 'short' }).format(date)
    const weekday = new Intl.DateTimeFormat('ru-RU', { weekday: 'short' }).format(date)
    return { value, label, weekday }
  })
})
</script>

<template>
  <div class="date-picker" role="listbox" aria-label="Дата">
    <button
      v-for="day in days"
      :key="day.value"
      type="button"
      :class="{ selected: modelValue === day.value }"
      :aria-selected="modelValue === day.value"
      @click="$emit('update:modelValue', day.value)"
    >
      <span>{{ day.weekday }}</span>
      <strong>{{ day.label }}</strong>
    </button>
  </div>
</template>

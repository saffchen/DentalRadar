<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: Number, default: null },
  count: { type: Number, default: 0 }
})

const reviewWord = computed(() => {
  const count = Number(props.count) || 0
  const mod100 = count % 100
  const mod10 = count % 10
  if (mod100 >= 11 && mod100 <= 14) return 'отзывов'
  if (mod10 === 1) return 'отзыв'
  if (mod10 >= 2 && mod10 <= 4) return 'отзыва'
  return 'отзывов'
})
</script>

<template>
  <span class="rating">
    <span class="rating-stars" aria-hidden="true">★</span>
    <span v-if="value == null">Нет оценок</span>
    <span v-else>{{ Number(value).toFixed(1) }} · {{ count }} {{ reviewWord }}</span>
  </span>
</template>

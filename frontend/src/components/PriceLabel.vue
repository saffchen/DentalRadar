<script setup>
import { computed } from 'vue'
import { formatPrice } from '../format'

const props = defineProps({
  price: { type: Number, required: true },
  from: { type: Boolean, default: false }
})

const text = computed(() => {
  const amount = new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    maximumFractionDigits: props.from ? 0 : 2
  }).format(props.price)
  return props.from ? `от ${amount}` : amount
})
</script>

<template>
  <span class="price-label">{{ from ? text : formatPrice(price) }}</span>
</template>

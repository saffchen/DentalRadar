<script setup>
import { computed } from 'vue'
import Rating from './Rating.vue'
import PriceLabel from './PriceLabel.vue'

const props = defineProps({
  clinic: { type: Object, required: true },
  favorite: { type: Boolean, default: false },
  active: { type: Boolean, default: false }
})
defineEmits(['open', 'favorite'])

const lowest = computed(() => {
  const prices = props.clinic.services.map((item) => Number(item.price)).filter((price) => price > 0)
  return prices.length ? Math.min(...prices) : null
})
</script>

<template>
  <article class="clinic-card" :class="{ active }">
    <button class="clinic-open" type="button" @click="$emit('open')">
      <span class="clinic-thumb" aria-hidden="true">
        <svg viewBox="0 0 48 48" width="36" height="36">
          <path d="M16 8c1.4 4 .6 8-1.2 11.2C12.4 23 11.6 27 12.6 32 14 38 18.4 42 24 42s10-4 11.4-10c1-5 .2-9-2.2-12.8C31.4 16 30.6 12 32 8c-1.4 2.4-2 5.2-1.2 8 .8 2.6 2.8 4.4 5.2 5.2" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
        </svg>
      </span>
      <span class="clinic-copy">
        <span class="clinic-name">{{ clinic.name }}</span>
        <span class="clinic-address">{{ clinic.address }}</span>
        <Rating :value="clinic.averageRating" :count="clinic.reviewCount" />
        <span v-if="lowest != null" class="clinic-from"><PriceLabel :price="lowest" from /></span>
        <span class="clinic-services">
          <span v-for="item in clinic.services.slice(0, 2)" :key="item.id">
            {{ item.name }} — <PriceLabel :price="Number(item.price)" />
          </span>
        </span>
      </span>
    </button>
    <button
      class="favorite"
      type="button"
      :aria-pressed="favorite"
      :aria-label="favorite ? 'Убрать из избранного' : 'Добавить в избранное'"
      @click="$emit('favorite')"
    >
      {{ favorite ? '♥' : '♡' }}
    </button>
  </article>
</template>

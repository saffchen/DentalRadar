<script setup>
defineProps({
  service: { type: String, default: '' },
  minRating: { type: String, default: '' },
  services: { type: Array, default: () => [] },
  count: { type: Number, default: 0 }
})
defineEmits(['update:service', 'update:minRating', 'apply', 'reset'])
</script>

<template>
  <form class="filter-panel" @submit.prevent="$emit('apply')">
    <div class="filter-panel-head">
      <h2>Фильтры</h2>
      <button class="text-button" type="button" @click="$emit('reset')">Сбросить</button>
    </div>
    <label>
      Услуга
      <select :value="service" @change="$emit('update:service', $event.target.value)">
        <option value="">Все услуги</option>
        <option v-for="name in services" :key="name" :value="name">{{ name }}</option>
      </select>
    </label>
    <fieldset>
      <legend>Оценка</legend>
      <label v-for="value in ['', '4', '3']" :key="value || 'any'">
        <input
          type="radio"
          name="rating"
          :value="value"
          :checked="minRating === value"
          @change="$emit('update:minRating', value)"
        />
        {{ value ? `${value} и выше` : 'Любая' }}
      </label>
    </fieldset>
    <button class="primary-button" type="submit">Показать {{ count }}</button>
  </form>
</template>

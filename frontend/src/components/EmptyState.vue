<script setup>
defineProps({
  title: { type: String, required: true },
  text: { type: String, required: true },
  action: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<template>
  <div class="empty-state">
    <h2>{{ title }}</h2>
    <p>{{ text }}</p>
    <button v-if="action" class="primary-button" type="button" @click="$emit('action')">{{ action }}</button>
  </div>
</template>

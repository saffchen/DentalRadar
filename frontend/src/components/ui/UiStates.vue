<script setup>
defineProps({
  kind: { type: String, required: true },
  title: { type: String, required: true },
  text: { type: String, default: '' },
  action: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<template>
  <div class="state-block" :class="kind" role="status">
    <h2>{{ title }}</h2>
    <p v-if="text">{{ text }}</p>
    <button v-if="action" class="primary-button" type="button" @click="$emit('action')">{{ action }}</button>
  </div>
</template>

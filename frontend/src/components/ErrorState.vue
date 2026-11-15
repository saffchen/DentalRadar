<script setup>
defineProps({
  text: { type: String, required: true }
})
defineEmits(['retry'])
</script>

<template>
  <div class="error-state" role="alert">
    <h2>Не удалось загрузить данные</h2>
    <p>{{ text }}</p>
    <button class="primary-button" type="button" @click="$emit('retry')">Повторить</button>
  </div>
</template>

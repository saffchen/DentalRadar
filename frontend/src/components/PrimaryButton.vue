<script setup>
defineProps({
  type: { type: String, default: 'button' }
})
</script>

<template>
  <button class="primary-button" :type="type">
    <slot />
  </button>
</template>

<script setup>
defineProps({
  type: { type: String, default: 'button' }
})
</script>

<template>
  <button class="secondary-button" :type="type">
    <slot />
  </button>
</template>

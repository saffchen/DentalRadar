<script setup>
defineProps({
  modelValue: { type: String, default: '' }
})
defineEmits(['update:modelValue'])
</script>

<template>
  <label class="search-input">
    <span class="sr-only">Поиск клиник, услуг, адреса</span>
    <input
      :value="modelValue"
      type="search"
      placeholder="Поиск клиник, услуг, адреса"
      @input="$emit('update:modelValue', $event.target.value)"
    />
  </label>
</template>

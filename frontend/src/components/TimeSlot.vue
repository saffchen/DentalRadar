<script setup>
defineProps({
  slots: { type: Array, default: () => [] },
  modelValue: { type: String, default: '' }
})
defineEmits(['update:modelValue'])
</script>

<template>
  <div class="time-slots" role="listbox" aria-label="Время">
    <button
      v-for="slot in slots"
      :key="slot.value || slot"
      type="button"
      :disabled="slot.disabled"
      :class="{ selected: modelValue === (slot.value || slot) }"
      @click="$emit('update:modelValue', slot.value || slot)"
    >
      {{ slot.value || slot }}
    </button>
  </div>
</template>

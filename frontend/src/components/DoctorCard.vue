<script setup>
defineProps({
  doctor: { type: Object, required: true },
  selected: { type: Boolean, default: false }
})
defineEmits(['select'])
</script>

<template>
  <button class="doctor-card" type="button" :aria-pressed="selected" @click="$emit('select')">
    <span class="doctor-avatar" aria-hidden="true">{{ doctor.name.slice(0, 1) }}</span>
    <span>
      <strong>{{ doctor.name }}</strong>
      <span class="note">{{ doctor.specialty }} · стаж {{ doctor.experience }} · {{ doctor.rating }}</span>
    </span>
  </button>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UserShell from '../components/layout/UserShell.vue'
import { doctorById } from '../data/demo'
import { useClinicStore } from '../stores/clinics'

const route = useRoute()
const router = useRouter()
const clinicStore = useClinicStore()
const clinicName = ref('')
const doctor = computed(() => doctorById(route.params.doctorId))

onMounted(async () => {
  await clinicStore.load()
  const clinic = clinicStore.items.find((item) => item.id === route.params.clinicId)
  clinicName.value = clinic?.name || 'Клиника'
})
</script>

<template>
  <UserShell>
    <article v-if="doctor" class="page-narrow">
      <button class="text-button" type="button" @click="router.back()">Назад</button>
      <div class="doctor-hero" aria-hidden="true">{{ doctor.name.slice(0, 1) }}</div>
      <h1>{{ doctor.name }}</h1>
      <p>{{ doctor.specialty }} · стаж {{ doctor.experience }}</p>
      <p>Рейтинг {{ doctor.rating }}</p>
      <p class="note">Демонстрационная карточка. Эти сведения не пришли из кабинета клиники.</p>
      <p>{{ doctor.about }}</p>
      <p>{{ clinicName }}</p>
      <button class="primary-button" type="button" @click="router.push({ path: '/booking', query: { clinicId: route.params.clinicId, doctorId: doctor.id } })">Записать к этому врачу</button>
    </article>
    <article v-else class="page-narrow">
      <h1>Врач не найден</h1>
      <button class="secondary-button" type="button" @click="router.push('/map')">К поиску</button>
    </article>
  </UserShell>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import UserShell from '../components/layout/UserShell.vue'
import ClinicCard from '../components/ClinicCard.vue'
import UiStates from '../components/ui/UiStates.vue'
import { useClinicStore } from '../stores/clinics'
import { usePreferenceStore } from '../stores/preferences'

const router = useRouter()
const clinicStore = useClinicStore()
const preferences = usePreferenceStore()
onMounted(() => clinicStore.load())

const favorites = () => clinicStore.items.filter((clinic) => preferences.favoriteIds.includes(clinic.id))
</script>

<template>
  <UserShell>
    <section class="page-narrow">
      <h1>Избранное</h1>
      <div v-if="clinicStore.status === 'loading'" class="skeleton-list"><span class="skeleton-card"></span></div>
      <UiStates v-else-if="clinicStore.status === 'error'" kind="error" title="Не удалось загрузить данные" :text="clinicStore.error" action="Повторить" @action="clinicStore.load(true)" />
      <UiStates v-else-if="!favorites().length" kind="empty" title="В избранном пока ничего нет" text="Отметьте клинику сердцем в результатах поиска." action="Найти клиники" @action="router.push('/map')" />
      <div v-else class="clinic-items">
        <ClinicCard
          v-for="clinic in favorites()"
          :key="clinic.id"
          :clinic="clinic"
          favorite
          @open="router.push(`/clinics/${clinic.id}`)"
          @favorite="preferences.toggleFavorite(clinic.id)"
        />
      </div>
    </section>
  </UserShell>
</template>

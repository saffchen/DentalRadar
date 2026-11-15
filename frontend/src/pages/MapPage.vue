<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { LocateFixed } from 'lucide-vue-next'
import UserShell from '../components/layout/UserShell.vue'
import { useClinicStore } from '../stores/clinics'
import { usePreferenceStore } from '../stores/preferences'
import { useClinicSearch } from '../composables/useClinicSearch'
import { mountMap } from '../composables/useMap'
import { lowestPrice } from '../composables/useClinicSearch'

const router = useRouter()
const clinicStore = useClinicStore()
const preferences = usePreferenceStore()
const { results } = useClinicSearch()
const mapRef = ref(null)
const selected = ref(null)
const geoNote = ref('')
let mapApi

onMounted(async () => {
  await clinicStore.load()
  mapApi = mountMap(mapRef.value, {
    center: [preferences.city.latitude, preferences.city.longitude],
    clinics: results.value,
    selectedId: selected.value?.id || '',
    onSelect: (clinic) => { selected.value = clinic }
  })
  const found = await mapApi.locate()
  if (!found) geoNote.value = `Геолокация недоступна. Показан город ${preferences.city.name}.`
})

onBeforeUnmount(() => mapApi?.destroy())
watch(results, (list) => mapApi?.draw(list, selected.value?.id || ''))

async function locate() {
  const found = await mapApi.locate()
  geoNote.value = found ? '' : `Геолокация недоступна. Показан город ${preferences.city.name}.`
}
</script>

<template>
  <UserShell>
    <section class="map-page">
      <p v-if="geoNote" class="map-note">{{ geoNote }}</p>
      <button class="icon-button locate-button" type="button" aria-label="Моё местоположение" @click="locate"><LocateFixed :size="18" /></button>
      <div ref="mapRef" class="map-canvas" role="region" aria-label="Карта клиник"></div>
      <article v-if="selected" class="map-selection">
        <button class="clinic-open" type="button" @click="router.push(`/clinics/${selected.id}`)">
          <strong>{{ selected.name }}</strong>
          <span>{{ selected.address }}</span>
          <span v-if="lowestPrice(selected) != null">от {{ Math.round(lowestPrice(selected)) }} ₽</span>
        </button>
        <button class="favorite" type="button" @click="preferences.toggleFavorite(selected.id)">{{ preferences.favoriteIds.includes(selected.id) ? '♥' : '♡' }}</button>
      </article>
    </section>
  </UserShell>
</template>

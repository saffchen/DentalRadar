<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { LocateFixed, SlidersHorizontal } from 'lucide-vue-next'
import UserShell from '../components/layout/UserShell.vue'
import ClinicCard from '../components/ClinicCard.vue'
import UiStates from '../components/ui/UiStates.vue'
import { useClinicStore } from '../stores/clinics'
import { usePreferenceStore } from '../stores/preferences'
import { useClinicSearch } from '../composables/useClinicSearch'
import { mountMap } from '../composables/useMap'
import { CITIES } from '../data/cities'

const router = useRouter()
const clinicStore = useClinicStore()
const preferences = usePreferenceStore()
const { results } = useClinicSearch()
const listRef = ref(null)
const mapRef = ref(null)
const sheet = ref(false)
const geoNote = ref('')
let mapApi

onMounted(async () => {
  await clinicStore.load()
  mapApi = mountMap(mapRef.value, {
    center: [preferences.city.latitude, preferences.city.longitude],
    clinics: results.value,
    selectedId: '',
    onSelect: openClinic
  })
  const found = await mapApi.locate()
  geoNote.value = found ? '' : `Геолокация недоступна. Показан город ${preferences.city.name}.`
  if (listRef.value) listRef.value.scrollTop = preferences.scroll
})

onBeforeUnmount(() => {
  preferences.scroll = listRef.value?.scrollTop || 0
  preferences.persist()
  mapApi?.destroy()
})

watch(results, (list) => mapApi?.draw(list, ''))

function changeCity(id) {
  preferences.cityId = id
  preferences.persist()
  const city = CITIES.find((item) => item.id === id)
  mapApi?.map.setView([city.latitude, city.longitude], 12)
  geoNote.value = `Показан город ${city.name}.`
}

function openClinic(clinic) {
  preferences.scroll = listRef.value?.scrollTop || 0
  preferences.persist()
  router.push(`/clinics/${clinic.id}`)
}

function rememberScroll() {
  preferences.scroll = listRef.value?.scrollTop || 0
  preferences.persist()
}
</script>

<template>
  <UserShell>
    <div class="search-page">
      <section ref="listRef" class="result-panel" @scroll.passive="rememberScroll">
        <div class="filter-row">
          <label>
            Услуга
            <select :value="preferences.services[0] || ''" @change="preferences.services = $event.target.value ? [$event.target.value] : []; preferences.persist()">
              <option value="">Все услуги</option>
              <option v-for="name in clinicStore.items.flatMap((item) => item.services.map((service) => service.name)).filter((name, index, list) => list.indexOf(name) === index)" :key="name" :value="name">{{ name }}</option>
            </select>
          </label>
          <label>
            Оценка
            <select v-model="preferences.minRating" @change="preferences.persist()">
              <option value="">Любая</option>
              <option value="4">4 и выше</option>
              <option value="3">3 и выше</option>
            </select>
          </label>
          <label class="city-select">
            Город
            <select :value="preferences.cityId" @change="changeCity($event.target.value)">
              <option v-for="city in CITIES" :key="city.id" :value="city.id">{{ city.name }}</option>
            </select>
          </label>
          <button class="text-button filter-open" type="button" @click="sheet = true"><SlidersHorizontal :size="16" /> Фильтры</button>
        </div>
        <p class="list-count">{{ clinicStore.status === 'loading' ? 'Загрузка клиник…' : `Найдено клиник: ${results.length}` }}</p>
        <p v-if="geoNote" class="note">{{ geoNote }}</p>
        <div v-if="clinicStore.status === 'loading'" class="skeleton-list" aria-hidden="true">
          <span v-for="item in 3" :key="item" class="skeleton-card"></span>
        </div>
        <UiStates v-else-if="clinicStore.status === 'error'" kind="error" title="Не удалось загрузить данные" :text="clinicStore.error" action="Повторить" @action="clinicStore.load(true)" />
        <UiStates v-else-if="!results.length" kind="empty" title="Клиники не найдены" text="Измените запрос или фильтры." action="Сбросить фильтры" @action="preferences.resetFilters()" />
        <div v-else class="clinic-items">
          <ClinicCard
            v-for="clinic in results"
            :key="clinic.id"
            :clinic="clinic"
            :favorite="preferences.favoriteIds.includes(clinic.id)"
            @open="openClinic(clinic)"
            @favorite="preferences.toggleFavorite(clinic.id)"
          />
        </div>
      </section>
      <section class="map-stage">
        <button class="icon-button locate-button" type="button" aria-label="Моё местоположение" @click="mapApi?.locate()"><LocateFixed :size="18" /></button>
        <div ref="mapRef" class="map-canvas" role="region" aria-label="Карта клиник"></div>
      </section>
      <button class="map-fab" type="button" @click="router.push('/map/view')"><LocateFixed :size="16" /> Карта</button>
    </div>
    <div v-if="sheet" class="sheet-backdrop" @click.self="sheet = false">
      <form class="bottom-sheet" @submit.prevent="sheet = false">
        <div class="sheet-head">
          <h2>Фильтры</h2>
          <button class="text-button" type="button" @click="preferences.resetFilters()">Сбросить</button>
        </div>
        <fieldset>
          <legend>Услуги</legend>
          <label v-for="name in [...new Set(clinicStore.items.flatMap((item) => item.services.map((service) => service.name)))]" :key="name">
            <input type="checkbox" :checked="preferences.services.includes(name)" @change="preferences.services = preferences.services.includes(name) ? preferences.services.filter((item) => item !== name) : [...preferences.services, name]; preferences.persist()" />
            {{ name }}
          </label>
        </fieldset>
        <fieldset>
          <legend>Рейтинг</legend>
          <label v-for="value in ['', '5', '4', '3']" :key="value || 'any'">
            <input type="radio" name="rating" :value="value" :checked="preferences.minRating === value" @change="preferences.minRating = value; preferences.persist()" />
            {{ value ? `${value} и выше` : 'Любая' }}
          </label>
        </fieldset>
        <label>Минимальная стоимость <input v-model.number="preferences.minPrice" type="range" min="0" max="50000" step="500" @change="preferences.persist()" /></label>
        <label>Максимальная стоимость <input v-model.number="preferences.maxPrice" type="range" min="0" max="100000" step="500" @change="preferences.persist()" /></label>
        <p class="note">от {{ preferences.minPrice }} ₽ до {{ preferences.maxPrice }} ₽</p>
        <button class="primary-button" type="submit">Показать {{ results.length }}</button>
      </form>
    </div>
  </UserShell>
</template>

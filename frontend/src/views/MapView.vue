<script setup>
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { setWorkerUrl } from 'maplibre-gl'
import workerUrl from 'maplibre-gl/dist/maplibre-gl-worker.mjs?worker&url'
import 'maplibre-gl/dist/maplibre-gl.css'
import { maplibreGL } from '@maplibre/maplibre-gl-leaflet'
import iconUrl from 'leaflet/dist/images/marker-icon.png'
import iconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png'
import shadowUrl from 'leaflet/dist/images/marker-shadow.png'
import { api } from '../api'
import { formatPrice, formatRating } from '../format'

setWorkerUrl(workerUrl)

const MOSCOW = [55.751244, 37.618423]
const mapElement = ref(null)
const service = ref('')
const minRating = ref('')
const serviceOptions = ref([])
const clinics = ref([])
const foundLabel = ref('Клиники не найдены')
const locationNote = ref('')
const error = ref('')
const loading = ref(true)
const activeClinicId = ref('')
const pane = ref('list')

const markerIcon = L.icon({
  iconUrl,
  iconRetinaUrl,
  shadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
})

let map
let markers
const markerById = new Map()

onMounted(async () => {
  map = L.map(mapElement.value)
  map.attributionControl.setPrefix(false)
  maplibreGL({ style: 'https://tiles.openfreemap.org/styles/liberty' }).addTo(map)
  markers = L.layerGroup().addTo(map)
  locate()
  try {
    await loadServiceOptions()
    await loadClinics()
  } catch (requestError) {
    error.value = requestError.message
  }
})

onUnmounted(() => {
  map?.remove()
})

watch([service, minRating], () => {
  loadClinics().catch((requestError) => {
    error.value = requestError.message
  })
})

watch(pane, async () => {
  await nextTick()
  map?.invalidateSize()
})

function locate() {
  if (!navigator.geolocation) {
    map.setView(MOSCOW, 11)
    locationNote.value = 'Местоположение недоступно. Карта открыта на Москве.'
    return
  }
  map.setView(MOSCOW, 11)
  navigator.geolocation.getCurrentPosition(
    (position) => {
      map.setView([position.coords.latitude, position.coords.longitude], 13)
      locationNote.value = ''
    },
    () => {
      map.setView(MOSCOW, 11)
      locationNote.value = 'Местоположение недоступно. Карта открыта на Москве.'
    },
    { enableHighAccuracy: true, timeout: 8000 }
  )
}

async function loadServiceOptions() {
  const all = await api('/api/clinics')
  const names = new Set()
  all.forEach((clinic) => clinic.services.forEach((item) => names.add(item.name)))
  serviceOptions.value = [...names].sort((left, right) => left.localeCompare(right, 'ru'))
}

async function loadClinics() {
  error.value = ''
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (service.value) {
      params.set('service', service.value)
    }
    if (minRating.value) {
      params.set('minRating', minRating.value)
    }
    const query = params.toString()
    clinics.value = await api(`/api/clinics${query ? `?${query}` : ''}`)
    foundLabel.value = `Найдено клиник: ${clinics.value.length}`
    drawMarkers()
    fitResults()
  } finally {
    loading.value = false
  }
}

function fitResults() {
  const hasFilter = Boolean(service.value || minRating.value)
  if (!clinics.value.length || !hasFilter) {
    return
  }
  const bounds = L.latLngBounds(clinics.value.map((clinic) => [clinic.latitude, clinic.longitude]))
  map.fitBounds(bounds, { padding: [48, 48], maxZoom: 14 })
}

async function focusClinic(clinic) {
  activeClinicId.value = clinic.id
  if (window.matchMedia('(max-width: 767px)').matches) {
    pane.value = 'map'
    await nextTick()
    map?.invalidateSize()
  }
  map.setView([clinic.latitude, clinic.longitude], 14)
  const marker = markerById.get(clinic.id)
  if (marker) {
    openPopup(marker, clinic)
  }
}

function visibleServices(clinic) {
  if (!service.value) {
    return orderedServices(clinic.services)
  }
  const matches = clinic.services.filter((item) => item.name === service.value)
  return matches.length ? matches : orderedServices(clinic.services)
}

function resetFilters() {
  service.value = ''
  minRating.value = ''
}

function drawMarkers() {
  markers.clearLayers()
  markerById.clear()
  clinics.value.forEach((clinic) => {
    const marker = L.marker([clinic.latitude, clinic.longitude], { icon: markerIcon })
    marker.bindPopup(buildPopup(clinic))
    marker.on('click', () => {
      activeClinicId.value = clinic.id
      openPopup(marker, clinic)
    })
    markers.addLayer(marker)
    markerById.set(clinic.id, marker)
  })
}

async function openPopup(marker, clinic) {
  activeClinicId.value = clinic.id
  try {
    const detail = await api(`/api/clinics/${clinic.id}`)
    marker.setPopupContent(buildPopup(clinic, detail, marker))
    marker.openPopup()
  } catch (requestError) {
    error.value = requestError.message
  }
}

function buildPopup(clinic, detail, marker) {
  const root = document.createElement('div')
  const title = document.createElement('h3')
  title.className = 'popup-title'
  title.textContent = clinic.name
  const address = document.createElement('p')
  address.textContent = clinic.address
  const rating = document.createElement('p')
  rating.textContent = formatRating(detail?.averageRating ?? clinic.averageRating, detail?.reviewCount ?? clinic.reviewCount)
  const list = document.createElement('ul')
  list.className = 'popup-list'
  orderedServices(clinic.services).forEach((item) => {
    const line = document.createElement('li')
    line.textContent = `${item.name} — ${formatPrice(item.price)}`
    list.append(line)
  })
  root.append(title, address, rating, list)
  if (detail) {
    root.append(reviewForm(clinic.id, detail.reviews.find((review) => review.mine), marker, clinic))
  }
  return root
}

function reviewForm(clinicId, mine, marker, clinic) {
  const form = document.createElement('form')
  form.className = 'popup-form'
  const rating = document.createElement('select')
  rating.setAttribute('aria-label', 'Оценка клиники')
  for (let value = 5; value >= 1; value -= 1) {
    const option = document.createElement('option')
    option.value = String(value)
    option.textContent = `${value} / 5`
    rating.append(option)
  }
  const text = document.createElement('textarea')
  text.setAttribute('aria-label', 'Текст отзыва')
  text.required = true
  text.maxLength = 2000
  text.rows = 3
  text.placeholder = 'Текст отзыва'
  if (mine) {
    rating.value = String(mine.rating)
    text.value = mine.text
  }
  const button = document.createElement('button')
  button.type = 'submit'
  button.textContent = mine ? 'Обновить отзыв' : 'Оставить отзыв'
  const message = document.createElement('p')
  message.className = 'note'
  form.append(rating, text, button, message)
  form.addEventListener('mousedown', (event) => event.stopPropagation())
  form.addEventListener('click', (event) => event.stopPropagation())
  form.addEventListener('submit', async (event) => {
    event.preventDefault()
    button.disabled = true
    try {
      await api(`/api/clinics/${clinicId}/reviews`, {
        method: 'PUT',
        body: JSON.stringify({ rating: Number(rating.value), text: text.value })
      })
      message.textContent = 'Отзыв сохранён'
      await loadClinics()
      const fresh = clinics.value.find((item) => item.id === clinicId) || clinic
      const detail = await api(`/api/clinics/${clinicId}`)
      const current = markerById.get(clinicId) || marker
      current.setPopupContent(buildPopup(fresh, detail, current))
      current.openPopup()
    } catch (requestError) {
      message.textContent = requestError.message
      button.disabled = false
    }
  })
  return form
}

function orderedServices(services) {
  return [...services].sort((left, right) => {
    if (left.name === service.value) {
      return -1
    }
    if (right.name === service.value) {
      return 1
    }
    return left.name.localeCompare(right.name, 'ru')
  })
}
</script>

<template>
  <main class="map-page">
    <div class="filters">
      <label>
        Услуга
        <select v-model="service">
          <option value="">Все услуги</option>
          <option v-for="name in serviceOptions" :key="name" :value="name">{{ name }}</option>
        </select>
      </label>
      <label>
        Оценка
        <select v-model="minRating">
          <option value="">Любая</option>
          <option value="5">5</option>
          <option value="4">4 и выше</option>
          <option value="3">3 и выше</option>
          <option value="2">2 и выше</option>
          <option value="1">1 и выше</option>
        </select>
      </label>
      <button v-if="service || minRating" class="secondary reset-filters" type="button" @click="resetFilters">
        Сбросить
      </button>
    </div>
    <div class="map-layout" :class="`pane-${pane}`">
      <aside class="clinic-list" aria-label="Список клиник">
        <p class="list-count" role="status">{{ loading ? 'Загрузка клиник…' : foundLabel }}</p>
        <p v-if="error" class="error" role="alert">{{ error }}</p>
        <p v-if="locationNote" class="note">{{ locationNote }}</p>
        <p v-if="!loading && !error && !clinics.length" class="note empty">
          Клиник не найдено. Измените услугу или оценку.
        </p>
        <ul class="clinic-items">
          <li v-for="clinic in clinics" :key="clinic.id">
            <button
              type="button"
              class="clinic-card"
              :class="{ active: clinic.id === activeClinicId }"
              :aria-pressed="clinic.id === activeClinicId"
              @click="focusClinic(clinic)"
            >
              <span class="clinic-name">{{ clinic.name }}</span>
              <span class="clinic-address">{{ clinic.address }}</span>
              <span class="clinic-rating">{{ formatRating(clinic.averageRating, clinic.reviewCount) }}</span>
              <span class="clinic-services">
                <span
                  v-for="item in visibleServices(clinic)"
                  :key="item.id"
                  class="clinic-service"
                  :class="{ matched: item.name === service }"
                >
                  {{ item.name }} — {{ formatPrice(item.price) }}
                </span>
              </span>
            </button>
          </li>
        </ul>
      </aside>
      <div ref="mapElement" class="map-canvas" role="region" aria-label="Карта клиник"></div>
    </div>
    <button v-if="pane === 'list'" class="map-switch" type="button" @click="pane = 'map'">Карта</button>
    <button v-else class="map-switch" type="button" @click="pane = 'list'">Список</button>
  </main>
</template>

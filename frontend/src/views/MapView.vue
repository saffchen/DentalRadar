<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
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
const foundLabel = ref('')
const locationNote = ref('')
const error = ref('')

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
}

function drawMarkers() {
  markers.clearLayers()
  markerById.clear()
  clinics.value.forEach((clinic) => {
    const marker = L.marker([clinic.latitude, clinic.longitude], { icon: markerIcon })
    marker.bindPopup(buildPopup(clinic))
    marker.on('click', () => openPopup(marker, clinic))
    markers.addLayer(marker)
    markerById.set(clinic.id, marker)
  })
}

async function openPopup(marker, clinic) {
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
  for (let value = 5; value >= 1; value -= 1) {
    const option = document.createElement('option')
    option.value = String(value)
    option.textContent = `${value} / 5`
    rating.append(option)
  }
  const text = document.createElement('textarea')
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
  <section class="map-page">
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
      <p class="note">{{ foundLabel }}</p>
    </div>
    <p v-if="locationNote" class="note" style="margin: 0.5rem 1rem">{{ locationNote }}</p>
    <p v-if="error" class="error" style="margin: 0.5rem 1rem">{{ error }}</p>
    <div ref="mapElement" class="map-canvas"></div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api'
import { formatDate, formatPrice, formatRating } from '../format'

const profile = ref(null)
const form = reactive({ name: '', description: '', address: '' })
const serviceForm = reactive({ id: '', name: '', price: '' })
const warning = ref('')
const loadError = ref('')
const profileError = ref('')
const profileStatus = ref('')
const serviceError = ref('')
const serviceStatus = ref('')
const loading = ref(true)
const savingProfile = ref(false)
const savingService = ref(false)

onMounted(async () => {
  try {
    await load()
  } catch (requestError) {
    loadError.value = requestError.message
  } finally {
    loading.value = false
  }
})

async function load() {
  profile.value = await api('/api/clinic/me')
  form.name = profile.value.name || ''
  form.description = profile.value.description || ''
  form.address = profile.value.address || ''
}

async function saveProfile() {
  profileError.value = ''
  profileStatus.value = ''
  warning.value = ''
  savingProfile.value = true
  try {
    const saved = await api('/api/clinic/me', {
      method: 'PUT',
      body: JSON.stringify({
        name: form.name,
        description: form.description,
        address: form.address
      })
    })
    profile.value = saved
    warning.value = saved.warning || ''
    profileStatus.value = 'Профиль сохранён'
  } catch (requestError) {
    profileError.value = requestError.message
  } finally {
    savingProfile.value = false
  }
}

function editService(service) {
  serviceForm.id = service.id
  serviceForm.name = service.name
  serviceForm.price = service.price
}

function resetService() {
  serviceForm.id = ''
  serviceForm.name = ''
  serviceForm.price = ''
}

async function saveService() {
  serviceError.value = ''
  serviceStatus.value = ''
  savingService.value = true
  const payload = JSON.stringify({
    name: serviceForm.name,
    price: Number(serviceForm.price)
  })
  try {
    if (serviceForm.id) {
      await api(`/api/clinic/me/services/${serviceForm.id}`, { method: 'PUT', body: payload })
    } else {
      await api('/api/clinic/me/services', { method: 'POST', body: payload })
    }
    resetService()
    serviceStatus.value = 'Услуга сохранена'
    await load()
  } catch (requestError) {
    serviceError.value = requestError.message
  } finally {
    savingService.value = false
  }
}

async function removeService(service) {
  if (!window.confirm(`Удалить услугу «${service.name}»?`)) {
    return
  }
  serviceError.value = ''
  serviceStatus.value = ''
  try {
    await api(`/api/clinic/me/services/${service.id}`, { method: 'DELETE' })
    if (serviceForm.id === service.id) {
      resetService()
    }
    serviceStatus.value = 'Услуга удалена'
    await load()
  } catch (requestError) {
    serviceError.value = requestError.message
  }
}
</script>

<template>
  <main class="page stack">
    <p v-if="loading" class="note">Загрузка кабинета…</p>
    <p v-else-if="loadError" class="error" role="alert">Кабинет не загружен: {{ loadError }}</p>
    <template v-else>
      <section class="panel stack">
        <div>
          <h1>Кабинет клиники</h1>
          <p v-if="profile" class="note">{{ formatRating(profile.averageRating, profile.reviewCount) }}</p>
        </div>
        <form class="stack" @submit.prevent="saveProfile">
          <label>
            Название
            <input v-model="form.name" required maxlength="120" />
          </label>
          <label>
            Описание
            <textarea v-model="form.description" rows="3" maxlength="2000" />
          </label>
          <label>
            Адрес
            <input v-model="form.address" maxlength="300" />
          </label>
          <p class="note">Сервер определит координаты по адресу через OpenStreetMap.</p>
          <p v-if="profile && profile.latitude != null" class="note">
            На карте: {{ profile.latitude }}, {{ profile.longitude }}
          </p>
          <p v-else-if="profile" class="note">Клиника не показана на карте.</p>
          <p v-if="warning" class="warning">{{ warning }}</p>
          <p v-if="profileError" class="error" role="alert">{{ profileError }}</p>
          <p v-if="profileStatus" class="status" role="status">{{ profileStatus }}</p>
          <button type="submit" :disabled="savingProfile">
            {{ savingProfile ? 'Сохранение…' : 'Сохранить профиль' }}
          </button>
        </form>
      </section>

      <section class="panel stack">
        <h2>Услуги и цены</h2>
        <form class="stack" @submit.prevent="saveService">
          <label>
            Услуга
            <input v-model="serviceForm.name" required maxlength="120" />
          </label>
          <label>
            Цена, ₽
            <input v-model="serviceForm.price" type="number" min="0.01" step="0.01" required />
          </label>
          <div class="row">
            <button type="submit" :disabled="savingService">
              {{ serviceForm.id ? 'Сохранить услугу' : 'Добавить услугу' }}
            </button>
            <button v-if="serviceForm.id" class="secondary" type="button" @click="resetService">Отмена</button>
          </div>
        </form>
        <p v-if="serviceError" class="error" role="alert">{{ serviceError }}</p>
        <p v-if="serviceStatus" class="status" role="status">{{ serviceStatus }}</p>
        <div v-if="profile && profile.services.length" class="table-wrap">
          <table>
            <thead>
              <tr>
                <th scope="col">Услуга</th>
                <th scope="col">Цена</th>
                <th scope="col"><span class="sr-only">Действия</span></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="service in profile.services" :key="service.id">
                <td>{{ service.name }}</td>
                <td>{{ formatPrice(service.price) }}</td>
                <td>
                  <div class="service-actions">
                    <button class="secondary" type="button" @click="editService(service)">Изменить</button>
                    <button class="danger" type="button" @click="removeService(service)">Удалить</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="note">Услуг пока нет.</p>
      </section>

      <section class="panel">
        <h2>Отзывы</h2>
        <p v-if="profile && !profile.reviews.length" class="note">Отзывов пока нет.</p>
        <article v-for="review in profile?.reviews || []" :key="review.id" class="review">
          <strong>{{ review.authorName }} · {{ review.rating }} / 5</strong>
          <p>{{ review.text }}</p>
          <p class="note">{{ formatDate(review.createdAt) }}</p>
        </article>
      </section>
    </template>
  </main>
</template>

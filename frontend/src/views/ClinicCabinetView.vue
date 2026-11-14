<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api'
import { formatDate, formatPrice, formatRating } from '../format'

const profile = ref(null)
const form = reactive({ name: '', description: '', address: '' })
const serviceForm = reactive({ id: '', name: '', price: '' })
const warning = ref('')
const error = ref('')
const pending = ref(false)

onMounted(async () => {
  try {
    await load()
  } catch (requestError) {
    error.value = requestError.message
  }
})

async function load() {
  error.value = ''
  profile.value = await api('/api/clinic/me')
  form.name = profile.value.name || ''
  form.description = profile.value.description || ''
  form.address = profile.value.address || ''
}

async function saveProfile() {
  error.value = ''
  warning.value = ''
  pending.value = true
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
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    pending.value = false
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
  error.value = ''
  pending.value = true
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
    await load()
  } catch (requestError) {
    error.value = requestError.message
  } finally {
    pending.value = false
  }
}

async function removeService(service) {
  if (!window.confirm(`Удалить услугу «${service.name}»?`)) {
    return
  }
  error.value = ''
  try {
    await api(`/api/clinic/me/services/${service.id}`, { method: 'DELETE' })
    if (serviceForm.id === service.id) {
      resetService()
    }
    await load()
  } catch (requestError) {
    error.value = requestError.message
  }
}
</script>

<template>
  <main class="page stack">
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
        <button type="submit" :disabled="pending">Сохранить профиль</button>
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
          <button type="submit" :disabled="pending">{{ serviceForm.id ? 'Сохранить услугу' : 'Добавить услугу' }}</button>
          <button v-if="serviceForm.id" class="secondary" type="button" @click="resetService">Отмена</button>
        </div>
      </form>
      <table v-if="profile && profile.services.length">
        <thead>
          <tr>
            <th>Услуга</th>
            <th>Цена</th>
            <th></th>
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
    <p v-if="error" class="error">{{ error }}</p>
  </main>
</template>

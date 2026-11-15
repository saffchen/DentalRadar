<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UserShell from '../components/layout/UserShell.vue'
import Rating from '../components/Rating.vue'
import PriceLabel from '../components/PriceLabel.vue'
import DoctorCard from '../components/DoctorCard.vue'
import UiStates from '../components/ui/UiStates.vue'
import { useClinicStore } from '../stores/clinics'
import { usePreferenceStore } from '../stores/preferences'
import { doctorsFor, DEMO_HOURS } from '../data/demo'

const route = useRoute()
const router = useRouter()
const clinicStore = useClinicStore()
const preferences = usePreferenceStore()
const detail = ref(null)
const status = ref('loading')
const error = ref('')
const tab = ref('about')
const doctors = computed(() => doctorsFor(route.params.id))

onMounted(load)

async function load() {
  status.value = 'loading'
  error.value = ''
  try {
    detail.value = await clinicStore.detail(route.params.id)
    status.value = 'ready'
  } catch (requestError) {
    status.value = 'error'
    error.value = requestError.message
  }
}

function book(serviceName) {
  router.push({ path: '/booking', query: { clinicId: detail.value.id, service: serviceName || '' } })
}

function routeTo() {
  window.open(`https://www.openstreetmap.org/directions?to=${detail.value.latitude}%2C${detail.value.longitude}`, '_blank', 'noopener')
}
</script>

<template>
  <UserShell>
    <article v-if="status === 'loading'" class="page-narrow"><div class="skeleton-card tall"></div></article>
    <article v-else-if="status === 'error'" class="page-narrow">
      <UiStates kind="error" title="Не удалось загрузить данные" :text="error" action="Повторить" @action="load" />
    </article>
    <article v-else class="page-narrow clinic-page">
      <button class="text-button" type="button" @click="router.back()">Назад</button>
      <div class="gallery" aria-label="Галерея">
        <div class="gallery-slide">Фото клиники не загружено</div>
      </div>
      <header class="detail-head">
        <div>
          <h1>{{ detail.name }}</h1>
          <p>{{ detail.address }}</p>
          <Rating :value="detail.averageRating" :count="detail.reviewCount" />
        </div>
        <button class="favorite" type="button" :aria-pressed="preferences.favoriteIds.includes(detail.id)" @click="preferences.toggleFavorite(detail.id)">
          {{ preferences.favoriteIds.includes(detail.id) ? '♥' : '♡' }}
        </button>
      </header>
      <p class="note">Координаты: {{ detail.latitude }}, {{ detail.longitude }}</p>
      <div class="detail-actions">
        <button class="primary-button" type="button" @click="book()">Записаться</button>
        <button class="secondary-button" type="button" @click="routeTo">Построить маршрут</button>
      </div>
      <div class="tabs" role="tablist">
        <button v-for="item in [['about','О клинике'],['prices','Услуги и цены'],['reviews','Отзывы'],['doctors','Врачи']]" :key="item[0]" type="button" :aria-selected="tab === item[0]" @click="tab = item[0]">{{ item[1] }}</button>
      </div>
      <section v-if="tab === 'about'">
        <p>{{ detail.description || 'Описание клиники пока не заполнено.' }}</p>
        <h2>График работы</h2>
        <p class="note">Пример графика. Клиника ещё не передала своё расписание.</p>
        <ul class="plain-list">
          <li v-for="row in DEMO_HOURS" :key="row.day"><span>{{ row.day }}</span><span>{{ row.value }}</span></li>
        </ul>
        <h2>Контакты</h2>
        <p class="note">Клиника не указала телефон и почту.</p>
      </section>
      <section v-else-if="tab === 'prices'">
        <ul class="price-list">
          <li v-for="item in detail.services" :key="item.id">
            <div>
              <strong>{{ item.name }}</strong>
              <p class="note">Стоматологическая услуга клиники.</p>
            </div>
            <div class="price-actions">
              <PriceLabel :price="Number(item.price)" />
              <button class="text-button" type="button" @click="book(item.name)">Выбрать</button>
            </div>
          </li>
        </ul>
      </section>
      <section v-else-if="tab === 'reviews'">
        <p v-if="!detail.reviews.length" class="note">Отзывов пока нет.</p>
        <article v-for="review in detail.reviews" :key="review.id" class="profile-review">
          <strong>{{ review.authorName }} · {{ review.rating }} / 5</strong>
          <p>{{ review.text }}</p>
        </article>
      </section>
      <section v-else>
        <UiStates v-if="!doctors.length" kind="empty" title="Врачи не добавлены" text="Клиника пока не передала список врачей." />
        <DoctorCard v-for="doctor in doctors" :key="doctor.id" :doctor="doctor" @select="router.push(`/doctors/${detail.id}/${doctor.id}`)" />
      </section>
    </article>
  </UserShell>
</template>

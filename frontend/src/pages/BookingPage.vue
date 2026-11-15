<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UserShell from '../components/layout/UserShell.vue'
import DoctorCard from '../components/DoctorCard.vue'
import DatePicker from '../components/DatePicker.vue'
import TimeSlot from '../components/TimeSlot.vue'
import { useClinicStore } from '../stores/clinics'
import { DEMO_TIMES, doctorsFor } from '../data/demo'
import { useAppointmentStore } from '../stores/appointments'

const route = useRoute()
const router = useRouter()
const clinicStore = useClinicStore()
const appointments = useAppointmentStore()
const detail = ref(null)
const step = ref(1)
const serviceName = ref(route.query.service || '')
const doctorId = ref(route.query.doctorId || '')
const date = ref(new Date().toISOString().slice(0, 10))
const time = ref('')
const saved = ref(null)

const doctors = computed(() => doctorsFor(route.query.clinicId))
const doctor = computed(() => doctors.value.find((item) => item.id === doctorId.value) || null)
const service = computed(() => detail.value?.services.find((item) => item.name === serviceName.value) || null)
const openTimes = computed(() => DEMO_TIMES.filter((slot) => !slot.disabled))

onMounted(async () => {
  detail.value = await clinicStore.detail(route.query.clinicId)
  if (!serviceName.value && detail.value.services[0]) serviceName.value = detail.value.services[0].name
  if (!doctorId.value && doctors.value[0]) doctorId.value = doctors.value[0].id
})

function saveDraft() {
  saved.value = appointments.add({
    clinicId: detail.value.id,
    clinicName: detail.value.name,
    clinicAddress: detail.value.address,
    serviceName: serviceName.value,
    price: service.value ? Number(service.value.price) : null,
    doctorName: doctor.value?.name || 'Врач не выбран',
    date: date.value,
    time: time.value
  })
  step.value = 6
}
</script>

<template>
  <UserShell>
    <article v-if="detail" class="page-narrow booking-page">
      <p class="demo-banner">Демонстрационный режим. Свободное время и подтверждение клиники не запрашиваются.</p>
      <h1>Запись на приём</h1>
      <p>{{ detail.name }}</p>
      <p class="note">{{ detail.address }}</p>
      <ol class="steps">
        <li :class="{ current: step === 1 }">Услуга</li>
        <li :class="{ current: step === 2 }">Врач</li>
        <li :class="{ current: step === 3 }">Дата</li>
        <li :class="{ current: step === 4 }">Время</li>
        <li :class="{ current: step === 5 }">Проверка</li>
      </ol>

      <section v-if="step === 1">
        <label v-for="item in detail.services" :key="item.id" class="choice">
          <input v-model="serviceName" type="radio" name="service" :value="item.name" />
          <span>{{ item.name }}</span>
          <strong>{{ item.price }} ₽</strong>
        </label>
        <button class="primary-button" type="button" :disabled="!serviceName" @click="step = 2">Дальше</button>
      </section>

      <section v-else-if="step === 2">
        <p v-if="!doctors.length" class="note">У клиники нет списка врачей. Запись к конкретному врачу недоступна.</p>
        <DoctorCard v-for="item in doctors" :key="item.id" :doctor="item" :selected="doctorId === item.id" @select="doctorId = item.id" />
        <button class="primary-button" type="button" :disabled="doctors.length && !doctorId" @click="step = 3">Дальше</button>
      </section>

      <section v-else-if="step === 3">
        <DatePicker v-model="date" />
        <button class="primary-button" type="button" @click="step = 4">Дальше</button>
      </section>

      <section v-else-if="step === 4">
        <p v-if="!openTimes.length" class="note">Нет свободного времени для записи.</p>
        <TimeSlot :slots="DEMO_TIMES" :model-value="time" @update:model-value="time = $event" />
        <button class="primary-button" type="button" :disabled="!time" @click="step = 5">Дальше</button>
      </section>

      <section v-else-if="step === 5" class="summary">
        <p><span>Клиника</span><strong>{{ detail.name }}</strong></p>
        <p><span>Адрес</span><strong>{{ detail.address }}</strong></p>
        <p><span>Услуга</span><strong>{{ serviceName }}</strong></p>
        <p><span>Врач</span><strong>{{ doctor?.name || 'Не выбран' }}</strong></p>
        <p><span>Дата</span><strong>{{ date }}</strong></p>
        <p><span>Время</span><strong>{{ time }}</strong></p>
        <p><span>Стоимость</span><strong>{{ service ? `${service.price} ₽` : 'Не указана' }}</strong></p>
        <button class="primary-button" type="button" @click="saveDraft">Сохранить черновик</button>
      </section>

      <section v-else class="summary">
        <h2>Черновик сохранён</h2>
        <p class="note">Клиника не получила эту запись. Это демонстрационный режим, без подтверждения от системы записи.</p>
        <p>{{ detail.name }} · {{ serviceName }}</p>
        <p>{{ doctor?.name }} · {{ date }} · {{ time }}</p>
        <button class="primary-button" type="button" @click="router.push('/appointments')">К списку записей</button>
      </section>
    </article>
  </UserShell>
</template>

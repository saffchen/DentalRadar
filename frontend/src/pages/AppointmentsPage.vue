<script setup>
import { computed, ref } from 'vue'
import UserShell from '../components/layout/UserShell.vue'
import UiStates from '../components/ui/UiStates.vue'
import { useAppointmentStore } from '../stores/appointments'

const appointments = useAppointmentStore()
const tab = ref('draft')
const pendingCancel = ref('')
appointments.reload()

const groups = computed(() => ({
  draft: appointments.items.filter((item) => item.status === 'draft'),
  done: appointments.items.filter((item) => item.status === 'done'),
  cancelled: appointments.items.filter((item) => item.status === 'cancelled')
}))
</script>

<template>
  <UserShell>
    <section class="page-narrow">
      <h1>Мои записи</h1>
      <div class="tabs">
        <button type="button" :aria-selected="tab === 'draft'" @click="tab = 'draft'">Черновики</button>
        <button type="button" :aria-selected="tab === 'done'" @click="tab = 'done'">Завершённые</button>
        <button type="button" :aria-selected="tab === 'cancelled'" @click="tab = 'cancelled'">Отменённые</button>
      </div>
      <UiStates v-if="!groups[tab].length" kind="empty" title="Записей пока нет" text="Здесь появятся записи выбранного статуса." action="Найти клинику" @action="$router.push('/map')" />
      <article v-for="item in groups[tab]" :key="item.id" class="record-card">
        <strong>{{ item.clinicName }}</strong>
        <p>{{ item.serviceName }}</p>
        <p>{{ item.doctorName }}</p>
        <p>{{ item.date }} · {{ item.time }}</p>
        <p v-if="item.price != null">{{ item.price }} ₽</p>
        <p class="note">{{ item.status === 'draft' ? 'Черновик, клиника не подтвердила' : item.status === 'cancelled' ? 'Отменена' : 'Завершена' }}</p>
        <button v-if="item.status === 'draft'" class="secondary-button" type="button" @click="pendingCancel = item.id">Отменить</button>
        <div v-if="pendingCancel === item.id" class="confirm-row">
          <p>Отменить этот черновик?</p>
          <button class="primary-button" type="button" @click="appointments.cancel(item.id); pendingCancel = ''">Да, отменить</button>
          <button class="text-button" type="button" @click="pendingCancel = ''">Нет</button>
        </div>
      </article>
    </section>
  </UserShell>
</template>

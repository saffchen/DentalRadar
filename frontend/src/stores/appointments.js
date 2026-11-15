import { defineStore } from 'pinia'

const keyFor = (userId) => `appointments:${userId || 'guest'}`

export const useAppointmentStore = defineStore('appointments', {
  state: () => ({
    items: read(localStorage.getItem('userId'))
  }),
  actions: {
    reload() {
      this.items = read(localStorage.getItem('userId'))
    },
    add(entry) {
      this.items = [{ id: crypto.randomUUID(), createdAt: new Date().toISOString(), status: 'draft', demo: true, ...entry }, ...this.items]
      this.save()
    },
    cancel(id) {
      this.items = this.items.map((item) => item.id === id ? { ...item, status: 'cancelled' } : item)
      this.save()
    },
    save() {
      localStorage.setItem(keyFor(localStorage.getItem('userId')), JSON.stringify(this.items))
    },
    clear() {
      this.items = []
      localStorage.removeItem(keyFor(localStorage.getItem('userId')))
    }
  }
})

function read(userId) {
  try {
    const stored = JSON.parse(localStorage.getItem(keyFor(userId)) || '[]')
    return Array.isArray(stored) ? stored : []
  } catch {
    return []
  }
}

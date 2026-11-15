import { defineStore } from 'pinia'
import { api } from '../api'

export const useClinicStore = defineStore('clinics', {
  state: () => ({
    items: [],
    status: 'idle',
    error: ''
  }),
  actions: {
    async load(force = false) {
      if (this.status === 'ready' && !force) return
      this.status = 'loading'
      this.error = ''
      try {
        this.items = await api('/api/clinics')
        this.status = 'ready'
      } catch (error) {
        this.status = 'error'
        this.error = error.message
      }
    },
    async detail(id) {
      return api(`/api/clinics/${id}`)
    }
  }
})

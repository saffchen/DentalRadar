import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    displayName: localStorage.getItem('displayName') || '',
    userId: localStorage.getItem('userId') || ''
  }),
  actions: {
    setSession(data) {
      this.token = data.token
      this.role = data.role
      this.displayName = data.displayName
      this.userId = data.userId
      localStorage.setItem('token', data.token)
      localStorage.setItem('role', data.role)
      localStorage.setItem('displayName', data.displayName)
      localStorage.setItem('userId', data.userId)
    },
    logout() {
      this.token = ''
      this.role = ''
      this.displayName = ''
      this.userId = ''
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('displayName')
      localStorage.removeItem('userId')
    }
  }
})

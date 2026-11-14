import { useAuthStore } from './stores/auth'

export async function api(path, options = {}) {
  const headers = new Headers(options.headers || {})
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  const token = localStorage.getItem('token')
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  const response = await fetch(path, { ...options, headers })
  const text = await response.text()
  let data = null
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = null
    }
  }
  if (response.status === 401 && !path.startsWith('/api/auth/')) {
    useAuthStore().logout()
    if (window.location.pathname !== '/login') {
      window.location.assign('/login')
    }
  }
  if (!response.ok) {
    const error = new Error(data?.error || 'Запрос не выполнен')
    error.status = response.status
    throw error
  }
  return data
}

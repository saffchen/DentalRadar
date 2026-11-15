import { defineStore } from 'pinia'
import { cityById } from '../data/cities'

const saved = readSession()

export const usePreferenceStore = defineStore('preferences', {
  state: () => ({
    query: saved.query,
    services: saved.services,
    minRating: saved.minRating,
    minPrice: saved.minPrice,
    maxPrice: saved.maxPrice,
    cityId: saved.cityId,
    scroll: saved.scroll,
    favoriteIds: readFavorites()
  }),
  getters: {
    city: (state) => cityById(state.cityId)
  },
  actions: {
    persist() {
      sessionStorage.setItem('moy-search', JSON.stringify({
        query: this.query,
        services: this.services,
        minRating: this.minRating,
        minPrice: this.minPrice,
        maxPrice: this.maxPrice,
        cityId: this.cityId,
        scroll: this.scroll
      }))
    },
    toggleFavorite(id) {
      this.favoriteIds = this.favoriteIds.includes(id)
        ? this.favoriteIds.filter((item) => item !== id)
        : [...this.favoriteIds, id]
      localStorage.setItem(favoriteKey(), JSON.stringify(this.favoriteIds))
    },
    resetFilters() {
      this.services = []
      this.minRating = ''
      this.minPrice = 0
      this.maxPrice = 100000
      this.persist()
    }
  }
})

function readSession() {
  try {
    const data = JSON.parse(sessionStorage.getItem('moy-search') || '{}')
    return {
      query: data.query || '',
      services: Array.isArray(data.services) ? data.services : [],
      minRating: data.minRating || '',
      minPrice: Number(data.minPrice) || 0,
      maxPrice: Number(data.maxPrice) || 100000,
      cityId: data.cityId || 'moscow',
      scroll: Number(data.scroll) || 0
    }
  } catch {
    return { query: '', services: [], minRating: '', minPrice: 0, maxPrice: 100000, cityId: 'moscow', scroll: 0 }
  }
}

function favoriteKey() {
  return `favorites:${localStorage.getItem('userId') || 'guest'}`
}

function readFavorites() {
  try {
    const data = JSON.parse(localStorage.getItem(favoriteKey()) || '[]')
    return Array.isArray(data) ? data : []
  } catch {
    return []
  }
}

import { computed } from 'vue'
import { useClinicStore } from '../stores/clinics'
import { usePreferenceStore } from '../stores/preferences'

export function useClinicSearch() {
  const clinics = useClinicStore()
  const preferences = usePreferenceStore()

  const serviceOptions = computed(() => {
    const names = new Set()
    clinics.items.forEach((clinic) => clinic.services.forEach((item) => names.add(item.name)))
    return [...names].sort((left, right) => left.localeCompare(right, 'ru'))
  })

  const results = computed(() => clinics.items.filter((clinic) => matches(clinic, preferences)))

  return { clinics, preferences, serviceOptions, results }
}

export function matches(clinic, preferences) {
  const query = preferences.query.trim().toLowerCase()
  if (query) {
    const blob = `${clinic.name} ${clinic.address} ${clinic.services.map((item) => item.name).join(' ')}`.toLowerCase()
    if (!blob.includes(query)) return false
  }
  if (preferences.services.length) {
    const names = clinic.services.map((item) => item.name)
    if (!preferences.services.some((name) => names.includes(name))) return false
  }
  if (preferences.minRating && (clinic.averageRating == null || clinic.averageRating < Number(preferences.minRating))) {
    return false
  }
  const prices = clinic.services.map((item) => Number(item.price))
  const lowest = prices.length ? Math.min(...prices) : 0
  if (lowest < preferences.minPrice || lowest > preferences.maxPrice) return false
  return true
}

export function lowestPrice(clinic) {
  const prices = clinic.services.map((item) => Number(item.price)).filter((price) => price > 0)
  return prices.length ? Math.min(...prices) : null
}

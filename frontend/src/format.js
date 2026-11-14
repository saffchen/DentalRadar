export function formatPrice(value) {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB'
  }).format(value)
}

export function formatDate(value) {
  return new Intl.DateTimeFormat('ru-RU', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value))
}

export function formatRating(value, count) {
  if (value == null) {
    return 'Нет оценок'
  }
  const reviews = count === 1 ? 'отзыв' : count < 5 ? 'отзыва' : 'отзывов'
  return `${value.toFixed(1)} · ${count} ${reviews}`
}

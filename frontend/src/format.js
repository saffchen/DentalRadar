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
  const total = Number(count) || 0
  return `${Number(value).toFixed(1)} · ${total} ${pluralReviews(total)}`
}

function pluralReviews(count) {
  const mod100 = count % 100
  const mod10 = count % 10
  if (mod100 >= 11 && mod100 <= 14) {
    return 'отзывов'
  }
  if (mod10 === 1) {
    return 'отзыв'
  }
  if (mod10 >= 2 && mod10 <= 4) {
    return 'отзыва'
  }
  return 'отзывов'
}

export const CITIES = [
  { id: 'moscow', name: 'Москва', latitude: 55.751244, longitude: 37.618423 },
  { id: 'spb', name: 'Санкт-Петербург', latitude: 59.93863, longitude: 30.31413 },
  { id: 'kazan', name: 'Казань', latitude: 55.796127, longitude: 49.106414 },
  { id: 'novosibirsk', name: 'Новосибирск', latitude: 55.030204, longitude: 82.92043 }
]

export function cityById(id) {
  return CITIES.find((city) => city.id === id) || CITIES[0]
}

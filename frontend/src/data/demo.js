export const DEMO_HOURS = [
  { day: 'Пн–Пт', value: '09:00–21:00' },
  { day: 'Сб', value: '10:00–18:00' },
  { day: 'Вс', value: 'Выходной' }
]

const doctors = [
  { id: 'd-center-1', clinicId: 'clinic-center', name: 'Смирнова А. В.', specialty: 'Терапевт', experience: '12 лет', rating: 4.9, about: 'Лечение кариеса и гигиена.', demo: true },
  { id: 'd-center-2', clinicId: 'clinic-center', name: 'Орлов И. П.', specialty: 'Хирург', experience: '8 лет', rating: 4.7, about: 'Удаление и имплантация.', demo: true },
  { id: 'd-north-1', clinicId: 'clinic-north', name: 'Кузнецова М. С.', specialty: 'Гигиенист', experience: '6 лет', rating: 4.8, about: 'Профессиональная чистка.', demo: true },
  { id: 'd-south-1', clinicId: 'clinic-south', name: 'Павлов Д. Н.', specialty: 'Ортопед', experience: '15 лет', rating: 4.9, about: 'Протезирование и имплантация.', demo: true }
]

export function doctorsFor(clinicId) {
  return doctors.filter((doctor) => doctor.clinicId === clinicId)
}

export function doctorById(id) {
  return doctors.find((doctor) => doctor.id === id) || null
}

export const DEMO_TIMES = [
  { value: '10:00', disabled: false },
  { value: '11:00', disabled: true },
  { value: '12:00', disabled: false },
  { value: '13:00', disabled: false },
  { value: '15:00', disabled: true },
  { value: '16:00', disabled: false },
  { value: '18:00', disabled: false }
]

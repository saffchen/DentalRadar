import L from 'leaflet'
import { setWorkerUrl } from 'maplibre-gl'
import workerUrl from 'maplibre-gl/dist/maplibre-gl-worker.mjs?worker&url'
import { maplibreGL } from '@maplibre/maplibre-gl-leaflet'

let workerReady = false

export function mountMap(element, { center, clinics, selectedId, onSelect }) {
  if (!workerReady) {
    setWorkerUrl(workerUrl)
    workerReady = true
  }
  const map = L.map(element, { zoomControl: true })
  map.attributionControl.setPrefix(false)
  maplibreGL({ style: 'https://tiles.openfreemap.org/styles/liberty' }).addTo(map)
  map.setView(center, 12)
  const markers = L.layerGroup().addTo(map)
  const icon = (active) => L.divIcon({
    className: 'clinic-marker',
    html: `<span class="clinic-marker-dot${active ? ' is-active' : ''}"></span>`,
    iconSize: [28, 28],
    iconAnchor: [14, 28]
  })
  function draw(list, activeId) {
    markers.clearLayers()
    list.forEach((clinic) => {
      const marker = L.marker([clinic.latitude, clinic.longitude], { icon: icon(clinic.id === activeId) })
      marker.on('click', () => onSelect(clinic))
      markers.addLayer(marker)
    })
  }
  draw(clinics, selectedId)
  return {
    map,
    draw,
    locate() {
      if (!navigator.geolocation) return Promise.resolve(false)
      return new Promise((resolve) => {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            map.setView([position.coords.latitude, position.coords.longitude], 13)
            resolve(true)
          },
          () => resolve(false),
          { enableHighAccuracy: true, timeout: 8000 }
        )
      })
    },
    destroy() {
      map.remove()
    }
  }
}

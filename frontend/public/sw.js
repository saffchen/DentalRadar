const SHELL = ['/', '/offline.html', '/manifest.webmanifest', '/icon.jpg']

self.addEventListener('install', (event) => {
  event.waitUntil(caches.open('moy-shell-v1').then((cache) => cache.addAll(SHELL)))
  self.skipWaiting()
})

self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim())
})

self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url)
  if (event.request.method !== 'GET' || url.pathname.startsWith('/api/')) return
  event.respondWith(
    fetch(event.request).catch(async () => {
      const cached = await caches.match(event.request)
      return cached || caches.match('/offline.html')
    })
  )
})

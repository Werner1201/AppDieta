const CACHE = "dieta-v5";
self.addEventListener("install", event => event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(["/", "/static/app.css?v=5", "/static/app.js?v=5"]))));
self.addEventListener("activate", event => event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(key => key !== CACHE).map(key => caches.delete(key))))));
self.addEventListener("fetch", event => {
  if (event.request.mode === "navigate") {
    event.respondWith(fetch(event.request).catch(() => caches.match("/")));
    return;
  }
  event.respondWith(caches.match(event.request).then(hit => hit || fetch(event.request)));
});

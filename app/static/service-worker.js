self.addEventListener("install",event=>event.waitUntil(caches.open("dieta-v1").then(cache=>cache.addAll(["/","/static/app.css","/static/app.js"]))));
self.addEventListener("fetch",event=>event.respondWith(caches.match(event.request).then(hit=>hit||fetch(event.request))));

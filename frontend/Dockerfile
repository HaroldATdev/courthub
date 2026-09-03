# ============================================================
# CourtHub · Dockerfile del Frontend (sitio estático + Nginx)
# Nginx sirve las vistas y actúa como proxy inverso de /api
# hacia el contenedor del backend.
# ============================================================
FROM nginx:1.27-alpine

# Configuración de Nginx (sitio estático + proxy /api -> backend:3000)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Archivos del sitio (HTML, CSS, JS)
COPY . /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

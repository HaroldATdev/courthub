# 🏟️ CourtHub — Sistema de Gestión de Canchas Deportivas

Proyecto final · **Marcos de Desarrollo Web**
Sistema de reservas de canchas deportivas (fútbol, fulbito, pádel, tenis y squash) con autenticación por roles, gestión de canchas, reservas con bloques horarios y clases impartidas por entrenadores.

| Capa     | Tecnología                                        |
|----------|---------------------------------------------------|
| Frontend | HTML5 + TailwindCSS (CDN) + JavaScript vanilla    |
| Backend  | Node.js 20 + Express (API REST + JWT + bcrypt)    |
| Base de datos | PostgreSQL 16 (schema + seed automáticos)    |
| Despliegue | Docker + Docker Compose (con volumen persistente) |

---

## 📚 Documentación del proyecto (rúbrica de evaluación)

### 1️⃣ Descripción general de la organización

**CourtHub** es una empresa administradora de complejos deportivos que gestiona una red de **9 canchas asociadas** de pádel, fútbol 7 y 11, tenis y squash. Cada sede ofrece alquiler de canchas por bloques horarios, servicios complementarios (estacionamiento, camarines, duchas, cafetería, iluminación LED) y **clases deportivas impartidas por entrenadores certificados** con cupos limitados.

- **Modelo de negocio:** alquiler de canchas por hora (precios en **soles peruanos, S/**) + inscripciones a clases + cargo por servicio del 10 % sobre cada reserva.
- **Actores del sistema:** *Cliente* (reserva y se inscribe), *Entrenador* (dicta clases), *Administrador* (supervisa todas las reservas de la red).
- **Canales actuales:** teléfono, WhatsApp y registro manual en cuadernos → de ahí nace la necesidad de automatización.

### 2️⃣ Proceso o procesos que se planea automatizar

| # | Proceso actual (manual) | Automatización con CourtHub |
|---|--------------------------|------------------------------|
| 1 | Consultar disponibilidad por teléfono/WhatsApp | Motor de **disponibilidad en tiempo real** con bloques de 1.5 h por cancha y fecha |
| 2 | Registrar la reserva en un cuaderno (genera dobles reservas) | **Reserva en línea** con bloqueo del bloque horario en base de datos (índice único parcial que impide solapamientos) |
| 3 | Cobro presencial o transferencia sin comprobante | **Módulo de pago** (tarjeta/PayPal/Webpay) con cálculo automático: subtotal + cargo por servicio 10 % + total |
| 4 | Reprogramaciones/cancelaciones por llamada | **Autoservicio**: el cliente cancela o reprograma desde su historial, liberando el bloque al instante |
| 5 | Inscripción a clases por lista manual | **Inscripción en línea con control de cupos** (barra de cupo, sin duplicados) |
| 6 | Historial disperso (sin seguimiento del cliente) | **Historial digital**: pestañas Próximas / Pasadas / Canceladas con totales pagados |

### 3️⃣ Problema encontrado y oportunidad de mejora

**Problemas detectados en la operación manual:**
- 🔴 **Reservas duplicadas:** dos clientes reservan la misma cancha y hora por canales distintos → conflictos el día del partido y pérdida de credibilidad.
- 🔴 **Tiempo del cliente:** coordinar por WhatsApp implica esperas de horas; no hay confirmación inmediata.
- 🔴 **Sin trazabilidad:** no existe historial de reservas, montos pagados ni comprobantes; las cancelaciones dependen de la memoria del administrador.
- 🔴 **Ingresos perdidos:** horas valle sin vender y clases que no se llenan por falta de difusión centralizada.

**Oportunidad de mejora:** una plataforma web de autoservicio disponible 24/7 que garantice la **unicidad de cada bloque reservado** a nivel de base de datos, confirme el pago al instante, centralice el historial del cliente y promueva las clases de los entrenadores — aumentando la ocupación de las canchas y reduciendo la carga operativa.

### 4️⃣ Diseño de las pantallas en Figma (6 pantallas · mínimo requerido: 5)

> 🔗 **Prototipo navegable en Figma:** `PEGAR_AQUI_EL_LINK_DE_TU_FIGMA`

| # | Pantalla diseñada | Elementos del prototipo |
|---|--------------------|--------------------------|
| 1 | **Login / Registro** | Pestañas Iniciar Sesión / Registrarse, campos con iconos, mostrar/ocultar contraseña, acceso social (Google/Apple), panel lateral con branding |
| 2 | **Dashboard (canchas disponibles)** | Buscador, pills por deporte, filtros de comuna y rango de precio, tarjetas de cancha con foto, rating, disponibilidad "Hoy" y precio/hora |
| 3 | **Detalle de cancha** | Galería, servicios con iconos, reglamento, **calendario mensual** + bloques horarios de 1.5 h (libres/ocupados) y resumen de selección |
| 4 | **Pago** | Métodos (tarjeta/PayPal/Webpay), formato automático del número de tarjeta, resumen con subtotal, cargo por servicio y total |
| 5 | **Mis reservas** | Pestañas con contadores (Próximas / Pasadas / Canceladas), tarjetas con estado, modales de cancelar, reprogramar y detalle |
| 6 | **Clases deportivas** | Filtro por deporte, tarjetas con entrenador, fecha/hora, **barra de cupo** y botón de inscripción |

### 5️⃣ Creación de las pantallas del proyecto web (HTML + CSS + Framework · 6 pantallas · mínimo requerido: 4)

Implementadas con **HTML5 + TailwindCSS (framework de utilidades) + JavaScript vanilla (ES6)** y consumiendo una **API REST real** (con modo demo local si el backend no está disponible):

| Pantalla Figma | Archivo implementado | Funcionalidad real |
|-----------------|----------------------|---------------------|
| Login / Registro | `frontend/index.html` | Autenticación real contra `/api/auth/login` y `/api/auth/register` (JWT + bcrypt) |
| Dashboard | `frontend/dashboard.html` | Listado desde `GET /api/canchas`, filtros y búsqueda funcionales |
| Detalle de cancha | `frontend/cancha-detalle.html` | `GET /api/canchas/:id/disponibilidad`, calendario y selección de bloque |
| Pago | `frontend/pago.html` | `POST /api/reservas` con validación de solapamiento y cálculo de totales |
| Mis reservas | `frontend/mis-reservas.html` | `GET /api/reservas`, cancelar y reprogramar (`PATCH`) |
| Clases | `frontend/clases.html` | `GET /api/clases`, inscripción/cupo real (`POST/DELETE /api/clases/:id/inscribirse`) |

Componentes compartidos: `frontend/css/styles.css` (botones, inputs con icono, pills, slots, calendario, modales, toasts) y `frontend/js/ui.js` (header, footer, sesión y utilidades).

### 6️⃣ Exposición — guion de demostración sugerido (≈5 min)

1. **Login** (`demo@courthub.com` / `Password123`) → mostrar validaciones y JWT en localStorage.
2. **Dashboard** → filtrar por deporte y rango de precio; explicar datos reales desde PostgreSQL.
3. **Detalle de cancha** → elegir fecha en el calendario y un bloque libre (mostrar uno ocupado).
4. **Pago** → completar tarjeta (se formatea sola) → confirmar → ver totales calculados (subtotal + 10 %).
5. **Mis reservas** → la reserva aparece en "Próximas"; cancelarla y mostrar que el bloque se libera; reprogramar otra.
6. **Clases** → inscribirse a una clase y ver la barra de cupo actualizarse.
7. Cerrar con la **arquitectura Docker**: `docker compose ps` mostrando db + backend + frontend y el proxy `/api`.

> 💡 **Tip:** si algo falla en vivo, el frontend tiene modo demo con datos mock (`js/data.js`) — la demo nunca se cae.

<!--RUBRICA-NEXT-->

## 📁 Estructura del proyecto

```
Proyecto final - Marcos de Desarrollo Web/
│
├── frontend/                     # Sitio estático (lo primero en evaluarse)
│   ├── index.html                # Login / Registro (prototipo 1)
│   ├── dashboard.html            # Canchas disponibles + filtros (prototipo 2)
│   ├── cancha-detalle.html       # Detalle + calendario + bloques horarios (prototipo 3)
│   ├── pago.html                 # Pago y confirmación de reserva (prototipo 4)
│   ├── mis-reservas.html         # Historial: próximas/pasadas/canceladas (prototipo 5)
│   ├── clases.html               # Clases deportivas (módulo de entrenadores)
│   ├── css/styles.css            # Componentes compartidos (botones, pills, slots…)
│   ├── js/
│   │   ├── icons.js              # Íconos SVG inline
│   │   ├── data.js               # Datos mock (canchas, clases, reservas demo)
│   │   ├── api.js                # Cliente HTTP: usa el backend si existe,
│   │   │                         # si no, responde con datos mock (modo demo)
│   │   └── ui.js                 # Header, footer, toasts, sesión y utilidades
│   ├── Dockerfile                # Nginx:alpine (sitio estático + proxy /api)
│   └── nginx.conf                # Proxy inverso de /api → backend:3000
│
├── backend/                      # API REST (Node.js + Express)
│   ├── package.json
│   ├── Dockerfile                # node:20-alpine
│   ├── .env.example
│   └── src/
│       ├── server.js             # Punto de entrada de Express
│       ├── config/db.js          # Pool de conexiones PostgreSQL (pg)
│       ├── middleware/auth.js    # JWT (autenticar) + roles (autorizar)
│       ├── utils/horarios.js     # Bloques de 1.5 h y utilidades de fecha
│       ├── controllers/          # auth · canchas · reservas · clases
│       └── routes/               # auth · canchas · reservas · clases
│
├── database/
│   ├── 01-schema.sql             # Tablas: usuarios, canchas, disponibilidad,
│   │                             # reservas, clases, inscripciones + índices
│   └── 02-seed.sql               # Datos demo idénticos al prototipo de Figma
│
├── docker-compose.yml            # db + backend + frontend interconectados
└── README.md
```

---

## 🚀 Puesta en marcha (un solo comando)

> Requisito: tener **Docker** (con Compose) instalado.

```bash
docker compose up --build
```

Con esto se levanta automáticamente:

| Servicio  | URL                                    | Descripción                          |
|-----------|----------------------------------------|--------------------------------------|
| Frontend  | http://localhost:8080                  | Login → Dashboard → Reserva → Pago   |
| Backend   | http://localhost:3000/api/health       | API REST (proxy también en `/api`)   |
| PostgreSQL| http://localhost:5433 (host)           | Datos persistentes en volumen `pgdata` |

La base de datos se crea y puebla sola la **primera vez** (scripts en `database/*.sql` montados en `/docker-entrypoint-initdb.d/`).

### 👤 Cuentas de demostración (contraseña: `Password123`)

| Correo                   | Rol           | Nota                                |
|--------------------------|---------------|-------------------------------------|
| `demo@courthub.com`      | Cliente       | Usuario con historial de reservas   |
| `entrenador@courthub.com`| Entrenador    | Carla Rojas — dicta clases          |
| `admin@courthub.com`     | Administrador | Puede ver todas las reservas        |

> 💡 **Modo demo sin backend:** puedes abrir `frontend/index.html` con cualquier servidor estático (o doble clic) y todo funciona con datos mock en `localStorage`. Si detecta la API en `/api`, usa el backend real automáticamente.

---

## 🛠️ Desarrollo sin Docker

```bash
# 1) Base de datos (requiere PostgreSQL local con la extensión pgcrypto)
psql -U postgres -c "CREATE DATABASE courthub;"
psql -U postgres -d courthub -f database/01-schema.sql
psql -U postgres -d courthub -f database/02-seed.sql

# 2) Backend
cd backend
cp .env.example .env      # ajusta PGHOST=127.0.0.1 si la BD es local
npm install
npm run dev               # http://localhost:3000

# 3) Frontend (cualquier servidor estático)
cd frontend
python3 -m http.server 5500   # http://localhost:5500
```

---

## 🔌 Endpoints principales (API REST)

| Método | Ruta                            | Descripción                                   |
|--------|---------------------------------|-----------------------------------------------|
| POST   | `/api/auth/register`            | Registro (cliente o entrenador)               |
| POST   | `/api/auth/login`               | Login → devuelve **JWT**                      |
| GET    | `/api/auth/me`                  | Perfil del usuario autenticado 🔒             |
| GET    | `/api/canchas?tipo=&q=`         | Listado de canchas con filtros 🔒             |
| GET    | `/api/canchas/:id`              | Detalle de una cancha 🔒                      |
| GET    | `/api/canchas/:id/disponibilidad?fecha=` | Bloques horarios ocupados/libres 🔒  |
| GET    | `/api/reservas`                 | Mis reservas (historial) 🔒                   |
| POST   | `/api/reservas`                 | Crear reserva (valida choques de horario) 🔒  |
| PATCH  | `/api/reservas/:id/cancelar`    | Cancelar reserva 🔒                           |
| PATCH  | `/api/reservas/:id/reprogramar` | Reprogramar fecha/hora 🔒                     |
| GET    | `/api/clases?deporte=`          | Clases disponibles con cupos 🔒               |
| POST   | `/api/clases/:id/inscribirse`   | Apuntarse a una clase 🔒                      |
| DELETE | `/api/clases/:id/inscribirse`   | Anular inscripción 🔒                         |

🔒 = requiere header `Authorization: Bearer <token>`

---

## 🧩 Módulos implementados

1. **Autenticación y roles** — registro/login con bcrypt + JWT; roles `cliente`, `entrenador`, `administrador` (el admin se crea solo por seed y puede listar todas las reservas).
2. **Gestión de canchas** — listado con búsqueda, filtro por deporte/precio/comuna, disponibilidad "hoy", detalle con servicios y reglamento, y bloques horarios de 1.5 h con calendario mensual.
3. **Reservas** — flujo completo: seleccionar cancha → fecha/bloque → pago (tarjeta/PayPal/Webpay) → confirmación; historial con pestañas (Próximas / Pasadas / Canceladas), cancelar y reprogramar con validación de solapamiento en BD (índice único parcial).
4. **Clases deportivas** — clases dictadas por entrenadores con cupos, inscripción/desinscripción y control de duplicados.

## 📝 Notas para la exposición

- El frontend replica el prototipo de Figma (paleta verde `#10b981`, tipografías Poppins/Inter, mismas vistas y textos).
- `js/api.js` muestra la **integración real frontend ↔ backend**: si `docker compose up` está corriendo, las llamadas van a la API; si se evalúa solo el frontend, funciona en modo demo.
- Para reiniciar la BD desde cero: `docker compose down -v && docker compose up --build`.


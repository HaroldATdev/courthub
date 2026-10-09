# 🏟️ CourtHub — Sistema de Gestión de Canchas Deportivas

Proyecto final · **Marcos de Desarrollo Web**
Sistema de reservas de canchas deportivas (fútbol, fulbito, pádel, tenis y squash) con autenticación por roles, gestión de canchas, reservas con bloques horarios, clases impartidas por entrenadores y un **panel de administración tipo ERP**, construido con **arquitectura MVC sobre Spring Boot**.

| Capa          | Tecnología                                                                                     |
|---------------|------------------------------------------------------------------------------------------------|
| **Model**     | Entidades JPA (`Usuario`, `Cancha`, `Reserva`, `Clase`, `Inscripcion`) + repositorios Spring Data + servicios de negocio |
| **View**      | Plantillas **Thymeleaf** (HTML5 + TailwindCSS + JavaScript vanilla)                            |
| **Controller**| `@Controller` de Spring MVC (formularios, `Model`, redirecciones) + `@RestController` (API REST JSON) |
| Seguridad     | Spring Security + JWT (cookie HttpOnly para las vistas · header Bearer para la API) + BCrypt    |
| Base de datos | **PostgreSQL 16** (esquema + datos demo cargados automáticamente por Spring)                   |
| Despliegue    | Docker + Docker Compose (build multi-etapa con Maven, volumen persistente)                     |

---

## 📚 Documentación del proyecto (rúbrica de evaluación)

### 1️⃣ Descripción general de la organización

**CourtHub** es una empresa administradora de complejos deportivos que gestiona una red de **9 canchas asociadas** en **Lima Metropolitana (Perú)** — pádel, fútbol 7 y 11, tenis y squash. Cada sede ofrece alquiler de canchas por bloques horarios, servicios complementarios (estacionamiento, camarines, duchas, cafetería, iluminación LED) y **clases deportivas impartidas por entrenadores certificados** con cupos limitados.

- **Modelo de negocio:** alquiler de canchas por hora (precios en **soles peruanos, S/**) + inscripciones a clases + cargo por servicio del 10 % sobre cada reserva.
- **Actores del sistema:** *Cliente* (reserva y se inscribe), *Entrenador* (dicta clases), *Administrador* (gestiona toda la operación desde el panel ERP).
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
| 7 | Control de la operación en hojas de cálculo | **Panel ERP del administrador**: KPIs, ingresos por mes/cancha/deporte/método de pago y mantenimiento de usuarios, canchas, reservas, clases e inscripciones |

### 3️⃣ Problema encontrado y oportunidad de mejora

**Problemas detectados en la operación manual:**
- 🔴 **Reservas duplicadas:** dos clientes reservan la misma cancha y hora por canales distintos → conflictos el día del partido y pérdida de credibilidad.
- 🔴 **Tiempo del cliente:** coordinar por WhatsApp implica esperas de horas; no hay confirmación inmediata.
- 🔴 **Sin trazabilidad:** no existe historial de reservas, montos pagados ni comprobantes; las cancelaciones dependen de la memoria del administrador.
- 🔴 **Ingresos perdidos:** horas valle sin vender y clases que no se llenan por falta de difusión centralizada.
- 🔴 **Sin visión gerencial:** la administración no sabe cuánto factura cada sede ni qué deporte rinde más.

**Oportunidad de mejora:** una plataforma web de autoservicio disponible 24/7 que garantice la **unicidad de cada bloque reservado** a nivel de base de datos, confirme el pago al instante, centralice el historial del cliente, promueva las clases de los entrenadores y entregue a la administración **indicadores en tiempo real** — aumentando la ocupación de las canchas y reduciendo la carga operativa.

### 4️⃣ Diseño de las pantallas en Figma (6 pantallas · mínimo requerido: 5)

> 🔗 **Prototipo navegable en Figma:** `https://www.figma.com/proto/LB87NlUOHl01KwCjoppLgG/Sistema-de-canchas?node-id=2-9&t=fFwPIvKcNqkH6Xxx-1`

| # | Pantalla diseñada | Elementos del prototipo |
|---|--------------------|--------------------------|
| 1 | **Login / Registro** | Pestañas Iniciar Sesión / Registrarse, campos con iconos, mostrar/ocultar contraseña, acceso social (Google/Apple), panel lateral con branding |
| 2 | **Dashboard (canchas disponibles)** | Buscador, pills por deporte, filtros de distrito y rango de precio, tarjetas de cancha con foto, rating, disponibilidad "Hoy" y precio/hora |
| 3 | **Detalle de cancha** | Galería, servicios con iconos, reglamento, **calendario mensual** + bloques horarios de 1.5 h (libres/ocupados) y resumen de selección |
| 4 | **Pago** | Métodos (tarjeta/PayPal/Webpay), formato automático del número de tarjeta, resumen con subtotal, cargo por servicio y total |
| 5 | **Mis reservas** | Pestañas con contadores (Próximas / Pasadas / Canceladas), tarjetas con estado, modales de cancelar, reprogramar y detalle |
| 6 | **Clases deportivas** | Filtro por deporte, tarjetas con entrenador, fecha/hora, **barra de cupo** y botón de inscripción |

### 5️⃣ Creación de las pantallas del proyecto web (HTML + CSS + Framework · 7 vistas · mínimo requerido: 4)

Implementadas como **vistas Thymeleaf** (HTML5 + TailwindCSS + JavaScript vanilla) renderizadas por **controladores Spring MVC**. Cada pantalla recibe sus datos en el `Model` y envía formularios `POST` al controlador:

| Pantalla | Vista (View) | Controlador (Controller) | Datos del modelo / acción |
|-----------|--------------|---------------------------|----------------------------|
| Login / Registro | `templates/index.html` | `AuthViewController` | `POST /login`, `POST /registro` → JWT en cookie, redirección por rol |
| Dashboard | `templates/dashboard.html` | `CanchaViewController#dashboard` | `GET /dashboard?tipo&q&ubicacion&precio` → lista de canchas filtrada (`th:each`) |
| Detalle de cancha | `templates/cancha-detalle.html` | `CanchaViewController#detalle` | `GET /canchas/{id}` → cancha, servicios, reglamento; calendario vía API REST |
| Pago | `templates/pago.html` | `ReservaViewController#pago / #confirmar` | `GET /pago` → cotización · `POST /reservas` (validación de tarjeta y términos) |
| Mis reservas | `templates/mis-reservas.html` | `ReservaViewController` | `GET /mis-reservas?tab` · `POST /reservas/{id}/cancelar` · `POST /reservas/{id}/reprogramar` |
| Clases | `templates/clases.html` | `ClaseViewController` | `GET /clases?deporte` · `POST /clases/{id}/inscribirse` · `POST /clases/{id}/abandonar` |
| **Panel ERP** (resumen + 5 mantenimientos) | `templates/admin/*.html` | `AdminViewController` | KPIs y gráficos SVG, filtros, formularios `th:object` de canchas/clases/usuarios, cambios de estado, exportación CSV |

Componentes compartidos: `templates/fragments/layout.html` (head, header, footer, scripts), `templates/admin/_layout.html` (barra lateral, insignias), `static/css/styles.css` y `static/js/ui.js`.

### 6️⃣ Exposición — guion de demostración sugerido (≈6 min)

1. **Login** (`demo@courthub.com` / `Password123`) → formulario POST, cookie `CH_TOKEN` (JWT) y redirección al dashboard.
2. **Dashboard** → filtrar por deporte, distrito y rango de precio; explicar que el `@Controller` consulta el servicio y Thymeleaf renderiza con `th:each`.
3. **Detalle de cancha** → elegir fecha en el calendario y un bloque libre (mostrar uno ocupado).
4. **Pago** → completar tarjeta (se formatea sola) → confirmar → ver totales calculados (subtotal + 10 %).
5. **Mis reservas** → la reserva aparece en "Próximas"; cancelarla y mostrar que el bloque se libera; reprogramar otra.
6. **Clases** → inscribirse a una clase y ver la barra de cupo actualizarse.
7. **Panel ERP** → cerrar sesión, entrar como `admin@courthub.com`: ver KPIs e ingresos por mes, filtrar reservas y cambiar un estado, crear una cancha y una clase, revisar inscripciones y usuarios, exportar CSV.
8. Cerrar con la **arquitectura**: diagrama MVC (abajo), `docker compose ps` mostrando `db` + `app`, y `GET /api/health`.

<!--RUBRICA-NEXT-->

## 🏗️ Arquitectura MVC

```
                 ┌──────────────────────────── Spring Boot (puerto 8080) ────────────────────────────┐
  Navegador ───▶ │  CONTROLLER                      MODEL                              VIEW          │
  (HTML/forms)   │  @Controller (vistas)  ───▶  @Service (negocio)  ───▶  @Repository  ◀─── Thymeleaf │ ──▶ HTML
                 │  AuthViewController          AuthService              Spring Data JPA   templates/*.html
  JS fetch ────▶ │  CanchaViewController        CanchaService            ┌─────────────┐              │
  (/api/**)      │  ReservaViewController       ReservaService           │ @Entity     │              │ ──▶ JSON
                 │  ClaseViewController         ClaseService             │ Usuario     │              │
                 │  AdminViewController         AdminService             │ Cancha      │              │
                 │  @RestController (API)       JwtService               │ Reserva     │              │
                 │  controller/api/*            ↑                        │ Clase       │              │
                 │                      Spring Security (JWT)            │ Inscripcion │              │
                 └─────────────────────────────────────────────────────  └──────┬──────┘ ─────────────┘
                                                                                ▼
                                                                          PostgreSQL 16
```

- **Model:** entidades JPA en `model/`, acceso a datos en `repository/` (consultas derivadas, JPQL y `Specification`), reglas de negocio en `service/` (transacciones, validaciones, cálculo de importes, control de cupos y solapamientos).
- **View:** plantillas Thymeleaf en `templates/` con fragmentos reutilizables; formularios con `th:object` / `th:field` y mensajes de validación (`BindingResult`); mensajes flash (`RedirectAttributes`) mostrados como toasts.
- **Controller:** `@Controller` en `controller/` devuelven nombres de vista o `redirect:`; los `@RestController` en `controller/api/` exponen la misma lógica como API JSON (usada por el calendario de disponibilidad y disponible para clientes externos).
- **Seguridad:** dos cadenas de filtros (`SecurityConfig`): `/api/**` sin estado con JWT por header y respuestas JSON 401/403; el resto con JWT en cookie HttpOnly, CSRF activo en formularios, redirección a `/login` y `/admin/**` solo para `ROLE_ADMINISTRADOR`.
- **Integridad:** índice único parcial `uq_reserva_bloque (cancha_id, fecha, hora_inicio) WHERE estado <> 'cancelada'` → imposible duplicar un bloque incluso con peticiones simultáneas.
- **Inicialización:** Spring ejecuta `db/schema.sql` al arrancar (`CREATE TABLE IF NOT EXISTS`) y `DataInitializer` carga `db/seed.sql` solo si la tabla `usuarios` está vacía.

## 📁 Estructura del proyecto

```
courthub/
├── backend/                                  # Aplicación Spring Boot MVC (Java 21)
│   ├── pom.xml                               # Web MVC · Thymeleaf · Data JPA · Security · Validation · PostgreSQL · JJWT · Lombok
│   ├── Dockerfile                            # Build multi-etapa: Maven → JRE 21
│   ├── .env.example
│   └── src/main/
│       ├── java/com/courthub/
│       │   ├── CourtHubApplication.java
│       │   ├── model/        Usuario · Cancha · Reserva · Clase · Inscripcion        (M)
│       │   ├── repository/   *Repository (Spring Data JPA)                            (M)
│       │   ├── service/      Auth · Cancha · Reserva · Clase · Admin · Jwt            (M)
│       │   ├── dto/          DTOs de la API + dto/form (objetos de formulario)
│       │   ├── controller/   AuthView · CanchaView · ReservaView · ClaseView · AdminView (C)
│       │   │   └── api/      AuthApi · CanchaApi · ReservaApi · ClaseApi · AdminApi · HealthApi
│       │   ├── security/     SecurityConfig · JwtAuthFilter · TokenCookie · AuthUser
│       │   ├── config/       DataInitializer (seed)
│       │   ├── exception/    ApiException · GlobalExceptionHandler (API)
│       │   └── util/         Horarios (bloques 1.5 h) · Fmt (formato para vistas)
│       └── resources/
│           ├── application.yml
│           ├── db/schema.sql · db/seed.sql
│           ├── templates/                      # VISTAS (V)
│           │   ├── fragments/layout.html       # head · header · footer · scripts
│           │   ├── index.html · dashboard.html · cancha-detalle.html · pago.html · mis-reservas.html · clases.html · error.html
│           │   └── admin/  _layout.html · resumen.html · reservas.html · canchas.html · cancha-form.html
│           │              clases.html · clase-form.html · clase-inscripciones.html · inscripciones.html · usuarios.html · usuario-form.html
│           └── static/css/styles.css · static/js/{icons,ui,api}.js
├── docker-compose.yml                        # db (PostgreSQL) + app (Spring Boot)
└── README.md
```

---

## 🚀 Puesta en marcha (un solo comando)

> Requisito: tener **Docker** (con Compose) instalado. No necesitas Java ni Maven: el `Dockerfile` compila el proyecto en una etapa intermedia.

```bash
docker compose up --build
```

| Servicio   | URL                                  | Descripción                                        |
|------------|--------------------------------------|----------------------------------------------------|
| Web (MVC)  | http://localhost:8080                | Login → Dashboard → Reserva → Pago → Mis reservas  |
| Panel ERP  | http://localhost:8080/admin          | Solo administrador                                 |
| API REST   | http://localhost:8080/api/health     | Endpoints JSON (JWT por header)                    |
| PostgreSQL | localhost:5433 (host)                | Datos persistentes en volumen `pgdata`             |

El primer arranque tarda unos minutos (Maven descarga dependencias). La base de datos se crea y puebla sola la **primera vez**.

### 👤 Cuentas de demostración (contraseña: `Password123`)

| Correo                    | Rol           | Nota                                          |
|---------------------------|---------------|-----------------------------------------------|
| `demo@courthub.com`       | Cliente       | Usuario con historial de reservas             |
| `entrenador@courthub.com` | Entrenador    | Carla Rojas — dicta clases                    |
| `admin@courthub.com`      | Administrador | Entra directo al **panel ERP** (`/admin`)     |

---

## 🛠️ Desarrollo sin Docker

Requisitos: **JDK 21**, **Maven 3.9+** y **PostgreSQL 16** local.

```bash
# 1) Base de datos (el esquema y el seed los carga la propia aplicación al arrancar)
psql -U postgres -c "CREATE USER courthub WITH PASSWORD 'courthub' SUPERUSER;"
psql -U postgres -c "CREATE DATABASE courthub OWNER courthub;"

# 2) Aplicación (variables opcionales: PGHOST, PGPORT, PGUSER, PGPASSWORD, PGDATABASE, JWT_SECRET, PORT)
cd backend
mvn spring-boot:run            # http://localhost:8080
```

> `THYMELEAF_CACHE=false` desactiva la caché de plantillas para editar las vistas sin reiniciar.
> `CREATE EXTENSION pgcrypto` (usado por el seed) requiere que el usuario sea superusuario o dueño de la base de datos.

---

## 🌐 Rutas de las vistas (Spring MVC)

| Método | Ruta                               | Controlador → Vista                              |
|--------|------------------------------------|--------------------------------------------------|
| GET    | `/login` · `/logout`               | `AuthViewController` → `index`                   |
| POST   | `/login` · `/registro`             | Autentica, guarda el JWT en cookie y redirige    |
| GET    | `/dashboard`                       | `CanchaViewController` → `dashboard` 🔒          |
| GET    | `/canchas/{id}`                    | `CanchaViewController` → `cancha-detalle` 🔒     |
| GET    | `/pago?canchaId&fecha&hora`        | `ReservaViewController` → `pago` 🔒              |
| POST   | `/reservas`                        | Crea la reserva → `redirect:/mis-reservas` 🔒    |
| GET    | `/mis-reservas?tab`                | `ReservaViewController` → `mis-reservas` 🔒      |
| POST   | `/reservas/{id}/cancelar` · `/reservas/{id}/reprogramar` | Cambia la reserva y redirige 🔒 |
| GET    | `/clases?deporte`                  | `ClaseViewController` → `clases` 🔒              |
| POST   | `/clases/{id}/inscribirse` · `/clases/{id}/abandonar` | Inscripción y redirige 🔒         |
| GET    | `/admin`                           | `AdminViewController` → `admin/resumen` 🔐       |
| GET/POST | `/admin/reservas`, `/admin/canchas`, `/admin/clases`, `/admin/inscripciones`, `/admin/usuarios` (+ `/nueva`, `/{id}/editar`, `/{id}/estado`, `/exportar`) | Mantenimientos del ERP 🔐 |

## 🔌 API REST (JSON)

| Método | Ruta                                       | Descripción                                   |
|--------|--------------------------------------------|-----------------------------------------------|
| GET    | `/api/health`                              | Estado de la aplicación                       |
| POST   | `/api/auth/register` · `/api/auth/login`   | Registro / login → devuelve **JWT**           |
| GET    | `/api/auth/me`                             | Perfil del usuario autenticado 🔒             |
| GET    | `/api/canchas?tipo=&q=&comuna=&precioMin=&precioMax=` | Listado de canchas con filtros 🔒  |
| GET    | `/api/canchas/{id}` · `/api/canchas/{id}/disponibilidad?fecha=` | Detalle y bloques libres 🔒 |
| GET/POST | `/api/reservas`                          | Mis reservas / crear reserva 🔒               |
| PATCH  | `/api/reservas/{id}/cancelar` · `/api/reservas/{id}/reprogramar` | Cancelar / reprogramar 🔒 |
| GET    | `/api/clases?deporte=`                     | Clases con cupos 🔒                           |
| POST/DELETE | `/api/clases/{id}/inscribirse`        | Apuntarse / anular inscripción 🔒             |
| GET    | `/api/admin/dashboard`                     | KPIs y reportes 🔐                            |
| CRUD   | `/api/admin/usuarios` · `/api/admin/canchas` · `/api/admin/reservas` · `/api/admin/clases` · `/api/admin/inscripciones` | Mantenimientos 🔐 |

🔒 = requiere sesión (cookie en las vistas · `Authorization: Bearer <token>` en la API) · 🔐 = además exige rol administrador

---

## 🗄️ Consultas SQL de evidencia

El archivo [`backend/src/main/resources/db/consultas-evidencia.sql`](backend/src/main/resources/db/consultas-evidencia.sql) contiene 16 consultas comentadas para demostrar la base de datos en la exposición: conteo de tablas, usuarios por rol (hash BCrypt), filtros del dashboard, historial del cliente, disponibilidad de bloques, pruebas de integridad (índice único parcial y UNIQUE de inscripciones, ejecutadas dentro de una transacción con `ROLLBACK`), ingresos por mes/cancha/deporte/método de pago, ocupación del día, clases con cupos, top de clientes y las restricciones e índices del esquema.

```bash
# Ejecutar todas las consultas con los contenedores levantados
docker compose exec -T db psql -U courthub -d courthub < backend/src/main/resources/db/consultas-evidencia.sql

# O abrir una consola interactiva (también desde pgAdmin/DBeaver en localhost:5433, usuario/clave courthub)
docker compose exec db psql -U courthub -d courthub
```

## 🧩 Módulos implementados

1. **Autenticación y roles** — registro/login con BCrypt + JWT (Spring Security); roles `cliente`, `entrenador`, `administrador`.
2. **Gestión de canchas** — listado con búsqueda, filtro por deporte/precio/distrito, detalle con servicios y reglamento, y bloques horarios de 1.5 h con calendario mensual.
3. **Reservas** — flujo completo: seleccionar cancha → fecha/bloque → pago (tarjeta/PayPal/Webpay) → confirmación; historial con pestañas, cancelar y reprogramar con validación de solapamiento en BD.
4. **Clases deportivas** — clases dictadas por entrenadores con cupos, inscripción/desinscripción y control de duplicados.
5. **Panel ERP (administrador)** — indicadores, gráficos de ingresos por mes y por cancha/deporte, gestión de estados de reservas e inscripciones, CRUD de usuarios, canchas y clases, exportación a CSV.

## 📝 Notas para la exposición

- Las vistas replican el prototipo de Figma (paleta verde `#10b981`, tipografías Poppins/Inter, mismas pantallas y textos).
- Para reiniciar la BD desde cero: `docker compose down -v && docker compose up --build`.

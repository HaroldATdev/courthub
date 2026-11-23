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
| Despliegue    | Docker + Docker Compose (local) · **Render** (app + PostgreSQL en producción) · **Vercel** (portada) |

> 🌐 **Aplicación en producción:** <https://courthub-1fwc.onrender.com> (Render, plan gratuito: la primera visita tras 15 min de inactividad tarda ~30 s en despertar).
> Usuarios: `admin` (administrador) · `gdiaz` (cliente). **Las claves de producción no están en este repositorio** (se definen con `ADMIN_PASSWORD` y `DEMO_PASSWORD` en Render y se entregan por un canal privado). El botón **Continuar con Google** está activo en producción.

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

1. **Login** (usuario `gdiaz` / `Password123`) → formulario POST validado contra la BD, cookie `CH_TOKEN` (JWT) y redirección por perfil (cliente → dashboard, administrador → panel ERP). Mostrar el enlace **Regístrate aquí** y las validaciones de DNI/usuario repetidos.
2. **Dashboard** → filtrar por deporte, distrito y rango de precio; explicar que el `@Controller` consulta el servicio y Thymeleaf renderiza con `th:each`.
3. **Detalle de cancha** → elegir fecha en el calendario y un bloque libre (mostrar uno ocupado).
4. **Pago** → completar tarjeta (se formatea sola) → confirmar → ver totales calculados (subtotal + 10 %).
5. **Mis reservas** → la reserva aparece en "Próximas"; cancelarla y mostrar que el bloque se libera; reprogramar otra.
6. **Clases** → inscribirse a una clase y ver la barra de cupo actualizarse.
7. **Panel ERP** → cerrar sesión, entrar como `admin`: activar la cuenta recién registrada en *Usuarios*, ver KPIs e ingresos por mes, filtrar reservas y cambiar un estado, crear una cancha y una clase eligiendo deporte y nivel desde las listas cargadas de la BD, exportar CSV.
8. **Google** → desde el login pulsar *Continuar con Google* con un Gmail nuevo: la cuenta se crea activa y entra directo al dashboard.
9. Cerrar con la **arquitectura** (diagrama MVC, abajo) y el **despliegue**: la app en Render (`courthub-1fwc.onrender.com`), la portada en Vercel y `docker compose ps` para el entorno local.

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
│       │   ├── model/        Usuario · Cancha · Reserva · Clase · Inscripcion · Deporte · NivelClase (M)
│       │   ├── repository/   *Repository (Spring Data JPA)                            (M)
│       │   ├── service/      Auth · Cancha · Reserva · Clase · Admin · Catalogo · Jwt (M)
│       │   ├── dto/          DTOs de la API + dto/form (objetos de formulario)
│       │   ├── controller/   AuthView · CanchaView · ReservaView · ClaseView · AdminView (C)
│       │   │   └── api/      AuthApi · CanchaApi · ReservaApi · ClaseApi · AdminApi · HealthApi
│       │   ├── security/     SecurityConfig · JwtAuthFilter · TokenCookie · AuthUser
│       │   ├── config/       DataInitializer (seed)
│       │   ├── exception/    ApiException · GlobalExceptionHandler (API)
│       │   └── util/         Horarios (bloques 1.5 h) · Fmt (formato para vistas)
│       └── resources/
│           ├── application.yml
│           ├── db/schema.sql · db/seed.sql · db/consultas-evidencia.sql
│           ├── templates/                      # VISTAS (V)
│           │   ├── fragments/layout.html       # head · header · footer · scripts
│           │   ├── index.html · dashboard.html · cancha-detalle.html · pago.html · mis-reservas.html · clases.html · error.html
│           │   └── admin/  _layout.html · resumen.html · reservas.html · canchas.html · cancha-form.html
│           │              clases.html · clase-form.html · clase-inscripciones.html · inscripciones.html · usuarios.html · usuario-form.html
│           └── static/css/styles.css · static/js/{icons,ui,api}.js
├── landing/index.html                        # Portada estática publicada en Vercel
├── render.yaml                               # Despliegue de app + BD en Render
├── vercel.json                               # Configuración del sitio estático en Vercel
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

> Si el puerto 8080 está ocupado en tu máquina: `APP_PORT=8090 docker compose up --build` (PowerShell: `$env:APP_PORT=8090; docker compose up --build`) y abre http://localhost:8090.

### 👤 Cuentas de demostración (entorno local: contraseña `Password123`)

| Usuario   | Correo                    | Perfil        | Estado | Nota                                          |
|-----------|---------------------------|---------------|--------|-----------------------------------------------|
| `gdiaz`   | `demo@courthub.com`       | Cliente       | activo | Usuario con historial de reservas             |
| `crojas`  | `entrenador@courthub.com` | Entrenador    | activo | Carla Rojas — dicta clases                    |
| `admin`   | `admin@courthub.com`      | Administrador | activo | Entra directo al **panel ERP** (`/admin`)     |

> El login acepta el **nombre de usuario o el correo**. Las cuentas de **cliente** (registro normal o Google) nacen en estado **activo** y entran de inmediato. Quien se registra con el perfil **administrador** queda **pendiente** hasta que otro administrador lo active en *Administración → Usuarios* (si no, cualquiera podría darse acceso total al panel).
>
> **Pagos:** cada reserva cobrada deja un registro en la tabla `pagos` (número de operación, método, últimos 4 dígitos de la tarjeta, subtotal, cargo, monto, estado `aprobado`/`reembolsado`, proveedor). Al cancelar la reserva el pago pasa a `reembolsado`. El proveedor es `simulado` sin llaves, o `culqi` con las llaves de prueba (ver *Pagos en sandbox*). Las inscripciones a clases de pago también se cobran y generan boleta.
>
> **Cierre del flujo de venta (entrega final):**
> - **Boleta en PDF:** cada reserva cobrada tiene su boleta (`GET /reservas/{id}/boleta`, serie `B001`, número correlativo, datos del cliente, detalle, op. gravada, IGV 18 % y total). La descarga el dueño de la reserva desde *Mis reservas → Boleta PDF* o el administrador desde *Reservas*. Es un comprobante de demostración: no se envía a SUNAT. Los datos del emisor se configuran con `courthub.emisor.razon-social`, `courthub.emisor.ruc` y `courthub.emisor.direccion`.
> - **Estado de la reserva:** una reserva `confirmada` pasa sola a `completada` cuando termina su bloque (tarea programada cada 5 minutos, hora de Lima); el administrador también puede completarla a mano. Si el admin confirma una reserva que estaba pendiente se registra su cobro.
> - **Reporte de ventas (`/admin/ventas`):** ventas por mes y por año con filtro de año y mes, indicadores (total, operaciones, ticket promedio, reembolsado), gráfico, detalle con enlace a cada boleta, exportación a CSV e impresión a PDF. Solo suman los pagos `aprobado`; los reembolsados se informan aparte.
> - **Navegación:** todas las páginas tienen opción de retorno (*Volver a Canchas*, *Volver al resumen* en cada sección del panel, *Volver a iniciar sesión*, botón *Volver* en la página de error).

> 🔐 **Claves en producción:** `Password123` solo vale para el entorno local. En el despliegue público la aplicación lee las variables `ADMIN_PASSWORD` (cuenta `admin`) y `DEMO_PASSWORD` (cuentas `gdiaz`, `crojas`, `pmunoz`, `asoto`, `lperez`) y sobrescribe las claves del seed en cada arranque (`DataInitializer.aplicarClavesDeEntorno`). Así la clave real nunca se publica en GitHub.

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
| POST   | `/login` · `/registro`             | Autentica, guarda el JWT en cookie y redirige (con 2FA activo, pide antes el código) |
| POST   | `/login/verificar` · `/login/reenviar` | Segundo paso: valida / reenvía el código del correo → `verificar-2fa` |
| GET    | `/dashboard`                       | `CanchaViewController` → `dashboard` 🔒          |
| GET    | `/canchas/{id}`                    | `CanchaViewController` → `cancha-detalle` 🔒     |
| GET    | `/pago?canchaId&fecha&hora`        | `ReservaViewController` → `pago` 🔒              |
| POST   | `/reservas`                        | Crea la reserva → `redirect:/mis-reservas` 🔒    |
| GET    | `/mis-reservas?tab`                | `ReservaViewController` → `mis-reservas` 🔒      |
| POST   | `/reservas/{id}/cancelar` · `/reservas/{id}/reprogramar` | Cambia la reserva y redirige 🔒 |
| GET    | `/clases?deporte`                  | `ClaseViewController` → `clases` 🔒              |
| GET    | `/clases/{id}/pago`                | `ClaseViewController` → `pago-clase` (clases de pago) 🔒 |
| POST   | `/clases/{id}/inscribirse` · `/clases/{id}/abandonar` | Inscripción (con cobro si tiene precio) y anulación con reembolso 🔒 |
| GET    | `/reservas/{id}/boleta` · `/clases/{id}/boleta` · `/boletas/{pagoId}` | Boleta de venta en PDF 🔒 |
| GET    | `/entrenador` · `/entrenador/clases/{id}` | `EntrenadorViewController` → `entrenador/*` (solo perfil entrenador) |
| POST   | `/entrenador/clases/{id}/inscripciones/{iid}/asistencia` | Marca o desmarca la asistencia de un alumno |
| GET    | `/admin/ventas` · `/admin/ventas/exportar` | Reporte de ventas por mes y año (+ CSV) 🔐 |
| GET    | `/admin`                           | `AdminViewController` → `admin/resumen` 🔐       |
| GET/POST | `/admin/reservas`, `/admin/canchas`, `/admin/clases`, `/admin/inscripciones`, `/admin/usuarios` (+ `/nueva`, `/{id}/editar`, `/{id}/estado`, `/exportar`) | Mantenimientos del ERP 🔐 |

## 🔌 API REST (JSON)

| Método | Ruta                                       | Descripción                                   |
|--------|--------------------------------------------|-----------------------------------------------|
| GET    | `/api/health`                              | Estado de la aplicación                       |
| POST   | `/api/auth/register`                       | Registro (dni, nombre, apellido, usuario, email, password, rol) → cliente **activo**; administrador **pendiente** |
| POST   | `/api/auth/login`                          | Login por `usuario` (nombre de usuario o correo) + `password` → **JWT** (solo cuentas activas). Con 2FA activo devuelve `{requiere2fa, desafio}` |
| POST   | `/api/auth/2fa`                            | `desafio` + `codigo` recibido por correo → **JWT** |
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

## 🎓 Tercer avance — acceso, registro, perfiles y catálogos

| # | Requisito | Implementación | Dónde |
|---|-----------|----------------|-------|
| 1 | **Página de acceso** que valida usuario y clave con la BD | Formulario `POST /login`; `AuthService.login` busca por **usuario o correo** (`UsuarioRepository.findByUsuarioOEmail`), compara el hash BCrypt y exige estado `activo` | `templates/index.html`, `AuthViewController`, `AuthService` |
| 2 | **Enlace de registro** al pie del login con *dni, nombres, apellidos, usuario, clave, perfil, estado* | El enlace "¿No tienes una cuenta? Regístrate aquí" abre el formulario `POST /registro`. Perfil: `cliente` o `administrador`. Estado por defecto **activo** para clientes (campo de solo lectura); el perfil administrador queda **pendiente** hasta que otro administrador lo active; también existe `anulado` | `templates/index.html`, `Formularios.RegistroForm`, `AuthService.registrar` |
| 2a | Validación por **DNI** repetido | `usuarios.existsByDni` → "El DNI … ya está registrado" + índice único parcial `uq_usuarios_dni` en la BD | `AuthService`, `db/schema.sql` |
| 2b | Validación por **usuario** repetido | `usuarios.existsByUsuarioIgnoreCase` → "El nombre de usuario … ya está en uso" + índice único `uq_usuarios_usuario (LOWER(usuario))` | `AuthService`, `db/schema.sql` |
| 3 | **Redirección según perfil** al iniciar sesión | `administrador` → `/admin` (panel ERP) · `entrenador` → `/entrenador` · `cliente` → `/dashboard`. Las cuentas `pendiente` o `anulado` reciben un mensaje y no entran | `AuthViewController.iniciarSesion` |
| 4 | **CRUD de usuarios** en el perfil administrador | Listado con filtros por perfil, estado y búsqueda (DNI, usuario, nombre, correo); crear, editar (formulario `th:object`), cambiar estado (**Activar / Pendiente / Anular**) y exportar CSV, con las mismas validaciones de DNI/usuario únicos | `/admin/usuarios`, `AdminViewController`, `AdminService` |
| 5 | **Listas desplegables desde tablas de la BD** en los dos CRUD del 2.º avance | Tabla `deportes` → select *Deporte* en **canchas** y **clases**; tabla `niveles_clase` → select *Nivel* en **clases**. Se cargan en `GlobalModelAttributes` vía `CatalogoService` y el servicio rechaza valores que no existan en el catálogo | `db/schema.sql`, `model/Deporte`, `model/NivelClase`, `admin/cancha-form.html`, `admin/clase-form.html` |

**Modelo de datos del avance:** `usuarios` incorpora `dni` (único), `usuario` (único, sin distinguir mayúsculas), `telefono` (celular peruano normalizado a `+51 9XX XXX XXX`) y `estado` (`activo` · `pendiente` · `anulado`, con `CHECK`). La tabla `claves_reset` guarda los enlaces de recuperación de contraseña (solo el hash del token). Las bases creadas antes se migran solas al arrancar (`DataInitializer.migrarTercerAvance`). Las consultas 17 a 22 de [`consultas-evidencia.sql`](backend/src/main/resources/db/consultas-evidencia.sql) sirven como evidencia.

**Mi perfil (extra):** desde el menú del usuario (avatar → *Mi perfil*, ruta `/perfil`) cada cuenta completa o corrige su DNI, nombres, apellidos, usuario, teléfono, nivel y deporte favorito; útil para las cuentas creadas con Google, que llegan solo con nombre y correo. Aplica las mismas validaciones de DNI y usuario únicos. El correo es de solo lectura y la contraseña no se edita ahí (previsto: verificación en dos pasos por correo en la siguiente entrega). También disponible como `PUT /api/auth/me`.

**Recuperar contraseña:** el enlace *¿Olvidaste tu contraseña?* del login lleva a `/olvide-clave`. Se pide el usuario o correo y, si la cuenta existe y está activa, se envía un enlace `/restablecer?token=…` (la respuesta es la misma exista o no la cuenta, para no revelar qué correos están registrados). El token es aleatorio de 256 bits, **en la BD solo se guarda su hash SHA-256** (tabla `claves_reset`), vale **30 minutos** y es de **un solo uso**; pedir uno nuevo invalida los anteriores. El correo sale por SMTP si se definen `MAIL_HOST`, `MAIL_USERNAME` y `MAIL_PASSWORD` (con Gmail: `smtp.gmail.com` y una *contraseña de aplicación*); sin esas variables el enlace se escribe en el log del servidor (modo desarrollo).

**Sesión de cuentas anuladas:** el filtro JWT consulta la BD en cada petición y solo acepta cuentas en estado `activo`. Al anular a un usuario su sesión se cierra de inmediato (antes seguía válida hasta que caducara el token de 8 h), y los cambios de perfil o de rol se aplican al instante.

**Capturas de pantalla:** las 16 capturas del flujo, cada una con su explicación para el informe, están en [`docs/capturas/`](docs/capturas/README.md).

**Inicio de sesión con Google (extra):** el botón *Continuar con Google* del login usa OAuth2/OpenID Connect de Spring Security (`GoogleOAuthConfig`, `GoogleLoginHandler`, `AuthService.loginConGoogle`). Google confirma la identidad; si el correo no existe, la cuenta se crea como cliente en estado *pendiente* (misma regla que el registro) y, si está activa, entra con la cookie JWT y la redirección por perfil. **Ya está configurado y probado en producción** (`courthub-1fwc.onrender.com`): al entrar con un Gmail nuevo la cuenta queda pendiente, el administrador la activa en *Usuarios* y el siguiente acceso con Google entra directo.

Cómo se configuró (para reproducirlo en otro entorno):

1. En <https://console.cloud.google.com/apis/credentials> se creó un *ID de cliente OAuth 2.0* de tipo **Aplicación web**, con pantalla de consentimiento en estado *Testing* (solo entran los Gmail agregados como usuarios de prueba).
2. Orígenes autorizados: `http://localhost:8080` y `https://courthub-1fwc.onrender.com`. URIs de redirección: `http://localhost:8080/login/oauth2/code/google` y `https://courthub-1fwc.onrender.com/login/oauth2/code/google`.
3. `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` se guardan como variables de entorno del servicio en Render (y, en local, en un archivo `.env` junto a `docker-compose.yml`, excluido de Git). Sin esas variables el botón muestra un aviso y todo lo demás funciona igual. `server.forward-headers-strategy=framework` hace que, detrás del proxy HTTPS de Render, el URI de retorno se construya con la URL pública.

**Flujo para la demo:** registrarse desde el login (probar un DNI y un usuario repetidos) → entrar con la cuenta nueva (queda activa) → registrar otra con perfil Administrador (queda pendiente: mensaje de activación) → entrar como `admin` → *Usuarios → filtro Pendiente → Activar* → crear una cancha y una clase eligiendo deporte y nivel desde las listas cargadas de la BD.

## ☁️ Despliegue en la nube (Render + Vercel)

**Vercel no puede ejecutar la aplicación** (no soporta Java/Spring Boot ni PostgreSQL), así que el despliegue se divide en dos:

| Qué | Dónde | Archivo |
|-----|-------|---------|
| Aplicación Spring Boot + base de datos PostgreSQL | **Render** (plan gratuito, despliega el `Dockerfile`) | [`render.yaml`](render.yaml) |
| Portada/landing del proyecto (estática) con enlaces a la app y al repositorio | **Vercel** | [`vercel.json`](vercel.json) + [`landing/index.html`](landing/index.html) |

**Estado actual:** la aplicación está desplegada en Render en <https://courthub-1fwc.onrender.com> (servicio web `courthub` + base de datos `courthub-db`, creados desde el Blueprint, con el seed demo cargado y el login con Google activo) y la portada está publicada en Vercel desde la carpeta `landing/`. Cada `git push` a `main` redespliega ambos automáticamente.

Cómo se hizo (para reproducirlo):

1. **Render:** <https://dashboard.render.com> → *New → Blueprint* → seleccionar el repositorio. Render lee `render.yaml`, crea la base de datos `courthub-db` y el servicio web `courthub`, inyecta las variables `PG*` y genera `JWT_SECRET`. El primer build tarda unos minutos (compila con Maven). Como el nombre `courthub.onrender.com` ya estaba ocupado, Render asignó `courthub-1fwc`. Las variables de Google se completaron después en *Environment*.
2. **Vercel:** importar el mismo repositorio en <https://vercel.com/new>; `vercel.json` indica que el sitio es estático y que la carpeta publicada es `landing/` (equivale a *Root Directory* = `landing` y *Framework Preset* = *Other*).
3. La constante `APP_URL` al final de `landing/index.html` apunta a la URL de Render para que los botones "Abrir la aplicación" lleven al sistema.

> El plan gratuito de Render "duerme" el servicio tras 15 minutos sin tráfico: la primera visita puede tardar unos 30 segundos en despertar.

## 💳 Pagos en sandbox (Culqi)

El cobro pasa por una **pasarela** intercambiable (`pasarela/PasarelaPago`), elegida por configuración:

| Modo | Cuándo | Qué hace |
|------|--------|----------|
| **Simulada** (por defecto) | Sin `CULQI_SECRET_KEY` | No se envía nada a ningún banco. Valida la tarjeta y aprueba, salvo las **tarjetas de prueba de rechazo** (p. ej. `4000 0400 0000 0008`, fondos insuficientes), que se rechazan con su motivo. |
| **Culqi sandbox** | Con `CULQI_PUBLIC_KEY` (`pk_test_…`) y `CULQI_SECRET_KEY` (`sk_test_…`) | La tarjeta se escribe en la ventana de **Culqi Checkout** y el navegador recibe un *token*; el servidor solo recibe ese token y crea el cargo (`POST /v2/charges`) con la llave privada. **El número de tarjeta nunca pasa por CourtHub.** El monto se recalcula en el servidor (en céntimos). |

- Alcance: reservas de cancha (subtotal + 10 % de cargo) e inscripciones a clases (precio de la clase). Cada pago aprobado queda en `pagos` con proveedor, referencia del cargo (`chr_test_…`) y los últimos 4 dígitos. Si la pasarela **rechaza** el pago, no queda ni la reserva/inscripción ni el pago (misma transacción) y se muestra el motivo.
- Llaves: se crean en el panel de integración de Culqi (**Desarrollo → API Keys**) y se guardan **solo** en variables de entorno (Render → Environment). Nunca en el repositorio.
- Tarjetas de prueba de Culqi: aprobada `4111 1111 1111 1111` (09/30, CVV 123); rechazadas, p. ej. `4000 0400 0000 0008` (fondos insuficientes) y `4000 0200 0000 0000` (robada).
- **Verificación realizada:** la integración se probó contra un servidor que imita las respuestas documentadas de Culqi (cargo aprobado, cargo rechazado, token ausente, monto en céntimos, llave privada fuera de la página). **No se ha probado contra el sandbox real de Culqi**: falta hacerlo con llaves de prueba propias; el formato exacto de la respuesta de error del API real no se pudo confirmar en su documentación.

## ✉️ Correo y verificación en dos pasos (2FA)

**Verificación en dos pasos:** tras validar usuario y clave, se envía un **código de 6 dígitos al correo** de la cuenta y solo con él se emite la sesión (pantalla `/login/verificar`; en la API: `POST /api/auth/login` devuelve `{requiere2fa, desafio}` y `POST /api/auth/2fa` entrega el JWT). El código y el desafío se guardan solo como hash (`verificaciones_2fa`), vencen a los 10 minutos, son de un solo uso y admiten 5 intentos; se puede reenviar un código nuevo cada 60 segundos. Con esto el correo queda comprobado en cada acceso y los clientes ya nacen activos sin aprobación del administrador. El acceso con Google no pide código (Google ya autentica).

**Se activa sola solo si hay proveedor de correo** (`BREVO_API_KEY` o `MAIL_HOST`) y `TWOFA_ENABLED` no es `false`: sin correo no habría forma de entregar el código y nadie podría entrar. Si el proveedor falla, el login muestra un aviso y no entra. **Salida de emergencia:** poner `TWOFA_ENABLED=false` en las variables de entorno y redesplegar.

**Proveedores de correo** (en este orden de prioridad):

1. **API HTTPS de Brevo** (`BREVO_API_KEY`, `MAIL_FROM` = remitente verificado en Brevo; plan gratuito de 300 correos/día). Usa el puerto 443, que nunca se bloquea. **Es la opción recomendada en Render.**
2. **SMTP** (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`; con Gmail, `smtp.gmail.com` y una *contraseña de aplicación*). Render puede **bloquear los puertos SMTP (25, 465 y 587) en el plan gratuito**: si el correo no sale aunque la configuración sea correcta, hay que pasar a Brevo.
3. Sin proveedor: el mensaje (p. ej. el enlace de recuperación) solo se escribe en el log del servidor y el 2FA queda apagado.

## 🧑‍🏫 Ambiente del entrenador

Los usuarios con perfil `entrenador` entran a `/entrenador` (solo ese perfil; el administrador y los clientes reciben *acceso denegado*): resumen con sus clases próximas y anteriores, alumnos inscritos, asistencias y lo cobrado por sus clases; lista de alumnos de cada clase (correo, teléfono, pago) y **control de asistencia** (marcar/desmarcar). Solo ve las clases que le asignó el administrador; la clase de otro entrenador responde 404. Quien ya asistió sigue contando como inscrito (no puede pagar dos veces).

## 🗄️ Base de datos en Supabase y RLS

La base de datos de producción está en **Supabase** (PostgreSQL); la aplicación solo cambió las variables `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD` y `PGDATABASE` (con el *Session pooler*, porque Render usa IPv4). `schema.sql` activa **RLS (Row Level Security)** en todas las tablas: la aplicación se conecta como propietario y no se ve afectada, pero las claves públicas de la API REST de Supabase (`anon`/`authenticated`) quedan sin acceso a ninguna tabla.

## 🧭 Hoja de ruta (lo que sigue)

| Pendiente | Detalle |
|-----------|---------|
| **Probar Culqi contra su sandbox real** | Crear cuenta de integración, cargar `CULQI_PUBLIC_KEY`/`CULQI_SECRET_KEY` en Render y hacer una reserva con la tarjeta de prueba. |
| **Reembolso real** | Hoy cancelar marca el pago como `reembolsado` en la base, pero no devuelve el dinero en Culqi (faltaría `POST /refunds`). |
| **Billeteras y 3-D Secure** | Yape/Plin y autenticación 3DS de Culqi. |
| **Más correos** | Confirmación de reserva y recordatorios (el envío ya existe). |
| **Entrenador crea sus clases** | Hoy el administrador las crea y las asigna. |

## ⚠️ Limitaciones conocidas (estado actual)

Funciona de punta a punta: acceso (usuario, correo y Google) con verificación en dos pasos cuando hay correo, registro con validaciones de DNI y usuario, recuperación de contraseña, Mi perfil, CRUD de usuarios, canchas, clases e inscripciones, reservas con bloqueo de bloque, cobro de reservas y clases (pasarela simulada o Culqi sandbox), boleta en PDF, reporte de ventas por mes y año, ambiente del entrenador y panel ERP. Lo siguiente está incompleto:

| Flujo | Estado actual |
|-------|---------------|
| **Dinero real** | No se mueve dinero: la pasarela simulada no cobra y Culqi se usa solo con llaves de **prueba**. Los métodos PayPal y Webpay (modo simulado) son solo etiquetas. |
| **Reembolsos y cancelación** | El pago pasa a `reembolsado` en la base, pero no hay devolución en la pasarela, no existen los "créditos CourtHub" que menciona el mensaje y no se aplica la regla de las 24 horas. |
| **Boleta** | Comprobante de demostración (serie `B001`, RUC ficticio): no se envía a SUNAT y no existe factura. |
| **Horarios por cancha** | La tabla `disponibilidad_canchas` existe pero no se usa: todas las canchas tienen los mismos bloques (08:00 a 23:00). |
| **Elementos sin función** | Botón Apple, campana de notificaciones, "Recordarme", interruptor "Disponible Hoy", Soporte y términos y condiciones. |
| **Pruebas automatizadas** | No hay tests en el repositorio; la verificación se hizo con scripts contra la aplicación en ejecución. |
| **Límite de intentos de login** | No hay bloqueo por intentos fallidos de contraseña (el código del 2FA sí limita a 5 intentos). |

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

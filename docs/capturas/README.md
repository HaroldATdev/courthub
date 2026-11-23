# Capturas de pantalla — Tercer avance

Capturas generadas sobre la aplicación en ejecución con la base de datos demo limpia (1280 × 860 px).
Cada una incluye una breve explicación para pegar en el informe.

| # | Archivo | Qué muestra (explicación para el informe) |
|---|---------|-------------------------------------------|
| 1 | `01-login-pagina-de-acceso.png` | **Página de acceso.** Formulario de login que solicita usuario (o correo) y clave y los valida contra la tabla `usuarios` de PostgreSQL (clave con hash BCrypt). Al pie, el enlace *"¿No tienes una cuenta? Regístrate aquí"*. |
| 2 | `02-registro-formulario-vacio.png` | **Formulario de registro** abierto desde el enlace del login. Campos: DNI, usuario, nombres, apellidos, correo, clave, confirmación, **perfil** (cliente / administrador) y **estado** fijo en *Pendiente*. |
| 3 | `03-registro-formulario-lleno.png` | Registro completado con los datos de un usuario nuevo antes de enviarlo. |
| 4 | `04-registro-validacion-dni-repetido.png` | **Validación a) por DNI:** se intenta registrar con el DNI `10000002` (ya pertenece a otro usuario) y el sistema lo rechaza con el mensaje *"El DNI 10000002 ya está registrado"*. La restricción también existe en la BD (índice único `uq_usuarios_dni`). |
| 5 | `05-registro-validacion-usuario-repetido.png` | **Validación b) por usuario:** se intenta usar el nombre de usuario `gdiaz` (ya en uso) y el sistema lo rechaza con *"El nombre de usuario … ya está en uso"* (índice único `uq_usuarios_usuario`, sin distinguir mayúsculas). |
| 6 | `06-registro-exitoso-cuenta-pendiente.png` | **Registro exitoso.** La aplicación vuelve al login e informa que la cuenta quedó en estado **pendiente** y que el administrador debe activarla. |
| 7 | `07-login-cuenta-pendiente-bloqueada.png` | La cuenta recién creada intenta iniciar sesión: la clave es correcta pero el estado es *pendiente*, por lo que el acceso se bloquea con el mensaje correspondiente. |
| 8 | `08-login-admin-redirige-panel-erp.png` | **Redirección por perfil (administrador).** Al ingresar con el usuario `admin` el sistema redirige al **panel ERP** (`/admin`) con los indicadores de la operación, incluido el número de cuentas pendientes. |
| 9 | `09-admin-usuarios-pendientes.png` | **CRUD de usuarios — filtro por estado.** Listado de cuentas pendientes de activación con los botones *Editar*, *Activar* y *Anular*. |
| 10 | `10-admin-usuario-activado.png` | El administrador **activa** la cuenta: el estado cambia a *Activo* y se muestra la confirmación. Solo el administrador puede realizar esta acción. |
| 11 | `11-admin-usuarios-listado-crud.png` | **CRUD de usuarios — listado completo** con DNI, nombre de usuario, perfil, estado, filtros por perfil/estado, búsqueda, botón *Nuevo usuario* y exportación a CSV. |
| 12 | `12-admin-usuario-editar-formulario.png` | **CRUD de usuarios — edición.** Formulario con DNI, usuario, nombres, apellidos, correo, clave (opcional), perfil, estado, teléfono, nivel y deporte favorito. Aplica las mismas validaciones de DNI y usuario únicos. |
| 13 | `13-admin-usuario-nuevo-estados.png` | **CRUD de usuarios — creación.** Lista de estados disponibles: *Pendiente (por defecto)*, *Activo* y *Anulado*. |
| 14 | `14-admin-cancha-select-deporte-desde-bd.png` | **Lista desplegable desde la BD (CRUD de canchas).** El campo *Deporte* se carga desde la tabla `deportes` (Fútbol, Tenis, Squash, Pádel); el servidor rechaza cualquier valor que no exista en esa tabla. |
| 15 | `15-admin-clase-selects-deporte-y-nivel-desde-bd.png` | **Listas desplegables desde la BD (CRUD de clases).** *Deporte* desde la tabla `deportes` y *Nivel* desde la tabla `niveles_clase`; el select de entrenador también sale de `usuarios` (perfil entrenador, estado activo). |
| 16 | `16-login-cliente-activado-redirige-dashboard.png` | **Redirección por perfil (cliente).** La cuenta ya activada inicia sesión y el sistema la lleva al **dashboard de canchas**, el ambiente del cliente. |

## Cómo regenerarlas

Con la aplicación levantada (`docker compose up`) se pueden volver a generar automáticamente con el script
`capturas.js` (Node + puppeteer-core, usa el Chrome instalado):

```bash
# base de datos limpia para que los datos demo salgan ordenados
docker compose down -v && docker compose up -d
OUT=docs/capturas/tercer-avance BASE=http://localhost:8080 node capturas.js
```


---

# Capturas — Entrega final (cierre del flujo de venta)

Generadas el 23/11/2026 recorriendo la aplicación en **Brave** de forma automática (script con `puppeteer-core`) sobre la base de datos demo limpia, con la pasarela de pago **simulada** y datos de prueba. Carpeta: `docs/capturas/entrega-final/`. Cada una incluye su explicación para el informe.

| # | Archivo | Qué muestra |
|---|---------|-------------|
| 1 | `01-login.png` | Página de acceso con el enlace de registro. |
| 2 | `02-registro-dni-repetido.png` | Validación por DNI repetido: el sistema rechaza el registro. |
| 3 | `03-registro-usuario-repetido.png` | Validación por nombre de usuario repetido. |
| 4 | `04-registro-exitoso-activa.png` | Registro exitoso: **los clientes nacen en estado activo** y pueden iniciar sesión de inmediato (solo el perfil administrador queda pendiente). |
| 5 | `05-cliente-dashboard.png` | Dashboard del cliente con el listado de canchas. |
| 6 | `06-cliente-detalle-cancha.png` | Detalle de la cancha con calendario y bloques horarios disponibles. |
| 7 | `07-cliente-pago-rechazado.png` | **Cobro:** una tarjeta de prueba de rechazo (fondos insuficientes) es rechazada y no se crea la reserva. |
| 8 | `08-cliente-reserva-confirmada.png` | Pago aprobado: la reserva queda confirmada en *Mis reservas*, con botón *Boleta PDF*. |
| 9 | `09-cliente-boleta-reserva-pdf.png` | **Boleta de venta en PDF** (serie B001, desglose de IGV 18 %, número de operación y medio de pago). Comprobante de demostración, sin validez ante SUNAT. |
| 10 | `10-cliente-clases.png` | Clases deportivas con precio y cupos. |
| 11 | `11-cliente-pago-clase.png` | Pantalla de pago de la inscripción a una clase. |
| 12 | `12-cliente-clase-inscrito.png` | Inscripción pagada: la clase muestra *Inscrito* y su boleta. |
| 13 | `13-cliente-boleta-clase-pdf.png` | Boleta en PDF de la inscripción a la clase. |
| 14 | `14-entrenador-panel.png` | **Ambiente del entrenador:** sus clases, alumnos, asistencias y lo cobrado. |
| 15 | `15-entrenador-alumnos.png` | Lista de alumnos de una clase (correo, teléfono, pago). |
| 16 | `16-entrenador-asistencia-marcada.png` | Control de asistencia: el entrenador marca a un alumno como *Asistió*. |
| 17 | `17-admin-resumen.png` | Panel ERP del administrador con indicadores y gráficos. |
| 18 | `18-admin-reservas.png` | Gestión de reservas con estados, boleta y acciones. |
| 19 | `19-admin-ventas-reporte.png` | **Reporte de ventas por mes y por año** con indicadores, gráfico, detalle y enlaces a cada boleta; exporta a CSV y se imprime a PDF. |
| 20 | `20-admin-usuarios.png` | Mantenimiento de usuarios con perfil y estado. |
| 21 | `21-admin-clase-nueva.png` | Formulario de nueva clase con las listas de deporte y nivel cargadas desde la base de datos. |
| 22 | `22-recuperar-clave-enviado.png` | Recuperación de contraseña: solicitud enviada (la respuesta es igual exista o no la cuenta). |
| 23 | `23-recuperar-clave-formulario.png` | Formulario de nueva contraseña abierto desde el enlace de un solo uso. |
| 24 | `24-recuperar-clave-listo.png` | Contraseña actualizada: el login informa que ya puede ingresar con la nueva. |
| 25 | `25-2fa-pantalla-codigo.png` | **Verificación en dos pasos:** tras usuario y clave se pide el código de 6 dígitos enviado al correo (mostrado enmascarado). |
| 26 | `26-2fa-codigo-incorrecto.png` | Código incorrecto: se informan los intentos que quedan (máximo 5). |
| 27 | `27-2fa-acceso-concedido.png` | Código correcto: se concede el acceso y se redirige al ambiente del perfil. |

Notas para el informe: las capturas 25 a 27 se tomaron con un servidor de correo simulado (el código se leyó de ese servidor); en producción el código llega por Brevo. El pago es simulado: no se mueve dinero.

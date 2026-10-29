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

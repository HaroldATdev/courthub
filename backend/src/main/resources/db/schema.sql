-- ============================================================
-- CourtHub · 01-schema.sql — Esquema de Base de Datos (PostgreSQL 16)
-- Se ejecuta automáticamente al crear el contenedor de Docker.
-- ============================================================

-- Extensión para hashear contraseñas desde SQL (bcrypt) en el seed
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1) USUARIOS  (roles: cliente | administrador | entrenador)
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
  id               SERIAL PRIMARY KEY,
  nombre           VARCHAR(100) NOT NULL,
  apellido         VARCHAR(100) NOT NULL DEFAULT '',
  email            VARCHAR(150) NOT NULL UNIQUE,
  password_hash    VARCHAR(200) NOT NULL,
  rol              VARCHAR(20)  NOT NULL DEFAULT 'cliente'
                   CHECK (rol IN ('cliente', 'administrador', 'entrenador')),
  telefono         VARCHAR(25)  NOT NULL DEFAULT '',
  nivel            VARCHAR(30)  NOT NULL DEFAULT 'Bronce',
  deporte_favorito VARCHAR(50)  NOT NULL DEFAULT 'Multi',
  -- Tercer avance: identificación y ciclo de vida de la cuenta
  dni              VARCHAR(8),                       -- único (validación en el registro)
  usuario          VARCHAR(50),                      -- nombre de usuario único (validación en el registro)
  estado           VARCHAR(20)  NOT NULL DEFAULT 'pendiente'
                   CHECK (estado IN ('activo', 'pendiente', 'anulado')),  -- solo el administrador activa
  fecha_registro   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Migración idempotente para bases creadas antes del tercer avance (la columna "activo" se migra en DataInitializer)
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS dni VARCHAR(8);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS usuario VARCHAR(50);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'pendiente';
CREATE UNIQUE INDEX IF NOT EXISTS uq_usuarios_dni ON usuarios (dni) WHERE dni IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_usuarios_usuario ON usuarios (LOWER(usuario)) WHERE usuario IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_usuarios_estado ON usuarios (estado);

-- ============================================================
-- 2) CANCHAS  (fútbol, fulbito, pádel, tenis, squash, etc.)
-- ============================================================
CREATE TABLE IF NOT EXISTS canchas (
  id             SERIAL PRIMARY KEY,
  nombre         VARCHAR(150) NOT NULL,
  tipo           VARCHAR(50)  NOT NULL, -- futbol | padel | tenis | squash
  subtipo        VARCHAR(80)  NOT NULL DEFAULT '',
  descripcion    TEXT         NOT NULL DEFAULT '',
  direccion      VARCHAR(220) NOT NULL DEFAULT '',
  comuna         VARCHAR(100) NOT NULL,
  ciudad         VARCHAR(100) NOT NULL DEFAULT 'Lima',
  precio_hora    NUMERIC(8,2) NOT NULL CHECK (precio_hora > 0),      -- Soles peruanos (S/) por hora
  calificacion   NUMERIC(2,1) NOT NULL DEFAULT 4.5 CHECK (calificacion BETWEEN 0 AND 5),
  num_opiniones  INTEGER      NOT NULL DEFAULT 0,
  imagen_url     VARCHAR(400) NOT NULL DEFAULT '',
  verificada     BOOLEAN      NOT NULL DEFAULT TRUE,
  servicios      TEXT[]       NOT NULL DEFAULT '{}',
  reglamento     TEXT         NOT NULL DEFAULT '',
  activa         BOOLEAN      NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_canchas_tipo ON canchas (tipo);
CREATE INDEX IF NOT EXISTS idx_canchas_comuna ON canchas (comuna);
-- ============================================================
-- 3) DISPONIBILIDAD DE CANCHAS  (horarios de atención por día)
--    dia_semana: 0 = domingo ... 6 = sábado
-- ============================================================
CREATE TABLE IF NOT EXISTS disponibilidad_canchas (
  id          SERIAL PRIMARY KEY,
  cancha_id   INTEGER NOT NULL REFERENCES canchas (id) ON DELETE CASCADE,
  dia_semana  SMALLINT NOT NULL CHECK (dia_semana BETWEEN 0 AND 6),
  hora_inicio TIME     NOT NULL,
  hora_fin    TIME     NOT NULL,
  UNIQUE (cancha_id, dia_semana, hora_inicio)
);

-- ============================================================
-- 4) RESERVAS  (estado: pendiente | confirmada | completada | cancelada)
-- ============================================================
CREATE TABLE IF NOT EXISTS reservas (
  id             SERIAL PRIMARY KEY,
  codigo         VARCHAR(20) UNIQUE NOT NULL DEFAULT ('CH-' || LPAD(FLOOR(RANDOM() * 90000 + 10000)::text, 5, '0')),
  usuario_id     INTEGER NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
  cancha_id      INTEGER NOT NULL REFERENCES canchas (id) ON DELETE CASCADE,
  fecha          DATE    NOT NULL,
  hora_inicio    TIME    NOT NULL,
  hora_fin       TIME    NOT NULL,
  estado         VARCHAR(20) NOT NULL DEFAULT 'pendiente'
                 CHECK (estado IN ('pendiente', 'confirmada', 'completada', 'cancelada')),
  subtotal       NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0),
  cargo_servicio NUMERIC(10,2) NOT NULL DEFAULT 0,
  total          NUMERIC(10,2) NOT NULL CHECK (total >= 0),
  metodo_pago    VARCHAR(40) NOT NULL DEFAULT 'Tarjeta de Crédito',
  notas          TEXT NOT NULL DEFAULT '',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CHECK (hora_fin > hora_inicio)
);

-- Un mismo bloque de cancha no puede reservarse dos veces (salvo canceladas)
CREATE UNIQUE INDEX IF NOT EXISTS uq_reserva_bloque ON reservas (cancha_id, fecha, hora_inicio)
  WHERE estado <> 'cancelada';
CREATE INDEX IF NOT EXISTS idx_reservas_usuario ON reservas (usuario_id);
CREATE INDEX IF NOT EXISTS idx_reservas_cancha_fecha ON reservas (cancha_id, fecha);
-- ============================================================
-- 5) CLASES DEPORTIVAS  (impartidas por entrenadores)
-- ============================================================
CREATE TABLE IF NOT EXISTS clases (
  id            SERIAL PRIMARY KEY,
  nombre        VARCHAR(150) NOT NULL,
  deporte       VARCHAR(50)  NOT NULL,
  nivel         VARCHAR(40)  NOT NULL DEFAULT 'Todos los niveles',
  descripcion   TEXT         NOT NULL DEFAULT '',
  entrenador_id INTEGER      NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
  cancha_id     INTEGER      REFERENCES canchas (id) ON DELETE SET NULL,
  fecha         DATE         NOT NULL,
  hora_inicio   TIME         NOT NULL,
  hora_fin      TIME         NOT NULL,
  cupo_maximo   INTEGER      NOT NULL DEFAULT 12 CHECK (cupo_maximo > 0),
  precio        NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (precio >= 0),
  activa        BOOLEAN      NOT NULL DEFAULT TRUE,
  CHECK (hora_fin > hora_inicio)
);

CREATE INDEX IF NOT EXISTS idx_clases_fecha ON clases (fecha);
CREATE INDEX IF NOT EXISTS idx_clases_deporte ON clases (deporte);

-- ============================================================
-- 6) INSCRIPCIONES A CLASES  (clientes se apuntan a clases)
-- ============================================================
CREATE TABLE IF NOT EXISTS inscripciones_clases (
  id                SERIAL PRIMARY KEY,
  clase_id          INTEGER NOT NULL REFERENCES clases (id) ON DELETE CASCADE,
  usuario_id        INTEGER NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
  estado            VARCHAR(20) NOT NULL DEFAULT 'inscrito'
                    CHECK (estado IN ('inscrito', 'cancelado', 'asistio')),
  fecha_inscripcion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (clase_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS idx_inscripciones_usuario ON inscripciones_clases (usuario_id);

-- ============================================================
-- 7) DEPORTES — catálogo para las listas desplegables de los CRUD
--    de canchas y clases (tercer avance). Datos de referencia idempotentes.
-- ============================================================
CREATE TABLE IF NOT EXISTS deportes (
  id      SERIAL PRIMARY KEY,
  codigo  VARCHAR(30) NOT NULL UNIQUE,   -- valor guardado en canchas.tipo / clases.deporte
  nombre  VARCHAR(60) NOT NULL,          -- etiqueta mostrada
  orden   SMALLINT    NOT NULL DEFAULT 0,
  activo  BOOLEAN     NOT NULL DEFAULT TRUE
);
INSERT INTO deportes (codigo, nombre, orden) VALUES
  ('futbol', 'Fútbol', 1), ('tenis', 'Tenis', 2), ('squash', 'Squash', 3), ('padel', 'Pádel', 4)
ON CONFLICT (codigo) DO NOTHING;

-- ============================================================
-- 8) NIVELES DE CLASE — catálogo para la lista desplegable del CRUD de clases
-- ============================================================
CREATE TABLE IF NOT EXISTS niveles_clase (
  id      SERIAL PRIMARY KEY,
  nombre  VARCHAR(40) NOT NULL UNIQUE,
  orden   SMALLINT    NOT NULL DEFAULT 0
);
INSERT INTO niveles_clase (nombre, orden) VALUES
  ('Todos los niveles', 1), ('Principiante', 2), ('Intermedio', 3), ('Avanzado', 4)
ON CONFLICT (nombre) DO NOTHING;

-- ============================================================
-- 9) CLAVES_RESET — recuperación de contraseña por correo (solo se guarda el hash del token)
-- ============================================================
CREATE TABLE IF NOT EXISTS claves_reset (
  id             SERIAL PRIMARY KEY,
  usuario_id     INTEGER     NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
  token_hash     VARCHAR(64) NOT NULL UNIQUE,
  expira         TIMESTAMPTZ NOT NULL,
  usado          BOOLEAN     NOT NULL DEFAULT FALSE,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_claves_reset_usuario ON claves_reset (usuario_id);

-- ============================================================
-- 10) PAGOS — un registro por cobro de reserva (estado: aprobado | rechazado | reembolsado)
--     proveedor 'simulado' mientras no haya pasarela; solo se guardan los últimos 4 dígitos de la tarjeta.
-- ============================================================
CREATE TABLE IF NOT EXISTS pagos (
  id               SERIAL PRIMARY KEY,
  reserva_id       INTEGER       NOT NULL REFERENCES reservas (id) ON DELETE CASCADE,
  usuario_id       INTEGER       NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
  operacion        VARCHAR(30)   NOT NULL UNIQUE,
  proveedor        VARCHAR(30)   NOT NULL DEFAULT 'simulado',
  metodo           VARCHAR(40)   NOT NULL,
  tarjeta_ultimos4 VARCHAR(4),
  subtotal         NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0),
  cargo_servicio   NUMERIC(10,2) NOT NULL DEFAULT 0,
  monto            NUMERIC(10,2) NOT NULL CHECK (monto >= 0),
  moneda           VARCHAR(3)    NOT NULL DEFAULT 'PEN',
  estado           VARCHAR(20)   NOT NULL DEFAULT 'aprobado'
                   CHECK (estado IN ('aprobado', 'rechazado', 'reembolsado')),
  fecha_pago       TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
-- Una reserva no puede tener dos cobros aprobados a la vez
CREATE UNIQUE INDEX IF NOT EXISTS uq_pago_reserva_aprobado ON pagos (reserva_id) WHERE estado = 'aprobado';
CREATE INDEX IF NOT EXISTS idx_pagos_usuario ON pagos (usuario_id);
CREATE INDEX IF NOT EXISTS idx_pagos_fecha ON pagos (fecha_pago);

-- ============================================================
-- 11) PAGOS también para inscripciones a clases + referencia de la pasarela
--     Un pago pertenece a UNA reserva o a UNA inscripción (nunca a ambas).
-- ============================================================
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS inscripcion_id INTEGER REFERENCES inscripciones_clases (id) ON DELETE CASCADE;
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS referencia VARCHAR(80);          -- id del cargo en la pasarela (p. ej. chr_test_...)
ALTER TABLE pagos ALTER COLUMN reserva_id DROP NOT NULL;
ALTER TABLE pagos DROP CONSTRAINT IF EXISTS ck_pago_origen;
ALTER TABLE pagos ADD CONSTRAINT ck_pago_origen CHECK ((reserva_id IS NOT NULL) <> (inscripcion_id IS NOT NULL));
CREATE UNIQUE INDEX IF NOT EXISTS uq_pago_inscripcion_aprobado ON pagos (inscripcion_id) WHERE estado = 'aprobado';
CREATE INDEX IF NOT EXISTS idx_pagos_inscripcion ON pagos (inscripcion_id);

-- ============================================================
-- 12) VERIFICACIONES_2FA — código de un solo uso enviado al correo en cada inicio de sesión
--     (solo se guardan hashes: del desafío y del código; vence a los 10 min y admite 5 intentos)
-- ============================================================
CREATE TABLE IF NOT EXISTS verificaciones_2fa (
  id             SERIAL PRIMARY KEY,
  usuario_id     INTEGER     NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
  desafio_hash   VARCHAR(64) NOT NULL UNIQUE,
  codigo_hash    VARCHAR(64) NOT NULL,
  expira         TIMESTAMPTZ NOT NULL,
  intentos       SMALLINT    NOT NULL DEFAULT 0,
  usado          BOOLEAN     NOT NULL DEFAULT FALSE,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_2fa_usuario ON verificaciones_2fa (usuario_id);

-- ============================================================
-- 13) SEGURIDAD (Supabase): activar RLS en todas las tablas.
--     La aplicación se conecta como propietario de las tablas (usuario postgres), que ignora RLS,
--     así que no cambia nada para ella; en cambio bloquea el acceso con las claves públicas de la API
--     REST de Supabase (anon / authenticated), que no tienen ninguna política.
-- ============================================================
ALTER TABLE usuarios               ENABLE ROW LEVEL SECURITY;
ALTER TABLE canchas                ENABLE ROW LEVEL SECURITY;
ALTER TABLE disponibilidad_canchas ENABLE ROW LEVEL SECURITY;
ALTER TABLE reservas               ENABLE ROW LEVEL SECURITY;
ALTER TABLE clases                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE inscripciones_clases   ENABLE ROW LEVEL SECURITY;
ALTER TABLE deportes               ENABLE ROW LEVEL SECURITY;
ALTER TABLE niveles_clase          ENABLE ROW LEVEL SECURITY;
ALTER TABLE claves_reset           ENABLE ROW LEVEL SECURITY;
ALTER TABLE pagos                  ENABLE ROW LEVEL SECURITY;
ALTER TABLE verificaciones_2fa     ENABLE ROW LEVEL SECURITY;

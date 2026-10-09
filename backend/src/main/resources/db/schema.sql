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
  activo           BOOLEAN      NOT NULL DEFAULT TRUE,
  fecha_registro   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

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



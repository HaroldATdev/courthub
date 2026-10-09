-- ============================================================
-- CourtHub · consultas-evidencia.sql
-- Consultas SQL de evidencia para la exposición (PostgreSQL 16).
--
-- Cómo ejecutarlas con Docker:
--   docker compose exec db psql -U courthub -d courthub -f /dev/stdin < backend/src/main/resources/db/consultas-evidencia.sql
-- o de forma interactiva:
--   docker compose exec db psql -U courthub -d courthub
-- ============================================================

\echo '=== 1) Tablas del esquema y cantidad de registros ==='
SELECT 'usuarios' AS tabla, COUNT(*) AS registros FROM usuarios
UNION ALL SELECT 'canchas', COUNT(*) FROM canchas
UNION ALL SELECT 'disponibilidad_canchas', COUNT(*) FROM disponibilidad_canchas
UNION ALL SELECT 'reservas', COUNT(*) FROM reservas
UNION ALL SELECT 'clases', COUNT(*) FROM clases
UNION ALL SELECT 'inscripciones_clases', COUNT(*) FROM inscripciones_clases
ORDER BY tabla;

\echo '=== 2) Usuarios por rol (las contraseñas se guardan con hash BCrypt) ==='
SELECT rol, COUNT(*) AS cantidad, STRING_AGG(email, ', ' ORDER BY email) AS correos,
       LEFT(MIN(password_hash), 7) AS prefijo_hash
FROM usuarios
GROUP BY rol
ORDER BY rol;

\echo '=== 3) Catálogo de canchas con precio del bloque de 1.5 h ==='
SELECT id, nombre, tipo, comuna, precio_hora,
       ROUND(precio_hora * 1.5, 2) AS precio_bloque,
       calificacion, ARRAY_LENGTH(servicios, 1) AS n_servicios, activa
FROM canchas
ORDER BY calificacion DESC, nombre;

\echo '=== 4) Filtro del dashboard: canchas de pádel entre S/ 80 y S/ 110 (misma lógica que CanchaRepository.buscar) ==='
SELECT id, nombre, comuna, precio_hora
FROM canchas
WHERE activa = TRUE
  AND tipo = 'padel'
  AND precio_hora BETWEEN 80 AND 110
ORDER BY calificacion DESC, nombre;

\echo '=== 5) Historial de reservas del cliente demo con su clasificación (Próximas / Pasadas / Canceladas) ==='
SELECT r.codigo, c.nombre AS cancha, r.fecha, r.hora_inicio, r.hora_fin, r.estado, r.total, r.metodo_pago,
       CASE
         WHEN r.estado = 'cancelada' THEN 'canceladas'
         WHEN r.estado = 'completada' OR r.fecha < CURRENT_DATE THEN 'pasadas'
         ELSE 'proximas'
       END AS pestana
FROM reservas r
JOIN canchas c ON c.id = r.cancha_id
JOIN usuarios u ON u.id = r.usuario_id
WHERE u.email = 'demo@courthub.com'
ORDER BY r.fecha DESC, r.hora_inicio DESC;

\echo '=== 6) Disponibilidad de la cancha 2 para una fecha: bloques de 1.5 h ocupados vs libres ==='
WITH bloques AS (
  SELECT (TIME '08:00' + (n * INTERVAL '90 minutes'))::time AS hora_inicio
  FROM generate_series(0, 9) AS n
)
SELECT b.hora_inicio,
       (b.hora_inicio + INTERVAL '90 minutes')::time AS hora_fin,
       CASE WHEN r.id IS NULL THEN 'libre' ELSE 'ocupado (' || r.codigo || ')' END AS estado
FROM bloques b
LEFT JOIN reservas r
       ON r.cancha_id = 2
      AND r.fecha = CURRENT_DATE + 2
      AND r.estado <> 'cancelada'
      AND r.hora_inicio < (b.hora_inicio + INTERVAL '90 minutes')::time
      AND b.hora_inicio < r.hora_fin
ORDER BY b.hora_inicio;

\echo '=== 7) Integridad: el índice único parcial impide reservar dos veces el mismo bloque (debe fallar) ==='
\echo '    (si ya existe una reserva confirmada para la cancha 2 el día CURRENT_DATE + 2 a las 18:00, el INSERT es rechazado)'
BEGIN;  -- la prueba se deshace al final con ROLLBACK para no alterar los datos
DO $$
BEGIN
  INSERT INTO reservas (usuario_id, cancha_id, fecha, hora_inicio, hora_fin, estado, subtotal, cargo_servicio, total)
  VALUES (2, 2, CURRENT_DATE + 2, '18:00', '19:30', 'confirmada', 142.50, 14.25, 156.75);
  RAISE NOTICE 'Bloque libre: la reserva se insertó (no había otra activa en ese bloque)';
EXCEPTION WHEN unique_violation THEN
  RAISE NOTICE 'Integridad OK: unique_violation en uq_reserva_bloque (bloque ya reservado)';
END $$;
ROLLBACK;

\echo '=== 8) Ingresos por mes (gráfico del panel ERP) ==='
SELECT TO_CHAR(fecha, 'YYYY-MM') AS mes,
       COUNT(*) AS reservas,
       SUM(subtotal) AS subtotal,
       SUM(cargo_servicio) AS cargo_servicio_10pct,
       SUM(total) AS ingresos
FROM reservas
WHERE estado <> 'cancelada'
GROUP BY 1
ORDER BY 1;

\echo '=== 9) Ranking de ingresos por cancha y por deporte (panel ERP) ==='
SELECT c.nombre AS cancha, c.tipo AS deporte, COUNT(r.id) AS reservas,
       COALESCE(SUM(r.total), 0) AS ingresos
FROM canchas c
LEFT JOIN reservas r ON r.cancha_id = c.id AND r.estado <> 'cancelada'
GROUP BY c.id, c.nombre, c.tipo
ORDER BY ingresos DESC, cancha;

SELECT c.tipo AS deporte, COUNT(*) AS reservas, SUM(r.total) AS ingresos,
       ROUND(100.0 * SUM(r.total) / SUM(SUM(r.total)) OVER (), 1) AS porcentaje
FROM reservas r JOIN canchas c ON c.id = r.cancha_id
WHERE r.estado <> 'cancelada'
GROUP BY c.tipo
ORDER BY ingresos DESC;

\echo '=== 10) Reservas por estado y por método de pago ==='
SELECT estado, COUNT(*) AS cantidad, SUM(total) AS monto FROM reservas GROUP BY estado ORDER BY estado;
SELECT metodo_pago, COUNT(*) AS cantidad, SUM(total) AS monto
FROM reservas WHERE estado <> 'cancelada'
GROUP BY metodo_pago ORDER BY monto DESC;

\echo '=== 11) Ocupación de hoy: bloques reservados sobre la capacidad total (canchas activas x 10 bloques) ==='
SELECT COUNT(*) FILTER (WHERE r.estado <> 'cancelada') AS bloques_reservados_hoy,
       (SELECT COUNT(*) FROM canchas WHERE activa) * 10 AS capacidad_bloques,
       ROUND(100.0 * COUNT(*) FILTER (WHERE r.estado <> 'cancelada') / ((SELECT COUNT(*) FROM canchas WHERE activa) * 10), 1) AS ocupacion_pct
FROM reservas r
WHERE r.fecha = CURRENT_DATE;

\echo '=== 12) Clases próximas con entrenador, cancha y cupo disponible (barra de cupo) ==='
SELECT k.id, k.nombre AS clase, k.deporte, k.nivel,
       u.nombre || ' ' || u.apellido AS entrenador,
       c.nombre AS cancha, k.fecha, k.hora_inicio, k.hora_fin,
       k.cupo_maximo,
       COUNT(i.id) FILTER (WHERE i.estado = 'inscrito') AS inscritos,
       k.cupo_maximo - COUNT(i.id) FILTER (WHERE i.estado = 'inscrito') AS cupos_disponibles,
       k.precio
FROM clases k
JOIN usuarios u ON u.id = k.entrenador_id
LEFT JOIN canchas c ON c.id = k.cancha_id
LEFT JOIN inscripciones_clases i ON i.clase_id = k.id
WHERE k.activa = TRUE AND k.fecha >= CURRENT_DATE
GROUP BY k.id, u.nombre, u.apellido, c.nombre
ORDER BY k.fecha, k.hora_inicio;

\echo '=== 13) Alumnos inscritos por clase (vista "Inscritos" del administrador) ==='
SELECT k.nombre AS clase, u.nombre || ' ' || u.apellido AS alumno, u.email, i.estado, i.fecha_inscripcion
FROM inscripciones_clases i
JOIN clases k ON k.id = i.clase_id
JOIN usuarios u ON u.id = i.usuario_id
ORDER BY k.fecha, k.nombre, i.fecha_inscripcion;

\echo '=== 14) Integridad: un alumno no puede inscribirse dos veces a la misma clase (UNIQUE clase_id, usuario_id) ==='
BEGIN;
DO $$
BEGIN
  INSERT INTO inscripciones_clases (clase_id, usuario_id) VALUES (1, 5);
  RAISE NOTICE 'Se insertó (el alumno 5 no estaba en la clase 1)';
EXCEPTION WHEN unique_violation THEN
  RAISE NOTICE 'Integridad OK: unique_violation (el alumno ya está inscrito en la clase 1)';
END $$;
ROLLBACK;

\echo '=== 15) Clientes con mayor gasto (top 5) ==='
SELECT u.nombre || ' ' || u.apellido AS cliente, u.email, u.nivel,
       COUNT(r.id) AS reservas, SUM(r.total) AS gasto_total
FROM usuarios u
JOIN reservas r ON r.usuario_id = u.id AND r.estado <> 'cancelada'
GROUP BY u.id
ORDER BY gasto_total DESC
LIMIT 5;

\echo '=== 16) Restricciones e índices definidos en el esquema ==='
SELECT conrelid::regclass AS tabla, conname AS restriccion, contype AS tipo
FROM pg_constraint
WHERE connamespace = 'public'::regnamespace
ORDER BY conrelid::regclass::text, conname;

SELECT tablename AS tabla, indexname AS indice, indexdef AS definicion
FROM pg_indexes
WHERE schemaname = 'public' AND indexname NOT LIKE '%_pkey'
ORDER BY tablename, indexname;

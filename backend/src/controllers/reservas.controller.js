/* ============================================================
   CourtHub API · controllers/reservas.controller.js
   Crear, listar, cancelar y reprogramar reservas del usuario
   ============================================================ */
const { query } = require('../config/db');
const { sumarMinutos, horaCorta, hoyISO } = require('../utils/horarios');

const DURACION_BLOQUE = 90;  // minutos
const TASA_CARGO = 0.10;     // 10% cargo por servicio CourtHub

/** Fecha (DATE de pg) → "yyyy-mm-dd". */
function isoFecha(d) {
  return (d instanceof Date ? d.toISOString() : String(d)).slice(0, 10);
}

/** Fila de reserva → formato del frontend (con snapshot de la cancha). */
function mapear(r) {
  return {
    id: r.id,
    codigo: r.codigo,
    cancha_id: r.cancha_id,
    cancha: {
      id: r.cancha_id,
      nombre: r.cancha_nombre,
      tipo: r.cancha_tipo,
      comuna: r.cancha_comuna,
      imagen: r.cancha_imagen
    },
    fecha: isoFecha(r.fecha),
    hora_inicio: horaCorta(r.hora_inicio),
    hora_fin: horaCorta(r.hora_fin),
    estado: r.estado,
    subtotal: Number(r.subtotal),
    cargo_servicio: Number(r.cargo_servicio),
    total: Number(r.total),
    metodo_pago: r.metodo_pago
  };
}

const SELECT_RESERVAS = `
  SELECT r.*, c.nombre AS cancha_nombre, c.tipo AS cancha_tipo,
         c.comuna AS cancha_comuna, c.imagen_url AS cancha_imagen
  FROM reservas r
  JOIN canchas c ON c.id = r.cancha_id`;

/** Valida formato básico de fecha yyyy-mm-dd. */
function fechaValida(f) {
  return /^\d{4}-\d{2}-\d{2}$/.test(String(f || ''));
}
// POST /api/reservas
exports.crear = async (req, res, next) => {
  try {
    const { cancha_id, fecha, hora_inicio, metodo_pago = 'Tarjeta de Crédito' } = req.body;
    if (!cancha_id || !fechaValida(fecha) || !hora_inicio) {
      return res.status(400).json({ error: 'Datos de reserva incompletos o inválidos.' });
    }
    const hoy = hoyISO();
    const ahora = new Date().toLocaleTimeString('sv-SE', { timeZone: 'America/Lima' }).slice(0, 5);
    if (fecha < hoy || (fecha === hoy && hora_inicio <= ahora)) {
      return res.status(400).json({ error: 'No puedes reservar bloques que ya pasaron.' });
    }

    const canchas = await query('SELECT * FROM canchas WHERE id = $1 AND activa = TRUE', [cancha_id]);
    const cancha = canchas[0];
    if (!cancha) return res.status(404).json({ error: 'Cancha no encontrada.' });

    const hora_fin = sumarMinutos(hora_inicio, DURACION_BLOQUE);

    const choques = await query(
      `SELECT 1 FROM reservas
       WHERE cancha_id = $1 AND fecha = $2 AND estado <> 'cancelada'
         AND hora_inicio < $3 AND $4 < hora_fin`,
      [cancha_id, fecha, hora_fin, hora_inicio]
    );
    if (choques.length) return res.status(409).json({ error: 'Ese bloque horario ya no está disponible. Elige otro.' });

    const subtotal = Math.round(Number(cancha.precio_hora) * 1.5 * 100) / 100;
    const cargo = Math.round(subtotal * TASA_CARGO * 100) / 100;
    const total = subtotal + cargo;

    const filas = await query(
      `INSERT INTO reservas (usuario_id, cancha_id, fecha, hora_inicio, hora_fin, estado, subtotal, cargo_servicio, total, metodo_pago)
       VALUES ($1, $2, $3, $4, $5, 'confirmada', $6, $7, $8, $9)
       RETURNING *`,
      [req.user.id, cancha_id, fecha, hora_inicio, hora_fin, subtotal, cargo, total, metodo_pago]
    );
    const fila = { ...filas[0], cancha_nombre: cancha.nombre, cancha_tipo: cancha.tipo, cancha_comuna: cancha.comuna, cancha_imagen: cancha.imagen_url };
    res.status(201).json({ reserva: mapear(fila) });
  } catch (e) { next(e); }
};

// GET /api/reservas  (propias; administrador ve todas con ?todas=1)
exports.listar = async (req, res, next) => {
  try {
    const verTodas = req.query.todas === '1' && req.user.rol === 'administrador';
    const where = verTodas ? '' : 'WHERE r.usuario_id = $1';
    const params = verTodas ? [] : [req.user.id];
    const filas = await query(
      `${SELECT_RESERVAS} ${where} ORDER BY r.fecha DESC, r.hora_inicio DESC`,
      params
    );
    res.json({ reservas: filas.map(mapear) });
  } catch (e) { next(e); }
};
// PATCH /api/reservas/:id/cancelar
exports.cancelar = async (req, res, next) => {
  try {
    const filas = await query(
      `UPDATE reservas SET estado = 'cancelada'
       WHERE id = $1 AND usuario_id = $2 AND estado IN ('pendiente', 'confirmada')
       RETURNING id`,
      [req.params.id, req.user.id]
    );
    if (!filas.length) return res.status(404).json({ error: 'Reserva no encontrada o ya cancelada.' });
    const completa = await query(`${SELECT_RESERVAS} WHERE r.id = $1`, [filas[0].id]);
    res.json({ reserva: mapear(completa[0]) });
  } catch (e) { next(e); }
};

// PATCH /api/reservas/:id/reprogramar
exports.reprogramar = async (req, res, next) => {
  try {
    const { fecha, hora_inicio } = req.body;
    if (!fechaValida(fecha) || !hora_inicio) {
      return res.status(400).json({ error: 'Indica la nueva fecha y hora de la reserva.' });
    }
    if (fecha < hoyISO()) return res.status(400).json({ error: 'La nueva fecha no puede ser pasada.' });

    const actuales = await query('SELECT * FROM reservas WHERE id = $1 AND usuario_id = $2', [req.params.id, req.user.id]);
    const reserva = actuales[0];
    if (!reserva) return res.status(404).json({ error: 'Reserva no encontrada.' });

    const hora_fin = sumarMinutos(hora_inicio, DURACION_BLOQUE);
    const choques = await query(
      `SELECT 1 FROM reservas
       WHERE cancha_id = $1 AND fecha = $2 AND estado <> 'cancelada' AND id <> $3
         AND hora_inicio < $4 AND $5 < hora_fin`,
      [reserva.cancha_id, fecha, reserva.id, hora_fin, hora_inicio]
    );
    if (choques.length) return res.status(409).json({ error: 'El nuevo bloque ya está ocupado. Elige otro horario.' });

    await query(
      `UPDATE reservas SET fecha = $1, hora_inicio = $2, hora_fin = $3, estado = 'confirmada' WHERE id = $4`,
      [fecha, hora_inicio, hora_fin, reserva.id]
    );
    const completa = await query(`${SELECT_RESERVAS} WHERE r.id = $1`, [reserva.id]);
    res.json({ reserva: mapear(completa[0]) });
  } catch (e) { next(e); }
};



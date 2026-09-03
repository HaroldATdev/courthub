/* ============================================================
   CourtHub API · controllers/canchas.controller.js
   Listado con filtros, detalle y disponibilidad por fecha
   ============================================================ */
const { query } = require('../config/db');
const { BLOQUES, sumarMinutos, horaCorta, hoyISO } = require('../utils/horarios');

/** Convierte una fila de canchas (snake_case) al formato del frontend. */
function mapear(c) {
  return {
    id: c.id,
    nombre: c.nombre,
    tipo: c.tipo,
    subtipo: c.subtipo,
    comuna: c.comuna,
    direccion: c.direccion,
    ciudad: c.ciudad,
    precioHora: Number(c.precio_hora),
    calificacion: Number(c.calificacion),
    opiniones: c.num_opiniones,
    verificada: c.verificada,
    imagen: c.imagen_url,
    servicios: c.servicios || [],
    reglamento: c.reglamento || '',
    descripcion: c.descripcion
  };
}

// GET /api/canchas?tipo=&q=&precioMin=&precioMax=
exports.listar = async (req, res, next) => {
  try {
    const { tipo, q, precioMin, precioMax } = req.query;
    const condiciones = ['activa = TRUE'];
    const params = [];

    if (tipo && tipo !== 'todos') { params.push(tipo); condiciones.push(`tipo = $${params.length}`); }
    if (q) {
      params.push(`%${q}%`);
      condiciones.push(`(nombre ILIKE $${params.length} OR comuna ILIKE $${params.length} OR tipo ILIKE $${params.length})`);
    }
    if (precioMin) { params.push(Number(precioMin)); condiciones.push(`precio_hora >= $${params.length}`); }
    if (precioMax) { params.push(Number(precioMax)); condiciones.push(`precio_hora <= $${params.length}`); }

    const filas = await query(
      `SELECT * FROM canchas WHERE ${condiciones.join(' AND ')} ORDER BY calificacion DESC, nombre ASC`,
      params
    );
    res.json({ canchas: filas.map(mapear) });
  } catch (e) { next(e); }
};

// GET /api/canchas/:id
exports.detalle = async (req, res, next) => {
  try {
    const filas = await query('SELECT * FROM canchas WHERE id = $1 AND activa = TRUE', [req.params.id]);
    if (!filas.length) return res.status(404).json({ error: 'Cancha no encontrada.' });
    res.json({ cancha: mapear(filas[0]) });
  } catch (e) { next(e); }
};

// GET /api/canchas/:id/disponibilidad?fecha=yyyy-mm-dd
exports.disponibilidad = async (req, res, next) => {
  try {
    const fecha = req.query.fecha || hoyISO();
    const hoy = hoyISO();
    const ahora = new Date().toLocaleTimeString('sv-SE', { timeZone: 'America/Santiago' }).slice(0, 5);

    const ocupadas = await query(
      `SELECT hora_inicio FROM reservas
       WHERE cancha_id = $1 AND fecha = $2 AND estado <> 'cancelada'`,
      [req.params.id, fecha]
    );
    const reservadas = new Set(ocupadas.map((r) => horaCorta(r.hora_inicio)));

    const bloques = BLOQUES.map((inicio) => {
      const fin = sumarMinutos(inicio, 90);
      let ocupado = reservadas.has(inicio);
      if (fecha < hoy) ocupado = true;                       // día pasado
      else if (fecha === hoy && inicio <= ahora) ocupado = true; // bloque ya iniciado hoy
      return { horaInicio: inicio, horaFin: fin, ocupado };
    });

    res.json({ fecha, bloques });
  } catch (e) { next(e); }
};

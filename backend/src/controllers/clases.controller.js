/* ============================================================
   CourtHub API · controllers/clases.controller.js
   Clases deportivas + inscripciones (cupo limitado)
   ============================================================ */
const { query } = require('../config/db');
const { horaCorta, hoyISO } = require('../utils/horarios');

function isoFecha(d) {
  return (d instanceof Date ? d.toISOString() : String(d)).slice(0, 10);
}

function mapear(k, inscritosSet) {
  const inscritos = Number(k.inscritos || 0);
  return {
    id: k.id,
    nombre: k.nombre,
    deporte: k.deporte,
    nivel: k.nivel,
    descripcion: k.descripcion,
    fecha: isoFecha(k.fecha),
    hora_inicio: horaCorta(k.hora_inicio),
    hora_fin: horaCorta(k.hora_fin),
    cupo_maximo: k.cupo_maximo,
    inscritos,
    cupos_disponibles: Math.max(0, k.cupo_maximo - inscritos),
    precio: Number(k.precio),
    entrenador: { nombre: `${k.entrenador_nombre} ${k.entrenador_apellido}`.trim() },
    cancha: k.cancha_nombre ? { nombre: k.cancha_nombre, comuna: k.cancha_comuna } : null,
    inscrito: inscritosSet ? inscritosSet.has(k.id) : false
  };
}

const SELECT_CLASES = `
  SELECT k.*,
         u.nombre AS entrenador_nombre, u.apellido AS entrenador_apellido,
         c.nombre AS cancha_nombre, c.comuna AS cancha_comuna,
         (SELECT COUNT(*)::int FROM inscripciones_clases i
           WHERE i.clase_id = k.id AND i.estado = 'inscrito') AS inscritos
  FROM clases k
  JOIN usuarios u ON u.id = k.entrenador_id
  LEFT JOIN canchas c ON c.id = k.cancha_id`;

// GET /api/clases?deporte=
exports.listar = async (req, res, next) => {
  try {
    const condiciones = ['k.activa = TRUE', 'k.fecha >= $1'];
    const params = [hoyISO()];
    if (req.query.deporte && req.query.deporte !== 'todos') {
      params.push(req.query.deporte);
      condiciones.push(`k.deporte = $${params.length}`);
    }
    const filas = await query(
      `${SELECT_CLASES} WHERE ${condiciones.join(' AND ')} ORDER BY k.fecha ASC, k.hora_inicio ASC`,
      params
    );
    const mias = await query('SELECT clase_id FROM inscripciones_clases WHERE usuario_id = $1 AND estado = $2', [req.user.id, 'inscrito']);
    const inscritosSet = new Set(mias.map((m) => m.clase_id));
    res.json({ clases: filas.map((k) => mapear(k, inscritosSet)) });
  } catch (e) { next(e); }
};

// POST /api/clases/:id/inscribirse
exports.inscribirse = async (req, res, next) => {
  try {
    const filas = await query('SELECT * FROM clases WHERE id = $1 AND activa = TRUE', [req.params.id]);
    const clase = filas[0];
    if (!clase) return res.status(404).json({ error: 'Clase no encontrada.' });

    const ocupacion = await query(
      `SELECT COUNT(*)::int AS n FROM inscripciones_clases WHERE clase_id = $1 AND estado = 'inscrito'`,
      [clase.id]
    );
    if (ocupacion[0].n >= clase.cupo_maximo) return res.status(409).json({ error: 'Lo sentimos, el cupo está lleno.' });

    try {
      await query(
        `INSERT INTO inscripciones_clases (clase_id, usuario_id) VALUES ($1, $2)`,
        [clase.id, req.user.id]
      );
    } catch (e) {
      if (e.code === '23505') return res.status(409).json({ error: 'Ya estás inscrito en esta clase.' });
      throw e;
    }
    res.status(201).json({ ok: true, mensaje: '¡Inscripción confirmada! Nos vemos en la cancha.' });
  } catch (e) { next(e); }
};

// DELETE /api/clases/:id/inscribirse  (anular inscripción)
exports.abandonar = async (req, res, next) => {
  try {
    const filas = await query(
      `DELETE FROM inscripciones_clases WHERE clase_id = $1 AND usuario_id = $2 RETURNING id`,
      [req.params.id, req.user.id]
    );
    if (!filas.length) return res.status(404).json({ error: 'No estás inscrito en esta clase.' });
    res.json({ ok: true, mensaje: 'Inscripción cancelada.' });
  } catch (e) { next(e); }
};

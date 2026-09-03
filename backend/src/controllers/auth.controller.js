/* ============================================================
   CourtHub API · controllers/auth.controller.js
   Registro, login y perfil (roles: cliente | administrador | entrenador)
   ============================================================ */
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { query } = require('../config/db');
const { JWT_SECRET } = require('../middleware/auth');

/** Genera un JWT con id, email y rol (expira en 8 h). */
function firmar(usuario) {
  return jwt.sign({ id: usuario.id, email: usuario.email, rol: usuario.rol }, JWT_SECRET, { expiresIn: '8h' });
}

/** Proyección pública del usuario (nunca expone el hash). */
function publico(u) {
  return {
    id: u.id,
    nombre: u.nombre,
    apellido: u.apellido,
    email: u.email,
    rol: u.rol,
    nivel: u.nivel,
    deporte: u.deporte_favorito
  };
}

// POST /api/auth/register
exports.registrar = async (req, res, next) => {
  try {
    const { nombre, apellido = '', email, password, rol = 'cliente' } = req.body;
    if (!nombre || !email || !password) {
      return res.status(400).json({ error: 'Nombre, correo y contraseña son obligatorios.' });
    }
    if (String(password).length < 6) {
      return res.status(400).json({ error: 'La contraseña debe tener al menos 6 caracteres.' });
    }
    const rolSeguro = ['cliente', 'entrenador'].includes(rol) ? rol : 'cliente'; // admin solo por seed

    const existe = await query('SELECT 1 FROM usuarios WHERE email = $1', [String(email).toLowerCase()]);
    if (existe.length) return res.status(409).json({ error: 'Ya existe una cuenta con ese correo.' });

    const hash = await bcrypt.hash(password, 10);
    const filas = await query(
      `INSERT INTO usuarios (nombre, apellido, email, password_hash, rol)
       VALUES ($1, $2, $3, $4, $5)
       RETURNING id, nombre, apellido, email, rol, nivel, deporte_favorito`,
      [nombre, apellido, String(email).toLowerCase(), hash, rolSeguro]
    );
    const usuario = filas[0];
    res.status(201).json({ token: firmar(usuario), usuario: publico(usuario) });
  } catch (e) { next(e); }
};

// POST /api/auth/login
exports.login = async (req, res, next) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ error: 'Ingresa tu correo y contraseña.' });

    const filas = await query('SELECT * FROM usuarios WHERE email = $1 AND activo = TRUE', [String(email).toLowerCase()]);
    const usuario = filas[0];
    if (!usuario) return res.status(401).json({ error: 'Credenciales incorrectas.' });

    const ok = await bcrypt.compare(password, usuario.password_hash);
    if (!ok) return res.status(401).json({ error: 'Credenciales incorrectas.' });

    res.json({ token: firmar(usuario), usuario: publico(usuario) });
  } catch (e) { next(e); }
};

// GET /api/auth/me  (perfil del token)
exports.perfil = async (req, res, next) => {
  try {
    const filas = await query(
      'SELECT id, nombre, apellido, email, rol, nivel, deporte_favorito FROM usuarios WHERE id = $1',
      [req.user.id]
    );
    if (!filas.length) return res.status(404).json({ error: 'Usuario no encontrado.' });
    res.json({ usuario: publico(filas[0]) });
  } catch (e) { next(e); }
};

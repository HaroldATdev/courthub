/* ============================================================
   CourtHub API · middleware/auth.js — Autenticación JWT y roles
   ============================================================ */
const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'courthub-dev-secret';

/** Verifica el header "Authorization: Bearer <token>" y adjunta req.user. */
function autenticar(req, res, next) {
  const header = req.headers.authorization || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  if (!token) return res.status(401).json({ error: 'Token de autenticación requerido.' });
  try {
    req.user = jwt.verify(token, JWT_SECRET); // { id, email, rol }
    next();
  } catch {
    return res.status(401).json({ error: 'Token inválido o expirado.' });
  }
}

/** Restringe una ruta a roles específicos. Uso: autorizar('administrador'). */
function autorizar(...roles) {
  return (req, res, next) => {
    if (!req.user) return res.status(401).json({ error: 'No autenticado.' });
    if (roles.length && !roles.includes(req.user.rol)) {
      return res.status(403).json({ error: 'No tienes permisos para esta acción.' });
    }
    next();
  };
}

module.exports = { autenticar, autorizar, JWT_SECRET };

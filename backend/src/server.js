/* ============================================================
   CourtHub API · server.js — Punto de entrada (Express)
   ============================================================ */
require('dotenv').config();
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');

const app = express();

/* ---------- Middleware global ---------- */
app.use(cors());
app.use(express.json());
app.use(morgan('dev'));

/* ---------- Rutas ---------- */
app.get('/api/health', (req, res) =>
  res.json({ ok: true, servicio: 'courthub-api', ts: new Date().toISOString() })
);

app.use('/api/auth', require('./routes/auth.routes'));
app.use('/api/canchas', require('./routes/canchas.routes'));
app.use('/api/reservas', require('./routes/reservas.routes'));
app.use('/api/clases', require('./routes/clases.routes'));

/* ---------- Manejo de errores ---------- */
app.use((req, res) => res.status(404).json({ error: 'Ruta no encontrada.' }));

app.use((err, req, res, next) => { // eslint-disable-line no-unused-vars
  console.error('[API] Error:', err.message);
  res.status(err.status || 500).json({ error: err.status ? err.message : 'Error interno del servidor.' });
});

/* ---------- Arranque ---------- */
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`⚡ CourtHub API escuchando en http://localhost:${PORT}`));

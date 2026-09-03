/* ============================================================
   CourtHub API · config/db.js — Pool de conexiones PostgreSQL
   ============================================================ */
const { Pool } = require('pg');

const pool = new Pool({
  host: process.env.PGHOST || 'db',
  port: Number(process.env.PGPORT || 5432),
  user: process.env.PGUSER || 'courthub',
  password: process.env.PGPASSWORD || 'courthub',
  database: process.env.PGDATABASE || 'courthub',
  max: 10
});

/** Ejecuta una consulta parametrizada y devuelve las filas. */
async function query(text, params) {
  const res = await pool.query(text, params);
  return res.rows;
}

module.exports = { pool, query };

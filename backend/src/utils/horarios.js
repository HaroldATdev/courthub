/* ============================================================
   CourtHub API · utils/horarios.js — Bloques horarios de 1.5 h
   ============================================================ */

/** Bloques de inicio disponibles cada día (duración fija: 1.5 h). */
const BLOQUES = ['08:00', '09:30', '11:00', '12:30', '14:00', '15:30', '17:00', '18:30', '20:00', '21:30'];

/** Suma minutos a una hora "HH:MM" y devuelve "HH:MM". */
function sumarMinutos(hhmm, minutos) {
  const [h, m] = String(hhmm).split(':').map(Number);
  const total = h * 60 + m + minutos;
  const hh = Math.floor(total / 60);
  const mm = total % 60;
  return `${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`;
}

/** "HH:MM" a partir de un TIME de PostgreSQL ("18:00:00"). */
function horaCorta(h) {
  return String(h).slice(0, 5);
}

/** Fecha actual (hora de Santiago) en formato ISO yyyy-mm-dd. */
function hoyISO() {
  return new Date().toLocaleDateString('sv-SE', { timeZone: 'America/Santiago' });
}

module.exports = { BLOQUES, sumarMinutos, horaCorta, hoyISO };

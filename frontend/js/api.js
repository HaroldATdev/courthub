/* ============================================================
   CourtHub · api.js — Cliente HTTP con fallback automático a Mock
   Si el backend Node/Express está levantado usa la API real (/api);
   si no, responde con datos locales (demo 100% frontend sin servidor).
   ============================================================ */
(function () {
  var API_BASE = localStorage.getItem('ch_api_base') || '/api';
  var backendActivo = null; // null = sin determinar

  function token() { return localStorage.getItem('ch_token') || ''; }

  function http(path, opts) {
    opts = opts || {};
    var headers = { 'Content-Type': 'application/json' };
    var t = token();
    if (t) headers['Authorization'] = 'Bearer ' + t;
    if (opts.headers) Object.keys(opts.headers).forEach(function (k) { headers[k] = opts.headers[k]; });
    return fetch(API_BASE + path, { method: opts.method || 'GET', headers: headers, body: opts.body ? JSON.stringify(opts.body) : undefined })
      .then(function (res) {
        return res.json().catch(function () { return {}; }).then(function (data) {
          if (!res.ok) { var e = new Error((data && data.error) || ('HTTP ' + res.status)); e.status = res.status; throw e; }
          return data;
        });
      });
  }

  /** Intenta la API real; ante fallo de red resuelve con el mock local. */
  function call(path, opts) {
    if (backendActivo === false) return mock(path, opts);
    return http(path, opts).then(function (data) { backendActivo = true; return data; })
      .catch(function (e) {
        if (e.status === undefined) { backendActivo = false; return mock(path, opts); }
        throw e; // error real del backend (credenciales inválidas, conflicto de horario, etc.)
      });
  }
  /* ============================================================
     MOCK API — replica los endpoints del backend con datos locales
     ============================================================ */
  function fail(status, msg) { var e = new Error(msg); e.status = status; return e; }
  function hashStr(s) { var h = 0; for (var i = 0; i < s.length; i++) { h = ((h * 31) + s.charCodeAt(i)) >>> 0; } return h; }
  function addMin(hhmm, mins) { var p = hhmm.split(':'); var t = (+p[0]) * 60 + (+p[1]) + mins; var h = Math.floor(t / 60), m = t % 60; return (h < 10 ? '0' : '') + h + ':' + (m < 10 ? '0' : '') + m; }
  var BLOQUES = ['08:00', '09:30', '11:00', '12:30', '14:00', '15:30', '17:00', '18:30', '20:00', '21:30'];

  function usuarioDemo(email) { return { nombre: 'Gustavo', apellido: 'Diaz', email: (email || 'demo@courthub.com').toLowerCase(), rol: 'cliente', nivel: 'Oro', deporte: 'Pádel' }; }
  function canchaSnap(id) { var c = CH.canchaById(id); return c ? { id: c.id, nombre: c.nombre, tipo: c.tipo, comuna: c.comuna, imagen: c.imagen, subtipo: c.subtipo } : null; }

  function normReserva(r) {
    var cid = r.cancha_id !== undefined ? r.cancha_id : r.canchaId;
    return {
      id: r.id, codigo: r.codigo, canchaId: cid, cancha: r.cancha || canchaSnap(cid),
      fecha: String(r.fecha).slice(0, 10),
      horaInicio: r.hora_inicio || r.horaInicio, horaFin: r.hora_fin || r.horaFin,
      estado: r.estado, total: r.total, metodoPago: r.metodo_pago || r.metodoPago
    };
  }

  function mock(path, opts) {
    return Promise.resolve().then(function () {
      opts = opts || {};
      var method = (opts.method || 'GET').toUpperCase();
      var body = opts.body || {};
      var clean = path.split('?')[0];
      var seg = clean.split('/').filter(Boolean);
      var query = {};
      (path.split('?')[1] || '').split('&').forEach(function (kv) {
        if (kv) { var p = kv.split('='); query[decodeURIComponent(p[0])] = decodeURIComponent(p[1] || ''); }
      });

      /* ---------- AUTH ---------- */
      if (seg[0] === 'auth' && seg[1] === 'login' && method === 'POST') {
        if (!body.email || !body.password) throw fail(400, 'Ingresa tu correo y contraseña.');
        if (String(body.password).length < 6) throw fail(400, 'La contraseña debe tener al menos 6 caracteres.');
        return { token: 'mock-token-' + Date.now(), usuario: usuarioDemo(body.email) };
      }
      if (seg[0] === 'auth' && seg[1] === 'register' && method === 'POST') {
        if (!body.nombre || !body.email || !body.password) throw fail(400, 'Completa todos los campos.');
        if (String(body.password).length < 6) throw fail(400, 'La contraseña debe tener al menos 6 caracteres.');
        var partes = String(body.nombre).trim().split(/\s+/);
        return {
          token: 'mock-token-' + Date.now(),
          usuario: { nombre: partes[0], apellido: partes.slice(1).join(' ') || '', email: String(body.email).toLowerCase(), rol: body.rol || 'cliente', nivel: 'Bronce', deporte: 'Multi' }
        };
      }

      /* ---------- CANCHAS ---------- */
      if (seg[0] === 'canchas' && seg.length === 1) {
        var lista = CH.Mock.CANCHAS.slice();
        if (query.tipo && query.tipo !== 'todos') lista = lista.filter(function (c) { return c.tipo === query.tipo; });
        if (query.q) { var q = query.q.toLowerCase(); lista = lista.filter(function (c) { return (c.nombre + ' ' + c.comuna + ' ' + c.tipo).toLowerCase().indexOf(q) !== -1; }); }
        if (query.precioMax) lista = lista.filter(function (c) { return c.precioHora <= +query.precioMax; });
        if (query.precioMin) lista = lista.filter(function (c) { return c.precioHora >= +query.precioMin; });
        return { canchas: lista };
      }
      if (seg[0] === 'canchas' && seg[1] && seg[2] === 'disponibilidad') {
        var fecha = query.fecha || hoyISO();
        var reservas = CH.Store.get('ch_reservas', []);
        var esPasada = fecha < hoyISO();
        return { fecha: fecha, bloques: BLOQUES.map(function (h, i) {
          var ocupado = reservas.some(function (r) { return String(r.canchaId) === seg[1] && r.fecha === fecha && r.horaInicio === h && r.estado !== 'cancelada'; });
          if (!ocupado && !esPasada && hashStr(seg[1] + fecha + i) % 10 < 3) ocupado = true; // disponibilidad simulada determinista
          return { horaInicio: h, horaFin: addMin(h, 90), ocupado: ocupado };
        }) };
      }
      if (seg[0] === 'canchas' && seg[1]) {
        var c = CH.canchaById(seg[1]);
        if (!c) throw fail(404, 'Cancha no encontrada.');
        return { cancha: c };
      }
      /* ---------- RESERVAS ---------- */
      if (seg[0] === 'reservas' && seg.length === 1 && method === 'GET') {
        var rs = CH.Store.get('ch_reservas', []).map(normReserva);
        rs.sort(function (a, b) { return a.fecha < b.fecha ? 1 : -1; });
        return { reservas: rs };
      }
      if (seg[0] === 'reservas' && seg.length === 1 && method === 'POST') {
        var hoy = hoyISO();
        if (!body.cancha_id || !body.fecha || !body.hora_inicio) throw fail(400, 'Faltan datos de la reserva.');
        if (body.fecha < hoy) throw fail(400, 'No puedes reservar en una fecha pasada.');
        var lista = CH.Store.get('ch_reservas', []);
        var choque = lista.some(function (r) {
          return String(r.canchaId) === String(body.cancha_id) && r.fecha === body.fecha && r.estado !== 'cancelada' &&
            r.horaInicio < body.hora_fin && body.hora_inicio < r.horaFin;
        });
        if (choque) throw fail(409, 'Ese bloque horario ya no está disponible. Elige otro.');
        var cancha = CH.canchaById(body.cancha_id);
        if (!cancha) throw fail(404, 'Cancha no encontrada.');
        var durH = 1.5; // bloques de 1.5 horas de juego
        var subtotal = Math.round(cancha.precioHora * durH * 100) / 100;
        var cargo = Math.round(subtotal * 0.10 * 100) / 100;
        var nuevo = {
          id: Date.now(), codigo: 'CH-' + String(hashStr(body.fecha + body.hora_inicio) % 90000 + 10000),
          canchaId: cancha.id, fecha: body.fecha, horaInicio: body.hora_inicio, horaFin: body.hora_fin,
          estado: 'confirmada', subtotal: subtotal, cargo: cargo, total: subtotal + cargo,
          metodoPago: body.metodo_pago || 'Tarjeta de Crédito'
        };
        lista.push(nuevo);
        CH.Store.set('ch_reservas', lista);
        return { reserva: normReserva(nuevo) };
      }
      if (seg[0] === 'reservas' && seg[2] === 'cancelar' && method === 'PATCH') {
        var listaC = CH.Store.get('ch_reservas', []);
        var rC = listaC.find(function (r) { return String(r.id) === seg[1]; });
        if (!rC) throw fail(404, 'Reserva no encontrada.');
        rC.estado = 'cancelada';
        CH.Store.set('ch_reservas', listaC);
        return { reserva: normReserva(rC) };
      }
      if (seg[0] === 'reservas' && seg[2] === 'reprogramar' && method === 'PATCH') {
        var listaR = CH.Store.get('ch_reservas', []);
        var rR = listaR.find(function (r) { return String(r.id) === seg[1]; });
        if (!rR) throw fail(404, 'Reserva no encontrada.');
        if (body.fecha < hoyISO()) throw fail(400, 'La nueva fecha no puede ser pasada.');
        rR.fecha = body.fecha; rR.horaInicio = body.hora_inicio; rR.horaFin = body.hora_fin;
        CH.Store.set('ch_reservas', listaR);
        return { reserva: normReserva(rR) };
      }
      /* ---------- CLASES ---------- */
      if (seg[0] === 'clases' && seg.length === 1 && method === 'GET') {
        var insc = CH.Store.get('ch_inscripciones', []);
        var cls = CH.Mock.CLASES.map(function (k) {
          var cancha = CH.canchaById(k.canchaId);
          return {
            id: k.id, nombre: k.nombre, deporte: k.deporte, nivel: k.nivel, precio: k.precio,
            entrenador: { nombre: k.entrenador }, cancha: { id: cancha.id, nombre: cancha.nombre, comuna: cancha.comuna },
            fecha: k.fecha, horaInicio: k.horaInicio, horaFin: k.horaFin,
            cupoMax: k.cupoMax, inscritos: k.inscritos, inscrito: insc.indexOf(k.id) !== -1
          };
        });
        if (query.deporte && query.deporte !== 'todos') cls = cls.filter(function (k) { return k.deporte === query.deporte; });
        return { clases: cls };
      }
      if (seg[0] === 'clases' && seg[2] === 'inscribirse' && method === 'POST') {
        var clase = CH.Mock.CLASES.find(function (k) { return String(k.id) === seg[1]; });
        if (!clase) throw fail(404, 'Clase no encontrada.');
        var ins = CH.Store.get('ch_inscripciones', []);
        if (ins.indexOf(clase.id) !== -1) throw fail(409, 'Ya estás inscrito en esta clase.');
        if (clase.inscritos >= clase.cupoMax) throw fail(409, 'Lo sentimos, el cupo está lleno.');
        clase.inscritos += 1;
        ins.push(clase.id);
        CH.Store.set('ch_inscripciones', ins);
        return { ok: true, mensaje: '¡Inscripción confirmada! Nos vemos en la cancha.', claseId: clase.id, inscritos: clase.inscritos };
      }
      if (seg[0] === 'clases' && seg[2] === 'inscribirse' && method === 'DELETE') {
        var claseD = CH.Mock.CLASES.find(function (k) { return String(k.id) === seg[1]; });
        var insD = CH.Store.get('ch_inscripciones', []);
        var idx = insD.indexOf(claseD.id);
        if (idx === -1) throw fail(404, 'No estás inscrito en esta clase.');
        insD.splice(idx, 1);
        claseD.inscritos = Math.max(0, claseD.inscritos - 1);
        CH.Store.set('ch_inscripciones', insD);
        return { ok: true, mensaje: 'Inscripción cancelada.' };
      }

      throw fail(404, 'Recurso no encontrado en modo demo: ' + clean);
    });
  }

  /** API pública usada por las páginas. */
  CH.api = {
    get: function (p) { return call(p); },
    post: function (p, body) { return call(p, { method: 'POST', body: body }); },
    patch: function (p, body) { return call(p, { method: 'PATCH', body: body }); },
    del: function (p, body) { return call(p, { method: 'DELETE', body: body }); },
    bloques: BLOQUES,
    normalizarReserva: normReserva
  };
})();




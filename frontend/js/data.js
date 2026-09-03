/* ============================================================
   CourtHub · data.js — Datos de demostración (mock) + persistencia local
   Se usan cuando el backend no está disponible (modo demo del frontend).
   ============================================================ */
(function () {
  /* ---------- Imágenes (Unsplash con fallback automático) ---------- */
  var IMG = {
    futbol: 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1000&q=70',
    futbolIndoor: 'https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=1000&q=70',
    padel: 'https://images.unsplash.com/photo-1606924842618-bdb0a07582f7?auto=format&fit=crop&w=1000&q=70',
    tenis: 'https://images.unsplash.com/photo-1595435742656-5272d0b3fa82?auto=format&fit=crop&w=1000&q=70',
    squash: 'https://images.unsplash.com/photo-1587280501635-68a0e82cd5ff?auto=format&fit=crop&w=1000&q=70',
    hero: 'https://images.unsplash.com/photo-1522778119026-d647f0596c20?auto=format&fit=crop&w=1600&q=70'
  };

  var hoy = new Date();
  /** Devuelve una fecha ISO (yyyy-mm-dd) desplazada N días desde hoy. */
  function d(offset) {
    var x = new Date(hoy);
    x.setDate(x.getDate() + offset);
    return x.toISOString().slice(0, 10);
  }

  var SERVICIOS = ['Estacionamiento', 'Duchas de agua caliente', 'Iluminación LED', 'Vestuarios', 'Cafetería & Bar', 'WiFi gratuito'];
  var REGLAMENTO = [
    'Se solicita llegar con 10 minutos de anticipación al bloque reservado.',
    'Usar calzado adecuado para la superficie de la cancha. Prohibido el uso de tacos de aluminio en césped sintético.',
    'Cancelaciones permitidas hasta con 24 horas de antelación para reembolso completo en créditos CourtHub.'
  ];
  /* ---------- Canchas (9 · las 6 del dashboard según el prototipo + 3 históricas) ---------- */
  var CANCHAS = [
    { id: 1, nombre: 'Club Pádel San Damian', tipo: 'padel', subtipo: 'Pádel · Indoor', comuna: 'Las Condes', direccion: 'Av. Apoquindo 4500, Las Condes, Santiago', precioHora: 105, calificacion: 4.9, opiniones: 124, verificada: true, imagen: IMG.padel, servicios: SERVICIOS, reglamento: REGLAMENTO, descripcion: 'Canchas de pádel indoor de última generación con cristal templado y césped Mondo. El club cuenta con academia propia, tienda especializada y zonas de descanso para antes y después del partido.' },
    { id: 2, nombre: 'Complejo Deportivo El Roble', tipo: 'futbol', subtipo: 'Fútbol 7 & 11', comuna: 'Providencia', direccion: 'Providencia, Santiago (A pasos del Metro Pedro de Valdivia)', precioHora: 95, calificacion: 4.8, opiniones: 124, verificada: true, imagen: IMG.futbol, servicios: SERVICIOS, descripcion: 'Nuestra cancha principal en el Complejo Deportivo El Roble cuenta con césped sintético de última generación a nivel profesional, ideal para partidos de fútbol 7. La iluminación LED de alta potencia garantiza una visibilidad óptima durante los bloques nocturnos. El club dispone de camarines equipados, estacionamiento vigilado y una excelente cafetería para el tercer tiempo.', reglamento: [
      'Se solicita llegar con 10 minutos de anticipación al bloque reservado.',
      'Usar calzado adecuado para césped sintético (multitaco o capellada de fútbol). Prohibido estoperoles o tacos.',
      'Cancelaciones permitidas hasta con 24 horas de antelación para reembolso completo en créditos CourtHub.'
    ] },
    { id: 3, nombre: 'Club de Tenis Central', tipo: 'tenis', subtipo: 'Arcilla · Outdoor', comuna: 'Vitacura', direccion: 'Av. Kennedy 5600, Vitacura, Santiago', precioHora: 85, calificacion: 4.8, opiniones: 56, verificada: true, imagen: IMG.tenis, servicios: SERVICIOS, reglamento: REGLAMENTO, descripcion: 'Canchas de arcilla batida en perfecto estado, sede de torneos regionales. Incluye técnico de suelo, academia de tenis para todos los niveles y arriendo de raquetas.' },
    { id: 4, nombre: 'Arena Squash Pro', tipo: 'squash', subtipo: 'Vidrio · Indoor', comuna: 'Santiago Centro', direccion: 'San Antonio 220, Santiago Centro', precioHora: 70, calificacion: 4.6, opiniones: 38, verificada: true, imagen: IMG.squash, servicios: SERVICIOS, reglamento: REGLAMENTO, descripcion: 'Canchas profesionales de squash con pared trasera de vidrio e iluminación uniforme sin sombras. Sede de ligas amateurs y clases particulares con entrenadores certificados.' },
    { id: 5, nombre: 'Pádel Premium Lo Barnechea', tipo: 'padel', subtipo: 'Pádel · Panorámico', comuna: 'Lo Barnechea', direccion: 'Av. La Dehesa 1200, Lo Barnechea, Santiago', precioHora: 130, calificacion: 4.9, opiniones: 210, verificada: true, imagen: IMG.padel, servicios: SERVICIOS, reglamento: REGLAMENTO, descripcion: 'Pistas panorámicas de pádel con la mejor vista de la cordillera. Césped de última generación, palas de arriendo premium y terraza con vista para el después del partido.' },
    { id: 6, nombre: 'Gimnasio Polideportivo Oriente', tipo: 'futbol', subtipo: 'Fútbol · Indoor', comuna: 'La Reina', direccion: 'Av. Príncipe de Gales 9500, La Reina, Santiago', precioHora: 110, calificacion: 4.5, opiniones: 47, verificada: true, imagen: IMG.futbolIndoor, servicios: SERVICIOS, reglamento: REGLAMENTO, descripcion: 'Polideportivo techado con cancha de fútbol indoor de piso flotante. Ideal para partidos nocturnos sin preocuparse del clima, con gradas para espectadores.' },
    { id: 7, nombre: 'Club Deportivo Central', tipo: 'tenis', subtipo: 'Cemento · Outdoor', comuna: 'Ñuñoa', direccion: 'Av. Irarrázaval 3400, Ñuñoa, Santiago', precioHora: 85, calificacion: 4.7, opiniones: 92, verificada: true, imagen: IMG.tenis, servicios: SERVICIOS, reglamento: REGLAMENTO, descripcion: 'Canchas de cemento sintético con iluminación nueva, ubicadas en pleno corazón de Ñuñoa. Arriendo por hora con pelotas incluidas.' },
    { id: 8, nombre: 'Cancha Municipal Norte', tipo: 'futbol', subtipo: 'Fútbol 7 · Outdoor', comuna: 'Recoleta', direccion: 'Calle El Arquillo 500, Recoleta, Santiago', precioHora: 75, calificacion: 4.4, opiniones: 63, verificada: true, imagen: IMG.futbol, servicios: SERVICIOS, reglamento: REGLAMENTO, descripcion: 'Cancha municipal renovada con césped sintético nuevo y camarines. Precios accesibles para partidos semanales con amigos.' },
    { id: 9, nombre: 'Pádel Center Sur', tipo: 'padel', subtipo: 'Pádel · Indoor', comuna: 'San Joaquín', direccion: 'Av. Vicuña Mackenna 4800, San Joaquín, Santiago', precioHora: 105, calificacion: 4.6, opiniones: 58, verificada: true, imagen: IMG.padel, servicios: SERVICIOS, reglamento: REGLAMENTO, descripcion: 'Centro de pádel con 4 canchas indoor, muros de cristal de 12 mm y escuela para niños y adultos.' }
  ];
  /* ---------- Clases deportivas (dictadas por entrenadores) ---------- */
  var CLASES = [
    { id: 1, nombre: 'Pádel Inicial: Golpes Básicos', deporte: 'padel', entrenador: 'Carla Rojas', canchaId: 1, fecha: d(2), horaInicio: '19:00', horaFin: '20:30', cupoMax: 8, inscritos: 5, precio: 55, nivel: 'Principiante' },
    { id: 2, nombre: 'Clínica de Fútbol 7: Definición', deporte: 'futbol', entrenador: 'Pedro Muñoz', canchaId: 2, fecha: d(3), horaInicio: '10:00', horaFin: '11:30', cupoMax: 14, inscritos: 9, precio: 45, nivel: 'Intermedio' },
    { id: 3, nombre: 'Squash para Principiantes', deporte: 'squash', entrenador: 'Pedro Muñoz', canchaId: 4, fecha: d(4), horaInicio: '08:00', horaFin: '09:30', cupoMax: 4, inscritos: 2, precio: 38, nivel: 'Principiante' },
    { id: 4, nombre: 'Tenis Intermedio: Derecha y Revés', deporte: 'tenis', entrenador: 'Carla Rojas', canchaId: 3, fecha: d(5), horaInicio: '17:00', horaFin: '18:30', cupoMax: 6, inscritos: 6, precio: 65, nivel: 'Intermedio' },
    { id: 5, nombre: 'Fulbito Amistoso + Táctica', deporte: 'futbol', entrenador: 'Carla Rojas', canchaId: 6, fecha: d(6), horaInicio: '20:00', horaFin: '21:30', cupoMax: 16, inscritos: 11, precio: 30, nivel: 'Todos los niveles' }
  ];

  /* ---------- Reservas de ejemplo (historial del usuario demo, como el prototipo) ---------- */
  var SEED_RESERVAS = [
    { id: 1, codigo: 'CH-84512', canchaId: 2, fecha: d(2),   horaInicio: '18:00', horaFin: '19:00', estado: 'confirmada', subtotal: 95, cargo: 0, total: 95, metodoPago: 'Tarjeta de Crédito' },
    { id: 2, codigo: 'CH-84513', canchaId: 4, fecha: d(4),   horaInicio: '10:00', horaFin: '11:00', estado: 'pendiente',  subtotal: 70, cargo: 0, total: 70, metodoPago: 'Webpay' },
    { id: 3, codigo: 'CH-84514', canchaId: 7, fecha: d(6),   horaInicio: '16:00', horaFin: '17:00', estado: 'confirmada', subtotal: 85, cargo: 0, total: 85, metodoPago: 'Tarjeta de Crédito' },
    { id: 4, codigo: 'CH-81007', canchaId: 8, fecha: d(-9),  horaInicio: '20:00', horaFin: '21:00', estado: 'completada', subtotal: 75, cargo: 0, total: 75, metodoPago: 'PayPal' },
    { id: 5, codigo: 'CH-80233', canchaId: 9, fecha: d(-12), horaInicio: '14:00', horaFin: '15:00', estado: 'cancelada',  subtotal: 105, cargo: 0, total: 105, metodoPago: 'Tarjeta de Crédito' }
  ];

  /* ---------- Etiquetas de deportes ---------- */
  var DEPORTES = { todos: 'Todos', futbol: 'Fútbol', tenis: 'Tenis', squash: 'Squash', padel: 'Pádel' };

  /* ---------- Persistencia local (localStorage) ---------- */
  var Store = {
    get: function (key, fallback) {
      try { var v = JSON.parse(localStorage.getItem(key)); return v === null || v === undefined ? fallback : v; }
      catch (e) { return fallback; }
    },
    set: function (key, value) { localStorage.setItem(key, JSON.stringify(value)); }
  };

  if (!Store.get('ch_reservas')) Store.set('ch_reservas', SEED_RESERVAS);
  if (!Store.get('ch_inscripciones')) Store.set('ch_inscripciones', []);

  CH.Mock = { CANCHAS: CANCHAS, CLASES: CLASES, IMG: IMG, DEPORTES: DEPORTES, fechaOffset: d, hoy: hoy };
  CH.Store = Store;
  CH.canchaById = function (id) {
    for (var i = 0; i < CANCHAS.length; i++) { if (String(CANCHAS[i].id) === String(id)) return CANCHAS[i]; }
    return null;
  };
})();




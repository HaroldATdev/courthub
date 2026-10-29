-- ============================================================
-- CourtHub · 02-seed.sql — Datos de ejemplo (coinciden con el prototipo)
-- Contraseña de TODOS los usuarios demo: Password123
-- Login por usuario (admin, gdiaz, crojas, pmunoz, asoto, lperez) o por correo. Todos en estado 'activo'.
-- ============================================================

-- ---------- USUARIOS ----------
INSERT INTO usuarios (nombre, apellido, email, password_hash, rol, telefono, nivel, deporte_favorito, dni, usuario, estado) VALUES
  ('Admin',     'Sistema', 'admin@courthub.com',     crypt('Password123', gen_salt('bf', 10)), 'administrador', '+51 9 5555 0001', 'Oro',  'Multi', '10000001', 'admin', 'activo'),
  ('Gustavo',   'Diaz',    'demo@courthub.com',      crypt('Password123', gen_salt('bf', 10)), 'cliente',       '+51 9 5555 0002', 'Oro',  'Pádel', '10000002', 'gdiaz', 'activo'),
  ('Carla',     'Rojas',   'entrenador@courthub.com',crypt('Password123', gen_salt('bf', 10)), 'entrenador',    '+51 9 5555 0003', 'Oro',  'Tenis', '10000003', 'crojas', 'activo'),
  ('Pedro',     'Muñoz',   'pmunoz@courthub.com',    crypt('Password123', gen_salt('bf', 10)), 'entrenador',    '+51 9 5555 0004', 'Plata','Fútbol', '10000004', 'pmunoz', 'activo'),
  ('Ana',       'Soto',    'ana@courthub.com',       crypt('Password123', gen_salt('bf', 10)), 'cliente',       '+51 9 5555 0005', 'Plata','Squash', '10000005', 'asoto', 'activo'),
  ('Luis',      'Perez',   'luis@courthub.com',      crypt('Password123', gen_salt('bf', 10)), 'cliente',       '+51 9 5555 0006', 'Bronce','Fútbol', '10000006', 'lperez', 'activo');

-- ---------- CANCHAS ----------
INSERT INTO canchas (nombre, tipo, subtipo, descripcion, direccion, comuna, precio_hora, calificacion, num_opiniones, imagen_url, verificada, servicios, reglamento) VALUES
  ('Club Pádel San Damian', 'padel', 'Pádel · Indoor',
   'Canchas de pádel indoor de última generación con cristal templado y césped Mondo. Academia propia, tienda especializada y zonas de descanso.',
   'Av. Larco 851, Miraflores', 'Miraflores', 105, 4.9, 124,
   'https://images.unsplash.com/photo-1606924842618-bdb0a07582f7?auto=format&fit=crop&w=1000&q=70', TRUE,
   ARRAY['Estacionamiento','Duchas de agua caliente','Iluminación LED','Vestuarios','Cafetería & Bar','WiFi gratuito'],
   'Se solicita llegar con 10 minutos de anticipación. Cancelaciones hasta 24 horas antes para reembolso en créditos CourtHub.'),
  ('Complejo Deportivo El Roble', 'futbol', 'Fútbol 7 & 11',
   'Cancha principal con césped sintético de última generación a nivel profesional, ideal para fútbol 7. Iluminación LED de alta potencia, camarines equipados, estacionamiento vigilado y cafetería.',
   'San Isidro (A pasos del Metropolitano, Est. Javier Prado)', 'San Isidro', 95, 4.8, 124,
   'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1000&q=70', TRUE,
   ARRAY['Estacionamiento','Duchas de agua caliente','Iluminación LED','Vestuarios','Cafetería & Bar','WiFi gratuito'],
   '• Se solicita llegar con 10 minutos de anticipación al bloque reservado.
• Usar calzado adecuado para césped sintético (multitaco o capellada de fútbol). Prohibido estoperoles o tacos.
• Cancelaciones permitidas hasta con 24 horas de antelación para reembolso completo en créditos CourtHub.'),
  ('Club de Tenis Central', 'tenis', 'Arcilla · Outdoor',
   'Canchas de arcilla batida en perfecto estado, sede de torneos regionales. Academia de tenis para todos los niveles y arriendo de raquetas.',
   'Av. Benavides 725, La Molina', 'La Molina', 85, 4.8, 56,
   'https://images.unsplash.com/photo-1595435742656-5272d0b3fa82?auto=format&fit=crop&w=1000&q=70', TRUE,
   ARRAY['Estacionamiento','Duchas de agua caliente','Iluminación LED','Vestuarios','Cafetería & Bar','WiFi gratuito'],
   'Se solicita llegar con 10 minutos de anticipación. Cancelaciones hasta 24 horas antes.'),
  ('Arena Squash Pro', 'squash', 'Vidrio · Indoor',
   'Canchas profesionales de squash con pared trasera de vidrio e iluminación uniforme. Ligas amateurs y clases con entrenadores certificados.',
   'Jr. de la Unión 450, Cercado de Lima', 'Cercado de Lima', 70, 4.6, 38,
   'https://images.unsplash.com/photo-1587280501635-68a0e82cd5ff?auto=format&fit=crop&w=1000&q=70', TRUE,
   ARRAY['Duchas de agua caliente','Iluminación LED','Vestuarios','Cafetería & Bar','WiFi gratuito'],
   'Se solicita llegar con 10 minutos de anticipación. Uso obligatorio de lentes protectores.'),
  ('Pádel Premium Surco', 'padel', 'Pádel · Panorámico',
   'Pistas panorámicas con vista a la cordillera. Césped de última generación, palas de arriendo premium y terraza.',
   'Av. El Polo 780, Santiago de Surco', 'Santiago de Surco', 130, 4.9, 210,
   'https://images.unsplash.com/photo-1606924842618-bdb0a07582f7?auto=format&fit=crop&w=1000&q=70', TRUE,
   ARRAY['Estacionamiento','Duchas de agua caliente','Iluminación LED','Vestuarios','Cafetería & Bar','WiFi gratuito'],
   'Se solicita llegar con 10 minutos de anticipación. Cancelaciones hasta 24 horas antes.'),
  ('Gimnasio Polideportivo Oriente', 'futbol', 'Fútbol · Indoor',
   'Polideportivo techado con cancha de fútbol indoor de piso flotante y gradas para espectadores.',
   'Av. Aviación 3200, San Borja', 'San Borja', 110, 4.5, 47,
   'https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=1000&q=70', TRUE,
   ARRAY['Estacionamiento','Duchas de agua caliente','Iluminación LED','Vestuarios','WiFi gratuito'],
   'Se solicita llegar con 10 minutos de anticipación. Prohibido calzado con taco.'),
  ('Club Deportivo Central', 'tenis', 'Cemento · Outdoor',
   'Canchas de cemento sintético con iluminación nueva en pleno Jesús María. Pelotas incluidas.',
   'Av. Salaverry 2570, Jesús María', 'Jesús María', 85, 4.7, 92,
   'https://images.unsplash.com/photo-1595435742656-5272d0b3fa82?auto=format&fit=crop&w=1000&q=70', TRUE,
   ARRAY['Estacionamiento','Duchas de agua caliente','Iluminación LED','Vestuarios','Cafetería & Bar'],
   'Se solicita llegar con 10 minutos de anticipación. Cancelaciones hasta 24 horas antes.'),
  ('Cancha Municipal Norte', 'futbol', 'Fútbol 7 · Outdoor',
   'Cancha municipal renovada con césped sintético nuevo y camarines.',
   'Jr. Junín 580, Rímac', 'Rímac', 75, 4.4, 63,
   'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=1000&q=70', TRUE,
   ARRAY['Estacionamiento','Iluminación LED','Vestuarios'],
   'Se solicita llegar con 10 minutos de anticipación.'),
  ('Pádel Center Sur', 'padel', 'Pádel · Indoor',
   'Centro de pádel con 4 canchas indoor, muros de cristal de 12 mm y escuela para niños y adultos.',
   'Av. Defensores del Morro 1500, Chorrillos', 'Chorrillos', 105, 4.6, 58,
   'https://images.unsplash.com/photo-1606924842618-bdb0a07582f7?auto=format&fit=crop&w=1000&q=70', TRUE,
   ARRAY['Estacionamiento','Duchas de agua caliente','Iluminación LED','Vestuarios','WiFi gratuito'],
   'Se solicita llegar con 10 minutos de anticipación. Cancelaciones hasta 24 horas antes.');

-- ---------- DISPONIBILIDAD (todas las canchas, todos los días 08:00-23:00) ----------
INSERT INTO disponibilidad_canchas (cancha_id, dia_semana, hora_inicio, hora_fin)
SELECT c.id, d::smallint, '08:00'::time, '23:00'::time
FROM canchas c CROSS JOIN generate_series(0, 6) AS d;
-- ---------- RESERVAS DEL USUARIO DEMO (como el prototipo) ----------
-- usuario demo = id 2 (Gustavo Diaz)
INSERT INTO reservas (usuario_id, cancha_id, fecha, hora_inicio, hora_fin, estado, subtotal, cargo_servicio, total, metodo_pago) VALUES
  (2, 2, CURRENT_DATE + 2,  '18:00', '19:00', 'confirmada', 95, 0, 95, 'Tarjeta de Crédito'),
  (2, 4, CURRENT_DATE + 4,  '10:00', '11:00', 'pendiente',  70, 0, 70, 'Webpay'),
  (2, 7, CURRENT_DATE + 6,  '16:00', '17:00', 'confirmada', 85, 0, 85, 'Tarjeta de Crédito'),
  (2, 8, CURRENT_DATE - 9,  '20:00', '21:00', 'completada', 75, 0, 75, 'PayPal'),
  (2, 9, CURRENT_DATE - 12, '14:00', '15:00', 'cancelada',  105, 0, 105, 'Tarjeta de Crédito');

-- ---------- CLASES DEPORTIVAS ----------
-- entrenadoras/entrenadores: Carla Rojas = id 3 · Pedro Muñoz = id 4
INSERT INTO clases (nombre, deporte, nivel, descripcion, entrenador_id, cancha_id, fecha, hora_inicio, hora_fin, cupo_maximo, precio) VALUES
  ('Pádel Inicial: Golpes Básicos',        'padel', 'Principiante',     'Aprende la técnica de derecha, revés y saque desde cero.',                3, 1, CURRENT_DATE + 2, '19:00', '20:30',  8, 55),
  ('Clínica de Fútbol 7: Definición',      'futbol','Intermedio',       'Ejercicios de finalización, potenciación y juego en espacios reducidos.', 4, 2, CURRENT_DATE + 3, '10:00', '11:30', 14, 45),
  ('Squash para Principiantes',            'squash','Principiante',     'Fundamentos del squash: agarre, drive, revés y desplazamiento.',          4, 4, CURRENT_DATE + 4, '08:00', '09:30',  4, 38),
  ('Tenis Intermedio: Derecha y Revés',    'tenis', 'Intermedio',       'Consistencia y potencia en golpes básicos con peloteo dirigido.',         3, 3, CURRENT_DATE + 5, '17:00', '18:30',  6, 65),
  ('Fulbito Amistoso + Táctica',           'futbol','Todos los niveles','Partido dirigido con feedback táctico en tiempo real.',                   3, 6, CURRENT_DATE + 6, '20:00', '21:30', 16,  30);

-- ---------- INSCRIPCIONES DE EJEMPLO ----------
-- Ana (id 5) y Luis (id 6) ya están anotados en algunas clases
INSERT INTO inscripciones_clases (clase_id, usuario_id) VALUES
  (1, 5), (1, 6),
  (2, 6),
  (3, 5);



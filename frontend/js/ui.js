/* ============================================================
   CourtHub · ui.js — Utilidades de interfaz, sesión y layout común
   ============================================================ */
(function () {
  /* ---------- Formato ---------- */
  CH.fmtPEN = function (n) { return 'S/ ' + Number(n || 0).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); };
  CH.cap = function (s) { return s ? s.charAt(0).toUpperCase() + s.slice(1) : s; };
  CH.hoyISO = function () { var d = new Date(); return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'); };
  CH.fmtFecha = function (iso, opts) {
    if (!iso) return '—';
    var dt = new Date(String(iso).slice(0, 10) + 'T12:00:00');
    return CH.cap(dt.toLocaleDateString('es-PE', opts || { weekday: 'long', day: 'numeric', month: 'short' }));
  };
  /** Fallback de imagen si Unsplash falla (usa picsum con semilla determinista). */
  CH.imgFallback = function (img, seed) {
    img.onerror = null;
    img.src = 'https://picsum.photos/seed/' + (seed || 'courthub') + '/900/560';
  };

  /* ---------- Sesión / usuario ---------- */
  CH.user = function () { try { return JSON.parse(localStorage.getItem('ch_user')); } catch (e) { return null; } };
  CH.setUser = function (usuario, token) { localStorage.setItem('ch_user', JSON.stringify(usuario)); if (token) localStorage.setItem('ch_token', token); };
  CH.logout = function () { localStorage.removeItem('ch_user'); localStorage.removeItem('ch_token'); location.href = 'index.html'; };
  CH.requireAuth = function () { var u = CH.user(); if (!u) location.replace('index.html'); return u; };
  CH.iniciales = function (u) { u = u || {}; return ((u.nombre || 'U').charAt(0) + (u.apellido || '').charAt(0)).toUpperCase(); };

  /* ---------- Etiquetas reutilizables ---------- */
  CH.sportTag = function (tipo) { return '<span class="tag">' + ((CH.Mock && CH.Mock.DEPORTES[tipo]) || tipo) + '</span>'; };
  CH.estadoBadge = function (estado) { return '<span class="badge badge-' + estado + '">' + CH.cap(estado) + '</span>'; };

  /* ---------- Toast ---------- */
  CH.toast = function (msg, tipo) {
    tipo = tipo || 'success';
    var cont = document.getElementById('ch-toast');
    if (!cont) { cont = document.createElement('div'); cont.id = 'ch-toast'; cont.className = 'fixed bottom-6 right-6 z-[70] flex flex-col gap-2 items-end pointer-events-none'; document.body.appendChild(cont); }
    var border = { success: 'border-primary-500', error: 'border-red-400', info: 'border-blue-400' }[tipo] || 'border-primary-500';
    var iconName = { success: 'checkCircle', error: 'xCircle', info: 'info' }[tipo] || 'checkCircle';
    var iconColor = { success: 'text-primary-500', error: 'text-red-400', info: 'text-blue-400' }[tipo] || 'text-primary-500';
    var el = document.createElement('div');
    el.className = 'fade-up pointer-events-auto flex items-center gap-3 bg-white border border-gray-100 border-l-4 ' + border + ' rounded-xl shadow-lg px-4 py-3 text-sm font-medium text-slate-700 max-w-xs';
    el.innerHTML = CH.icon(iconName, 'w-5 h-5 shrink-0 ' + iconColor) + '<span>' + msg + '</span>';
    cont.appendChild(el);
    setTimeout(function () {
      el.style.transition = 'opacity .3s, transform .3s';
      el.style.opacity = '0';
      el.style.transform = 'translateY(8px)';
      setTimeout(function () { el.remove(); }, 320);
    }, 3600);
  };
  /* ---------- Header / navegación ---------- */
  CH.renderHeader = function (activo) {
    var u = CH.user() || { nombre: 'Invitado', apellido: '', email: 'Conéctate desde el login', nivel: 'Bronce', deporte: 'Multi' };
    var q = new URLSearchParams(location.search).get('q') || '';
    var links = [
      { id: 'canchas', href: 'dashboard.html', label: 'Canchas' },
      { id: 'clases', href: 'clases.html', label: 'Clases' },
      { id: 'reservas', href: 'mis-reservas.html', label: 'Mis Reservas' }
    ];
    var nav = links.map(function (l) {
      return '<a href="' + l.href + '" class="transition ' + (l.id === activo ? 'text-primary-600 font-semibold' : 'text-slate-500 hover:text-slate-800') + '">' + l.label + '</a>';
    }).join('');

    var host = document.getElementById('ch-header');
    if (!host) return;
    host.innerHTML =
      '<header class="sticky top-0 z-40 bg-white/95 backdrop-blur border-b border-gray-100">' +
        '<div class="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center gap-3 sm:gap-4">' +
          '<a href="dashboard.html" class="flex items-center gap-2.5 shrink-0">' + CH.logoHTML() +
            '<span class="font-display font-bold text-[21px] tracking-tight text-slate-900">CourtHub</span></a>' +
          '<form id="ch-search-form" class="hidden md:flex flex-1 justify-center px-2 lg:px-8">' +
            '<div class="relative w-full max-w-md">' +
              '<span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">' + CH.icon('search', 'w-[17px] h-[17px]') + '</span>' +
              '<input name="q" value="' + q + '" placeholder="Buscar canchas, clubes o deportes..." autocomplete="off" ' +
                'class="w-full h-11 rounded-full bg-gray-100/90 border border-transparent focus:border-primary-300 focus:bg-white pl-11 pr-4 text-sm outline-none transition placeholder:text-gray-400">' +
            '</div>' +
          '</form>' +
          '<nav class="hidden lg:flex items-center gap-6 text-sm ml-auto">' + nav + '</nav>' +
          '<button id="ch-bell" title="Notificaciones" class="relative w-10 h-10 rounded-full grid place-items-center text-slate-500 hover:bg-gray-100 transition shrink-0">' +
            CH.icon('bell', 'w-5 h-5') +
            '<span class="absolute top-2 right-2.5 w-2 h-2 rounded-full bg-primary-500 ring-2 ring-white"></span>' +
          '</button>' +
          '<span class="hidden sm:block w-px h-6 bg-gray-200 shrink-0"></span>' +
          '<div class="relative shrink-0">' +
            '<button id="ch-profile-btn" class="flex items-center gap-2.5 rounded-full hover:bg-gray-50 pl-0.5 pr-1.5 py-1 transition">' +
              '<span class="w-9 h-9 rounded-full bg-primary-100 text-primary-700 grid place-items-center text-xs font-bold">' + CH.iniciales(u) + '</span>' +
              '<span class="hidden sm:block text-left leading-tight">' +
                '<span class="block text-sm font-semibold text-slate-800">' + (u.nombre || '') + ' ' + (u.apellido || '') + '</span>' +
                '<span class="block text-[11px] text-primary-600 font-medium">Nivel ' + (u.nivel || 'Bronce') + ' • ' + (u.deporte || 'Multi') + '</span>' +
              '</span>' +
            '</button>' +
            '<div id="ch-profile-menu" class="hidden absolute right-0 mt-2 w-60 bg-white border border-gray-100 rounded-2xl shadow-xl p-2 z-50">' +
              '<div class="px-3 py-2 border-b border-gray-50 mb-1">' +
                '<p class="text-sm font-semibold text-slate-800 truncate">' + (u.nombre || '') + ' ' + (u.apellido || '') + '</p>' +
                '<p class="text-xs text-gray-400 truncate">' + (u.email || '') + '</p>' +
              '</div>' +
              '<a href="mis-reservas.html" class="flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm text-slate-600 hover:bg-gray-50">' + CH.icon('calendar', 'w-4 h-4') + 'Mis Reservas</a>' +
              '<a href="clases.html" class="flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm text-slate-600 hover:bg-gray-50">' + CH.icon('activity', 'w-4 h-4') + 'Clases Deportivas</a>' +
              '<button id="ch-logout" class="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm text-red-500 hover:bg-red-50 mt-1 border-t border-gray-50 pt-2.5">' + CH.icon('logout', 'w-4 h-4') + 'Cerrar sesión</button>' +
            '</div>' +
          '</div>' +
        '</div>' +
      '</header>';

    host.querySelector('#ch-search-form').addEventListener('submit', function (e) {
      e.preventDefault();
      var v = this.q.value.trim();
      location.href = 'dashboard.html' + (v ? '?q=' + encodeURIComponent(v) : '');
    });
    host.querySelector('#ch-bell').addEventListener('click', function () { CH.toast('No tienes notificaciones nuevas.', 'info'); });
    var btn = host.querySelector('#ch-profile-btn'), menu = host.querySelector('#ch-profile-menu');
    btn.addEventListener('click', function (e) { e.stopPropagation(); menu.classList.toggle('hidden'); });
    document.addEventListener('click', function () { menu.classList.add('hidden'); });
    menu.addEventListener('click', function (e) { e.stopPropagation(); });
    host.querySelector('#ch-logout').addEventListener('click', CH.logout);
  };
  /* ---------- Footer ---------- */
  CH.renderFooter = function () {
    var host = document.getElementById('ch-footer');
    if (!host) return;
    host.innerHTML = '<footer class="border-t border-gray-100 bg-white mt-6"><div class="max-w-6xl mx-auto px-4 sm:px-6 py-6 flex flex-col sm:flex-row items-center justify-between gap-3 text-sm text-gray-400">' +
      '<div class="flex items-center gap-2">' + CH.logoHTML('w-6 h-6') + '<span class="font-display font-bold text-slate-600">CourtHub</span></div>' +
      '<p>© ' + new Date().getFullYear() + ' CourtHub — Proyecto universitario · Marcos de Desarrollo Web</p></div></footer>';
  };

  /* ---------- Modales ---------- */
  CH.openModal = function (id) { document.getElementById(id).classList.add('open'); };
  CH.closeModal = function (id) { document.getElementById(id).classList.remove('open'); };
  CH.wireModals = function () {
    document.querySelectorAll('.ch-modal').forEach(function (m) {
      m.addEventListener('click', function (e) { if (e.target === m) m.classList.remove('open'); });
      m.querySelectorAll('[data-close]').forEach(function (b) { b.addEventListener('click', function () { m.classList.remove('open'); }); });
    });
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') document.querySelectorAll('.ch-modal.open').forEach(function (m) { m.classList.remove('open'); });
    });
  };
})();




/* ============================================================
   CourtHub · ui.js — Utilidades de interfaz (toasts, modales,
   menú de perfil, fallback de imágenes). El HTML lo renderiza
   el servidor (Thymeleaf); aquí solo hay comportamiento.
   ============================================================ */
window.CH = window.CH || {};
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

  /** Fallback de imagen si Unsplash falla (picsum con semilla determinista). */
  CH.imgFallback = function (img, seed) {
    img.onerror = null;
    img.src = 'https://picsum.photos/seed/' + (seed || 'courthub') + '/900/560';
  };

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
    el.innerHTML = CH.icon(iconName, 'w-5 h-5 shrink-0 ' + iconColor) + '<span></span>';
    el.querySelector('span').textContent = msg;
    cont.appendChild(el);
    setTimeout(function () {
      el.style.transition = 'opacity .3s, transform .3s';
      el.style.opacity = '0';
      el.style.transform = 'translateY(8px)';
      setTimeout(function () { el.remove(); }, 320);
    }, 4000);
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

  /* ---------- Comportamiento común al cargar ---------- */
  document.addEventListener('DOMContentLoaded', function () {
    // Mensaje flash del servidor (RedirectAttributes → ${toast})
    var flash = document.getElementById('ch-flash');
    if (flash && flash.textContent.trim()) CH.toast(flash.textContent.trim(), flash.getAttribute('data-tipo') || 'success');

    // Menú de perfil del header
    var btn = document.getElementById('ch-profile-btn'), menu = document.getElementById('ch-profile-menu');
    if (btn && menu) {
      btn.addEventListener('click', function (e) { e.stopPropagation(); menu.classList.toggle('hidden'); });
      document.addEventListener('click', function () { menu.classList.add('hidden'); });
      menu.addEventListener('click', function (e) { e.stopPropagation(); });
    }
    var bell = document.getElementById('ch-bell');
    if (bell) bell.addEventListener('click', function () { CH.toast('No tienes notificaciones nuevas.', 'info'); });

    // Fallback de imágenes: <img data-fallback="semilla">
    document.querySelectorAll('img[data-fallback]').forEach(function (img) {
      img.addEventListener('error', function () { CH.imgFallback(img, img.getAttribute('data-fallback')); });
    });

    // Confirmaciones: <form data-confirm="¿Seguro?">
    document.querySelectorAll('form[data-confirm]').forEach(function (f) {
      f.addEventListener('submit', function (e) { if (!confirm(f.getAttribute('data-confirm'))) e.preventDefault(); });
    });

    // Selects que envían su formulario al cambiar
    document.querySelectorAll('[data-autosubmit]').forEach(function (el) {
      el.addEventListener('change', function () { el.form.submit(); });
    });

    CH.wireModals();
  });
})();

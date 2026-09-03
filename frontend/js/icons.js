/* ============================================================
   CourtHub · icons.js — Íconos SVG inline (trazos estilo Feather/Lucide, MIT)
   Uso: CH.icon('nombre', 'w-5 h-5')
   ============================================================ */
window.CH = window.CH || {};

CH.ICONS = {
  logo: '<rect x="3" y="5" width="18" height="14" rx="2.5"/><line x1="12" y1="5" x2="12" y2="19"/><circle cx="12" cy="12" r="2.6"/>',
  mail: '<rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-10 6L2 7"/>',
  lock: '<rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>',
  eye: '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>',
  eyeOff: '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>',
  search: '<circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>',
  bell: '<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>',
  star: '<polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>',
  pin: '<path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>',
  arrowRight: '<line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>',
  arrowLeft: '<line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>',
  calendar: '<rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>',
  clock: '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>',
  check: '<polyline points="20 6 9 17 4 12"/>',
  checkCircle: '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>',
  x: '<line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>',
  xCircle: '<circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>'
};
CH.ICONS.users = '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>';
CH.ICONS.shieldCheck = '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><polyline points="9 11.5 11 13.5 15 9.5"/>';
CH.ICONS.zap = '<polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>';
CH.ICONS.wifi = '<path d="M5 12.55a11 11 0 0 1 14.08 0"/><path d="M1.42 9a16 16 0 0 1 21.16 0"/><path d="M8.53 16.11a6 6 0 0 1 6.95 0"/><line x1="12" y1="20" x2="12.01" y2="20"/>';
CH.ICONS.droplet = '<path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/>';
CH.ICONS.car = '<path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/><circle cx="7" cy="17" r="2"/><path d="M9 17h6"/><circle cx="17" cy="17" r="2"/>';
CH.ICONS.coffee = '<path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/><line x1="6" y1="1" x2="6" y2="4"/><line x1="10" y1="1" x2="10" y2="4"/><line x1="14" y1="1" x2="14" y2="4"/>';
CH.ICONS.ruler = '<path d="M21.3 15.3a2.4 2.4 0 0 1 0 3.4l-2.6 2.6a2.4 2.4 0 0 1-3.4 0L2.7 8.7a2.4 2.4 0 0 1 0-3.4l2.6-2.6a2.4 2.4 0 0 1 3.4 0Z"/><path d="m14.5 12.5 2-2"/><path d="m11.5 9.5 2-2"/><path d="m8.5 6.5 2-2"/><path d="m17.5 15.5 2-2"/>';
CH.ICONS.leaf = '<path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z"/><path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"/>';
CH.ICONS.info = '<circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>';
CH.ICONS.card = '<rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/>';
CH.ICONS.wallet = '<path d="M21 12V7H5a2 2 0 0 1 0-4h14v4"/><path d="M3 5v14a2 2 0 0 0 2 2h16v-5"/><path d="M18 12a2 2 0 0 0 0 4h4v-4Z"/>';
CH.ICONS.chevronLeft = '<polyline points="15 18 9 12 15 6"/>';
CH.ICONS.chevronRight = '<polyline points="9 18 15 12 9 6"/>';
CH.ICONS.user = '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>';
CH.ICONS.logout = '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>';
CH.ICONS.activity = '<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>';

/** Devuelve un ícono SVG inline. */
CH.icon = function (name, cls) {
  cls = cls || 'w-5 h-5';
  if (name === 'google') {
    return '<svg class="' + cls + '" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.27-4.74 3.27-8.1z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09A6.9 6.9 0 0 1 5.49 12c0-.73.13-1.43.35-2.09V7.07H2.18A10.94 10.94 0 0 0 1 12c0 1.78.43 3.45 1.18 4.93l3.66-2.84z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84C6.71 7.31 9.14 5.38 12 5.38z"/></svg>';
  }
  if (name === 'apple') {
    return '<svg class="' + cls + '" viewBox="0 0 24 24" fill="currentColor"><path d="M16.365 1.43c0 1.14-.47 2.29-1.27 3.13-.86.92-2.24 1.63-3.36 1.54-.14-1.1.43-2.28 1.22-3.12.85-.9 2.3-1.6 3.41-1.55zm3.7 16.53c-.6 1.34-.9 1.94-1.68 3.13-1.09 1.67-2.63 3.75-4.54 3.77-1.7.02-2.13-1.1-4.44-1.09-2.3.01-2.79 1.11-4.49 1.1-1.9-.02-3.36-1.9-4.45-3.57C-1.53 16.6-1.85 10.4 1.02 7.08c1.03-1.2 2.65-1.99 4.16-1.99 1.75 0 2.85 1.1 4.3 1.1 1.4 0 2.25-1.1 4.28-1.1 1.34 0 2.94.75 4.04 2.03-3.55 1.98-2.98 7.02.25 8.84z"/></svg>';
  }
  var path = CH.ICONS[name] || CH.ICONS.info;
  return '<svg class="' + cls + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + path + '</svg>';
};

/** Ícono del logotipo (escudo de cancha) sobre fondo redondeado. */
CH.logoHTML = function (size) {
  size = size || 'w-9 h-9';
  return '<span class="' + size + ' rounded-[10px] bg-primary-500 grid place-items-center text-white shadow-[0_4px_12px_-4px_rgba(16,185,129,.7)]">' + CH.icon('logo', 'w-[18px] h-[18px]') + '</span>';
};

/** Reemplaza los <span data-icon="nombre" data-cls="..."> del HTML estático por el SVG. */
CH.hydrateIcons = function (root) {
  (root || document).querySelectorAll('[data-icon]').forEach(function (el) {
    el.innerHTML = CH.icon(el.getAttribute('data-icon'), el.getAttribute('data-cls') || 'w-5 h-5');
    el.removeAttribute('data-icon');
  });
};
document.addEventListener('DOMContentLoaded', function () { CH.hydrateIcons(); });



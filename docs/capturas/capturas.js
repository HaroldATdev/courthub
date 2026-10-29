// Capturas del tercer avance de CourtHub (automatiza Chrome en modo headless)
const puppeteer = require('puppeteer-core');
const path = require('path');
const fs = require('fs');

const BASE = process.env.BASE || 'http://localhost:8090';
const OUT = process.env.OUT || path.resolve('capturas');
const CHROME = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
fs.mkdirSync(OUT, { recursive: true });

const RND = String(Date.now()).slice(-5);
const NUEVO = { dni: '7' + RND.padStart(7, '0'), usuario: 'mperez' + RND, email: `mperez${RND}@gmail.com`, nombres: 'María', apellidos: 'Pérez Quispe' };

let n = 0;
async function shot(page, nombre, opts = {}) {
  n++;
  const file = path.join(OUT, `${String(n).padStart(2, '0')}-${nombre}.png`);
  await new Promise(r => setTimeout(r, opts.wait ?? 400));
  await page.screenshot({ path: file, fullPage: opts.fullPage ?? false });
  console.log('✔', path.basename(file));
}
async function setTab(page, registro) {
  await page.evaluate(r => document.getElementById(r ? 'tab-registro' : 'tab-login').click(), registro);
}
async function fill(page, campos) {
  for (const [sel, val] of Object.entries(campos)) {
    await page.$eval(sel, el => { el.value = ''; });
    await page.type(sel, val);
  }
}
async function login(page, usuario, clave) {
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle0' });
  await fill(page, { '#login-usuario': usuario, '#login-pass': clave });
  await Promise.all([page.waitForNavigation({ waitUntil: 'networkidle0' }), page.click('#form-login button[type=submit]')]);
}
/** Muestra un <select> como lista abierta (size = nº de opciones) para que la captura enseñe las opciones de la BD. */
async function abrirSelect(page, sel) {
  await page.$eval(sel, s => { s.size = s.options.length; s.style.height = 'auto'; s.style.backgroundImage = 'none'; });
}

(async () => {
  const browser = await puppeteer.launch({ executablePath: CHROME, headless: true, args: ['--no-sandbox', '--lang=es-PE'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 860, deviceScaleFactor: 1 });

  // 1) Página de acceso
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle0' });
  await shot(page, 'login-pagina-de-acceso');

  // 2) Formulario de registro (enlace al pie del login)
  await page.evaluate(() => document.getElementById('link-registro').click());
  await shot(page, 'registro-formulario-vacio');
  await fill(page, { '#reg-dni': '10000002', '#reg-usuario': NUEVO.usuario, '#reg-nombres': NUEVO.nombres, '#reg-apellidos': NUEVO.apellidos, '#reg-email': NUEVO.email, '#reg-pass': 'Clave123', '#reg-pass2': 'Clave123' });
  await shot(page, 'registro-formulario-lleno');
  // 2a) DNI repetido (10000002 = Gustavo Diaz)
  await Promise.all([page.waitForNavigation({ waitUntil: 'networkidle0' }), page.click('#form-registro button[type=submit]')]);
  await shot(page, 'registro-validacion-dni-repetido');
  // 2b) usuario repetido (gdiaz)
  await setTab(page, true);
  await fill(page, { '#reg-dni': NUEVO.dni, '#reg-usuario': 'gdiaz', '#reg-pass': 'Clave123', '#reg-pass2': 'Clave123' });
  await Promise.all([page.waitForNavigation({ waitUntil: 'networkidle0' }), page.click('#form-registro button[type=submit]')]);
  await shot(page, 'registro-validacion-usuario-repetido');
  // registro correcto → vuelve al login con aviso
  await setTab(page, true);
  await fill(page, { '#reg-dni': NUEVO.dni, '#reg-usuario': NUEVO.usuario, '#reg-pass': 'Clave123', '#reg-pass2': 'Clave123' });
  await Promise.all([page.waitForNavigation({ waitUntil: 'networkidle0' }), page.click('#form-registro button[type=submit]')]);
  await shot(page, 'registro-exitoso-cuenta-pendiente');

  // 3) La cuenta pendiente no puede entrar
  await fill(page, { '#login-usuario': NUEVO.usuario, '#login-pass': 'Clave123' });
  await Promise.all([page.waitForNavigation({ waitUntil: 'networkidle0' }), page.click('#form-login button[type=submit]')]);
  await shot(page, 'login-cuenta-pendiente-bloqueada');

  // Login como administrador → redirige al panel ERP
  await login(page, 'admin', 'Password123');
  await shot(page, 'login-admin-redirige-panel-erp');

  // 4) CRUD de usuarios
  await page.goto(`${BASE}/admin/usuarios?estado=pendiente`, { waitUntil: 'networkidle0' });
  await shot(page, 'admin-usuarios-pendientes');
  await page.goto(`${BASE}/admin/usuarios?q=${NUEVO.usuario}`, { waitUntil: 'networkidle0' });
  await Promise.all([page.waitForNavigation({ waitUntil: 'networkidle0' }), page.click('form button[value=activo]')]);
  await shot(page, 'admin-usuario-activado', { wait: 600 });
  await page.goto(`${BASE}/admin/usuarios`, { waitUntil: 'networkidle0' });
  await shot(page, 'admin-usuarios-listado-crud', { fullPage: true });
  const editar = await page.$eval(`a[href*="/admin/usuarios/"][href$="/editar"]`, a => a.getAttribute('href'));
  await page.goto(`${BASE}${editar}`, { waitUntil: 'networkidle0' });
  await shot(page, 'admin-usuario-editar-formulario', { fullPage: true });
  await page.goto(`${BASE}/admin/usuarios/nuevo`, { waitUntil: 'networkidle0' });
  await abrirSelect(page, '#estado');
  await shot(page, 'admin-usuario-nuevo-estados', { fullPage: true });

  // 5) Listas desplegables cargadas desde tablas de la BD
  await page.goto(`${BASE}/admin/canchas/nueva`, { waitUntil: 'networkidle0' });
  await abrirSelect(page, '#tipo');
  await shot(page, 'admin-cancha-select-deporte-desde-bd', { fullPage: true });
  await page.goto(`${BASE}/admin/clases/nueva`, { waitUntil: 'networkidle0' });
  await abrirSelect(page, '#deporte');
  await abrirSelect(page, '#nivel');
  await shot(page, 'admin-clase-selects-deporte-y-nivel-desde-bd', { fullPage: true });

  // Login del usuario activado → redirige al dashboard de cliente
  await page.goto(`${BASE}/logout`, { waitUntil: 'networkidle0' });
  await login(page, NUEVO.usuario, 'Clave123');
  await shot(page, 'login-cliente-activado-redirige-dashboard');

  await browser.close();
  console.log('Listo:', n, 'capturas en', OUT);
})().catch(e => { console.error('ERROR', e); process.exit(1); });

// Recorrido automático de la demo con Brave (puppeteer-core). Requiere: npm i puppeteer-core · Brave instalado · la app en http://localhost:8090
// Recorrido de la exposición de CourtHub con Brave (puppeteer-core controla el ejecutable de Brave con un perfil temporal).
// Uso: BASE=http://localhost:8090 OUT=<carpeta> node expo.js            (flujo completo, pasarela simulada)
//      FASE=2fa MAILS=<mails.log> node expo.js                          (solo el segundo factor, con Brevo simulado)
const puppeteer = require('puppeteer-core');
const path = require('path');
const fs = require('fs');
const os = require('os');
const { execSync } = require('child_process');

const BASE = process.env.BASE || 'http://localhost:8090';
const OUT = process.env.OUT || path.resolve('capturas');
const FASE = process.env.FASE || 'completo';
const PROYECTO = process.env.PROYECTO || 'C:\\Users\\Usuario\\Downloads\\courthub';
const BRAVE = 'C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe';
fs.mkdirSync(OUT, { recursive: true });

const RND = String(Date.now()).slice(-6);
const NUEVO = { dni: '7' + RND.padStart(7, '0'), usuario: 'mperez' + RND, email: `mperez${RND}@gmail.com`, nombres: 'María', apellidos: 'Pérez Quispe', clave: 'ClaveDemo123' };
const pausa = ms => new Promise(r => setTimeout(r, ms));
let n = Number(process.env.START || 0);
const fallos = [];

async function shot(page, nombre, full = false) {
  n++;
  await pausa(700);
  const file = path.join(OUT, `${String(n).padStart(2, '0')}-${nombre}.png`);
  await page.screenshot({ path: file, fullPage: full });
  console.log('  📸', path.basename(file));
}
async function paso(titulo, fn) {
  console.log('▶', titulo);
  try { await fn(); } catch (e) { console.log('  ✖ FALLÓ:', e.message.split('\n')[0]); fallos.push(titulo + ': ' + e.message.split('\n')[0]); }
}
async function clickNav(page, sel) {
  await Promise.all([page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 30000 }), page.click(sel)]);
}
async function ir(page, ruta) { await page.goto(BASE + ruta, { waitUntil: 'networkidle2', timeout: 30000 }); }
async function escribir(page, sel, texto) {
  await page.$eval(sel, el => { el.value = ''; });
  await page.type(sel, texto, { delay: 25 });
}
async function login(page, usuario, clave) {
  await ir(page, '/login');
  await escribir(page, '#login-usuario', usuario);
  await escribir(page, '#login-pass', clave);
  await clickNav(page, '#form-login button[type=submit]');
}
async function logout(page) { await ir(page, '/logout'); }
async function textoPagina(page) { return page.evaluate(() => document.body.innerText); }
async function esperarTexto(page, texto) {
  const t = await textoPagina(page);
  if (!t.toLowerCase().includes(texto.toLowerCase())) throw new Error(`no aparece "${texto}"`);
}
async function boletaPdf(browser, href, nombre) {
  const tab = await browser.newPage();
  await tab.setViewport({ width: 1100, height: 1000 });
  try { await tab.goto(href, { waitUntil: 'networkidle2', timeout: 20000 }); } catch (e) { /* el visor de PDF puede abortar la navegación */ }
  await pausa(1500);
  await shot(tab, nombre);
  await tab.close();
}
async function pagarConTarjeta(page, numero) {
  await escribir(page, '#t-nombre', 'María Pérez');
  await escribir(page, '#t-numero', numero);
  await escribir(page, '#t-venc', '1230');
  await escribir(page, '#t-cvv', '123');
  await page.click('#terminos');
}

(async () => {
  const browser = await puppeteer.launch({
    executablePath: BRAVE, headless: false, defaultViewport: null, slowMo: 90,
    userDataDir: fs.mkdtempSync(path.join(os.tmpdir(), 'brave-expo-')),
    args: ['--window-size=1400,950', '--no-first-run', '--no-default-browser-check', '--disable-features=Translate']
  });
  const page = (await browser.pages())[0];
  await page.setViewport({ width: 1366, height: 860 });
  const cuerpoMisReservas = []; // href de boletas

  if (FASE === '2fa') {
    await paso('2FA · login con usuario y clave pide el código', async () => {
      await login(page, 'crojas', process.env.CLAVE || 'Password123');
      await esperarTexto(page, 'Verifica tu identidad');
      await shot(page, '2fa-pantalla-codigo');
    });
    await paso('2FA · código incorrecto', async () => {
      await escribir(page, '#codigo', '000000');
      await clickNav(page, 'form[action$="/login/verificar"] button[type=submit]');
      await esperarTexto(page, 'Código incorrecto');
      await shot(page, '2fa-codigo-incorrecto');
    });
    await paso('2FA · código correcto → ambiente del entrenador', async () => {
      const lineas = fs.readFileSync(process.env.MAILS, 'utf8').trim().split('\n').map(l => JSON.parse(l));
      const ultimo = lineas.filter(l => l.body.to[0].email === 'entrenador@courthub.com').pop();
      const codigo = ultimo.body.subject.match(/(\d{6})/)[1];
      console.log('  ✉ correo recibido por Brevo (simulado), asunto:', ultimo.body.subject);
      await escribir(page, '#codigo', codigo);
      await clickNav(page, 'form[action$="/login/verificar"] button[type=submit]');
      await esperarTexto(page, 'Ambiente del entrenador');
      await shot(page, '2fa-acceso-concedido');
    });
    await browser.close();
    console.log(fallos.length ? '\n✖ Fallos: ' + fallos.length : '\n✔ 2FA sin fallos');
    process.exit(fallos.length ? 1 : 0);
  }

  /* ===================== CLIENTE ===================== */
  await paso('Cliente · pantalla de login', async () => { await ir(page, '/login'); await shot(page, 'login'); });

  await paso('Cliente · registro con DNI repetido', async () => {
    await page.click('#tab-registro');
    await escribir(page, '#reg-dni', '10000002');            // DNI de gdiaz
    await escribir(page, '#reg-usuario', NUEVO.usuario);
    await escribir(page, '#reg-nombres', NUEVO.nombres);
    await escribir(page, '#reg-apellidos', NUEVO.apellidos);
    await escribir(page, '#reg-email', NUEVO.email);
    await escribir(page, '#reg-pass', NUEVO.clave);
    await escribir(page, '#reg-pass2', NUEVO.clave);
    await clickNav(page, '#form-registro button[type=submit]');
    await esperarTexto(page, 'ya está registrado');
    await shot(page, 'registro-dni-repetido');
  });
  await paso('Cliente · registro con usuario repetido', async () => {
    await escribir(page, '#reg-dni', NUEVO.dni);
    await escribir(page, '#reg-usuario', 'gdiaz');
    await escribir(page, '#reg-pass', NUEVO.clave);
    await escribir(page, '#reg-pass2', NUEVO.clave);
    await clickNav(page, '#form-registro button[type=submit]');
    await esperarTexto(page, 'ya está en uso');
    await shot(page, 'registro-usuario-repetido');
  });
  await paso('Cliente · registro exitoso (cuenta activa)', async () => {
    await escribir(page, '#reg-usuario', NUEVO.usuario);
    await escribir(page, '#reg-pass', NUEVO.clave);
    await escribir(page, '#reg-pass2', NUEVO.clave);
    await clickNav(page, '#form-registro button[type=submit]');
    await esperarTexto(page, 'ya está activa');
    await shot(page, 'registro-exitoso-activa');
  });
  await paso('Cliente · entra a su dashboard', async () => {
    await login(page, NUEVO.usuario, NUEVO.clave);
    await esperarTexto(page, 'Reservar');
    await shot(page, 'cliente-dashboard');
  });
  await paso('Cliente · detalle de cancha, calendario y bloque', async () => {
    await clickNav(page, 'a[href^="/canchas/"]');
    // elige un día del calendario unos días después de hoy (o el último disponible del mes)
    const objetivo = new Date(Date.now() + 4 * 864e5).toISOString().slice(0, 10);
    const dias = await page.$$eval('#cal-grid button[data-fecha]:not([disabled])', bs => bs.map(b => b.dataset.fecha));
    const elegido = dias.find(d => d >= objetivo) || dias[dias.length - 1];
    if (elegido) { await page.click(`#cal-grid button[data-fecha="${elegido}"]`); await pausa(900); }
    await shot(page, 'cliente-detalle-cancha');
  });
  await paso('Cliente · pago con tarjeta rechazada', async () => {
    await clickNav(page, '#btn-continuar');
    await esperarTexto(page, 'Datos de Pago');
    await pagarConTarjeta(page, '4000040000000008');
    await clickNav(page, '#btn-pagar');
    await esperarTexto(page, 'Pago rechazado');
    await shot(page, 'cliente-pago-rechazado');
  });
  await paso('Cliente · pago aprobado → Mis reservas', async () => {
    await escribir(page, '#t-numero', '4111111111111111');
    await escribir(page, '#t-cvv', '123');
    if (!(await page.$eval('#terminos', e => e.checked))) await page.click('#terminos');
    await clickNav(page, '#btn-pagar');
    await esperarTexto(page, 'Mis Reservas');
    await shot(page, 'cliente-reserva-confirmada');
  });
  await paso('Cliente · boleta en PDF de la reserva', async () => {
    await ir(page, '/mis-reservas?tab=todas');
    const href = await page.$eval('a[href$="/boleta"]', a => a.href);
    await boletaPdf(browser, href, 'cliente-boleta-reserva-pdf');
  });
  await paso('Cliente · clases y pago de inscripción', async () => {
    await ir(page, '/clases');
    await shot(page, 'cliente-clases');
    await clickNav(page, 'a[href*="/pago?"], a[href*="/pago"]');
    await esperarTexto(page, 'Pago de la clase');
    await shot(page, 'cliente-pago-clase');
    await pagarConTarjeta(page, '5111111111111118');
    await clickNav(page, '#btn-pagar');
    await esperarTexto(page, 'Boleta PDF');
    await shot(page, 'cliente-clase-inscrito');
  });
  await paso('Cliente · boleta en PDF de la clase', async () => {
    const href = await page.$eval('a[href$="/boleta"]', a => a.href);
    await boletaPdf(browser, href, 'cliente-boleta-clase-pdf');
  });
  await logout(page);

  /* ===================== ENTRENADOR ===================== */
  await paso('Entrenador · entra a su ambiente', async () => {
    await login(page, 'crojas', 'Password123');
    await esperarTexto(page, 'Ambiente del entrenador');
    await shot(page, 'entrenador-panel', true);
  });
  await paso('Entrenador · alumnos y asistencia', async () => {
    await clickNav(page, 'a[href^="/entrenador/clases/"]');
    await esperarTexto(page, 'Alumnos de la clase');
    await shot(page, 'entrenador-alumnos');
    const btn = await page.$('form[action*="asistencia"] button');
    if (btn) {
      await Promise.all([page.waitForNavigation({ waitUntil: 'networkidle2' }), btn.click()]);
      await esperarTexto(page, 'Asistió');
      await shot(page, 'entrenador-asistencia-marcada');
    }
  });
  await logout(page);

  /* ===================== ADMINISTRADOR ===================== */
  await paso('Admin · panel ERP', async () => {
    await login(page, 'admin', 'Password123');
    await esperarTexto(page, 'Panel ERP');
    await shot(page, 'admin-resumen', true);
  });
  await paso('Admin · reservas (con boleta y estados)', async () => {
    await ir(page, '/admin/reservas');
    await shot(page, 'admin-reservas', true);
  });
  await paso('Admin · reporte de ventas por mes y año', async () => {
    await ir(page, '/admin/ventas');
    await esperarTexto(page, 'Reporte de ventas');
    await shot(page, 'admin-ventas-reporte', true);
  });
  await paso('Admin · usuarios (cuentas activas)', async () => {
    await ir(page, '/admin/usuarios');
    await shot(page, 'admin-usuarios', true);
  });
  await paso('Admin · nueva clase con listas desde la BD', async () => {
    await ir(page, '/admin/clases/nueva');
    await shot(page, 'admin-clase-nueva');
  });
  await logout(page);

  /* ===================== RECUPERAR CONTRASEÑA ===================== */
  await paso('Recuperar contraseña por correo', async () => {
    await ir(page, '/olvide-clave');
    await escribir(page, '#identificador', NUEVO.usuario);
    await clickNav(page, 'form[action$="/olvide-clave"] button[type=submit]');
    await esperarTexto(page, 'Si la cuenta existe');
    await shot(page, 'recuperar-clave-enviado');
    await pausa(2500);
    const logs = execSync('docker compose logs app', { cwd: PROYECTO, encoding: 'utf8', maxBuffer: 1e8 });
    const enlaces = [...logs.matchAll(/Enlace de recuperación para [^:]+: (http\S+restablecer\?token=[A-Za-z0-9_-]+)/g)];
    if (!enlaces.length) throw new Error('no apareció el enlace en el log (¿hay proveedor de correo configurado?)');
    const enlace = enlaces[enlaces.length - 1][1].replace(/^https?:\/\/[^/]+/, BASE);
    await page.goto(enlace, { waitUntil: 'networkidle2' });
    await esperarTexto(page, 'Nueva contraseña');
    await shot(page, 'recuperar-clave-formulario');
    await escribir(page, '#password', 'NuevaClave456');
    await escribir(page, '#password2', 'NuevaClave456');
    await clickNav(page, 'form[action$="/restablecer"] button[type=submit]');
    await esperarTexto(page, 'Contraseña actualizada');
    await shot(page, 'recuperar-clave-listo');
  });

  await browser.close();
  console.log(fallos.length ? `\n✖ Pasos con fallo (${fallos.length}):\n - ` + fallos.join('\n - ') : '\n✔ Recorrido completo sin fallos');
  process.exit(fallos.length ? 1 : 0);
})().catch(e => { console.error('Error fatal:', e); process.exit(2); });

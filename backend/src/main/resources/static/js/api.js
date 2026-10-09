/* ============================================================
   CourtHub · api.js — Cliente HTTP mínimo para la API REST (/api)
   La sesión viaja en la cookie HttpOnly CH_TOKEN; el header
   X-Requested-With habilita la cookie en /api (protección CSRF).
   ============================================================ */
window.CH = window.CH || {};
(function () {
  function http(path, opts) {
    opts = opts || {};
    return fetch('/api' + path, {
      method: opts.method || 'GET',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json', 'X-Requested-With': 'fetch' },
      body: opts.body ? JSON.stringify(opts.body) : undefined
    }).then(function (res) {
      return res.json().catch(function () { return {}; }).then(function (data) {
        if (!res.ok) { var e = new Error((data && data.error) || ('HTTP ' + res.status)); e.status = res.status; throw e; }
        return data;
      });
    });
  }
  CH.api = {
    get: function (p) { return http(p); },
    post: function (p, body) { return http(p, { method: 'POST', body: body }); },
    patch: function (p, body) { return http(p, { method: 'PATCH', body: body }); },
    del: function (p, body) { return http(p, { method: 'DELETE', body: body }); }
  };
})();

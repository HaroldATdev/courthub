/* CourtHub API · routes/auth.routes.js */
const router = require('express').Router();
const controller = require('../controllers/auth.controller');
const { autenticar } = require('../middleware/auth');

router.post('/register', controller.registrar);
router.post('/login', controller.login);
router.get('/me', autenticar, controller.perfil);

module.exports = router;

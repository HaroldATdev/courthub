/* CourtHub API · routes/canchas.routes.js */
const router = require('express').Router();
const controller = require('../controllers/canchas.controller');
const { autenticar } = require('../middleware/auth');

router.get('/', autenticar, controller.listar);
router.get('/:id', autenticar, controller.detalle);
router.get('/:id/disponibilidad', autenticar, controller.disponibilidad);

module.exports = router;

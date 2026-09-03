/* CourtHub API · routes/reservas.routes.js */
const router = require('express').Router();
const controller = require('../controllers/reservas.controller');
const { autenticar } = require('../middleware/auth');

router.use(autenticar); // todas las rutas de reservas requieren sesión

router.get('/', controller.listar);
router.post('/', controller.crear);
router.patch('/:id/cancelar', controller.cancelar);
router.patch('/:id/reprogramar', controller.reprogramar);

module.exports = router;

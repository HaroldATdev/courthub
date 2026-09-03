/* CourtHub API · routes/clases.routes.js */
const router = require('express').Router();
const controller = require('../controllers/clases.controller');
const { autenticar } = require('../middleware/auth');

router.use(autenticar);

router.get('/', controller.listar);
router.post('/:id/inscribirse', controller.inscribirse);
router.delete('/:id/inscribirse', controller.abandonar);

module.exports = router;

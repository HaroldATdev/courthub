package com.courthub.config;

import com.courthub.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;

/**
 * Carga los datos de demostración (db/seed.sql) la primera vez que arranca la aplicación,
 * es decir, solo cuando la tabla "usuarios" está vacía. El esquema (db/schema.sql) lo
 * ejecuta Spring automáticamente antes (spring.sql.init).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final DataSource dataSource;
    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder encoder;

    @Value("${courthub.seed-demo:true}")
    private boolean seedDemo;

    /** Clave del administrador en producción (variable ADMIN_PASSWORD de Render); vacía = no se toca. */
    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    /** Clave de las cuentas demo (gdiaz, crojas, pmunoz, asoto, lperez) en producción (DEMO_PASSWORD). */
    @Value("${DEMO_PASSWORD:}")
    private String demoPassword;

    private static final List<String> USUARIOS_DEMO = List.of("gdiaz", "crojas", "pmunoz", "asoto", "lperez");

    /**
     * Seguridad en producción: la clave "Password123" del seed es pública (está en el README), así que
     * si el despliegue define ADMIN_PASSWORD y/o DEMO_PASSWORD se sobrescriben en cada arranque.
     * Así la clave real nunca está en el repositorio, solo en las variables de entorno del servidor.
     */
    private void aplicarClavesDeEntorno() {
        if (adminPassword != null && !adminPassword.isBlank()) {
            usuarioRepository.findByUsuarioOEmail("admin").ifPresent(u -> {
                if (!encoder.matches(adminPassword, u.getPasswordHash())) {
                    u.setPasswordHash(encoder.encode(adminPassword));
                    usuarioRepository.save(u);
                    log.info("[Seguridad] Clave del administrador actualizada desde ADMIN_PASSWORD");
                }
            });
        }
        if (demoPassword != null && !demoPassword.isBlank()) {
            for (String nombre : USUARIOS_DEMO) {
                usuarioRepository.findByUsuarioOEmail(nombre).ifPresent(u -> {
                    if (!encoder.matches(demoPassword, u.getPasswordHash())) {
                        u.setPasswordHash(encoder.encode(demoPassword));
                        usuarioRepository.save(u);
                    }
                });
            }
            log.info("[Seguridad] Claves de las cuentas demo actualizadas desde DEMO_PASSWORD");
        }
    }

    /**
     * Bases creadas antes del tercer avance: la columna booleana "activo" se convierte en el nuevo
     * campo "estado" (activo -> 'activo', inactivo -> 'anulado'), se genera un nombre de usuario a partir
     * del correo y se elimina la columna antigua. En una BD nueva no hace nada.
     */
    private void migrarTercerAvance() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        Integer tieneActivo = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'usuarios' AND column_name = 'activo'", Integer.class);
        if (tieneActivo != null && tieneActivo > 0) {
            log.info("[Migración] Convirtiendo usuarios.activo -> usuarios.estado ...");
            jdbc.update("UPDATE usuarios SET estado = CASE WHEN activo THEN 'activo' ELSE 'anulado' END");
            jdbc.update("ALTER TABLE usuarios DROP COLUMN activo");
        }
        jdbc.update("UPDATE usuarios SET usuario = split_part(email, '@', 1) WHERE usuario IS NULL");
    }

    @Bean
    public ApplicationRunner cargarDatosDemo() {
        return args -> {
            migrarTercerAvance();
            if (!seedDemo) { aplicarClavesDeEntorno(); return; }
            if (usuarioRepository.count() > 0) {
                log.info("[Seed] La base de datos ya tiene datos; se omite db/seed.sql");
                aplicarClavesDeEntorno();
                return;
            }
            log.info("[Seed] Base de datos vacía: cargando datos de demostración (db/seed.sql)...");
            try (Connection conn = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(conn,
                        new EncodedResource(new ClassPathResource("db/seed.sql"), StandardCharsets.UTF_8));
            }
            log.info("[Seed] Datos demo cargados. Usuarios: admin / gdiaz (clave Password123 salvo ADMIN_PASSWORD / DEMO_PASSWORD)");
            aplicarClavesDeEntorno();
        };
    }
}

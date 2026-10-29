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

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

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

    @Value("${courthub.seed-demo:true}")
    private boolean seedDemo;

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
            if (!seedDemo) return;
            if (usuarioRepository.count() > 0) {
                log.info("[Seed] La base de datos ya tiene datos; se omite db/seed.sql");
                return;
            }
            log.info("[Seed] Base de datos vacía: cargando datos de demostración (db/seed.sql)...");
            try (Connection conn = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(conn,
                        new EncodedResource(new ClassPathResource("db/seed.sql"), StandardCharsets.UTF_8));
            }
            log.info("[Seed] Datos demo cargados. Usuarios: demo@courthub.com / admin@courthub.com (Password123)");
        };
    }
}

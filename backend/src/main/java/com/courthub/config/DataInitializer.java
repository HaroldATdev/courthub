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

    @Bean
    public ApplicationRunner cargarDatosDemo() {
        return args -> {
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

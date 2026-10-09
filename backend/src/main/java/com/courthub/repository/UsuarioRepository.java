package com.courthub.repository;
import com.courthub.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Usuario> findByRolAndActivoTrueOrderByNombreAsc(String rol);

    List<Usuario> findAllByOrderByFechaRegistroDesc();

    long countByActivoTrue();

    /** Conteo de usuarios por rol (panel admin). */
    @Query("SELECT u.rol, COUNT(u) FROM Usuario u WHERE u.activo = TRUE GROUP BY u.rol")
    List<Object[]> contarPorRol();
}

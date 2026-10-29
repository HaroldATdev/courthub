package com.courthub.repository;

import com.courthub.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    /** Login: el identificador puede ser el nombre de usuario o el correo. */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.usuario) = LOWER(:id) OR LOWER(u.email) = LOWER(:id)")
    Optional<Usuario> findByUsuarioOEmail(@Param("id") String identificador);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsuarioIgnoreCase(String usuario);

    boolean existsByDni(String dni);

    List<Usuario> findByRolAndEstadoOrderByNombreAsc(String rol, String estado);

    List<Usuario> findAllByOrderByFechaRegistroDesc();

    long countByEstado(String estado);

    /** Conteo de usuarios activos por rol (panel admin). */
    @Query("SELECT u.rol, COUNT(u) FROM Usuario u WHERE u.estado = 'activo' GROUP BY u.rol")
    List<Object[]> contarPorRol();
}

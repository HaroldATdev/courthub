package com.courthub.repository;

import com.courthub.model.Verificacion2fa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface Verificacion2faRepository extends JpaRepository<Verificacion2fa, Long> {

    @EntityGraph(attributePaths = "usuario")
    Optional<Verificacion2fa> findByDesafioHash(String desafioHash);

    /** Al emitir un código nuevo, los anteriores del usuario dejan de servir. */
    @Modifying
    @Query("UPDATE Verificacion2fa v SET v.usado = TRUE WHERE v.usuario.id = :usuarioId AND v.usado = FALSE")
    void invalidarPendientes(@Param("usuarioId") Long usuarioId);
}

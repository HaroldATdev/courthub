package com.courthub.repository;

import com.courthub.model.ClaveReset;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClaveResetRepository extends JpaRepository<ClaveReset, Long> {

    @EntityGraph(attributePaths = "usuario")
    Optional<ClaveReset> findByTokenHash(String tokenHash);

    /** Invalida los enlaces anteriores del usuario al pedir uno nuevo. */
    @Modifying
    @Query("UPDATE ClaveReset c SET c.usado = TRUE WHERE c.usuario.id = :usuarioId AND c.usado = FALSE")
    void invalidarPendientes(@Param("usuarioId") Long usuarioId);
}

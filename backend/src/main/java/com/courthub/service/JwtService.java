package com.courthub.service;

import com.courthub.model.Usuario;
import com.courthub.security.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/** Emisión y validación de tokens JWT (HS256) con los datos públicos del usuario. */
@Service
public class JwtService {

    private final SecretKey clave;

    @Getter
    private final long horasExpiracion;

    public JwtService(@Value("${courthub.jwt.secret}") String secreto,
                      @Value("${courthub.jwt.expiration-hours:8}") long horasExpiracion) {
        this.clave = Keys.hmacShaKeyFor(sha256(secreto)); // SHA-256 garantiza 256 bits para HS256
        this.horasExpiracion = horasExpiracion;
    }

    /** Genera un JWT con id, email, rol y datos de perfil (expira en 8 h por defecto). */
    public String generar(Usuario u) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(u.getId()))
                .claim("id", u.getId())
                .claim("email", u.getEmail())
                .claim("rol", u.getRol())
                .claim("nombre", u.getNombre())
                .claim("apellido", u.getApellido())
                .claim("nivel", u.getNivel())
                .claim("deporte", u.getDeporteFavorito())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(horasExpiracion, ChronoUnit.HOURS)))
                .signWith(clave)
                .compact();
    }

    /** Valida la firma y expiración; lanza JwtException si el token no es válido. */
    public AuthUser parsear(String token) {
        Claims c = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
        return new AuthUser(c.get("id", Long.class), c.get("email", String.class), c.get("rol", String.class),
                c.get("nombre", String.class), c.get("apellido", String.class),
                c.get("nivel", String.class), c.get("deporte", String.class));
    }

    private static byte[] sha256(String texto) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

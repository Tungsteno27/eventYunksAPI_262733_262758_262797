package com.example.eventYunksAPI.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long usuarioId, String rol) {
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                //El de nuestro proyecto agrega el rol tambíen en el token
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims leerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUsuarioId(String token) {
        return Long.valueOf(leerClaims(token).getSubject());
    }

    public String extractRol(String token) {
        return leerClaims(token).get("rol", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            leerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

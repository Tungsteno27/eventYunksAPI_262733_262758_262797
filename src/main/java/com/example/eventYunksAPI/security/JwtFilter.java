package com.example.eventYunksAPI.security;

import com.example.eventYunksAPI.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;

    private boolean isPublicRoute(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if (method.equalsIgnoreCase("OPTIONS")) return true;
        if (path.equals("/api/sesiones") && method.equals("POST")) return true; //Este es el de inicio de sesión
        if (path.equals("/api/asistentes") && method.equals("POST")) return true; //Este es el de registro

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (isPublicRoute(request)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            enviarNoAutorizado(response, "Se requiere token de autenticación");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            enviarNoAutorizado(response, "Token inválido o expirado");
            return;
        }

        request.setAttribute("usuarioId", jwtService.extractUsuarioId(token));
        request.setAttribute("rol", jwtService.extractRol(token));
        chain.doFilter(request, response);
    }

    private void enviarNoAutorizado(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"error\":\"" + mensaje + "\"}");
    }
}

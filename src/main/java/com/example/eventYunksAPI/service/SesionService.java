package com.example.eventYunksAPI.service;

import com.example.eventYunksAPI.dto.LoginRequest;
import com.example.eventYunksAPI.dto.SesionResponse;
import com.example.eventYunksAPI.dto.UsuarioResponse;
import com.example.eventYunksAPI.exceptions.ForbiddenException;
import com.example.eventYunksAPI.exceptions.UnauthorizedException;
import com.example.eventYunksAPI.model.Asistente;
import com.example.eventYunksAPI.model.Usuario;
import com.example.eventYunksAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SesionService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /**
     * Método que valida las credenciales de un usuario
     * @param request  el correo y contraseña del usuario a iniciar sesión
     * @return el token y los datos del asistente
     * @throws UnauthorizedException si el correo o la contraseña son incorrectos
     * @throws ForbiddenException si la cuenta está suspendida o no es de un asistente
     */
    public SesionResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo().trim()).orElse(null);
        if (usuario == null || !passwordEncoder.matches(request.getContrasenia(), usuario.getHashContrasenia())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ForbiddenException("La cuenta fue suspendida");
        }
        if (!(usuario instanceof Asistente)) {
            throw new ForbiddenException("Esta cuenta no pertenece a un asistente");
        }
        return crearSesion(usuario);
    }
    /**
     * Método que genera el token de jwt de un usuario que ya se autenticó
     * @param usuario el usuario autenticado
     * @return el token y los datos del usuario
     */
    public SesionResponse crearSesion(Usuario usuario) {
        String token = jwtService.generateToken(usuario.getId(), "ASISTENTE");
        UsuarioResponse datos = new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getTelefono());
        return new SesionResponse(token, "Bearer", datos);
    }
}

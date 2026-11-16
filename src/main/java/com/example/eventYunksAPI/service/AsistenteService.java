package com.example.eventYunksAPI.service;

import com.example.eventYunksAPI.dto.RegistroAsistenteRequest;
import com.example.eventYunksAPI.dto.SesionResponse;
import com.example.eventYunksAPI.dto.UsuarioResponse;
import com.example.eventYunksAPI.exceptions.ConflictException;
import com.example.eventYunksAPI.exceptions.UnauthorizedException;
import com.example.eventYunksAPI.model.Asistente;
import com.example.eventYunksAPI.repository.AsistenteRepository;
import com.example.eventYunksAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsistenteService {

    @Autowired
    private AsistenteRepository asistenteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SesionService sesionService;

    /**
     * Datos del asistente con sesión iniciada (nombre y correo de la pantalla mis-tickets)
     * @param usuarioId el id que viene en el token
     * @return los datos del asistente
     * @throws UnauthorizedException si el usuario ya no existe
     */
    @Transactional(readOnly = true)
    public UsuarioResponse perfil(Long usuarioId) {
        Asistente asistente = asistenteRepository.findById(usuarioId)
                .orElseThrow(() -> new UnauthorizedException("La sesión no es válida"));
        return new UsuarioResponse(asistente.getId(), asistente.getNombre(), asistente.getCorreo(), asistente.getTelefono());
    }

    /**
     *  Método que crea la cuenta de un asistente y crea su sesión
     * @param request los datos del formulario de registro
     * @return el token y los datos del asistente recién creado
     * @throws ConflictException si el correo ya está duplicado
     */
    public SesionResponse registrar(RegistroAsistenteRequest request) {
        String correo = request.getCorreo().trim();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }

        Asistente asistente = new Asistente();
        asistente.setNombre(request.getNombre().trim());
        asistente.setCorreo(correo);
        asistente.setHashContrasenia(passwordEncoder.encode(request.getContrasenia()));
        asistente.setTelefono(request.getTelefono() == null || request.getTelefono().isBlank() ? null : request.getTelefono().trim());
        asistente.setActivo(true);
        asistenteRepository.save(asistente);
        return sesionService.crearSesion(asistente);
    }
}

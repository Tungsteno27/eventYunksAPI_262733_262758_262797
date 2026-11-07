package com.example.eventYunksAPI.service;

import com.example.eventYunksAPI.dto.RegistroAsistenteRequest;
import com.example.eventYunksAPI.dto.SesionResponse;
import com.example.eventYunksAPI.exceptions.ConflictException;
import com.example.eventYunksAPI.model.Asistente;
import com.example.eventYunksAPI.repository.AsistenteRepository;
import com.example.eventYunksAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

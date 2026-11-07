package com.example.eventYunksAPI.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroAsistenteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 255, message = "El correo no puede pasar de 255 caracteres")
    private String correo;


    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private String contrasenia;

    //Pt: tuve que buscar la regex para validar telefonos xd
    @Pattern(regexp = "^$|^[0-9 +()\\-]{7,20}$", message = "El teléfono no tiene un formato válido")
    private String telefono;
}

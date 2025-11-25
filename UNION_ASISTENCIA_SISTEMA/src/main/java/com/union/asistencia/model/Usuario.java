package com.union.asistencia.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    private Integer id;
    private String username;
    private String passwordHash;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private LocalDateTime fechaCreacion;
    private Boolean activo;
    private String docenteId; // NUEVO: Para relacionar con docente

    // Método personalizado adicional
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    // NUEVO: Método para verificar si es administrador
    public boolean esAdministrador() {
        return "ADMIN".equalsIgnoreCase(this.rol);
    }

    // NUEVO: Método para verificar si es docente
    public boolean esDocente() {
        return "DOCENTE".equalsIgnoreCase(this.rol);
    }
}
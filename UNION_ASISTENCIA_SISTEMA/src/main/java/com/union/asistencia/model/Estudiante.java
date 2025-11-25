package com.union.asistencia.model;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estudiante {
    private Integer id;
    private String codigoEstudiante;
    private String dni;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String carrera;
    private Integer semestre;
    private String grupo;
    private LocalDate fechaNacimiento;
    private String direccion;
    private Boolean activo;
    private LocalDate fechaRegistro;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    // ✅ MÉTODO AGREGADO PARA SOLUCIONAR EL ERROR
    // En Estudiante.java, agrega este método:
    public boolean isActivo() {
        return activo != null && activo;
    }
}
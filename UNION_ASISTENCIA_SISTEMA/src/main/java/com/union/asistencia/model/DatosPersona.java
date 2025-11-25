package com.union.asistencia.model;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatosPersona {
    private String dni;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String departamento;
    private String provincia;
    private String distrito;

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
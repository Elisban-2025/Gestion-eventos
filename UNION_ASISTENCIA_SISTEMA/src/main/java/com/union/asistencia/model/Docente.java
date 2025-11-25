package com.union.asistencia.model;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Docente {
    private Integer id;
    private String codigoDocente;
    private String dni;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String facultad;
    private String especialidad;
    private Integer cargaHoraria;
    private LocalDate fechaContratacion;
    private Boolean activo;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
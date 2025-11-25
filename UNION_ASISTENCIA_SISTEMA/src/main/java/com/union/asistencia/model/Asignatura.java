package com.union.asistencia.model;

import lombok.*;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asignatura {
    private Integer id;
    private String codigoAsignatura;
    private String nombre;
    private Integer creditos;
    private Integer horasTeoria;
    private Integer horasPractica;
    private String ciclo;
    private String facultad;
    private String planEstudios;
    private Boolean activo;

    // Relaciones (se cargan separadamente)
    private Integer docenteId;
    private Docente docente;

    public String getNombreCompleto() {
        return codigoAsignatura + " - " + nombre;
    }
}
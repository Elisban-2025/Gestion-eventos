package com.union.asistencia.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {
    private Integer id;
    private String nombre;
    private String tipo; // ACADEMICO, CULTURAL, DEPORTIVO, INSTITUCIONAL
    private String descripcion;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String lugar;
    private String responsable;
    private Integer capacidadMaxima;
    private Boolean requiereInscripcion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    public String getEstado() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(fechaHoraInicio)) return "PROGRAMADO";
        if (now.isAfter(fechaHoraInicio) && now.isBefore(fechaHoraFin)) return "EN_CURSO";
        return "FINALIZADO";
    }
}
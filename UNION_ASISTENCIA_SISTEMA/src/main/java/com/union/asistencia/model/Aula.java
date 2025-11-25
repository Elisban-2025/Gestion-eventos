package com.union.asistencia.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aula {
    private Integer id;
    private String codigoAula;
    private String nombre;
    private String edificio;
    private Integer capacidad;
    private String tipo; // AULA, LABORATORIO, AUDITORIO, TALLER
    private String equipamiento;
    private Boolean disponible;
    private String observaciones;
}
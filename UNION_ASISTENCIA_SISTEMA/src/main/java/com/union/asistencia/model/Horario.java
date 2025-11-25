package com.union.asistencia.model;

import lombok.*;
import java.time.LocalTime;
import java.time.DayOfWeek;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Horario {
    private Integer id;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String tipo; // TEORIA, PRACTICA, LABORATORIO
    private String aula;

    // Relaciones
    private Integer asignaturaId;
    private Integer docenteId;

    // Objetos relacionados
    private Asignatura asignatura;
    private Docente docente;

    public String getDiaSemanaString() {
        switch (diaSemana) {
            case MONDAY: return "Lunes";
            case TUESDAY: return "Martes";
            case WEDNESDAY: return "Miércoles";
            case THURSDAY: return "Jueves";
            case FRIDAY: return "Viernes";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: return "";
        }
    }
}
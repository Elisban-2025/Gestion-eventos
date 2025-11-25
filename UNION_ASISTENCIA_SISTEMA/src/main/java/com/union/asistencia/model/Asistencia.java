package com.union.asistencia.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asistencia {
    // ==================== IDENTIFICACIÓN PRINCIPAL ====================
    /** Identificador único del registro de asistencia (clave primaria) */
    private Integer id;

    // ==================== INFORMACIÓN TEMPORAL ====================
    /** Fecha y hora exacta en que se registró la asistencia */
    private LocalDateTime fechaHora;

    // ==================== ESTADO Y TIPO DE REGISTRO ====================
    /** Estado de la asistencia: PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO */
    private String estado;

    /** Observaciones adicionales sobre el registro de asistencia */
    private String observaciones;

    /** Método de registro: MANUAL, QR, BIOMETRICO */
    private String tipoRegistro;

    // ==================== RELACIONES CON OTRAS ENTIDADES (IDs) ====================
    /** ID del estudiante relacionado (clave foránea) */
    private Integer estudianteId;

    /** ID de la asignatura relacionada (clave foránea) */
    private Integer asignaturaId;

    /** ID del docente relacionado (clave foránea) */
    private Integer docenteId;

    /** ID del evento relacionado (clave foránea, opcional) */
    private Integer eventoId;

    // ==================== OBJETOS RELACIONADOS COMPLETOS ====================
    /** Objeto Estudiante completo con toda su información */
    private Estudiante estudiante;

    /** Objeto Asignatura completo con toda su información */
    private Asignatura asignatura;

    /** Objeto Docente completo con toda su información */
    private Docente docente;

    /** Objeto Evento completo con toda su información */
    private Evento evento;
}
package com.union.asistencia.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipanteEvento {
    private Integer id;
    private String tipoParticipante; // ESTUDIANTE, DOCENTE, EXTERNO
    private Boolean asistio;
    private LocalDateTime fechaInscripcion;
    private String codigoQR;

    // Relaciones
    private Integer eventoId;
    private Integer estudianteId;
    private Integer docenteId;

    // Objetos relacionados
    private Evento evento;
    private Estudiante estudiante;
    private Docente docente;

    public String getNombreParticipante() {
        if (estudiante != null) return estudiante.getNombreCompleto();
        if (docente != null) return docente.getNombreCompleto();
        return "Externo";
    }
}
package com.union.asistencia.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaEvento {
    private Integer id;
    private Integer eventoId;
    private Integer estudianteId;
    private Integer docenteId;
    private LocalDateTime fechaHoraRegistro;
    private String tipoRegistro; // QR, MANUAL
    private String estado; // PRESENTE, AUSENTE

    // Relaciones
    private Evento evento;
    private Estudiante estudiante;
    private Docente docente;

    public String getNombreEvento() {
        return evento != null ? evento.getNombre() : "N/A";
    }

    public String getNombreParticipante() {
        if (estudiante != null) {
            return estudiante.getNombreCompleto();
        } else if (docente != null) {
            return docente.getNombreCompleto();
        }
        return "N/A";
    }

    public String getTipoParticipante() {
        if (estudiante != null) return "ESTUDIANTE";
        if (docente != null) return "DOCENTE";
        return "EXTERNO";
    }

    public String getCodigoParticipante() {
        if (estudiante != null) return estudiante.getCodigoEstudiante();
        if (docente != null) return docente.getCodigoDocente();
        return "N/A";
    }
}
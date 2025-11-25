package com.union.asistencia.service;

import com.union.asistencia.dao.AsistenciaDAO;
import com.union.asistencia.dao.ReporteDAO;
import com.union.asistencia.model.Asistencia;
import lombok.extern.java.Log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Log
public class ReportService {

    private final AsistenciaDAO asistenciaDAO;
    private final ReporteDAO reporteDAO;

    public ReportService() {
        this.asistenciaDAO = new AsistenciaDAO();
        this.reporteDAO = new ReporteDAO();
    }

    public List<Asistencia> generarReporteAsistenciaPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        return asistenciaDAO.obtenerTodas().stream()
                .filter(a -> !a.getFechaHora().isBefore(inicio) && !a.getFechaHora().isAfter(fin))
                .toList();
    }

    public Map<String, Long> generarEstadisticasAsistencia(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Asistencia> asistencias = generarReporteAsistenciaPorFecha(fechaInicio, fechaFin);

        long presentes = asistencias.stream()
                .filter(a -> "PRESENTE".equals(a.getEstado()))
                .count();

        long ausentes = asistencias.stream()
                .filter(a -> "AUSENTE".equals(a.getEstado()))
                .count();

        long tardanzas = asistencias.stream()
                .filter(a -> "TARDANZA".equals(a.getEstado()))
                .count();

        long justificados = asistencias.stream()
                .filter(a -> "JUSTIFICADO".equals(a.getEstado()))
                .count();

        return Map.of(
                "PRESENTE", presentes,
                "AUSENTE", ausentes,
                "TARDANZA", tardanzas,
                "JUSTIFICADO", justificados,
                "TOTAL", (long) asistencias.size()
        );
    }

    public String generarReporteTexto(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Long> estadisticas = generarEstadisticasAsistencia(fechaInicio, fechaFin);

        StringBuilder reporte = new StringBuilder();
        reporte.append("REPORTE DE ASISTENCIA - UNIVERSIDAD PERUANA UNIÓN\n");
        reporte.append("Período: ").append(fechaInicio).append(" a ").append(fechaFin).append("\n\n");

        reporte.append("ESTADÍSTICAS:\n");
        reporte.append("Presentes: ").append(estadisticas.get("PRESENTE")).append("\n");
        reporte.append("Ausentes: ").append(estadisticas.get("AUSENTE")).append("\n");
        reporte.append("Tardanzas: ").append(estadisticas.get("TARDANZA")).append("\n");
        reporte.append("Justificados: ").append(estadisticas.get("JUSTIFICADO")).append("\n");
        reporte.append("Total registros: ").append(estadisticas.get("TOTAL")).append("\n");

        if (estadisticas.get("TOTAL") > 0) {
            double porcentajeAsistencia = (estadisticas.get("PRESENTE") + estadisticas.get("JUSTIFICADO")) * 100.0 / estadisticas.get("TOTAL");
            reporte.append("Porcentaje de asistencia: ").append(String.format("%.2f", porcentajeAsistencia)).append("%\n");
        }

        return reporte.toString();
    }
}
package com.union.asistencia.dao;

import lombok.extern.java.Log;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
public class ReporteDAO {
    public List<Map<String, Object>> generarReporteAsistenciaMensual(int mes, int anio) {
        List<Map<String, Object>> reporte = new ArrayList<>();

        String sql = "SELECT e.codigo_estudiante, e.nombre, e.apellido, e.carrera, " +
                "COUNT(a.id) as total_clases, " +
                "SUM(CASE WHEN a.estado = 'PRESENTE' THEN 1 ELSE 0 END) as asistencias, " +
                "SUM(CASE WHEN a.estado = 'AUSENTE' THEN 1 ELSE 0 END) as ausencias, " +
                "SUM(CASE WHEN a.estado = 'TARDANZA' THEN 1 ELSE 0 END) as tardanzas, " +
                "SUM(CASE WHEN a.estado = 'JUSTIFICADO' THEN 1 ELSE 0 END) as justificados " +
                "FROM estudiantes e " +
                "LEFT JOIN asistencias a ON e.id = a.estudiante_id " +
                "AND strftime('%m', a.fecha_hora) = ? AND strftime('%Y', a.fecha_hora) = ? " +
                "WHERE e.activo = TRUE " +
                "GROUP BY e.id, e.codigo_estudiante, e.nombre, e.apellido, e.carrera " +
                "ORDER BY e.apellido, e.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, String.format("%02d", mes));
            stmt.setString(2, String.valueOf(anio));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("Código", rs.getString("codigo_estudiante"));
                fila.put("Estudiante", rs.getString("nombre") + " " + rs.getString("apellido"));
                fila.put("Carrera", rs.getString("carrera"));
                fila.put("Total Clases", rs.getInt("total_clases"));
                fila.put("Asistencias", rs.getInt("asistencias"));
                fila.put("Ausencias", rs.getInt("ausencias"));
                fila.put("Tardanzas", rs.getInt("tardanzas"));
                fila.put("Justificados", rs.getInt("justificados"));

                int total = rs.getInt("total_clases");
                int presentes = rs.getInt("asistencias") + rs.getInt("justificados");
                double porcentaje = total > 0 ? (presentes * 100.0) / total : 0;
                fila.put("Porcentaje Asistencia", String.format("%.2f%%", porcentaje));

                reporte.add(fila);
            }
        } catch (SQLException e) {
            log.severe("Error al generar reporte de asistencia mensual: " + e.getMessage());
        }
        return reporte;
    }

    public List<Map<String, Object>> generarReporteAsistenciaPorAsignatura(Integer asignaturaId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Map<String, Object>> reporte = new ArrayList<>();
        String sql = "SELECT e.codigo_estudiante, e.nombre, e.apellido, " +
                "COUNT(a.id) as total_clases, " +
                "SUM(CASE WHEN a.estado = 'PRESENTE' THEN 1 ELSE 0 END) as asistencias, " +
                "SUM(CASE WHEN a.estado = 'AUSENTE' THEN 1 ELSE 0 END) as ausencias, " +
                "SUM(CASE WHEN a.estado = 'TARDANZA' THEN 1 ELSE 0 END) as tardanzas " +
                "FROM estudiantes e " +
                "LEFT JOIN asistencias a ON e.id = a.estudiante_id " +
                "AND a.asignatura_id = ? " +
                "AND DATE(a.fecha_hora) BETWEEN ? AND ? " +
                "WHERE e.activo = TRUE " +
                "GROUP BY e.id, e.codigo_estudiante, e.nombre, e.apellido " +
                "ORDER BY e.apellido, e.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, asignaturaId);
            stmt.setString(2, fechaInicio.toString());
            stmt.setString(3, fechaFin.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("Código", rs.getString("codigo_estudiante"));
                fila.put("Estudiante", rs.getString("nombre") + " " + rs.getString("apellido"));
                fila.put("Total Clases", rs.getInt("total_clases"));
                fila.put("Asistencias", rs.getInt("asistencias"));
                fila.put("Ausencias", rs.getInt("ausencias"));
                fila.put("Tardanzas", rs.getInt("tardanzas"));

                int total = rs.getInt("total_clases");
                int presentes = rs.getInt("asistencias");
                double porcentaje = total > 0 ? (presentes * 100.0) / total : 0;
                fila.put("Porcentaje Asistencia", String.format("%.2f%%", porcentaje));

                reporte.add(fila);
            }
        } catch (SQLException e) {
            log.severe("Error al generar reporte de asistencia por asignatura: " + e.getMessage());
        }
        return reporte;
    }

    public Map<String, Object> generarEstadisticasGenerales(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> estadisticas = new HashMap<>();
        String sql = "SELECT " +
                "COUNT(DISTINCT e.id) as total_estudiantes, " +
                "COUNT(DISTINCT d.id) as total_docentes, " +
                "COUNT(DISTINCT a.id) as total_asignaturas, " +
                "COUNT(asist.id) as total_asistencias, " +
                "SUM(CASE WHEN asist.estado = 'PRESENTE' THEN 1 ELSE 0 END) as asistencias_presentes, " +
                "SUM(CASE WHEN asist.estado = 'AUSENTE' THEN 1 ELSE 0 END) as asistencias_ausentes, " +
                "SUM(CASE WHEN asist.estado = 'TARDANZA' THEN 1 ELSE 0 END) as asistencias_tardanzas " +
                "FROM estudiantes e, docentes d, asignaturas a " +
                "LEFT JOIN asistencias asist ON a.id = asist.asignatura_id " +
                "AND DATE(asist.fecha_hora) BETWEEN ? AND ? " +
                "WHERE e.activo = TRUE AND d.activo = TRUE AND a.activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fechaInicio.toString());
            stmt.setString(2, fechaFin.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                estadisticas.put("Total Estudiantes", rs.getInt("total_estudiantes"));
                estadisticas.put("Total Docentes", rs.getInt("total_docentes"));
                estadisticas.put("Total Asignaturas", rs.getInt("total_asignaturas"));
                estadisticas.put("Total Asistencias", rs.getInt("total_asistencias"));
                estadisticas.put("Asistencias Presentes", rs.getInt("asistencias_presentes"));
                estadisticas.put("Asistencias Ausentes", rs.getInt("asistencias_ausentes"));
                estadisticas.put("Asistencias Tardanzas", rs.getInt("asistencias_tardanzas"));

                int totalAsistencias = rs.getInt("total_asistencias");
                int presentes = rs.getInt("asistencias_presentes");
                double porcentajeAsistencia = totalAsistencias > 0 ? (presentes * 100.0) / totalAsistencias : 0;
                estadisticas.put("Porcentaje Asistencia General", String.format("%.2f%%", porcentajeAsistencia));
            }
        } catch (SQLException e) {
            log.severe("Error al generar estadísticas generales: " + e.getMessage());
        }
        return estadisticas;
    }

    // ✅ MÉTODO CORREGIDO PARA PARTICIPACIÓN EN EVENTOS
    public List<Map<String, Object>> generarReporteParticipacionEventos(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Map<String, Object>> reporte = new ArrayList<>();

        String sql = "SELECT ev.nombre, ev.tipo, ev.fecha_hora_inicio, ev.lugar, " +
                "COUNT(pe.id) as total_inscritos, " +
                "SUM(CASE WHEN pe.asistio = 1 THEN 1 ELSE 0 END) as total_asistentes " +
                "FROM eventos ev " +
                "LEFT JOIN participantes_evento pe ON ev.id = pe.evento_id " +
                "WHERE date(ev.fecha_hora_inicio) BETWEEN date(?) AND date(?) " +
                "GROUP BY ev.id, ev.nombre, ev.tipo, ev.fecha_hora_inicio, ev.lugar " +
                "ORDER BY ev.fecha_hora_inicio";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fechaInicio.toString());
            stmt.setString(2, fechaFin.toString());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("Evento", rs.getString("nombre"));
                fila.put("Tipo", rs.getString("tipo"));

                // Manejar fecha de forma segura
                String fechaStr = rs.getString("fecha_hora_inicio");
                if (fechaStr != null) {
                    try {
                        // Convertir formato SQLite a LocalDateTime
                        LocalDateTime fechaHora = LocalDateTime.parse(fechaStr.replace(' ', 'T'));
                        fila.put("Fecha", fechaHora.toLocalDate().toString());
                    } catch (Exception e) {
                        fila.put("Fecha", fechaStr);
                    }
                } else {
                    fila.put("Fecha", "N/A");
                }

                fila.put("Lugar", rs.getString("lugar"));
                fila.put("Inscritos", rs.getInt("total_inscritos"));
                fila.put("Asistentes", rs.getInt("total_asistentes"));

                int inscritos = rs.getInt("total_inscritos");
                int asistentes = rs.getInt("total_asistentes");
                double porcentaje = inscritos > 0 ? (asistentes * 100.0) / inscritos : 0;
                fila.put("Participación", String.format("%.2f%%", porcentaje));

                reporte.add(fila);
            }
        } catch (SQLException e) {
            log.severe("Error al generar reporte de participación en eventos: " + e.getMessage());
        }
        return reporte;
    }
}
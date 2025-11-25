package com.union.asistencia.dao;

import com.union.asistencia.model.AsistenciaEvento;
import com.union.asistencia.model.Estudiante;
import com.union.asistencia.model.Docente;
import com.union.asistencia.model.Evento;
import lombok.extern.java.Log;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Log
public class AsistenciaEventoDAO {

    public boolean registrarAsistencia(AsistenciaEvento asistencia) {
        String sql = "INSERT INTO asistencias_eventos (evento_id, estudiante_id, docente_id, fecha_hora_registro, tipo_registro, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, asistencia.getEventoId());

            if (asistencia.getEstudianteId() != null) {
                pstmt.setInt(2, asistencia.getEstudianteId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            if (asistencia.getDocenteId() != null) {
                pstmt.setInt(3, asistencia.getDocenteId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(5, asistencia.getTipoRegistro());
            pstmt.setString(6, asistencia.getEstado());

            int affectedRows = pstmt.executeUpdate();
            log.info("✅ Asistencia a evento registrada: Evento " + asistencia.getEventoId());
            return affectedRows > 0;

        } catch (SQLException e) {
            log.severe("❌ Error registrando asistencia a evento: " + e.getMessage());
            return false;
        }
    }

    /**
     * NUEVO MÉTODO: Registra asistencia rápida por DNI/código de estudiante
     */
    public boolean registrarAsistenciaRapida(int estudianteId, int eventoId) {
        String sql = "INSERT INTO asistencias_eventos (evento_id, estudiante_id, fecha_hora_registro, tipo_registro, estado) " +
                "VALUES (?, ?, datetime('now'), 'RAPIDO_DNI', 'PRESENTE')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventoId);
            pstmt.setInt(2, estudianteId);

            int affectedRows = pstmt.executeUpdate();
            log.info("✅ Asistencia rápida registrada: Estudiante " + estudianteId + " en Evento " + eventoId);
            return affectedRows > 0;

        } catch (SQLException e) {
            log.severe("❌ Error registrando asistencia rápida: " + e.getMessage());
            return false;
        }
    }

    /**
     * NUEVO MÉTODO: Registra asistencia en participantes_evento (TABLA QUE SE MUESTRA)
     */
    public boolean registrarAsistenciaParticipante(int estudianteId, int eventoId) {
        String sql = "INSERT INTO participantes_evento (evento_id, estudiante_id, tipo_participante, asistio, fecha_inscripcion) " +
                "VALUES (?, ?, 'ESTUDIANTE', 1, datetime('now'))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventoId);
            pstmt.setInt(2, estudianteId);

            int affectedRows = pstmt.executeUpdate();
            log.info("✅ Asistencia registrada en participantes: Estudiante " + estudianteId + " en Evento " + eventoId);
            return affectedRows > 0;

        } catch (SQLException e) {
            log.severe("❌ Error registrando asistencia en participantes: " + e.getMessage());
            return false;
        }
    }

    /**
     * NUEVO MÉTODO: Verifica si un estudiante ya tiene asistencia registrada en un evento
     */
    public boolean existeAsistenciaEstudiante(int estudianteId, int eventoId) {
        String sql = "SELECT COUNT(*) FROM asistencias_eventos WHERE evento_id = ? AND estudiante_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventoId);
            pstmt.setInt(2, estudianteId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            log.severe("❌ Error verificando asistencia de estudiante: " + e.getMessage());
        }
        return false;
    }

    /**
     * NUEVO MÉTODO: Verifica si ya existe en participantes_evento
     */
    public boolean existeParticipante(int estudianteId, int eventoId) {
        String sql = "SELECT COUNT(*) FROM participantes_evento WHERE evento_id = ? AND estudiante_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventoId);
            pstmt.setInt(2, estudianteId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            log.severe("❌ Error verificando participante: " + e.getMessage());
        }
        return false;
    }

    public boolean yaRegistrado(Integer eventoId, Integer estudianteId, Integer docenteId) {
        String sql = "SELECT COUNT(*) FROM asistencias_eventos WHERE evento_id = ? AND (estudiante_id = ? OR docente_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventoId);
            pstmt.setInt(2, estudianteId != null ? estudianteId : -1);
            pstmt.setInt(3, docenteId != null ? docenteId : -1);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            log.severe("❌ Error verificando asistencia a evento: " + e.getMessage());
        }
        return false;
    }

    public List<AsistenciaEvento> obtenerAsistenciasPorEvento(Integer eventoId) {
        List<AsistenciaEvento> asistencias = new ArrayList<>();
        String sql = "SELECT ae.*, e.nombre as evento_nombre, " +
                "est.nombre as estudiante_nombre, est.apellido as estudiante_apellido, est.codigo_estudiante, " +
                "doc.nombre as docente_nombre, doc.apellido as docente_apellido, doc.codigo_docente " +
                "FROM asistencias_eventos ae " +
                "LEFT JOIN eventos e ON ae.evento_id = e.id " +
                "LEFT JOIN estudiantes est ON ae.estudiante_id = est.id " +
                "LEFT JOIN docentes doc ON ae.docente_id = doc.id " +
                "WHERE ae.evento_id = ? ORDER BY ae.fecha_hora_registro DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventoId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                asistencias.add(mapResultSetToAsistenciaEvento(rs));
            }

        } catch (SQLException e) {
            log.severe("❌ Error obteniendo asistencias por evento: " + e.getMessage());
        }
        return asistencias;
    }

    public List<AsistenciaEvento> obtenerTodasAsistencias() {
        List<AsistenciaEvento> asistencias = new ArrayList<>();
        String sql = "SELECT ae.*, e.nombre as evento_nombre, " +
                "est.nombre as estudiante_nombre, est.apellido as estudiante_apellido, est.codigo_estudiante, " +
                "doc.nombre as docente_nombre, doc.apellido as docente_apellido, doc.codigo_docente " +
                "FROM asistencias_eventos ae " +
                "LEFT JOIN eventos e ON ae.evento_id = e.id " +
                "LEFT JOIN estudiantes est ON ae.estudiante_id = est.id " +
                "LEFT JOIN docentes doc ON ae.docente_id = doc.id " +
                "ORDER BY ae.fecha_hora_registro DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                asistencias.add(mapResultSetToAsistenciaEvento(rs));
            }

        } catch (SQLException e) {
            log.severe("❌ Error obteniendo todas las asistencias: " + e.getMessage());
        }
        return asistencias;
    }

    public int contarAsistenciasPorEvento(Integer eventoId) {
        String sql = "SELECT COUNT(*) FROM asistencias_eventos WHERE evento_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventoId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            log.severe("❌ Error contando asistencias por evento: " + e.getMessage());
        }
        return 0;
    }

    private AsistenciaEvento mapResultSetToAsistenciaEvento(ResultSet rs) throws SQLException {
        // Mapear evento
        Evento evento = Evento.builder()
                .id(rs.getInt("evento_id"))
                .nombre(rs.getString("evento_nombre"))
                .build();

        // Mapear estudiante si existe
        Estudiante estudiante = null;
        if (rs.getObject("estudiante_id") != null) {
            estudiante = Estudiante.builder()
                    .id(rs.getInt("estudiante_id"))
                    .nombre(rs.getString("estudiante_nombre"))
                    .apellido(rs.getString("estudiante_apellido"))
                    .codigoEstudiante(rs.getString("codigo_estudiante"))
                    .build();
        }

        // Mapear docente si existe
        Docente docente = null;
        if (rs.getObject("docente_id") != null) {
            docente = Docente.builder()
                    .id(rs.getInt("docente_id"))
                    .nombre(rs.getString("docente_nombre"))
                    .apellido(rs.getString("docente_apellido"))
                    .codigoDocente(rs.getString("codigo_docente"))
                    .build();
        }

        return AsistenciaEvento.builder()
                .id(rs.getInt("id"))
                .eventoId(rs.getInt("evento_id"))
                .estudianteId(rs.getObject("estudiante_id") != null ? rs.getInt("estudiante_id") : null)
                .docenteId(rs.getObject("docente_id") != null ? rs.getInt("docente_id") : null)
                .fechaHoraRegistro(rs.getTimestamp("fecha_hora_registro").toLocalDateTime())
                .tipoRegistro(rs.getString("tipo_registro"))
                .estado(rs.getString("estado"))
                .evento(evento)
                .estudiante(estudiante)
                .docente(docente)
                .build();
    }
}
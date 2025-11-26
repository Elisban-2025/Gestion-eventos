package com.union.asistencia.dao;

import com.union.asistencia.model.Asignatura;
import com.union.asistencia.model.Docente;
import com.union.asistencia.model.Horario;
import lombok.extern.java.Log;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class HorarioDAO {
    public boolean guardar(Horario horario) {
        String sql = "INSERT INTO horarios (dia_semana, hora_inicio, hora_fin, tipo, aula, asignatura_id, docente_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, horario.getDiaSemana().name());
            // CORRECCIÓN: Guardar como string en formato HH:mm
            stmt.setString(2, horario.getHoraInicio().toString());
            stmt.setString(3, horario.getHoraFin().toString());
            stmt.setString(4, horario.getTipo());
            stmt.setString(5, horario.getAula());
            stmt.setInt(6, horario.getAsignaturaId());
            stmt.setInt(7, horario.getDocenteId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        horario.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            log.severe("Error al guardar horario: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Horario> obtenerTodos() {
        List<Horario> horarios = new ArrayList<>();
        String sql = "SELECT h.*, a.nombre as asignatura_nombre, d.nombre as docente_nombre, d.apellido as docente_apellido " +
                "FROM horarios h " +
                "JOIN asignaturas a ON h.asignatura_id = a.id " +
                "JOIN docentes d ON h.docente_id = d.id " +
                "ORDER BY h.dia_semana, h.hora_inicio";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                horarios.add(mapResultSetToHorario(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener horarios: " + e.getMessage());
        }
        return horarios;
    }

    public List<Horario> obtenerPorDocente(Integer docenteId) {
        List<Horario> horarios = new ArrayList<>();
        String sql = "SELECT h.*, a.nombre as asignatura_nombre, d.nombre as docente_nombre, d.apellido as docente_apellido " +
                "FROM horarios h " +
                "JOIN asignaturas a ON h.asignatura_id = a.id " +
                "JOIN docentes d ON h.docente_id = d.id " +
                "WHERE h.docente_id = ? " +
                "ORDER BY h.dia_semana, h.hora_inicio";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, docenteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                horarios.add(mapResultSetToHorario(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener horarios por docente: " + e.getMessage());
        }
        return horarios;
    }

    public List<Horario> obtenerPorDia(DayOfWeek dia) {
        List<Horario> horarios = new ArrayList<>();
        String sql = "SELECT h.*, a.nombre as asignatura_nombre, d.nombre as docente_nombre, d.apellido as docente_apellido " +
                "FROM horarios h " +
                "JOIN asignaturas a ON h.asignatura_id = a.id " +
                "JOIN docentes d ON h.docente_id = d.id " +
                "WHERE h.dia_semana = ? " +
                "ORDER BY h.hora_inicio";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dia.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                horarios.add(mapResultSetToHorario(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener horarios por día: " + e.getMessage());
        }
        return horarios;
    }

    // CORREGIR método actualizar
    public boolean actualizar(Horario horario) {
        String sql = "UPDATE horarios SET dia_semana = ?, hora_inicio = ?, hora_fin = ?, tipo = ?, " +
                "aula = ?, asignatura_id = ?, docente_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, horario.getDiaSemana().name());
            // CORRECCIÓN: Usar string
            stmt.setString(2, horario.getHoraInicio().toString());
            stmt.setString(3, horario.getHoraFin().toString());
            stmt.setString(4, horario.getTipo());
            stmt.setString(5, horario.getAula());
            stmt.setInt(6, horario.getAsignaturaId());
            stmt.setInt(7, horario.getDocenteId());
            stmt.setInt(8, horario.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al actualizar horario: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(Integer id) {
        String sql = "DELETE FROM horarios WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al eliminar horario: " + e.getMessage());
            return false;
        }
    }

    // CORREGIR método existeConflictoHorario
    public boolean existeConflictoHorario(Horario horario) {
        String sql = "SELECT COUNT(*) FROM horarios WHERE " +
                "dia_semana = ? AND " +
                "((hora_inicio <= ? AND hora_fin >= ?) OR " +
                "(hora_inicio <= ? AND hora_fin >= ?) OR " +
                "(? <= hora_fin AND ? >= hora_inicio)) AND " +
                "(aula = ? OR docente_id = ?) AND " +
                "(id != ? OR ? = -1)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String horaInicioStr = horario.getHoraInicio().toString();
            String horaFinStr = horario.getHoraFin().toString();

            stmt.setString(1, horario.getDiaSemana().name());
            stmt.setString(2, horaFinStr);
            stmt.setString(3, horaInicioStr);
            stmt.setString(4, horaInicioStr);
            stmt.setString(5, horaFinStr);
            stmt.setString(6, horaInicioStr);
            stmt.setString(7, horaFinStr);
            stmt.setString(8, horario.getAula());
            stmt.setInt(9, horario.getDocenteId());
            stmt.setInt(10, horario.getId() != null ? horario.getId() : -1);
            stmt.setInt(11, horario.getId() != null ? horario.getId() : -1);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.severe("Error al verificar conflicto de horario: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    // CORREGIR método mapResultSetToHorario
    private Horario mapResultSetToHorario(ResultSet rs) throws SQLException {
        return Horario.builder()
                .id(rs.getInt("id"))
                .diaSemana(DayOfWeek.valueOf(rs.getString("dia_semana")))
                // CORRECCIÓN: Parsear desde string
                .horaInicio(LocalTime.parse(rs.getString("hora_inicio")))
                .horaFin(LocalTime.parse(rs.getString("hora_fin")))
                .tipo(rs.getString("tipo"))
                .aula(rs.getString("aula"))
                .asignaturaId(rs.getInt("asignatura_id"))
                .docenteId(rs.getInt("docente_id"))
                .asignatura(Asignatura.builder()
                        .nombre(rs.getString("asignatura_nombre"))
                        .build())
                .docente(Docente.builder()
                        .nombre(rs.getString("docente_nombre"))
                        .apellido(rs.getString("docente_apellido"))
                        .build())
                .build();
    }
}
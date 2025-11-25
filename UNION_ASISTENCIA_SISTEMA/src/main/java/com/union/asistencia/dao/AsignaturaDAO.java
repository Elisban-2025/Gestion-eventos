package com.union.asistencia.dao;

import com.union.asistencia.model.Asignatura;
import lombok.extern.java.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class AsignaturaDAO {

    public boolean guardar(Asignatura asignatura) {
        String sql = "INSERT INTO asignaturas (codigo_asignatura, nombre, creditos, horas_teoria, " +
                "horas_practica, ciclo, facultad, plan_estudios, docente_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, asignatura.getCodigoAsignatura());
            stmt.setString(2, asignatura.getNombre());
            stmt.setInt(3, asignatura.getCreditos());
            stmt.setInt(4, asignatura.getHorasTeoria());
            stmt.setInt(5, asignatura.getHorasPractica());
            stmt.setString(6, asignatura.getCiclo());
            stmt.setString(7, asignatura.getFacultad());
            stmt.setString(8, asignatura.getPlanEstudios());
            stmt.setObject(9, asignatura.getDocenteId(), Types.INTEGER);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        asignatura.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            log.severe("Error al guardar asignatura: " + e.getMessage());
        }
        return false;
    }

    public List<Asignatura> obtenerTodos() {
        List<Asignatura> asignaturas = new ArrayList<>();
        String sql = "SELECT a.*, d.nombre as docente_nombre, d.apellido as docente_apellido " +
                "FROM asignaturas a LEFT JOIN docentes d ON a.docente_id = d.id " +
                "WHERE a.activo = TRUE ORDER BY a.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                asignaturas.add(mapResultSetToAsignatura(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener asignaturas: " + e.getMessage());
        }
        return asignaturas;
    }

    public Optional<Asignatura> obtenerPorId(Integer id) {
        String sql = "SELECT a.*, d.nombre as docente_nombre, d.apellido as docente_apellido " +
                "FROM asignaturas a LEFT JOIN docentes d ON a.docente_id = d.id " +
                "WHERE a.id = ? AND a.activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToAsignatura(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener asignatura por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean actualizar(Asignatura asignatura) {
        String sql = "UPDATE asignaturas SET codigo_asignatura = ?, nombre = ?, creditos = ?, " +
                "horas_teoria = ?, horas_practica = ?, ciclo = ?, facultad = ?, " +
                "plan_estudios = ?, docente_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, asignatura.getCodigoAsignatura());
            stmt.setString(2, asignatura.getNombre());
            stmt.setInt(3, asignatura.getCreditos());
            stmt.setInt(4, asignatura.getHorasTeoria());
            stmt.setInt(5, asignatura.getHorasPractica());
            stmt.setString(6, asignatura.getCiclo());
            stmt.setString(7, asignatura.getFacultad());
            stmt.setString(8, asignatura.getPlanEstudios());
            stmt.setObject(9, asignatura.getDocenteId(), Types.INTEGER);
            stmt.setInt(10, asignatura.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al actualizar asignatura: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(Integer id) {
        String sql = "UPDATE asignaturas SET activo = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al eliminar asignatura: " + e.getMessage());
            return false;
        }
    }

    public List<Asignatura> buscarPorNombre(String criterio) {
        List<Asignatura> asignaturas = new ArrayList<>();
        String sql = "SELECT a.*, d.nombre as docente_nombre, d.apellido as docente_apellido " +
                "FROM asignaturas a LEFT JOIN docentes d ON a.docente_id = d.id " +
                "WHERE a.activo = TRUE AND (a.nombre LIKE ? OR a.codigo_asignatura LIKE ?) " +
                "ORDER BY a.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeCriterio = "%" + criterio + "%";
            stmt.setString(1, likeCriterio);
            stmt.setString(2, likeCriterio);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                asignaturas.add(mapResultSetToAsignatura(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al buscar asignaturas: " + e.getMessage());
        }
        return asignaturas;
    }

    private Asignatura mapResultSetToAsignatura(ResultSet rs) throws SQLException {
        return Asignatura.builder()
                .id(rs.getInt("id"))
                .codigoAsignatura(rs.getString("codigo_asignatura"))
                .nombre(rs.getString("nombre"))
                .creditos(rs.getInt("creditos"))
                .horasTeoria(rs.getInt("horas_teoria"))
                .horasPractica(rs.getInt("horas_practica"))
                .ciclo(rs.getString("ciclo"))
                .facultad(rs.getString("facultad"))
                .planEstudios(rs.getString("plan_estudios"))
                .docenteId(rs.getObject("docente_id") != null ? rs.getInt("docente_id") : null)
                .activo(rs.getBoolean("activo"))
                .build();
    }
}
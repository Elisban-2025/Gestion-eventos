package com.union.asistencia.dao;

import com.union.asistencia.model.Docente;
import com.union.asistencia.util.DateUtils;
import lombok.extern.java.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class DocenteDAO {

    public boolean guardar(Docente docente) {
        // ✅ AGREGAR CAMPO 'activo' AL INSERT
        String sql = "INSERT INTO docentes (codigo_docente, dni, nombre, apellido, email, telefono, " +
                "facultad, especialidad, carga_horaria, fecha_contratacion, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // ✅ VERIFICAR DUPLICADO CON MÉTODO MEJORADO
            if (existeCodigoDocente(docente.getCodigoDocente())) {
                log.severe("🚫 ERROR: Código de docente ya existe - " + docente.getCodigoDocente());
                return false;
            }

            stmt.setString(1, docente.getCodigoDocente());
            stmt.setString(2, docente.getDni());
            stmt.setString(3, docente.getNombre());
            stmt.setString(4, docente.getApellido());
            stmt.setString(5, docente.getEmail());
            stmt.setString(6, docente.getTelefono());
            stmt.setString(7, docente.getFacultad());
            stmt.setString(8, docente.getEspecialidad());
            stmt.setInt(9, docente.getCargaHoraria());
            stmt.setString(10, DateUtils.formatDateForSQLite(docente.getFechaContratacion()));
            stmt.setBoolean(11, true); // ✅ SIEMPRE ACTIVO AL CREAR

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        docente.setId(generatedKeys.getInt(1));
                        log.info("✅ Docente guardado exitosamente - ID: " + docente.getId() + ", Código: " + docente.getCodigoDocente());
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            log.severe("❌ Error al guardar docente: " + e.getMessage());
            log.severe("📋 Detalles - Código: " + docente.getCodigoDocente() + ", DNI: " + docente.getDni());

            // ✅ DETECTAR ESPECÍFICAMENTE ERROR DE CÓDIGO DUPLICADO
            if (e.getMessage().contains("UNIQUE constraint failed") && e.getMessage().contains("codigo_docente")) {
                log.severe("🚫 ERROR: Código de docente duplicado - " + docente.getCodigoDocente());
            }

            e.printStackTrace();
        }
        return false;
    }

    public List<Docente> obtenerTodos() {
        List<Docente> docentes = new ArrayList<>();
        String sql = "SELECT * FROM docentes WHERE activo = TRUE ORDER BY apellido, nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                docentes.add(mapResultSetToDocente(rs));
            }
            log.info("📥 " + docentes.size() + " docentes obtenidos de la BD");
        } catch (SQLException e) {
            log.severe("❌ Error al obtener docentes: " + e.getMessage());
        }
        return docentes;
    }

    public Optional<Docente> obtenerPorId(Integer id) {
        String sql = "SELECT * FROM docentes WHERE id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToDocente(rs));
            }
        } catch (SQLException e) {
            log.severe("❌ Error al obtener docente por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Docente> obtenerPorCodigo(String codigoDocente) {
        String sql = "SELECT * FROM docentes WHERE codigo_docente = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoDocente);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Docente docente = mapResultSetToDocente(rs);
                log.info("🔍 Docente encontrado por código: " + codigoDocente + " - " + docente.getNombreCompleto());
                return Optional.of(docente);
            } else {
                log.info("🔍 Código disponible: " + codigoDocente);
            }
        } catch (SQLException e) {
            log.severe("❌ Error al obtener docente por código: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean actualizar(Docente docente) {
        String sql = "UPDATE docentes SET codigo_docente = ?, dni = ?, nombre = ?, apellido = ?, " +
                "email = ?, telefono = ?, facultad = ?, especialidad = ?, carga_horaria = ?, " +
                "fecha_contratacion = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // ✅ VERIFICAR DUPLICADO EN ACTUALIZACIÓN (excluyendo el registro actual)
            Optional<Docente> existente = obtenerPorCodigo(docente.getCodigoDocente());
            if (existente.isPresent() && !existente.get().getId().equals(docente.getId())) {
                log.severe("🚫 ERROR: Código de docente ya existe en actualización - " + docente.getCodigoDocente());
                return false;
            }

            stmt.setString(1, docente.getCodigoDocente());
            stmt.setString(2, docente.getDni());
            stmt.setString(3, docente.getNombre());
            stmt.setString(4, docente.getApellido());
            stmt.setString(5, docente.getEmail());
            stmt.setString(6, docente.getTelefono());
            stmt.setString(7, docente.getFacultad());
            stmt.setString(8, docente.getEspecialidad());
            stmt.setInt(9, docente.getCargaHoraria());

            // ✅ USANDO TU DateUtils ACTUALIZADO
            stmt.setString(10, DateUtils.formatDateForSQLite(docente.getFechaContratacion()));
            stmt.setInt(11, docente.getId());

            boolean exito = stmt.executeUpdate() > 0;
            if (exito) {
                log.info("✅ Docente actualizado exitosamente - ID: " + docente.getId());
            } else {
                log.warning("⚠️ No se pudo actualizar docente - ID: " + docente.getId());
            }
            return exito;
        } catch (SQLException e) {
            log.severe("❌ Error al actualizar docente: " + e.getMessage());

            // ✅ DETECTAR ESPECÍFICAMENTE ERROR DE CÓDIGO DUPLICADO
            if (e.getMessage().contains("UNIQUE constraint failed") && e.getMessage().contains("codigo_docente")) {
                log.severe("🚫 ERROR: Código de docente duplicado en actualización - " + docente.getCodigoDocente());
            }

            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(Integer id) {
        String sql = "UPDATE docentes SET activo = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) {
                log.info("✅ Docente marcado como inactivo - ID: " + id);
            }
            return exito;
        } catch (SQLException e) {
            log.severe("❌ Error al eliminar docente: " + e.getMessage());
            return false;
        }
    }

    public List<Docente> buscarPorNombre(String criterio) {
        List<Docente> docentes = new ArrayList<>();
        String sql = "SELECT * FROM docentes WHERE activo = TRUE AND " +
                "(nombre LIKE ? OR apellido LIKE ? OR codigo_docente LIKE ?) " +
                "ORDER BY apellido, nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeCriterio = "%" + criterio + "%";
            stmt.setString(1, likeCriterio);
            stmt.setString(2, likeCriterio);
            stmt.setString(3, likeCriterio);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                docentes.add(mapResultSetToDocente(rs));
            }
            log.info("🔍 Búsqueda completada - " + docentes.size() + " docentes encontrados para: " + criterio);
        } catch (SQLException e) {
            log.severe("❌ Error al buscar docentes: " + e.getMessage());
        }
        return docentes;
    }

    // ✅ MÉTODO NUEVO PARA VERIFICAR CÓDIGO DUPLICADO
    private boolean existeCodigoDocente(String codigoDocente) {
        String sql = "SELECT COUNT(*) as count FROM docentes WHERE codigo_docente = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoDocente);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                log.info("🔍 Verificación código " + codigoDocente + ": " + count + " registros encontrados");
                return count > 0;
            }
        } catch (SQLException e) {
            log.severe("❌ Error al verificar código docente: " + e.getMessage());
        }
        return false;
    }

    private Docente mapResultSetToDocente(ResultSet rs) throws SQLException {
        return Docente.builder()
                .id(rs.getInt("id"))
                .codigoDocente(rs.getString("codigo_docente"))
                .dni(rs.getString("dni"))
                .nombre(rs.getString("nombre"))
                .apellido(rs.getString("apellido"))
                .email(rs.getString("email"))
                .telefono(rs.getString("telefono"))
                .facultad(rs.getString("facultad"))
                .especialidad(rs.getString("especialidad"))
                .cargaHoraria(rs.getInt("carga_horaria"))

                // ✅ USANDO TU DateUtils ACTUALIZADO
                .fechaContratacion(DateUtils.parseDateFromSQLite(rs.getString("fecha_contratacion")))

                .activo(rs.getBoolean("activo"))
                .build();
    }
}
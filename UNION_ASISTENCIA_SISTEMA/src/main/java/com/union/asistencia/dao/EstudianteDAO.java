package com.union.asistencia.dao;

import com.union.asistencia.model.Estudiante;
import com.union.asistencia.util.DateUtils;
import lombok.extern.java.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class EstudianteDAO {

    public boolean guardar(Estudiante estudiante) {
        log.info("🔍 Intentando guardar estudiante: " + estudiante.getNombre() + " " + estudiante.getApellido());

        // ✅ PRIMERO: Verificar duplicados de manera MÁS EFECTIVA
        if (existeEstudianteConDni(estudiante.getDni())) {
            log.severe("❌ ERROR CRÍTICO: Ya existe un estudiante con DNI: " + estudiante.getDni());
            return false;
        }

        if (existeEstudianteConCodigo(estudiante.getCodigoEstudiante())) {
            log.severe("❌ ERROR CRÍTICO: Ya existe un estudiante con código: " + estudiante.getCodigoEstudiante());
            return false;
        }

        // ✅ USAR INSERT OR IGNORE para evitar errores de constraint
        String sql = "INSERT OR IGNORE INTO estudiantes (codigo_estudiante, dni, nombre, apellido, email, telefono, " +
                "carrera, semestre, grupo, fecha_nacimiento, direccion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, estudiante.getCodigoEstudiante());
            stmt.setString(2, estudiante.getDni());
            stmt.setString(3, estudiante.getNombre());
            stmt.setString(4, estudiante.getApellido());
            stmt.setString(5, estudiante.getEmail());
            stmt.setString(6, estudiante.getTelefono());
            stmt.setString(7, estudiante.getCarrera());
            stmt.setInt(8, estudiante.getSemestre());
            stmt.setString(9, estudiante.getGrupo());
            stmt.setString(10, DateUtils.formatDateForSQLite(estudiante.getFechaNacimiento()));
            stmt.setString(11, estudiante.getDireccion());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        estudiante.setId(generatedKeys.getInt(1));
                    }
                }
                log.info("✅ ESTUDIANTE GUARDADO EXITOSAMENTE: " + estudiante.getNombre() + " " + estudiante.getApellido());
                return true;
            } else {
                log.severe("❌ NO SE PUDO GUARDAR - Posible duplicado ignorado");
                return false;
            }
        } catch (SQLException e) {
            log.severe("❌ ERROR DE BASE DE DATOS: " + e.getMessage());
            return false;
        }
    }

    // ✅ MÉTODO MEJORADO: Verificar solo DNI
    public boolean existeEstudianteConDni(String dni) {
        String sql = "SELECT COUNT(*) as count FROM estudiantes WHERE dni = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dni);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                if (count > 0) {
                    log.warning("⚠️ DNI DUPLICADO ENCONTRADO: " + dni);
                    // ✅ Obtener información del estudiante existente
                    String infoSql = "SELECT id, codigo_estudiante, nombre, apellido FROM estudiantes WHERE dni = ? AND activo = TRUE";
                    try (PreparedStatement infoStmt = conn.prepareStatement(infoSql)) {
                        infoStmt.setString(1, dni);
                        ResultSet infoRs = infoStmt.executeQuery();
                        if (infoRs.next()) {
                            log.warning("⚠️ Estudiante existente - ID: " + infoRs.getInt("id") +
                                    ", Código: " + infoRs.getString("codigo_estudiante") +
                                    ", Nombre: " + infoRs.getString("nombre") + " " + infoRs.getString("apellido"));
                        }
                    }
                }
                return count > 0;
            }
        } catch (SQLException e) {
            log.severe("Error al verificar DNI: " + e.getMessage());
        }
        return false;
    }

    // ✅ MÉTODO MEJORADO: Verificar solo código
    public boolean existeEstudianteConCodigo(String codigo) {
        String sql = "SELECT COUNT(*) as count FROM estudiantes WHERE codigo_estudiante = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                if (count > 0) {
                    log.warning("⚠️ CÓDIGO DUPLICADO ENCONTRADO: " + codigo);
                }
                return count > 0;
            }
        } catch (SQLException e) {
            log.severe("Error al verificar código: " + e.getMessage());
        }
        return false;
    }

    // ✅ MÉTODO PARA ELIMINAR ESTUDIANTE POR DNI (para limpiar el duplicado)
    public boolean eliminarEstudiantePorDni(String dni) {
        String sql = "DELETE FROM estudiantes WHERE dni = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dni);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                log.info("✅ ESTUDIANTE ELIMINADO: DNI " + dni);
                return true;
            } else {
                log.warning("⚠️ No se encontró estudiante con DNI: " + dni);
                return false;
            }
        } catch (SQLException e) {
            log.severe("❌ Error al eliminar estudiante: " + e.getMessage());
            return false;
        }
    }

    // ✅ MÉTODO PARA OBTENER TODOS LOS ESTUDIANTES (para diagnóstico)
    public List<Estudiante> obtenerTodosLosEstudiantes() {
        List<Estudiante> estudiantes = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                estudiantes.add(mapResultSetToEstudiante(rs));
            }
            log.info("📊 TOTAL ESTUDIANTES EN BD: " + estudiantes.size());
        } catch (SQLException e) {
            log.severe("Error al obtener estudiantes: " + e.getMessage());
        }
        return estudiantes;
    }

    // El resto de los métodos se mantienen...
    public List<Estudiante> obtenerTodos() {
        List<Estudiante> estudiantes = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes WHERE activo = TRUE ORDER BY apellido, nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                estudiantes.add(mapResultSetToEstudiante(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener estudiantes: " + e.getMessage());
        }
        return estudiantes;
    }

    public Optional<Estudiante> obtenerPorId(Integer id) {
        String sql = "SELECT * FROM estudiantes WHERE id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEstudiante(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener estudiante por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Estudiante> obtenerPorCodigo(String codigoEstudiante) {
        String sql = "SELECT * FROM estudiantes WHERE codigo_estudiante = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoEstudiante);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEstudiante(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener estudiante por código: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Estudiante buscarPorDniOCodigo(String dniCodigo) {
        String sql = "SELECT * FROM estudiantes WHERE (dni = ? OR codigo_estudiante = ?) AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dniCodigo);
            pstmt.setString(2, dniCodigo);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEstudiante(rs);
            }
        } catch (SQLException e) {
            log.severe("Error al buscar estudiante por DNI/código: " + e.getMessage());
        }
        return null;
    }

    public boolean actualizar(Estudiante estudiante) {
        String sql = "UPDATE estudiantes SET codigo_estudiante = ?, dni = ?, nombre = ?, apellido = ?, " +
                "email = ?, telefono = ?, carrera = ?, semestre = ?, grupo = ?, " +
                "fecha_nacimiento = ?, direccion = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estudiante.getCodigoEstudiante());
            stmt.setString(2, estudiante.getDni());
            stmt.setString(3, estudiante.getNombre());
            stmt.setString(4, estudiante.getApellido());
            stmt.setString(5, estudiante.getEmail());
            stmt.setString(6, estudiante.getTelefono());
            stmt.setString(7, estudiante.getCarrera());
            stmt.setInt(8, estudiante.getSemestre());
            stmt.setString(9, estudiante.getGrupo());
            stmt.setString(10, DateUtils.formatDateForSQLite(estudiante.getFechaNacimiento()));
            stmt.setString(11, estudiante.getDireccion());
            stmt.setInt(12, estudiante.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al actualizar estudiante: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(Integer id) {
        String sql = "UPDATE estudiantes SET activo = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al eliminar estudiante: " + e.getMessage());
            return false;
        }
    }

    public List<Estudiante> buscarPorNombre(String criterio) {
        List<Estudiante> estudiantes = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes WHERE activo = TRUE AND " +
                "(nombre LIKE ? OR apellido LIKE ? OR codigo_estudiante LIKE ?) " +
                "ORDER BY apellido, nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeCriterio = "%" + criterio + "%";
            stmt.setString(1, likeCriterio);
            stmt.setString(2, likeCriterio);
            stmt.setString(3, likeCriterio);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                estudiantes.add(mapResultSetToEstudiante(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al buscar estudiantes: " + e.getMessage());
        }
        return estudiantes;
    }

    private Estudiante mapResultSetToEstudiante(ResultSet rs) throws SQLException {
        return Estudiante.builder()
                .id(rs.getInt("id"))
                .codigoEstudiante(rs.getString("codigo_estudiante"))
                .dni(rs.getString("dni"))
                .nombre(rs.getString("nombre"))
                .apellido(rs.getString("apellido"))
                .email(rs.getString("email"))
                .telefono(rs.getString("telefono"))
                .carrera(rs.getString("carrera"))
                .semestre(rs.getInt("semestre"))
                .grupo(rs.getString("grupo"))
                .fechaNacimiento(DateUtils.parseDateFromSQLite(rs.getString("fecha_nacimiento")))
                .direccion(rs.getString("direccion"))
                .activo(rs.getBoolean("activo"))
                .fechaRegistro(rs.getTimestamp("fecha_registro") != null ?
                        rs.getTimestamp("fecha_registro").toLocalDateTime().toLocalDate() : null)
                .build();
    }
}
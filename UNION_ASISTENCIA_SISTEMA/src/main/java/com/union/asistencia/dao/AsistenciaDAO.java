package com.union.asistencia.dao;

import com.union.asistencia.model.Asistencia;
import com.union.asistencia.model.Estudiante;
import com.union.asistencia.model.Asignatura;
import com.union.asistencia.model.Docente;
import com.union.asistencia.util.DateUtils;
import lombok.extern.java.Log;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Log
public class AsistenciaDAO {

    public boolean guardar(Asistencia asistencia) {
        // Consulta SQL para insertar nuevo registro de asistencia
        String sql = "INSERT INTO asistencias (fecha_hora, estado, observaciones, tipo_registro, " +
                "estudiante_id, asignatura_id, docente_id, evento_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // ==================== ASIGNACIÓN DE PARÁMETROS A LA CONSULTA ====================
            // Parámetro 1: Fecha y hora formateada para SQLite
            stmt.setString(1, DateUtils.formatDateTimeForSQLite(asistencia.getFechaHora()));
            // Parámetro 2: Estado de la asistencia
            stmt.setString(2, asistencia.getEstado());
            // Parámetro 3: Observaciones adicionales
            stmt.setString(3, asistencia.getObservaciones());
            // Parámetro 4: Tipo de registro utilizado
            stmt.setString(4, asistencia.getTipoRegistro());
            // Parámetro 5: ID del estudiante
            stmt.setInt(5, asistencia.getEstudianteId());
            // Parámetro 6: ID de la asignatura
            stmt.setInt(6, asistencia.getAsignaturaId());
            // Parámetro 7: ID del docente
            stmt.setInt(7, asistencia.getDocenteId());
            // Parámetro 8: ID del evento (puede ser nulo)
            stmt.setObject(8, asistencia.getEventoId(), Types.INTEGER);

            // Ejecutar la inserción y obtener número de filas afectadas
            int affectedRows = stmt.executeUpdate();

            // Verificar si la inserción fue exitosa y obtener el ID generado
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        asistencia.setId(generatedKeys.getInt(1));
                        log.info("✅ Asistencia guardada exitosamente - ID: " + asistencia.getId());
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            log.severe("❌ Error al guardar asistencia: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene todos los registros de asistencia de la base de datos.
     * Incluye información relacionada de estudiantes, asignaturas y docentes.
     *
     * @return Lista de objetos Asistencia con todos los registros
     */
    public List<Asistencia> obtenerTodas() {
        List<Asistencia> asistencias = new ArrayList<>();

        // Consulta SQL que une múltiples tablas para obtener datos completos
        String sql = "SELECT a.*, e.nombre as estudiante_nombre, e.apellido as estudiante_apellido, " +
                "e.codigo_estudiante, asig.nombre as asignatura_nombre, " +
                "d.nombre as docente_nombre, d.apellido as docente_apellido " +
                "FROM asistencias a " +
                "JOIN estudiantes e ON a.estudiante_id = e.id " +
                "JOIN asignaturas asig ON a.asignatura_id = asig.id " +
                "JOIN docentes d ON a.docente_id = d.id " +
                "ORDER BY a.fecha_hora DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Procesar cada fila del resultado y convertir a objetos Asistencia
            while (rs.next()) {
                asistencias.add(mapResultSetToAsistencia(rs));
            }
            log.info("📥 " + asistencias.size() + " asistencias obtenidas de la BD");
        } catch (SQLException e) {
            log.severe("❌ Error al obtener asistencias: " + e.getMessage());
            e.printStackTrace(); // Para debugging detallado
        } catch (Exception e) {
            log.severe("❌ Error inesperado al obtener asistencias: " + e.getMessage());
            e.printStackTrace(); // Para debugging detallado
        }
        return asistencias;
    }

    /**
     * Obtiene las asistencias de un estudiante específico dentro de un rango de fechas.
     *
     * @param estudianteId ID del estudiante a consultar
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de asistencias que cumplen con los criterios
     */
    public List<Asistencia> obtenerPorEstudianteYFecha(Integer estudianteId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Asistencia> asistencias = new ArrayList<>();

        // Consulta SQL con filtros por estudiante y rango de fechas
        String sql = "SELECT a.*, e.nombre as estudiante_nombre, e.apellido as estudiante_apellido, " +
                "e.codigo_estudiante, asig.nombre as asignatura_nombre, " +
                "d.nombre as docente_nombre, d.apellido as docente_apellido " +
                "FROM asistencias a " +
                "JOIN estudiantes e ON a.estudiante_id = e.id " +
                "JOIN asignaturas asig ON a.asignatura_id = asig.id " +
                "JOIN docentes d ON a.docente_id = d.id " +
                "WHERE a.estudiante_id = ? AND a.fecha_hora BETWEEN ? AND ? " +
                "ORDER BY a.fecha_hora DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // ==================== ASIGNACIÓN DE PARÁMETROS CON FILTROS ====================
            // Parámetro 1: ID del estudiante
            stmt.setInt(1, estudianteId);
            // Parámetro 2: Fecha de inicio formateada
            stmt.setString(2, DateUtils.formatDateTimeForSQLite(fechaInicio));
            // Parámetro 3: Fecha de fin formateada
            stmt.setString(3, DateUtils.formatDateTimeForSQLite(fechaFin));

            ResultSet rs = stmt.executeQuery();

            // Procesar resultados y construir objetos Asistencia
            while (rs.next()) {
                asistencias.add(mapResultSetToAsistencia(rs));
            }
            log.info("📥 " + asistencias.size() + " asistencias encontradas para estudiante ID: " + estudianteId);
        } catch (SQLException e) {
            log.severe("❌ Error al obtener asistencias por estudiante y fecha: " + e.getMessage());
        }
        return asistencias;
    }

    /**
     * Actualiza un registro de asistencia existente en la base de datos.
     *
     * @param asistencia Objeto Asistencia con los datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizar(Asistencia asistencia) {
        // Consulta SQL para actualizar registro existente
        String sql = "UPDATE asistencias SET fecha_hora = ?, estado = ?, observaciones = ?, " +
                "tipo_registro = ?, estudiante_id = ?, asignatura_id = ?, docente_id = ?, " +
                "evento_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // ==================== ASIGNACIÓN DE PARÁMETROS PARA ACTUALIZACIÓN ====================
            // Parámetro 1: Fecha y hora actualizada
            stmt.setString(1, DateUtils.formatDateTimeForSQLite(asistencia.getFechaHora()));
            // Parámetro 2: Estado actualizado
            stmt.setString(2, asistencia.getEstado());
            // Parámetro 3: Observaciones actualizadas
            stmt.setString(3, asistencia.getObservaciones());
            // Parámetro 4: Tipo de registro actualizado
            stmt.setString(4, asistencia.getTipoRegistro());
            // Parámetro 5: ID del estudiante actualizado
            stmt.setInt(5, asistencia.getEstudianteId());
            // Parámetro 6: ID de la asignatura actualizada
            stmt.setInt(6, asistencia.getAsignaturaId());
            // Parámetro 7: ID del docente actualizado
            stmt.setInt(7, asistencia.getDocenteId());
            // Parámetro 8: ID del evento actualizado (puede ser nulo)
            stmt.setObject(8, asistencia.getEventoId(), Types.INTEGER);
            // Parámetro 9: ID del registro a actualizar (clave WHERE)
            stmt.setInt(9, asistencia.getId());

            // Ejecutar actualización y verificar resultado
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) {
                log.info("✅ Asistencia actualizada exitosamente - ID: " + asistencia.getId());
            }
            return exito;
        } catch (SQLException e) {
            log.severe("❌ Error al actualizar asistencia: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un registro de asistencia de la base de datos.
     *
     * @param id ID del registro de asistencia a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminar(Integer id) {
        // Consulta SQL para eliminar registro por ID
        String sql = "DELETE FROM asistencias WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Parámetro 1: ID del registro a eliminar
            stmt.setInt(1, id);

            // Ejecutar eliminación y verificar resultado
            boolean exito = stmt.executeUpdate() > 0;
            if (exito) {
                log.info("✅ Asistencia eliminada exitosamente - ID: " + id);
            }
            return exito;
        } catch (SQLException e) {
            log.severe("❌ Error al eliminar asistencia: " + e.getMessage());
            return false;
        }
    }

    /**
     * Convierte un ResultSet de base de datos en un objeto Asistencia.
     * Este método mapea las columnas de la base de datos a las propiedades del objeto.
     *
     * @param rs ResultSet con los datos de la base de datos
     * @return Objeto Asistencia poblado con los datos
     * @throws SQLException Si hay error al acceder a los datos del ResultSet
     */
    private Asistencia mapResultSetToAsistencia(ResultSet rs) throws SQLException {
        // ==================== PROCESAMIENTO DE FECHA/HORA ====================
        // Leer fecha_hora como String desde la base de datos
        String fechaHoraString = rs.getString("fecha_hora");
        LocalDateTime fechaHora = null;

        try {
            // Intentar parsear la fecha/hora desde el formato de SQLite
            if (fechaHoraString != null && !fechaHoraString.trim().isEmpty()) {
                // SQLite almacena fechas en formato "yyyy-MM-dd HH:mm:ss"
                fechaHora = LocalDateTime.parse(fechaHoraString,
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (Exception e) {
            // En caso de error en el parseo, usar fecha actual y loguear advertencia
            log.warning("⚠️ Error al parsear fecha_hora: " + fechaHoraString + " - " + e.getMessage());
            fechaHora = LocalDateTime.now(); // Fallback a fecha actual
        }

        // ==================== CONSTRUCCIÓN DE OBJETOS RELACIONADOS ====================
        // Construir objeto Estudiante con datos de la consulta
        Estudiante estudiante = Estudiante.builder()
                .id(rs.getInt("estudiante_id"))
                .nombre(rs.getString("estudiante_nombre"))
                .apellido(rs.getString("estudiante_apellido"))
                .codigoEstudiante(rs.getString("codigo_estudiante"))
                .build();

        // Construir objeto Asignatura con datos de la consulta
        Asignatura asignatura = Asignatura.builder()
                .id(rs.getInt("asignatura_id"))
                .nombre(rs.getString("asignatura_nombre"))
                .build();

        // Construir objeto Docente con datos de la consulta
        Docente docente = Docente.builder()
                .id(rs.getInt("docente_id"))
                .nombre(rs.getString("docente_nombre"))
                .apellido(rs.getString("docente_apellido"))
                .build();

        // ==================== CONSTRUCCIÓN DEL OBJETO ASISTENCIA COMPLETO ====================
        return Asistencia.builder()
                .id(rs.getInt("id")) // ID principal del registro
                .fechaHora(fechaHora) // Fecha y hora procesada
                .estado(rs.getString("estado")) // Estado de asistencia
                .observaciones(rs.getString("observaciones")) // Observaciones
                .tipoRegistro(rs.getString("tipo_registro")) // Tipo de registro
                .estudianteId(rs.getInt("estudiante_id")) // ID del estudiante
                .asignaturaId(rs.getInt("asignatura_id")) // ID de la asignatura
                .docenteId(rs.getInt("docente_id")) // ID del docente
                .eventoId(rs.getObject("evento_id") != null ? rs.getInt("evento_id") : null) // ID del evento (opcional)
                .estudiante(estudiante) // Objeto estudiante completo
                .asignatura(asignatura) // Objeto asignatura completa
                .docente(docente) // Objeto docente completo
                .build();
    }

    /**
     * Verifica si ya existe una asistencia registrada para un estudiante en un evento específico
     */
    public boolean existeAsistenciaEvento(Integer estudianteId, Integer eventoId) {
        String sql = "SELECT COUNT(*) FROM asistencias WHERE estudiante_id = ? AND evento_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, estudianteId);
            stmt.setInt(2, eventoId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.severe("❌ Error al verificar asistencia existente: " + e.getMessage());
        }
        return false;
    }

    /**
     * Registra asistencia automáticamente desde QR de evento
     */
    public boolean registrarAsistenciaDesdeQR(Integer estudianteId, Integer eventoId, String metodoRegistro) {
        String sql = "INSERT INTO asistencias (fecha_hora, estado, observaciones, tipo_registro, " +
                "estudiante_id, asignatura_id, docente_id, evento_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Usar fecha/hora actual
            stmt.setString(1, DateUtils.formatDateTimeForSQLite(LocalDateTime.now()));
            stmt.setString(2, "PRESENTE"); // Estado automático
            stmt.setString(3, "Registro automático vía QR"); // Observación
            stmt.setString(4, metodoRegistro); // QR, MANUAL, etc.
            stmt.setInt(5, estudianteId);

            // Para eventos, estos pueden ser null o valores por defecto
            stmt.setNull(6, Types.INTEGER); // asignatura_id
            stmt.setNull(7, Types.INTEGER); // docente_id
            stmt.setInt(8, eventoId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                log.info("✅ Asistencia registrada desde QR - Estudiante: " + estudianteId + ", Evento: " + eventoId);
                return true;
            }
        } catch (SQLException e) {
            log.severe("❌ Error al registrar asistencia desde QR: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene el ID de estudiante por código de estudiante
     */
    public Integer obtenerEstudianteIdPorCodigo(String codigoEstudiante) {
        String sql = "SELECT id FROM estudiantes WHERE codigo_estudiante = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoEstudiante);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            log.severe("❌ Error al obtener ID de estudiante: " + e.getMessage());
        }
        return null;
    }

}
package com.union.asistencia.dao;

import com.union.asistencia.model.Docente;
import com.union.asistencia.model.Estudiante;
import com.union.asistencia.model.Evento;
import com.union.asistencia.model.ParticipanteEvento;
import lombok.extern.java.Log;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class EventoDAO {

    public boolean guardar(Evento evento) {
        String sql = "INSERT INTO eventos (nombre, tipo, descripcion, fecha_hora_inicio, fecha_hora_fin, " +
                "lugar, responsable, capacidad_maxima, requiere_inscripcion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, evento.getNombre());
            stmt.setString(2, evento.getTipo());
            stmt.setString(3, evento.getDescripcion());

            // ✅ CORRECCIÓN: Formato ISO para SQLite
            stmt.setString(4, evento.getFechaHoraInicio().toString().replace('T', ' '));
            stmt.setString(5, evento.getFechaHoraFin().toString().replace('T', ' '));

            stmt.setString(6, evento.getLugar());
            stmt.setString(7, evento.getResponsable());
            stmt.setObject(8, evento.getCapacidadMaxima(), Types.INTEGER);
            stmt.setBoolean(9, evento.getRequiereInscripcion());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        evento.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            log.severe("Error al guardar evento: " + e.getMessage());
        }
        return false;
    }

    public List<Evento> obtenerTodos() {
        List<Evento> eventos = new ArrayList<>();
        // ⛔ CAMBIAR: Quitar "WHERE activo = TRUE" para cargar TODOS los eventos
        String sql = "SELECT * FROM eventos ORDER BY fecha_hora_inicio DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                eventos.add(mapResultSetToEvento(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener eventos: " + e.getMessage());
        }
        return eventos;
    }

    public List<Evento> obtenerProximos() {
        List<Evento> eventos = new ArrayList<>();
        // ⛔ CAMBIAR: Quitar "WHERE activo = TRUE" para cargar TODOS los eventos próximos
        String sql = "SELECT * FROM eventos WHERE fecha_hora_inicio > datetime('now') " +
                "ORDER BY fecha_hora_inicio ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                eventos.add(mapResultSetToEvento(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener eventos próximos: " + e.getMessage());
        }
        return eventos;
    }

    public Optional<Evento> obtenerPorId(Integer id) {
        // ⛔ CAMBIAR: Quitar "AND activo = TRUE" para poder ver eventos inactivos
        String sql = "SELECT * FROM eventos WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEvento(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener evento por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean actualizar(Evento evento) {
        String sql = "UPDATE eventos SET nombre = ?, tipo = ?, descripcion = ?, fecha_hora_inicio = ?, " +
                "fecha_hora_fin = ?, lugar = ?, responsable = ?, capacidad_maxima = ?, " +
                "requiere_inscripcion = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, evento.getNombre());
            stmt.setString(2, evento.getTipo());
            stmt.setString(3, evento.getDescripcion());
            // ✅ CORRECCIÓN: Para SQLite, usar string
            stmt.setString(4, evento.getFechaHoraInicio().toString().replace('T', ' '));
            stmt.setString(5, evento.getFechaHoraFin().toString().replace('T', ' '));
            stmt.setString(6, evento.getLugar());
            stmt.setString(7, evento.getResponsable());
            stmt.setObject(8, evento.getCapacidadMaxima(), Types.INTEGER);
            stmt.setBoolean(9, evento.getRequiereInscripcion());
            stmt.setInt(10, evento.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al actualizar evento: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(Integer id) {
        // ⛔ CAMBIAR: DELETE real en lugar de marcar como inactivo
        String sql = "DELETE FROM eventos WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al eliminar evento: " + e.getMessage());
            return false;
        }
    }

    // MÉTODOS PARA PARTICIPANTES DE EVENTOS

    public boolean inscribirParticipante(ParticipanteEvento participante) {
        String sql = "INSERT INTO participantes_evento (tipo_participante, evento_id, estudiante_id, docente_id, codigo_qr) " +
                "VALUES (?, ?, ?, ?, ?)";

        // ✅ AGREGAR DEBUG TEMPORAL
        System.out.println("🔍 DEBUG INSCRIPCIÓN:");
        System.out.println("   Tipo: " + participante.getTipoParticipante());
        System.out.println("   Evento ID: " + participante.getEventoId());
        System.out.println("   Estudiante ID: " + participante.getEstudianteId());
        System.out.println("   Docente ID: " + participante.getDocenteId());
        System.out.println("   QR: " + participante.getCodigoQR());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, participante.getTipoParticipante());
            stmt.setInt(2, participante.getEventoId());
            stmt.setObject(3, participante.getEstudianteId(), Types.INTEGER);
            stmt.setObject(4, participante.getDocenteId(), Types.INTEGER);
            stmt.setString(5, participante.getCodigoQR());

            int result = stmt.executeUpdate();
            System.out.println("✅ Inserción ejecutada, filas afectadas: " + result);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error al inscribir participante: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean registrarAsistenciaEvento(Integer participanteId) {
        String sql = "UPDATE participantes_evento SET asistio = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, participanteId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al registrar asistencia a evento: " + e.getMessage());
            return false;
        }
    }

    public List<ParticipanteEvento> obtenerParticipantesPorEvento(Integer eventoId) {
        // ✅ AGREGAR DEBUG
        System.out.println("🔍 Obteniendo participantes para evento: " + eventoId);

        List<ParticipanteEvento> participantes = new ArrayList<>();
        String sql = "SELECT p.*, e.nombre as estudiante_nombre, e.apellido as estudiante_apellido, " +
                "d.nombre as docente_nombre, d.apellido as docente_apellido " +
                "FROM participantes_evento p " +
                "LEFT JOIN estudiantes e ON p.estudiante_id = e.id " +
                "LEFT JOIN docentes d ON p.docente_id = d.id " +
                "WHERE p.evento_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventoId);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                participantes.add(mapResultSetToParticipante(rs));
                count++;
            }
            System.out.println("📊 Participantes encontrados: " + count);

        } catch (SQLException e) {
            log.severe("Error al obtener participantes del evento: " + e.getMessage());
        }
        return participantes;
    }

    public int contarParticipantesPorEvento(Integer eventoId) {
        String sql = "SELECT COUNT(*) FROM participantes_evento WHERE evento_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.severe("Error al contar participantes del evento: " + e.getMessage());
        }
        return 0;
    }

    // ✅ MÉTODO CORREGIDO PARA MAPEAR EVENTOS
    private Evento mapResultSetToEvento(ResultSet rs) throws SQLException {
        String fechaInicioStr = rs.getString("fecha_hora_inicio");
        String fechaFinStr = rs.getString("fecha_hora_fin");

        LocalDateTime fechaInicio = null;
        LocalDateTime fechaFin = null;

        try {
            if (fechaInicioStr != null) {
                // Convertir formato SQLite a LocalDateTime
                fechaInicioStr = fechaInicioStr.replace(' ', 'T');
                fechaInicio = LocalDateTime.parse(fechaInicioStr);
            }
            if (fechaFinStr != null) {
                fechaFinStr = fechaFinStr.replace(' ', 'T');
                fechaFin = LocalDateTime.parse(fechaFinStr);
            }
        } catch (Exception e) {
            log.warning("Error parsing date from SQLite: " + e.getMessage());
            // Usar fechas por defecto si hay error
            fechaInicio = LocalDateTime.now();
            fechaFin = LocalDateTime.now().plusHours(2);
        }

        return Evento.builder()
                .id(rs.getInt("id"))
                .nombre(rs.getString("nombre"))
                .tipo(rs.getString("tipo"))
                .descripcion(rs.getString("descripcion"))
                .fechaHoraInicio(fechaInicio)
                .fechaHoraFin(fechaFin)
                .lugar(rs.getString("lugar"))
                .responsable(rs.getString("responsable"))
                .capacidadMaxima(rs.getObject("capacidad_maxima") != null ? rs.getInt("capacidad_maxima") : null)
                .requiereInscripcion(rs.getBoolean("requiere_inscripcion"))
                .activo(rs.getBoolean("activo"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion") != null ?
                        rs.getTimestamp("fecha_creacion").toLocalDateTime() : LocalDateTime.now())
                .build();
    }

    private ParticipanteEvento mapResultSetToParticipante(ResultSet rs) throws SQLException {
        return ParticipanteEvento.builder()
                .id(rs.getInt("id"))
                .tipoParticipante(rs.getString("tipo_participante"))
                .asistio(rs.getBoolean("asistio"))
                .fechaInscripcion(rs.getTimestamp("fecha_inscripcion").toLocalDateTime())
                .codigoQR(rs.getString("codigo_qr"))
                .eventoId(rs.getInt("evento_id"))
                .estudianteId(rs.getObject("estudiante_id") != null ? rs.getInt("estudiante_id") : null)
                .docenteId(rs.getObject("docente_id") != null ? rs.getInt("docente_id") : null)
                .estudiante(rs.getObject("estudiante_id") != null ?
                        Estudiante.builder()
                                .nombre(rs.getString("estudiante_nombre"))
                                .apellido(rs.getString("estudiante_apellido"))
                                .build() : null)
                .docente(rs.getObject("docente_id") != null ?
                        Docente.builder()
                                .nombre(rs.getString("docente_nombre"))
                                .apellido(rs.getString("docente_apellido"))
                                .build() : null)
                .build();
    }

    /**
     * Obtiene un evento por su ID
     */
    public Optional<Evento> obtenerEventoPorId(Integer eventoId) {
        String sql = "SELECT * FROM eventos WHERE id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEvento(rs));
            }
        } catch (SQLException e) {
            log.severe("❌ Error al obtener evento por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Verifica si un evento existe y está activo
     */
    public boolean eventoExiste(Integer eventoId) {
        String sql = "SELECT COUNT(*) FROM eventos WHERE id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.severe("❌ Error al verificar evento: " + e.getMessage());
        }
        return false;
    }

}
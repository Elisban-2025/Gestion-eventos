package com.union.asistencia.dao;

import com.union.asistencia.model.Aula;
import lombok.extern.java.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class AulaDAO {

    public boolean guardar(Aula aula) {
        String sql = "INSERT INTO aulas (codigo_aula, nombre, edificio, capacidad, tipo, equipamiento, disponible, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, aula.getCodigoAula());
            stmt.setString(2, aula.getNombre());
            stmt.setString(3, aula.getEdificio());
            stmt.setInt(4, aula.getCapacidad());
            stmt.setString(5, aula.getTipo());
            stmt.setString(6, aula.getEquipamiento());
            stmt.setBoolean(7, aula.getDisponible());
            stmt.setString(8, aula.getObservaciones());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        aula.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            log.severe("Error al guardar aula: " + e.getMessage());
        }
        return false;
    }

    public List<Aula> obtenerTodas() {
        List<Aula> aulas = new ArrayList<>();
        String sql = "SELECT * FROM aulas ORDER BY edificio, nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                aulas.add(mapResultSetToAula(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener aulas: " + e.getMessage());
        }
        return aulas;
    }

    public List<Aula> obtenerDisponibles() {
        List<Aula> aulas = new ArrayList<>();
        String sql = "SELECT * FROM aulas WHERE disponible = TRUE ORDER BY edificio, nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                aulas.add(mapResultSetToAula(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener aulas disponibles: " + e.getMessage());
        }
        return aulas;
    }

    public Optional<Aula> obtenerPorId(Integer id) {
        String sql = "SELECT * FROM aulas WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToAula(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener aula por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Aula> obtenerPorCodigo(String codigoAula) {
        String sql = "SELECT * FROM aulas WHERE codigo_aula = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoAula);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToAula(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener aula por código: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean actualizar(Aula aula) {
        String sql = "UPDATE aulas SET codigo_aula = ?, nombre = ?, edificio = ?, capacidad = ?, " +
                "tipo = ?, equipamiento = ?, disponible = ?, observaciones = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, aula.getCodigoAula());
            stmt.setString(2, aula.getNombre());
            stmt.setString(3, aula.getEdificio());
            stmt.setInt(4, aula.getCapacidad());
            stmt.setString(5, aula.getTipo());
            stmt.setString(6, aula.getEquipamiento());
            stmt.setBoolean(7, aula.getDisponible());
            stmt.setString(8, aula.getObservaciones());
            stmt.setInt(9, aula.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al actualizar aula: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(Integer id) {
        String sql = "DELETE FROM aulas WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al eliminar aula: " + e.getMessage());
            return false;
        }
    }

    public List<Aula> buscarPorEdificio(String edificio) {
        List<Aula> aulas = new ArrayList<>();
        String sql = "SELECT * FROM aulas WHERE edificio LIKE ? ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + edificio + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                aulas.add(mapResultSetToAula(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al buscar aulas por edificio: " + e.getMessage());
        }
        return aulas;
    }

    public List<Aula> buscarPorTipo(String tipo) {
        List<Aula> aulas = new ArrayList<>();
        String sql = "SELECT * FROM aulas WHERE tipo = ? ORDER BY edificio, nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                aulas.add(mapResultSetToAula(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al buscar aulas por tipo: " + e.getMessage());
        }
        return aulas;
    }

    private Aula mapResultSetToAula(ResultSet rs) throws SQLException {
        return Aula.builder()
                .id(rs.getInt("id"))
                .codigoAula(rs.getString("codigo_aula"))
                .nombre(rs.getString("nombre"))
                .edificio(rs.getString("edificio"))
                .capacidad(rs.getInt("capacidad"))
                .tipo(rs.getString("tipo"))
                .equipamiento(rs.getString("equipamiento"))
                .disponible(rs.getBoolean("disponible"))
                .observaciones(rs.getString("observaciones"))
                .build();
    }
}
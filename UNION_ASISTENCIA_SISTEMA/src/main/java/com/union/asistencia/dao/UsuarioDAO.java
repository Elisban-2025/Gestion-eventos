package com.union.asistencia.dao;

import com.union.asistencia.model.Usuario;
import com.union.asistencia.util.PasswordUtils;
import lombok.extern.java.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class UsuarioDAO {

    public boolean probarConexion() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            log.severe("Error probando conexión a BD: " + e.getMessage());
            return false;
        }
    }

    public Optional<Usuario> autenticar(String username, String password) {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                // ✅ VERIFICACIÓN MEJORADA Y CORREGIDA
                System.out.println("=== DEBUG AUTENTICACIÓN ===");
                System.out.println("Usuario: " + username);
                System.out.println("Contraseña ingresada: " + password);
                System.out.println("Hash almacenado: " + storedHash);

                // Usar PasswordUtils.checkPassword que maneja ambos casos
                if (PasswordUtils.checkPassword(password, storedHash)) {
                    System.out.println("✅ Autenticación exitosa");
                    return Optional.of(mapResultSetToUsuario(rs));
                } else {
                    System.out.println("❌ Las contraseñas NO coinciden");
                }
            } else {
                System.out.println("❌ Usuario no encontrado: " + username);
            }
        } catch (SQLException e) {
            log.severe("Error en autenticación: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean crearUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, nombre, apellido, email, rol) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, PasswordUtils.hashPassword(usuario.getPasswordHash()));
            stmt.setString(3, usuario.getNombre());
            stmt.setString(4, usuario.getApellido());
            stmt.setString(5, usuario.getEmail());
            stmt.setString(6, usuario.getRol());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al crear usuario: " + e.getMessage());
            return false;
        }
    }

    public List<Usuario> obtenerTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY fecha_creacion DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    // ✅ MÉTODO NUEVO: Obtener solo usuarios activos
    public List<Usuario> obtenerActivos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE activo = true ORDER BY fecha_creacion DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            log.severe("Error al obtener usuarios activos: " + e.getMessage());
        }
        return usuarios;
    }

    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET username = ?, nombre = ?, apellido = ?, email = ?, " +
                "rol = ?, activo = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getApellido());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getRol());
            stmt.setBoolean(6, usuario.getActivo());
            stmt.setInt(7, usuario.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarPassword(Integer usuarioId, String nuevaPassword) {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, PasswordUtils.hashPassword(nuevaPassword));
            stmt.setInt(2, usuarioId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al actualizar contraseña: " + e.getMessage());
            return false;
        }
    }

    public boolean usuarioExiste(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.severe("Error al verificar usuario: " + e.getMessage());
        }
        return false;
    }

    public boolean bloquearUsuario(String username) {
        String sql = "UPDATE usuarios SET activo = FALSE WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al bloquear usuario: " + e.getMessage());
            return false;
        }
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        return Usuario.builder()
                .id(rs.getInt("id"))
                .username(rs.getString("username"))
                .passwordHash(rs.getString("password_hash"))
                .nombre(rs.getString("nombre"))
                .apellido(rs.getString("apellido"))
                .email(rs.getString("email"))
                .rol(rs.getString("rol"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime())
                .activo(rs.getBoolean("activo"))
                .build();
    }

    // ========== MÉTODOS NUEVOS PARA GENERACIÓN AUTOMÁTICA ==========

    public boolean existeUsername(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("Error al verificar username: " + e.getMessage());
            return false;
        }
    }

    public boolean existeUsuarioPorDocenteId(String docenteId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE docente_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, docenteId);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("Error al verificar usuario por docente: " + e.getMessage());
            return false;
        }
    }

    public boolean existeUsuarioPorEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("Error al verificar usuario por email: " + e.getMessage());
            return false;
        }
    }

    public Usuario obtenerPorUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND activo = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUsuario(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por username: " + e.getMessage());
        }
        return null;
    }

    public boolean crearUsuarioConDocenteId(Usuario usuario, String docenteId) {
        String sql = "INSERT INTO usuarios (username, password_hash, nombre, apellido, email, rol, activo, docente_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getPasswordHash());
            stmt.setString(3, usuario.getNombre());
            stmt.setString(4, usuario.getApellido());
            stmt.setString(5, usuario.getEmail());
            stmt.setString(6, usuario.getRol());
            stmt.setBoolean(7, usuario.getActivo());
            stmt.setString(8, docenteId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.severe("Error al crear usuario con docente_id: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(Integer usuarioId) {
        String sql = "UPDATE usuarios SET activo = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.severe("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
}
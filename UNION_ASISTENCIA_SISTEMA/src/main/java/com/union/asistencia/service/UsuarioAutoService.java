package com.union.asistencia.service;

import com.union.asistencia.model.Docente;
import com.union.asistencia.model.Usuario;
import com.union.asistencia.dao.UsuarioDAO;
import com.union.asistencia.util.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioAutoService {

    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Genera un usuario automáticamente para un docente
     */
    public static Usuario generarUsuarioParaDocente(Docente docente) {
        Usuario usuario = new Usuario();

        // Generar username único (ej: nombre.apellido)
        String usernameBase = generarUsername(docente.getNombre(), docente.getApellido());
        String username = generarUsernameUnico(usernameBase);

        // Generar contraseña temporal
        String passwordTemp = generarPasswordTemporal();

        usuario.setUsername(username);
        usuario.setPasswordHash(PasswordUtils.hashPassword(passwordTemp));
        usuario.setNombre(docente.getNombre());
        usuario.setApellido(docente.getApellido());
        usuario.setEmail(docente.getEmail());
        usuario.setRol("DOCENTE");
        usuario.setActivo(true);

        System.out.println("✅ Usuario generado para docente: " + docente.getNombreCompleto());
        System.out.println("👤 Username: " + username);
        System.out.println("🔑 Password temporal: " + passwordTemp);

        return usuario;
    }

    /**
     * Genera username base (nombre.apellido)
     */
    private static String generarUsername(String nombre, String apellido) {
        if (nombre == null || apellido == null) {
            return "docente.user";
        }

        String nombreBase = nombre.split(" ")[0].toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u")
                .replaceAll("[^a-z]", "");

        String apellidoBase = apellido.split(" ")[0].toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u")
                .replaceAll("[^a-z]", "");

        if (nombreBase.isEmpty()) nombreBase = "user";
        if (apellidoBase.isEmpty()) apellidoBase = "docente";

        return nombreBase + "." + apellidoBase;
    }

    /**
     * Verifica que el username sea único y genera variantes si es necesario
     */
    private static String generarUsernameUnico(String usernameBase) {
        String username = usernameBase;
        int contador = 1;

        while (usuarioDAO.existeUsername(username)) {
            username = usernameBase + contador;
            contador++;
            if (contador > 100) {
                // Fallback: usar timestamp
                username = usernameBase + System.currentTimeMillis() % 1000;
                break;
            }
        }

        return username;
    }

    /**
     * Genera una contraseña temporal segura
     */
    private static String generarPasswordTemporal() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * caracteres.length());
            password.append(caracteres.charAt(index));
        }

        return password.toString();
    }

    /**
     * Crea usuario para docente y lo guarda en la base de datos
     */
    public static boolean crearUsuarioParaDocente(Docente docente) {
        try {
            // Verificar si ya existe usuario para este docente por email
            if (usuarioDAO.existeUsuarioPorEmail(docente.getEmail())) {
                System.out.println("⚠️ Ya existe usuario para docente: " + docente.getNombreCompleto());
                return true;
            }

            Usuario usuario = generarUsuarioParaDocente(docente);
            return usuarioDAO.crearUsuario(usuario);

        } catch (Exception e) {
            System.err.println("❌ Error al crear usuario para docente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método para crear usuario con docente_id específico
     */
    public static boolean crearUsuarioParaDocenteConId(Docente docente) {
        String sql = "INSERT INTO usuarios (username, password_hash, nombre, apellido, email, rol, activo, docente_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = com.union.asistencia.dao.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String usernameBase = generarUsername(docente.getNombre(), docente.getApellido());
            String username = generarUsernameUnico(usernameBase);
            String passwordTemp = generarPasswordTemporal();

            pstmt.setString(1, username);
            pstmt.setString(2, PasswordUtils.hashPassword(passwordTemp));
            pstmt.setString(3, docente.getNombre());
            pstmt.setString(4, docente.getApellido());
            pstmt.setString(5, docente.getEmail());
            pstmt.setString(6, "DOCENTE");
            pstmt.setBoolean(7, true);
            pstmt.setString(8, docente.getCodigoDocente());

            int resultado = pstmt.executeUpdate();

            if (resultado > 0) {
                System.out.println("✅ Usuario creado automáticamente para docente: " + docente.getNombreCompleto());
                System.out.println("👤 Username: " + username);
                System.out.println("🔑 Password: " + passwordTemp);
                System.out.println("📋 Código docente: " + docente.getCodigoDocente());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al crear usuario con docente_id: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si existe un usuario para el docente
     */
    public static boolean existeUsuarioParaDocente(String codigoDocente) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE docente_id = ?";

        try (Connection conn = com.union.asistencia.dao.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigoDocente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al verificar usuario para docente: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene las credenciales del usuario generado para un docente
     */
    public static String obtenerCredencialesDocente(String codigoDocente) {
        String sql = "SELECT username, password_hash FROM usuarios WHERE docente_id = ?";

        try (Connection conn = com.union.asistencia.dao.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigoDocente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                String passwordHash = rs.getString("password_hash");
                return "Usuario: " + username + "\nContraseña: [Generada automáticamente]";
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener credenciales: " + e.getMessage());
        }
        return "No se encontraron credenciales para el docente";
    }
}
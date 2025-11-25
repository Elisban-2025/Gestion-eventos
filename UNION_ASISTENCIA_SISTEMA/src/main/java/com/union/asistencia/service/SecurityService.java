package com.union.asistencia.service;

import com.union.asistencia.dao.UsuarioDAO;
import com.union.asistencia.model.Usuario;
import com.union.asistencia.util.PasswordUtils;
import lombok.extern.java.Log;

import java.util.Optional;

@Log
public class SecurityService {

    private final UsuarioDAO usuarioDAO;

    public SecurityService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public Optional<Usuario> autenticarUsuario(String username, String password) {
        return usuarioDAO.autenticar(username, password);
    }

    public boolean cambiarPassword(String username, String nuevaPassword) {
        if (!PasswordUtils.isPasswordStrong(nuevaPassword)) {
            log.warning("La nueva contraseña no cumple con los requisitos de seguridad");
            return false;
        }

        // En una implementación real, aquí se actualizaría la contraseña en la base de datos
        // Por ahora, simulamos la actualización
        String hashedPassword = PasswordUtils.hashPassword(nuevaPassword);
        log.info("Contraseña actualizada para el usuario: " + username);
        return true;
    }

    public boolean verificarFortalezaPassword(String password) {
        return PasswordUtils.isPasswordStrong(password);
    }

    public String generarPasswordTemporal() {
        return PasswordUtils.generateRandomPassword();
    }

    public boolean usuarioTienePermiso(Usuario usuario, String permiso) {
        if (usuario == null) return false;

        // Lógica de permisos basada en el rol
        switch (usuario.getRol()) {
            case "ADMIN":
                return true; // Los administradores tienen todos los permisos
            case "DOCENTE":
                return permiso.equals("GESTION_ASISTENCIA") ||
                        permiso.equals("CONSULTAR_REPORTES") ||
                        permiso.equals("GESTION_EVENTOS");
            case "COORDINADOR":
                return permiso.equals("GESTION_ASISTENCIA") ||
                        permiso.equals("CONSULTAR_REPORTES") ||
                        permiso.equals("GESTION_EVENTOS") ||
                        permiso.equals("GESTION_DOCENTES") ||
                        permiso.equals("GESTION_ESTUDIANTES");
            default:
                return false;
        }
    }
}
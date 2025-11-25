package com.union.asistencia.util;

import lombok.extern.java.Log;

@Log
public class Validaciones {

    public static boolean validarDNI(String dni) {
        if (dni == null || dni.length() != 8) {
            return false;
        }
        try {
            Integer.parseInt(dni);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    public static boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return false;
        }
        // Eliminar espacios, guiones, paréntesis, etc.
        String telefonoLimpio = telefono.replaceAll("[^0-9]", "");
        return telefonoLimpio.length() >= 9 && telefonoLimpio.length() <= 12;
    }

    public static boolean validarCodigoEstudiante(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        // Formato: 20240001 (8 dígitos)
        return codigo.matches("^\\d{8}$");
    }

    public static boolean validarCodigoDocente(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        // Formato: DOC001 (3 letras y 3 dígitos)
        return codigo.matches("^[A-Z]{3}\\d{3}$");
    }

    public static boolean validarCodigoAsignatura(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        // Formato: IS001 (2 letras y 3 dígitos)
        return codigo.matches("^[A-Z]{2}\\d{3}$");
    }

    public static boolean validarSoloLetras(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }
        return texto.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");
    }

    public static boolean validarSoloNumeros(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }
        return texto.matches("^[0-9]+$");
    }

    public static boolean validarRangoEntero(int numero, int min, int max) {
        return numero >= min && numero <= max;
    }

    public static boolean validarLongitud(String texto, int min, int max) {
        if (texto == null) {
            return false;
        }
        int longitud = texto.trim().length();
        return longitud >= min && longitud <= max;
    }
}
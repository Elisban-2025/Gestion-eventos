package com.union.asistencia.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {

    // FORMATOS CORREGIDOS
    private static final DateTimeFormatter DATE_FORMATTER_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_SQLITE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER_SQLITE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDate parseDateFromSQLite(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        // Si es un timestamp numérico (como 1108702800000)
        if (dateString.matches("^\\d+$")) {
            try {
                long timestamp = Long.parseLong(dateString);
                // Si es timestamp en milisegundos
                if (timestamp > 1000000000000L) {
                    return LocalDateTime.ofEpochSecond(timestamp/1000, 0, java.time.ZoneOffset.UTC).toLocalDate();
                } else {
                    return LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.UTC).toLocalDate();
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing timestamp: " + dateString);
                return null;
            }
        }

        // Intentar diferentes formatos de fecha
        DateTimeFormatter[] formatters = {
                DATE_FORMATTER_SQLITE,
                DATE_FORMATTER_DISPLAY,
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // Continuar con el siguiente formatter
            }
        }

        System.err.println("No se pudo parsear la fecha: " + dateString);
        return null;
    }

    public static String formatDateForSQLite(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER_SQLITE);
    }

    public static String formatDateTimeForSQLite(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATE_TIME_FORMATTER_SQLITE);
    }

    // Resto de métodos existentes...
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER_DISPLAY);
    }

    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(TIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FORMATTER_DISPLAY);
    }

    public static LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER_DISPLAY);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDateTime parseDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER_DISPLAY);
        } catch (Exception e) {
            return null;
        }
    }
}
package com.union.asistencia.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.java.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Log
public class ExportUtils {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // ============================================
    // MÉTODOS PÚBLICOS DE EXPORTACIÓN
    // ============================================

    /**
     * Exporta una TableView a formato CSV con manejo seguro de tipos genéricos
     * @param tabla TableView a exportar
     * @param nombreArchivo Nombre base del archivo (sin extensión)
     * @param <T> Tipo de datos de la tabla
     */
    public static <T> void exportarTablaACSV(TableView<T> tabla, String nombreArchivo) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte CSV");
            fileChooser.setInitialFileName(nombreArchivo + "_" +
                    LocalDateTime.now().format(DATE_FORMATTER) + ".csv");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
            );

            File archivo = fileChooser.showSaveDialog(new Stage());
            if (archivo == null) {
                return;
            }

            FileWriter escritor = new FileWriter(archivo);

            // ESCRIBIR ENCABEZADOS
            List<TableColumn<T, ?>> columnas = tabla.getColumns();
            int totalColumnas = columnas.size();

            for (int i = 0; i < totalColumnas; i++) {
                String header = columnas.get(i).getText();
                escritor.write(escapeCSV(header));
                if (i < totalColumnas - 1) {
                    escritor.write(",");
                }
            }
            escritor.write("\n");

            // ESCRIBIR DATOS - CON MANEJO SEGURO DE TIPOS
            List<T> items = tabla.getItems();
            int totalFilas = items.size();

            for (int filaIndex = 0; filaIndex < totalFilas; filaIndex++) {
                T filaItem = items.get(filaIndex);

                for (int colIndex = 0; colIndex < totalColumnas; colIndex++) {
                    TableColumn<T, ?> columna = columnas.get(colIndex);

                    // LÍNEA CORREGIDA - Usa método auxiliar seguro
                    Object valorCelda = getCellDataSafe(columna, filaItem);

                    String valor = (valorCelda != null) ? valorCelda.toString() : "";
                    escritor.write(escapeCSV(valor));

                    if (colIndex < totalColumnas - 1) {
                        escritor.write(",");
                    }
                }
                escritor.write("\n");
            }

            escritor.close();

            mostrarAlertaExito("Exportación completada",
                    "Se exportaron " + totalFilas + " registros a:\n" + archivo.getAbsolutePath());

            log.info("Exportación exitosa: " + archivo.getAbsolutePath());

        } catch (IOException e) {
            log.severe("Error de E/S al exportar CSV: " + e.getMessage());
            mostrarAlertaError("Error de exportación",
                    "No se pudo guardar el archivo: " + e.getMessage());
        } catch (Exception e) {
            log.severe("Error inesperado al exportar CSV: " + e.getMessage());
            mostrarAlertaError("Error de exportación",
                    "Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Versión alternativa más segura sin tipos genéricos estrictos
     * @param tabla TableView a exportar
     * @param nombreArchivo Nombre base del archivo
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void exportarTablaACSVSeguro(TableView<?> tabla, String nombreArchivo) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte CSV");
            fileChooser.setInitialFileName(nombreArchivo + "_" +
                    LocalDateTime.now().format(DATE_FORMATTER) + ".csv");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
            );

            File archivo = fileChooser.showSaveDialog(new Stage());
            if (archivo == null) return;

            FileWriter escritor = new FileWriter(archivo);

            // ENCABEZADOS
            List columnas = tabla.getColumns();
            int numColumnas = columnas.size();
            for (int i = 0; i < numColumnas; i++) {
                TableColumn columna = (TableColumn) columnas.get(i);
                escritor.write(escapeCSV(columna.getText()));
                if (i < numColumnas - 1) escritor.write(",");
            }
            escritor.write("\n");

            // DATOS - MANEJO SEGURO SIN TIPADO ESTRICTO
            List items = tabla.getItems();
            int numFilas = items.size();

            for (int i = 0; i < numFilas; i++) {
                Object item = items.get(i);

                for (int j = 0; j < numColumnas; j++) {
                    try {
                        TableColumn columna = (TableColumn) columnas.get(j);
                        Object valor = columna.getCellData(item);
                        String texto = (valor != null) ? valor.toString() : "";
                        escritor.write(escapeCSV(texto));
                    } catch (Exception e) {
                        log.warning("Error en celda [" + i + "," + j + "]: " + e.getMessage());
                        escritor.write("");
                    }

                    if (j < numColumnas - 1) escritor.write(",");
                }
                escritor.write("\n");
            }

            escritor.close();

            mostrarAlertaExito("Exportación exitosa",
                    numFilas + " registros exportados a: " + archivo.getAbsolutePath());

            log.info("Exportación CSV segura completada: " + archivo.getAbsolutePath());

        } catch (IOException e) {
            log.severe("Error de E/S en exportación: " + e.getMessage());
            mostrarAlertaError("Error", "Exportación fallida: " + e.getMessage());
        } catch (Exception e) {
            log.severe("Error inesperado en exportación: " + e.getMessage());
            mostrarAlertaError("Error", "Exportación fallida: " + e.getMessage());
        }
    }

    /**
     * Exporta datos de texto plano a un archivo
     * @param datos Contenido del texto a exportar
     * @param nombreArchivo Nombre base del archivo
     */
    public static void exportarDatosTexto(String datos, String nombreArchivo) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Texto");
            fileChooser.setInitialFileName(nombreArchivo + "_" +
                    LocalDateTime.now().format(DATE_FORMATTER) + ".txt");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt")
            );

            File archivo = fileChooser.showSaveDialog(new Stage());
            if (archivo == null) return;

            FileWriter escritor = new FileWriter(archivo);
            escritor.write(datos);
            escritor.close();

            mostrarAlertaExito("Exportación completada",
                    "Archivo guardado en: " + archivo.getAbsolutePath());

            log.info("Texto exportado: " + archivo.getAbsolutePath());

        } catch (IOException e) {
            log.severe("Error al exportar texto: " + e.getMessage());
            mostrarAlertaError("Error de exportación",
                    "No se pudo guardar el archivo: " + e.getMessage());
        } catch (Exception e) {
            log.severe("Error inesperado al exportar texto: " + e.getMessage());
            mostrarAlertaError("Error de exportación",
                    "Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Exporta un reporte con formato profesional (encabezado y pie de página)
     * @param titulo Título del reporte
     * @param contenido Contenido del reporte
     * @param nombreArchivo Nombre base del archivo
     */
    public static void exportarReporteFormateado(String titulo, String contenido, String nombreArchivo) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte");
            fileChooser.setInitialFileName(nombreArchivo + "_" +
                    LocalDateTime.now().format(DATE_FORMATTER) + ".txt");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"),
                    new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );

            File archivo = fileChooser.showSaveDialog(new Stage());
            if (archivo == null) return;

            PrintWriter writer = new PrintWriter(new FileWriter(archivo));

            // Encabezado del reporte
            writer.println("=".repeat(80));
            writer.println(centrarTexto(titulo, 80));
            writer.println("=".repeat(80));
            writer.println("Fecha de generación: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            ));
            writer.println("Sistema de Control de Asistencia - UPeU");
            writer.println("=".repeat(80));
            writer.println();

            // Contenido del reporte
            writer.println(contenido);

            // Pie de página
            writer.println();
            writer.println("=".repeat(80));
            writer.println("Fin del reporte");
            writer.println("=".repeat(80));

            writer.close();

            mostrarAlertaExito("Exportación completada",
                    "Reporte guardado en: " + archivo.getAbsolutePath());
            log.info("Reporte exportado exitosamente: " + archivo.getAbsolutePath());

        } catch (IOException e) {
            log.severe("Error de E/S al exportar reporte: " + e.getMessage());
            mostrarAlertaError("Error de exportación",
                    "No se pudo guardar el reporte: " + e.getMessage());
        } catch (Exception e) {
            log.severe("Error inesperado al exportar reporte: " + e.getMessage());
            mostrarAlertaError("Error de exportación",
                    "Error inesperado: " + e.getMessage());
        }
    }

    // ============================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ============================================

    /**
     * Obtiene datos de una celda de forma segura, manejando problemas de tipos genéricos
     * Este método soluciona el error de ClassCastException que puede ocurrir
     * cuando se trabaja con TableColumn<T, ?> y wildcards genéricos
     *
     * @param columna Columna de la tabla
     * @param item Item/fila de la tabla
     * @param <T> Tipo de datos del item
     * @return Valor de la celda o null si hay error
     */
    @SuppressWarnings("unchecked")
    private static <T> Object getCellDataSafe(TableColumn<T, ?> columna, T item) {
        try {
            // Intento directo de obtener datos
            return columna.getCellData(item);
        } catch (ClassCastException e) {
            // Si falla por problema de tipos, usar método alternativo
            try {
                var observableValue = columna.getCellObservableValue(item);
                return (observableValue != null) ? observableValue.getValue() : null;
            } catch (Exception ex) {
                log.warning("Error al obtener valor de celda: " + ex.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.warning("Error inesperado al obtener datos de celda: " + e.getMessage());
            return null;
        }
    }

    /**
     * Escapa caracteres especiales para formato CSV según RFC 4180
     * @param value Valor a escapar
     * @return Valor escapado y entrecomillado si es necesario
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";

        // Escapar comillas dobles duplicándolas
        String escaped = value.replace("\"", "\"\"");

        // Entrecomillar si contiene caracteres especiales
        if (value.contains(",") || value.contains("\"") ||
                value.contains("\n") || value.contains("\r")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }

    /**
     * Centra un texto dentro de un ancho especificado con espacios
     * @param texto Texto a centrar
     * @param ancho Ancho total deseado
     * @return Texto centrado
     */
    private static String centrarTexto(String texto, int ancho) {
        if (texto == null) texto = "";

        if (texto.length() >= ancho) {
            return texto.substring(0, ancho);
        }

        int espaciosIzquierda = (ancho - texto.length()) / 2;
        return " ".repeat(espaciosIzquierda) + texto;
    }

    /**
     * Muestra un diálogo de alerta de éxito
     * @param titulo Título de la alerta
     * @param mensaje Mensaje detallado
     */
    private static void mostrarAlertaExito(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de alerta de error
     * @param titulo Título de la alerta
     * @param mensaje Mensaje detallado del error
     */
    private static void mostrarAlertaError(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
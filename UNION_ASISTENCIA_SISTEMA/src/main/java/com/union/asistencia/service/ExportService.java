package com.union.asistencia.service;

import com.union.asistencia.util.ExportUtils;
import lombok.extern.java.Log;

@Log
public class ExportService {

    public void exportarReporteAsistencia(String reporteTexto, String nombreArchivo) {
        ExportUtils.exportarDatosTexto(reporteTexto, nombreArchivo);
    }

    public void exportarReporteAsistenciaFormateado(String titulo, String contenido, String nombreArchivo) {
        ExportUtils.exportarReporteFormateado(titulo, contenido, nombreArchivo);
    }

    public void exportarDatosEstudiantes() {
        // En una implementación real, aquí se obtendrían los datos de la base de datos
        // y se exportarían usando ExportUtils
        log.info("Exportando datos de estudiantes...");

        // Ejemplo de implementación:
        String datosEstudiantes = generarReporteEstudiantes();
        ExportUtils.exportarReporteFormateado(
                "Reporte de Estudiantes",
                datosEstudiantes,
                "Estudiantes_UPeU"
        );
    }

    public void exportarDatosDocentes() {
        log.info("Exportando datos de docentes...");

        String datosDocentes = generarReporteDocentes();
        ExportUtils.exportarReporteFormateado(
                "Reporte de Docentes",
                datosDocentes,
                "Docentes_UPeU"
        );
    }

    public void exportarDatosAsignaturas() {
        log.info("Exportando datos de asignaturas...");

        String datosAsignaturas = generarReporteAsignaturas();
        ExportUtils.exportarReporteFormateado(
                "Reporte de Asignaturas",
                datosAsignaturas,
                "Asignaturas_UPeU"
        );
    }

    public void exportarDatosEventos() {
        log.info("Exportando datos de eventos...");

        String datosEventos = generarReporteEventos();
        ExportUtils.exportarReporteFormateado(
                "Reporte de Eventos",
                datosEventos,
                "Eventos_UPeU"
        );
    }

    // Métodos de ejemplo para generar reportes
    private String generarReporteEstudiantes() {
        return """
               TOTAL DE ESTUDIANTES: 150
               
               Por carrera:
               - Ingeniería de Sistemas: 45
               - Contabilidad: 35
               - Enfermería: 40
               - Derecho: 20
               - Psicología: 10
               
               Distribución por semestre:
               - Semestre 1: 30
               - Semestre 2: 25
               - Semestre 3: 35
               - Semestre 4: 30
               - Semestre 5: 20
               - Semestre 6: 10
               """;
    }

    private String generarReporteDocentes() {
        return """
               TOTAL DE DOCENTES: 25
               
               Por facultad:
               - Ingeniería: 8
               - Ciencias de la Salud: 6
               - Ciencias Empresariales: 5
               - Humanidades: 4
               - Derecho: 2
               
               Carga horaria promedio: 36 horas/semana
               """;
    }

    private String generarReporteAsignaturas() {
        return """
               TOTAL DE ASIGNATURAS: 45
               
               Distribución por tipo:
               - Teoría: 25
               - Práctica: 12
               - Laboratorio: 8
               
               Créditos promedio: 4 créditos
               """;
    }

    private String generarReporteEventos() {
        return """
               EVENTOS PROGRAMADOS: 8
               
               Próximos eventos:
               - Semana de la Ingeniería: 15-17 Mayo 2024
               - Festival Cultural: 10 Junio 2024
               - Conferencia IA: 20 Abril 2024
               
               Eventos por tipo:
               - Académicos: 5
               - Culturales: 2
               - Deportivos: 1
               """;
    }
}
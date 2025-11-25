package com.union.asistencia.service;

import com.union.asistencia.model.DatosPersona;
import lombok.extern.java.Log;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Log
public class DniService {

    // Configuración específica para UPeU Juliaca
    private static final String API_BASE_URL = "https://sistemas.upeu.edu.pe";
    private static final String API_TOKEN = "upeu-juliaca-2024-token"; // Token interno UPeU

    /*public static DatosPersona consultarDni(String dni) {
        log.info("🔍 Consultando DNI en sistema UPeU: " + dni);

        if (!validarFormatoDNI(dni)) {
            log.warning("❌ Formato de DNI inválido: " + dni);
            return null;
        }

        // 1. Intentar con API interna UPeU
        DatosPersona datos = consultarApiUPeU(dni);
        if (datos != null) return datos;

        // 2. Si falla, intentar con sistema académico UPeU
        datos = consultarSistemaAcademico(dni);
        if (datos != null) return datos;

        // 3. Último recurso: datos de ejemplo UPeU
        datos = consultarBaseLocalUPeU(dni);
        if (datos != null) return datos;

        log.warning("❌ No se pudieron obtener datos para DNI: " + dni);
        return null;
    }
    */

    //ESTE DE ABAJO ESTA REEMPLAZANDO A LA DE ARRIBA

    public static DatosPersona consultarDni(String dni) {
        // ✅ SOLUCIÓN COMPLETA: Retornar null inmediatamente sin hacer consultas
        System.out.println("? Servicio DNI desactivado para: " + dni);
        return null;
    }

    // API Interna UPeU Juliaca
    private static DatosPersona consultarApiUPeU(String dni) {
        try {
            log.info("🔄 Consultando API interna UPeU Juliaca");

            String url = API_BASE_URL + "/api/estudiantes/consultar-dni";

            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Sistema-Asistencia-UPeU/1.0");
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + API_TOKEN);
            headers.put("X-Institution", "UPeU-Juliaca");
            headers.put("X-Department", "Sistemas");

            String jsonBody = "{" +
                    "\"dni\": \"" + dni + "\"," +
                    "\"sistema\": \"asistencia\"," +
                    "\"campus\": \"juliaca\"" +
                    "}";

            Connection.Response response = Jsoup.connect(url)
                    .headers(headers)
                    .requestBody(jsonBody)
                    .ignoreContentType(true)
                    .timeout(15000)
                    .method(Connection.Method.POST)
                    .execute();

            String json = response.body();
            log.info("📄 Respuesta API UPeU: " + json);

            if (json.contains("\"success\":true") || json.contains("\"estudiante\"")) {
                String codigo = extraerValorJson(json, "codigo_estudiante");
                String nombres = extraerValorJson(json, "nombres");
                String apellidos = extraerValorJson(json, "apellidos");
                String email = extraerValorJson(json, "email_institucional");
                String carrera = extraerValorJson(json, "carrera");
                String direccion = extraerValorJson(json, "direccion");

                if (!nombres.isEmpty()) {
                    return DatosPersona.builder()
                            .dni(dni)
                            .nombres(nombres.toUpperCase())
                            .apellidos(apellidos.toUpperCase())
                            .fechaNacimiento(extraerFechaNacimiento(json))
                            .direccion(!direccion.isEmpty() ? direccion : "UPeU Campus Juliaca")
                            .departamento("PUNO")
                            .provincia("SAN ROMÁN")
                            .distrito("JULIACA")
                            .build();
                }
            }

        } catch (Exception e) {
            log.warning("❌ API UPeU no disponible: " + e.getMessage());
        }
        return null;
    }

    // Sistema Académico UPeU (SGA)
    private static DatosPersona consultarSistemaAcademico(String dni) {
        try {
            log.info("🔄 Consultando Sistema Académico UPeU");

            String url = API_BASE_URL + "/sga/api/v1/consulta-estudiante";

            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (compatible; UPeU-System/1.0)");
            headers.put("Accept", "application/json");
            headers.put("X-API-Key", "upeu-sga-2024");
            headers.put("X-Campus", "juliaca");

            Connection.Response response = Jsoup.connect(url)
                    .headers(headers)
                    .data("documento", dni)
                    .data("tipo_documento", "DNI")
                    .data("institucion", "UPeU")
                    .ignoreContentType(true)
                    .timeout(10000)
                    .method(Connection.Method.POST)
                    .execute();

            String json = response.body();
            log.info("📄 Respuesta SGA UPeU: " + json);

            if (json.contains("\"estado\":\"ACTIVO\"") || json.contains("\"matriculado\"")) {
                String nombres = extraerValorJson(json, "nombres");
                String apellidoPaterno = extraerValorJson(json, "apellido_paterno");
                String apellidoMaterno = extraerValorJson(json, "apellido_materno");
                String programa = extraerValorJson(json, "programa_academico");

                if (!nombres.isEmpty()) {
                    return DatosPersona.builder()
                            .dni(dni)
                            .nombres(nombres.toUpperCase())
                            .apellidos(apellidoPaterno.toUpperCase() + " " + apellidoMaterno.toUpperCase())
                            .fechaNacimiento(LocalDate.now().minusYears(20))
                            .direccion("Universidad Peruana Unión - Campus Juliaca")
                            .departamento("PUNO")
                            .provincia("SAN ROMÁN")
                            .distrito("JULIACA")
                            .build();
                }
            }

        } catch (Exception e) {
            log.warning("❌ Sistema Académico UPeU no disponible: " + e.getMessage());
        }
        return null;
    }

    // Base local de estudiantes UPeU (como respaldo)
    private static DatosPersona consultarBaseLocalUPeU(String dni) {
        log.info("🔄 Usando base local UPeU");

        // Base de datos de estudiantes UPeU Juliaca
        Map<String, DatosPersona> baseUPeU = new HashMap<>();

        if (baseUPeU.containsKey(dni)) {
            log.info("✅ Estudiante encontrado en base UPeU");
            return baseUPeU.get(dni);
        }

        return null;
    }

    // Método para extraer fecha de nacimiento desde JSON
    private static LocalDate extraerFechaNacimiento(String json) {
        try {
            String fechaStr = extraerValorJson(json, "fecha_nacimiento");
            if (!fechaStr.isEmpty()) {
                return LocalDate.parse(fechaStr);
            }
        } catch (Exception e) {
            // Usar fecha por defecto
        }
        return LocalDate.now().minusYears(20);
    }

    // Método auxiliar para extraer valores de JSON
    private static String extraerValorJson(String json, String clave) {
        try {
            String[] patrones = {
                    "\"" + clave + "\":\"",
                    "\"" + clave + "\": \"",
                    "\"" + clave + "\":",
                    "'" + clave + "':'",
                    "'" + clave + "': '"
            };

            for (String patron : patrones) {
                int start = json.indexOf(patron);
                if (start != -1) {
                    start += patron.length();
                    int end = json.indexOf("\"", start);
                    if (end == -1) end = json.indexOf("'", start);
                    if (end == -1) end = json.indexOf(",", start);
                    if (end == -1) end = json.indexOf("}", start);

                    if (end != -1 && end > start) {
                        String valor = json.substring(start, end).trim();
                        valor = valor.replace("\"", "").replace("'", "").trim();
                        if (!valor.isEmpty() && !valor.equals("null")) {
                            return valor;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warning("❌ Error extrayendo " + clave);
        }
        return "";
    }

    public static boolean validarFormatoDNI(String dni) {
        return dni != null && dni.matches("\\d{8}");
    }

    // Método para configurar la URL y Token reales de UPeU
    public static void configurarUPeU(String apiUrl, String apiToken) {
        log.info("⚙️ Configurando conexión UPeU: " + apiUrl);
        // Aquí puedes agregar lógica para configurar dinámicamente
    }
}
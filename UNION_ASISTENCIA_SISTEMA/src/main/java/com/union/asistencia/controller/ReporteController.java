package com.union.asistencia.controller;

import com.union.asistencia.dao.ReporteDAO;
import com.union.asistencia.model.Usuario;
import com.union.asistencia.util.ExportUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.java.Log;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Log
public class ReporteController extends BaseController {

    @FXML private TableView<Map<String, Object>> tableView;
    @FXML private ComboBox<String> cbxTipoReporte;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private ComboBox<Integer> cbxMes;
    @FXML private ComboBox<Integer> cbxAnio;
    @FXML private Button btnGenerar;
    @FXML private Button btnExportar;

    private ReporteDAO reporteDAO;
    private Usuario usuarioLogueado;
    private ObservableList<Map<String, Object>> reporteData;

    @Override
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        inicializar();
    }

    @FXML
    private void initialize() {
        reporteDAO = new ReporteDAO();
        reporteData = FXCollections.observableArrayList();

        configurarCombobox();
        configurarEventos();

        tableView.setItems(reporteData);
    }

    private void inicializar() {
        // Configurar fechas por defecto
        dpFechaInicio.setValue(LocalDate.now().withDayOfMonth(1));
        dpFechaFin.setValue(LocalDate.now());
    }

    private void configurarCombobox() {
        cbxTipoReporte.getItems().addAll(
                "Asistencia Mensual",
                "Asistencia por Asignatura",
                "Estadísticas Generales",
                "Participación en Eventos"
        );

        // Meses
        for (int i = 1; i <= 12; i++) {
            cbxMes.getItems().add(i);
        }
        cbxMes.setValue(LocalDate.now().getMonthValue());

        // Años (últimos 5 años)
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear + 2; i++) {
            cbxAnio.getItems().add(i);
        }
        cbxAnio.setValue(currentYear);
    }

    private void configurarEventos() {
        cbxTipoReporte.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        actualizarVisibilidadControles(newVal);
                    }
                });
    }

    private void actualizarVisibilidadControles(String tipoReporte) {
        switch (tipoReporte) {
            case "Asistencia Mensual":
                cbxMes.setDisable(false);
                cbxAnio.setDisable(false);
                dpFechaInicio.setDisable(true);
                dpFechaFin.setDisable(true);
                break;
            case "Asistencia por Asignatura":
            case "Estadísticas Generales":
            case "Participación en Eventos":
                cbxMes.setDisable(true);
                cbxAnio.setDisable(true);
                dpFechaInicio.setDisable(false);
                dpFechaFin.setDisable(false);
                break;
        }
    }

    @FXML
    private void generarReporte() {
        String tipoReporte = cbxTipoReporte.getValue();
        if (tipoReporte == null) {
            mostrarAlerta("Advertencia", "Seleccione un tipo de reporte", Alert.AlertType.WARNING);
            return;
        }

        try {
            List<Map<String, Object>> datos;

            switch (tipoReporte) {
                case "Asistencia Mensual":
                    int mes = cbxMes.getValue();
                    int anio = cbxAnio.getValue();
                    datos = reporteDAO.generarReporteAsistenciaMensual(mes, anio);
                    break;
                case "Asistencia por Asignatura":
                    // Necesitaríamos un ComboBox para seleccionar asignatura, por simplicidad usamos la primera
                    // En un caso real, deberíamos tener un ComboBox de asignaturas
                    datos = reporteDAO.generarReporteAsistenciaPorAsignatura(1,
                            dpFechaInicio.getValue(), dpFechaFin.getValue());
                    break;
                case "Estadísticas Generales":
                    Map<String, Object> estadisticas = reporteDAO.generarEstadisticasGenerales(
                            dpFechaInicio.getValue(), dpFechaFin.getValue());
                    // Convertir a lista para mostrar en tabla
                    datos = List.of(estadisticas);
                    break;
                case "Participación en Eventos":
                    datos = reporteDAO.generarReporteParticipacionEventos(
                            dpFechaInicio.getValue(), dpFechaFin.getValue());
                    break;
                default:
                    datos = List.of();
            }

            reporteData.setAll(datos);
            configurarColumnas(datos);

        } catch (Exception e) {
            log.severe("Error al generar reporte: " + e.getMessage());
            mostrarAlerta("Error", "No se pudo generar el reporte: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void configurarColumnas(List<Map<String, Object>> datos) {
        tableView.getColumns().clear();

        if (!datos.isEmpty()) {
            Map<String, Object> primeraFila = datos.get(0);
            for (String key : primeraFila.keySet()) {
                TableColumn<Map<String, Object>, String> columna = new TableColumn<>(key);

                // ✅ CORRECCIÓN: Usar CellValueFactory personalizado para Map
                columna.setCellValueFactory(cellData -> {
                    Map<String, Object> item = cellData.getValue();
                    Object value = item.get(key);
                    return new SimpleStringProperty(value != null ? value.toString() : "");
                });

                tableView.getColumns().add(columna);
            }

            System.out.println("✅ Columnas configuradas: " + tableView.getColumns().size());
        } else {
            System.out.println("ℹ️ No hay datos para configurar columnas");
        }
    }

    @FXML
    private void exportarReporte() {
        if (reporteData.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay datos para exportar", Alert.AlertType.WARNING);
            return;
        }

        String tipoReporte = cbxTipoReporte.getValue();
        String nombreArchivo = "Reporte_" + (tipoReporte != null ? tipoReporte.replace(" ", "_") : "General");
        ExportUtils.exportarTablaACSV(tableView, nombreArchivo);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
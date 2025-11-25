package com.union.asistencia.controller;

import com.union.asistencia.dao.HorarioDAO;
import com.union.asistencia.dao.AsignaturaDAO;
import com.union.asistencia.dao.DocenteDAO;
import com.union.asistencia.dao.AulaDAO;
import com.union.asistencia.model.Horario;
import com.union.asistencia.model.Asignatura;
import com.union.asistencia.model.Docente;
import com.union.asistencia.model.Aula;
import com.union.asistencia.model.Usuario;
import com.union.asistencia.util.ExportUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.java.Log;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;

@Log
public class HorarioController extends BaseController {

    @FXML private TableView<Horario> tableView;
    @FXML private TableColumn<Horario, Integer> colId;
    @FXML private TableColumn<Horario, String> colDia;
    @FXML private TableColumn<Horario, String> colHoraInicio;
    @FXML private TableColumn<Horario, String> colHoraFin;
    @FXML private TableColumn<Horario, String> colTipo;
    @FXML private TableColumn<Horario, String> colAula;
    @FXML private TableColumn<Horario, String> colAsignatura;
    @FXML private TableColumn<Horario, String> colDocente;

    @FXML private ComboBox<DayOfWeek> cbxDia;
    @FXML private ComboBox<LocalTime> cbxHoraInicio;
    @FXML private ComboBox<LocalTime> cbxHoraFin;
    @FXML private ComboBox<String> cbxTipo;
    @FXML private ComboBox<String> cbxAula;
    @FXML private ComboBox<Asignatura> cbxAsignatura;
    @FXML private ComboBox<Docente> cbxDocente;
    @FXML private TextField txtBuscar;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnExportar;

    private ObservableList<Horario> horariosList;
    private ObservableList<Asignatura> asignaturasList;
    private ObservableList<Docente> docentesList;
    private ObservableList<String> aulasList;

    private HorarioDAO horarioDAO;
    private AsignaturaDAO asignaturaDAO;
    private DocenteDAO docenteDAO;
    private AulaDAO aulaDAO;

    private Usuario usuarioLogueado;
    private Horario horarioSeleccionado;

    @Override
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        inicializar();
    }

    @FXML
    private void initialize() {
        horarioDAO = new HorarioDAO();
        asignaturaDAO = new AsignaturaDAO();
        docenteDAO = new DocenteDAO();
        aulaDAO = new AulaDAO();

        horariosList = FXCollections.observableArrayList();
        asignaturasList = FXCollections.observableArrayList();
        docentesList = FXCollections.observableArrayList();
        aulasList = FXCollections.observableArrayList();

        horarioSeleccionado = null;

        configurarTabla();
        configurarCombobox();
        configurarEventos();

        tableView.setItems(horariosList);
    }

    private void inicializar() {
        cargarDatos();
        limpiarFormulario();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Día de la semana - convertido a texto legible
        colDia.setCellValueFactory(cellData -> {
            DayOfWeek dia = cellData.getValue().getDiaSemana();
            if (dia != null) {
                String nombreDia = dia.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault());
                // Capitalizar primera letra
                nombreDia = nombreDia.substring(0, 1).toUpperCase() + nombreDia.substring(1);
                return new SimpleStringProperty(nombreDia);
            }
            return new SimpleStringProperty("");
        });

        // Hora de inicio - formateada
        colHoraInicio.setCellValueFactory(cellData -> {
            LocalTime hora = cellData.getValue().getHoraInicio();
            return new SimpleStringProperty(hora != null ? hora.toString() : "");
        });

        // Hora de fin - formateada
        colHoraFin.setCellValueFactory(cellData -> {
            LocalTime hora = cellData.getValue().getHoraFin();
            return new SimpleStringProperty(hora != null ? hora.toString() : "");
        });

        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colAula.setCellValueFactory(new PropertyValueFactory<>("aula"));

        // Asignatura y Docente (si son objetos complejos)
        colAsignatura.setCellValueFactory(cellData -> {
            // Asumiendo que tienes un método para obtener el nombre de la asignatura
            // Si no, puedes usar: cellData.getValue().getAsignaturaId() y buscar el nombre
            String nombreAsignatura = "Asignatura no encontrada";
            try {
                Asignatura asignatura = asignaturasList.stream()
                        .filter(a -> a.getId().equals(cellData.getValue().getAsignaturaId()))
                        .findFirst()
                        .orElse(null);
                nombreAsignatura = asignatura != null ? asignatura.getNombre() : "No asignada";
            } catch (Exception e) {
                log.warning("Error al cargar nombre de asignatura: " + e.getMessage());
            }
            return new SimpleStringProperty(nombreAsignatura);
        });

        colDocente.setCellValueFactory(cellData -> {
            // Asumiendo que tienes un método para obtener el nombre del docente
            String nombreDocente = "Docente no encontrado";
            try {
                Docente docente = docentesList.stream()
                        .filter(d -> d.getId().equals(cellData.getValue().getDocenteId()))
                        .findFirst()
                        .orElse(null);
                nombreDocente = docente != null ? docente.getNombreCompleto() : "No asignado";
            } catch (Exception e) {
                log.warning("Error al cargar nombre de docente: " + e.getMessage());
            }
            return new SimpleStringProperty(nombreDocente);
        });
    }

    private void configurarCombobox() {
        // Días de la semana
        cbxDia.getItems().addAll(DayOfWeek.values());

        // Horas
        ObservableList<LocalTime> horas = FXCollections.observableArrayList();
        for (int i = 7; i <= 22; i++) {
            horas.add(LocalTime.of(i, 0));
            horas.add(LocalTime.of(i, 30));
        }
        cbxHoraInicio.setItems(horas);
        cbxHoraFin.setItems(horas);

        // Tipos
        cbxTipo.getItems().addAll("TEORIA", "PRACTICA", "LABORATORIO");

        // Aulas disponibles
        aulasList.setAll(aulaDAO.obtenerDisponibles().stream()
                .map(Aula::getCodigoAula)
                .toList());
        cbxAula.setItems(aulasList);

        // Asignaturas
        asignaturasList.setAll(asignaturaDAO.obtenerTodos());
        cbxAsignatura.setItems(asignaturasList);
        cbxAsignatura.setCellFactory(param -> new ListCell<Asignatura>() {
            @Override
            protected void updateItem(Asignatura item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreCompleto());
            }
        });

        // Docentes
        docentesList.setAll(docenteDAO.obtenerTodos());
        cbxDocente.setItems(docentesList);
        cbxDocente.setCellFactory(param -> new ListCell<Docente>() {
            @Override
            protected void updateItem(Docente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreCompleto());
            }
        });
    }

    private void configurarEventos() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        horarioSeleccionado = newSelection;
                        cargarDatosFormulario(newSelection);
                    }
                });

        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && tableView.getSelectionModel().getSelectedItem() == null) {
                limpiarSeleccion();
            }
        });
    }

    private void cargarDatos() {
        horariosList.setAll(horarioDAO.obtenerTodos());
    }

    @FXML
    private void guardarHorario() {
        if (validarFormulario()) {
            Horario horario;
            boolean esNuevo = false;

            if (horarioSeleccionado == null) {
                horario = new Horario();
                esNuevo = true;
            } else {
                horario = horarioSeleccionado;
            }

            horario.setDiaSemana(cbxDia.getValue());
            horario.setHoraInicio(cbxHoraInicio.getValue());
            horario.setHoraFin(cbxHoraFin.getValue());
            horario.setTipo(cbxTipo.getValue());
            horario.setAula(cbxAula.getValue());
            horario.setAsignaturaId(cbxAsignatura.getValue().getId());
            horario.setDocenteId(cbxDocente.getValue().getId());

            // Verificar conflicto de horario
            if (horarioDAO.existeConflictoHorario(horario)) {
                mostrarAlerta("Conflicto de Horario",
                        "Ya existe un horario programado en el mismo día, aula o docente en el rango de horas seleccionado.",
                        Alert.AlertType.ERROR);
                return;
            }

            boolean exito;
            if (esNuevo) {
                exito = horarioDAO.guardar(horario);
            } else {
                exito = horarioDAO.actualizar(horario);
            }

            if (exito) {
                mostrarAlerta("Éxito",
                        esNuevo ? "Horario registrado correctamente" : "Horario actualizado correctamente",
                        Alert.AlertType.INFORMATION);
                limpiarFormulario();
                cargarDatos();
            } else {
                mostrarAlerta("Error",
                        esNuevo ? "No se pudo registrar el horario" : "No se pudo actualizar el horario",
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void eliminarHorario() {
        if (horarioSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("¿Está seguro de eliminar el horario?");
            alert.setContentText("Esta acción no se puede deshacer.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (horarioDAO.eliminar(horarioSeleccionado.getId())) {
                    mostrarAlerta("Éxito", "Horario eliminado correctamente", Alert.AlertType.INFORMATION);
                    limpiarFormulario();
                    cargarDatos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el horario", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Seleccione un horario para eliminar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void cancelar() {
        limpiarFormulario();
    }

    @FXML
    private void buscarHorarios() {
        String criterio = txtBuscar.getText().trim();
        if (criterio.isEmpty()) {
            cargarDatos();
        } else {
            // Implementar búsqueda segura
            ObservableList<Horario> listaFiltrada = FXCollections.observableArrayList();

            for (Horario horario : horariosList) {
                // Buscar por día, aula, asignatura o docente
                String dia = horario.getDiaSemana() != null ?
                        horario.getDiaSemana().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()) : "";
                String aula = horario.getAula() != null ? horario.getAula() : "";
                String tipo = horario.getTipo() != null ? horario.getTipo() : "";

                // Buscar nombre de asignatura
                String nombreAsignatura = "";
                if (horario.getAsignaturaId() != null) {
                    Asignatura asignatura = asignaturasList.stream()
                            .filter(a -> a.getId().equals(horario.getAsignaturaId()))
                            .findFirst()
                            .orElse(null);
                    nombreAsignatura = asignatura != null ? asignatura.getNombre() : "";
                }

                // Buscar nombre de docente
                String nombreDocente = "";
                if (horario.getDocenteId() != null) {
                    Docente docente = docentesList.stream()
                            .filter(d -> d.getId().equals(horario.getDocenteId()))
                            .findFirst()
                            .orElse(null);
                    nombreDocente = docente != null ? docente.getNombreCompleto() : "";
                }

                if (dia.toLowerCase().contains(criterio.toLowerCase()) ||
                        aula.toLowerCase().contains(criterio.toLowerCase()) ||
                        tipo.toLowerCase().contains(criterio.toLowerCase()) ||
                        nombreAsignatura.toLowerCase().contains(criterio.toLowerCase()) ||
                        nombreDocente.toLowerCase().contains(criterio.toLowerCase())) {
                    listaFiltrada.add(horario);
                }
            }

            tableView.getItems().setAll(listaFiltrada);
        }
    }

    @FXML
    private void exportarDatos() {
        if (tableView.getItems().isEmpty()) {
            mostrarAlerta("Advertencia", "No hay datos para exportar", Alert.AlertType.WARNING);
            return;
        }

        ExportUtils.exportarTablaACSVSeguro(tableView, "Reporte_Horarios_UPeU");
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (cbxDia.getValue() == null) {
            errores.append("• El día de la semana es obligatorio\n");
        }

        if (cbxHoraInicio.getValue() == null) {
            errores.append("• La hora de inicio es obligatoria\n");
        }

        if (cbxHoraFin.getValue() == null) {
            errores.append("• La hora de fin es obligatoria\n");
        } else if (cbxHoraInicio.getValue() != null &&
                cbxHoraFin.getValue().isBefore(cbxHoraInicio.getValue())) {
            errores.append("• La hora de fin debe ser posterior a la hora de inicio\n");
        }

        if (cbxTipo.getValue() == null) {
            errores.append("• El tipo de clase es obligatorio\n");
        }

        if (cbxAula.getValue() == null) {
            errores.append("• El aula es obligatoria\n");
        }

        if (cbxAsignatura.getValue() == null) {
            errores.append("• La asignatura es obligatoria\n");
        }

        if (cbxDocente.getValue() == null) {
            errores.append("• El docente es obligatorio\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de Validación", "Por favor corrija los siguientes errores:\n\n" + errores.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void cargarDatosFormulario(Horario horario) {
        cbxDia.setValue(horario.getDiaSemana());
        cbxHoraInicio.setValue(horario.getHoraInicio());
        cbxHoraFin.setValue(horario.getHoraFin());
        cbxTipo.setValue(horario.getTipo());
        cbxAula.setValue(horario.getAula());

        Asignatura asignatura = asignaturasList.stream()
                .filter(a -> a.getId().equals(horario.getAsignaturaId()))
                .findFirst()
                .orElse(null);
        cbxAsignatura.setValue(asignatura);

        Docente docente = docentesList.stream()
                .filter(d -> d.getId().equals(horario.getDocenteId()))
                .findFirst()
                .orElse(null);
        cbxDocente.setValue(docente);

        btnGuardar.setText("💾 Actualizar Horario");
    }

    private void limpiarFormulario() {
        cbxDia.getSelectionModel().clearSelection();
        cbxHoraInicio.getSelectionModel().clearSelection();
        cbxHoraFin.getSelectionModel().clearSelection();
        cbxTipo.getSelectionModel().clearSelection();
        cbxAula.getSelectionModel().clearSelection();
        cbxAsignatura.getSelectionModel().clearSelection();
        cbxDocente.getSelectionModel().clearSelection();
        txtBuscar.clear();

        limpiarSeleccion();
    }

    private void limpiarSeleccion() {
        tableView.getSelectionModel().clearSelection();
        horarioSeleccionado = null;
        btnGuardar.setText("💾 Guardar Horario");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
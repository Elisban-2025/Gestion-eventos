package com.union.asistencia.controller;

import com.union.asistencia.dao.AsignaturaDAO;
import com.union.asistencia.dao.DocenteDAO;
import com.union.asistencia.model.Asignatura;
import com.union.asistencia.model.Docente;
import com.union.asistencia.model.Usuario;
import com.union.asistencia.util.ExportUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.java.Log;

@Log
public class AsignaturaController extends BaseController {

    @FXML private TableView<Asignatura> tableView;
    @FXML private TableColumn<Asignatura, Integer> colId;
    @FXML private TableColumn<Asignatura, String> colCodigo;
    @FXML private TableColumn<Asignatura, String> colNombre;
    @FXML private TableColumn<Asignatura, Integer> colCreditos;
    @FXML private TableColumn<Asignatura, String> colCiclo;
    @FXML private TableColumn<Asignatura, String> colFacultad;
    @FXML private TableColumn<Asignatura, String> colDocente;

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private Spinner<Integer> spnCreditos;
    @FXML private Spinner<Integer> spnHorasTeoria;
    @FXML private Spinner<Integer> spnHorasPractica;
    @FXML private TextField txtCiclo;
    @FXML private ComboBox<String> cbxFacultad;
    @FXML private TextField txtPlanEstudios;
    @FXML private ComboBox<Docente> cbxDocente;
    @FXML private TextField txtBuscar;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnExportar;

    private ObservableList<Asignatura> asignaturasList;
    private ObservableList<Docente> docentesList;
    private AsignaturaDAO asignaturaDAO;
    private DocenteDAO docenteDAO;
    private Usuario usuarioLogueado;
    private Asignatura asignaturaSeleccionada;

    @Override
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        inicializar();
    }

    @FXML
    private void initialize() {
        asignaturaDAO = new AsignaturaDAO();
        docenteDAO = new DocenteDAO();
        asignaturasList = FXCollections.observableArrayList();
        docentesList = FXCollections.observableArrayList();
        asignaturaSeleccionada = null;

        configurarTabla();
        configurarCombobox();
        configurarSpinners();
        configurarEventos();

        tableView.setItems(asignaturasList);
    }

    private void inicializar() {
        cargarDatos();
        limpiarFormulario();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoAsignatura"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCreditos.setCellValueFactory(new PropertyValueFactory<>("creditos"));
        colCiclo.setCellValueFactory(new PropertyValueFactory<>("ciclo"));
        colFacultad.setCellValueFactory(new PropertyValueFactory<>("facultad"));

        // CORRECCIÓN: Configurar columna docente para mostrar nombre del docente
        colDocente.setCellValueFactory(cellData -> {
            Asignatura asignatura = cellData.getValue();
            if (asignatura.getDocenteId() != null) {
                // Buscar el docente en la lista
                for (Docente docente : docentesList) {
                    if (docente.getId().equals(asignatura.getDocenteId())) {
                        return new javafx.beans.property.SimpleStringProperty(
                                docente.getNombreCompleto()
                        );
                    }
                }
            }
            return new javafx.beans.property.SimpleStringProperty("No asignado");
        });
    }

    private void configurarCombobox() {
        cbxFacultad.getItems().addAll(
                "Ingeniería",
                "Ciencias de la Salud",
                "Ciencias Empresariales",
                "Humanidades",
                "Teología"
        );

        // Cargar docentes
        docentesList.setAll(docenteDAO.obtenerTodos());
        cbxDocente.setItems(docentesList);

        // Configurar display para docentes
        cbxDocente.setCellFactory(param -> new ListCell<Docente>() {
            @Override
            protected void updateItem(Docente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreCompleto() + " - " + item.getCodigoDocente());
            }
        });

        cbxDocente.setButtonCell(new ListCell<Docente>() {
            @Override
            protected void updateItem(Docente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreCompleto() + " - " + item.getCodigoDocente());
            }
        });
    }

    private void configurarSpinners() {
        SpinnerValueFactory<Integer> creditosFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 4);
        spnCreditos.setValueFactory(creditosFactory);

        SpinnerValueFactory<Integer> horasTeoriaFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 3);
        spnHorasTeoria.setValueFactory(horasTeoriaFactory);

        SpinnerValueFactory<Integer> horasPracticaFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 2);
        spnHorasPractica.setValueFactory(horasPracticaFactory);
    }

    private void configurarEventos() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        asignaturaSeleccionada = newSelection;
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
        asignaturasList.setAll(asignaturaDAO.obtenerTodos());
    }

    @FXML
    private void guardarAsignatura() {
        if (validarFormulario()) {
            Asignatura asignatura;
            boolean esNuevo = false;

            if (asignaturaSeleccionada == null) {
                asignatura = new Asignatura();
                esNuevo = true;
            } else {
                asignatura = asignaturaSeleccionada;
            }

            asignatura.setCodigoAsignatura(txtCodigo.getText().trim());
            asignatura.setNombre(txtNombre.getText().trim());
            asignatura.setCreditos(spnCreditos.getValue());
            asignatura.setHorasTeoria(spnHorasTeoria.getValue());
            asignatura.setHorasPractica(spnHorasPractica.getValue());
            asignatura.setCiclo(txtCiclo.getText().trim());
            asignatura.setFacultad(cbxFacultad.getValue());
            asignatura.setPlanEstudios(txtPlanEstudios.getText().trim());
            asignatura.setActivo(true);

            if (cbxDocente.getValue() != null) {
                asignatura.setDocenteId(cbxDocente.getValue().getId());
            }

            boolean exito;
            if (esNuevo) {
                exito = asignaturaDAO.guardar(asignatura);
            } else {
                exito = asignaturaDAO.actualizar(asignatura);
            }

            if (exito) {
                mostrarAlerta("Éxito",
                        esNuevo ? "Asignatura registrada correctamente" : "Asignatura actualizada correctamente",
                        Alert.AlertType.INFORMATION);
                limpiarFormulario();
                cargarDatos();
            } else {
                mostrarAlerta("Error",
                        esNuevo ? "No se pudo registrar la asignatura" : "No se pudo actualizar la asignatura",
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void eliminarAsignatura() {
        if (asignaturaSeleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("¿Está seguro de eliminar la asignatura?");
            alert.setContentText("Esta acción no se puede deshacer.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (asignaturaDAO.eliminar(asignaturaSeleccionada.getId())) {
                    mostrarAlerta("Éxito", "Asignatura eliminada correctamente", Alert.AlertType.INFORMATION);
                    limpiarFormulario();
                    cargarDatos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la asignatura", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Seleccione una asignatura para eliminar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void cancelar() {
        limpiarFormulario();
    }

    @FXML
    private void buscarAsignaturas() {
        String criterio = txtBuscar.getText().trim();
        if (criterio.isEmpty()) {
            cargarDatos();
        } else {
            asignaturasList.setAll(asignaturaDAO.buscarPorNombre(criterio));
        }
    }

    @FXML
    private void exportarDatos() {
        if (asignaturasList.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay datos para exportar", Alert.AlertType.WARNING);
            return;
        }
        ExportUtils.exportarTablaACSV(tableView, "Reporte_Asignaturas_UPeU");
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (txtCodigo.getText().trim().isEmpty()) {
            errores.append("• El código de asignatura es obligatorio\n");
        }

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre es obligatorio\n");
        }

        if (cbxFacultad.getValue() == null) {
            errores.append("• La facultad es obligatoria\n");
        }

        if (txtCiclo.getText().trim().isEmpty()) {
            errores.append("• El ciclo es obligatorio\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de Validación", "Por favor corrija los siguientes errores:\n\n" + errores.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void cargarDatosFormulario(Asignatura asignatura) {
        txtCodigo.setText(asignatura.getCodigoAsignatura());
        txtNombre.setText(asignatura.getNombre());
        spnCreditos.getValueFactory().setValue(asignatura.getCreditos());
        spnHorasTeoria.getValueFactory().setValue(asignatura.getHorasTeoria());
        spnHorasPractica.getValueFactory().setValue(asignatura.getHorasPractica());
        txtCiclo.setText(asignatura.getCiclo());
        cbxFacultad.setValue(asignatura.getFacultad());
        txtPlanEstudios.setText(asignatura.getPlanEstudios());

        if (asignatura.getDocenteId() != null) {
            Docente docente = docentesList.stream()
                    .filter(d -> d.getId().equals(asignatura.getDocenteId()))
                    .findFirst()
                    .orElse(null);
            cbxDocente.setValue(docente);
        }

        btnGuardar.setText("💾 Actualizar Asignatura");
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        spnCreditos.getValueFactory().setValue(4);
        spnHorasTeoria.getValueFactory().setValue(3);
        spnHorasPractica.getValueFactory().setValue(2);
        txtCiclo.clear();
        cbxFacultad.getSelectionModel().clearSelection();
        txtPlanEstudios.clear();
        cbxDocente.getSelectionModel().clearSelection();
        txtBuscar.clear();

        limpiarSeleccion();
    }

    private void limpiarSeleccion() {
        tableView.getSelectionModel().clearSelection();
        asignaturaSeleccionada = null;
        btnGuardar.setText("💾 Guardar Asignatura");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
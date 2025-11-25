package com.union.asistencia.controller;

import com.union.asistencia.dao.AulaDAO;
import com.union.asistencia.model.Aula;
import com.union.asistencia.model.Usuario;
import com.union.asistencia.util.ExportUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.java.Log;

import java.util.Optional;

@Log
public class AulaController extends BaseController {

    @FXML private TableView<Aula> tableView;
    @FXML private TableColumn<Aula, Integer> colId;
    @FXML private TableColumn<Aula, String> colCodigo;
    @FXML private TableColumn<Aula, String> colNombre;
    @FXML private TableColumn<Aula, String> colEdificio;
    @FXML private TableColumn<Aula, Integer> colCapacidad;
    @FXML private TableColumn<Aula, String> colTipo;
    @FXML private TableColumn<Aula, Boolean> colDisponible;

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEdificio;
    @FXML private Spinner<Integer> spnCapacidad;
    @FXML private ComboBox<String> cbxTipo;
    @FXML private TextArea txtEquipamiento;
    @FXML private CheckBox chkDisponible;
    @FXML private TextArea txtObservaciones;
    @FXML private TextField txtBuscar;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnEliminar;
    @FXML private Button btnExportar;
    @FXML private ComboBox<String> cbxTipoFiltro;

    private ObservableList<Aula> aulasList;
    private AulaDAO aulaDAO;
    private Usuario usuarioLogueado;
    private Aula aulaSeleccionada;

    @Override
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        inicializar();
    }

    @FXML
    private void initialize() {
        aulaDAO = new AulaDAO();
        aulasList = FXCollections.observableArrayList();
        aulaSeleccionada = null;

        configurarTabla();
        configurarCombobox();
        configurarSpinner();
        configurarEventos();
        configurarFiltros();

        tableView.setItems(aulasList);
    }

    @FXML
    private void inicializar() {
        cargarDatos();
        limpiarFormulario();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoAula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEdificio.setCellValueFactory(new PropertyValueFactory<>("edificio"));
        colCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidad"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colDisponible.setCellValueFactory(new PropertyValueFactory<>("disponible"));

        // Formatear columna de disponibilidad
        colDisponible.setCellFactory(tc -> new TableCell<Aula, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✅ Disponible" : "❌ Ocupada");
                    if (item) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void configurarCombobox() {
        cbxTipo.getItems().addAll(
                "AULA",
                "LABORATORIO",
                "AUDITORIO",
                "SALA DE CONFERENCIAS",
                "TALLER",
                "SALA DE COMPUTACIÓN",
                "SALA DE ESTUDIO"
        );
    }

    private void configurarFiltros() {
        cbxTipoFiltro.getItems().addAll("TODOS", "AULA", "LABORATORIO", "AUDITORIO", "SALA DE CONFERENCIAS");
        cbxTipoFiltro.setValue("TODOS");
    }

    private void configurarSpinner() {
        SpinnerValueFactory<Integer> capacidadFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 30);
        spnCapacidad.setValueFactory(capacidadFactory);
    }

    private void configurarEventos() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        aulaSeleccionada = newSelection;
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
        aulasList.setAll(aulaDAO.obtenerTodas());
    }

    @FXML
    private void guardarAula() {
        if (validarFormulario()) {
            Aula aula;
            boolean esNuevo = false;

            if (aulaSeleccionada == null) {
                aula = new Aula();
                esNuevo = true;
            } else {
                aula = aulaSeleccionada;
            }

            aula.setCodigoAula(txtCodigo.getText().trim());
            aula.setNombre(txtNombre.getText().trim());
            aula.setEdificio(txtEdificio.getText().trim());
            aula.setCapacidad(spnCapacidad.getValue());
            aula.setTipo(cbxTipo.getValue());
            aula.setEquipamiento(txtEquipamiento.getText().trim());
            aula.setDisponible(chkDisponible.isSelected());
            aula.setObservaciones(txtObservaciones.getText().trim());

            boolean exito;
            if (esNuevo) {
                exito = aulaDAO.guardar(aula);
            } else {
                exito = aulaDAO.actualizar(aula);
            }

            if (exito) {
                mostrarAlerta("Éxito",
                        esNuevo ? "Aula registrada correctamente" : "Aula actualizada correctamente",
                        Alert.AlertType.INFORMATION);
                limpiarFormulario();
                cargarDatos();
            } else {
                mostrarAlerta("Error",
                        esNuevo ? "No se pudo registrar el aula" : "No se pudo actualizar el aula",
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void eliminarAula() {
        if (aulaSeleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("¿Está seguro de eliminar el aula?");
            alert.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (aulaDAO.eliminar(aulaSeleccionada.getId())) {
                    mostrarAlerta("Éxito", "Aula eliminada correctamente", Alert.AlertType.INFORMATION);
                    limpiarFormulario();
                    cargarDatos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el aula", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Seleccione un aula para eliminar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void cancelar() {
        limpiarFormulario();
    }

    @FXML
    private void buscarAulas() {
        String criterio = txtBuscar.getText().trim();
        if (criterio.isEmpty()) {
            cargarDatos();
        } else {
            // ✅ CORRECCIÓN: Buscar en la base de datos en lugar de filtrar la lista actual
            aulasList.setAll(aulaDAO.buscarPorEdificio(criterio));

            // Si no se encontraron resultados por edificio, buscar por nombre o código
            if (aulasList.isEmpty()) {
                // Buscar en todas las aulas por nombre o código
                ObservableList<Aula> todasLasAulas = FXCollections.observableArrayList(aulaDAO.obtenerTodas());
                aulasList.setAll(todasLasAulas.filtered(a ->
                        a.getNombre().toLowerCase().contains(criterio.toLowerCase()) ||
                                a.getCodigoAula().toLowerCase().contains(criterio.toLowerCase())
                ));
            }
        }
    }

    // ✅ MÉTODO FALTANTE AGREGADO
    @FXML
    private void filtrarPorTipo() {
        String tipoSeleccionado = cbxTipoFiltro.getValue();
        if (tipoSeleccionado == null || "TODOS".equals(tipoSeleccionado)) {
            cargarDatos();
        } else {
            aulasList.setAll(aulaDAO.buscarPorTipo(tipoSeleccionado));
        }
    }

    // ✅ MÉTODO FALTANTE AGREGADO
    @FXML
    private void exportarDatos() {
        if (aulasList.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay datos para exportar", Alert.AlertType.WARNING);
            return;
        }
        ExportUtils.exportarTablaACSV(tableView, "Reporte_Aulas_UPeU");
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (txtCodigo.getText().trim().isEmpty()) {
            errores.append("• El código del aula es obligatorio\n");
        }

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre del aula es obligatorio\n");
        }

        if (txtEdificio.getText().trim().isEmpty()) {
            errores.append("• El edificio es obligatorio\n");
        }

        if (cbxTipo.getValue() == null) {
            errores.append("• El tipo de aula es obligatorio\n");
        }

        if (spnCapacidad.getValue() == null || spnCapacidad.getValue() <= 0) {
            errores.append("• La capacidad debe ser mayor a 0\n");
        }

        // Validar código único (solo para nuevas aulas)
        if (aulaSeleccionada == null) {
            Optional<Aula> aulaExistente = aulaDAO.obtenerPorCodigo(txtCodigo.getText().trim());
            if (aulaExistente.isPresent()) {
                errores.append("• El código del aula ya existe\n");
            }
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de Validación",
                    "Por favor corrija los siguientes errores:\n\n" + errores.toString(),
                    Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void cargarDatosFormulario(Aula aula) {
        txtCodigo.setText(aula.getCodigoAula());
        txtNombre.setText(aula.getNombre());
        txtEdificio.setText(aula.getEdificio());
        spnCapacidad.getValueFactory().setValue(aula.getCapacidad());
        cbxTipo.setValue(aula.getTipo());
        txtEquipamiento.setText(aula.getEquipamiento());
        chkDisponible.setSelected(aula.getDisponible());
        txtObservaciones.setText(aula.getObservaciones());

        btnGuardar.setText("💾 Actualizar Aula");
    }

    @FXML
    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtEdificio.clear();
        spnCapacidad.getValueFactory().setValue(30);
        cbxTipo.getSelectionModel().clearSelection();
        txtEquipamiento.clear();
        chkDisponible.setSelected(true);
        txtObservaciones.clear();
        txtBuscar.clear();

        limpiarSeleccion();
    }

    private void limpiarSeleccion() {
        tableView.getSelectionModel().clearSelection();
        aulaSeleccionada = null;
        btnGuardar.setText("💾 Guardar Aula");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ✅ MÉTODO ADICIONAL: Buscar aulas por múltiples criterios
    @FXML
    private void buscarAulasAvanzado() {
        String criterio = txtBuscar.getText().trim();
        if (criterio.isEmpty()) {
            cargarDatos();
            return;
        }

        // Intentar buscar por código primero
        Optional<Aula> aulaPorCodigo = aulaDAO.obtenerPorCodigo(criterio);
        if (aulaPorCodigo.isPresent()) {
            aulasList.setAll(FXCollections.observableArrayList(aulaPorCodigo.get()));
            return;
        }

        // Buscar por edificio
        aulasList.setAll(aulaDAO.buscarPorEdificio(criterio));
        if (!aulasList.isEmpty()) {
            return;
        }

        // Buscar por tipo
        aulasList.setAll(aulaDAO.buscarPorTipo(criterio.toUpperCase()));
        if (!aulasList.isEmpty()) {
            return;
        }

        // Si no encuentra por los métodos anteriores, buscar en toda la lista
        ObservableList<Aula> todasLasAulas = FXCollections.observableArrayList(aulaDAO.obtenerTodas());
        aulasList.setAll(todasLasAulas.filtered(a ->
                a.getNombre().toLowerCase().contains(criterio.toLowerCase()) ||
                        a.getCodigoAula().toLowerCase().contains(criterio.toLowerCase()) ||
                        a.getEdificio().toLowerCase().contains(criterio.toLowerCase()) ||
                        (a.getEquipamiento() != null && a.getEquipamiento().toLowerCase().contains(criterio.toLowerCase()))
        ));

        if (aulasList.isEmpty()) {
            mostrarAlerta("Búsqueda", "No se encontraron aulas con el criterio: " + criterio, Alert.AlertType.INFORMATION);
        }
    }

    // ✅ MÉTODO: Filtrar aulas disponibles
    @FXML
    private void filtrarAulasDisponibles() {
        aulasList.setAll(aulaDAO.obtenerDisponibles());
    }

    // ✅ MÉTODO: Generar código automático de aula
    @FXML
    private void generarCodigoAutomatico() {
        if (txtEdificio.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty()) {
            return;
        }

        String inicialEdificio = txtEdificio.getText().trim().substring(0, 1).toUpperCase();
        String nombreAula = txtNombre.getText().trim().toUpperCase().replace(" ", "");

        // Buscar el próximo número disponible
        int siguienteNumero = 1;
        for (Aula aula : aulasList) {
            if (aula.getCodigoAula().startsWith(inicialEdificio + "-" + nombreAula)) {
                try {
                    String[] partes = aula.getCodigoAula().split("-");
                    if (partes.length > 2) {
                        int num = Integer.parseInt(partes[2]);
                        if (num >= siguienteNumero) {
                            siguienteNumero = num + 1;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignorar códigos que no siguen el formato
                }
            }
        }

        String codigoGenerado = inicialEdificio + "-" + nombreAula + "-" + String.format("%02d", siguienteNumero);
        txtCodigo.setText(codigoGenerado);
    }
}
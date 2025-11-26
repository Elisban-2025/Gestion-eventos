package com.union.asistencia.controller;

import com.union.asistencia.dao.DocenteDAO;
import com.union.asistencia.model.Docente;
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
public class DocenteController extends BaseController {

    @FXML private TableView<Docente> tableView;
    @FXML private TableColumn<Docente, Integer> colId;
    @FXML private TableColumn<Docente, String> colCodigo;
    @FXML private TableColumn<Docente, String> colDni;
    @FXML private TableColumn<Docente, String> colNombre;
    @FXML private TableColumn<Docente, String> colApellido;
    @FXML private TableColumn<Docente, String> colEmail;
    @FXML private TableColumn<Docente, String> colFacultad;
    @FXML private TableColumn<Docente, String> colEspecialidad;
    @FXML private TableColumn<Docente, Integer> colCargaHoraria;

    @FXML private TextField txtCodigo;
    @FXML private TextField txtDni;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private ComboBox<String> cbxFacultad;
    @FXML private TextField txtEspecialidad;
    @FXML private Spinner<Integer> spnCargaHoraria;
    @FXML private DatePicker dpFechaContratacion;
    @FXML private TextField txtBuscar;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnEliminar;
    @FXML private Button btnExportar;

    private ObservableList<Docente> docentesList;
    private DocenteDAO docenteDAO;
    private Usuario usuarioLogueado;
    private Docente docenteSeleccionado;

    @Override
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        inicializar();
    }

    @FXML
    private void initialize() {
        docenteDAO = new DocenteDAO();
        docentesList = FXCollections.observableArrayList();
        docenteSeleccionado = null;

        configurarTabla();
        configurarCombobox();
        configurarSpinner();
        configurarEventos();

        tableView.setItems(docentesList);
    }

    @FXML
    private void inicializar() {
        cargarDatos();
        limpiarFormulario();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoDocente"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFacultad.setCellValueFactory(new PropertyValueFactory<>("facultad"));
        colEspecialidad.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
        colCargaHoraria.setCellValueFactory(new PropertyValueFactory<>("cargaHoraria"));
    }

    private void configurarCombobox() {
        cbxFacultad.getItems().addAll(
                "Ingeniería",
                "Ciencias de la Salud",
                "Ciencias Empresariales",
                "Educación",
                "Teología",
                "Enfermería",
                "Medicina",
                "Derecho"
        );
    }

    private void configurarSpinner() {
        SpinnerValueFactory<Integer> cargaHorariaFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60, 40);
        spnCargaHoraria.setValueFactory(cargaHorariaFactory);
    }

    private void configurarEventos() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        docenteSeleccionado = newSelection;
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
        docentesList.setAll(docenteDAO.obtenerTodos());
    }

    @FXML
    private void guardarDocente() {
        if (validarFormulario()) {
            Docente docente;
            boolean esNuevo = false;

            if (docenteSeleccionado == null) {
                docente = new Docente();
                esNuevo = true;

                // ✅ VALIDACIÓN DE CÓDIGO ÚNICO PARA NUEVOS DOCENTES
                if (!validarCodigoUnico(docente)) {
                    return;
                }
            } else {
                docente = docenteSeleccionado;

                // ✅ VALIDACIÓN DE CÓDIGO ÚNICO PARA ACTUALIZACIONES (si cambió el código)
                if (!docente.getCodigoDocente().equals(txtCodigo.getText().trim())) {
                    if (!validarCodigoUnico(docente)) {
                        return;
                    }
                }
            }

            docente.setCodigoDocente(txtCodigo.getText().trim());
            docente.setDni(txtDni.getText().trim());
            docente.setNombre(txtNombre.getText().trim());
            docente.setApellido(txtApellido.getText().trim());
            docente.setEmail(txtEmail.getText().trim());
            docente.setTelefono(txtTelefono.getText().trim());
            docente.setFacultad(cbxFacultad.getValue());
            docente.setEspecialidad(txtEspecialidad.getText().trim());
            docente.setCargaHoraria(spnCargaHoraria.getValue());
            docente.setFechaContratacion(dpFechaContratacion.getValue());
            docente.setActivo(true);

            boolean exito;
            if (esNuevo) {
                exito = docenteDAO.guardar(docente);

                // ✅ GENERAR USUARIO AUTOMÁTICO SOLO PARA NUEVOS DOCENTES
                if (exito) {
                    boolean usuarioCreado = com.union.asistencia.service.UsuarioAutoService.crearUsuarioParaDocenteConId(docente);
                    if (usuarioCreado) {
                        mostrarAlerta("Éxito Completo",
                                "✅ Docente registrado correctamente\n\n" +
                                        "👤 Usuario generado automáticamente\n" +
                                        "📧 Email: " + docente.getEmail() + "\n" +
                                        "🔑 Contraseña: Generada automáticamente\n\n" +
                                        "El docente puede iniciar sesión con sus credenciales",
                                Alert.AlertType.INFORMATION);
                    } else {
                        mostrarAlerta("Éxito Parcial",
                                "✅ Docente registrado correctamente\n\n" +
                                        "⚠️ Usuario no generado automáticamente\n" +
                                        "Contacte al administrador para crear usuario manual",
                                Alert.AlertType.INFORMATION);
                    }
                }
            } else {
                exito = docenteDAO.actualizar(docente);
                if (exito) {
                    mostrarAlerta("Éxito", "Docente actualizado correctamente", Alert.AlertType.INFORMATION);
                }
            }

            if (exito) {
                limpiarFormulario();
                cargarDatos();
            } else {
                mostrarAlerta("Error",
                        esNuevo ? "No se pudo registrar el docente" : "No se pudo actualizar el docente",
                        Alert.AlertType.ERROR);
            }
        }
    }

    // ✅ MÉTODO DE VALIDACIÓN DE CÓDIGO ÚNICO
    private boolean validarCodigoUnico(Docente docente) {
        String codigo = txtCodigo.getText().trim();

        // Verificar si el código ya existe (excepto para el docente actual que se está editando)
        Optional<Docente> docenteExistente = docenteDAO.obtenerPorCodigo(codigo);

        if (docenteExistente.isPresent()) {
            // Si es un nuevo docente O si está editando un docente diferente
            if (docenteSeleccionado == null ||
                    !docenteSeleccionado.getId().equals(docenteExistente.get().getId())) {

                mostrarAlerta("Error de Validación",
                        "El código de docente '" + codigo + "' ya existe.\n" +
                                "Por favor use un código único como: DOC003, DOC004, etc.",
                        Alert.AlertType.ERROR);
                return false;
            }
        }
        return true;
    }

    @FXML
    private void eliminarDocente() {
        if (docenteSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("¿Está seguro de eliminar al docente?");
            alert.setContentText("Esta acción marcará al docente como inactivo.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (docenteDAO.eliminar(docenteSeleccionado.getId())) {
                    mostrarAlerta("Éxito", "Docente eliminado correctamente", Alert.AlertType.INFORMATION);
                    limpiarFormulario();
                    cargarDatos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el docente", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Seleccione un docente para eliminar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void cancelar() {
        limpiarFormulario();
    }

    @FXML
    private void buscarDocentes() {
        String criterio = txtBuscar.getText().trim();
        if (criterio.isEmpty()) {
            cargarDatos();
        } else {
            docentesList.setAll(docenteDAO.buscarPorNombre(criterio));
        }
    }

    @FXML
    private void exportarDatos() {
        if (docentesList.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay datos para exportar", Alert.AlertType.WARNING);
            return;
        }
        ExportUtils.exportarTablaACSV(tableView, "Reporte_Docentes_UPeU");
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (txtCodigo.getText().trim().isEmpty()) {
            errores.append("• El código de docente es obligatorio\n");
        }

        if (txtDni.getText().trim().isEmpty()) {
            errores.append("• El DNI es obligatorio\n");
        }

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre es obligatorio\n");
        }

        if (txtApellido.getText().trim().isEmpty()) {
            errores.append("• El apellido es obligatorio\n");
        }

        if (txtEmail.getText().trim().isEmpty()) {
            errores.append("• El email es obligatorio\n");
        }

        if (cbxFacultad.getValue() == null) {
            errores.append("• La facultad es obligatoria\n");
        }

        if (dpFechaContratacion.getValue() == null) {
            errores.append("• La fecha de contratación es obligatoria\n");
        }

        // Validar formato de email
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errores.append("• El formato del email no es válido\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de Validación",
                    "Por favor corrija los siguientes errores:\n\n" + errores.toString(),
                    Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void cargarDatosFormulario(Docente docente) {
        txtCodigo.setText(docente.getCodigoDocente());
        txtDni.setText(docente.getDni());
        txtNombre.setText(docente.getNombre());
        txtApellido.setText(docente.getApellido());
        txtEmail.setText(docente.getEmail());
        txtTelefono.setText(docente.getTelefono());
        cbxFacultad.setValue(docente.getFacultad());
        txtEspecialidad.setText(docente.getEspecialidad());
        spnCargaHoraria.getValueFactory().setValue(docente.getCargaHoraria());
        dpFechaContratacion.setValue(docente.getFechaContratacion());

        btnGuardar.setText("💾 Actualizar Docente");
    }

    @FXML
    private void limpiarFormulario() {
        txtCodigo.clear();
        txtDni.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtEmail.clear();
        txtTelefono.clear();
        cbxFacultad.getSelectionModel().clearSelection();
        txtEspecialidad.clear();
        spnCargaHoraria.getValueFactory().setValue(40);
        dpFechaContratacion.setValue(null);
        txtBuscar.clear();

        limpiarSeleccion();
    }

    private void limpiarSeleccion() {
        tableView.getSelectionModel().clearSelection();
        docenteSeleccionado = null;
        btnGuardar.setText("💾 Guardar Docente");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void generarCodigoAutomatico() {
        if (txtNombre.getText().trim().isEmpty() || txtApellido.getText().trim().isEmpty()) {
            return;
        }

        String inicialNombre = txtNombre.getText().trim().substring(0, 1).toUpperCase();
        String inicialApellido = txtApellido.getText().trim().substring(0, 1).toUpperCase();

        // Buscar el próximo número disponible
        int siguienteNumero = 1;
        for (Docente doc : docentesList) {
            if (doc.getCodigoDocente().startsWith("DOC")) {
                try {
                    int num = Integer.parseInt(doc.getCodigoDocente().substring(3));
                    if (num >= siguienteNumero) {
                        siguienteNumero = num + 1;
                    }
                } catch (NumberFormatException e) {
                    // Ignorar códigos que no siguen el formato
                }
            }
        }

        String codigoGenerado = "DOC" + String.format("%03d", siguienteNumero);
        txtCodigo.setText(codigoGenerado);
    }
}
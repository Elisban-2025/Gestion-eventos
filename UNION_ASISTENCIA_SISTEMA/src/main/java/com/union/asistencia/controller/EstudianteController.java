package com.union.asistencia.controller;

import com.union.asistencia.dao.EstudianteDAO;
import com.union.asistencia.model.Estudiante;
import com.union.asistencia.model.Usuario;
import com.union.asistencia.util.ExportUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Optional;

@Log
public class EstudianteController extends BaseController {

    @FXML private TableView<Estudiante> tableView;
    @FXML private TableColumn<Estudiante, Integer> colId;
    @FXML private TableColumn<Estudiante, String> colCodigo;
    @FXML private TableColumn<Estudiante, String> colDni;
    @FXML private TableColumn<Estudiante, String> colNombre;
    @FXML private TableColumn<Estudiante, String> colApellido;
    @FXML private TableColumn<Estudiante, String> colEmail;
    @FXML private TableColumn<Estudiante, String> colCarrera;
    @FXML private TableColumn<Estudiante, Integer> colSemestre;
    @FXML private TableColumn<Estudiante, String> colGrupo;

    @FXML private TextField txtCodigo;
    @FXML private TextField txtDni;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private ComboBox<String> cbxCarrera;
    @FXML private Spinner<Integer> spnSemestre;
    @FXML private TextField txtGrupo;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtBuscar;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnEliminar;
    @FXML private Button btnExportar;
    @FXML private Button btnLimpiarDuplicados;

    private ObservableList<Estudiante> estudiantesList;
    private EstudianteDAO estudianteDAO;
    private Estudiante estudianteSeleccionado;
    private Usuario usuarioLogueado;

    @Override
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        inicializar();
    }

    @FXML
    private void initialize() {
        System.out.println("✅ Inicializando EstudianteController");
        try {
            estudianteDAO = new EstudianteDAO();
            estudiantesList = FXCollections.observableArrayList();
            estudianteSeleccionado = null;

            configurarTabla();
            configurarCombobox();
            configurarSpinner();
            configurarEventos();

            tableView.setItems(estudiantesList);

            System.out.println("✅ EstudianteController inicializado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error en initialize de EstudianteController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void inicializar() {
        System.out.println("✅ Inicializando datos de estudiantes");
        try {
            cargarDatos();
            limpiarFormulario();
            System.out.println("✅ Datos de estudiantes cargados correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error cargando datos de estudiantes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoEstudiante"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCarrera.setCellValueFactory(new PropertyValueFactory<>("carrera"));
        colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grupo"));
    }

    private void configurarCombobox() {
        cbxCarrera.getItems().addAll(
                "Ingeniería de Sistemas",
                "Ingeniería Civil",
                "Ingeniería Industrial",
                "Medicina",
                "Enfermería",
                "Derecho",
                "Administración",
                "Contabilidad",
                "Psicología",
                "Educación"
        );
    }

    private void configurarSpinner() {
        SpinnerValueFactory<Integer> semestreFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 1);
        spnSemestre.setValueFactory(semestreFactory);
    }

    private void configurarEventos() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        estudianteSeleccionado = newSelection;
                        cargarDatosFormulario(newSelection);
                    }
                });
    }

    private void cargarDatos() {
        try {
            List<Estudiante> estudiantes = estudianteDAO.obtenerTodos();
            System.out.println("📊 Estudiantes cargados desde BD: " + estudiantes.size());
            estudiantesList.setAll(estudiantes);

            if (estudiantes.isEmpty()) {
                System.out.println("ℹ️ No hay estudiantes en la base de datos");
            }
        } catch (Exception e) {
            System.err.println("❌ Error cargando estudiantes desde BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void guardarEstudiante() {
        if (validarFormulario()) {
            if (estudianteSeleccionado == null) {
                String codigo = txtCodigo.getText().trim();
                String dni = txtDni.getText().trim();

                boolean codigoExiste = estudianteDAO.existeEstudianteConCodigo(codigo);
                boolean dniExiste = estudianteDAO.existeEstudianteConDni(dni);

                if (codigoExiste || dniExiste) {
                    StringBuilder mensaje = new StringBuilder();
                    mensaje.append("❌ No se puede registrar el estudiante:\n\n");

                    if (codigoExiste) {
                        mensaje.append("• El código ").append(codigo).append(" ya está registrado\n");
                    }
                    if (dniExiste) {
                        mensaje.append("• El DNI ").append(dni).append(" ya está registrado\n");
                    }

                    mensaje.append("\nPor favor use valores únicos.");
                    mostrarAlerta("Error de Validación", mensaje.toString(), Alert.AlertType.ERROR);
                    return;
                }
            }

            Estudiante estudiante;
            boolean esNuevo = false;

            if (estudianteSeleccionado == null) {
                estudiante = new Estudiante();
                esNuevo = true;
            } else {
                estudiante = estudianteSeleccionado;
            }

            estudiante.setCodigoEstudiante(txtCodigo.getText().trim());
            estudiante.setDni(txtDni.getText().trim());
            estudiante.setNombre(txtNombre.getText().trim());
            estudiante.setApellido(txtApellido.getText().trim());
            estudiante.setEmail(txtEmail.getText().trim());
            estudiante.setTelefono(txtTelefono.getText().trim());
            estudiante.setCarrera(cbxCarrera.getValue());
            estudiante.setSemestre(spnSemestre.getValue());
            estudiante.setGrupo(txtGrupo.getText().trim());
            estudiante.setFechaNacimiento(dpFechaNacimiento.getValue());
            estudiante.setDireccion(txtDireccion.getText().trim());
            estudiante.setActivo(true);

            boolean exito;
            if (esNuevo) {
                exito = estudianteDAO.guardar(estudiante);
            } else {
                exito = estudianteDAO.actualizar(estudiante);
            }

            if (exito) {
                mostrarAlerta("Éxito",
                        esNuevo ? "Estudiante registrado correctamente" : "Estudiante actualizado correctamente",
                        Alert.AlertType.INFORMATION);
                limpiarFormulario();
                cargarDatos();
            } else {
                mostrarAlerta("Error",
                        esNuevo ? "No se pudo registrar el estudiante" : "No se pudo actualizar el estudiante",
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void eliminarEstudiante() {
        if (estudianteSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("¿Está seguro de eliminar el estudiante?");
            alert.setContentText("Esta acción marcará al estudiante como inactivo.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (estudianteDAO.eliminar(estudianteSeleccionado.getId())) {
                    mostrarAlerta("Éxito", "Estudiante eliminado correctamente", Alert.AlertType.INFORMATION);
                    limpiarFormulario();
                    cargarDatos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el estudiante", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Seleccione un estudiante para eliminar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void cancelar() {
        limpiarFormulario();
    }

    @FXML
    private void buscarEstudiantes() {
        String criterio = txtBuscar.getText().trim();
        if (criterio.isEmpty()) {
            cargarDatos();
        } else {
            List<Estudiante> estudiantes = estudianteDAO.buscarPorNombre(criterio);
            estudiantesList.setAll(estudiantes);
        }
    }

    @FXML
    private void exportarDatos() {
        if (estudiantesList.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay datos para exportar", Alert.AlertType.WARNING);
            return;
        }
        ExportUtils.exportarTablaACSV(tableView, "Reporte_Estudiantes_UPeU");
    }

    @FXML
    private void limpiarEstudianteDuplicado() {
        TextInputDialog dialog = new TextInputDialog("74988143");
        dialog.setTitle("Limpiar Estudiante Duplicado");
        dialog.setHeaderText("Ingrese el DNI del estudiante a eliminar");
        dialog.setContentText("DNI:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(dni -> {
            if (dni.trim().isEmpty()) {
                mostrarAlerta("Error", "Debe ingresar un DNI", Alert.AlertType.ERROR);
                return;
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Eliminación");
            confirmacion.setHeaderText("¿Está seguro de eliminar el estudiante con DNI: " + dni + "?");
            confirmacion.setContentText("Esta acción no se puede deshacer.");

            if (confirmacion.showAndWait().get() == ButtonType.OK) {
                if (estudianteDAO.eliminarEstudiantePorDni(dni)) {
                    mostrarAlerta("Éxito", "Estudiante con DNI " + dni + " eliminado correctamente", Alert.AlertType.INFORMATION);
                    cargarDatos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el estudiante con DNI " + dni, Alert.AlertType.ERROR);
                }
            }
        });
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (txtCodigo.getText().trim().isEmpty()) {
            errores.append("• El código del estudiante es obligatorio\n");
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

        if (cbxCarrera.getValue() == null) {
            errores.append("• La carrera es obligatoria\n");
        }

        if (dpFechaNacimiento.getValue() == null) {
            errores.append("• La fecha de nacimiento es obligatoria\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de Validación", "Por favor corrija los siguientes errores:\n\n" + errores.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void cargarDatosFormulario(Estudiante estudiante) {
        txtCodigo.setText(estudiante.getCodigoEstudiante());
        txtDni.setText(estudiante.getDni());
        txtNombre.setText(estudiante.getNombre());
        txtApellido.setText(estudiante.getApellido());
        txtEmail.setText(estudiante.getEmail());
        txtTelefono.setText(estudiante.getTelefono());
        cbxCarrera.setValue(estudiante.getCarrera());
        spnSemestre.getValueFactory().setValue(estudiante.getSemestre());
        txtGrupo.setText(estudiante.getGrupo());
        dpFechaNacimiento.setValue(estudiante.getFechaNacimiento());
        txtDireccion.setText(estudiante.getDireccion());

        btnGuardar.setText("💾 Actualizar Estudiante");
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtDni.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtEmail.clear();
        txtTelefono.clear();
        cbxCarrera.getSelectionModel().clearSelection();
        if (spnSemestre.getValueFactory() != null) {
            spnSemestre.getValueFactory().setValue(1);
        }
        txtGrupo.clear();
        dpFechaNacimiento.setValue(null);
        txtDireccion.clear();
        txtBuscar.clear();

        limpiarSeleccion();
    }

    private void limpiarSeleccion() {
        tableView.getSelectionModel().clearSelection();
        estudianteSeleccionado = null;
        btnGuardar.setText("💾 Guardar Estudiante");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
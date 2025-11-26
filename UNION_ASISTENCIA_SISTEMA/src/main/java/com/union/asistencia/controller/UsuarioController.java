package com.union.asistencia.controller;

import com.union.asistencia.dao.UsuarioDAO;
import com.union.asistencia.model.Usuario;
import com.union.asistencia.util.PasswordUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import lombok.extern.java.Log;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

@Log
public class UsuarioController extends BaseController {

    @FXML private TableView<Usuario> tableView;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colApellido;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colFechaCreacion;
    @FXML private TableColumn<Usuario, Boolean> colActivo;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cbxRol;
    @FXML private CheckBox chkActivo;
    @FXML private TextField txtBuscar;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnGenerarPassword;
    @FXML private Button btnCrearEventos;
    @FXML private Button btnEliminar;

    private ObservableList<Usuario> usuariosList;
    private UsuarioDAO usuarioDAO;
    private Usuario usuarioLogueado;
    private Usuario usuarioSeleccionado;

    @Override
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        inicializar();
    }

    @FXML
    private void initialize() {
        usuarioDAO = new UsuarioDAO();
        usuariosList = FXCollections.observableArrayList();
        usuarioSeleccionado = null;

        configurarTabla();
        configurarCombobox();
        configurarEventos();

        tableView.setItems(usuariosList);
    }

    private void inicializar() {
        cargarDatos();
        limpiarFormulario();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // ✅ SOLUCIÓN: Usar StringValueFactory para la fecha
        colFechaCreacion.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            if (usuario.getFechaCreacion() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(usuario.getFechaCreacion().format(formatter));
            } else {
                return new SimpleStringProperty("");
            }
        });

        // Formatear activo
        colActivo.setCellFactory(tc -> new TableCell<Usuario, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✅ Activo" : "❌ Inactivo");
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
        cbxRol.getItems().addAll("ADMIN", "DOCENTE", "COORDINADOR", "EVENTOS");
    }

    private void configurarEventos() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        usuarioSeleccionado = newSelection;
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
        try {
            // ✅ SOLUCIÓN: Cargar solo usuarios activos
            usuariosList.setAll(usuarioDAO.obtenerActivos());
            System.out.println("✅ Tabla actualizada con " + usuariosList.size() + " usuarios activos");
        } catch (Exception e) {
            System.err.println("❌ Error al cargar usuarios: " + e.getMessage());
            mostrarAlerta("Error", "No se pudieron cargar los usuarios: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void guardarUsuario() {
        if (validarFormulario()) {
            Usuario usuario;
            boolean esNuevo = false;

            if (usuarioSeleccionado == null) {
                usuario = new Usuario();
                esNuevo = true;
            } else {
                usuario = usuarioSeleccionado;
            }

            usuario.setUsername(txtUsername.getText().trim());

            // Solo actualizar password si se proporcionó uno nuevo
            if (!txtPassword.getText().isEmpty()) {
                usuario.setPasswordHash(PasswordUtils.hashPassword(txtPassword.getText()));
            } else if (esNuevo) {
                // Para usuario nuevo, password es obligatorio
                mostrarAlerta("Error", "La contraseña es obligatoria para nuevos usuarios", Alert.AlertType.ERROR);
                return;
            }

            usuario.setNombre(txtNombre.getText().trim());
            usuario.setApellido(txtApellido.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setRol(cbxRol.getValue());
            usuario.setActivo(chkActivo.isSelected());

            boolean exito;
            if (esNuevo) {
                exito = usuarioDAO.crearUsuario(usuario);
            } else {
                exito = usuarioDAO.actualizar(usuario);
            }

            if (exito) {
                mostrarAlerta("Éxito",
                        esNuevo ? "Usuario registrado correctamente" : "Usuario actualizado correctamente",
                        Alert.AlertType.INFORMATION);
                limpiarFormulario();
                cargarDatos(); // ✅ ACTUALIZAR TABLA DESPUÉS DE GUARDAR
            } else {
                mostrarAlerta("Error",
                        esNuevo ? "No se pudo registrar el usuario" : "No se pudo actualizar el usuario",
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void generarPassword() {
        String nuevaPassword = PasswordUtils.generateRandomPassword();
        txtPassword.setText(nuevaPassword);
        mostrarAlerta("Contraseña Generada",
                "Se ha generado una nueva contraseña: " + nuevaPassword +
                        "\n\nGuárdela en un lugar seguro.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void cancelar() {
        limpiarFormulario();
    }

    @FXML
    private void buscarUsuarios() {
        String criterio = txtBuscar.getText().trim();
        if (criterio.isEmpty()) {
            cargarDatos(); // ✅ Esto cargará solo usuarios activos
        } else {
            ObservableList<Usuario> usuariosFiltrados = FXCollections.observableArrayList();

            // ✅ CAMBIO: Buscar en TODOS los usuarios (activos e inactivos) para búsqueda
            for (Usuario usuario : usuarioDAO.obtenerTodos()) {
                if (usuario.getUsername().toLowerCase().contains(criterio.toLowerCase()) ||
                        usuario.getNombre().toLowerCase().contains(criterio.toLowerCase()) ||
                        usuario.getApellido().toLowerCase().contains(criterio.toLowerCase()) ||
                        usuario.getEmail().toLowerCase().contains(criterio.toLowerCase())) {
                    usuariosFiltrados.add(usuario);
                }
            }

            tableView.setItems(usuariosFiltrados);
        }
    }

    @FXML
    private void crearUsuarioParaEventos() {
        try {
            Dialog<Usuario> dialog = new Dialog<>();
            dialog.setTitle("Crear Usuario para Eventos");
            dialog.setHeaderText("Crear usuario con acceso SOLO al módulo de Eventos");

            ButtonType crearButtonType = new ButtonType("Crear", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(crearButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField username = new TextField();
            username.setPromptText("Usuario (ej: eventos.coord)");
            PasswordField password = new PasswordField();
            password.setPromptText("Contraseña");
            TextField nombre = new TextField();
            nombre.setPromptText("Nombre");
            TextField apellido = new TextField();
            apellido.setPromptText("Apellido");
            TextField email = new TextField();
            email.setPromptText("Email");

            grid.add(new Label("Usuario:"), 0, 0);
            grid.add(username, 1, 0);
            grid.add(new Label("Contraseña:"), 0, 1);
            grid.add(password, 1, 1);
            grid.add(new Label("Nombre:"), 0, 2);
            grid.add(nombre, 1, 2);
            grid.add(new Label("Apellido:"), 0, 3);
            grid.add(apellido, 1, 3);
            grid.add(new Label("Email:"), 0, 4);
            grid.add(email, 1, 4);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == crearButtonType) {
                    Usuario usuario = new Usuario();
                    usuario.setUsername(username.getText());
                    usuario.setPasswordHash(PasswordUtils.hashPassword(password.getText()));
                    usuario.setNombre(nombre.getText());
                    usuario.setApellido(apellido.getText());
                    usuario.setEmail(email.getText());
                    usuario.setRol("EVENTOS");
                    usuario.setActivo(true);
                    return usuario;
                }
                return null;
            });

            Optional<Usuario> result = dialog.showAndWait();
            result.ifPresent(usuario -> {
                if (usuarioDAO.crearUsuario(usuario)) {
                    mostrarAlerta("✅ Éxito",
                            "Usuario creado exitosamente\n\n" +
                                    "👤 Usuario: " + usuario.getUsername() + "\n" +
                                    "🎯 Rol: EVENTOS\n" +
                                    "📧 Email: " + usuario.getEmail() + "\n\n" +
                                    "🔒 Acceso: SOLO módulo de Eventos",
                            Alert.AlertType.INFORMATION);
                    cargarDatos(); // ✅ ACTUALIZAR TABLA DESPUÉS DE CREAR
                } else {
                    mostrarAlerta("❌ Error", "No se pudo crear el usuario", Alert.AlertType.ERROR);
                }
            });

        } catch (Exception e) {
            mostrarAlerta("❌ Error", "Error al crear usuario: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (txtUsername.getText().trim().isEmpty()) {
            errores.append("• El nombre de usuario es obligatorio\n");
        }

        if (usuarioSeleccionado == null && txtPassword.getText().isEmpty()) {
            errores.append("• La contraseña es obligatoria para nuevos usuarios\n");
        }

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre es obligatorio\n");
        }

        if (txtApellido.getText().trim().isEmpty()) {
            errores.append("• El apellido es obligatorio\n");
        }

        if (txtEmail.getText().trim().isEmpty()) {
            errores.append("• El email es obligatorio\n");
        } else if (!txtEmail.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errores.append("• El formato del email no es válido\n");
        }

        if (cbxRol.getValue() == null) {
            errores.append("• El rol es obligatorio\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de Validación", "Por favor corrija los siguientes errores:\n\n" + errores.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void cargarDatosFormulario(Usuario usuario) {
        txtUsername.setText(usuario.getUsername());
        txtPassword.clear();
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtEmail.setText(usuario.getEmail());
        cbxRol.setValue(usuario.getRol());
        chkActivo.setSelected(usuario.getActivo());

        btnGuardar.setText("💾 Actualizar Usuario");
    }

    private void limpiarFormulario() {
        txtUsername.clear();
        txtPassword.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtEmail.clear();
        cbxRol.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        txtBuscar.clear();

        limpiarSeleccion();
    }

    private void limpiarSeleccion() {
        tableView.getSelectionModel().clearSelection();
        usuarioSeleccionado = null;
        btnGuardar.setText("💾 Guardar Usuario");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void eliminarUsuario() {
        if (usuarioSeleccionado != null) {
            // No permitir eliminar al usuario actualmente logueado
            if (usuarioSeleccionado.getUsername().equals(usuarioLogueado.getUsername())) {
                mostrarAlerta("Error", "No puede eliminar su propio usuario", Alert.AlertType.ERROR);
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("¿Está seguro de eliminar al usuario?");
            alert.setContentText("Usuario: " + usuarioSeleccionado.getUsername() +
                    "\nNombre: " + usuarioSeleccionado.getNombreCompleto() +
                    "\n\nEsta acción marcará al usuario como inactivo.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (usuarioDAO.eliminar(usuarioSeleccionado.getId())) {
                    mostrarAlerta("Éxito", "Usuario eliminado correctamente", Alert.AlertType.INFORMATION);
                    limpiarFormulario();
                    cargarDatos(); // ✅ ACTUALIZAR TABLA DESPUÉS DE ELIMINAR
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el usuario", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Seleccione un usuario para eliminar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void recargarTabla() {
        cargarDatos();
    }
}
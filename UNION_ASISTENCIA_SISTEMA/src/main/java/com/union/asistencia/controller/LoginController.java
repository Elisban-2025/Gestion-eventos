package com.union.asistencia.controller;

import com.union.asistencia.dao.UsuarioDAO;
import com.union.asistencia.model.Usuario;
import com.union.asistencia.util.PasswordUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log
public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private Button btnLogin;
    @FXML private Button btnTogglePassword;
    @FXML private Label lblError;
    @FXML private Label lblIntentos;
    @FXML private Hyperlink linkRecuperacion;
    @FXML private ProgressIndicator progressIntentos;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Map<String, Integer> intentosFallidos = new HashMap<>();
    private Map<String, Long> bloqueosTemporales = new HashMap<>();
    private final int MAX_INTENTOS = 5;
    private final long TIEMPO_BLOQUEO = 2 * 60 * 1000; // ✅ CORREGIDO: 2 minutos de tiepoo de esperaaaaaaa

    @FXML
    private void initialize() {
        lblError.setVisible(false);
        lblIntentos.setVisible(false);
        progressIntentos.setVisible(false);

        if (txtPasswordVisible != null) {
            txtPasswordVisible.setVisible(false);
        }

        configurarEventos();
        configurarProgressIntentos();
        verificarVersion();
        configurarFocusAutomatico();

        // ========== FORZAR TAMAÑO DE VENTANA 270x300 ==========
        Platform.runLater(() -> {
            // Obtener la ventana de cualquier nodo
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setMinWidth(270);
            stage.setMaxWidth(270);
            stage.setMinHeight(300);
            stage.setMaxHeight(300);
            stage.setWidth(270);
            stage.setHeight(300);
            stage.setResizable(false);
            stage.centerOnScreen(); // Centrar en pantalla
            System.out.println("✅ Ventana de login configurada a 270x300");
        });
        // ========== FIN ALTERNATIVA ==========
    }

    private void configurarEventos() {
        txtPassword.setOnAction(e -> login());
        // txtPasswordVisible.setOnAction(e -> login());

        if (txtPasswordVisible != null) {
            txtPasswordVisible.setOnAction(e -> login());
        }

        btnLogin.setOnAction(e -> login());

        txtUsername.textProperty().addListener((obs, oldVal, newVal) -> {
            lblError.setVisible(false);
        });

        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            lblError.setVisible(false);
        });
    }

    private void configurarProgressIntentos() {
        progressIntentos.setProgress(0);
    }

    private void configurarFocusAutomatico() {
        Platform.runLater(() -> txtUsername.requestFocus());
    }

    private void verificarVersion() {
        log.info("Sistema de Gestión de Asistencia - Versión 1.0.0");
        log.info("Desarrollado por: Elisban Huaylla");
    }

    @FXML
    private void login() {
        if (!validarConexionBD()) {
            return;
        }

        String username = txtUsername.getText().trim();
        String password = obtenerPassword();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor ingrese usuario y contraseña");
            return;
        }

        if (estaBloqueado(username)) {
            long tiempoRestante = getTiempoRestanteBloqueo(username);
            mostrarError("Usuario bloqueado. Tiempo restante: " + formatTiempo(tiempoRestante));
            return;
        }

        Optional<Usuario> usuarioOpt = usuarioDAO.autenticar(username, password);

        if (usuarioOpt.isPresent()) {
            intentosFallidos.remove(username);
            actualizarUIIntentos(0);
            limpiarCampos();
            abrirVentanaPrincipal(usuarioOpt.get());
        } else {
            manejarIntentoFallido(username);
        }
    }

    private boolean validarConexionBD() {
        try {
            if (!usuarioDAO.probarConexion()) {
                mostrarError("Error de conexión a la base de datos. Contacte al administrador.");
                return false;
            }
            return true;
        } catch (Exception e) {
            mostrarError("Error al conectar con la base de datos: " + e.getMessage());
            return false;
        }
    }

    private String obtenerPassword() {
        return txtPassword.isVisible() ? txtPassword.getText() : txtPasswordVisible.getText();
    }

    private void manejarIntentoFallido(String username) {
        int intentos = intentosFallidos.getOrDefault(username, 0) + 1;
        intentosFallidos.put(username, intentos);

        int intentosRestantes = MAX_INTENTOS - intentos;
        actualizarUIIntentos(intentos);

        if (intentos >= MAX_INTENTOS) {
            bloquearUsuario(username);
            mostrarError("Usuario bloqueado por " + MAX_INTENTOS + " intentos fallidos. Espere 2 minutos."); // ✅ CORREGIDO: 2 minutos
        } else {
            mostrarError("Usuario o contraseña incorrectos. Intentos restantes: " + intentosRestantes);
        }
    }

    private boolean estaBloqueado(String username) {
        Long tiempoBloqueo = bloqueosTemporales.get(username);
        if (tiempoBloqueo != null) {
            if (System.currentTimeMillis() - tiempoBloqueo < TIEMPO_BLOQUEO) {
                return true;
            } else {
                bloqueosTemporales.remove(username);
                intentosFallidos.remove(username);
            }
        }
        return false;
    }

    private long getTiempoRestanteBloqueo(String username) {
        Long tiempoBloqueo = bloqueosTemporales.get(username);
        if (tiempoBloqueo != null) {
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoBloqueo;
            return TIEMPO_BLOQUEO - tiempoTranscurrido;
        }
        return 0;
    }

    private String formatTiempo(long milisegundos) {
        long minutos = milisegundos / (60 * 1000);
        long segundos = (milisegundos % (60 * 1000)) / 1000;
        return String.format("%02d:%02d", minutos, segundos);
    }

    private void bloquearUsuario(String username) {
        bloqueosTemporales.put(username, System.currentTimeMillis());

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            bloqueosTemporales.remove(username);
            intentosFallidos.remove(username);
            log.info("Usuario " + username + " desbloqueado automáticamente después de 2 minutos");
        }, 2, TimeUnit.MINUTES); // ✅ CORREGIDO: 2 minutos

        scheduler.shutdown();
    }

    private void actualizarUIIntentos(int intentos) {
        if (intentos > 0) {
            double progreso = (double) intentos / MAX_INTENTOS;
            progressIntentos.setProgress(progreso);
            progressIntentos.setVisible(true);

            lblIntentos.setText("Intentos: " + intentos + "/" + MAX_INTENTOS);
            lblIntentos.setVisible(true);

            if (intentos >= 4) {
                progressIntentos.setStyle("-fx-progress-color: red;");
                lblIntentos.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else if (intentos >= 2) {
                progressIntentos.setStyle("-fx-progress-color: orange;");
                lblIntentos.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            } else {
                progressIntentos.setStyle("-fx-progress-color: #f39c12;");
                lblIntentos.setStyle("-fx-text-fill: #f39c12;");
            }
        } else {
            progressIntentos.setVisible(false);
            lblIntentos.setVisible(false);
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        // ✅ AGREGAR VERIFICACIONES AL INICIO:
        if (txtPassword == null || txtPasswordVisible == null) {
            return;
        }

        if (txtPassword.isVisible()) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPassword.setVisible(false);
            btnTogglePassword.setText("🙈");
            btnTogglePassword.setStyle("-fx-background-color: #e74c3c;");
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPasswordVisible.setVisible(false);
            btnTogglePassword.setText("👁️");
            btnTogglePassword.setStyle("-fx-background-color: #3498db;");
        }
    }

    private void limpiarCampos() {
        try {
            System.out.println("🔧 Limpiando campos...");

            // Limpiar campos de usuario y contraseña
            if (txtUsername != null) {
                txtUsername.clear();
                System.out.println("✅ txtUsername limpiado");
            }

            if (txtPassword != null) {
                txtPassword.clear();
                System.out.println("✅ txtPassword limpiado");
            }

            // Limpiar campo de password visible (si existe)
            if (txtPasswordVisible != null) {
                txtPasswordVisible.clear();
                System.out.println("✅ txtPasswordVisible limpiado");
            } else {
                System.out.println("ℹ️ txtPasswordVisible es null (puede ser normal)");
            }

            // Ocultar elementos de UI
            if (lblError != null) {
                lblError.setVisible(false);
            }
            if (progressIntentos != null) {
                progressIntentos.setVisible(false);
            }
            if (lblIntentos != null) {
                lblIntentos.setVisible(false);
            }

            System.out.println("🔧 Campos limpiados exitosamente");

        } catch (Exception e) {
            System.out.println("⚠️ Error al limpiar campos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirVentanaPrincipal(Usuario usuario) {
        try {
            System.out.println("Abriendo ventana principal para usuario: " + usuario.getUsername());

            // Cargar el FXML de la ventana principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/union/asistencia/view/main.fxml"));            Parent root = loader.load();

            // Obtener el controlador y configurar el usuario
            MainController mainController = loader.getController();

            // Verificar que el controlador no sea null
            if (mainController != null) {
                System.out.println("Controlador principal obtenido");
                mainController.setUsuarioLogueado(usuario);
            } else {
                System.out.println("Controlador principal es null");
            }

            // Crear nueva escena
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Sistema de Gestión de Asistencia - " + usuario.getNombre() + " " + usuario.getApellido());
            stage.setMaximized(true);

            // Cerrar ventana de login de forma segura
            if (txtUsername != null && txtUsername.getScene() != null) {
                Stage loginStage = (Stage) txtUsername.getScene().getWindow();
                loginStage.close();
                System.out.println("Ventana de login cerrada");
            }

            // Mostrar ventana principal
            stage.show();

            System.out.println("Ventana principal abierta exitosamente");

        } catch (Exception e) {
            // Manejar cualquier error de forma simple
            System.out.println("Error al cargar la aplicacion: " + e.toString());
            mostrarError("Error al cargar la aplicacion principal");
        }
    }

    private void configurarTeclaEscSalir(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                salirAplicacion();
            }
        });
    }

    private void salirAplicacion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Salir del Sistema");
        alert.setHeaderText("¿Está seguro que desea salir?");
        alert.setContentText("Se cerrará el Sistema de Gestión de Asistencia.\n\n" +
                "Desarrollado por: Elisban Huaylla\n" +
                "Contacto: elisbanhauylla@gmail.com\n" +
                "Teléfono: 949329822");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
            System.exit(0);
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    @FXML
    private void recuperarPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recuperación de Contraseña");
        alert.setHeaderText("Contacte al Administrador");
        alert.setContentText("Para recuperar su contraseña, contacte al administrador del sistema.\n\n" +
                "Desarrollador: Elisban Huaylla\n" +
                "Email: elisbanhauylla@gmail.com\n" +
                "Teléfono: 949329822\n\n" +
                "Universidad Peruana Unión - Sistema de Gestión de Asistencia");
        alert.showAndWait();
    }
}
package com.union.asistencia.controller;

import com.union.asistencia.model.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

@Log
public class MainController extends BaseController {

    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private VBox contentPane;
    @FXML private ComboBox<String> cbxTemas;
    @FXML private MenuButton btnConfiguracion;
    @FXML private ImageView imgLogoHeader;

    private Preferences prefs;
    private Timeline carruselTimeline;
    private StackPane watermarkContainer;
    private Usuario usuarioLogueado;

    @FXML
    private void initialize() {
        prefs = Preferences.userNodeForPackage(MainController.class);
        initializeWatermarkSystem();
    }

    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        if (usuario != null) {
            System.out.println("🔐 Usuario logueado: " + usuario.getNombreCompleto() + " - Rol: " + usuario.getRol());

            if (lblUsuario != null) {
                lblUsuario.setText("Usuario: " + usuario.getNombre() + " " + usuario.getApellido());
            }
            if (lblRol != null) {
                lblRol.setText("Rol: " + usuario.getRol());
            }

            if (cbxTemas != null) {
                configurarSelectorTemas();
            }

            if (imgLogoHeader != null) {
                inicializarCarruselLogos(imgLogoHeader);
            }

            configurarInterfazPorRol();
        }
    }

    // CORREGIDO: Usar Platform.runLater para asegurar que la escena esté lista
    private void configurarInterfazPorRol() {
        if (usuarioLogueado == null) return;

        String rol = usuarioLogueado.getRol();
        System.out.println("🎮 Configurando interfaz para rol: " + rol);

        // Usar Platform.runLater para asegurar que la escena esté disponible
        javafx.application.Platform.runLater(() -> {
            if (rol != null && rol.equalsIgnoreCase("DOCENTE")) {
                ocultarModulosNoDocentes();
            } else if (rol != null && rol.equalsIgnoreCase("EVENTOS")) {
                ocultarTodoExceptoEventos();
            }
        });
    }

    // CORREGIDO: Verificar que getScene() no sea null
    private void ocultarTodoExceptoEventos() {
        try {
            // Verificar que la escena esté disponible
            if (this.lblUsuario == null || this.lblUsuario.getScene() == null) {
                System.out.println("⚠️ Escena no disponible, reintentando...");
                // Reintentar después de un delay
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
                    ocultarTodoExceptoEventos();
                }));
                timeline.play();
                return;
            }

            String[] botonesAOcultar = {
                    "btnUsuarios", "btnDocentes", "btnConfiguracion",
                    "btnEstudiantes", "btnAsignaturas", "btnAsistencias",
                    "btnHorarios", "btnAulas", "btnReportes", "btnDashboard"
            };

            for (String botonId : botonesAOcultar) {
                javafx.scene.Node node = this.lblUsuario.getScene().lookup("#" + botonId);
                if (node != null) {
                    node.setVisible(false);
                    System.out.println("🔒 Ocultado: " + botonId);
                }
            }

            System.out.println("✅ Solo módulo Eventos disponible para rol EVENTOS");

        } catch (Exception e) {
            System.err.println("⚠️ Error al ocultar módulos: " + e.getMessage());
        }
    }

    // CORREGIDO: Verificar que getScene() no sea null
    private void ocultarModulosNoDocentes() {
        try {
            // Verificar que la escena esté disponible
            if (this.lblUsuario == null || this.lblUsuario.getScene() == null) {
                System.out.println("⚠️ Escena no disponible, reintentando...");
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
                    ocultarModulosNoDocentes();
                }));
                timeline.play();
                return;
            }

            String[] botonesAOcultar = {
                    "btnUsuarios", "btnDocentes", "btnConfiguracion"
            };

            for (String botonId : botonesAOcultar) {
                javafx.scene.Node node = this.lblUsuario.getScene().lookup("#" + botonId);
                if (node != null) {
                    node.setVisible(false);
                    System.out.println("🔒 Ocultado: " + botonId);
                }
            }

            System.out.println("✅ Módulos restringidos ocultados para docente");

        } catch (Exception e) {
            System.err.println("⚠️ Error al ocultar módulos: " + e.getMessage());
        }
    }

    private void initializeWatermarkSystem() {
        try {
            watermarkContainer = new StackPane();
            watermarkContainer.getStyleClass().add("watermark-container");
            System.out.println("✅ Sistema de watermarks inicializado");
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar sistema de watermarks: " + e.getMessage());
        }
    }

    private void configurarSelectorTemas() {
        try {
            cbxTemas.getItems().clear();
            cbxTemas.getItems().addAll("Tema Claro", "Tema Oscuro", "Tema Institucional");

            String temaGuardado = prefs.get("tema_preferido", "Tema Institucional");
            cbxTemas.setValue(temaGuardado);

            cbxTemas.setOnAction(e -> cambiarTema());

            System.out.println("✅ Selector de temas configurado: " + temaGuardado);
        } catch (Exception e) {
            System.err.println("❌ Error al configurar selector de temas: " + e.getMessage());
        }
    }

    private void cambiarTema() {
        try {
            String temaSeleccionado = cbxTemas.getValue();
            if (temaSeleccionado != null) {
                prefs.put("tema_preferido", temaSeleccionado);
                aplicarTema(temaSeleccionado);
                System.out.println("🎨 Tema cambiado a: " + temaSeleccionado);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al cambiar tema: " + e.getMessage());
        }
    }

    private void aplicarTema(String tema) {
        try {
            Scene scene = lblUsuario.getScene();
            if (scene != null) {
                scene.getStylesheets().clear();
                scene.getRoot().getStyleClass().removeAll("light-theme", "dark-theme", "corporate-theme");

                switch (tema) {
                    case "Tema Claro":
                        scene.getRoot().getStyleClass().add("light-theme");
                        scene.getStylesheets().add(getClass().getResource("/com/union/asistencia/css/themes/light-theme.css").toExternalForm());
                        break;
                    case "Tema Oscuro":
                        scene.getRoot().getStyleClass().add("dark-theme");
                        scene.getStylesheets().add(getClass().getResource("/com/union/asistencia/css/themes/dark-theme.css").toExternalForm());
                        break;
                    case "Tema Institucional":
                        scene.getRoot().getStyleClass().add("corporate-theme");
                        scene.getStylesheets().add(getClass().getResource("/com/union/asistencia/css/themes/corporate-theme.css").toExternalForm());
                        break;
                }

                scene.getStylesheets().add(getClass().getResource("/com/union/asistencia/css/styles.css").toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("❌ Error al aplicar tema: " + e.getMessage());
        }
    }

    private void inicializarCarruselLogos(ImageView imageView) {
        try {
            List<String> logos = ConfiguracionController.getLogosCarrusel();
            if (logos.isEmpty()) {
                String mainLogo = prefs.get("ruta_logo", "");
                if (!mainLogo.isEmpty() && new File(mainLogo).exists()) {
                    Image image = new Image(new File(mainLogo).toURI().toString());
                    imageView.setImage(image);
                    System.out.println("✅ Logo principal cargado");
                } else {
                    System.out.println("ℹ️ No hay logos configurados");
                }
                return;
            }

            imageView.setFitWidth(120);
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            final int[] currentIndex = {0};

            carruselTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(5), e -> {
                        if (!logos.isEmpty()) {
                            String logoPath = logos.get(currentIndex[0]);
                            try {
                                Image image = new Image(new File(logoPath).toURI().toString());
                                imageView.setImage(image);
                                System.out.println("🔄 Logo carrusel cambiado: " + (currentIndex[0] + 1) + "/" + logos.size());
                            } catch (Exception ex) {
                                System.err.println("❌ Error al cargar logo del carrusel: " + logoPath);
                            }

                            currentIndex[0] = (currentIndex[0] + 1) % logos.size();
                        }
                    })
            );

            carruselTimeline.setCycleCount(Timeline.INDEFINITE);
            carruselTimeline.play();

            String firstLogo = logos.get(0);
            Image image = new Image(new File(firstLogo).toURI().toString());
            imageView.setImage(image);

            System.out.println("🎠 Carrusel de logos iniciado: " + logos.size() + " logos, rotación cada 5 segundos");

        } catch (Exception e) {
            System.err.println("❌ Error al inicializar carrusel de logos: " + e.getMessage());
        }
    }

    // MÉTODOS PARA CARGAR MÓDULOS
    @FXML private void cargarEstudiantes() { cargarVentana("estudiante"); }
    @FXML private void cargarDocentes() { cargarVentana("docente"); }
    @FXML private void cargarAsignaturas() { cargarVentana("asignatura"); }
    @FXML private void cargarAsistencias() { cargarVentana("asistencia"); }
    @FXML private void cargarHorarios() { cargarVentana("horario"); }
    @FXML private void cargarAulas() { cargarVentana("aula"); }
    @FXML private void cargarReportes() { cargarVentana("reporte"); }
    @FXML private void cargarUsuarios() { cargarVentana("usuario"); }
    @FXML private void cargarEventos() { cargarVentana("evento"); }
    @FXML private void cargarConfiguracion() { cargarVentana("configuracion"); }

    private void cargarVentana(String fxmlName) {
        try {
            System.out.println("🔄 Cargando ventana: " + fxmlName);

            if (!tienePermisoParaVista(fxmlName)) {
                mostrarAlerta("Acceso Denegado",
                        "No tiene permisos para acceder al módulo: " + fxmlName,
                        Alert.AlertType.WARNING);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/union/asistencia/view/" + fxmlName + ".fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setUsuarioLogueado(usuarioLogueado);
            }

            contentPane.getChildren().setAll(root);

            System.out.println("✅ Ventana " + fxmlName + " cargada exitosamente");

        } catch (IOException e) {
            System.out.println("❌ Error al cargar la ventana: " + fxmlName + " - " + e.getMessage());
            mostrarError("Error al cargar la ventana: " + fxmlName);
        }
    }

    private boolean tienePermisoParaVista(String fxmlName) {
        if (usuarioLogueado == null) return false;

        String rol = usuarioLogueado.getRol();
        if (rol == null) return false;

        if (rol.equalsIgnoreCase("ADMIN")) {
            return true;
        }

        if (rol.equalsIgnoreCase("DOCENTE")) {
            return esVistaPermitidaParaDocentes(fxmlName);
        }

        if (rol.equalsIgnoreCase("EVENTOS")) {
            return fxmlName.equals("evento");
        }

        return false;
    }

    private boolean esVistaPermitidaParaDocentes(String fxmlName) {
        String[] vistasPermitidas = {
                "asistencia",
                "estudiante",
                "evento",
                "horario",
                "asignatura"
        };

        for (String vista : vistasPermitidas) {
            if (fxmlName.equals(vista)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void cerrarSesion() {
        try {
            if (carruselTimeline != null) {
                carruselTimeline.stop();
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cerrar Sesión");
            alert.setHeaderText("¿Está seguro que desea cerrar sesión?");
            alert.setContentText("Será redirigido a la pantalla de login.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                Stage stage = (Stage) lblUsuario.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/com/union/asistencia/view/login.fxml"));
                Scene scene = new Scene(root, 1000, 700);

                scene.getStylesheets().add(getClass().getResource("/com/union/asistencia/css/styles.css").toExternalForm());

                stage.setScene(scene);
                stage.setTitle("Universidad Peruana Unión - Login");
                stage.setMaximized(false);
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            log.severe("Error al cerrar sesión: " + e.getMessage());
            mostrarError("Error al cerrar sesión: " + e.getMessage());
        }
    }

    @FXML
    private void abrirConfiguracion() {
        mostrarInfo("Configuración del sistema - Próximamente");
    }

    @FXML
    private void abrirAcercaDe() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Universidad Peruana Unión - Sistema de Gestión de Asistencia");
        alert.setContentText("Versión: 1.0.0\n\n" +
                "Desarrollado para:\n" +
                "Universidad Peruana Unión\n\n" +
                "Características:\n" +
                "• Gestión completa de estudiantes y docentes\n" +
                "• Control de asistencia con múltiples métodos\n" +
                "• Gestión de horarios y aulas\n" +
                "• Reportes y estadísticas avanzadas\n" +
                "• Gestión de eventos académicos\n" +
                "• Sistema de seguridad avanzado\n\n" +
                "© 2024 - Universidad Peruana Unión");
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void cargarDashboard() {
        System.out.println("📊 Intentando cargar dashboard...");
        mostrarInfo("Dashboard - Próximamente");
    }
}
package com.union.asistencia.controller;

import com.union.asistencia.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

@Log
public class ConfiguracionController extends BaseController {

    @FXML private ComboBox<String> cbxTema;
    @FXML private TextField txtNombreUniversidad;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtRector;
    @FXML private Button btnSeleccionarLogo;
    @FXML private Label lblRutaLogo;
    @FXML private Label lblInfoLogos;
    @FXML private Button btnAgregarLogos;
    @FXML private CheckBox chkNotificaciones;
    @FXML private CheckBox chkBackupAutomatico;
    @FXML private Spinner<Integer> spnIntervaloBackup;
    @FXML private Button btnGuardarConfig;

    // Controles de Watermark
    @FXML private CheckBox chkWatermarkActivo;
    @FXML private Label lblEstadoWatermark;
    @FXML private Slider sldOpacidadWatermark;
    @FXML private ComboBox<String> cbxPosicionWatermark;
    @FXML private TextField txtWatermarkPersonalizado;
    @FXML private Button btnAgregarWatermark;
    @FXML private Button btnLimpiarWatermarks;

    private Preferences prefs;
    private static List<String> logosCarrusel = new ArrayList<>();
    private VBox carruselContainer;

    @FXML
    private void initialize() {
        prefs = Preferences.userNodeForPackage(ConfiguracionController.class);

        // Configurar controles
        configurarControles();
        cargarConfiguracionExistente();

        System.out.println("✅ ConfiguraciónController inicializado");
    }

    /**
     * Configura todos los controles de la interfaz
     */
    private void configurarControles() {
        try {
            // Configurar combo box de temas
            cbxTema.getItems().addAll("Tema Claro", "Tema Oscuro", "Tema Institucional");

            // Configurar spinner de backup
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24, 6);
            spnIntervaloBackup.setValueFactory(valueFactory);

            // Configurar controles de watermark
            configurarControlesWatermark();

            System.out.println("✅ Controles configurados correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error al configurar controles: " + e.getMessage());
        }
    }

    /**
     * Configura los controles específicos del watermark
     */
    private void configurarControlesWatermark() {
        try {
            // Configurar checkbox de activación
            chkWatermarkActivo.setSelected(true);
            chkWatermarkActivo.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    lblEstadoWatermark.setText("Watermark: ACTIVO");
                    lblEstadoWatermark.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else {
                    lblEstadoWatermark.setText("Watermark: INACTIVO");
                    lblEstadoWatermark.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    watermarkService.stopWatermarkSystem();
                }
            });

            // Configurar slider de opacidad
            sldOpacidadWatermark.setMin(0.05);
            sldOpacidadWatermark.setMax(0.2);
            sldOpacidadWatermark.setValue(0.08);
            sldOpacidadWatermark.setShowTickLabels(true);
            sldOpacidadWatermark.setShowTickMarks(true);

            // Configurar combobox de posición
            cbxPosicionWatermark.getItems().addAll("Centro", "Esquina Inferior Derecha", "Esquina Superior Izquierda", "Diagonal");
            cbxPosicionWatermark.setValue("Centro");

            // Estado inicial
            lblEstadoWatermark.setText("Watermark: ACTIVO");
            lblEstadoWatermark.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

            System.out.println("✅ Controles de watermark configurados");

        } catch (Exception e) {
            System.err.println("❌ Error al configurar controles de watermark: " + e.getMessage());
        }
    }

    /**
     * Carga la configuración existente desde preferencias
     */
    private void cargarConfiguracionExistente() {
        try {
            // Cargar tema
            String tema = prefs.get("tema_preferido", "Tema Institucional");
            cbxTema.setValue(tema);

            // Cargar información de la universidad
            txtNombreUniversidad.setText(prefs.get("nombre_universidad", "Universidad Peruana Unión"));
            txtDireccion.setText(prefs.get("direccion_universidad", "Carretera Juliaca - Puno S/N"));
            txtTelefono.setText(prefs.get("telefono_universidad", "+51 123 456 789"));
            txtEmail.setText(prefs.get("email_universidad", "contacto@upeu.edu.pe"));
            txtRector.setText(prefs.get("rector_universidad", "Dr. Director General"));

            // Cargar ruta del logo
            String rutaLogo = prefs.get("ruta_logo", "");
            if (!rutaLogo.isEmpty()) {
                lblRutaLogo.setText(new File(rutaLogo).getName());
            }

            // Cargar configuración del sistema
            chkNotificaciones.setSelected(prefs.getBoolean("notificaciones_activas", true));
            chkBackupAutomatico.setSelected(prefs.getBoolean("backup_automatico", true));
            spnIntervaloBackup.getValueFactory().setValue(prefs.getInt("intervalo_backup", 6));

            // Cargar carrusel de logos
            cargarCarruselDesdePreferencias();

            System.out.println("✅ Configuración existente cargada");

        } catch (Exception e) {
            System.err.println("❌ Error al cargar configuración existente: " + e.getMessage());
        }
    }

    /**
     * Método para seleccionar logo principal - CORREGIDO
     */
    @FXML
    private void seleccionarLogo() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Logo Principal");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                    new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
            );

            File selectedFile = fileChooser.showOpenDialog(btnSeleccionarLogo.getScene().getWindow());
            if (selectedFile != null) {
                // Guardar la ruta del logo
                prefs.put("ruta_logo", selectedFile.getAbsolutePath());
                lblRutaLogo.setText(selectedFile.getName());

                mostrarAlerta("✅ Logo seleccionado", "El logo principal ha sido configurado correctamente.", Alert.AlertType.INFORMATION);
                System.out.println("✅ Logo principal seleccionado: " + selectedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("❌ Error al seleccionar logo: " + e.getMessage());
            mostrarAlerta("❌ Error", "No se pudo seleccionar el logo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Método para agregar múltiples logos al carrusel - CORREGIDO
     */
    @FXML
    private void agregarLogosCarrusel() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Logos para Carrusel");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(btnAgregarLogos.getScene().getWindow());
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                // Crear directorio para logos si no existe
                File logosDir = new File("data/logos");
                if (!logosDir.exists()) {
                    logosDir.mkdirs();
                }

                int logosAgregados = 0;
                for (File file : selectedFiles) {
                    try {
                        // Copiar archivo al directorio de logos
                        File destino = new File(logosDir, file.getName());
                        Files.copy(file.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);

                        // Agregar a la lista
                        if (!logosCarrusel.contains(destino.getAbsolutePath())) {
                            logosCarrusel.add(destino.getAbsolutePath());
                            logosAgregados++;
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error al copiar logo: " + file.getName() + " - " + e.getMessage());
                    }
                }

                // Guardar carrusel
                guardarCarruselEnPreferencias();
                actualizarInfoLogos();

                mostrarAlerta("✅ Logos agregados",
                        "Se agregaron " + logosAgregados + " logos al carrusel automático.",
                        Alert.AlertType.INFORMATION);

                System.out.println("✅ " + logosAgregados + " logos agregados al carrusel");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al agregar logos al carrusel: " + e.getMessage());
            mostrarAlerta("❌ Error", "No se pudieron agregar los logos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Método para restaurar valores por defecto - CORREGIDO
     */
    @FXML
    private void restaurarValoresPorDefecto() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Restaurar Valores por Defecto");
            alert.setHeaderText("¿Está seguro que desea restaurar los valores por defecto?");
            alert.setContentText("Esta acción no se puede deshacer.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                // Restaurar valores por defecto
                prefs.remove("nombre_universidad");
                prefs.remove("direccion_universidad");
                prefs.remove("telefono_universidad");
                prefs.remove("email_universidad");
                prefs.remove("rector_universidad");
                prefs.remove("ruta_logo");
                prefs.putBoolean("notificaciones_activas", true);
                prefs.putBoolean("backup_automatico", true);
                prefs.putInt("intervalo_backup", 6);

                // Limpiar carrusel
                logosCarrusel.clear();
                prefs.remove("carrusel_logos");

                // Recargar configuración
                cargarConfiguracionExistente();

                // Restaurar watermarks
                watermarkService.clearWatermarkTexts();

                mostrarAlerta("✅ Valores restaurados",
                        "Todos los valores han sido restaurados a su configuración por defecto.",
                        Alert.AlertType.INFORMATION);

                System.out.println("✅ Valores por defecto restaurados");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al restaurar valores por defecto: " + e.getMessage());
            mostrarAlerta("❌ Error", "No se pudieron restaurar los valores: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Carga el carrusel desde las preferencias
     */
    private void cargarCarruselDesdePreferencias() {
        try {
            logosCarrusel.clear();

            String carruselGuardado = prefs.get("carrusel_logos", "");
            if (!carruselGuardado.isEmpty()) {
                String[] logos = carruselGuardado.split(";");
                for (String logo : logos) {
                    if (!logo.trim().isEmpty() && new File(logo).exists()) {
                        logosCarrusel.add(logo);
                    }
                }
            }

            actualizarInfoLogos();
            System.out.println("✅ Carrusel cargado: " + logosCarrusel.size() + " logos");

        } catch (Exception e) {
            System.err.println("❌ Error al cargar carrusel: " + e.getMessage());
        }
    }

    /**
     * Guarda el carrusel en las preferencias
     */
    private void guardarCarruselEnPreferencias() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String logo : logosCarrusel) {
                if (sb.length() > 0) sb.append(";");
                sb.append(logo);
            }

            prefs.put("carrusel_logos", sb.toString());
            actualizarInfoLogos();
            System.out.println("✅ Carrusel guardado: " + logosCarrusel.size() + " logos");

        } catch (Exception e) {
            System.err.println("❌ Error al guardar carrusel: " + e.getMessage());
        }
    }

    /**
     * Actualiza la información del carrusel en la interfaz
     */
    private void actualizarInfoLogos() {
        lblInfoLogos.setText("Logos en carrusel: " + logosCarrusel.size());
    }

    /**
     * Método para limpiar el carrusel
     */
    @FXML
    private void limpiarCarrusel() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Limpiar Carrusel");
            alert.setHeaderText("¿Está seguro que desea limpiar el carrusel de logos?");
            alert.setContentText("Se eliminarán todos los logos del carrusel.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                logosCarrusel.clear();
                guardarCarruselEnPreferencias();

                mostrarAlerta("✅ Carrusel limpiado",
                        "El carrusel de logos ha sido limpiado correctamente.",
                        Alert.AlertType.INFORMATION);

                System.out.println("✅ Carrusel de logos limpiado");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al limpiar carrusel: " + e.getMessage());
            mostrarAlerta("❌ Error", "No se pudo limpiar el carrusel: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Método para guardar toda la configuración
     */
    @FXML
    private void guardarConfiguracion() {
        try {
            // Guardar tema
            if (cbxTema.getValue() != null) {
                prefs.put("tema_preferido", cbxTema.getValue());
            }

            // Guardar información de la universidad
            prefs.put("nombre_universidad", txtNombreUniversidad.getText());
            prefs.put("direccion_universidad", txtDireccion.getText());
            prefs.put("telefono_universidad", txtTelefono.getText());
            prefs.put("email_universidad", txtEmail.getText());
            prefs.put("rector_universidad", txtRector.getText());

            // Guardar configuración del sistema
            prefs.putBoolean("notificaciones_activas", chkNotificaciones.isSelected());
            prefs.putBoolean("backup_automatico", chkBackupAutomatico.isSelected());
            prefs.putInt("intervalo_backup", spnIntervaloBackup.getValue());

            // Aplicar tema actual
            aplicarTema(cbxTema.getValue());

            mostrarAlerta("✅ Configuración Guardada",
                    "La configuración ha sido guardada exitosamente.\n\n" +
                            "• Tema: " + cbxTema.getValue() + "\n" +
                            "• Logos en carrusel: " + logosCarrusel.size() + "\n" +
                            "• Watermark: " + (chkWatermarkActivo.isSelected() ? "ACTIVO" : "INACTIVO"),
                    Alert.AlertType.INFORMATION);

            System.out.println("✅ Configuración guardada - Tema: " + cbxTema.getValue());

        } catch (Exception e) {
            System.err.println("❌ Error al guardar configuración: " + e.getMessage());
            mostrarAlerta("❌ Error", "No se pudo guardar la configuración: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Aplica el tema seleccionado
     */
    private void aplicarTema(String tema) {
        try {
            Stage stage = (Stage) cbxTema.getScene().getWindow();
            if (stage != null && stage.getScene() != null) {
                stage.getScene().getStylesheets().clear();
                stage.getScene().getRoot().getStyleClass().removeAll("light-theme", "dark-theme", "corporate-theme");

                switch (tema) {
                    case "Tema Claro":
                        stage.getScene().getRoot().getStyleClass().add("light-theme");
                        stage.getScene().getStylesheets().add(getClass().getResource("/com/union/asistencia/css/themes/light-theme.css").toExternalForm());
                        break;
                    case "Tema Oscuro":
                        stage.getScene().getRoot().getStyleClass().add("dark-theme");
                        stage.getScene().getStylesheets().add(getClass().getResource("/com/union/asistencia/css/themes/dark-theme.css").toExternalForm());
                        break;
                    case "Tema Institucional":
                        stage.getScene().getRoot().getStyleClass().add("corporate-theme");
                        stage.getScene().getStylesheets().add(getClass().getResource("/com/union/asistencia/css/themes/corporate-theme.css").toExternalForm());
                        break;
                }

                // Aplicar estilo base
                stage.getScene().getStylesheets().add(getClass().getResource("/com/union/asistencia/css/styles.css").toExternalForm());

                System.out.println("✅ Tema " + tema + " aplicado");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al aplicar tema: " + e.getMessage());
        }
    }

    /**
     * Muestra una alerta
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Obtiene la lista de logos del carrusel (para uso en MainController)
     */
    public static List<String> getLogosCarrusel() {
        return new ArrayList<>(logosCarrusel);
    }

    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        System.out.println("✅ Configuración inicializada para usuario: " + usuario.getUsername());
    }
}
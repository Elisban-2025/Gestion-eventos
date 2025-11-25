package com.union.asistencia.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Servicio para manejar watermarks rotativos elegantes
 * Integrado con el sistema de temas existente
 */
public class WatermarkService {

    private static WatermarkService instance;
    private Timeline watermarkTimeline;
    private List<String> watermarkTexts;
    private Label watermarkLabel;
    private Random random;

    // Constructor privado para Singleton
    private WatermarkService() {
        this.watermarkTexts = new ArrayList<>();
        this.random = new Random();
        initializeWatermarkTexts();
    }

    public static WatermarkService getInstance() {
        if (instance == null) {
            instance = new WatermarkService();
        }
        return instance;
    }

    /**
     * Inicializa los textos del watermark rotativo
     */
    private void initializeWatermarkTexts() {
        watermarkTexts.clear();
        watermarkTexts.add("UNIVERSIDAD PERUANA UNIÓN");
        watermarkTexts.add("SISTEMA DE GESTIÓN DE ASISTENCIA");
        watermarkTexts.add("CONFIDENCIAL - USO INTERNO");
        watermarkTexts.add("VERSIÓN 1.0.0");
        watermarkTexts.add("© 2024 UPEU - TODOS LOS DERECHOS RESERVADOS");
        watermarkTexts.add("CALIDAD EDUCATIVA ADVENTISTA");
        watermarkTexts.add("EXCELENCIA ACADÉMICA");
        watermarkTexts.add("FORMACIÓN INTEGRAL");

        System.out.println("✅ WatermarkService inicializado con " + watermarkTexts.size() + " textos");
    }

    /**
     * Crea y configura el label del watermark
     */
    private void createWatermarkLabel() {
        watermarkLabel = new Label();
        watermarkLabel.getStyleClass().add("watermark-label");
        watermarkLabel.setMouseTransparent(true);
        watermarkLabel.setFocusTraversable(false);

        // Estilos base del watermark
        watermarkLabel.setStyle(
                "-fx-font-size: 48px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: rgba(0, 80, 158, 0.08); " +
                        "-fx-opacity: 0.8; " +
                        "-fx-rotate: -15; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0.5, 0, 0);"
        );

        System.out.println("✅ Label de watermark creado y configurado");
    }

    /**
     * Inicia el sistema de watermarks en un contenedor
     */
    public void startWatermarkSystem(Pane container) {
        try {
            // Detener timeline existente si hay una
            stopWatermarkSystem();

            // Crear label si no existe
            if (watermarkLabel == null) {
                createWatermarkLabel();
            }

            // Asegurar que el contenedor sea un StackPane
            if (!(container instanceof StackPane)) {
                System.out.println("⚠️  El contenedor no es StackPane, no se puede agregar watermark");
                return;
            }

            StackPane stackContainer = (StackPane) container;

            // Remover watermark anterior si existe
            stackContainer.getChildren().remove(watermarkLabel);

            // Agregar watermark al contenedor
            stackContainer.getChildren().add(watermarkLabel);

            // Posicionar en el centro
            watermarkLabel.setTranslateX(0);
            watermarkLabel.setTranslateY(0);

            // Configurar timeline para rotación de textos
            watermarkTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(8), e -> rotateWatermarkText())
            );
            watermarkTimeline.setCycleCount(Timeline.INDEFINITE);

            // Mostrar primer texto inmediatamente
            rotateWatermarkText();

            // Iniciar timeline
            watermarkTimeline.play();

            System.out.println("✅ Sistema de watermarks iniciado en contenedor");

        } catch (Exception e) {
            System.err.println("❌ Error al iniciar sistema de watermarks: " + e.getMessage());
        }
    }

    /**
     * Rota el texto del watermark
     */
    private void rotateWatermarkText() {
        if (watermarkTexts.isEmpty() || watermarkLabel == null) return;

        try {
            // Seleccionar texto aleatorio
            int randomIndex = random.nextInt(watermarkTexts.size());
            String newText = watermarkTexts.get(randomIndex);

            // Aplicar efecto de transición suave
            watermarkLabel.setOpacity(0.3);
            watermarkLabel.setText(newText);

            // Pequeña variación en la rotación para hacerlo más dinámico
            double rotation = -15 + (random.nextDouble() * 10 - 5); // -20 a -10 grados
            watermarkLabel.setRotate(rotation);

            // Restaurar opacidad con efecto suave
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(1000), watermarkLabel);
            fadeIn.setToValue(0.8);
            fadeIn.play();

            System.out.println("🔄 Watermark rotado: " + newText);

        } catch (Exception e) {
            System.err.println("❌ Error al rotar watermark: " + e.getMessage());
        }
    }

    /**
     * Detiene el sistema de watermarks
     */
    public void stopWatermarkSystem() {
        try {
            if (watermarkTimeline != null) {
                watermarkTimeline.stop();
                watermarkTimeline = null;
                System.out.println("✅ Sistema de watermarks detenido");
            }

            if (watermarkLabel != null) {
                // Remover de cualquier contenedor padre
                if (watermarkLabel.getParent() != null) {
                    ((Pane) watermarkLabel.getParent()).getChildren().remove(watermarkLabel);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error al detener sistema de watermarks: " + e.getMessage());
        }
    }

    /**
     * Actualiza los estilos del watermark según el tema
     */
    public void updateWatermarkStyle(String theme) {
        if (watermarkLabel == null) return;

        try {
            String baseStyle =
                    "-fx-font-size: 48px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-opacity: 0.8; " +
                            "-fx-rotate: -15; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0.5, 0, 0);";

            String colorStyle;

            switch (theme) {
                case "Tema Claro":
                    colorStyle = "-fx-text-fill: rgba(52, 152, 219, 0.08);";
                    break;
                case "Tema Oscuro":
                    colorStyle = "-fx-text-fill: rgba(236, 240, 241, 0.08);";
                    break;
                case "Tema Institucional":
                default:
                    colorStyle = "-fx-text-fill: rgba(0, 80, 158, 0.08);";
                    break;
            }

            watermarkLabel.setStyle(baseStyle + colorStyle);
            System.out.println("🎨 Estilo de watermark actualizado para tema: " + theme);

        } catch (Exception e) {
            System.err.println("❌ Error al actualizar estilo de watermark: " + e.getMessage());
        }
    }

    /**
     * Agrega textos personalizados al watermark
     */
    public void addCustomWatermarkText(String text) {
        if (text != null && !text.trim().isEmpty()) {
            watermarkTexts.add(text.trim());
            System.out.println("✅ Texto personalizado agregado al watermark: " + text);
        }
    }

    /**
     * Limpia todos los textos del watermark
     */
    public void clearWatermarkTexts() {
        watermarkTexts.clear();
        initializeWatermarkTexts(); // Restaurar textos por defecto
        System.out.println("✅ Textos de watermark restaurados a valores por defecto");
    }
}
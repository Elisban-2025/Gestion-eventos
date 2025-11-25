package com.union.asistencia.controller;

import com.union.asistencia.model.Usuario;
import com.union.asistencia.service.WatermarkService;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Controlador base abstracto para implementación global de watermarks
 * Mantiene compatibilidad con la interfaz existente
 */
public abstract class BaseController {

    protected Usuario usuarioLogueado;
    protected WatermarkService watermarkService;

    public BaseController() {
        this.watermarkService = WatermarkService.getInstance();
    }

    /**
     * Establece el usuario logueado (método original de la interfaz)
     */
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
    }

    /**
     * Inicializa el sistema de watermarks en un contenedor
     * Debe ser llamado por los controladores hijos después de cargar su FXML
     */
    protected void initializeWatermark(Pane container) {
        try {
            if (container != null) {
                // Convertir a StackPane si es necesario para superposición
                if (!(container instanceof StackPane)) {
                    System.out.println("⚠️  El contenedor no es StackPane, no se puede agregar watermark");
                    return;
                }

                watermarkService.startWatermarkSystem(container);
                System.out.println("✅ Watermark inicializado en controlador: " + this.getClass().getSimpleName());
            }
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar watermark: " + e.getMessage());
        }
    }

    /**
     * Detiene el sistema de watermarks
     * Debe ser llamado al cerrar la ventana o cambiar de vista
     */
    protected void stopWatermark() {
        try {
            watermarkService.stopWatermarkSystem();
            System.out.println("✅ Watermark detenido en controlador: " + this.getClass().getSimpleName());
        } catch (Exception e) {
            System.err.println("❌ Error al detener watermark: " + e.getMessage());
        }
    }

    /**
     * Actualiza el estilo del watermark según el tema
     */
    protected void updateWatermarkStyle(String theme) {
        try {
            watermarkService.updateWatermarkStyle(theme);
            System.out.println("✅ Estilo de watermark actualizado: " + theme);
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar estilo de watermark: " + e.getMessage());
        }
    }
}
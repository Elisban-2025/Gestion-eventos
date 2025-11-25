package com.union.asistencia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar la vista principal
        Parent root = FXMLLoader.load(getClass().getResource("/com/union/asistencia/view/main.fxml"));
        
        // Configurar la escena
        Scene scene = new Scene(root);
        
        // Configurar el stage principal
        primaryStage.setTitle("Sistema de Asistencia - UPEU Juliaca");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // Pantalla completa
        
        // Establecer icono de la aplicación
        try {
            Image icon = new Image(getClass().getResourceAsStream("/logo.jpeg"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono: " + e.getMessage());
        }
        
        // Mostrar la ventana
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        // Primero inicializar Spring Boot en un hilo separado
        new Thread(() -> {
            try {
                org.springframework.boot.SpringApplication.run(com.union.asistencia.AsistenciaApplication.class, args);
            } catch (Exception e) {
                System.err.println("Error iniciando Spring Boot: " + e.getMessage());
            }
        }).start();
        
        // Luego iniciar JavaFX
        launch(args);
    }
}
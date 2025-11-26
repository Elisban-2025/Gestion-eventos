package com.union.asistencia.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class QRReader {

    public static String leerQRDesdeArchivo(String imagePath) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(imagePath));
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();

        } catch (IOException | NotFoundException e) {
            System.err.println("Error leyendo QR: " + e.getMessage());
            return null;
        }
    }

    public static String seleccionarYLeerQR() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar código QR escaneado");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            return leerQRDesdeArchivo(file.getAbsolutePath());
        }

        return null;
    }
}
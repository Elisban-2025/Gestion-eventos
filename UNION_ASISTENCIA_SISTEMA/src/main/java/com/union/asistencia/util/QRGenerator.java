package com.union.asistencia.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import lombok.extern.java.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Log
public class QRGenerator {

    public static Image generarQRCode(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            return new Image(inputStream);

        } catch (WriterException | IOException e) {
            log.severe("Error al generar código QR: " + e.getMessage());
            return null;
        }
    }

    public static String generarCodigoAsistencia(String estudianteCodigo, String asignaturaCodigo) {
        return String.format("UPeU-ASIST-%s-%s-%d",
                estudianteCodigo,
                asignaturaCodigo,
                System.currentTimeMillis());
    }

    public static String generarCodigoEvento(String eventoId, String participanteId) {
        return String.format("UPeU-EVENT-%s-%s-%d",
                eventoId,
                participanteId,
                System.currentTimeMillis());
    }
}
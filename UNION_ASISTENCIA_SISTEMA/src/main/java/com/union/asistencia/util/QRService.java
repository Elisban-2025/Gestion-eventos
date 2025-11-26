package com.union.asistencia.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

public class QRService {

    public static ImageView generarQR(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);

            return imageView;

        } catch (WriterException e) {
            e.printStackTrace();
            return new ImageView();
        }
    }

    public static String generarDataEstudiante(String id, String codigo, String nombre, String curso) {
        return String.format("EST|%s|%s|%s|%s|%s",
                id, codigo, nombre, curso, System.currentTimeMillis());
    }

    public static String generarDataDocente(String id, String codigo, String nombre, String materia) {
        return String.format("DOC|%s|%s|%s|%s|%s",
                id, codigo, nombre, materia, System.currentTimeMillis());
    }

    public static QRData procesarQRData(String qrData) {
        if (qrData == null || qrData.isEmpty()) {
            return null;
        }

        String[] partes = qrData.split("\\|");
        if (partes.length >= 6) {
            String tipo = partes[0];
            String id = partes[1];
            String codigo = partes[2];
            String nombre = partes[3];
            String detalle = partes[4];
            String timestamp = partes[5];

            return new QRData(tipo, id, codigo, nombre, detalle, timestamp);
        }

        return null;
    }

    public static class QRData {
        private String tipo;
        private String id;
        private String codigo;
        private String nombre;
        private String detalle;
        private String timestamp;

        public QRData(String tipo, String id, String codigo, String nombre, String detalle, String timestamp) {
            this.tipo = tipo;
            this.id = id;
            this.codigo = codigo;
            this.nombre = nombre;
            this.detalle = detalle;
            this.timestamp = timestamp;
        }

        // Getters
        public String getTipo() { return tipo; }
        public String getId() { return id; }
        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public String getDetalle() { return detalle; }
        public String getTimestamp() { return timestamp; }

        public boolean esEstudiante() { return "EST".equals(tipo); }
        public boolean esDocente() { return "DOC".equals(tipo); }

        @Override
        public String toString() {
            return String.format("%s - %s (%s)", nombre, codigo, detalle);
        }
    }

    public static String generarDataEvento(String eventoId, String nombreEvento, String lugar, String fecha) {
        return String.format("EVT|%s|%s|%s|%s|%s",
                eventoId, nombreEvento, lugar, fecha, System.currentTimeMillis());
    }


    public static EventoQRData procesarQREvento(String qrData) {
        if (qrData == null || qrData.isEmpty()) {
            return null;
        }

        String[] partes = qrData.split("\\|");
        if (partes.length >= 6 && "EVT".equals(partes[0])) {
            String eventoId = partes[1];
            String nombreEvento = partes[2];
            String lugar = partes[3];
            String fecha = partes[4];
            String timestamp = partes[5];

            return new EventoQRData(eventoId, nombreEvento, lugar, fecha, timestamp);
        }

        return null;
    }

    public static class EventoQRData {
        private String eventoId;
        private String nombreEvento;
        private String lugar;
        private String fecha;
        private String timestamp;

        public EventoQRData(String eventoId, String nombreEvento, String lugar, String fecha, String timestamp) {
            this.eventoId = eventoId;
            this.nombreEvento = nombreEvento;
            this.lugar = lugar;
            this.fecha = fecha;
            this.timestamp = timestamp;
        }

        // Getters
        public String getEventoId() { return eventoId; }
        public String getNombreEvento() { return nombreEvento; }
        public String getLugar() { return lugar; }
        public String getFecha() { return fecha; }
        public String getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("%s - %s (%s)", nombreEvento, lugar, fecha);
        }
    }

}
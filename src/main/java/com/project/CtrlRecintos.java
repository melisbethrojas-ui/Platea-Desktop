package com.project;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class CtrlRecintos implements Initializable{

    // Colores de fondo por zona (solo identifican la zona).
    private static final Map<String, Color> COLORS_ZONES = new HashMap<>();

    static {
        COLORS_ZONES.put("Z1", Color.web("#4A90D9"));
        COLORS_ZONES.put("Z2", Color.web("#4CAF50"));
        COLORS_ZONES.put("Z3", Color.web("#E6C229"));
        COLORS_ZONES.put("Z4", Color.web("#9C27B0"));
        COLORS_ZONES.put("Z5", Color.web("#F28C28"));
        COLORS_ZONES.put("Z6", Color.web("#00ACC1"));
        COLORS_ZONES.put("Z7", Color.web("#E91E63"));
    }

    private static final Color COLOR_LIURE = Color.web("#9E9E9E");
    private static final Color COLOR_VENUT = Color.web("#D32F2F");

    private JSONArray matriu;
    private int files;
    private int columnes;
    private double radi;

    @FXML
    private Canvas canvaRecintes;

    @FXML
    private Label labelRecinte;

    @FXML
    private TextArea txtDetallVenda;

    @FXML
    void volverConfiguracion(ActionEvent event) {
        UtilsViews.setView("ViewTaquilla");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        canvaRecintes.setOnMouseClicked(this::seientClicat);
    }

    // Recibe la matriz de asientos del evento y la pinta en el canvas.
    public void rebreMapa(JSONObject msgObj) {

        if (msgObj.has("error")) {
            System.out.println("Error mapa: " + msgObj.optString("error"));
            return;
        }

        files = msgObj.optInt("files", 0);
        columnes = msgObj.optInt("columnes", 0);
        matriu = msgObj.optJSONArray("matriu");

        if (matriu == null || files <= 0 || columnes <= 0) {
            System.out.println("Mapa sense dades vàlides.");
            return;
        }

        labelRecinte.setText("Esdeveniment " + msgObj.optString("esdev", ""));
        txtDetallVenda.clear();

        drawSitios();
    }

    // Pinta la matriz recibida: cuadrado por zona + circulo por estado.
    private void drawSitios() {

        GraphicsContext gc = canvaRecintes.getGraphicsContext2D();

        gc.clearRect(0, 0, canvaRecintes.getWidth(), canvaRecintes.getHeight());

        radi = Math.min(
            canvaRecintes.getWidth() / columnes,
            canvaRecintes.getHeight() / files
        ) / 2;

        double costat = radi * 2;

        for (int f = 0; f < files; f++) {
            for (int c = 0; c < columnes; c++) {

                JSONArray celda = matriu.getJSONArray(f).optJSONArray(c);

                // null = hueco: ahi no hay asiento.
                if (celda == null) {
                    continue;
                }

                double x = c * costat;
                double y = f * costat;

                Color colorZona = COLORS_ZONES.getOrDefault(celda.getString(0), Color.GRAY);
                gc.setFill(colorZona);
                gc.fillRect(x, y, costat, costat);

                boolean venut = celda.length() > 4 && "V".equals(celda.getString(3));

                gc.setFill(venut ? COLOR_VENUT : COLOR_LIURE);
                gc.fillOval(x, y, costat, costat);
            }
        }
    }

    // Hit-test: localiza la celda pulsada y muestra el detalle si esta vendida.
    private void seientClicat(MouseEvent event) {

        if (matriu == null || radi <= 0) {
            return;
        }

        double costat = radi * 2;

        int fila = (int) (event.getY() / costat);
        int columna = (int) (event.getX() / costat);

        if (fila < 0 || fila >= files || columna < 0 || columna >= columnes) {
            return;
        }

        JSONArray celda = matriu.getJSONArray(fila).optJSONArray(columna);

        if (celda == null || celda.length() < 9 || !"V".equals(celda.getString(3))) {
            return;
        }

        String zona = celda.getString(0);
        int filaZona = celda.getInt(1);
        int numero = celda.getInt(2);
        String codi = celda.getString(4);
        String comprador = celda.getString(5);
        String correu = celda.getString(6);
        String dataCompra = celda.getString(7);
        double preu = celda.getDouble(8);

        txtDetallVenda.setText(
            "Zona " + zona + " - fila " + filaZona + ", seient " + numero + "\n"
            + "Venda: " + codi + "\n"
            + "Comprador: " + comprador + "\n"
            + "Correu: " + correu + "\n"
            + "Data compra: " + dataCompra + "\n"
            + "Preu: " + preu + " €"
        );
    }

}

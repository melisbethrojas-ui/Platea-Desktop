package com.project;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CtrlPuerta {

    @FXML
    private TextField txtCodigoEntrada;

    @FXML
    private Label txtResultado;

    @FXML
    private void volverConfiguracion() {
        if (Main.wsClient != null) {
            Main.wsClient.close();
        }

        UtilsViews.setView("ViewConfig");
    }

    public void rebreValidacio(JSONObject resposta) {

        System.out.println("Respuesta recibida: " + resposta);

        if (resposta.getBoolean("ok")) {

            String text = "✅ ENTRADA VÀLIDA\n" +
                    "Titular: " + resposta.getString("titular") +
                    "\nTipus: " + resposta.getString("tipusEntrada");

            if (resposta.has("fila")) {
                text += "\nFila: " + resposta.getInt("fila");
                text += "\nSeient: " + resposta.getInt("numero");
            }

            txtResultado.setText(text);

        } else {
            txtResultado.setText("❌ " + resposta.optString("motiu", "Entrada no válida"));
        }
    }

    @FXML
    private void validarEntrada() {
        String codigo = txtCodigoEntrada.getText();

        if (codigo == null || codigo.trim().isEmpty()) {
            txtResultado.setText("Introduce el código de entrada");
            return;
        }

        if (Main.wsClient == null) {
            txtResultado.setText("No hay conexión con el servidor");
            return;
        }

        txtResultado.setText("Validando entrada...");

        JSONObject request = new JSONObject();
        request.put("type", "validar");
        request.put("localitzador", codigo.trim());

        System.out.println("¿WebSocket conectado?: " + Main.wsClient.isOpen());
        System.out.println("Enviando petición: " + request);

        Main.wsClient.safeSend(request.toString());

    }
}
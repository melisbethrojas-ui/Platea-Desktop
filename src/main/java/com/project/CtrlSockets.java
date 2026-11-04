
package com.project;

import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;

public class CtrlSockets implements Initializable {

    @FXML
    private TextField txtField;

    @FXML
    private TextArea txtArea;

    @FXML
    private ChoiceBox<String> choiceType;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        choiceType.getItems().clear();
        choiceType.getItems().addAll("ping", "bounce");
        choiceType.setValue("ping");

        txtField.setOnAction(event -> sendMessage());
    }

    // Construye el JSON y lo envía al servidor.
    @FXML
    private void sendMessage() {

        String txt = txtField.getText().trim();
        String type = choiceType.getValue();

        if (type == null) {
            return;
        }

        JSONObject obj = new JSONObject();
        obj.put("type", type);

        // Bounce devuelve el mismo mensaje.
        if (type.equals("bounce")) {
            obj.put("message", txt);
        }

        Main.wsClient.safeSend(obj.toString());

        txtArea.appendText("\n\nEnviado: " + obj);
        txtField.clear();

        System.out.println("Send WebSocket: " + obj);
    }

    // Recibe las respuestas del servidor.
    public void receiveMessage(JSONObject messageObj) {

        System.out.println("Receive WebSocket: " + messageObj);

        String type = messageObj.optString("type", "");

        if (type.equals("pong")) {

            txtArea.appendText("\n\nPong: servidor conectado correctamente");

        } else if (type.equals("bounce")) {

            txtArea.appendText(
                "\n\nBounce: " + messageObj.optString("message", "")
            );

        } else {

            txtArea.appendText(
                "\n\nRespuesta desconocida: " + messageObj
            );
        }
    }
}

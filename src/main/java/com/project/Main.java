package com.project;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    public static UtilsWS wsClient;

    public static CtrlConfig ctrlConfig;
    public static CtrlSockets ctrlSockets;
    public static CtrlTaquilla ctrlTaquilla;
    public static CtrlRecintos ctrlSalas;

    public static void main(String[] args) {
        launch(args);
    }

    // Inicializa la ventana, carga las vistas y recupera la configuración guardada.
    @Override
    public void start(Stage stage) throws Exception {

        final int windowWidth = 400;
        final int windowHeight = 300;

        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");

        UtilsViews.addView(getClass(), "ViewConfig", "/assets/viewConfig.fxml");
        UtilsViews.addView(getClass(), "ViewSockets", "/assets/viewSockets.fxml");
        UtilsViews.addView(getClass(), "ViewTaquilla", "/assets/viewTaquilla.fxml");
        UtilsViews.addView(getClass(), "ViewPuerta", "/assets/viewPuerta.fxml");
        UtilsViews.addView(getClass(), "ViewOficina", "/assets/viewOficina.fxml");
        UtilsViews.addView(getClass(), "ViewSalas", "/assets/viewSalas.fxml");
        

        ctrlConfig = (CtrlConfig) UtilsViews.getController("ViewConfig");
        ctrlSockets = (CtrlSockets) UtilsViews.getController("ViewSockets");
        ctrlTaquilla= (CtrlTaquilla) UtilsViews.getController("ViewTaquilla");
        ctrlSalas = (CtrlRecintos) UtilsViews.getController("ViewSalas");
        
        Scene scene = new Scene(UtilsViews.parentContainer);

        stage.setScene(scene);
        stage.setTitle("Platea - Desktop");
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.show();

        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image("file:/icons/icon.png");
            stage.getIcons().add(icon);
        }

        if (ctrlConfig.loadConfig()) {
            connectToServer();
        } else {
            UtilsViews.setView("ViewConfig");
        }
    }

    // Cierra la conexión con el servidor cuando se termina la aplicación.
    @Override
    public void stop() {
        if (wsClient != null) {
            wsClient.forceExit();
        }
        System.exit(0);
    }

    // Convierte un JSONArray en una lista Java del tipo indicado.
    public static <T> List<T> jsonArrayToList(JSONArray array, Class<T> clazz) {
        List<T> list = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            T value = clazz.cast(array.get(i));
            list.add(value);
        }

        return list;
    }

    // Valida la URL, establece la conexión y abre la pantalla correspondiente a la ubicación elegida.
    public static void connectToServer() {

        if (ctrlConfig.getServidor().isEmpty()) {
            ctrlConfig.txtMessage.setTextFill(Color.RED);
            ctrlConfig.txtMessage.setText("Introduce la URL del servidor");
            return;
        }

        ctrlConfig.txtMessage.setTextFill(Color.BLACK);
        ctrlConfig.txtMessage.setText("Conectando...");

        wsClient = UtilsWS.getSharedInstance(ctrlConfig.getServidor());

        wsClient.onOpen((response) -> {
            Platform.runLater(() -> {
                ctrlConfig.txtMessage.setTextFill(Color.GREEN);
                ctrlConfig.txtMessage.setText("Conexión correcta");
                ctrlConfig.saveConfig();

                switch (ctrlConfig.getUbicacion()) {
                    case "taquilla":
                        UtilsViews.setView("ViewTaquilla");

                        JSONObject request = new JSONObject();
                        request.put("type", "esdev");
                        wsClient.safeSend(request.toString());
                        break;

                    case "porta":
                        UtilsViews.setView("ViewPuerta");
                        break;

                    case "oficina":
                        UtilsViews.setView("ViewOficina");
                        break;
                    case "recintes":
                        UtilsViews.setView("ViewSalas");
                        break;
                }
            });
        });

        wsClient.onMessage((response) -> {
            Platform.runLater(() -> wsMessage(response));
        });

        wsClient.onError((response) -> {
            Platform.runLater(() -> wsError(response));
        });

        wsClient.connect();
    }

    // Distribuye los mensajes recibidos: los eventos se muestran en taquilla y el resto se deriva al controlador de pruebas WebSocket.
    private static void wsMessage(String response) {
        JSONObject msgObj = new JSONObject(response);

        String type = msgObj.optString("type", "");

        if (type.equals("esdev")) {
            ctrlTaquilla.receiveEsdev(msgObj);
        } else if (type.equals("mapa")) {
            ctrlSalas.rebreMapa(msgObj);
        } else {
            ctrlSockets.receiveMessage(msgObj);
        }
    }

    // Muestra el error de conexión y devuelve al usuario a la pantalla de configuración.
    private static void wsError(String response) {

        ctrlConfig.txtMessage.setTextFill(Color.RED);
        ctrlConfig.txtMessage.setText(
            "No se pudo conectar. Comprueba la URL e inténtalo de nuevo."
        );

        UtilsViews.setView("ViewConfig");

        System.out.println(response);
    }
}
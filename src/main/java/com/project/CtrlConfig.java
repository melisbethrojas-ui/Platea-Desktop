package com.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CtrlConfig implements Initializable {

    private static final String CONFIG_FILE = "CONFIG.XML";

    @FXML
    private ChoiceBox<String> choiceUbicacion;

    @FXML
    private TextField txtServidor;

    @FXML
    public Label txtMessage;

    // Carga las ubicaciones disponibles
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        choiceUbicacion.getItems().addAll(
            "taquilla",
            "porta",
            "oficina",
            "recintes"
        );

        choiceUbicacion.setValue("taquilla");
    }

    @FXML
    private void connectToServer() {
        Main.connectToServer();
    }

    public String getUbicacion() {
        return choiceUbicacion.getValue();
    }

    public String getServidor() {
        return txtServidor.getText().trim();
    }

    // Lee CONFIG.XML y recupera la ubicación y la URL guardadas en una ejecución anterior.
    public boolean loadConfig() {
        File file = new File(CONFIG_FILE);

        if (!file.exists()) {
            return false;
        }

        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(file)) {
            properties.loadFromXML(input);

            String ubicacion = properties.getProperty("ubicacion");
            String servidor = properties.getProperty("servidor");

            if (ubicacion == null || servidor == null) {
                return false;
            }

            choiceUbicacion.setValue(ubicacion);
            txtServidor.setText(servidor);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // Guarda en CONFIG.XML la ubicación seleccionada y la dirección del servidor.
    public void saveConfig() {
        Properties properties = new Properties();

        properties.setProperty("ubicacion", getUbicacion());
        properties.setProperty("servidor", getServidor());

        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.storeToXML(output, "Configuracion de Platea");

        } catch (Exception e) {
            txtMessage.setText("Error guardando CONFIG.XML");
        }
    }
}
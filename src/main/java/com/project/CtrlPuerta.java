package com.project;

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
}
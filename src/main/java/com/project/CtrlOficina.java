package com.project;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class CtrlOficina {

    @FXML
    private TableView<?> tableReembolsos;

    // Cierra la conexión actual y vuelve a la pantalla de configuración.
   @FXML
    private void volverConfiguracion() {
        if (Main.wsClient != null) {
            Main.wsClient.close();
        }

        UtilsViews.setView("ViewConfig");
    }
}
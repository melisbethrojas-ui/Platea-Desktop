
package com.project;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class CtrlTaquilla {

    @FXML private Label txtVentas;
    @FXML private AnchorPane paneOcupacion;
    @FXML private VBox contenedorEventos;

    // Cierra la conexión actual y vuelve a la pantalla de configuración.
    @FXML
    private void volverConfiguracion() {
        if (Main.wsClient != null) {
            Main.wsClient.close();
        }

        UtilsViews.setView("ViewConfig");
    }

    // Recibe los eventos enviados por el servidor y los muestra en pantalla.
    public void receiveEsdev(JSONObject messageObj) {

        // Limpiar el contenido anterior para evitar duplicar eventos.
        contenedorEventos.getChildren().clear();

        // Obtenemos el objeto que contiene todos los eventos.
        JSONObject esdeveniments =
            messageObj.optJSONObject("esdeveniments");

        // Comprobamos si el servidor ha devuelto algún evento.
        if (esdeveniments == null || esdeveniments.length() == 0) {

            Label mensaje = new Label(
                "No hi ha esdeveniments disponibles."
            );

            mensaje.getStyleClass().add("event-info");

            contenedorEventos.getChildren().add(mensaje);
            return;
        }

        // Recorremos todos los eventos recibidos del servidor.
        for (String id : esdeveniments.keySet()) {

            JSONObject esdeveniment =
                esdeveniments.optJSONObject(id);

            // Ignoramos los elementos que no sean objetos JSON válidos.
            if (esdeveniment == null) {
                continue;
            }

            // Creamos el nombre del evento.
            Label nom = new Label(
                esdeveniment.optString("nom", "Sense nom")
            );

            nom.getStyleClass().add("event-name");

            // Mostramos la fecha y hora del evento.
            Label data = new Label(
                "Data: " + esdeveniment.optString("dataHora", "")
            );

            data.getStyleClass().add("event-info");

            // Mostramos la categoría del evento.
            Label categoria = new Label(
                "Categoria: " + esdeveniment.optString("categoria", "")
            );

            categoria.getStyleClass().add("event-info");

            // Mostramos la descripción del evento.
            Label descripcio = new Label(
                esdeveniment.optString("descripcio", "")
            );

            descripcio.setWrapText(true);
            descripcio.getStyleClass().add("event-description");

            // Creamos el contenedor visual de cada evento.
            VBox bloqueEvento = new VBox(7);

            bloqueEvento.getStyleClass().add("event-card");

            bloqueEvento.getChildren().addAll(
                nom,
                data,
                categoria,
                descripcio
            );

            // Obtenemos los tipos de entrada asociados al evento.
            JSONArray tipusEntrades =
                esdeveniment.optJSONArray("tipusEntrades");

            // Comprobamos si el evento tiene tipos de entrada.
            if (tipusEntrades != null && tipusEntrades.length() > 0) {

                Label tituloEntradas =
                    new Label("Tipus d'entrada:");

                tituloEntradas.getStyleClass().add("ticket-heading");

                bloqueEvento.getChildren().add(tituloEntradas);

                // Recorremos los diferentes tipos de entrada.
                for (int i = 0; i < tipusEntrades.length(); i++) {

                    JSONObject entrada =
                        tipusEntrades.optJSONObject(i);

                    // Ignoramos las entradas que no sean objetos JSON válidos.
                    if (entrada == null) {
                        continue;
                    }

                    // Obtenemos la zona correspondiente a la entrada.
                    String zona = entrada.optString("zona", "");

                    // Si la zona llega como objeto JSON, obtenemos su nombre.
                    JSONObject zonaObj =
                        entrada.optJSONObject("zona");

                    if (zonaObj != null) {
                        zona = zonaObj.optString(
                            "nom",
                            zonaObj.toString()
                        );
                    }

                    // Mostramos los datos de cada tipo de entrada.
                    Label datosEntrada = new Label(
                        "- " + entrada.optString("nom", "Sense nom")
                        + " | Zona: " + zona
                        + " | Preu: " + entrada.optString("preu", "")
                        + " | Quota: " + entrada.optString("quota", "")
                    );

                    datosEntrada.setWrapText(true);
                    datosEntrada.getStyleClass().add("ticket-info");

                    bloqueEvento.getChildren().add(datosEntrada);
                }
            }

            // Añadimos el evento completo al contenedor principal.
            contenedorEventos.getChildren().add(bloqueEvento);
        }
    }
}

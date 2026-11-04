package com.project;

import java.util.ArrayList;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class UtilsViews {

    public static StackPane parentContainer = new StackPane();
    public static ArrayList<Object> controllers = new ArrayList<>();

    // Carga un archivo FXML, lo añade al contenedor principal y guarda su controlador asociado.
    public static void addView(Class<?> cls, String name, String path) throws Exception {
        
        boolean defaultView = false;
        FXMLLoader loader = new FXMLLoader(cls.getResource(path));
        Pane view = loader.load();
        ObservableList<Node> children = parentContainer.getChildren();

        // La primera vista cargada se establece como vista inicial.
        if (children.isEmpty()) {
            defaultView = true;
        }

        view.setId(name);
        view.setVisible(defaultView);
        view.setManaged(defaultView);

        children.add(view);
        controllers.add(loader.getController());
    }

    // Devuelve el controlador de la vista identificada por su nombre.
    public static Object getController(String viewId) {
        int index = 0;
        for (Node n : parentContainer.getChildren()) {
            if (n.getId().equals(viewId)) {
                return controllers.get(index);
            }
            index++;
        }
        return null;
    }

    // Obtiene el identificador de la vista que está visible actualmente.
    public static String getActiveView() {
        for (Node n : parentContainer.getChildren()) {
            if (n.isVisible()) {
                return n.getId();
            }
        }
        return null; // No hi ha cap vista activa
    }

    // Muestra la vista solicitada y oculta todas las demás.
    public static void setView(String viewId) {

        ArrayList<Node> list = new ArrayList<>();
        list.addAll(parentContainer.getChildrenUnmodifiable());

        // Muestra la vista solicitada y oculta las demás.
        for (Node n : list) {
            if (n.getId().equals(viewId)) {
                n.setVisible(true);
                n.setManaged(true);
            } else {
                n.setVisible(false);
                n.setManaged(false);
            }
        }

        // Quita el foco de los botones para evitar que quede seleccionado al cambiar de pantalla.
        parentContainer.requestFocus();
    }

    // Cambia de vista mediante una transición horizontal animada.
    public static void setViewAnimating(String viewId) {

        ArrayList<Node> list = new ArrayList<>();
        list.addAll(parentContainer.getChildrenUnmodifiable());

        // Localiza la vista que está activa en este momento.
        Node curView = null;
        for (Node n : list) {
            if (n.isVisible()) {
                curView = n;
            }
        }

        if (curView.getId().equals(viewId)) {
            return; // No hace nada si la vista solicitada ya está activa.
        }

        // Localiza la vista que se quiere mostrar a continuación.
        Node nxtView = null;
        for (Node n : list) {
            if (n.getId().equals(viewId)) {
                nxtView = n;
            }
        }

        // Hace visible la nueva vista antes de iniciar la animación.
        nxtView.setVisible(true);
        nxtView.setManaged(true);

        // Calcula el desplazamiento de las vistas para realizar la transición.
        double width = parentContainer.getScene().getWidth();
        double xLeftStart = 0;
        double xLeftEnd = 0;
        double xRightStart = 0;
        double xRightEnd = 0;
        Node animatedViewLeft = null;
        Node animatedViewRight = null;

        if (list.indexOf(curView) < list.indexOf(nxtView)) {

            // Si la vista actual aparece antes en la lista, la transición se realiza hacia la izquierda.
            xLeftStart = 0;
            xLeftEnd = -width;
            xRightStart = width;
            xRightEnd = 0;
            animatedViewLeft = curView;
            animatedViewRight = nxtView;

            curView.translateXProperty().set(xLeftStart);
            nxtView.translateXProperty().set(xRightStart);

        } else { 

            // Si la vista actual aparece después en la lista, la transición se realiza hacia la derecha.
            xLeftStart = -width;
            xLeftEnd = 0;
            xRightStart = 0;
            xRightEnd = width;
            animatedViewLeft = nxtView;
            animatedViewRight = curView;

            curView.translateXProperty().set(xRightStart);
            nxtView.translateXProperty().set(xLeftStart);
        }

        // Ejecuta la animación de salida y entrada de las vistas. 
        final double seconds = 0.4;
        KeyValue kvLeft = new KeyValue(animatedViewLeft.translateXProperty(), xLeftEnd, Interpolator.EASE_BOTH);
        KeyFrame kfLeft = new KeyFrame(Duration.seconds(seconds), kvLeft);
        Timeline timelineLeft = new Timeline();
        timelineLeft.getKeyFrames().add(kfLeft);
        timelineLeft.play();

        // Ejecuta la segunda parte de la transición y, al terminar, oculta la vista anterior. 
        KeyValue kvRight = new KeyValue(animatedViewRight.translateXProperty(), xRightEnd, Interpolator.EASE_BOTH);
        KeyFrame kfRight = new KeyFrame(Duration.seconds(seconds), kvRight);
        Timeline timelineRight = new Timeline();
        timelineRight.getKeyFrames().add(kfRight);
        timelineRight.setOnFinished(t -> {
            // Oculta las demás vistas y restablece sus posiciones.
            for (Node n : list) {
                if (!n.getId().equals(viewId)) {
                    n.setVisible(false);
                    n.setManaged(false);
                }
                n.translateXProperty().set(0);
            }
        });
        timelineRight.play();

        // Quita el foco de los botones para evitar que quede seleccionado al cambiar de pantalla.
        parentContainer.requestFocus();
    }
}

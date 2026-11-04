package com.project;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

public class UtilsWS {

    private static UtilsWS sharedInstance = null;

    private WebSocketClient client;

    private Consumer<String> onOpenCallBack;
    private Consumer<String> onMessageCallBack;
    private Consumer<String> onErrorCallBack;

    private String location;

    private boolean connecting = false;

    // Constructor privado: la instancia se obtiene mediante getSharedInstance para reutilizar la conexión.
    private UtilsWS(String location) {
        this.location = location;
    }

    // Inicia la conexión si no existe ya una conexión abierta o en proceso de apertura.
    public void connect() {

        if (client != null && (client.isOpen() || connecting)) {
            return;
        }

        createNewWebSocketClient();
    }

    // Crea el cliente WebSocket y define qué hacer al conectar, recibir mensajes, cerrar o producirse un error.
    private void createNewWebSocketClient() {

        try {
            connecting = true;

            client = new WebSocketClient(new URI(location), new Draft_6455()) {

                @Override
                public void onOpen(ServerHandshake handshake) {

                    connecting = false;

                    String message = "WS connected to: " + getURI();
                    System.out.println(message);

                    if (onOpenCallBack != null) {
                        onOpenCallBack.accept(message);
                    }
                }

                @Override
                public void onMessage(String message) {

                    if (onMessageCallBack != null) {
                        onMessageCallBack.accept(message);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                    connecting = false;

                    System.out.println("WS closed: " + reason);
                }

                @Override
                public void onError(Exception e) {

                    connecting = false;

                    String error = e.getMessage();
                    String message = "WS connection error: " + error;

                    System.out.println(message);

                    if (onErrorCallBack != null) {
                        onErrorCallBack.accept(message);
                    }
                }
            };

            client.connect();

        } catch (URISyntaxException e) {

            connecting = false;

            String message = "URL no válida: " + location;

            System.out.println(message);

            if (onErrorCallBack != null) {
                onErrorCallBack.accept(message);
            }
        }
    }

    // Devuelve la instancia compartida; si cambia la URL, cierra la conexión anterior y crea otra.
    public static synchronized UtilsWS getSharedInstance(String location) {

        if (sharedInstance == null ||
                !sharedInstance.location.equals(location)) {

            if (sharedInstance != null &&
                    sharedInstance.client != null) {
                sharedInstance.client.close();
            }

            sharedInstance = new UtilsWS(location);
        }

        return sharedInstance;
    }

    // Registra las funciones que recibirán los avisos de conexión, mensajes y errores.
    public void onOpen(Consumer<String> callBack) {
        onOpenCallBack = callBack;
    }

    public void onMessage(Consumer<String> callBack) {
        onMessageCallBack = callBack;
    }

    public void onError(Consumer<String> callBack) {
        onErrorCallBack = callBack;
    }

    // Envía texto únicamente si el cliente está conectado; en caso contrario, informa del problema.
    public void safeSend(String text) {

        try {
            if (client != null && client.isOpen()) {
                client.send(text);
            } else {
                System.out.println("WS Error: Client is not connected.");
            }
        } catch (Exception e) {
            System.out.println("WS Error sending message: " + e.getMessage());
        }
    }

    // Fuerza el cierre de la conexión, esperando a que termine antes de salir.
    public void forceExit() {

        System.out.println("WS Closing ...");

        try {
            if (client != null && !client.isClosed()) {
                client.closeBlocking();
            }
        } catch (Exception e) {
            System.out.println("WS Error closing connection: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    // Indica si existe una conexión WebSocket abierta.
    public boolean isOpen() {
        return client != null && client.isOpen();
    }
    
    // Solicita el cierre de la conexión si todavía está abierta.
    public void close() {
    if (client != null && !client.isClosed()) {
        client.close();
    }
}
}
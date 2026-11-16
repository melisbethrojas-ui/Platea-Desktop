package com.project;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    private ScheduledExecutorService keepAliveScheduler;

    // Constructor privado: la instancia se obtiene mediante getSharedInstance para
    // reutilizar la conexión.
    private UtilsWS(String location) {
        this.location = location;
    }

    // Inicia la conexión si no existe ya una conexión abierta o en proceso de
    // apertura.
    public void connect() {

        if (client != null && (client.isOpen() || connecting)) {
            return;
        }

        createNewWebSocketClient();
    }

    // Crea el cliente WebSocket y define qué hacer al conectar, recibir mensajes,
    // cerrar o producirse un error.
    private void createNewWebSocketClient() {

        try {
            connecting = true;

            client = new WebSocketClient(new URI(location), new Draft_6455()) {

                @Override
                public void onOpen(ServerHandshake handshake) {

                    connecting = false;

                    String message = "WS connected to: " + getURI();
                    System.out.println(message);

                    iniciarKeepAlive();

                    if (onOpenCallBack != null) {
                        onOpenCallBack.accept(message);
                    }
                }

            

                @Override
                public void onMessage(String message) {
                    System.out.println("RESPUESTA DEL SERVIDOR: " + message);

                    if (onMessageCallBack != null) {
                        onMessageCallBack.accept(message);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                    connecting = false;
                    aturarKeepAlive();

                    System.out.println("WS closed");
                    System.out.println("Código: " + code);
                    System.out.println("Motivo: " + reason);
                    System.out.println("Cierre remoto: " + remote);
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

    // Devuelve la instancia compartida; si cambia la URL, cierra la conexión
    // anterior y crea otra.
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

    // Registra las funciones que recibirán los avisos de conexión, mensajes y
    // errores.
    public void onOpen(Consumer<String> callBack) {
        onOpenCallBack = callBack;
    }

    public void onMessage(Consumer<String> callBack) {
        onMessageCallBack = callBack;
    }

    public void onError(Consumer<String> callBack) {
        onErrorCallBack = callBack;
    }

    // Envía texto únicamente si el cliente está conectado; en caso contrario,
    // informa del problema.
    public void safeSend(String text) {
        try {
            System.out.println("1. Comprobando conexión...");

            if (client != null && client.isOpen()) {
                System.out.println("2. Enviando mensaje: " + text);

                client.send(text);

                System.out.println("3. Mensaje enviado correctamente");
            } else {
                System.out.println("ERROR: Cliente desconectado");
            }

        } catch (Exception e) {
            System.out.println("ERROR al enviar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void iniciarKeepAlive() {
        aturarKeepAlive();
        keepAliveScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ws-keepalive");
            t.setDaemon(true);
            return t;
        });
        keepAliveScheduler.scheduleAtFixedRate(() -> {
            try {
                if (client != null && client.isOpen()) {
                    client.sendPing();
                }
            } catch (Exception e) {
                System.out.println("KeepAlive ping falló: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    private void aturarKeepAlive() {
        if (keepAliveScheduler != null && !keepAliveScheduler.isShutdown()) {
            keepAliveScheduler.shutdownNow();
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
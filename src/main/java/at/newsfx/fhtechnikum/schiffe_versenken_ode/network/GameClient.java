package at.newsfx.fhtechnikum.schiffe_versenken_ode.network;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Consumer;

public class GameClient {

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> onMessageReceived;
    private Consumer<String> onError;
    private Runnable onConnected;
    private volatile boolean running;

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.running = false;
    }

    public void setOnMessageReceived(Consumer<String> handler) {
        this.onMessageReceived = handler;
    }

    public void setOnError(Consumer<String> handler) {
        this.onError = handler;
    }

    public void setOnConnected(Runnable handler) {
        this.onConnected = handler;
    }

    public void connect() {
        running = true;
        Thread clientThread = new Thread(() -> {
            try {
                socket = new Socket(host, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                if (onConnected != null) {
                    onConnected.run();
                }

                String message;
                while (running && (message = in.readLine()) != null) {
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(message);
                    }
                }
            } catch (UnknownHostException e) {
                if (running && onError != null) {
                    onError.accept("Unbekannter Host: " + host);
                }
            } catch (ConnectException e) {
                if (running && onError != null) {
                    onError.accept("Verbindung zu " + host + ":" + port + " abgelehnt.");
                }
            } catch (IOException e) {
                if (running && onError != null) {
                    onError.accept("Verbindungsfehler: " + e.getMessage());
                }
            }
        });
        clientThread.setDaemon(true);
        clientThread.start();
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void stop() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return running;
    }
}

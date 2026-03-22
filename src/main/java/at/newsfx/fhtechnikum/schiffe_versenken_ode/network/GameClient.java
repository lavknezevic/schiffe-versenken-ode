package at.newsfx.fhtechnikum.schiffe_versenken_ode.network;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class GameClient {

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> onMessageReceived;
    private Consumer<String> onError;
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

    public void connect() {
        running = true;
        Thread clientThread = new Thread(() -> {
            try {
                socket = new Socket(host, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                String message;
                while (running && (message = in.readLine()) != null) {
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(message);
                    }
                }
            } catch (IOException e) {
                if (running && onError != null) {
                    onError.accept(e.getMessage());
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

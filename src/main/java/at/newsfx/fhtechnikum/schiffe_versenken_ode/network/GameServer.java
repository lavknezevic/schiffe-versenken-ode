package at.newsfx.fhtechnikum.schiffe_versenken_ode.network;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class GameServer {

    private final int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> onMessageReceived;
    private Consumer<String> onError;
    private volatile boolean running;

    public GameServer(int port) {
        this.port = port;
        this.running = false;
    }

    public void setOnMessageReceived(Consumer<String> handler) {
        this.onMessageReceived = handler;
    }

    public void setOnError(Consumer<String> handler) {
        this.onError = handler;
    }

    public void start() {
        running = true;
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                clientSocket = serverSocket.accept();
                serverSocket.close();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);

                String message;
                while (running && (message = in.readLine()) != null) {
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(message);
                    }
                }
            } catch (BindException e) {
                if (running && onError != null) {
                    onError.accept("Port " + port + " bereits belegt: " + e.getMessage());
                }
            } catch (IOException e) {
                if (running && onError != null) {
                    onError.accept("Serverfehler: " + e.getMessage());
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
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
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return running;
    }
}

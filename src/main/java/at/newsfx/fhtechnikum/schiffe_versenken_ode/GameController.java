package at.newsfx.fhtechnikum.schiffe_versenken_ode;

import at.newsfx.fhtechnikum.schiffe_versenken_ode.exception.InvalidMessageException;
import at.newsfx.fhtechnikum.schiffe_versenken_ode.exception.InvalidPlacementException;
import at.newsfx.fhtechnikum.schiffe_versenken_ode.model.*;
import at.newsfx.fhtechnikum.schiffe_versenken_ode.network.GameClient;
import at.newsfx.fhtechnikum.schiffe_versenken_ode.network.GameServer;
import at.newsfx.fhtechnikum.schiffe_versenken_ode.util.ConfigLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

public class GameController {

    private static final String CLASS_EMPTY = "cell-empty";
    private static final String CLASS_SHIP = "cell-ship";
    private static final String CLASS_HIT = "cell-hit";
    private static final String CLASS_MISS = "cell-miss";
    private static final String CLASS_SUNK = "cell-sunk";

    private final Board myBoard;
    private final CellState[][] enemyView;
    private final Button[][] myGridButtons;
    private final Button[][] enemyGridButtons;

    private final ConfigLoader config;
    private final GameStats stats;

    private GameServer server;
    private GameClient client;
    private boolean isHost;
    private GameState gameState;

    private Label statusLabel;
    private Label statsLabel;
    private TextArea logArea;
    private Button hostButton;
    private Button joinButton;
    private Button randomButton;
    private Button clearButton;
    private Button readyButton;
    private Button restartButton;
    private Button acceptRestartButton;
    private TextField nameField;

    private String playerName;
    private String opponentName;
    private boolean restartRequested;

    public GameController() {
        myBoard = new Board();
        enemyView = new CellState[Board.SIZE][Board.SIZE];
        myGridButtons = new Button[Board.SIZE][Board.SIZE];
        enemyGridButtons = new Button[Board.SIZE][Board.SIZE];
        config = ConfigLoader.getInstance();
        stats = new GameStats();
        gameState = GameState.START;
        playerName = config.getPlayerName();

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                enemyView[r][c] = CellState.EMPTY;
            }
        }
    }

    public Parent buildUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox topBar = buildTopBar();
        root.setTop(topBar);

        HBox boards = buildBoards();
        root.setCenter(boards);

        VBox bottomBar = buildBottomBar();
        root.setBottom(bottomBar);

        updateStatus("Willkommen! Hoste ein Spiel oder trete einem bei.");
        updateStatsDisplay();

        root.getStylesheets().add(
                getClass().getResource("style.css").toExternalForm()
        );
        return root;
    }

    private VBox buildTopBar() {
        VBox top = new VBox(5);
        top.setPadding(new Insets(0, 0, 10, 0));

        statusLabel = new Label("Schiffe Versenken");
        statusLabel.getStyleClass().add("status-label");

        statsLabel = new Label();
        statsLabel.getStyleClass().add("stats-label");

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);

        nameField = new TextField(playerName);
        nameField.setPromptText("Spielername");
        nameField.setPrefWidth(120);

        hostButton = new Button("Spiel hosten");
        hostButton.setOnAction(e -> hostGame());

        joinButton = new Button("Spiel beitreten");
        joinButton.setOnAction(e -> joinGame());

        randomButton = new Button("Zufällig platzieren");
        randomButton.setOnAction(e -> randomPlacement());

        clearButton = new Button("Zurücksetzen");
        clearButton.setOnAction(e -> clearPlacement());

        readyButton = new Button("Bereit");
        readyButton.setOnAction(e -> setReady());
        readyButton.setDisable(true);

        restartButton = new Button("Neustart");
        restartButton.setOnAction(e -> requestRestart());
        restartButton.setDisable(true);

        acceptRestartButton = new Button("Rematch annehmen");
        acceptRestartButton.setOnAction(e -> acceptRestart());
        acceptRestartButton.setVisible(false);

        buttons.getChildren().addAll(nameField, hostButton, joinButton, randomButton, clearButton, readyButton, restartButton, acceptRestartButton);

        top.getChildren().addAll(statusLabel, statsLabel, buttons);
        return top;
    }

    private HBox buildBoards() {
        HBox boards = new HBox(30);
        boards.setAlignment(Pos.CENTER);

        VBox myBoardBox = new VBox(5);
        myBoardBox.setAlignment(Pos.CENTER);
        Label myLabel = new Label("Mein Feld");
        myLabel.getStyleClass().add("board-label");
        GridPane myGrid = createGrid(true);
        myBoardBox.getChildren().addAll(myLabel, myGrid);

        VBox enemyBoardBox = new VBox(5);
        enemyBoardBox.setAlignment(Pos.CENTER);
        Label enemyLabel = new Label("Gegner");
        enemyLabel.getStyleClass().add("board-label");
        GridPane enemyGrid = createGrid(false);
        enemyBoardBox.getChildren().addAll(enemyLabel, enemyGrid);

        boards.getChildren().addAll(myBoardBox, enemyBoardBox);
        return boards;
    }

    private GridPane createGrid(boolean isMyGrid) {
        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);

        for (int c = 0; c < Board.SIZE; c++) {
            Label colLabel = new Label(String.valueOf(c));
            colLabel.getStyleClass().add("grid-header");
            colLabel.setMinWidth(35);
            colLabel.setAlignment(Pos.CENTER);
            grid.add(colLabel, c + 1, 0);
        }
        for (int r = 0; r < Board.SIZE; r++) {
            Label rowLabel = new Label(String.valueOf((char) ('A' + r)));
            rowLabel.getStyleClass().add("grid-header");
            rowLabel.setMinHeight(35);
            rowLabel.setAlignment(Pos.CENTER);
            grid.add(rowLabel, 0, r + 1);
        }

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Button cell = new Button();
                cell.getStyleClass().addAll("cell", CLASS_EMPTY);
                final int row = r;
                final int col = c;

                if (isMyGrid) {
                    cell.setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.SECONDARY) {
                            togglePlacementDirection();
                        } else {
                            onMyGridClick(row, col);
                        }
                    });
                    myGridButtons[r][c] = cell;
                } else {
                    cell.setOnAction(e -> onEnemyGridClick(row, col));
                    enemyGridButtons[r][c] = cell;
                }
                grid.add(cell, c + 1, r + 1);
            }
        }
        return grid;
    }

    private VBox buildBottomBar() {
        VBox bottom = new VBox(5);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(120);
        logArea.setPromptText("Spielverlauf...");
        logArea.getStyleClass().add("log-area");
        bottom.getChildren().add(logArea);
        return bottom;
    }

    private void applyPlayerName() {
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            playerName = name;
        }
        nameField.setDisable(true);
        updateStatsDisplay();
    }

    private void hostGame() {
        applyPlayerName();
        gameState = GameState.CONNECTING;
        updateStatus("Warte auf Mitspieler auf Port " + config.getPort() + "...");
        hostButton.setDisable(true);
        joinButton.setDisable(true);
        isHost = true;

        server = new GameServer(config.getPort());
        server.setOnMessageReceived(msg -> Platform.runLater(() -> handleMessage(msg)));
        server.setOnError(err -> Platform.runLater(() -> {
            log("Verbindungsfehler: " + err);
            updateStatus("Verbindungsfehler: " + err);
            hostButton.setDisable(false);
            joinButton.setDisable(false);
            nameField.setDisable(false);
            gameState = GameState.START;
        }));
        server.start();
        log("Server gestartet auf Port " + config.getPort());
    }

    private void joinGame() {
        applyPlayerName();
        gameState = GameState.CONNECTING;
        updateStatus("Verbinde zu " + config.getHost() + ":" + config.getPort() + "...");
        hostButton.setDisable(true);
        joinButton.setDisable(true);
        isHost = false;

        client = new GameClient(config.getHost(), config.getPort());
        client.setOnMessageReceived(msg -> Platform.runLater(() -> handleMessage(msg)));
        client.setOnError(err -> Platform.runLater(() -> {
            log("Verbindungsfehler: " + err);
            updateStatus("Verbindungsfehler: " + err);
            hostButton.setDisable(false);
            joinButton.setDisable(false);
            nameField.setDisable(false);
            gameState = GameState.START;
        }));
        client.setOnConnected(() -> Platform.runLater(() -> sendMessage("CONNECT:" + playerName)));
        client.connect();
        log("Verbinde zu " + config.getHost() + ":" + config.getPort());
    }

    private void randomPlacement() {
        if (gameState != GameState.SETUP && gameState != GameState.CONNECTING && gameState != GameState.START) {
            return;
        }
        resetShipPlacement();
        myBoard.placeShipsRandomly();
        shipPlacementIndex = shipsToPlace.length;
        refreshMyGrid();
        readyButton.setDisable(gameState == GameState.START);
        log("Schiffe zufällig platziert.");
    }

    private void clearPlacement() {
        if (gameState != GameState.SETUP && gameState != GameState.CONNECTING && gameState != GameState.START) {
            return;
        }
        resetShipPlacement();
        myBoard.clear();
        refreshMyGrid();
        readyButton.setDisable(true);
        log("Spielfeld zurückgesetzt. Platziere: " + shipsToPlace[0].getName() +
                " (" + shipsToPlace[0].getLength() + " Felder)");
    }

    private void resetShipPlacement() {
        shipPlacementIndex = 0;
        shipsToPlace[0] = new Carrier();
        shipsToPlace[1] = new Battleship();
        shipsToPlace[2] = new Cruiser();
        shipsToPlace[3] = new Submarine();
        shipsToPlace[4] = new Destroyer();
    }

    private void setReady() {
        if (myBoard.getShipCount() < 5) {
            log("Bitte platziere zuerst alle 5 Schiffe!");
            return;
        }
        sendMessage("READY");
        readyButton.setDisable(true);
        randomButton.setDisable(true);
        clearButton.setDisable(true);
        gameState = GameState.WAITING_FOR_OPPONENT;
        updateStatus("Warte auf Gegner...");
        log("Bereit! Warte auf Gegner...");
    }

    private void onMyGridClick(int row, int col) {
        if (gameState == GameState.START || gameState == GameState.CONNECTING || gameState == GameState.SETUP) {
            manualPlacement(row, col);
        }
    }

    private Ship currentShipToPlace;
    private int shipPlacementIndex = 0;
    private boolean placingHorizontal = true;
    private final Ship[] shipsToPlace = {
            new Carrier(), new Battleship(), new Cruiser(), new Submarine(), new Destroyer()
    };

    private void togglePlacementDirection() {
        if (gameState == GameState.MY_TURN || gameState == GameState.ENEMY_TURN
                || gameState == GameState.GAME_OVER || gameState == GameState.WAITING_FOR_OPPONENT) {
            return;
        }
        placingHorizontal = !placingHorizontal;
        log("Ausrichtung: " + (placingHorizontal ? "Horizontal" : "Vertikal"));
    }

    private void manualPlacement(int row, int col) {
        if (shipPlacementIndex >= shipsToPlace.length) {
            log("Alle Schiffe bereits platziert. Klicke 'Zufällig platzieren' zum Zurücksetzen.");
            return;
        }
        currentShipToPlace = shipsToPlace[shipPlacementIndex];
        try {
            myBoard.placeShip(currentShipToPlace, row, col, placingHorizontal);
            refreshMyGrid();
            log(currentShipToPlace.getName() + " platziert bei " + (char) ('A' + row) + col);
            shipPlacementIndex++;
            if (shipPlacementIndex < shipsToPlace.length) {
                log("Platziere: " + shipsToPlace[shipPlacementIndex].getName() +
                        " (" + shipsToPlace[shipPlacementIndex].getLength() + " Felder) - Rechtsklick zum Drehen");
            } else {
                log("Alle Schiffe platziert! Klicke 'Bereit' wenn verbunden.");
                if (gameState == GameState.SETUP || gameState == GameState.CONNECTING) {
                    readyButton.setDisable(false);
                }
            }
        } catch (InvalidPlacementException e) {
            log(e.getMessage());
        }
    }

    private void onEnemyGridClick(int row, int col) {
        if (gameState != GameState.MY_TURN) {
            return;
        }
        if (enemyView[row][col] != CellState.EMPTY) {
            log("Feld bereits beschossen!");
            return;
        }
        sendMessage("SHOOT:" + row + "," + col);
        gameState = GameState.ENEMY_TURN;
        updateStatus("Gegner ist am Zug...");
    }

    private void handleMessage(String message) {
        if (message == null) return;

        if (message.startsWith("CONNECT:")) {
            String incomingName = message.substring(8);
            if (opponentName == null) {
                opponentName = incomingName;
                log(opponentName + " hat sich verbunden!");
                sendMessage("CONNECT:" + playerName);
            }
            gameState = GameState.SETUP;
            updateStatus("Verbunden mit " + opponentName + ". Platziere deine Schiffe!");
            readyButton.setDisable(myBoard.getShipCount() < 5);
            if (shipPlacementIndex == 0) {
                log("Platziere: " + shipsToPlace[0].getName() +
                        " (" + shipsToPlace[0].getLength() + " Felder) - Rechtsklick zum Drehen");
            }
        } else if (message.equals("READY")) {
            log(opponentName + " ist bereit!");
            if (gameState == GameState.WAITING_FOR_OPPONENT) {
                startGame();
                sendMessage("START");
            } else {
                gameState = GameState.SETUP;
                updateStatus(opponentName + " ist bereit. Platziere deine Schiffe und klicke 'Bereit'.");
            }
        } else if (message.equals("START")) {
            startGame();
        } else if (message.startsWith("SHOOT:")) {
            handleIncomingShot(message);
        } else if (message.startsWith("RESULT:")) {
            handleShotResult(message);
        } else if (message.equals("GAMEOVER:LOSE")) {
            gameState = GameState.GAME_OVER;
            stats.recordWin(playerName);
            updateStatus("Du hast gewonnen!");
            log("Alle gegnerischen Schiffe versenkt! Sieg!");
            updateStatsDisplay();
            restartButton.setDisable(false);
        } else if (message.equals("RESTART_REQUEST")) {
            if (restartRequested) {
                sendMessage("RESTART_ACCEPT");
                log("Beide wollen Rematch!");
                performRestart();
            } else {
                log(opponentName + " möchte ein Rematch!");
                updateStatus(opponentName + " möchte ein Rematch!");
                acceptRestartButton.setVisible(true);
            }
        } else if (message.equals("RESTART_ACCEPT")) {
            log(opponentName + " hat das Rematch angenommen!");
            performRestart();
        }
    }

    private void startGame() {
        if (isHost) {
            gameState = GameState.MY_TURN;
            updateStatus("Spiel gestartet! Du bist am Zug.");
        } else {
            gameState = GameState.ENEMY_TURN;
            updateStatus("Spiel gestartet! " + opponentName + " beginnt.");
        }
        log("Spiel gestartet!");
    }

    private int[] parseCoordinates(String coordString) throws InvalidMessageException {
        String[] parts = coordString.split(",");
        if (parts.length < 2) {
            throw new InvalidMessageException("Koordinaten unvollstaendig: " + coordString);
        }
        try {
            int row = Integer.parseInt(parts[0].trim());
            int col = Integer.parseInt(parts[1].trim());
            return new int[]{row, col};
        } catch (NumberFormatException e) {
            throw new InvalidMessageException("Ungueltige Koordinaten: " + coordString);
        }
    }

    private void handleIncomingShot(String message) {
        try {
            if (!message.startsWith("SHOOT:") || message.length() <= 6) {
                throw new InvalidMessageException("Ungueltiges SHOOT-Format: " + message);
            }
            int[] coords = parseCoordinates(message.substring(6));
            int row = coords[0];
            int col = coords[1];

            ShotResult result = myBoard.shootAt(row, col);
            refreshMyGrid();

            String response = "RESULT:" + row + "," + col + ":";
            if (result.isSunk()) {
                Ship sunkShip = myBoard.getShipAt(row, col);
                response += "SUNK:" + result.getShipName()
                        + ":" + sunkShip.getStartRow() + "," + sunkShip.getStartCol()
                        + "," + (sunkShip.isHorizontal() ? "H" : "V") + "," + sunkShip.getLength();
                log(opponentName + " hat " + result.getShipName() + " versenkt bei " + (char) ('A' + row) + col + "!");
            } else if (result.isHit()) {
                response += "HIT";
                log(opponentName + " hat getroffen bei " + (char) ('A' + row) + col + "!");
            } else {
                response += "MISS";
                log(opponentName + " daneben bei " + (char) ('A' + row) + col + ".");
            }
            sendMessage(response);

            if (myBoard.allShipsSunk()) {
                gameState = GameState.GAME_OVER;
                sendMessage("GAMEOVER:LOSE");
                stats.recordLoss(playerName);
                updateStatus("Alle Schiffe versenkt. Du hast verloren!");
                log("Alle deine Schiffe wurden versenkt. Niederlage!");
                updateStatsDisplay();
                restartButton.setDisable(false);
            } else {
                gameState = GameState.MY_TURN;
                updateStatus("Du bist am Zug!");
            }
        } catch (InvalidMessageException e) {
            log("Ungueltige Nachricht: " + e.getMessage());
            gameState = GameState.MY_TURN;
            updateStatus("Du bist am Zug!");
        } catch (ArrayIndexOutOfBoundsException e) {
            log("Fehler: Koordinaten ausserhalb des Spielfelds.");
            gameState = GameState.MY_TURN;
            updateStatus("Du bist am Zug!");
        }
    }

    private void handleShotResult(String message) {
        try {
            String payload = message.substring(7);
            String[] parts = payload.split(":");
            if (parts.length < 2) {
                throw new InvalidMessageException("RESULT-Nachricht unvollstaendig: " + message);
            }
            int[] coords = parseCoordinates(parts[0]);
            int row = coords[0];
            int col = coords[1];
            String result = parts[1];

            if (result.equals("HIT")) {
                enemyView[row][col] = CellState.HIT;
                setCellClass(enemyGridButtons[row][col], CLASS_HIT);
                log("Treffer bei " + (char) ('A' + row) + col + "!");
            } else if (result.equals("SUNK")) {
                String shipName = parts.length > 2 ? parts[2] : "Schiff";
                if (parts.length > 3) {
                    String[] shipInfo = parts[3].split(",");
                    int sr = Integer.parseInt(shipInfo[0]);
                    int sc = Integer.parseInt(shipInfo[1]);
                    boolean horiz = shipInfo[2].equals("H");
                    int len = Integer.parseInt(shipInfo[3]);
                    for (int i = 0; i < len; i++) {
                        int cr = horiz ? sr : sr + i;
                        int cc = horiz ? sc + i : sc;
                        enemyView[cr][cc] = CellState.HIT;
                        setCellClass(enemyGridButtons[cr][cc], CLASS_SUNK);
                    }
                } else {
                    enemyView[row][col] = CellState.HIT;
                    setCellClass(enemyGridButtons[row][col], CLASS_SUNK);
                }
                log(shipName + " versenkt bei " + (char) ('A' + row) + col + "!");
            } else {
                enemyView[row][col] = CellState.MISS;
                setCellClass(enemyGridButtons[row][col], CLASS_MISS);
                log("Daneben bei " + (char) ('A' + row) + col + ".");
            }
        } catch (InvalidMessageException e) {
            log("Ungueltige Nachricht: " + e.getMessage());
            gameState = GameState.MY_TURN;
            updateStatus("Du bist am Zug!");
        } catch (NumberFormatException e) {
            log("Fehler beim Parsen der Schiffsdaten: " + e.getMessage());
            gameState = GameState.MY_TURN;
            updateStatus("Du bist am Zug!");
        } catch (ArrayIndexOutOfBoundsException e) {
            log("Fehler: Koordinaten ausserhalb des Spielfelds.");
            gameState = GameState.MY_TURN;
            updateStatus("Du bist am Zug!");
        }
    }

    private void requestRestart() {
        restartRequested = true;
        sendMessage("RESTART_REQUEST");
        restartButton.setDisable(true);
        updateStatus("Rematch angefragt, warte auf " + opponentName + "...");
        log("Rematch angefragt...");
    }

    private void acceptRestart() {
        sendMessage("RESTART_ACCEPT");
        acceptRestartButton.setVisible(false);
        performRestart();
    }

    private void performRestart() {
        restartRequested = false;
        myBoard.clear();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                enemyView[r][c] = CellState.EMPTY;
                setCellClass(enemyGridButtons[r][c], CLASS_EMPTY);
            }
        }
        shipPlacementIndex = 0;
        shipsToPlace[0] = new Carrier();
        shipsToPlace[1] = new Battleship();
        shipsToPlace[2] = new Cruiser();
        shipsToPlace[3] = new Submarine();
        shipsToPlace[4] = new Destroyer();

        refreshMyGrid();
        gameState = GameState.SETUP;
        readyButton.setDisable(true);
        randomButton.setDisable(false);
        clearButton.setDisable(false);
        restartButton.setDisable(true);
        acceptRestartButton.setVisible(false);
        updateStatus("Neues Spiel! Platziere deine Schiffe.");
        log("--- Neues Spiel ---");
        log("Platziere: " + shipsToPlace[0].getName() +
                " (" + shipsToPlace[0].getLength() + " Felder) - Rechtsklick zum Drehen");
    }

    private void refreshMyGrid() {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                CellState state = myBoard.getCell(r, c);
                switch (state) {
                    case EMPTY -> setCellClass(myGridButtons[r][c], CLASS_EMPTY);
                    case SHIP -> setCellClass(myGridButtons[r][c], CLASS_SHIP);
                    case HIT -> setCellClass(myGridButtons[r][c],
                            myBoard.isShipSunkAt(r, c) ? CLASS_SUNK : CLASS_HIT);
                    case MISS -> setCellClass(myGridButtons[r][c], CLASS_MISS);
                }
            }
        }
    }

    private void setCellClass(Button cell, String cssClass) {
        cell.getStyleClass().removeAll(CLASS_EMPTY, CLASS_SHIP, CLASS_HIT, CLASS_MISS, CLASS_SUNK);
        cell.getStyleClass().add(cssClass);
    }

    private void sendMessage(String message) {
        if (isHost && server != null) {
            server.send(message);
        } else if (client != null) {
            client.send(message);
        }
    }

    private void updateStatus(String text) {
        statusLabel.setText(text);
    }

    private void updateStatsDisplay() {
        statsLabel.setText(stats.getAllStatsDisplay());
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    public void shutdown() {
        if (server != null) server.stop();
        if (client != null) client.stop();
    }
}

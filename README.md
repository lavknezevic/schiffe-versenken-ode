# Schiffe Versenken

Ein Netzwerk-Multiplayer Schiffe Versenken Spiel, implementiert in JavaFX.

## Voraussetzungen

- Java 21
- Maven (oder den mitgelieferten Maven Wrapper `mvnw` verwenden)

## Spiel starten

```bash
./mvnw clean javafx:run
```

Unter Windows:
```bash
mvnw.cmd clean javafx:run
```

## Konfiguration

Die Verbindungseinstellungen werden in der Datei `config.properties` im Projektverzeichnis gespeichert.
Beim ersten Start wird eine Standardkonfiguration erstellt:

```properties
host=localhost
port=5000
playerName=Player
```

- `host`: IP-Adresse des Gegenspielers (nur fuer den beitretenden Spieler relevant)
- `port`: Port fuer die Verbindung
- `playerName`: Anzeigename des Spielers

## Spielablauf

1. **Verbindung herstellen**: Ein Spieler klickt "Spiel hosten", der andere "Spiel beitreten"
2. **Schiffe platzieren**: Klicke auf das eigene Feld um Schiffe zu platzieren. Rechtsklick dreht die Ausrichtung (horizontal/vertikal). Alternativ "Zufaellig platzieren" fuer eine zufaellige Platzierung.
3. **Bereit**: Wenn alle 5 Schiffe platziert sind, klicke "Bereit"
4. **Spielen**: Abwechselnd auf das gegnerische Feld klicken um Schuesse abzufeuern
5. **Gewinner**: Wer zuerst alle gegnerischen Schiffe versenkt, gewinnt

## Schiffe

| Schiff     | Laenge |
|------------|--------|
| Carrier    | 5      |
| Battleship | 4      |
| Cruiser    | 3      |
| Submarine  | 3      |
| Destroyer  | 2      |

## Farblegende

- **Blau**: Leeres Feld
- **Grau**: Eigenes Schiff
- **Rot**: Treffer
- **Weiss**: Daneben
- **Dunkelrot**: Versenkt

## Spielstatistiken

Siege, Niederlagen und Unentschieden werden in `stats.csv` gespeichert und beim Start geladen.

## Netzwerkprotokoll

Die Kommunikation erfolgt ueber TCP Sockets mit einem einfachen textbasierten Protokoll.

## Projektstruktur

```
src/main/java/at/newsfx/fhtechnikum/schiffe_versenken_ode/
├── GameApplication.java     - JavaFX Application
├── GameController.java      - Spielsteuerung und GUI
├── Launcher.java            - Einstiegspunkt
├── exception/
│   ├── ConnectionException.java      - Verbindungsfehler
│   ├── InvalidMessageException.java  - Ungueltige Netzwerknachricht
│   └── InvalidPlacementException.java - Ungueltige Schiffplatzierung
├── model/
│   ├── Board.java           - Spielfeld (10x10)
│   ├── CellState.java       - Zellzustaende
│   ├── GameState.java       - Spielzustaende
│   ├── GameStats.java       - Statistiken (File IO)
│   ├── Ship.java            - Abstrakte Schiff-Klasse
│   ├── Carrier.java         - Flugzeugtraeger (5)
│   ├── Battleship.java      - Schlachtschiff (4)
│   ├── Cruiser.java         - Kreuzer (3)
│   ├── Submarine.java       - U-Boot (3)
│   ├── Destroyer.java       - Zerstoerer (2)
│   └── ShotResult.java      - Schussergebnis
├── network/
│   ├── GameServer.java      - TCP Server
│   └── GameClient.java      - TCP Client
└── util/
    └── ConfigLoader.java    - Konfiguration aus Properties (Singleton)
```

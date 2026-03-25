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

- `host`: IP-Adresse des Gegenspielers (nur für den beitretenden Spieler relevant)
- `port`: Port für die Verbindung
- `playerName`: Anzeigename des Spielers

## Implementierte Features

### Grundfunktionen (Genügend)
- GUI für das Spiel Schiffe Versenken mit JavaFX
- Schiffe manuell auf dem Spielfeld platzieren (Rechtsklick zum Drehen)
- Abwechselndes Raten der Positionen gegen einen Mitspieler
- Gewinner wird automatisch ermittelt wenn alle Schiffe versenkt sind
- Neustart/Rematch nach Spielende möglich
- 2 Spieler über Netzwerk (TCP Sockets)
- Verbindungskonfiguration (IP-Adresse, Port) über `config.properties`

### Erweiterte Features (Befriedigend)
- Spielstatistiken: Siege, Niederlagen und Unentschieden werden berechnet und angezeigt
- Spielernamen werden gespeichert und in der Statistik geführt (`stats.csv`)
- Zufällige Platzierung aller Schiffe für schnelleren Spielstart

### Technische Umsetzung
- Vererbung und abstrakte Klassen (Ship-Hierarchie mit 5 Schifftypen)
- Eigene Exceptions (InvalidPlacementException, InvalidMessageException, ConnectionException)
- Exception Handling mit spezifischen Catches, throws-Deklarationen und Multicatch
- File IO mit try-with-resources (CSV für Statistiken, Properties für Konfiguration)
- Multithreading: Netzwerk-IO läuft in eigenen Threads, GUI bleibt responsiv (Platform.runLater)
- Singleton Pattern für ConfigLoader
- Saubere Zugriffsrechte (private Felder, Getter/Setter, protected Konstruktoren)

## Spielablauf

1. **Verbindung herstellen**: Ein Spieler klickt "Spiel hosten", der andere "Spiel beitreten"
2. **Schiffe platzieren**: Klicke auf das eigene Feld um Schiffe zu platzieren. Rechtsklick dreht die Ausrichtung (horizontal/vertikal). Alternativ "Zufällig platzieren" für eine zufällige Platzierung.
3. **Bereit**: Wenn alle 5 Schiffe platziert sind, klicke "Bereit"
4. **Spielen**: Abwechselnd auf das gegnerische Feld klicken um Schüsse abzufeuern
5. **Gewinner**: Wer zuerst alle gegnerischen Schiffe versenkt, gewinnt

## Schiffe

| Schiff     | Länge |
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
- **Weiß**: Daneben
- **Dunkelrot**: Versenkt

## Spielstatistiken

Siege, Niederlagen und Unentschieden werden in `stats.csv` gespeichert und beim Start geladen.

## Netzwerkprotokoll

Die Kommunikation erfolgt über TCP Sockets mit einem einfachen textbasierten Protokoll.

## Projektstruktur

```
src/main/java/at/newsfx/fhtechnikum/schiffe_versenken_ode/
├── GameApplication.java     - JavaFX Application
├── GameController.java      - Spielsteuerung und GUI
├── Launcher.java            - Einstiegspunkt
├── exception/
│   ├── ConnectionException.java      - Verbindungsfehler
│   ├── InvalidMessageException.java  - Ungültige Netzwerknachricht
│   └── InvalidPlacementException.java - Ungültige Schiffplatzierung
├── model/
│   ├── Board.java           - Spielfeld (10x10)
│   ├── CellState.java       - Zellzustände
│   ├── GameState.java       - Spielzustände
│   ├── GameStats.java       - Statistiken (File IO)
│   ├── Ship.java            - Abstrakte Schiff-Klasse
│   ├── Carrier.java         - Flugzeugträger (5)
│   ├── Battleship.java      - Schlachtschiff (4)
│   ├── Cruiser.java         - Kreuzer (3)
│   ├── Submarine.java       - U-Boot (3)
│   ├── Destroyer.java       - Zerstörer (2)
│   └── ShotResult.java      - Schussergebnis
├── network/
│   ├── GameServer.java      - TCP Server
│   └── GameClient.java      - TCP Client
└── util/
    └── ConfigLoader.java    - Konfiguration aus Properties (Singleton)
```

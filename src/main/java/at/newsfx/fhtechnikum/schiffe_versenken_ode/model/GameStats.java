package at.newsfx.fhtechnikum.schiffe_versenken_ode.model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameStats {

    private static final String STATS_FILE = "stats.csv";
    private static final Logger LOGGER = Logger.getLogger(GameStats.class.getName());

    private final Map<String, int[]> playerStats;

    public GameStats() {
        playerStats = new HashMap<>();
        load();
    }

    public void recordWin(String playerName) {
        load();
        int[] stats = getOrCreate(playerName);
        stats[0]++;
        save();
    }

    public void recordLoss(String playerName) {
        load();
        int[] stats = getOrCreate(playerName);
        stats[1]++;
        save();
    }

    public void recordDraw(String playerName) {
        load();
        int[] stats = getOrCreate(playerName);
        stats[2]++;
        save();
    }

    public int getWins(String playerName) {
        int[] stats = playerStats.get(playerName);
        return stats != null ? stats[0] : 0;
    }

    public int getLosses(String playerName) {
        int[] stats = playerStats.get(playerName);
        return stats != null ? stats[1] : 0;
    }

    public int getDraws(String playerName) {
        int[] stats = playerStats.get(playerName);
        return stats != null ? stats[2] : 0;
    }

    public String getStatsDisplay(String playerName) {
        int w = getWins(playerName);
        int l = getLosses(playerName);
        int d = getDraws(playerName);
        return playerName + " - W: " + w + " | L: " + l + " | D: " + d;
    }

    public String getAllStatsDisplay() {
        if (playerStats.isEmpty()) {
            return "Keine Statistiken vorhanden.";
        }
        StringBuilder sb = new StringBuilder("Statistiken: ");
        boolean first = true;
        for (Map.Entry<String, int[]> entry : playerStats.entrySet()) {
            if (!first) {
                sb.append("  |  ");
            }
            int[] s = entry.getValue();
            sb.append(entry.getKey()).append(" W:").append(s[0])
              .append(" L:").append(s[1]).append(" D:").append(s[2]);
            first = false;
        }
        return sb.toString();
    }

    private int[] getOrCreate(String playerName) {
        return playerStats.computeIfAbsent(playerName, k -> new int[]{0, 0, 0});
    }

    private void load() {
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    String name = parts[0].trim();
                    int wins = Integer.parseInt(parts[1].trim());
                    int losses = Integer.parseInt(parts[2].trim());
                    int draws = Integer.parseInt(parts[3].trim());
                    playerStats.put(name, new int[]{wins, losses, draws});
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "Statistikdatei nicht gefunden: {0}", STATS_FILE);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Ungueltige Zahl in Statistikdatei: {0}", e.getMessage());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Laden der Statistiken: {0}", e.getMessage());
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STATS_FILE))) {
            for (Map.Entry<String, int[]> entry : playerStats.entrySet()) {
                int[] s = entry.getValue();
                writer.write(entry.getKey() + ";" + s[0] + ";" + s[1] + ";" + s[2]);
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Speichern der Statistiken: {0}", e.getMessage());
        }
    }
}

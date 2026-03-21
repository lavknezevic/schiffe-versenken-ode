package at.newsfx.fhtechnikum.schiffe_versenken_ode.util;

import java.io.*;
import java.util.Properties;

public class ConfigLoader {

    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;
    private static final String DEFAULT_PLAYER_NAME = "Player";

    private final Properties properties;

    public ConfigLoader() {
        properties = new Properties();
        load();
    }

    public ConfigLoader(String filePath) {
        properties = new Properties();
        load(filePath);
    }

    private void load() {
        load(CONFIG_FILE);
    }

    private void load(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            createDefault(filePath);
        }
        try (FileReader reader = new FileReader(filePath)) {
            properties.load(reader);
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    private void createDefault(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            Properties defaults = new Properties();
            defaults.setProperty("host", DEFAULT_HOST);
            defaults.setProperty("port", String.valueOf(DEFAULT_PORT));
            defaults.setProperty("playerName", DEFAULT_PLAYER_NAME);
            defaults.store(writer, "Schiffe Versenken Configuration");
        } catch (IOException e) {
            System.err.println("Failed to create default config: " + e.getMessage());
        }
    }

    public String getHost() {
        return properties.getProperty("host", DEFAULT_HOST);
    }

    public int getPort() {
        try {
            return Integer.parseInt(properties.getProperty("port", String.valueOf(DEFAULT_PORT)));
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }

    public String getPlayerName() {
        return properties.getProperty("playerName", DEFAULT_PLAYER_NAME);
    }
}

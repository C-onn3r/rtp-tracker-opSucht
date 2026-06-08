package net.opsucht.RtpTracker;

import net.minecraft.client.MinecraftClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RtpDataManager {
    public static class DayData {
        public int count = 0;
        public double wage = 0.0;
        public int playtimeMinutes = 0;
    }

    public static final Map<String, DayData> rtpHistory = new LinkedHashMap<>();
    public static final int COST_PER_RTP = 25000;
    private static final DateTimeFormatter GERMAN_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static Path saveFile;
    private static boolean timerStarted = false;

    // NEU: Globale Variablen für Rekorde und Spam-Schutz
    public static double highestPayout = 0.0;
    public static boolean debugMode = false; // Standardmäßig AUS (kein Spam!)

    public static void loadHistory() {
        saveFile = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/rtp_tracker_history.txt");
        try {
            if (Files.exists(saveFile)) {
                List<String> lines = Files.readAllLines(saveFile);
                rtpHistory.clear();
                for (String line : lines) {
                    if (line.startsWith("META,")) {
                        String[] parts = line.split(",");
                        if (parts.length > 1) highestPayout = Double.parseDouble(parts[1]);
                        if (parts.length > 2) debugMode = Boolean.parseBoolean(parts[2]);
                        continue;
                    }
                    if (line.contains(",")) {
                        String[] parts = line.split(",");
                        DayData data = new DayData();
                        data.count = Integer.parseInt(parts[1]);
                        if (parts.length > 2) data.wage = Double.parseDouble(parts[2]);
                        if (parts.length > 3) data.playtimeMinutes = Integer.parseInt(parts[3]);
                        rtpHistory.put(parts[0], data);
                    }
                }
            }
            startPlaytimeTimer();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void startPlaytimeTimer() {
        if (timerStarted) return;
        timerStarted = true;
        Thread timer = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000);
                    String heute = LocalDate.now().format(GERMAN_DATE);
                    DayData data = rtpHistory.computeIfAbsent(heute, k -> new DayData());
                    data.playtimeMinutes++;
                    saveHistory();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
        timer.setDaemon(true);
        timer.setName("RTP-Tracker-Playtime");
        timer.start();
    }

    public static void checkHighestPayout(double amount) {
        if (amount > highestPayout) {
            highestPayout = amount;
            saveHistory();
        }
    }

    public static void addRtpPosition() {
        String date = LocalDate.now().format(GERMAN_DATE);
        DayData data = rtpHistory.computeIfAbsent(date, k -> new DayData());
        data.count++;
        saveHistory();
    }

    public static void addJobWage(String date, double amount) {
        DayData data = rtpHistory.computeIfAbsent(date, k -> new DayData());
        data.wage += amount;
        saveHistory();
    }

    public static void saveHistory() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("META,").append(highestPayout).append(",").append(debugMode).append("\n");
            for (Map.Entry<String, DayData> entry : rtpHistory.entrySet()) {
                sb.append(entry.getKey()).append(",")
                  .append(entry.getValue().count).append(",")
                  .append(entry.getValue().wage).append(",")
                  .append(entry.getValue().playtimeMinutes).append("\n");
            }
            if (!Files.exists(saveFile.getParent())) Files.createDirectories(saveFile.getParent());
            Files.writeString(saveFile, sb.toString());
        } catch (IOException e) { e.printStackTrace(); }
    }
}

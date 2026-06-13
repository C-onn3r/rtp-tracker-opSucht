package net.opsucht.RtpTracker;

import net.minecraft.client.MinecraftClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RtpDataManager {
    public static class DayData {
        public int count = 0;
        public double wage = 0.0;
        public double xp = 0.0; // NEU: Speichert die Tages-XP
    }

    public static final Map<String, DayData> rtpHistory = new LinkedHashMap<>();
    public static final int COST_PER_RTP = 25000;
    private static final DateTimeFormatter GERMAN_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static Path saveFile;

    public static double highestPayout = 0.0;
    public static boolean debugMode = false;

    public static double livePayoutSum = 0.0;
    public static int livePayoutCount = 0;

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
                        if (parts.length > 3) livePayoutSum = Double.parseDouble(parts[3]);
                        if (parts.length > 4) livePayoutCount = Integer.parseInt(parts[4]);
                        continue;
                    }
                    if (line.contains(",")) {
                        String[] parts = line.split(",");
                        DayData data = new DayData();
                        data.count = Integer.parseInt(parts[1]);
                        if (parts.length > 2) data.wage = Double.parseDouble(parts[2]);
                        if (parts.length > 3) data.xp = Double.parseDouble(parts[3]); // NEU: Lädt XP
                        rtpHistory.put(parts[0], data);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
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

    // Präzise über Actionbar aufgerufen (Geld)
    public static void addJobWage(String date, double amount) {
        DayData data = rtpHistory.computeIfAbsent(date, k -> new DayData());
        data.wage += amount;
        saveHistory();
    }

    // NEU: Fügt die XP zum aktuellen Tag hinzu
    public static void addJobXp(String date, double amount) {
        DayData data = rtpHistory.computeIfAbsent(date, k -> new DayData());
        data.xp += amount;
        saveHistory();
    }

    public static void processChatPayoutStats(String date, double amount) {
        checkHighestPayout(amount);
        logSinglePayout(date, amount);
        
        if (amount > 8500.0) {
            livePayoutSum += amount;
            livePayoutCount++;
        }
        
        saveHistory();
    }

    private static void logSinglePayout(String date, double amount) {
        try {
            Path logFile = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/rtp_all_payouts.txt");
            if (!Files.exists(logFile.getParent())) {
                Files.createDirectories(logFile.getParent());
            }
            String entry = date + "," + amount + "\n";
            Files.writeString(logFile, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void saveHistory() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("META,").append(highestPayout).append(",").append(debugMode).append(",")
              .append(livePayoutSum).append(",").append(livePayoutCount).append("\n");
              
            for (Map.Entry<String, DayData> entry : rtpHistory.entrySet()) {
                sb.append(entry.getKey()).append(",")
                  .append(entry.getValue().count).append(",")
                  .append(entry.getValue().wage).append(",")
                  .append(entry.getValue().xp).append("\n"); // NEU: Speichert XP ab
            }
            if (!Files.exists(saveFile.getParent())) Files.createDirectories(saveFile.getParent());
            Files.writeString(saveFile, sb.toString());
        } catch (IOException e) { e.printStackTrace(); }
    }
}
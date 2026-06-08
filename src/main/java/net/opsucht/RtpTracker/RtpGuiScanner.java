package net.opsucht.RtpTracker;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.text.Text;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

public class RtpGuiScanner {
    public static boolean isScanning = false;
    private static final HashSet<String> processedTimestamps = new HashSet<>();
    private static Path scanLogFile;

    public static void register() {
        scanLogFile = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/rtp_tracker_scanned.txt");
        try {
            if (Files.exists(scanLogFile)) {
                // KORRIGIERT: Keine Fantasie-Variablen mehr, lädt jetzt einfach sauber scanLogFile
                processedTimestamps.addAll(Files.readAllLines(scanLogFile));
            }
        } catch (Exception e) { e.printStackTrace(); }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isScanning || client.currentScreen == null || client.player == null) return;

            if (client.currentScreen instanceof GenericContainerScreen) {
                GenericContainerScreen screen = (GenericContainerScreen) client.currentScreen;
                var handler = screen.getScreenHandler();
                int neuErfasst = 0;

                for (int i = 0; i < handler.slots.size(); i++) {
                    ItemStack stack = handler.getSlot(i).getStack();
                    if (stack.isEmpty()) continue;

                    try {
                        // KORRIGIERT: Erstellt den perfekten TooltipContext aus den Registry-Daten des Spielers
                        TooltipContext context = TooltipContext.create(client.player.getRegistryManager());
                        
                        // Übergibt exakt die 3 geforderten Argumente: Context, Player, TooltipType
                        List<Text> tooltip = stack.getTooltip(
                            context, 
                            client.player, 
                            net.minecraft.item.tooltip.TooltipType.BASIC
                        );
                        
                        if (tooltip.size() < 3) continue;

                        String nameLine = tooltip.get(0).getString().trim(); 
                        String timeLine = tooltip.get(1).getString().trim(); 
                        String descLine = tooltip.get(2).getString().trim(); 

                        if (!timeLine.contains(":") || timeLine.length() < 19) continue;
                        if (processedTimestamps.contains(timeLine)) continue;

                        String[] dateTime = timeLine.split(" ");
                        if (dateTime.length < 2) continue;
                        String datePart = dateTime[0]; 
                        String timePart = dateTime[1]; 

                        String[] dSplit = datePart.split("\\.");
                        if (dSplit.length == 3) {
                            int tag = Integer.parseInt(dSplit[0]);
                            int monat = Integer.parseInt(dSplit[1]);
                            int jahr = Integer.parseInt(dSplit[2]);

                            if (jahr > 2026) continue;
                            if (jahr == 2026) {
                                if (monat > 6) continue;
                                if (monat == 6) {
                                    if (tag > 6) continue;
                                    if (tag == 6) {
                                        int stunde = Integer.parseInt(timePart.split(":")[0]);
                                        if (stunde >= 13) continue; 
                                    }
                                }
                            }
                        }

                        boolean erfolg = false;
                        if (descLine.contains("Random-Teleport")) {
                            RtpDataManager.DayData data = RtpDataManager.rtpHistory.computeIfAbsent(datePart, k -> new RtpDataManager.DayData());
                            data.count++;
                            processedTimestamps.add(timeLine);
                            erfolg = true;
                        } else if (descLine.contains("Jobvergütung")) {
                            String cleanWage = nameLine.replace("$", "").replace(".", "").replace(",", ".").trim();
                            double wage = Double.parseDouble(cleanWage);

                            RtpDataManager.DayData data = RtpDataManager.rtpHistory.get(datePart);
                            if (data == null) {
                                data = new RtpDataManager.DayData();
                                RtpDataManager.rtpHistory.put(datePart, data);
                            }
                            data.wage += wage;
                            processedTimestamps.add(timeLine);
                            erfolg = true;
                        }

                        if (erfolg) neuErfasst++;

                    } catch (Exception e) { /* Ignorieren bei Lesefehlern */ }
                }

                if (neuErfasst > 0) {
                    RtpDataManager.saveHistory();
                    saveScanLog();
                    client.player.sendMessage(Text.literal("§6[Tracker] §a" + neuErfasst + " historische Transaktionen aufgesaugt!"), false);
                }
            }
        });
    }

    private static void saveScanLog() {
        try {
            Files.write(scanLogFile, processedTimestamps);
        } catch (IOException e) { e.printStackTrace(); }
    }
}

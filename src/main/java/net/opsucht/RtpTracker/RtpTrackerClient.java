package net.opsucht.RtpTracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RtpTrackerClient implements ClientModInitializer {
    private static final DateTimeFormatter GERMAN_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void onInitializeClient() {
        RtpDataManager.loadHistory();
        
        // Aktiviert das neue Scanner-Addon
        RtpGuiScanner.register();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String cleanText = message.getString();
            
            if (cleanText.contains("zur Warteschlange hinzugefügt") && cleanText.contains("Du wurdest für das Biom")) {
                RtpDataManager.addRtpPosition();
                if (RtpDataManager.debugMode) sendMessage("§6[Tracker] §aRTP erfasst! (/rtpmenu)");
            }
            
            if (cleanText.contains("Du hast deinen Lohn von") && cleanText.contains("erhalten!")) {
                try {
                    int startIdx = cleanText.indexOf("Lohn von ") + 9;
                    int endIdx = cleanText.indexOf("$ erhalten");
                    if (startIdx > 8 && endIdx > startIdx) {
                        String wageStr = cleanText.substring(startIdx, endIdx).trim();
                        wageStr = wageStr.replace(".", "").replace(",", ".");
                        double wage = Double.parseDouble(wageStr);
                        
                        RtpDataManager.checkHighestPayout(wage);
                        
                        String heute = LocalDate.now().format(GERMAN_DATE);
                        RtpDataManager.addJobWage(heute, wage);
                        
                        if (RtpDataManager.debugMode) {
                            sendMessage("§6[Tracker] §eLohn von +" + String.format("%,.2f", wage) + "$ verbucht!");
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("rtpmenu")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.execute(() -> client.setScreen(new RtpTrackerScreen()));
                    return 1;
                })
                .then(ClientCommandManager.literal("debug").executes(context -> {
                    RtpDataManager.debugMode = !RtpDataManager.debugMode;
                    RtpDataManager.saveHistory();
                    sendMessage("§6[Tracker] §eChat-Benachrichtigungen: " + (RtpDataManager.debugMode ? "§aAN" : "§cAUS"));
                    return 1;
                }))
                // NEU: Der Befehl zum Aktivieren des Staubsaugers
                .then(ClientCommandManager.literal("nachtragen").executes(context -> {
                    RtpGuiScanner.isScanning = !RtpGuiScanner.isScanning;
                    sendMessage("§6[Tracker] §eScanner-Modus: " + (RtpGuiScanner.isScanning ? "§aAN §7(Öffne jetzt dein Transaktions-GUI)" : "§cAUS"));
                    return 1;
                }))
            );
        });
    }

    private void sendMessage(String text) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal(text), false);
        }
    }
}

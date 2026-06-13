package net.opsucht.RtpTracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RtpTrackerClient implements ClientModInitializer {
    private static final DateTimeFormatter GERMAN_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Pattern ACTIONBAR_MONEY_PATTERN = Pattern.compile("\\+([0-9.,]+)\\$");
    private static final Pattern ACTIONBAR_XP_PATTERN = Pattern.compile("\\+([0-9.,]+)\\s*XP"); // NEU: Regex für XP

    @Override
    public void onInitializeClient() {
        RtpDataManager.loadHistory();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String cleanText = message.getString();
            
            if (cleanText.contains("zur Warteschlange hinzugefügt") && cleanText.contains("Du wurdest für das Biom")) {
                RtpDataManager.addRtpPosition();
                if (RtpDataManager.debugMode) sendMessage("§6[Tracker] §aRTP erfasst! (/rtpmenu)");
            }
            
            if (!overlay && cleanText.contains("Du hast deinen Lohn von") && cleanText.contains("erhalten!")) {
                try {
                    int startIdx = cleanText.indexOf("Lohn von ") + 9;
                    int endIdx = cleanText.indexOf("$ erhalten");
                    if (startIdx > 8 && endIdx > startIdx) {
                        String wageStr = cleanText.substring(startIdx, endIdx).trim();
                        wageStr = wageStr.replace(".", "").replace(",", ".");
                        double wage = Double.parseDouble(wageStr);
                        RtpDataManager.processChatPayoutStats(LocalDate.now().format(GERMAN_DATE), wage);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }

            // Actionbar-Tracker (Geld & XP)
            if (overlay) {
                String heute = LocalDate.now().format(GERMAN_DATE);

                // Geld auslesen
                Matcher moneyMatcher = ACTIONBAR_MONEY_PATTERN.matcher(cleanText);
                if (moneyMatcher.find()) {
                    try {
                        String moneyStr = moneyMatcher.group(1).replace(".", "").replace(",", ".");
                        RtpDataManager.addJobWage(heute, Double.parseDouble(moneyStr));
                    } catch (Exception ignored) {}
                }

                // NEU: XP auslesen
                Matcher xpMatcher = ACTIONBAR_XP_PATTERN.matcher(cleanText);
                if (xpMatcher.find()) {
                    try {
                        String xpStr = xpMatcher.group(1).replace(".", "").replace(",", ".");
                        RtpDataManager.addJobXp(heute, Double.parseDouble(xpStr));
                    } catch (Exception ignored) {}
                }
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
            );
        });
    }

    private void sendMessage(String text) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal(text), false);
        }
    }
}
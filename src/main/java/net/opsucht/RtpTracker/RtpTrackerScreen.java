package net.opsucht.RtpTracker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class RtpTrackerScreen extends Screen {
    private final int boxWidth = 420; // Von 370 auf 420 erhöht, um Überlappungen zu verhindern
    private final int boxHeight = 240;
    private int startX, startY;
    private int currentPage = 0;
    private final int entriesPerPage = 5;
    private final List<String> dateKeys = new ArrayList<>();
    
    private static String selectedDateKey = null;

    public RtpTrackerScreen() { super(Text.literal("RTP Ausgaben-Historie")); }

    @Override
    protected void init() {
        RtpDataManager.loadHistory();
        dateKeys.clear();
        dateKeys.addAll(RtpDataManager.rtpHistory.keySet());
        
        if (selectedDateKey == null || !dateKeys.contains(selectedDateKey)) {
            if (!dateKeys.isEmpty()) selectedDateKey = dateKeys.get(dateKeys.size() - 1);
        }
        
        this.startX = (this.width - boxWidth) / 2;
        this.startY = (this.height - boxHeight) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("<-"), b -> { if (currentPage > 0) { currentPage--; this.clearAndInit(); } }).dimensions(startX + 10, startY + boxHeight - 20, 30, 14).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Schließen"), b -> this.close()).dimensions(startX + (boxWidth - 70) / 2, startY + boxHeight - 20, 70, 14).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("->"), b -> { if ((currentPage + 1) * entriesPerPage < dateKeys.size()) { currentPage++; this.clearAndInit(); } }).dimensions(startX + boxWidth - 40, startY + boxHeight - 20, 30, 14).build());

        List<String> reverseKeys = new ArrayList<>();
        for (int i = dateKeys.size() - 1; i >= 0; i--) reverseKeys.add(dateKeys.get(i));

        int startIndex = currentPage * entriesPerPage;
        int visibleEnd = Math.min(startIndex + entriesPerPage, reverseKeys.size());
        int buttonY = startY + 47;
        
        for (int i = startIndex; i < visibleEnd; i++) {
            final String date = reverseKeys.get(i);
            
            this.addDrawableChild(ButtonWidget.builder(Text.literal("*"), b -> { selectedDateKey = date; this.clearAndInit(); }).dimensions(startX + 8, buttonY, 11, 11).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> { RtpDataManager.DayData d = RtpDataManager.rtpHistory.get(date); if (d != null && d.count > 0) { d.count--; RtpDataManager.saveHistory(); this.clearAndInit(); } }).dimensions(startX + 102, buttonY, 11, 11).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> { RtpDataManager.DayData d = RtpDataManager.rtpHistory.get(date); if (d != null) { d.count++; RtpDataManager.saveHistory(); this.clearAndInit(); } }).dimensions(startX + 128, buttonY, 11, 11).build());
            
            buttonY += 14;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(startX, startY, startX + boxWidth, startY + boxHeight, 0xF5101010);
        context.fill(startX - 1, startY - 1, startX + boxWidth + 1, startY, 0xFFD4AF37);
        context.fill(startX - 1, startY + boxHeight, startX + boxWidth + 1, startY + boxHeight + 1, 0xFFD4AF37);
        context.fill(startX - 1, startY, startX, startY + boxHeight, 0xFFD4AF37);
        context.fill(startX + boxWidth, startY, startX + boxWidth + 1, startY + boxHeight, 0xFFD4AF37);

        Text titleText = Text.literal("§6§lOPSucht RTP Buchhaltung");
        int titleWidth = this.textRenderer.getWidth(titleText);
        context.drawText(textRenderer, titleText, startX + (boxWidth - titleWidth) / 2, startY + 10, 0xFFFFFFFF, true);
        
        int totalPages = (int) Math.ceil((double) dateKeys.size() / entriesPerPage);
        if (totalPages == 0) totalPages = 1;
        String pageStr = "§7" + (currentPage + 1) + "/" + totalPages;
        context.drawText(textRenderer, Text.literal(pageStr), startX + boxWidth - 35, startY + 10, 0xFFFFFFFF, true);

        // Neu ausgerichtete Spalten-X-Koordinaten für maximale Beinfreiheit
        context.drawText(textRenderer, Text.literal("§e§nDatum"), startX + 22, startY + 30, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§e§nRTP"), startX + 113, startY + 30, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§e§nLohn"), startX + 165, startY + 30, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§e§nBilanz"), startX + 265, startY + 30, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§e§nJob-XP"), startX + 360, startY + 30, 0xFFFFFFFF, true);
        context.fill(startX + 10, startY + 42, startX + boxWidth - 10, startY + 43, 0x44FFFFFF);

        long totalRtpCount = 0;
        double totalWage = 0;
        double totalXp = 0;
        for (RtpDataManager.DayData d : RtpDataManager.rtpHistory.values()) {
            totalRtpCount += d.count;
            totalWage += d.wage;
            totalXp += d.xp;
        }
        double totalBilanz = totalWage - (totalRtpCount * 25000);

        List<String> reverseKeys = new ArrayList<>();
        for (int i = dateKeys.size() - 1; i >= 0; i--) reverseKeys.add(dateKeys.get(i));

        int startIndex = currentPage * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, reverseKeys.size());
        int y = startY + 48;
        
        for (int i = startIndex; i < endIndex; i++) {
            String date = reverseKeys.get(i);
            RtpDataManager.DayData d = RtpDataManager.rtpHistory.get(date);
            double bilanz = d.wage - (d.count * 25000);
            
            String prefix = date.equals(selectedDateKey) ? "§6§l>" : "  ";
            String dateColor = date.equals(selectedDateKey) ? "§6" : "§f";

            context.drawText(textRenderer, Text.literal(prefix + dateColor + date.substring(0, 5)), startX + 22, y, 0xFFFFFFFF, true);
            context.drawText(textRenderer, Text.literal("§e" + d.count), startX + 115, y, 0xFFFFFFFF, true);
            context.drawText(textRenderer, Text.literal("§2" + String.format("%,.1f", d.wage) + "$"), startX + 165, y, 0xFFFFFFFF, true);
            context.drawText(textRenderer, Text.literal((bilanz >= 0 ? "§a+" : "§c") + String.format("%,.1f", bilanz) + "$"), startX + 265, y, 0xFFFFFFFF, true);
            context.drawText(textRenderer, Text.literal("§b" + String.format("%,.1f", d.xp)), startX + 360, y, 0xFFFFFFFF, true);
            y += 14;
        }

        context.fill(startX + 10, startY + 122, startX + boxWidth - 10, startY + 123, 0x44FFFFFF);
        
        String globalBilanzStr = (totalBilanz >= 0 ? "§a§l+" : "§c§l") + String.format("%,.1f", totalBilanz) + "$";
        context.drawText(textRenderer, Text.literal("§6§lGESAMT:"), startX + 22, startY + 128, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§e" + totalRtpCount + "x"), startX + 115, startY + 128, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§2" + String.format("%,.1f", totalWage) + "$"), startX + 165, startY + 128, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal(globalBilanzStr), startX + 265, startY + 128, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§b" + String.format("%,.1f", totalXp)), startX + 360, startY + 128, 0xFFFFFFFF, true);

        context.fill(startX + 10, startY + 142, startX + boxWidth - 10, startY + 143, 0x44FFFFFF);
        String maxPayoutStr = "§b§lHöchstes Payout: §3" + String.format("%,.2f", RtpDataManager.highestPayout) + "$";
        context.drawText(textRenderer, Text.literal(maxPayoutStr), startX + 22, startY + 148, 0xFFFFFFFF, true);

        context.fill(startX + 10, startY + 162, startX + boxWidth - 10, startY + 163, 0x44FFFFFF);
        double avgPayout = RtpDataManager.livePayoutCount > 0 ? RtpDataManager.livePayoutSum / RtpDataManager.livePayoutCount : 0.0;
        String avgPayoutStr = "§b§lDurchschnitts-Payout: §3" + String.format("%,.2f", avgPayout) + "$";
        context.drawText(textRenderer, Text.literal(avgPayoutStr), startX + 22, startY + 168, 0xFFFFFFFF, true);

        super.render(context, mouseX, mouseY, delta);
    }
}

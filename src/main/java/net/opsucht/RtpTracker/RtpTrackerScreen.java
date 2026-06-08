package net.opsucht.RtpTracker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class RtpTrackerScreen extends Screen {
    private final int boxWidth = 350;
    private final int boxHeight = 240; // Erhöht für die Rekordzeile
    private int startX, startY;
    private int currentPage = 0;
    private final int entriesPerPage = 5;
    private final List<String> dateKeys = new ArrayList<>();
    private TextFieldWidget wageInput;
    
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
            this.addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> { RtpDataManager.DayData d = RtpDataManager.rtpHistory.get(date); if (d != null && d.count > 0) { d.count--; RtpDataManager.saveHistory(); this.clearAndInit(); } }).dimensions(startX + 112, buttonY, 11, 11).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> { RtpDataManager.DayData d = RtpDataManager.rtpHistory.get(date); if (d != null) { d.count++; RtpDataManager.saveHistory(); this.clearAndInit(); } }).dimensions(startX + 140, buttonY, 11, 11).build());
            
            buttonY += 14;
        }

        this.wageInput = this.addDrawableChild(new TextFieldWidget(textRenderer, startX + 115, startY + 192, 120, 15, Text.literal("")));
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Eintragen"), b -> {
            if (selectedDateKey != null) {
                try {
                    double val = Double.parseDouble(wageInput.getText().replace(".", "").replace(",", "."));
                    RtpDataManager.addJobWage(selectedDateKey, val);
                    wageInput.setText("");
                    this.clearAndInit();
                } catch (Exception e) { wageInput.setText("Fehler!"); }
            }
        }).dimensions(startX + 242, startY + 191, 65, 16).build());
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

        context.drawText(textRenderer, Text.literal("§e§nDatum (Zeit)"), startX + 22, startY + 30, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§e§nRTP"), startX + 125, startY + 30, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§e§nLohn"), startX + 195, startY + 30, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§e§nBilanz"), startX + 275, startY + 30, 0xFFFFFFFF, true);
        context.fill(startX + 10, startY + 42, startX + boxWidth - 10, startY + 43, 0x44FFFFFF);

        long totalRtpCount = 0;
        double totalWage = 0;
        for (RtpDataManager.DayData d : RtpDataManager.rtpHistory.values()) {
            totalRtpCount += d.count;
            totalWage += d.wage;
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
            String hoursStr = String.format("§7(%.1fh)", d.playtimeMinutes / 60.0).replace(",", ".");

            context.drawText(textRenderer, Text.literal(prefix + dateColor + date.substring(0, 5) + " " + hoursStr), startX + 22, y, 0xFFFFFFFF, true);
            context.drawText(textRenderer, Text.literal("§e" + d.count), startX + 127, y, 0xFFFFFFFF, true);
            context.drawText(textRenderer, Text.literal("§2" + String.format("%,d", (long) d.wage) + "$"), startX + 195, y, 0xFFFFFFFF, true);
            context.drawText(textRenderer, Text.literal((bilanz >= 0 ? "§a+" : "§c") + String.format("%,d", (long) bilanz) + "$"), startX + 275, y, 0xFFFFFFFF, true);
            y += 14;
        }

        context.fill(startX + 10, startY + 122, startX + boxWidth - 10, startY + 123, 0x44FFFFFF);
        
        String globalBilanzStr = (totalBilanz >= 0 ? "§a§l+" : "§c§l") + String.format("%,d", (long) totalBilanz) + "$";
        context.drawText(textRenderer, Text.literal("§6§lGESAMT:"), startX + 22, startY + 128, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§e" + totalRtpCount + "x"), startX + 127, startY + 128, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("§2" + String.format("%,d", (long) totalWage) + "$"), startX + 195, startY + 128, 0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal(globalBilanzStr), startX + 275, startY + 128, 0xFFFFFFFF, true);

        // NEU: Trennlinie & Zeile für den All-Time-Lohnrekord (Ungerundet!)
        context.fill(startX + 10, startY + 142, startX + boxWidth - 10, startY + 143, 0x44FFFFFF);
        String maxPayoutStr = "§b§lHöchstes Payout: §3" + String.format("%,.2f", RtpDataManager.highestPayout) + "$";
        context.drawText(textRenderer, Text.literal(maxPayoutStr), startX + 22, startY + 148, 0xFFFFFFFF, true);

        context.fill(startX + 10, startY + 162, startX + boxWidth - 10, startY + 163, 0x44FFFFFF);

        String activeEditDate = selectedDateKey != null ? selectedDateKey.substring(0, 5) : "--";
        context.drawText(textRenderer, Text.literal("§eLohn für [" + activeEditDate + "]:"), startX + 12, startY + 196, 0xFFFFFFFF, true);

        super.render(context, mouseX, mouseY, delta);
    }
}

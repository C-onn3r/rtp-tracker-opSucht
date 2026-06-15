#!/bin/bash

# ==============================================================================
# CONFIGURATION / EINSTELLUNGEN
# ==============================================================================
# Trage hier den absoluten Pfad zu deiner rtp_tracker_history.txt ein
FILE_PATH="/home/DEIN_LINUX_NUTZERNAME/.minecraft/config/rtp_tracker_history.txt"

# Füge hier deinen Discord-Webhook-Link ein
WEBHOOK_URL="HIER_DEINEN_DISCORD_WEBHOOK_EINFÜGEN"
# ==============================================================================

# Erzwinge Standard-Zahlenformatierung für awk (Locale-Schutz)
export LC_ALL=C

# Prüfen, ob die Datei existiert
if [ ! -f "$FILE_PATH" ]; then
    echo "Fehler: Datei '$FILE_PATH' wurde nicht gefunden!"
    exit 1
fi

# Prüfen, ob der Webhook eingetragen wurde
if [[ "$WEBHOOK_URL" == "HIER_DEINEN_DISCORD_WEBHOOK_EINFÜGEN" || -z "$WEBHOOK_URL" ]]; then
    echo "Fehler: Bitte konfiguriere zuerst deine WEBHOOK_URL im Skript!"
    exit 1
fi

# Aktuelle Uhrzeit für den Sende-Zeitstempel holen
SEND_TIME=$(date +"%H:%M")

# ==========================================
# VERARBEITUNG & FORMATIERUNG PER AWK
# ==========================================
PAYLOAD=$(awk -v title="📆 RTP-Statistik vom " -v send_time="$SEND_TIME" -v color="3447003" '
# Hilfsfunktion für deutsche Fließkommazahlen (z.B. 1.234.567,8)
function format_de(num,    is_neg, int_part, dec_part, res, len, i, c, parts) {
    num = sprintf("%.1f", num)
    is_neg = (num ~ /^-/) ? "-" : ""
    if (is_neg) num = substr(num, 2)
    split(num, parts, ".")
    int_part = parts[1]
    dec_part = parts[2]
    res = ""
    len = length(int_part)
    c = 0
    for (i = len; i > 0; i--) {
        c++
        res = substr(int_part, i, 1) res
        if (c % 3 == 0 && i > 1) res = "." res
    }
    return is_neg res "," dec_part
}

# Hilfsfunktion für deutsche Ganzzahlen (z.B. 1.234.567)
function format_int_de(num,    is_neg, res, len, i, c) {
    num = sprintf("%.0f", num)
    is_neg = (num ~ /^-/) ? "-" : ""
    if (is_neg) num = substr(num, 2)
    res = ""
    len = length(num)
    c = 0
    for (i = len; i > 0; i--) {
        c++
        res = substr(num, i, 1) res
        if (c % 3 == 0 && i > 1) res = "." res
    }
    return is_neg res
}

BEGIN { FS=","; TOTAL_RTP=0; TOTAL_LOHN=0; TOTAL_XP=0 }

# Filter: Überspringe Zeilen, die nicht mit einem Datum/Zahl beginnen (z.B. META)
$1 !~ /^[0-9]/ || NF < 4 { next }

{
    # Whitespaces und Zeilenumbrüche entfernen
    gsub(/[\r\n ]/, "", $2)
    gsub(/[\r\n ]/, "", $3)
    gsub(/[\r\n ]/, "", $4)
    
    datum = $1
    rtp = $2 + 0
    lohn = $3 + 0
    xp = $4 + 0
    
    # All-Time Summen bilden
    TOTAL_RTP += rtp
    TOTAL_LOHN += lohn
    TOTAL_XP += xp
}

END {
    # 1. HEUTE BERECHNUNGEN (Letzte eingelesene Zeile)
    rtp_kosten = rtp * 25000
    bilanz = lohn - rtp_kosten
    
    rtp_kosten_fmt = (rtp_kosten > 0 ? "-" : "") format_int_de(rtp_kosten) "$"
    lohn_fmt = format_de(lohn) "$"
    bilanz_fmt = (bilanz >= 0 ? "+" : "") format_de(bilanz) "$"
    xp_fmt = format_de(xp)
    
    # 2. ALL-TIME BERECHNUNGEN
    total_rtp_kosten = TOTAL_RTP * 25000
    total_bilanz = TOTAL_LOHN - total_rtp_kosten
    
    total_rtp_fmt = format_int_de(TOTAL_RTP)
    total_rtp_kosten_fmt = (total_rtp_kosten > 0 ? "-" : "") format_int_de(total_rtp_kosten) "$"
    total_lohn_fmt = format_de(TOTAL_LOHN) "$"
    total_bilanz_fmt = (total_bilanz >= 0 ? "+" : "") format_de(total_bilanz) "$"
    total_xp_fmt = format_de(TOTAL_XP)
    
    # Discord Embed Felder zusammenbauen
    field_heute = "🔄 **RTPs:** " rtp " (" rtp_kosten_fmt ")\\n💰 **Lohn:** " lohn_fmt "\\n📊 **Bilanz:** " bilanz_fmt "\\n✨ **Job-XP:** " xp_fmt
    field_gesamt = "🔄 **RTPs gesamt:** " total_rtp_fmt " (" total_rtp_kosten_fmt ")\\n💰 **Lohn gesamt:** " total_lohn_fmt "\\n📈 **Bilanz gesamt:** " total_bilanz_fmt "\\n✨ **Job-XP gesamt:** " total_xp_fmt
    
    full_title = title datum " (" send_time " Uhr)"
    
    # JSON Payload ausgeben
    printf "{\n  \"embeds\": [\n    {\n      \"title\": \"%s\",\n      \"color\": %s,\n      \"fields\": [\n        {\n          \"name\": \"─── HEUTE ───\",\n          \"value\": \"%s\",\n          \"inline\": false\n        },\n        {\n          \"name\": \"─── ALL-TIME HISTORIE ───\",\n          \"value\": \"%s\",\n          \"inline\": false\n        }\n      ],\n      \"footer\": {\n        \"text\": \"Stündliches automatisches Update\"\n      }\n    }\n  ]\n}\n", full_title, color, field_heute, field_gesamt
}
' "$FILE_PATH")

# JSON an Discord senden
curl -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$WEBHOOK_URL"
